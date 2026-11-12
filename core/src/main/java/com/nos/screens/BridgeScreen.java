package com.nos.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.NosGame;
import com.nos.data.*;
import com.nos.input.BridgeInput;
import com.nos.input.BridgeKeyBindings;
import com.nos.render.BridgeRenderer;
import com.nos.render.BridgeUiState;
import com.nos.systems.FurnaceSystem;
import com.nos.systems.ObservatorySystem;
import com.nos.systems.RidgeSystem;
import com.nos.world.BridgeEncounter;

/** Coordena entrada, encontro e menus modais; a simulação para enquanto o jogador lê. */
public final class BridgeScreen extends ScreenAdapter {
    private BridgeEncounter encounter;
    private final String levelPath;
    private final BridgeInput input=new BridgeInput();
    private final BridgeRenderer renderer;
    private final BridgeUiState ui;
    private final FitViewport viewport=new FitViewport(960,540);
    private final Preferences preferences=Gdx.app.getPreferences("nos-controls");
    private final SaveData progress;
    private final Vector2 pointer=new Vector2();
    private float accumulator;
    private boolean showHelp,smokeTouch,settingsKeyFrame;
    private boolean gardenRecorded,journeyRecorded;
    private final boolean[] settingsHeld=new boolean[256];
    private int windowWidth=1280,windowHeight=720;
    private final boolean touchControls=Gdx.app.getType()==Application.ApplicationType.Android||Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen);
    private final InputAdapter settingsInput=new InputAdapter() {
        @Override public boolean keyDown(int key) {
            settingsKeyFrame=true;
            if(key>=0&&key<settingsHeld.length) { if(settingsHeld[key]) return true;settingsHeld[key]=true; }
            if(ui.awaitingKey) {
                if(key==Input.Keys.ESCAPE) { ui.awaitingKey=false;ui.bindingMessage="bridgeBindingsIntro";return true; }
                BridgeKeyBindings.Assignment result=ui.bindings.assign(BridgeKeyBindings.Action.values()[ui.selectedAction],key);
                ui.bindingMessage=result==BridgeKeyBindings.Assignment.ACCEPTED?"bridgeBindingSaved":result==BridgeKeyBindings.Assignment.IN_USE?"bridgeBindingInUse":"bridgeBindingReserved";
                if(result==BridgeKeyBindings.Assignment.ACCEPTED) ui.bindings.save(preferences);
                ui.awaitingKey=false;return true;
            }
            if(key==Input.Keys.ESCAPE) closeSettings();
            else if(key==Input.Keys.UP) ui.selectedAction=(ui.selectedAction+7)%8;
            else if(key==Input.Keys.DOWN) ui.selectedAction=(ui.selectedAction+1)%8;
            else if(key==Input.Keys.LEFT||key==Input.Keys.RIGHT) ui.selectedAction=(ui.selectedAction+4)%8;
            else if(key==Input.Keys.ENTER) { ui.awaitingKey=true;ui.bindingMessage="bridgeBindingCapture"; }
            else if(key==Input.Keys.F8) resetBindings();
            return true;
        }
        @Override public boolean keyUp(int key) { if(key>=0&&key<settingsHeld.length) settingsHeld[key]=false;return true; }
        @Override public boolean touchDown(int x,int y,int p,int b) {
            if(p!=0||b!=Input.Buttons.LEFT) return false;
            viewport.unproject(pointer.set(x,y));
            if(inside(pointer.x,pointer.y,190,97,230,30)) { resetBindings();return true; }
            if(inside(pointer.x,pointer.y,565,97,190,30)) { closeSettings();return true; }
            for(int i=0;i<8;i++) {
                float bx=i<4?190:500,by=302-(i%4)*42;
                if(inside(pointer.x,pointer.y,bx,by,260,35)) { ui.selectedAction=i;ui.awaitingKey=true;ui.bindingMessage="bridgeBindingCapture";break; }
            }
            return true;
        }
    };
    public BridgeScreen() { this(false); }
    public BridgeScreen(boolean begin) { this(begin,null,false); }
    public BridgeScreen(boolean begin,SaveData progress,boolean resumeCheckpoint) {
        this(begin,progress,resumeCheckpoint,false);
    }
    public BridgeScreen(boolean begin,SaveData progress,boolean resumeCheckpoint,boolean waterTwo) {
        this(begin,progress,resumeCheckpoint,waterTwo?2:1);
    }
    public BridgeScreen(boolean begin,SaveData progress,boolean resumeCheckpoint,int phase) {
        this.progress=progress;
        levelPath=phase==4?LevelLoader.FOURTH_WATER:phase==3?LevelLoader.THIRD_WATER:phase==2?LevelLoader.SECOND_WATER:LevelLoader.FIRST_WATER;
        encounter=new BridgeEncounter(new LevelLoader().readBridge(levelPath));
        renderer=new BridgeRenderer(encounter.getData().backgroundPath);ui=renderer.ui();
        ui.bindings.load(preferences);input.setBindings(ui.bindings);
        ui.textScale=Math.max(1,Math.min(1.3f,preferences.getFloat("textScale",1)));
        if(begin) {
            if(resumeCheckpoint) encounter.startAtGardenCheckpoint();else encounter.start();
            renderer.snapTo(encounter);
        }
    }
    public boolean isConfiguringControls() { return ui.settings||settingsKeyFrame; }
    public boolean isWaterTwo() { return encounter.getData().phaseNumber==2; }
    public boolean isWaterThree() { return encounter.getData().phaseNumber==3; }
    public boolean isWaterFour() { return encounter.getData().phaseNumber==4; }
    public int getPhaseNumber() { return encounter.getData().phaseNumber; }
    @Override public void show() { input.setTouchControls(touchControls);Gdx.input.setInputProcessor(input); }
    @Override public void render(float delta) {
        input.project(viewport,Gdx.graphics.getHeight());
        boolean start=input.start(),pause=input.pause(),help=input.help(),restart=input.restart();
        int action=input.action();
        if(!ui.settings&&start&&!restart&&encounter.getState()==BridgeEncounter.State.WON
            &&Gdx.app.getApplicationListener() instanceof NosGame) {
            NosGame game=(NosGame)Gdx.app.getApplicationListener();
            if(isWaterFour()) game.showGameOver(true);else if(isWaterThree()) game.beginWaterFour();else if(isWaterTwo()) game.beginWaterThree();else game.beginWaterTwo();
            return;
        }
        if(!ui.settings&&!settingsKeyFrame) {
            if(Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
            if(Gdx.input.isKeyJustPressed(Input.Keys.F6)) cycleTextSize();
            if(Gdx.input.isKeyJustPressed(Input.Keys.F7)&&encounter.getState()==BridgeEncounter.State.PAUSED) openSettings();
        }
        settingsKeyFrame=false;
        if(!ui.settings) {
            if(restart||(start&&encounter.getState()==BridgeEncounter.State.WON)) restart();
            else if(encounter.getState()==BridgeEncounter.State.READY&&(start||pause||help)) { encounter.start();input.clear(); }
            else if(help||pause) toggleMenu();
            else if(start&&encounter.getState()==BridgeEncounter.State.PAUSED) {
                if(Gdx.input.justTouched()) menuClick();else toggleMenu();
            }
            if(action==2&&encounter.getState()==BridgeEncounter.State.PLAYING) encounter.interact();
        }
        if(encounter.getState()==BridgeEncounter.State.PLAYING) {
            accumulator+=Math.min(.1f,delta);
            if(Gdx.app.getApplicationListener() instanceof NosGame) ((NosGame)Gdx.app.getApplicationListener()).addRunTime(Math.min(.1f,delta));
            while(accumulator>=BridgeTuning.FIXED_STEP) { encounter.update(input.x(),input.y(),BridgeTuning.FIXED_STEP,input.isSprinting());accumulator-=BridgeTuning.FIXED_STEP; }
        } else accumulator=0;
        recordProgress();
        if(encounter.isOutOfLives()&&Gdx.app.getApplicationListener() instanceof NosGame) {
            ((NosGame)Gdx.app.getApplicationListener()).showGameOver(false);return;
        }
        if(encounter.isLocked()&&encounter.getData().phaseNumber==1&&Gdx.app.getApplicationListener() instanceof NosGame) {
            NosGame game=(NosGame)Gdx.app.getApplicationListener();
            if(game.soundtrack()!=null) game.soundtrack().garden();
        }
        ScreenUtils.clear(.30f,.32f,.28f,1);
        renderer.render(encounter,viewport,Math.min(.1f,delta),touchControls||smokeTouch,showHelp);
    }
    private void recordProgress() {
        if(progress==null) return;
        if(isWaterFour()) {
            if(!journeyRecorded&&encounter.getState()==BridgeEncounter.State.WON) { progress.recordFourthJourneyComplete();journeyRecorded=true; }
            return;
        }
        if(isWaterThree()) {
            if(!journeyRecorded&&encounter.getState()==BridgeEncounter.State.WON) { progress.recordThirdJourneyComplete();journeyRecorded=true; }
            return;
        }
        if(isWaterTwo()) {
            if(!journeyRecorded&&encounter.getState()==BridgeEncounter.State.WON) { progress.recordSecondJourneyComplete();journeyRecorded=true; }
            return;
        }
        if(!gardenRecorded&&encounter.isLocked()) { progress.recordGardenCheckpoint();gardenRecorded=true; }
        if(!journeyRecorded&&encounter.getState()==BridgeEncounter.State.WON) { progress.recordJourneyComplete();journeyRecorded=true; }
    }
    private void restart() {
        if(Gdx.app.getApplicationListener() instanceof NosGame) ((NosGame)Gdx.app.getApplicationListener()).addRunRestart();
        boolean gardenCheckpoint=encounter.getData().phaseNumber==1&&encounter.isLocked();
        encounter=new BridgeEncounter(new LevelLoader().readBridge(levelPath));
        if(gardenCheckpoint) encounter.startAtGardenCheckpoint();else encounter.start();
        renderer.snapTo(encounter);
        gardenRecorded=false;journeyRecorded=false;
        input.clearForMenu();accumulator=0;showHelp=false;ui.settings=false;ui.awaitingKey=false;ui.resetPresentation();Gdx.input.setInputProcessor(input);
    }
    private void toggleMenu() {
        if(encounter.getState()==BridgeEncounter.State.PLAYING) { encounter.togglePause();showHelp=true; }
        else if(encounter.getState()==BridgeEncounter.State.PAUSED) { encounter.togglePause();showHelp=false; }
        input.clearForMenu();
    }
    private void menuClick() {
        viewport.unproject(pointer.set(Gdx.input.getX(),Gdx.input.getY()));
        if(inside(pointer.x,pointer.y,190,92,176,34)) { toggleMenu();return; }
        if(inside(pointer.x,pointer.y,190,141,176,30)) cycleTextSize();
        else if(inside(pointer.x,pointer.y,384,141,176,30)) openSettings();
        else if(inside(pointer.x,pointer.y,578,141,176,30)) toggleFullscreen();
    }
    private static boolean inside(float x,float y,float bx,float by,float w,float h) { return x>=bx&&x<=bx+w&&y>=by&&y<=by+h; }
    private void cycleTextSize() { ui.cycleTextScale();preferences.putFloat("textScale",ui.textScale);preferences.flush(); }
    private void openSettings() { ui.settings=true;ui.awaitingKey=false;ui.bindingMessage="bridgeBindingsIntro";input.clear();java.util.Arrays.fill(settingsHeld,false);Gdx.input.setInputProcessor(settingsInput); }
    private void closeSettings() {
        ui.settings=false;ui.awaitingKey=false;settingsKeyFrame=true;input.clear();
        for(int key:new int[]{Input.Keys.ESCAPE,Input.Keys.ENTER,ui.bindings.key(BridgeKeyBindings.Action.PAUSE),
            ui.bindings.key(BridgeKeyBindings.Action.HELP),ui.bindings.key(BridgeKeyBindings.Action.RESTART)})
            if(Gdx.input.isKeyPressed(key)) input.keyDown(key);
        input.pause();input.help();input.restart();input.start();
        java.util.Arrays.fill(settingsHeld,false);Gdx.input.setInputProcessor(input);
    }
    private void resetBindings() { ui.bindings.reset();ui.bindings.save(preferences);ui.bindingMessage="bridgeBindingsReset";ui.awaitingKey=false; }
    private void toggleFullscreen() {
        if(Gdx.graphics.isFullscreen()) Gdx.graphics.setWindowedMode(windowWidth,windowHeight);
        else { windowWidth=Gdx.graphics.getWidth();windowHeight=Gdx.graphics.getHeight();Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode()); }
    }
    public void prepareSmokeScene() { prepareSmokeScene(false); }
    public void prepareSmokeScene(String scenario) {
        smokeTouch=scenario.equals("--touch");
        if(encounter.challenge()!=null) {
            inspectChallenge(scenario);
            if(scenario.endsWith("-large")) ui.textScale=1.3f;
            renderer.snapTo(encounter);return;
        }
        if(scenario.startsWith("--garden")) {
            encounter.startAtGardenCheckpoint();
            if(scenario.endsWith("-won")||scenario.endsWith("-menu")) {
                walkTo(1080,270);waitTicks(800);
                if(encounter.getState()!=BridgeEncounter.State.WON) throw new IllegalStateException("Falha no reencontro da ponte.");
            }
            renderer.snapTo(encounter);return;
        }
        boolean modal=scenario.equals("--help")||scenario.equals("--settings")||scenario.equals("--settings-large")||scenario.equals("--large");
        boolean compact=scenario.equals("--compact")||scenario.equals("--compact-large");
        prepareSmokeScene(scenario.equals("--mechanism")||modal||compact);
        if(scenario.equals("--fixed")) {
            walkTo(encounter.getData().latch[0],encounter.getData().latch[1]);encounter.interact();
            walkTo(encounter.getData().latch[0]+120,encounter.getData().latch[1]);renderer.snapTo(encounter);
        }
        if(modal) { encounter.togglePause();showHelp=true; }
        if(scenario.equals("--settings")||scenario.equals("--settings-large")) ui.settings=true;
        if(scenario.equals("--large")||scenario.equals("--settings-large")) ui.textScale=1.3f;
        if(scenario.equals("--compact-large")) ui.textScale=1.3f;
        if(compact) ui.update(encounter,60);
    }
    public void prepareSmokeScene(boolean atMechanism) {
        encounter.start();walkTo(encounter.getData().winch[0]-42,encounter.getData().winch[1]);
        if(atMechanism) { renderer.snapTo(encounter);return; }
        encounter.interact();
        for(int i=0;i<1200&&encounter.getBound()<0;i++) encounter.update(0,0,BridgeTuning.FIXED_STEP);
        for(int i=0;i<120;i++) encounter.update(0,0,BridgeTuning.FIXED_STEP);
        walkTo(encounter.getData().bridge[0]+encounter.getData().bridge[2]/2,encounter.getData().winch[1]);renderer.snapTo(encounter);
    }
    private void waitTicks(int ticks) {
        for(int i=0;i<ticks;i++) encounter.update(0,0,BridgeTuning.FIXED_STEP);
    }
    private void inspectChallenge(String scenario) {
        encounter.start();
        // A inspeção usa as mesmas entradas físicas da campanha e um progresso em memória.
        String scene=scenario.endsWith("-large")?scenario.substring(0,scenario.length()-6):scenario;
        boolean finish=scene.endsWith("-won")||scene.endsWith("-menu");
        if(encounter.challenge() instanceof FurnaceSystem) inspectFurnace(scene,finish);
        else if(encounter.challenge() instanceof RidgeSystem) inspectRidge(scene,finish);
        else if(encounter.challenge() instanceof ObservatorySystem) inspectObservatory(scene,finish);
        if(finish&&encounter.getState()!=BridgeEncounter.State.WON)
            throw new IllegalStateException("A travessia não terminou em "+scene+".");
    }
    private void inspectFurnace(String scene,boolean finish) {
        if(scene.endsWith("-start")) return;
        if(scene.contains("-shortcut")||scene.endsWith("-door")) {
            walkTo(440,335);
            walkTo(600,335);
            if(!finish&&!scene.endsWith("-door")) return;
            HazardData ember=encounter.getData().hazards[0];
            float rest=ember.warningSeconds+ember.activeSeconds;
            float cycle=rest+ember.restSeconds;
            for(int i=0;i<4000;i++) {
                float local=(encounter.actions().time()+ember.phase)%cycle;
                if(local>=rest&&local<rest+2*BridgeTuning.FIXED_STEP) break;
                waitTicks(1);
            }
            walkTo(1080,335);
            walkTo(1100,335);
            encounter.interact();
            if(!((FurnaceSystem)encounter.challenge()).isDoorOpen())
                throw new IllegalStateException("O atalho não abriu a porta para Reflexo.");
            waitTicks(finish?1200:260);
        } else {
            walkTo(440,145);
            if(!finish) { walkTo(800,145);return; }
            walkTo(1050,145);
            walkTo(1110,260);
            waitTicks(1000);
        }
        if(finish) { walkTo(1490,270);waitTicks(1200); }
    }
    private void inspectRidge(String scene,boolean finish) {
        if(scene.endsWith("-start")) return;
        walkTo(1180,170);
        walkTo(1180,430);
        if(scene.endsWith("-ascent")) return;
        walkTo(430,430);
        walkTo(430,710);
        walkTo(1030,710);
        walkTo(1030,880);
        if(!finish&&!scene.endsWith("-landed")&&!scene.endsWith("-shelter")) return;
        RidgeSystem ridge=(RidgeSystem)encounter.challenge();
        for(int i=0;i<1600&&!ridge.hasCrossed();i++) {
            float up=!ridge.isRiding()&&encounter.hero(0).getY()<915?1:0;
            encounter.update(0,up,BridgeTuning.FIXED_STEP);
        }
        if(!ridge.hasCrossed()) throw new IllegalStateException("A rajada não alcançou o outro lado.");
        if(scene.endsWith("-landed")) return;
        walkTo(1290,940);
        walkTo(1290,1140);
        walkTo(1370,1140);
        if(finish) { encounter.interact();waitTicks(1800); }
    }
    private void inspectObservatory(String scene,boolean finish) {
        if(scene.endsWith("-start")) return;
        walkTo(422,270);
        waitTicks(1200);
        ObservatorySystem observatory=(ObservatorySystem)encounter.challenge();
        if(!observatory.isSignalling()) throw new IllegalStateException("Reflexo não alcançou o sinal.");
        if(!finish&&!scene.endsWith("-open")) return;
        walkTo(438.59f,353.42f);
        walkTo(485.85f,424.15f);
        walkTo(556.58f,471.41f);
        walkTo(640,484);
        encounter.interact();
        if(!observatory.isPassageOpen(observatory.correctPassage()))
            throw new IllegalStateException("A passagem indicada não abriu.");
        if(finish) { walkTo(640,270);waitTicks(1800); }
    }
    private void walkTo(float x,float y) {
        for(int i=0;i<6000&&encounter.getState()==BridgeEncounter.State.PLAYING;i++) {
            float dx=x-encounter.hero(0).getX(),dy=y-encounter.hero(0).getY(),d=(float)Math.hypot(dx,dy);
            if(d<7) break;encounter.update(dx/d,dy/d,BridgeTuning.FIXED_STEP);
        }
        for(int i=0;i<40;i++) encounter.update(0,0,BridgeTuning.FIXED_STEP);
    }
    @Override public void resize(int w,int h) { viewport.update(w,h,true); }
    @Override public void pause() {
        input.clear();java.util.Arrays.fill(settingsHeld,false);
        if(ui.awaitingKey) { ui.awaitingKey=false;ui.bindingMessage="bridgeBindingsIntro"; }
        if(encounter.getState()==BridgeEncounter.State.PLAYING) { encounter.togglePause();showHelp=true; }
    }
    @Override public void hide() { input.clear();if(Gdx.input.getInputProcessor()==input||Gdx.input.getInputProcessor()==settingsInput) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { hide();renderer.dispose(); }
}
