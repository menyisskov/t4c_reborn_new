package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Kill a separate writer JVM at the commit boundary; never target the running game server. */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineStoreCrashTest {
  @TempDir Path directory;

  @ParameterizedTest @ValueSource(booleans={false,true})
  void processCrashCannotPartiallyGrantOrDuplicateGameplayResult(boolean committed) throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var key=store.provisionAccount(); var created=store.create(key.accountId(),"Crash boundary",0,10,20);
    var progress=CharacterProgressTest.sample(); UUID event=UUID.randomUUID();
    Path payload=directory.resolve("progress.json"), signal=directory.resolve("ready"), log=directory.resolve("writer.log");
    Files.writeString(payload,new Gson().toJson(progress));
    String executable=System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
    var builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin",executable).toString(),
        "-cp",System.getProperty("surefire.test.class.path",System.getProperty("java.class.path")),
        CrashWriter.class.getName(),created.id().toString(),event.toString(),payload.toString(),signal.toString(),Boolean.toString(committed));
    // One-use test credentials stay out of command lines and test output.
    builder.environment().put("T4C_TEST_ACCESS_KEY",key.secret());
    var process=builder.redirectErrorStream(true).redirectOutput(log.toFile()).start();
    try {
      long deadline=System.nanoTime()+Duration.ofSeconds(20).toNanos();
      while (!Files.exists(signal) && process.isAlive() && System.nanoTime()<deadline) Thread.sleep(50);
      assertTrue(Files.exists(signal),"Writer did not reach the requested commit boundary");
      process.destroyForcibly(); assertTrue(process.waitFor(10,TimeUnit.SECONDS));
      var recovered=store.load(key.accountId(),created.id());
      assertEquals(committed ? 7 : 0,recovered.gold());
      assertEquals(committed ? 11 : 0,recovered.experience());
      assertEquals(committed ? progress : null,recovered.progress());
      // Deterministically advance only this dead writer's lease; real-time expiry has separate QA.
      try (var c=connection(); var s=c.prepareStatement("UPDATE online_characters SET session_until=clock_timestamp()-interval '1 second' WHERE id=?")) {
        s.setObject(1,created.id()); s.executeUpdate();
      }
      var replacement=store.acquire(key.secret(),created.id());
      var retry=store.commitGameplayEvent(replacement,created.revision(),event,7,11,progress);
      assertEquals(!committed,retry.applied());
      assertEquals(7,retry.character().gold()); assertEquals(11,retry.character().experience());
      assertEquals(progress,retry.character().progress());
    } finally {
      if (process.isAlive()) { process.destroyForcibly(); process.waitFor(10,TimeUnit.SECONDS); }
    }
  }

  private static Connection connection() throws SQLException {
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
  }

  public static final class CrashWriter {
    public static void main(String[] args) throws Exception {
      var session=OnlineStore.fromEnvironment().acquire(System.getenv("T4C_TEST_ACCESS_KEY"),UUID.fromString(args[0]));
      var progress=new Gson().fromJson(Files.readString(Path.of(args[2])),CharacterProgress.class);
      var store=new OnlineStore(() -> {
        Connection actual=connection();
        return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,values) -> {
          try {
            if (method.getName().equals("commit")) {
              if (Boolean.parseBoolean(args[4])) actual.commit();
              Files.writeString(Path.of(args[3]),"ready");
              // Parent forcibly terminates this process before an acknowledgement can return.
              new java.util.concurrent.CountDownLatch(1).await();
            }
            return method.invoke(actual,values);
          } catch (InvocationTargetException error) { throw error.getCause(); }
        });
      });
      store.commitGameplayEvent(session,session.character().revision(),UUID.fromString(args[1]),7,11,progress);
    }
  }
}
