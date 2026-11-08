package com.nos.data;

/** Trajeto e ciclo legível de um perigo ambiental; phase é um deslocamento em segundos. */
public final class HazardData {
    public String type="ember";
    public float x,y,amplitudeX,amplitudeY,phase;
    public float period=5,radius=24;
    public float warningSeconds=1,activeSeconds=2,restSeconds=2;
    /** Vetor da rajada em unidades por segundo; vetor nulo usa empurrão radial. */
    public float pushX,pushY;
}
