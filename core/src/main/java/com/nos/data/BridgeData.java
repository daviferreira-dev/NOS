package com.nos.data;

/** Planta do encontro, alinhada ao cenário da ponte. */
public final class BridgeData {
    public float width,height;
    public float[] ground,river,bridge,winch,latch,shadowStart,reflectionStart,exit;
    public float[][] obstacles;
    public GardenData garden;
    public FurnaceData furnace;
    public RidgeData ridge;
    public ObservatoryData observatory;
    public float[] checkpointShadow,checkpointReflection;
    public RelayData relay;
    public HazardData[] hazards;
    public float[] recoveryPoint;
    public float localRecoveryMargin;
    public int phaseNumber=1;
    public boolean gardenOnly;
    public String backgroundPath;
    public String destinationKey="bridgeGardenLabel";
}
