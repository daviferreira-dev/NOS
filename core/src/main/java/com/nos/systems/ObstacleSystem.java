package com.nos.systems;

import com.badlogic.gdx.math.Vector2;
import com.nos.data.LevelData;
import com.nos.data.Tuning;
import com.nos.world.MirrorPair;
import com.nos.world.World;

/** Correntes periódicas: o instante de passagem determina o impacto. */
public final class ObstacleSystem {
    public static final class Gate {
        public final LevelData.GateData data;
        public final float distance;
        public final Vector2 position = new Vector2();
        Gate(LevelData.GateData data, World world) {
            if (data.period <= 0 || data.opening <= 0 || data.opening >= data.period
                || data.progress <= 0 || data.progress >= 1) throw new IllegalArgumentException("Corrente inválida.");
            this.data = data;
            distance = world.getTrail().getLength() * data.progress;
            world.getTrail().positionAtDistance(distance, position);
        }
        public float phase(double time) { return (float) ((time + data.offset) % data.period); }
        public float drift(double time) { return (float)Math.sin((time+data.offset)*Math.PI*2/data.period)*110; }
        public boolean isOpen(double time) { return Math.abs(drift(time)) > 110*Math.cos(Math.PI*data.opening/data.period*.5); }
    }
    private final Gate[] gates;
    public ObstacleSystem(LevelData data, MirrorPair pair) {
        gates = new Gate[data.gates.length];
        for (int i = 0; i < gates.length; i++) {
            LevelData.GateData gate = data.gates[i];
            gates[i] = new Gate(gate, gate.world == 0 ? pair.getWorldA() : pair.getWorldB());
        }
    }
    public Gate[] getGates() { return gates; }
    public int update(MirrorPair pair, float beforeA, float beforeB, float tideTime) {
        int hits = 0;
        for (Gate gate : gates) {
            World world = gate.data.world == 0 ? pair.getWorldA() : pair.getWorldB();
            float before = gate.data.world == 0 ? beforeA : beforeB;
            float after = world.getHero().getDistance();
            if (before < gate.distance && after >= gate.distance && !gate.isOpen(tideTime)) {
                world.getHero().seek(gate.distance - Tuning.HIT_PUSHBACK);
                world.setInk(world.getInk() - Tuning.HIT_INK_COST);
                hits++;
            }
        }
        return hits;
    }
}
