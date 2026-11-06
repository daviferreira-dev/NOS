package com.nos.data;

/** Pontos das trilhas carregados do JSON da fase. */
public final class LevelData {
    public float[][] trailA;
    public float[][] trailB;
    public GateData[] gates = new GateData[0];

    public static final class GateData {
        public int world;
        public float progress;
        public float period;
        public float opening;
        public float offset;
    }
}
