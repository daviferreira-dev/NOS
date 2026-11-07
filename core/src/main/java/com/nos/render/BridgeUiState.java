package com.nos.render;

import com.nos.input.BridgeKeyBindings;
import com.nos.world.BridgeEncounter;

/** Estado de apresentação e acessibilidade, separado da simulação do encontro. */
public final class BridgeUiState {
    public final BridgeKeyBindings bindings=new BridgeKeyBindings();
    public float textScale=1;
    public boolean settings,awaitingKey;
    public int selectedAction;
    public String bindingMessage="bridgeBindingsIntro";
    private float playingTime,objectiveAge;
    private String objective;
    public void update(BridgeEncounter encounter,float delta) {
        String current=encounter.objectiveKey();
        if(!current.equals(objective)) { objective=current;objectiveAge=0; }
        if(encounter.getState()==BridgeEncounter.State.PLAYING) { playingTime+=delta;objectiveAge+=delta; }
    }
    public boolean expandedObjective() { return objectiveAge<7; }
    public boolean fullShortcuts() { return playingTime<45; }
    public void resetPresentation() { playingTime=0;objectiveAge=0;objective=null; }
    public void cycleTextScale() { textScale=textScale<1.1f?1.15f:textScale<1.25f?1.3f:1; }
}
