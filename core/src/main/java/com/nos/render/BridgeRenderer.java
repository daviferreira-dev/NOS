package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nos.data.BridgeData;
import com.nos.entities.PuzzleHero;
import com.nos.world.BridgeEncounter;

/** A ponte, cordas e corpos compartilham a planta usada pela física. */
public final class BridgeRenderer implements Disposable {
    private final GameAssets assets;
    private final SpriteBatch batch=new SpriteBatch();
    private final ShapeRenderer shapes=new ShapeRenderer();
    private final BridgeHudRenderer hud;
    private final GardenMechanismRenderer gardenRenderer;
    private final BridgeUiState ui=new BridgeUiState();
    private final TextureRegion bridgeRock;
    private final Matrix4 scene=new Matrix4();
    private final GlyphLayout layout=new GlyphLayout();
    private final Color ink=new Color(.23f,.22f,.36f,1),paper=new Color(.98f,.95f,.87f,1),gold=new Color(.64f,.45f,.22f,1);
    private float camera,time;
    private final int[] actorOrder={0,1,2};
    public BridgeRenderer() { this(null); }
    public BridgeRenderer(String background) {
        assets=new GameAssets(background);bridgeRock=assets.bridgeStone;
        hud=new BridgeHudRenderer(assets,batch,shapes);
        gardenRenderer=new GardenMechanismRenderer(assets,batch,shapes);
    }
    public BridgeUiState ui() { return ui; }
    public void snapTo(BridgeEncounter encounter) {
        camera=Math.max(0,Math.min(encounter.getData().width-960,encounter.hero(encounter.getActive()).getX()-360));
    }
    public void render(BridgeEncounter encounter,Viewport viewport,float delta) {
        render(encounter,viewport,delta,false,false);
    }
    public void render(BridgeEncounter encounter,Viewport viewport,float delta,boolean touchControls,boolean help) {
        ui.update(encounter,delta);
        if(encounter.getState()==BridgeEncounter.State.PLAYING) time+=delta;
        BridgeData data=encounter.getData();
        float[] interaction=encounter.interactionPoint();
        float target=Math.max(0,Math.min(data.width-960,encounter.hero(encounter.getActive()).getX()-360));
        camera+=(target-camera)*(1-(float)Math.exp(-5*delta));
        viewport.apply();scene.setToOrtho2D(camera,0,960,540);
        batch.setProjectionMatrix(scene);shapes.setProjectionMatrix(scene);
        batch.begin();batch.setColor(Color.WHITE);batch.draw(assets.bridgeBackground,0,0,data.width,data.height);
        for(float[] obstacle:data.obstacles) batch.draw(bridgeRock,obstacle[0],obstacle[1],obstacle[2],obstacle[3]);
        batch.setColor(Color.WHITE);batch.end();
        blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
        if(encounter.hasGarden()) gardenRenderer.cables(encounter);
        float[] bridge=data.bridge;
        shapes.setColor(.16f,.20f,.22f,.2f);
        shapes.rect(bridge[0],bridge[1]-4,bridge[2]*encounter.getSupport(),bridge[3]+8);
        shapes.end();
        if(encounter.hasGarden()) { batch.begin();gardenRenderer.floor(encounter);batch.end(); }
        blend();shapes.begin(ShapeRenderer.ShapeType.Line);
        if(encounter.getSupport()>.05f) {
            shapes.setColor(.45f,.36f,.23f,.85f);
            float end=bridge[0]+bridge[2]*encounter.getSupport();
            shapes.line(bridge[0],bridge[1]+bridge[3],end,bridge[1]+bridge[3]);
            shapes.line(bridge[0],bridge[1],end,bridge[1]);
        }
        if(encounter.getBound()>=0) {
            PuzzleHero bound=encounter.hero(encounter.getBound());shapes.setColor(gold);
            shapes.line(bound.getX()+13,bound.getY()+34,data.winch[0]+16,data.winch[1]+20);
        }
        shapes.end();
        batch.begin();batch.setColor(Color.WHITE);
        float visible=encounter.getSupport();
        if(visible>0) batch.draw(assets.bridgeDeck,bridge[0]-7,bridge[1]-5,(bridge[2]+14)*visible,bridge[3]+10,
            0,0,Math.max(1,(int)(assets.bridgeDeck.getWidth()*visible)),assets.bridgeDeck.getHeight(),false,false);
        batch.end();
        if(interaction!=null) {
            blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(gold.r,gold.g,gold.b,.12f+.035f*(float)Math.sin(time*2));
            shapes.ellipse(interaction[0]-28,interaction[1]-8,56,18);shapes.end();
        }
        batch.begin();batch.setColor(Color.WHITE);
        if(!data.gardenOnly) {
            mechanism(data.winch,0,encounter.getBound()>=0);
            mechanism(data.latch,1,encounter.isLocked());
        }
        batch.end();
        blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
        for(int i=0;i<2;i++) {
            PuzzleHero h=encounter.hero(i);
            shapes.setColor(ink.r,ink.g,ink.b,i==0?.29f:.18f);shapes.ellipse(h.getX()-13,h.getY()-5,26,10);
            if(i==0) {
                shapes.setColor(ink);shapes.triangle(h.getX()-6,h.getY()+79,h.getX()+6,h.getY()+79,h.getX(),h.getY()+71);
                shapes.setColor(paper);shapes.triangle(h.getX()-4,h.getY()+78,h.getX()+4,h.getY()+78,h.getX(),h.getY()+73);
            }
        }
        shapes.end();batch.begin();batch.setColor(Color.WHITE);
        int count=encounter.hasGarden()?3:2;
        for(int i=0;i<count;i++) actorOrder[i]=i;
        for(int i=1;i<count;i++) {
            int value=actorOrder[i],j=i-1;
            while(j>=0&&actorY(encounter,actorOrder[j])<actorY(encounter,value)) { actorOrder[j+1]=actorOrder[j];j--; }
            actorOrder[j+1]=value;
        }
        for(int i=0;i<count;i++) {
            int actor=actorOrder[i];
            if(actor<2) hero(encounter,actor);
            else batch.draw(assets.gardenPlanter,encounter.garden().cartX()-38,encounter.garden().cartY()-15,76,59);
        }
        batch.end();
        if(interaction!=null&&encounter.getState()==BridgeEncounter.State.PLAYING) {
            String action=actionText(encounter);
            assets.font.getData().setScale(.29f*ui.textScale);layout.setText(assets.font,action);
            float promptWidth=Math.max(124,layout.width+20);
            blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(paper.r,paper.g,paper.b,.96f);
            shapes.rect(interaction[0]-promptWidth/2,interaction[1]+67,promptWidth,29);
            shapes.setColor(gold);shapes.rect(interaction[0]-promptWidth/2,interaction[1]+94,promptWidth,2);
            shapes.end();batch.begin();
            centered(action,interaction[0],interaction[1]+81.5f+UiTypography.BODY_VISUAL_CENTER*.29f*ui.textScale,.29f*ui.textScale,ink);
            batch.end();
        }
        String speech=encounter.companionSpeech();
        if(speech!=null&&encounter.getState()==BridgeEncounter.State.PLAYING) {
            PuzzleHero other=encounter.hero(1);
            blend();shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(paper.r,paper.g,paper.b,.93f);
            shapes.rect(other.getX()-82,other.getY()+88,164,29);shapes.end();
            batch.begin();centered(assets.text.get(speech),other.getX(),other.getY()+102.5f+UiTypography.BODY_VISUAL_CENTER*.30f*ui.textScale,.30f*ui.textScale,ink);batch.end();
        }
        if(encounter.isLocked()&&(!encounter.hasGarden()||encounter.garden().isOpen())) {
            float destinationX=data.exit[0]+data.exit[2]/2,destinationY=data.exit[1]+32;
            blend();shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(paper.r,paper.g,paper.b,.9f);
            shapes.rect(destinationX-51,destinationY,102,26);shapes.end();
            batch.begin();centered(assets.text.get(data.destinationKey),destinationX,destinationY+13+UiTypography.BODY_VISUAL_CENTER*.33f*ui.textScale,.33f*ui.textScale,ink);batch.end();
        }
        batch.setProjectionMatrix(viewport.getCamera().combined);shapes.setProjectionMatrix(viewport.getCamera().combined);
        hud.render(encounter,touchControls,help,ui);
    }
    private String actionText(BridgeEncounter encounter) {
        String action=assets.text.get(encounter.action());
        int bound=ui.bindings.key(com.nos.input.BridgeKeyBindings.Action.INTERACT);
        String key=bound==com.badlogic.gdx.Input.Keys.SPACE?assets.text.get("bridgeKeySpace"):com.badlogic.gdx.Input.Keys.toString(bound);
        return action.startsWith("E  ")?key+action.substring(1):action;
    }
    private void mechanism(float[] point,int type,boolean active) {
        batch.draw(assets.mechanisms[active?1:0][type],point[0]-33,point[1]-10,66,66);
    }
    private float actorY(BridgeEncounter encounter,int index) { return index<2?encounter.hero(index).getY():encounter.garden().cartY(); }
    private void hero(BridgeEncounter encounter,int index) {
        PuzzleHero hero=encounter.hero(index);
        HeroFrame frame=encounter.getBound()==index?assets.poses[index][1]
            :index==0&&encounter.hasGarden()&&encounter.garden().isDragging()&&!hero.isMoving()?assets.poses[index][1]
            :hero.isMoving()?assets.walk[index][encounter.frame(index)]:assets.poses[index][0];
        frame.draw(batch,hero.getX(),hero.getY(),hero.getFacing()<0);
    }
    private void blend() { Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA); }
    private void text(String s,float x,float y,float scale,Color color) {
        assets.font.getData().setScale(scale);assets.font.setColor(color);assets.font.draw(batch,s,x,y);
    }
    private void centered(String s,float x,float y,float scale,Color color) {
        assets.font.getData().setScale(scale);layout.setText(assets.font,s);text(s,x-layout.width/2,y,scale,color);
    }
    @Override public void dispose() { batch.dispose();shapes.dispose();assets.dispose(); }
}
