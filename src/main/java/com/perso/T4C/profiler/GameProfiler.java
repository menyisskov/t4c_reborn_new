package com.perso.T4C.profiler;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameProfiler {
  private static final Logger log = LoggerFactory.getLogger(GameProfiler.class);
  private Recording recording;
  private boolean active = false;

  public boolean isActive() {
    return active;
  }

  public String start() {
    if (active) {
      log.warn("Profiler already running");
      return null;
    }
    try {
      Configuration cfg = Configuration.getConfiguration("profile");
      recording = new Recording(cfg);
      recording.enable("T4C.Frame").withoutThreshold();
      recording.enable("T4C.Section").withoutThreshold();
      recording.enable("jdk.GarbageCollection");
      recording.enable("jdk.GCHeapSummary");
      recording.enable("jdk.ObjectAllocationInNewTLAB");
      recording.enable("jdk.ObjectAllocationOutsideTLAB");
      recording.enable("jdk.CPULoad");
      recording.enable("jdk.ThreadCPULoad");
      recording.enable("jdk.ClassLoad");
      String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      Path out = Paths.get("profiler_" + timestamp + ".jfr").toAbsolutePath();
      recording.setDestination(out);
      recording.setToDisk(true);
      recording.start();
      active = true;
      log.info("JFR recording started → {}", out);
      return out.toString();
    } catch (IOException | java.text.ParseException e) {
      log.error("Failed to start JFR recording", e);
      return null;
    }
  }

  public long stop() {
    if (!active || recording == null) return 0;
    try {
      recording.stop();
      recording.close();
      long bytes =
          recording.getDestination() != null ? recording.getDestination().toFile().length() : 0;
      log.info("JFR recording stopped ({} KB)", bytes / 1024);
      return bytes;
    } catch (Exception e) {
      log.error("Failed to stop JFR recording", e);
      return 0;
    } finally {
      active = false;
      recording = null;
    }
  }

  public void toggle(Runnable onStart, Runnable onStop) {
    if (active) {
      stop();
      if (onStop != null) onStop.run();
    } else {
      String path = start();
      if (path != null && onStart != null) onStart.run();
    }
  }
}
