package com.nos.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nos.NosGame;
import com.nos.screens.GameScreen;
import com.nos.screens.IntroScreen;
import com.nos.screens.PuzzleScreen;
import com.nos.screens.BridgeScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (args.length >= 2 && (args[0].equals("--smoke-test") || args[0].equals("--intro-smoke"))) {
            Lwjgl3ApplicationConfiguration config = getDefaultConfiguration();
            config.setInitialVisible(false);
            new Lwjgl3Application(new NosGame(args[0].equals("--smoke-test")) {
                private int frames;
                @Override public void render() {
                    if (frames == 3) {
                        if(getScreen() instanceof GameScreen) ((GameScreen)getScreen()).prepareSmokeScene();
                        else if(getScreen() instanceof PuzzleScreen) ((PuzzleScreen)getScreen()).prepareSmokeScene();
                        else if(getScreen() instanceof BridgeScreen) ((BridgeScreen)getScreen()).prepareSmokeScene();
                        else ((IntroScreen)getScreen()).prepareSmokeScene();
                    }
                    super.render();
                    if (frames == 1) {
                        Pixmap intro = Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
                        PixmapIO.writePNG(Gdx.files.absolute(args[1]+"-intro.png"),intro,-1,true);
                        intro.dispose();
                    }
                    if (++frames == 6 || frames == 12) {
                        Pixmap picture = Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
                        PixmapIO.writePNG(Gdx.files.absolute(args[1] + (frames == 6 ? "-play.png" : "-pause.png")), picture, -1, true);
                        picture.dispose();
                        if (frames == 6) getScreen().pause();
                        else Gdx.app.exit();
                    }
                }
            },config);
            return;
        }
        if (StartupHelper.startNewJvmIfRequired()) return;
        if(args.length>0&&args[0].equals("--mirror-room")) {
            new Lwjgl3Application(new NosGame(true) {
                @Override public void create() { setScreen(new PuzzleScreen()); }
            },getDefaultConfiguration());return;
        }
        if(args.length>0&&args[0].equals("--legacy")) {
            new Lwjgl3Application(new NosGame(true,true),getDefaultConfiguration());return;
        }
        new Lwjgl3Application(new NosGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("NOS");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(1280, 720);
        configuration.setWindowSizeLimits(960,540,-1,-1);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
