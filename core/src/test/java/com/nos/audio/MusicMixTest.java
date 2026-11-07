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
}
