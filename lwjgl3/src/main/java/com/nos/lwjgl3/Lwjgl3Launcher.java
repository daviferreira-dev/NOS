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
        if(args.length>0&&args[0].equals("--audio-check")) { AudioCheck.run();return; }
        if (args.length >= 2 && (args[0].equals("--smoke-test") || args[0].equals("--intro-smoke"))) {
            Lwjgl3ApplicationConfiguration config = getDefaultConfiguration();
            config.setInitialVisible(false);
            config.disableAudio(true);
            new Lwjgl3Application(new NosGame(args[0].equals("--smoke-test"),false,true) {
                private int frames;
                @Override public void create() {
                    super.create();
                    if(args.length>2&&args[2].startsWith("--water-two")) { progress().recordJourneyComplete();beginWaterTwo(); }
                }
                @Override public void render() {
                    if (frames == 3) {
                        if(getScreen() instanceof GameScreen) ((GameScreen)getScreen()).prepareSmokeScene();
                        else if(getScreen() instanceof PuzzleScreen) ((PuzzleScreen)getScreen()).prepareSmokeScene();
                        else if(getScreen() instanceof BridgeScreen) ((BridgeScreen)getScreen()).prepareSmokeScene(args.length>2?args[2]:"");
                        else {
                            String scenario=args.length>2?args[2]:"reflection";
                            if(scenario.equals("--resume-start")||scenario.equals("--fresh-start")||scenario.equals("--resume-water-two")) {
                                // Nesta aplicação de inspeção, progress usa somente memória.
                                if(scenario.equals("--resume-water-two")) progress().recordJourneyComplete();else progress().recordGardenCheckpoint();
                                ((IntroScreen)getScreen()).prepareSmokeScene("title");
                                Gdx.input.getInputProcessor().keyDown(scenario.equals("--fresh-start")?com.badlogic.gdx.Input.Keys.N:com.badlogic.gdx.Input.Keys.ENTER);
                                for(int i=0;i<40&&getScreen() instanceof IntroScreen;i++) getScreen().render(.05f);
                                if(!(getScreen() instanceof BridgeScreen)||!progress().canResumeJourney())
                                    throw new IllegalStateException("Falha ao iniciar a partir do menu de progresso.");
                                if(scenario.equals("--resume-water-two")&&!((BridgeScreen)getScreen()).isWaterTwo())
                                    throw new IllegalStateException("A continuação não abriu Água 02.");
                            } else ((IntroScreen)getScreen()).prepareSmokeScene(scenario);
                        }
                    }
                    if(frames==4&&args.length>2&&(args[2].equals("--garden-menu")||args[2].equals("--water-two-menu"))&&getScreen() instanceof BridgeScreen) {
                        Gdx.input.getInputProcessor().keyDown(com.badlogic.gdx.Input.Keys.ENTER);
                        getScreen().render(.05f);
                        if(args[2].equals("--garden-menu")) {
                            if(!(getScreen() instanceof BridgeScreen)||!((BridgeScreen)getScreen()).isWaterTwo())
                                throw new IllegalStateException("Falha ao seguir da primeira para a segunda fase.");
                        } else if(!(getScreen() instanceof IntroScreen)||!progress().isSecondJourneyComplete())
                            throw new IllegalStateException("Falha no retorno ao menu após concluir a segunda fase.");
                        for(int i=0;i<20;i++) getScreen().render(.05f);
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
