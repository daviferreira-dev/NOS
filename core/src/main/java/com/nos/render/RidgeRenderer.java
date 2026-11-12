package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.nos.data.BridgeData;
import com.nos.data.RidgeData;
import com.nos.systems.RidgeSystem;
import com.nos.world.BridgeEncounter;

/** A mesma linha que sustenta os pés recorta a encosta em rocha, terra e capim. */
public final class RidgeRenderer implements Disposable {
    private static final int TEXEL = 2;
    private final GameAssets assets;
    private Texture ledge;
    private RidgeData painted;

    public RidgeRenderer(GameAssets assets) {
        this.assets = assets;
    }

    public void draw(BridgeEncounter world, SpriteBatch batch, ShapeRenderer shapes) {
        BridgeData map = world.getData();
        RidgeData ridge = map.ridge;
        RidgeSystem wind = (RidgeSystem) world.challenge();
        if (painted != ridge) {
            dispose();
            ledge = paintLedge(map);
            painted = ridge;
        }

        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(ledge, 0, 0, map.width, map.height);
        for (float[] obstacle : map.obstacles) {
            batch.draw(assets.bridgeStone, obstacle[0], obstacle[1], obstacle[2], obstacle[3]);
        }
        for (int turn = 1; turn < ridge.path.length - 1; turn++) {
            float[] p = ridge.path[turn];
            float[] next = ridge.path[turn + 1];
            float dx = next[0] - p[0], dy = next[1] - p[1];
            float length = Math.max(1, (float) Math.hypot(dx, dy));
            cairn(batch, p[0] - dy / length * (ridge.halfWidth - 9),
                    p[1] + dx / length * (ridge.halfWidth - 9), 1);
        }
        float shelterX = ridge.shelterPoint[0], shelterY = ridge.shelterPoint[1];
        cairn(batch, shelterX - 24, shelterY + 24, 1.45f);
        cairn(batch, shelterX + 22, shelterY + 26, .9f);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float[] launch = ridge.launchZone;
        float strength = wind.windActive() ? 1 : wind.windWarning() * .72f;
        streamer(shapes, launch[0] + launch[2] * .5f, launch[1] + launch[3] - 6,
                wind.time(), strength, 1);
        streamer(shapes, ridge.landingPoint[0], ridge.landingPoint[1] + ridge.halfWidth - 12,
                wind.time() + .7f, strength, .74f);
        if (strength > .08f) {
            seedFlight(shapes, ridge, wind.time(), strength);
        }
        shapes.end();
    }

    private void cairn(SpriteBatch batch, float x, float y, float scale) {
        batch.draw(assets.bridgeStone, x - 10 * scale, y - 2 * scale, 22 * scale, 9 * scale);
        batch.draw(assets.bridgeStone, x - 8 * scale, y + 4 * scale, 15 * scale, 8 * scale);
        batch.draw(assets.bridgeStone, x - 4 * scale, y + 10 * scale, 8 * scale, 6 * scale);
    }

    private static void streamer(ShapeRenderer shapes, float x, float y, float time, float strength, float scale) {
        shapes.setColor(.17f, .20f, .25f, .24f);
        shapes.ellipse(x - 4, y - 2, 18 * scale, 4 * scale);
        shapes.setColor(.32f, .27f, .24f, 1);
        shapes.rectLine(x, y, x + 1, y + 45 * scale, 3 * scale);
        shapes.setColor(.79f, .67f, .46f, 1);
        shapes.rectLine(x + 1, y + scale, x + 2, y + 45 * scale, scale);
        float px = x + 1, py = y + 42 * scale;
        for (int i = 1; i <= 6; i++) {
            float u = i / 6f;
            float nx = x + 1 + u * (7 + 39 * strength) * scale;
            float wave = MathUtils.sin(time * (3 + 5 * strength) - u * 5) * u * 3;
            float ny = y + (42 - (1 - strength) * u * 23 + wave) * scale;
            shapes.setColor(i % 2 == 1 ? .65f : .89f, i % 2 == 1 ? .32f : .80f,
                    i % 2 == 1 ? .23f : .64f, .96f);
            shapes.rectLine(px, py, nx, ny, (6 - u * 3) * scale);
            px = nx;
            py = ny;
        }
    }

    private static void seedFlight(ShapeRenderer shapes, RidgeData ridge, float time, float strength) {
        float[] start = ridge.launchZone, end = ridge.landingPoint;
        float x0 = start[0] + start[2] * .35f, y0 = start[1] + start[3] * .5f;
        for (int i = 0; i < 18; i++) {
            float u = (time * .55f + i * .618034f) % 1;
            float x = MathUtils.lerp(x0, end[0], u);
            float y = MathUtils.lerp(y0, end[1], u) + ((i * 31) % 55) - 19
                    + MathUtils.sin(u * 6 + i) * 9;
            float opacity = strength * MathUtils.sin(MathUtils.PI * u);
            shapes.setColor(.97f, .88f, .64f, opacity * .8f);
            shapes.rectLine(x - 3, y - 1, x + 1, y + 1, 1.4f);
        }
    }

