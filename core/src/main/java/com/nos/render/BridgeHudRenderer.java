package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.nos.input.BridgeKeyBindings;
import com.nos.world.BridgeEncounter;

/** Papel, tipografia e hierarquia comuns; ajuda e configuração substituem o HUD durante a pausa. */
final class BridgeHudRenderer {
    private final GameAssets assets;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final GlyphLayout layout=new GlyphLayout();
    private final Color ink=new Color(.19f,.20f,.30f,1),paper=new Color(.985f,.96f,.89f,1),gold=new Color(.56f,.40f,.24f,1),edge=new Color(.70f,.64f,.53f,1);
    BridgeHudRenderer(GameAssets assets,SpriteBatch batch,ShapeRenderer shapes) { this.assets=assets;this.batch=batch;this.shapes=shapes; }
    void render(BridgeEncounter encounter,boolean touch,boolean help,BridgeUiState ui) {
        if(encounter.getState()==BridgeEncounter.State.PAUSED) { if(ui.settings) bindings(ui);else pause(ui);return; }
        if(encounter.getState()!=BridgeEncounter.State.PLAYING) { result(encounter,ui);return; }
        float scale=ui.textScale;
        boolean expanded=ui.expandedObjective();
        String goal=assets.text.get(encounter.objectiveKey());
        float goalWidth=348;
        assets.title.getData().setScale(.43f*scale);layout.setText(assets.title,goal,ink,goalWidth,Align.left,true);
        float objectiveHeight=expanded?Math.max(79,44+layout.height):40;
        String stage=assets.text.get("bridgeStage"+encounter.objectiveStep());
        assets.font.getData().setScale(.28f*scale);layout.setText(assets.font,stage);
        float summaryX=43+layout.width+16,summaryWidth=392-summaryX;
        String summary=assets.text.get(encounter.objectiveKey()+"Compact");
        if(!expanded) { layout.setText(assets.font,summary,ink,summaryWidth,Align.left,true);objectiveHeight=Math.max(40,layout.height+20); }
        float statusWidth=touch?340:320,statusTextWidth=statusWidth-36;
        String status=assets.text.get(encounter.companionStatus());
        assets.font.getData().setScale(.33f*scale);layout.setText(assets.font,assets.text.get("bridgeShadowControlled"));float statusHeaderHeight=layout.height;
        assets.font.getData().setScale(.27f*scale);layout.setText(assets.font,status,ink,statusTextWidth,Align.left,true);
        float statusHeight=Math.max(55,statusHeaderHeight+layout.height+20),statusHeaderY=20+statusHeight-10,statusBodyY=statusHeaderY-statusHeaderHeight-6;
        String notice=encounter.notice();
        float alpha=encounter.noticeVisibility();
        float noticeHeight=0;
        if(notice!=null) { assets.font.getData().setScale(.31f*scale);layout.setText(assets.font,localized(notice,ui),ink,443,Align.left,true);noticeHeight=Math.max(74,45+layout.height); }
        String shortcuts=shortcutText(ui);
        assets.font.getData().setScale(.29f*scale);layout.setText(assets.font,shortcuts);
        if(layout.width>500) { shortcuts=keyName(ui.bindings.key(BridgeKeyBindings.Action.HELP))+"  "+assets.text.get("bridgeHelpShort");layout.setText(assets.font,shortcuts); }
        float keysWidth=layout.width+28,keysX=936-keysWidth;
        fill();panel(24,516-objectiveHeight,388,objectiveHeight,1);
        if(notice!=null) panel(432,516-noticeHeight,504,noticeHeight,alpha);
        panel(touch?178:24,20,statusWidth,statusHeight,1);
        if(touch) {
            shapes.setColor(paper.r,paper.g,paper.b,.91f);shapes.circle(90,50,32);
            shapes.setColor(ink);shapes.rectLine(73,50,107,50,2);shapes.rectLine(90,33,90,67,2);
            panel(780,26,130,43,1);
        } else panel(keysX,20,keysWidth,32,1);
        shapes.end();batch.begin();
        text(stage,43,expanded?510:505,.28f*scale,gold);
        if(expanded) wrappedTitle(goal,43,485,goalWidth,.43f*scale,ink);
        else wrapped(summary,summaryX,505,summaryWidth,.28f*scale,ink);
        float sx=touch?196:42;
        text(assets.text.get("bridgeShadowControlled"),sx,statusHeaderY,.33f*scale,ink);
        wrapped(status,sx,statusBodyY,statusTextWidth,.27f*scale,ink);
        if(touch) centered(assets.text.get(encounter.action()),845,56,.28f*scale,ink);
        else text(shortcuts,keysX+14,44,.29f*scale,ink);
        if(notice!=null) {
            Color tint=new Color(ink.r,ink.g,ink.b,alpha);
            text(assets.text.get(notice+"Title"),449,503,.36f*scale,tint);
            wrapped(localized(notice,ui),449,474,469,.31f*scale,tint);
        }
        batch.end();
    }
    private String shortcutText(BridgeUiState ui) {
        String help=keyName(ui.bindings.key(BridgeKeyBindings.Action.HELP));
        if(!ui.fullShortcuts()) return help+"  "+assets.text.get("bridgeHelpShort");
        return movementKeys(ui)+"  "+assets.text.get("bridgeMoveShort")+"    "+keyName(ui.bindings.key(BridgeKeyBindings.Action.INTERACT))+"  "+assets.text.get("bridgeInteractShort")+"    "+help+"  "+assets.text.get("bridgeHelpShort");
    }
    private String movementKeys(BridgeUiState ui) {
        StringBuilder keys=new StringBuilder();
        for(BridgeKeyBindings.Action a:new BridgeKeyBindings.Action[]{BridgeKeyBindings.Action.UP,BridgeKeyBindings.Action.LEFT,BridgeKeyBindings.Action.DOWN,BridgeKeyBindings.Action.RIGHT}) { if(keys.length()>0) keys.append(' ');keys.append(keyName(ui.bindings.key(a))); }
        return keys.toString();
    }
    private void pause(BridgeUiState ui) {
        float s=ui.textScale;
        fill();dim();panel(155,72,650,396,1);
        cap(ui.bindings.key(BridgeKeyBindings.Action.UP),226,289,34,30,ui);
        cap(ui.bindings.key(BridgeKeyBindings.Action.LEFT),189,255,34,30,ui);
        cap(ui.bindings.key(BridgeKeyBindings.Action.DOWN),226,255,34,30,ui);
        cap(ui.bindings.key(BridgeKeyBindings.Action.RIGHT),263,255,34,30,ui);
        cap(ui.bindings.key(BridgeKeyBindings.Action.INTERACT),190,201,48,30,ui);
        cap(ui.bindings.key(BridgeKeyBindings.Action.PAUSE),500,289,100,30,ui);
        cap(ui.bindings.key(BridgeKeyBindings.Action.RESTART),500,255,48,30,ui);
        cap(Input.Keys.M,500,221,48,30,ui);cap(ui.bindings.key(BridgeKeyBindings.Action.HELP),500,187,48,30,ui);
        button(190,141,176,30);button(384,141,176,30);button(578,141,176,30);button(190,92,176,34);
        shapes.end();batch.begin();
        title(assets.text.get("bridgePauseTitle"),188,450,.60f*s,ink);
        text(assets.text.get("bridgePauseSubtitle"),190,385,.30f*s,gold);
        title(assets.text.get("bridgeMovementGroup"),190,365,.37f*s,ink);
        title(assets.text.get("bridgeSystemGroup"),500,365,.37f*s,ink);
        for(BridgeKeyBindings.Action a:new BridgeKeyBindings.Action[]{BridgeKeyBindings.Action.UP,BridgeKeyBindings.Action.LEFT,BridgeKeyBindings.Action.DOWN,BridgeKeyBindings.Action.RIGHT}) {
            float x=a==BridgeKeyBindings.Action.UP||a==BridgeKeyBindings.Action.DOWN?243:a==BridgeKeyBindings.Action.LEFT?206:280;
            keyLabel(ui.bindings.key(a),x,a==BridgeKeyBindings.Action.UP?309:275,ui);
        }
        rowLabel(assets.text.get("bridgeMoveShort"),315,270,.31f*s,ink);
        keyLabel(ui.bindings.key(BridgeKeyBindings.Action.INTERACT),214,221,ui);rowLabel(assets.text.get("bridgeInteractShort"),253,216,.31f*s,ink);
        keyLabel(ui.bindings.key(BridgeKeyBindings.Action.PAUSE),550,309,ui,88);rowLabel(assets.text.get("bridgePauseShort"),620,304,.31f*s,ink);
        keyLabel(ui.bindings.key(BridgeKeyBindings.Action.RESTART),524,275,ui);rowLabel(assets.text.get("bridgeRestartShort"),565,270,.31f*s,ink);
        keyLabel(Input.Keys.M,524,241,ui);rowLabel(assets.text.get("bridgeMusicShort"),565,236,.31f*s,ink);
        keyLabel(ui.bindings.key(BridgeKeyBindings.Action.HELP),524,207,ui);rowLabel(assets.text.get("bridgeHelpCloseShort"),565,202,.31f*s,ink);
        buttonLabel(assets.text.get("bridgeTextOption")+" "+Math.round(s*100)+"%",278,156,.27f*s,ink);
        buttonLabel(assets.text.get("bridgeBindingsOption"),472,156,.27f*s,ink);
        buttonLabel(assets.text.get("bridgeFullscreenOption"),666,156,.27f*s,ink);
        buttonLabel(assets.text.get("bridgeContinue"),278,109,.32f*s,ink);
        rowLabel(assets.text.get("bridgePauseHint"),388,109,.27f*s,gold);
        batch.end();
    }
    private void bindings(BridgeUiState ui) {
        float s=ui.textScale;
        fill();dim();panel(155,72,650,396,1);
        for(int i=0;i<8;i++) {
            float x=i<4?190:500,y=302-(i%4)*42;
            if(i==ui.selectedAction) { shapes.setColor(gold.r,gold.g,gold.b,.14f);shapes.rect(x-4,y,268,36); }
            cap(ui.bindings.key(BridgeKeyBindings.Action.values()[i]),x+177,y+2,78,29,ui);
        }
        button(190,97,230,30);button(565,97,190,30);shapes.end();batch.begin();
        title(assets.text.get("bridgeBindingsTitle"),188,450,.60f*s,ink);
        wrapped(assets.text.get(ui.bindingMessage),190,388,564,.29f*s,ui.awaitingKey?gold:ink);
        for(int i=0;i<8;i++) {
            BridgeKeyBindings.Action action=BridgeKeyBindings.Action.values()[i];float x=i<4?190:500,y=302-(i%4)*42;
            text(assets.text.get("bridgeBinding"+action.name()),x+6,y+27,.29f*s,ink);
            keyLabel(ui.bindings.key(action),x+216,y+22,ui,70);
        }
        buttonLabel(assets.text.get("bridgeBindingsDefaults"),305,112,.27f*s,ink);buttonLabel(assets.text.get("bridgeBindingsBack"),660,112,.27f*s,ink);
        text(assets.text.get("bridgeBindingsNavigation"),190,151,.27f*s,gold);
        batch.end();
    }
    private void result(BridgeEncounter encounter,BridgeUiState ui) {
        fill();dim();panel(175,135,610,300,1);shapes.end();batch.begin();batch.setColor(Color.WHITE);
        if(encounter.getState()==BridgeEncounter.State.READY) {
            batch.draw(assets.logo,405,341,150,75);centeredTitle(assets.text.get("bridgeIntroTitle"),480,329,.45f,ink);
            for(int i=1;i<=3;i++) centered(assets.text.get("bridgeIntro"+i),480,282-(i-1)*31,.34f,ink);
            centered(assets.text.get("bridgeStart"),480,164,.34f,gold);
        } else {
            centeredTitle(assets.text.get("bridgeWon"),480,340,.54f,ink);centered(assets.text.get("bridgeWonDetail"),480,267,.36f,ink);
            centered(localized("bridgeAgain",ui),480,192,.33f*ui.textScale,gold);
        }
        batch.end();
    }
    private void panel(float x,float y,float w,float h,float alpha) {
        shapes.setColor(paper.r,paper.g,paper.b,.95f*alpha);shapes.rect(x,y,w,h);
        shapes.setColor(gold.r,gold.g,gold.b,.68f*alpha);shapes.rect(x,y+h-2,w,2);
    }
    private void button(float x,float y,float w,float h) { shapes.setColor(gold.r,gold.g,gold.b,.14f);shapes.rect(x,y,w,h); }
    private void cap(int key,float x,float y,float w,float h,BridgeUiState ui) { shapes.setColor(edge);shapes.rect(x,y,w,h);shapes.setColor(paper);shapes.rect(x+1,y+2,w-2,h-3); }
    private String keyName(int key) { if(key==Input.Keys.SPACE) return assets.text.get("bridgeKeySpace");return Input.Keys.toString(key); }
    private String localized(String key,BridgeUiState ui) { return assets.text.get(key).replace("{interact}",keyName(ui.bindings.key(BridgeKeyBindings.Action.INTERACT))).replace("{restart}",keyName(ui.bindings.key(BridgeKeyBindings.Action.RESTART))); }
    private void keyLabel(int key,float x,float y,BridgeUiState ui) {
        keyLabel(key,x,y,ui,30);
    }
    private void keyLabel(int key,float x,float y,BridgeUiState ui,float maxWidth) {
        float scale=.29f*ui.textScale;String label=keyName(key);assets.font.getData().setScale(scale);layout.setText(assets.font,label);
        if(layout.width>maxWidth) scale*=maxWidth/layout.width;
        centered(label,x,y-5+UiTypography.BODY_VISUAL_CENTER*scale,scale,ink);
    }
    private void rowLabel(String s,float x,float centerY,float scale,Color color) { text(s,x,centerY+UiTypography.BODY_VISUAL_CENTER*scale,scale,color); }
    private void buttonLabel(String s,float x,float centerY,float scale,Color color) { centered(s,x,centerY+UiTypography.BODY_VISUAL_CENTER*scale,scale,color); }
    private void dim() { shapes.setColor(ink.r,ink.g,ink.b,.55f);shapes.rect(0,0,960,540); }
    private void fill() { Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);shapes.begin(ShapeRenderer.ShapeType.Filled); }
    private void text(String s,float x,float y,float scale,Color color) { assets.font.getData().setScale(scale);assets.font.setColor(color);assets.font.draw(batch,s,x,y); }
    private void wrapped(String s,float x,float y,float width,float scale,Color color) { assets.font.getData().setScale(scale);assets.font.setColor(color);assets.font.draw(batch,s,x,y,width,Align.left,true); }
    private void centered(String s,float x,float y,float scale,Color color) { assets.font.getData().setScale(scale);layout.setText(assets.font,s);text(s,x-layout.width/2,y,scale,color); }
    private void title(String s,float x,float y,float scale,Color color) { assets.title.getData().setScale(scale);assets.title.setColor(color);assets.title.draw(batch,s,x,y); }
    private void wrappedTitle(String s,float x,float y,float width,float scale,Color color) { assets.title.getData().setScale(scale);assets.title.setColor(color);assets.title.draw(batch,s,x,y,width,Align.left,true); }
    private void centeredTitle(String s,float x,float y,float scale,Color color) { assets.title.getData().setScale(scale);layout.setText(assets.title,s);title(s,x-layout.width/2,y,scale,color); }
}
