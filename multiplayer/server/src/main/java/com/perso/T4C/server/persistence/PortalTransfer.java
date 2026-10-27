package com.perso.T4C.server.persistence;

import com.perso.T4C.online.model.CharacterProgress;
import java.util.Objects;

/** Immutable simulation-authored transfer; never deserialize this from a network request. */
public record PortalTransfer(int portalId,String sourceRegion,String destinationRegion,
    CharacterProgress.Respawn source,CharacterProgress.Respawn destination,
    long expectedEpoch,long cooldownUntil) {
  public PortalTransfer {
    Objects.requireNonNull(source);Objects.requireNonNull(destination);
    if(portalId<0 || expectedEpoch<0 || cooldownUntil<0 || sourceRegion==null || sourceRegion.isBlank()
        || destinationRegion==null || destinationRegion.isBlank() || sourceRegion.length()>128 || destinationRegion.length()>128)
      throw new IllegalArgumentException("Invalid portal transfer");
  }
}
