package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.LevelData;
import com.nos.data.Tuning;
import com.nos.world.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayableSystemsTest {
    private LevelData simpleLevel() {
        LevelData data=new LevelData();
        data.trailA=new float[][] {{0,0},{1000,0}};
        data.trailB=new float[][] {{0,100},{1000,100}};
        return data;
    }
    private PlaySession actualLevel() {
        return new PlaySession(new Json().fromJson(LevelData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/water-01.json")));
    }
    @Test public void fadingStopsHeroButObservationRestoresMovement() {
        PlaySession run=new PlaySession(simpleLevel());
        run.start();
        World a=run.getPair().getWorldA();
        run.getPair().setBalance(-1);
        FadeSystem fade=new FadeSystem();
        fade.update(a,100,1);
        assertEquals(0,a.getInk(),0);
        run.getPair().update(.1f);
        assertEquals(0,a.getHero().getDistance(),0);
        run.getPair().setBalance(1);
        fade.update(a,1,1);
        run.getPair().update(.1f);
        assertTrue(a.getInk()>0);
        assertTrue(a.getHero().getDistance()>0);
    }
    @Test public void harmonyRequiresSustainedImbalanceAndCanRecover() {
        HarmonySystem harmony=new HarmonySystem();
        for(int i=0;i<120;i++) harmony.update(Tuning.FIXED_STEP,1,1);
        assertEquals(0,harmony.getStrain(),0);
        for(int i=0;i<480;i++) harmony.update(Tuning.FIXED_STEP,1,1);
        assertTrue(harmony.getStrain()>0);
        float high=harmony.getStrain();
        for(int i=0;i<120;i++) harmony.update(Tuning.FIXED_STEP,0,1);
        assertTrue(harmony.getStrain()<high);
    }
    @Test public void pauseFreezesSimulationAndCheckpointCostsOneAttempt() {
        PlaySession run=new PlaySession(simpleLevel());
        run.start(); run.update(Tuning.FIXED_STEP); run.togglePause();
        float time=run.getTime(); run.update(Tuning.FIXED_STEP);
        assertEquals(time,run.getTime(),0);
        run.togglePause();
        run.getHarmony().hit();run.getHarmony().hit();run.getHarmony().hit();
        run.update(Tuning.FIXED_STEP);
        assertEquals(2,run.getPot().getRemaining());
        assertEquals(0,run.getPair().getWorldA().getHero().getDistance(),0);
        assertEquals(PlaySession.State.PLAYING,run.getState());
    }
    @Test public void emptyPotEndsRunAndCannotBecomeNegative() {
        InkPot pot=new InkPot();
        assertTrue(pot.spend());assertTrue(pot.spend());assertFalse(pot.spend());assertFalse(pot.spend());
        assertEquals(0,pot.getRemaining());
        pot.refill();assertEquals(1,pot.getRemaining());
    }
    @Test public void simultaneousArrivalWinsAndProducesScore() {
        PlaySession run=new PlaySession(simpleLevel()); run.start();
        for(int i=0;i<4000&&run.getState()==PlaySession.State.PLAYING;i++) run.update(Tuning.FIXED_STEP);
        assertEquals(PlaySession.State.WON,run.getState());
        assertTrue(run.getScore()>0);
        assertEquals(0,run.getArrivalGap(),.001f);
    }
    @Test public void actualPhaseHasIntersectionsAndPeriodicObstacles() {
        PlaySession run=actualLevel();
        assertTrue(run.getEncounters().getCrossings().size>=2);
        assertEquals(6,run.getObstacles().getGates().length);
        ObstacleSystem.Gate gate=run.getObstacles().getGates()[0];
        assertTrue(gate.isOpen(gate.data.period-gate.data.offset+gate.data.period*.25));
        assertFalse(gate.isOpen(gate.data.period-gate.data.offset));
    }
    @Test public void actualPhaseCanBeWonWithAlternatingAttention() {
        boolean won=false;
        for(int period=4;period<=14&&!won;period+=2) {
            for(int phase=0;phase<12&&!won;phase++) {
                PlaySession run=actualLevel();run.start();
                for(int i=0;i<15000&&run.getState()==PlaySession.State.PLAYING;i++) {
                    run.getPair().setBalance(.15f+(float)Math.sin(run.getTime()*Math.PI*2/period+phase*.5)*.55f);
                    run.update(Tuning.FIXED_STEP);
                }
                if(run.getState()==PlaySession.State.WON) {
                    System.out.println("Travessia completa: periodo="+period+" fase="+phase+" tempo="+run.getTime()+" impactos="+run.getHits());
                    won=true;
                }
            }
        }
        assertTrue("A fase precisa permitir vitória com atenção alternada.",won);
    }
    @Test public void ignoringTheWholePhaseDoesNotAutomaticallyWin() {
        PlaySession run=actualLevel();run.start();
        for(int i=0;i<15000&&run.getState()==PlaySession.State.PLAYING;i++) {
            run.getPair().setBalance(0);
            run.update(Tuning.FIXED_STEP);
        }
        System.out.println("Sem atenção: "+run.getState()+" impactos="+run.getHits()+" tempo="+run.getTime());
        assertEquals(PlaySession.State.LOST,run.getState());
    }
}
