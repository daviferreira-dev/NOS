package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nos.audio.Soundtrack;
import com.nos.data.OpeningTuning;
import com.nos.world.OpeningSequence;

/** Montagem por enquadramentos e dissoluções; a marca só aparece na tela de título. */
public final class IntroRenderer implements Disposable {
    private final OpeningAssets assets=new OpeningAssets();
    private final SpriteBatch batch=new SpriteBatch();
    private final ShapeRenderer shapes=new ShapeRenderer();
    private final GlyphLayout layout=new GlyphLayout();
    private final Color paper=new Color(.975f,.95f,.88f,1),ink=new Color(.19f,.20f,.32f,1),gold=new Color(.56f,.39f,.21f,1);
    public void render(OpeningSequence sequence,Soundtrack music,Viewport viewport) {
        render(sequence,music,viewport,0);
    }
    public void render(OpeningSequence sequence,Soundtrack music,Viewport viewport,int progress) {
        viewport.apply();batch.setProjectionMatrix(viewport.getCamera().combined);shapes.setProjectionMatrix(viewport.getCamera().combined);
        if(sequence.getState()==OpeningSequence.State.CUTSCENE) cutscene(sequence);else title(sequence,music,progress);
    }
    private void cutscene(OpeningSequence sequence) {
        float p=sequence.shotProgress();int shot=sequence.shot();Texture frame=assets.scenes[shot];
        float height=shot==0?560:540,width=height*frame.getWidth()/frame.getHeight();
        float camera=shot==0?(width-960)*.5f:(width-960)*(.14f+.17f*p);
        if(shot==4) camera=(width-960)*(.65f+.25f*p);
        batch.begin();batch.setColor(Color.WHITE);batch.draw(frame,-camera,(540-height)/2,width,height);
        if(shot>0) {
            float travel=Math.min(1,p/.72f),shadowX=250+travel*260,otherX=170+Math.min(1,Math.max(0,p-.16f)/.7f)*290;
            boolean shadowMoving=p<.72f,otherMoving=p>.16f&&p<.86f;
            if(shot==1) { shadowX=220+travel*180;otherX=360;otherMoving=false; }
            if(shot==3) { shadowX=430;shadowMoving=false;otherX=650-travel*170;otherMoving=p<.72f; }
            if(shot==4) { shadowX=360+travel*180;otherX=300+travel*180;otherMoving=shadowMoving; }
            HeroFrame shadow=shadowMoving?assets.walk[0][(int)(sequence.getTime()*8)%4]:assets.idle[0][0];
            HeroFrame other=otherMoving?assets.walk[1][(int)(sequence.getTime()*7)%4]:assets.idle[1][0];
            other.draw(batch,otherX,shot==3?280:254,shot==3);shadow.draw(batch,shadowX,222,false);
        }
        batch.end();
        fill();shapes.setColor(ink.r*.35f,ink.g*.35f,ink.b*.35f,1-sequence.sceneVisibility());shapes.rect(0,0,960,540);
        shapes.setColor(.055f,.065f,.10f,1);shapes.rect(0,0,960,49);shapes.rect(0,498,960,42);shapes.end();
        batch.begin();centered(assets.text.get("openingVerse"+(shot+1)),480,42,.40f,new Color(paper.r,paper.g,paper.b,sequence.sceneVisibility()));
        text(assets.text.get("openingSkip"),26,525,.28f,paper);batch.end();
    }
    private void title(OpeningSequence sequence,Soundtrack music,int progress) {
        boolean saved=progress>0&&progress<=5;
        batch.begin();batch.setColor(Color.WHITE);batch.draw(assets.wide,0,0,960,540,0,0,assets.wide.getWidth(),assets.wide.getHeight(),true,false);batch.end();
        fill();shapes.setColor(paper.r,paper.g,paper.b,.97f);shapes.rect(0,0,385,540);
        Color edge=new Color(paper.r,paper.g,paper.b,.97f),clear=new Color(paper.r,paper.g,paper.b,0);
        shapes.rect(385,0,180,540,edge,clear,clear,edge);
        shapes.setColor(ink);float[] start=saved?OpeningTuning.RESUME_START:OpeningTuning.START;shapes.rect(start[0],start[1],start[2],start[3]);
        shapes.setColor(gold.r,gold.g,gold.b,.55f);shapes.rect(76,saved?175:179,264,1);shapes.end();
        batch.begin();batch.setColor(Color.WHITE);batch.draw(assets.logo,76,310,264,132);
        assets.title.getData().setScale(.45f);assets.title.setColor(ink);assets.title.draw(batch,assets.text.get("openingTagline"),78,304);
        centered(assets.text.get(!saved?"openingStart":progress==5?"openingRevisitIris":progress==4?"openingContinueIris":progress==3?"openingContinueWaterThree":progress==2?"openingContinueWaterTwo":"openingContinue"),208,start[1]+35,.43f,paper);
        if(saved) {
            text(assets.text.get("openingFromBeginning"),91,198,.33f,ink);
            text(assets.text.get("openingFromBeginningKey"),315,198,.33f,gold);
        }
        text(assets.text.get("openingReplay"),91,166,.35f,ink);
        text(assets.text.get("openingReplayKey"),315,166,.35f,gold);
        text(assets.text.get(!music.isAvailable()?"openingAudioUnavailable":music.isMuted()?"openingMusicOff":"openingMusicOn"),91,116,.31f,ink);
        text(assets.text.get("openingMusicKey"),315,116,.31f,gold);
        if(progress>=2) text(assets.text.get("openingChooseChapter"),76,68,.35f,gold);
        else text(assets.text.get(saved?"openingProgressHint":"openingWait"),76,62,.28f,ink);
        text(assets.text.get(!saved?"openingMenuKeys":progress>=4?"openingMenuKeysIris":progress>=3?"openingMenuKeysWaterThree":progress>=2?"openingMenuKeysWaterTwo":"openingMenuKeysSaved"),76,38,.27f,ink);batch.end();
        float darkness=Math.max(1-sequence.titleVisibility(),sequence.startFade());
        if(darkness>0) { fill();shapes.setColor(ink.r*.35f,ink.g*.35f,ink.b*.35f,darkness);shapes.rect(0,0,960,540);shapes.end(); }
    }
    public void renderChapters(Viewport viewport,int progress) {
        com.badlogic.gdx.utils.ScreenUtils.clear(ink);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();batch.setColor(Color.WHITE);
        batch.draw(assets.wide,0,0,960,540,0,0,assets.wide.getWidth(),assets.wide.getHeight(),true,false);
        batch.end();
        int unlocked=Math.min(4,progress);
        fill();
        shapes.setColor(paper);shapes.rect(0,0,395,540);
        for(int phase=1;phase<=unlocked;phase++) {
            shapes.setColor(ink);
            shapes.rect(76,OpeningTuning.chapterBottom(phase),264,OpeningTuning.ROW_HEIGHT);
        }
        shapes.end();
        batch.begin();
        assets.title.setColor(ink);assets.title.getData().setScale(.63f);
        assets.title.draw(batch,assets.text.get("chaptersTitle"),76,448);
        text(assets.text.get("chaptersHint"),76,393,.28f,ink);
        for(int phase=1;phase<=unlocked;phase++) {
            float y=OpeningTuning.chapterBottom(phase)+32;
            text(Integer.toString(phase),91,y,.34f,paper);
            text(assets.text.get("chapterName"+phase),130,y,.38f,paper);
        }
        text(assets.text.get("chaptersBack"),76,103,.32f,gold);
        batch.end();
    }
    private void fill() { Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);shapes.begin(ShapeRenderer.ShapeType.Filled); }
    private void text(String s,float x,float y,float scale,Color color) { assets.body.getData().setScale(scale);assets.body.setColor(color);assets.body.draw(batch,s,x,y); }
    private void centered(String s,float x,float y,float scale,Color color) { assets.body.getData().setScale(scale);layout.setText(assets.body,s);text(s,x-layout.width/2,y,scale,color); }
    @Override public void dispose() { batch.dispose();shapes.dispose();assets.dispose(); }
}
