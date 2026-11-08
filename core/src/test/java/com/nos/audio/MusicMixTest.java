package com.nos.audio;

import com.nos.data.OpeningTuning;
import org.junit.Test;
import static org.junit.Assert.*;

public class MusicMixTest {
    @Test public void fadesInWithoutExceedingConfiguredVolume() {
        MusicMix mix=new MusicMix();mix.update(.5f);
        assertTrue(mix.opening()>0&&mix.opening()<OpeningTuning.MUSIC_VOLUME);
        mix.update(100);assertEquals(OpeningTuning.MUSIC_VOLUME,mix.opening(),0);assertEquals(0,mix.journey(),0);
    }
    @Test public void changesThemeGraduallyAndCanMuteEitherScene() {
        MusicMix mix=new MusicMix();mix.update(10);mix.setScene(MusicMix.Scene.JOURNEY);mix.update(.5f);
        assertTrue(mix.opening()>0);assertTrue(mix.journey()>0);
        mix.update(10);assertEquals(0,mix.opening(),0);assertEquals(OpeningTuning.MUSIC_VOLUME,mix.journey(),0);
        mix.setMuted(true);mix.update(10);assertEquals(0,mix.journey(),0);
        mix.setMuted(false);mix.update(10);assertEquals(OpeningTuning.MUSIC_VOLUME,mix.journey(),0);
    }
    @Test public void silenceDoesNotRestartOpening() {
        MusicMix mix=new MusicMix();mix.update(10);mix.setScene(MusicMix.Scene.SILENT);mix.update(10);
        assertEquals(0,mix.opening(),0);assertEquals(0,mix.journey(),0);
    }
    @Test public void gardenCrossfadesFromJourneyAndRespectsMute() {
        MusicMix mix=new MusicMix();mix.setScene(MusicMix.Scene.JOURNEY);mix.update(10);
        mix.setScene(MusicMix.Scene.GARDEN);mix.update(.5f);
        assertTrue(mix.journey()>0);assertTrue(mix.garden()>0);
        assertTrue(mix.journey()+mix.garden()<=OpeningTuning.MUSIC_VOLUME+.0001f);
        mix.update(10);assertEquals(0,mix.journey(),0);
        assertEquals(OpeningTuning.MUSIC_VOLUME,mix.garden(),0);
        mix.setMuted(true);mix.update(10);assertEquals(0,mix.garden(),0);
        mix.setMuted(false);mix.update(10);assertEquals(OpeningTuning.MUSIC_VOLUME,mix.garden(),0);
    }
    @Test public void selectingGardenAgainDoesNotRestartItsFade() {
        MusicMix mix=new MusicMix();mix.setScene(MusicMix.Scene.GARDEN);mix.update(.3f);
        float previous=mix.garden();mix.setScene(MusicMix.Scene.GARDEN);mix.update(.3f);
        assertTrue(mix.garden()>previous);
        mix.setScene(MusicMix.Scene.SILENT);mix.update(10);assertEquals(0,mix.garden(),0);
    }
}
