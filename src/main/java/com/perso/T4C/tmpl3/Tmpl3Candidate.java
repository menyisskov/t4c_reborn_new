package com.perso.T4C.tmpl3;

public final class Tmpl3Candidate {
    private final Tmpl3Mask mask;
    private final double terrainScore;
    private final double compatibilityScore;
    private final double finalScore;

    public Tmpl3Candidate(Tmpl3Mask mask, double terrainScore) {
        this(mask, terrainScore, 0d, terrainScore);
    }

    public Tmpl3Candidate(Tmpl3Mask mask, double terrainScore, double compatibilityScore, double finalScore) {
        this.mask = mask;
        this.terrainScore = terrainScore;
        this.compatibilityScore = compatibilityScore;
        this.finalScore = finalScore;
    }

    public Tmpl3Mask mask() {
        return mask;
    }

    public int id() {
        return mask.id();
    }

    public double terrainScore() {
        return terrainScore;
    }

    public double compatibilityScore() {
        return compatibilityScore;
    }

    public double finalScore() {
        return finalScore;
    }

    public Tmpl3Candidate withCompatibility(double compatibilityScore, double finalScore) {
        return new Tmpl3Candidate(mask, terrainScore, compatibilityScore, finalScore);
    }
}
