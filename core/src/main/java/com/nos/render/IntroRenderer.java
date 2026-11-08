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
        float p=sequence.shotProgress();int shot=sequence.shot();Texture frame=shot==1?assets.reflection:assets.wide;
        float zoom=shot==0?1.02f+p*.06f:shot==1?1.04f+p*.05f:1.17f-p*.08f;
        float width=960*zoom,height=width*frame.getHeight()/frame.getWidth();
        batch.begin();batch.setColor(Color.WHITE);batch.draw(frame,(960-width)/2+(shot==1?-10*p:12*p),(540-height)/2,width,height);batch.end();
        fill();shapes.setColor(ink.r*.35f,ink.g*.35f,ink.b*.35f,1-sequence.sceneVisibility());shapes.rect(0,0,960,540);
        shapes.setColor(.055f,.065f,.10f,1);shapes.rect(0,0,960,49);shapes.rect(0,498,960,42);shapes.end();
        batch.begin();centered(assets.text.get("openingVerse"+(shot+1)),480,42,.40f,new Color(paper.r,paper.g,paper.b,sequence.sceneVisibility()));
        text(assets.text.get("openingSkip"),26,531,.28f,paper);batch.end();
    }
    private void title(OpeningSequence sequence,Soundtrack music,int progress) {
        boolean saved=progress>0&&progress<=4;
        batch.begin();batch.setColor(Color.WHITE);batch.draw(assets.wide,0,0,960,540,0,0,assets.wide.getWidth(),assets.wide.getHeight(),true,false);batch.end();
        fill();shapes.setColor(paper.r,paper.g,paper.b,.97f);shapes.rect(0,0,385,540);
        Color edge=new Color(paper.r,paper.g,paper.b,.97f),clear=new Color(paper.r,paper.g,paper.b,0);
        shapes.rect(385,0,180,540,edge,clear,clear,edge);
        shapes.setColor(ink);float[] start=saved?OpeningTuning.RESUME_START:OpeningTuning.START;shapes.rect(start[0],start[1],start[2],start[3]);
        shapes.setColor(gold.r,gold.g,gold.b,.55f);shapes.rect(76,saved?175:179,264,1);shapes.end();
        batch.begin();batch.setColor(Color.WHITE);batch.draw(assets.logo,76,310,264,132);
        assets.title.getData().setScale(.45f);assets.title.setColor(ink);assets.title.draw(batch,assets.text.get("openingTagline"),78,304);
        centered(assets.text.get(!saved?"openingStart":progress==4?"openingRevisitWaterThree":progress==3?"openingContinueWaterThree":progress==2?"openingContinueWaterTwo":"openingContinue"),208,start[1]+35,.43f,paper);
        if(saved) {
            text(assets.text.get("openingFromBeginning"),91,198,.33f,ink);
            text(assets.text.get("openingFromBeginningKey"),315,198,.33f,gold);
        }
        text(assets.text.get("openingReplay"),91,166,.35f,ink);
        text(assets.text.get("openingReplayKey"),315,166,.35f,gold);
        text(assets.text.get(!music.isAvailable()?"openingAudioUnavailable":music.isMuted()?"openingMusicOff":"openingMusicOn"),91,116,.31f,ink);
        text(assets.text.get("openingMusicKey"),315,116,.31f,gold);
        text(assets.text.get(saved?"openingProgressHint":"openingWait"),76,62,.28f,ink);
        text(assets.text.get(!saved?"openingMenuKeys":progress>=3?"openingMenuKeysWaterThree":progress>=2?"openingMenuKeysWaterTwo":"openingMenuKeysSaved"),76,38,.27f,ink);batch.end();
        float darkness=Math.max(1-sequence.titleVisibility(),sequence.startFade());
        if(darkness>0) { fill();shapes.setColor(ink.r*.35f,ink.g*.35f,ink.b*.35f,darkness);shapes.rect(0,0,960,540);shapes.end(); }
    }
    private void fill() { Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);shapes.begin(ShapeRenderer.ShapeType.Filled); }
    private void text(String s,float x,float y,float scale,Color color) { assets.body.getData().setScale(scale);assets.body.setColor(color);assets.body.draw(batch,s,x,y); }
    private void centered(String s,float x,float y,float scale,Color color) { assets.body.getData().setScale(scale);layout.setText(assets.body,s);text(s,x-layout.width/2,y,scale,color); }
    @Override public void dispose() { batch.dispose();shapes.dispose();assets.dispose(); }
}
