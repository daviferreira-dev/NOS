package com.nos.lwjgl3;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl3.*;

/** Verifica o decoder e o dispositivo real com volume zero e janela invisível. */
final class AudioCheck {
    static void run() {
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();
        config.setInitialVisible(false);config.setWindowedMode(64,64);config.setForegroundFPS(60);
        new Lwjgl3Application(new ApplicationAdapter() {
            private final AssetManager assets=new AssetManager();
            private final String[] paths={"audio/entre-margens.ogg","audio/o-que-permanece.ogg","audio/o-jardim-escuta.ogg"};
            private final Music[] tracks=new Music[paths.length];
            private int frames;
            @Override public void create() {
                for(String path:paths) assets.load(path,Music.class);
                assets.finishLoading();
                for(int i=0;i<paths.length;i++) { tracks[i]=assets.get(paths[i],Music.class);tracks[i].setVolume(0);tracks[i].play(); }
            }
            @Override public void render() {
                if(++frames<90) return;
                for(int i=0;i<paths.length;i++) {
                    if(!tracks[i].isPlaying()||tracks[i].getPosition()<.25f) throw new IllegalStateException("Áudio sem reprodução: "+paths[i]);
                    System.out.println("PASS: "+paths[i]+" decodificado em streaming, volume zero, posição "+tracks[i].getPosition());
                }
                Gdx.app.exit();
            }
            @Override public void dispose() { assets.dispose(); }
        },config);
    }
    private AudioCheck() { }
}
