package com.nos.world;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.nos.data.Tuning;

/** Spline aberta com tabela de distâncias para manter a velocidade uniforme. */
public final class Trail {
    private final CatmullRomSpline<Vector2> spline;
    private final float[] distances = new float[Tuning.TRAIL_SAMPLES + 1];

    public Trail(float[][] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("A trilha precisa de pelo menos dois pontos.");
        }
        Vector2[] controls = new Vector2[points.length + 2];
        for (int i = 0; i < points.length; i++) {
            if (points[i] == null || points[i].length != 2
                || !Float.isFinite(points[i][0]) || !Float.isFinite(points[i][1])) {
                throw new IllegalArgumentException("Cada ponto deve conter duas coordenadas finitas.");
            }
            controls[i + 1] = new Vector2(points[i][0], points[i][1]);
        }
        controls[0] = controls[1].cpy();
        controls[controls.length - 1] = controls[controls.length - 2].cpy();
        spline = new CatmullRomSpline<>(controls, false);
        Vector2 previous = new Vector2();
        Vector2 current = new Vector2();
        spline.valueAt(previous, 0f);
        for (int i = 1; i < distances.length; i++) {
            spline.valueAt(current, i / (float) Tuning.TRAIL_SAMPLES);
            distances[i] = distances[i - 1] + previous.dst(current);
            previous.set(current);
        }
        if (!(getLength() > 0f) || !Float.isFinite(getLength())) {
            throw new IllegalArgumentException("A trilha deve ter comprimento positivo e finito.");
        }
    }

    public float getLength() {
        return distances[distances.length - 1];
    }

    public Vector2 positionAtDistance(float distance, Vector2 out) {
        float target = Math.max(0f, Math.min(getLength(), distance));
        int low = 0;
        int high = distances.length - 1;
        while (low + 1 < high) {
            int mid = (low + high) / 2;
            if (distances[mid] < target) low = mid;
            else high = mid;
        }
        float span = distances[high] - distances[low];
        float fraction = span > 0f ? (target - distances[low]) / span : 0f;
        return spline.valueAt(out, (low + fraction) / Tuning.TRAIL_SAMPLES);
    }
}
