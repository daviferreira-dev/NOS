package com.nos.data;

/** Planta do encontro, alinhada ao cenário da ponte. */
public final class BridgeData {
    public float width,height;
    public float[] ground,river,bridge,winch,latch,shadowStart,reflectionStart,exit;
    public float[][] obstacles;
    public GardenData garden;
    public RelayData relay;
    public int phaseNumber=1;
    public boolean gardenOnly;
    public String backgroundPath;
    public String destinationKey="bridgeGardenLabel";
}
