package com.nos.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.nos.data.LevelData;
import com.nos.data.Tuning;
import com.nos.entities.Hero;

/** Verificações sem OpenGL e sem dependências adicionais de teste. */
public final class MovementChecks {
    private static void near(double actual, double expected, double tolerance) {
        if (Math.abs(actual - expected) > tolerance) {
            throw new AssertionError(actual + " != " + expected);
        }
    }

    public static void main(String[] args) {
        LevelData data = new Json().fromJson(LevelData.class, new FileHandle(args[0]));
        Trail trailA = new Trail(data.trailA);
        Trail trailB = new Trail(data.trailB);
        Vector2 point = new Vector2();
        trailA.positionAtDistance(0, point);
        near(point.x, data.trailA[0][0], .001);
        near(point.y, data.trailA[0][1], .001);
        trailA.positionAtDistance(trailA.getLength(), point);
        near(point.x, data.trailA[data.trailA.length - 1][0], .001);
        near(point.y, data.trailA[data.trailA.length - 1][1], .001);

        MirrorPair pair = new MirrorPair(trailA, trailB);
        pair.setBalance(1f);
        pair.update(1f);
        near(pair.getWorldA().getHero().getDistance(), Tuning.HERO_SPEED * .2, .001);
        near(pair.getWorldB().getHero().getDistance(), Tuning.HERO_SPEED * 1.8, .001);
        near(pair.getWorldA().getSpeed() + pair.getWorldB().getSpeed(), 2, .00001);
        float before = pair.getWorldA().getHero().getDistance();
        pair.update(0f);
        near(pair.getWorldA().getHero().getDistance(), before, .00001);

        Hero singleStep = new Hero(trailA);
        Hero manySteps = new Hero(trailA);
        singleStep.update(2f);
        for (int i = 0; i < 120; i++) manySteps.update(1f / 60);
        near(singleStep.getDistance(), manySteps.getDistance(), .001);
        near(singleStep.getX(), manySteps.getX(), .01);
        near(singleStep.getY(), manySteps.getY(), .01);

        // A parametrização por distância evita acelerar em trechos curtos da spline.
        Trail straight = new Trail(new float[][] {{0,0},{100,0},{400,0},{1000,0}});
        straight.positionAtDistance(500f, point);
        near(point.x, 500, .05);
        pair.update(10000f);
        Hero arrived = pair.getWorldA().getHero();
        if (!arrived.hasArrived() || !pair.getWorldB().getHero().hasArrived()) {
            throw new AssertionError("Os dois heróis devem chegar ao fim.");
        }
        float endX = arrived.getX();
        pair.update(10f);
        near(arrived.getX(), endX, .00001);
        near(arrived.getDistance(), trailA.getLength(), .001);
        System.out.println("PASS: JSON, endpoints, world clocks, frame independence, distance mapping and arrival.");
    }
}
