package com.nos.systems;

import com.nos.data.Tuning;
import com.nos.entities.Hero;
import com.nos.world.MirrorPair;

/** Converte o olhar em equilíbrio; o espelho aplica as velocidades. */
public final class GazeTimeSystem {
    public enum Mode { PROXIMITY, HORIZONTAL }

    private Mode mode = Mode.PROXIMITY;

    public Mode getMode() { return mode; }

    public void toggleMode() {
        mode = mode == Mode.PROXIMITY ? Mode.HORIZONTAL : Mode.PROXIMITY;
    }

    public float proximityBalance(float x, float y, float ax, float ay, float bx, float by) {
        double distanceA = Math.hypot((double) x - ax, (double) y - ay);
        double distanceB = Math.hypot((double) x - bx, (double) y - by);
        double sum = distanceA + distanceB;
        if (!Double.isFinite(sum) || sum <= Tuning.GAZE_DISTANCE_EPSILON) return 0f;
        return (float) ((distanceB - distanceA) / sum);
    }

    public float horizontalBalance(float x, float width) {
        if (!Float.isFinite(x) || !Float.isFinite(width) || width <= 0f) return 0f;
        return 1f - 2f * Math.max(0f, Math.min(1f, x / width));
    }

    public void apply(MirrorPair pair, float x, float y, boolean active, float width) {
        if (!active) {
            pair.setBalance(0f);
        } else if (mode == Mode.HORIZONTAL) {
            pair.setBalance(horizontalBalance(x, width));
        } else {
            Hero a = pair.getWorldA().getHero();
            Hero b = pair.getWorldB().getHero();
            pair.setBalance(proximityBalance(x, y, a.getX(), a.getY(), b.getX(), b.getY()));
        }
    }
}