    private Texture paintLedge(BridgeData map) {
        TextureData original = assets.bridgeBackground.getTextureData();
        if (!original.isPrepared()) original.prepare();
        Pixmap stone = original.consumePixmap();
        int width = (int) Math.ceil(map.width / TEXEL), height = (int) Math.ceil(map.height / TEXEL);
        Pixmap pixels = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        try {
            pixels.setBlending(Pixmap.Blending.None);
            RidgeData ridge = map.ridge;
            float[][] path = ridge.path;
            for (int py = 0; py < height; py++) {
                float y = map.height - (py + .5f) * TEXEL;
                for (int px = 0; px < width; px++) {
                    float x = (px + .5f) * TEXEL;
                    float grain = noise(x * .07f, y * .12f);
                    float contour = (noise(x * .025f, y * .035f) - .5f) * 10 + (grain - .5f) * 4;
                    float radius = ridge.halfWidth + contour;
                    float dxFissure = x - (ridge.windBarrier[0] + ridge.windBarrier[2] * .5f);
                    float dyFissure = y - ridge.landingPoint[1];
                    float seam = Math.abs(dxFissure + dyFissure * .14f + (grain - .5f) * 8);
                    boolean inGap = Math.abs(dyFissure) < ridge.halfWidth + 6 && seam < 10;
                    float distance = nearest(path, x, y);
                    boolean surface = distance <= radius && !inGap;
                    float depth = 39 + noise(x * .019f, y * .013f) * 50;
                    boolean face = !surface && nearest(path, x + 5, y + depth * .6f) <= radius + 6;
                    if (!surface && !face) continue;
                    int color;
                    if (surface) {
                        float u = mirror(x * .0026f + y * .0007f);
                        float v = mirror(y * .0042f - x * .0003f);
                        color = sample(stone, .83f + u * .16f, .03f + v * .115f);
                        float wear = MathUtils.clamp(1 - distance / radius, 0, 1);
                        float tint = .10f + wear * .24f;
                        if (radius - distance < 3) tint += .13f;
                        color = wash(color, 199, 185, 157, tint, 255);
                    } else {
                        float u = mirror(x * .00135f - y * .0007f);
                        float v = mirror(y * .0032f + x * .0004f);
                        color = sample(stone, .59f + u * .20f, .36f + v * .23f);
                        float feather = MathUtils.clamp((radius + 6 - nearest(path, x + 5, y + depth * .6f)) / 9, 0, 1);
                        color = wash(color, 61, 66, 80, .24f, (int) (feather * 246));
                    }
                    pixels.drawPixel(px, py, color);
                }
            }
            Texture texture = new Texture(pixels);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return texture;
        } finally {
            pixels.dispose();
            if (original.disposePixmap()) stone.dispose();
        }
    }

    private static float nearest(float[][] path, float x, float y) {
        float best = Float.MAX_VALUE;
        for (int i = 1; i < path.length; i++) {
            float[] a = path[i - 1], b = path[i];
            float dx = b[0] - a[0], dy = b[1] - a[1];
            float length2 = dx * dx + dy * dy;
            float t = length2 == 0 ? 0 : MathUtils.clamp(((x - a[0]) * dx + (y - a[1]) * dy) / length2, 0, 1);
            float sx = x - a[0] - t * dx, sy = y - a[1] - t * dy;
            best = Math.min(best, sx * sx + sy * sy);
        }
        return (float) Math.sqrt(best);
    }

    private static float noise(float x, float y) {
        int ix = MathUtils.floor(x), iy = MathUtils.floor(y);
        float u = x - ix, v = y - iy;
        u *= u * (3 - 2 * u);
        v *= v * (3 - 2 * v);
        float top = MathUtils.lerp(hash(ix, iy), hash(ix + 1, iy), u);
        float bottom = MathUtils.lerp(hash(ix, iy + 1), hash(ix + 1, iy + 1), u);
        return MathUtils.lerp(top, bottom, v);
    }

    private static float hash(int x, int y) {
        int bits = x * 124972673 + y * 604727;
        bits = (bits ^ (bits >>> 13)) * 1274126177;
        return (bits >>> 8) / 16777216f;
    }

    private static float mirror(float value) {
        float phase = value - 2 * MathUtils.floor(value / 2);
        return phase <= 1 ? phase : 2 - phase;
    }

    private static int sample(Pixmap pigment, float u, float v) {
        return pigment.getPixel((int) (u * (pigment.getWidth() - 1)), (int) (v * (pigment.getHeight() - 1)));
    }

    private static int wash(int color, int r, int g, int b, float amount, int alpha) {
        int red = (int) MathUtils.lerp(color >>> 24, r, amount);
        int green = (int) MathUtils.lerp((color >>> 16) & 255, g, amount);
        int blue = (int) MathUtils.lerp((color >>> 8) & 255, b, amount);
        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }

    @Override
    public void dispose() {
        if (ledge != null) ledge.dispose();
        ledge = null;
        painted = null;
    }
}
