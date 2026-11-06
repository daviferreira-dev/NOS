package com.nos.world;

import com.nos.data.LevelData;
import com.nos.data.Tuning;
import com.nos.systems.*;

/** Uma tentativa jogável; concentra transições, não a renderização. */
public final class PlaySession {
    public enum State { READY, PLAYING, PAUSED, WON, LOST }
    private final MirrorPair pair;
    private final FadeSystem fade = new FadeSystem();
    private final HarmonySystem harmony = new HarmonySystem();
    private final InkPot pot = new InkPot();
    private final ObstacleSystem obstacles;
    private final InkEncounterSystem encounters;
    private Checkpoint checkpoint;
    private State state = State.READY;
    private boolean checkpointReached;
    private float difficulty = .75f;
    private float time, arrivalA = -1, arrivalB = -1, strainIntegral, noticeTime;
    private int hits, encounterCount, score;
    private String notice = "welcome";

    public PlaySession(LevelData data) {
        pair = new MirrorPair(new Trail(data.trailA), new Trail(data.trailB));
        obstacles = new ObstacleSystem(data, pair);
        encounters = new InkEncounterSystem(pair);
        checkpoint = new Checkpoint(pair);
    }
    public void start() { if (state == State.READY) state = State.PLAYING; }
    public void togglePause() {
        if (state == State.PLAYING) state = State.PAUSED;
        else if (state == State.PAUSED) state = State.PLAYING;
    }
    public void chooseDifficulty(int choice) {
        if (state == State.READY) difficulty = choice == 1 ? .75f : choice == 2 ? 1f : 1.35f;
    }
    public void update(float delta) {
        if (state != State.PLAYING) return;
        if (!Float.isFinite(delta) || delta < 0 || delta > .05f) throw new IllegalArgumentException("Use passos fixos de simulação.");
        time += delta;
        noticeTime = Math.max(0, noticeTime - delta);
        World a = pair.getWorldA(), b = pair.getWorldB();
        float beforeA = a.getHero().getDistance(), beforeB = b.getHero().getDistance();
        fade.update(a, delta * a.getSpeed(), difficulty);
        fade.update(b, delta * b.getSpeed(), difficulty);
        pair.update(delta);
        int impacts = obstacles.update(pair, beforeA, beforeB, time);
        for (int i = 0; i < impacts; i++) harmony.hit();
        hits += impacts;
        if (impacts > 0) notify("hit");
        harmony.update(delta, pair.getBalance(), difficulty);
        strainIntegral += harmony.getStrain() * delta;
        int gathered = encounters.update(pair, beforeA, beforeB, time);
        encounterCount += gathered;
        for (int i = 0; i < gathered; i++) pot.refill();
        if (gathered > 0) notify("encounter");
        if (!checkpointReached && progress(a) >= Tuning.CHECKPOINT_PROGRESS && progress(b) >= Tuning.CHECKPOINT_PROGRESS) {
            checkpointReached = true;
            checkpoint = new Checkpoint(pair);
            notify("checkpoint");
        }
        if (a.getHero().hasArrived() && arrivalA < 0) arrivalA = time;
        if (b.getHero().hasArrived() && arrivalB < 0) arrivalB = time;
        if (harmony.isBroken()) retry("harmonyBroken");
        else if (time > Tuning.RUN_TIMEOUT) { state = State.LOST; notify("timeout"); }
        else if (arrivalA >= 0 && arrivalB >= 0) {
            if (Math.abs(arrivalA - arrivalB) <= Tuning.SYNC_WINDOW) {
                state = State.WON;
                score = new ScoreSystem().calculate(Math.abs(arrivalA - arrivalB), strainIntegral / time, hits, encounterCount);
            } else retry("outOfSync");
        }
    }
    private void retry(String reason) {
        notify(reason);
        if (!pot.spend()) { state = State.LOST; return; }
        checkpoint.restore(pair);
        harmony.reset();
        encounters.resetTimings();
        arrivalA = -1;
        arrivalB = -1;
    }
    private void notify(String key) { notice = key; noticeTime = 3f; }
    public static float progress(World world) { return world.getHero().getDistance() / world.getTrail().getLength(); }
    public MirrorPair getPair() { return pair; }
    public State getState() { return state; }
    public HarmonySystem getHarmony() { return harmony; }
    public InkPot getPot() { return pot; }
    public ObstacleSystem getObstacles() { return obstacles; }
    public InkEncounterSystem getEncounters() { return encounters; }
    public float getTime() { return time; }
    public float getDifficulty() { return difficulty; }
    public int getScore() { return score; }
    public int getEncounterCount() { return encounterCount; }
    public int getHits() { return hits; }
    public boolean hasCheckpoint() { return checkpointReached; }
    public float getArrivalGap() { return arrivalA >= 0 && arrivalB >= 0 ? Math.abs(arrivalA - arrivalB) : -1; }
    public String getNotice() { return noticeTime > 0 ? notice : "objective"; }
}
