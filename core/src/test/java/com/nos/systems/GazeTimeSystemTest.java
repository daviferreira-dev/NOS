package com.nos.systems;

import com.nos.world.MirrorPair;
import com.nos.world.Trail;
import org.junit.Test;

import static org.junit.Assert.*;

/** Verifica o controle temporal sem contexto OpenGL. */
public class GazeTimeSystemTest {
    private final GazeTimeSystem gaze = new GazeTimeSystem();

    private MirrorPair createPair() {
        return new MirrorPair(
            new Trail(new float[][] {{0,0}, {1000,0}}),
            new Trail(new float[][] {{0,100}, {1000,100}}));
    }

    @Test public void lookingAtAProducesPositiveBalance() {
        assertEquals(1f, gaze.proximityBalance(0, 0, 0, 0, 0, 100), .00001f);
    }

    @Test public void lookingAtBProducesNegativeBalance() {
        assertEquals(-1f, gaze.proximityBalance(0, 100, 0, 0, 0, 100), .00001f);
    }

    @Test public void equalDistancesAndCoincidentPositionsAreNeutral() {
        assertEquals(0f, gaze.proximityBalance(0, 50, 0, 0, 0, 100), 0f);
        assertEquals(0f, gaze.proximityBalance(10, 20, 10, 20, 10, 20), 0f);
    }

    @Test public void nonFiniteCoordinatesDoNotPoisonWorldClocks() {
        assertEquals(0f, gaze.proximityBalance(Float.NaN, 0, 0, 0, 0, 100), 0f);
        assertEquals(0f, gaze.proximityBalance(Float.POSITIVE_INFINITY, 0, 0, 0, 0, 100), 0f);
        assertEquals(0f, gaze.horizontalBalance(Float.NaN, 960), 0f);
    }

    @Test public void horizontalReadingHasNeutralCenterAndClampedEdges() {
        assertEquals(1f, gaze.horizontalBalance(-100, 960), 0f);
        assertEquals(0f, gaze.horizontalBalance(480, 960), 0f);
        assertEquals(-1f, gaze.horizontalBalance(1100, 960), 0f);
        assertEquals(0f, gaze.horizontalBalance(100, 0), 0f);
    }

    @Test public void modeToggleIsReversible() {
        assertEquals(GazeTimeSystem.Mode.PROXIMITY, gaze.getMode());
        gaze.toggleMode();
        assertEquals(GazeTimeSystem.Mode.HORIZONTAL, gaze.getMode());
        gaze.toggleMode();
        assertEquals(GazeTimeSystem.Mode.PROXIMITY, gaze.getMode());
    }

    @Test public void observedWorldSlowsAndOtherWorldSpeedsUp() {
        MirrorPair pair = createPair();
        gaze.apply(pair, 0, 0, true, 960);
        pair.update(1f);
        assertEquals(.2f, pair.getWorldA().getSpeed(), .00001f);
        assertEquals(1.8f, pair.getWorldB().getSpeed(), .00001f);
        assertTrue(pair.getWorldA().getHero().getDistance() < pair.getWorldB().getHero().getDistance());
    }

    @Test public void inactiveInputRestoresNeutralSpeeds() {
        MirrorPair pair = createPair();
        gaze.apply(pair, 0, 0, true, 960);
        gaze.apply(pair, 0, 0, false, 960);
        assertEquals(1f, pair.getWorldA().getSpeed(), 0f);
        assertEquals(1f, pair.getWorldB().getSpeed(), 0f);
    }

    @Test public void horizontalModeIgnoresHeroDistances() {
        MirrorPair pair = createPair();
        gaze.toggleMode();
        gaze.apply(pair, 960, 0, true, 960);
        assertEquals(1.8f, pair.getWorldA().getSpeed(), .00001f);
        assertEquals(.2f, pair.getWorldB().getSpeed(), .00001f);
    }

    @Test public void speedSumRemainsConstantAcrossBothReadings() {
        MirrorPair pair = createPair();
        for (int mode = 0; mode < 2; mode++) {
            for (int x = 0; x <= 960; x++) {
                gaze.apply(pair, x, 25, true, 960);
                assertEquals(2f, pair.getWorldA().getSpeed() + pair.getWorldB().getSpeed(), .00001f);
                assertTrue(pair.getWorldA().getSpeed() > 0f);
                assertTrue(pair.getWorldB().getSpeed() > 0f);
            }
            gaze.toggleMode();
        }
    }
}
