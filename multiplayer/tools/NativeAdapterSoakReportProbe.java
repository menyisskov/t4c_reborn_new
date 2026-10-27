import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Real Windows delete-sharing regression for NativeAdapterSoak's periodic JSON replacement. */
class NativeAdapterSoakReportProbe {
  private static final String OLD="{\"finished\":false,\"generation\":1}";
  private static final String NEW="{\"finished\":true,\"generation\":2}";

  private static final class Lock implements AutoCloseable {
    private final Process process;
    private final Path release;

    Lock(Process process,Path release) { this.process=process;this.release=release; }

    @Override public void close() throws Exception {
      if(!Files.exists(release)) Files.writeString(release,"release",StandardOpenOption.CREATE_NEW);
      if(!process.waitFor(3,TimeUnit.SECONDS)) {
        process.destroyForcibly();
        process.waitFor(3,TimeUnit.SECONDS);
        throw new AssertionError("Owned Windows file-lock helper did not exit");
      }
      require(process.exitValue()==0,"Windows file-lock helper failed");
    }
  }

  private static void require(boolean valid,String message) {
    if(!valid) throw new AssertionError(message);
  }

  private static Lock hold(Path directory,Path script,Path target) throws Exception {
    Path ready=directory.resolve("lock.ready"),release=directory.resolve("lock.release");
    Process process=new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive",
        "-File",script.toString(),target.toString(),
        ready.toString(),release.toString())
        .redirectOutput(directory.resolve("lock.out.log").toFile())
        .redirectError(directory.resolve("lock.err.log").toFile()).start();
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
    while(!Files.exists(ready) && process.isAlive() && System.nanoTime()<deadline)
      Thread.sleep(20);
    if(!Files.exists(ready)) {
      if(process.isAlive()) process.destroyForcibly();
      process.waitFor(3,TimeUnit.SECONDS);
      throw new AssertionError("Windows file-lock helper did not acquire its FileStream");
    }
    return new Lock(process,release);
  }

  private static void assertJson(Path file,boolean finished,int generation) throws Exception {
    var json=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    require(json.get("finished").getAsBoolean()==finished
        && json.get("generation").getAsInt()==generation,"Report JSON changed unexpectedly");
  }

  public static void main(String[] args) throws Exception {
    if(args.length!=0) throw new IllegalArgumentException("No arguments expected");
    if(!System.getProperty("os.name").toLowerCase().contains("win")) {
      System.out.println("SKIP: real FileStream deny-delete regression requires Windows");
      return;
    }
    Path root=Files.createTempDirectory("t4c-0221-report-lock-");
    Path script=root.resolve("hold-report.ps1");
    Files.writeString(script,"""
        param([string]$Target,[string]$Ready,[string]$Release)
        $ErrorActionPreference = 'Stop'
        $stream = [System.IO.FileStream]::new($Target,[System.IO.FileMode]::Open,
            [System.IO.FileAccess]::Read,[System.IO.FileShare]::Read)
        try {
            [System.IO.File]::WriteAllText($Ready,'locked')
            $until = [DateTime]::UtcNow.AddSeconds(8)
            while (-not [System.IO.File]::Exists($Release)) {
                if ([DateTime]::UtcNow -ge $until) { throw 'Lock release timed out' }
                Start-Sleep -Milliseconds 20
            }
        } finally { $stream.Dispose() }
        """,StandardOpenOption.CREATE_NEW);

    Path brief=Files.createDirectory(root.resolve("brief"));
    Path briefReport=brief.resolve("report.json"),briefPending=brief.resolve("report.json.tmp");
    Files.writeString(briefReport,OLD,StandardOpenOption.CREATE_NEW);
    Files.writeString(briefPending,NEW,StandardOpenOption.CREATE_NEW);
    AtomicReference<Throwable> briefFailure=new AtomicReference<>();
    try(Lock lock=hold(brief,script,briefReport)) {
      Thread mover=Thread.startVirtualThread(() -> {
        try { NativeAdapterSoak.replaceReport(briefPending,briefReport); }
        catch(Throwable failure) { briefFailure.set(failure); }
      });
      Thread.sleep(150);
      require(mover.isAlive(),"Locked target did not delay replacement");
      assertJson(briefReport,false,1);
      lock.close();
      mover.join(3000);
      require(!mover.isAlive(),"Transient report replacement exceeded deadline");
    }
    if(briefFailure.get()!=null) throw new AssertionError("Transient lock was not retried",briefFailure.get());
    assertJson(briefReport,true,2);
    require(!Files.exists(briefPending),"Transient replacement left a pending report");

    Path lasting=Files.createDirectory(root.resolve("lasting"));
    Path lastingReport=lasting.resolve("report.json"),lastingPending=lasting.resolve("report.json.tmp");
    Files.writeString(lastingReport,OLD,StandardOpenOption.CREATE_NEW);
    Files.writeString(lastingPending,NEW,StandardOpenOption.CREATE_NEW);
    try(Lock ignored=hold(lasting,script,lastingReport)) {
      long started=System.nanoTime();
      try {
        NativeAdapterSoak.replaceReport(lastingPending,lastingReport);
        throw new AssertionError("Persistent lock was silently accepted");
      } catch(java.nio.file.AccessDeniedException expected) {
        long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-started);
        require(elapsed>=1800 && elapsed<=3000,"Persistent lock was not bounded near two seconds: "+elapsed);
        assertJson(lastingReport,false,1);
        require(Files.exists(lastingPending),"Failed replacement discarded the pending report");
      }
    }
    System.out.println("PASS: transient deny-delete retried; persistent denial bounded; prior JSON preserved. Fixture: "+root);
  }
}
