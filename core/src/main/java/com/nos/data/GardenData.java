package com.nos.data;

/** Planta opcional dos contrapesos do pátio; retângulos usam x, y, largura e altura. */
public final class GardenData {
    public float[] cartStart, helperPlate, weightPlate, gate;
    public float[] checkpointShadow, checkpointReflection;
    public float[][] barriers;
    public float[][] helperCable,weightCable;
    /** Na segunda travessia, o mesmo peso abre uma passagem antes de liberar a saída. */
    public float[] transferPlate,transferGate;
    public float[][] transferCable;
}
