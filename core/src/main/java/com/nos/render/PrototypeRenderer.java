package com.nos.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nos.data.Tuning;
import com.nos.entities.Hero;
import com.nos.world.MirrorPair;
import com.nos.world.Trail;
import com.nos.world.World;

/** Desenha somente formas em cinza; não altera os mundos. */
public final class PrototypeRenderer implements Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Vector2 previous = new Vector2();
    private final Vector2 current = new Vector2();

    public void render(MirrorPair pair, Viewport viewport) {
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawTrail(pair.getWorldA().getTrail(), Color.DARK_GRAY);
        drawTrail(pair.getWorldB().getTrail(), Color.GRAY);
        drawHero(pair.getWorldA(), false);
        drawHero(pair.getWorldB(), true);
        shapes.end();
    }

    private void drawTrail(Trail trail, Color color) {
        shapes.setColor(color);
        trail.positionAtDistance(0f, previous);
        for (int i = 1; i <= Tuning.TRAIL_SAMPLES; i++) {
            trail.positionAtDistance(trail.getLength() * i / Tuning.TRAIL_SAMPLES, current);
            shapes.rectLine(previous, current, Tuning.TRAIL_WIDTH);
            previous.set(current);
        }
    }

    private void drawHero(World world, boolean square) {
        Hero hero = world.getHero();
        shapes.setColor(Color.WHITE);
        float radius = Tuning.HERO_RADIUS;
        if (square) shapes.rect(hero.getX() - radius, hero.getY() - radius, radius * 2, radius * 2);
        else shapes.circle(hero.getX(), hero.getY(), radius);
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
