package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HttpRequestAdmissionTest {
  @Test void sessionPermitReleasesItsIdentityAndPublicRequestsHaveASeparateCap() {
    var admission=new HttpRequestAdmission();
    try(var first=admission.session("first");var second=admission.session("first");
        var publicOne=admission.publicRequest();var publicTwo=admission.publicRequest()) {
      assertEquals(503,assertThrows(PreviewWorld.Rejected.class,()->admission.session("first")).status);
      assertEquals(503,assertThrows(PreviewWorld.Rejected.class,admission::publicRequest).status);
      try(var other=admission.session("other");var rosterOne=admission.rosterRequest();
          var rosterTwo=admission.rosterRequest()) {
        assertEquals(2,admission.activeSessions());
        assertEquals(503,assertThrows(PreviewWorld.Rejected.class,admission::rosterRequest).status);
      }
    }
    assertEquals(0,admission.activeSessions(),"No inactive token may remain in the admission map");
    try(var restored=admission.session("first");var available=admission.publicRequest()) {
      assertEquals(1,admission.activeSessions());
    }
  }

  @Test void exchangeCapRejectsWithoutQueueAndReleasesOnCompletion() throws Exception {
    var entered=new CountDownLatch(HttpRequestAdmission.MAX_EXCHANGES);
    var release=new CountDownLatch(1);
    try(var executor=new HttpRequestAdmission.ExchangeExecutor()) {
      try {
        for(int i=0;i<HttpRequestAdmission.MAX_EXCHANGES;i++) executor.execute(() -> {
          entered.countDown();
          try { release.await(); } catch(InterruptedException signal) { Thread.currentThread().interrupt(); }
        });
        assertTrue(entered.await(3,TimeUnit.SECONDS));
        assertEquals(HttpRequestAdmission.MAX_EXCHANGES,executor.active());
        assertThrows(RejectedExecutionException.class,()->executor.execute(()->{}));
      } finally { release.countDown(); }
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);
      while(executor.active()!=0 && System.nanoTime()<deadline) Thread.sleep(10);
      assertEquals(0,executor.active());
      var resumed=new CountDownLatch(1);
      executor.execute(resumed::countDown);
      assertTrue(resumed.await(1,TimeUnit.SECONDS));
    }
  }
}
