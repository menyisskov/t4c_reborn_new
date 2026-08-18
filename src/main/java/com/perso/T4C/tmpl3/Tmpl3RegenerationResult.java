package com.perso.T4C.tmpl3;

import java.util.List;

public record Tmpl3RegenerationResult(
    int scannedCount, List<Tmpl3Change> changes, List<Tmpl3Placement> placements) {}
