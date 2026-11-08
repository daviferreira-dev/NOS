package com.nos.systems;

/** Pontuação provisória da primeira fase, premiando sincronia e harmonia. */
public final class ScoreSystem {
    public int calculate(float arrivalGap, float averageStrain, int hits, int encounters) {
        float base = Math.max(0, 1000 - arrivalGap * 100) + (1 - averageStrain) * 500 + Math.max(0, 6 - hits) * 100;
        return Math.round(base * (1 + encounters * .25f));
    }
}
