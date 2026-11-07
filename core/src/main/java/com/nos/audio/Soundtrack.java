package com.nos.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;

/** Duas composições originais em streaming; o menu não interrompe a trilha da abertura. */
public final class Soundtrack implements Disposable {
    private final AssetManager manager=new AssetManager();
    private final MusicMix mix=new MusicMix();
    private Music opening,journey;
    private Preferences preferences;
    private boolean paused;
    private final boolean silent;
    public Soundtrack(boolean silent) {
        this.silent=silent;
        if(silent) return;
        preferences=Gdx.app.getPreferences("nos-audio");
        mix.setMuted(preferences.getBoolean("muted",false));
        try {
            manager.load("audio/entre-margens.ogg",Music.class);
            manager.load("audio/o-que-permanece.ogg",Music.class);
            manager.finishLoading();
            opening=manager.get("audio/entre-margens.ogg",Music.class);
            journey=manager.get("audio/o-que-permanece.ogg",Music.class);
            opening.setLooping(true);journey.setLooping(true);
        } catch(RuntimeException error) {
            opening=null;journey=null;
            Gdx.app.error("NÓS","Não foi possível iniciar a música.",error);
        }
    }
    public void opening() { mix.setScene(MusicMix.Scene.OPENING); }
    public void journey() { mix.setScene(MusicMix.Scene.JOURNEY); }
    public void fadeOut() { mix.setScene(MusicMix.Scene.SILENT); }
    public void replay() {
        opening();
        if(opening!=null) { opening.stop();opening.setPosition(0); }
    }
    public void toggleMute() {
        mix.setMuted(!mix.isMuted());
        if(preferences!=null) { preferences.putBoolean("muted",mix.isMuted());preferences.flush(); }
    }
    public boolean isMuted() { return mix.isMuted(); }
    public boolean isAvailable() { return silent||opening!=null&&journey!=null; }
    public void update(float delta) {
        if(paused) return;
        mix.update(delta);apply(opening,mix.opening());apply(journey,mix.journey());
    }
    private void apply(Music music,float volume) {
        if(music==null) return;
        music.setVolume(volume);
        if(volume>0&&!music.isPlaying()) music.play();
        else if(volume==0&&music.isPlaying()) music.pause();
    }
    public void pause() { paused=true;if(opening!=null) opening.pause();if(journey!=null) journey.pause(); }
    public void resume() { paused=false; }
    @Override public void dispose() { manager.dispose(); }
}
