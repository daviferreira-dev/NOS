package com.nos.systems;

import com.nos.data.Tuning;

/** Acumula desequilíbrio sustentado e impactos, sem depender da apresentação. */
public final class HarmonySystem {
    private float strain;
    private float sustained;

    public void update(float delta, float balance, float difficulty) {
        if (isBroken()) return;
        if (Math.abs(balance) > Tuning.HARMONY_THRESHOLD) {
            sustained += delta;
            if (sustained > Tuning.HARMONY_GRACE) strain += delta * Tuning.HARMONY_RISE * difficulty;
        } else {
            sustained = 0;
            strain -= delta * Tuning.HARMONY_RECOVERY;
        }
        strain = Math.max(0, Math.min(1, strain));
    }
    public void hit() { strain = Math.min(1, strain + Tuning.HIT_DAMAGE); }
    public float getStrain() { return strain; }
    public boolean isBroken() { return strain >= 1; }
    public void reset() { strain = 0; sustained = 0; }
}
