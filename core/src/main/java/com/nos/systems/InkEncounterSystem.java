package com.nos.systems;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.nos.data.Tuning;
import com.nos.world.MirrorPair;
import com.nos.world.Trail;

/** Pré-calcula cruzamentos das splines e registra encontros não repetíveis. */
public final class InkEncounterSystem {
    public static final class Crossing {
        public final Vector2 position;
        public final float distanceA, distanceB;
        public float timeA = -1, timeB = -1;
        public boolean collected;
        Crossing(Vector2 point, float a, float b) { position = point.cpy(); distanceA = a; distanceB = b; }
    }
    private final Array<Crossing> crossings = new Array<>();
    public InkEncounterSystem(MirrorPair pair) {
        Trail a = pair.getWorldA().getTrail(), b = pair.getWorldB().getTrail();
        Vector2 a0 = new Vector2(), a1 = new Vector2(), b0 = new Vector2(), b1 = new Vector2(), point = new Vector2();
        int samples = 100;
        for (int i = 0; i < samples; i++) {
            a.positionAtDistance(a.getLength() * i / samples, a0);
            a.positionAtDistance(a.getLength() * (i + 1) / samples, a1);
            for (int j = 0; j < samples; j++) {
                b.positionAtDistance(b.getLength() * j / samples, b0);
                b.positionAtDistance(b.getLength() * (j + 1) / samples, b1);
                if (!Intersector.intersectSegments(a0, a1, b0, b1, point)) continue;
                float da = a.getLength() * (i + a0.dst(point) / Math.max(.001f, a0.dst(a1))) / samples;
                float db = b.getLength() * (j + b0.dst(point) / Math.max(.001f, b0.dst(b1))) / samples;
                boolean duplicate = false;
                for (Crossing old : crossings) if (Math.abs(old.distanceA - da) < 30 && Math.abs(old.distanceB - db) < 30) duplicate = true;
                if (!duplicate) crossings.add(new Crossing(point, da, db));
            }
        }
    }
    public Array<Crossing> getCrossings() { return crossings; }
    public void resetTimings() { for (Crossing c : crossings) { c.timeA = -1; c.timeB = -1; } }
    public int update(MirrorPair pair, float beforeA, float beforeB, float time) {
        int collected = 0;
        for (Crossing c : crossings) {
            if (c.collected) continue;
            if (c.timeA < 0 && beforeA <= c.distanceA && pair.getWorldA().getHero().getDistance() >= c.distanceA) c.timeA = time;
            if (c.timeB < 0 && beforeB <= c.distanceB && pair.getWorldB().getHero().getDistance() >= c.distanceB) c.timeB = time;
            if (c.timeA >= 0 && c.timeB >= 0 && Math.abs(c.timeA - c.timeB) <= Tuning.ENCOUNTER_WINDOW) {
                c.collected = true;
                collected++;
            }
        }
        return collected;
    }
}
