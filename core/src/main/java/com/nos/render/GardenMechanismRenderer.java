package com.nos.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.nos.data.GardenData;
import com.nos.systems.GardenGateSystem;
import com.nos.world.BridgeEncounter;

/** Apresenta a ligação física entre os pedais, seus cabos e a grade. */
final class GardenMechanismRenderer {
    private final GameAssets assets;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Color pressed=new Color(.78f,.72f,.60f,1);
    GardenMechanismRenderer(GameAssets assets,SpriteBatch batch,ShapeRenderer shapes) {
        this.assets=assets;this.batch=batch;this.shapes=shapes;
    }
    /** Chamado com ShapeRenderer preenchido e projeção do mundo já configurados. */
    void cables(BridgeEncounter encounter) {
        GardenData data=encounter.getData().garden;GardenGateSystem garden=encounter.garden();
        cable(data.helperCable,garden.helperPressed());cable(data.weightCable,garden.weightPressed());
        if(garden.hasTransfer()) cable(data.transferCable,garden.transferPressed()||garden.isTransferLatched());
    }
    private void cable(float[][] points,boolean taut) {
        if(points==null) return;
        for(int i=1;i<points.length;i++) {
            float[] from=points[i-1],to=points[i];
            shapes.setColor(.24f,.20f,.16f,.24f);shapes.rectLine(from[0],from[1]-1,to[0],to[1]-1,3);
            shapes.setColor(taut?.54f:.42f,taut?.40f:.35f,taut?.24f:.27f,.8f);
            shapes.rectLine(from[0],from[1],to[0],to[1],1.8f);
            shapes.setColor(.86f,.78f,.60f,.48f);shapes.rectLine(from[0],from[1]+.45f,to[0],to[1]+.45f,.5f);
        }
        for(int i=1;i<points.length-1;i++) {
            shapes.setColor(.39f,.31f,.23f,.85f);shapes.circle(points[i][0],points[i][1],2.6f,12);
            shapes.setColor(.74f,.65f,.45f,.9f);shapes.circle(points[i][0],points[i][1],1.1f,10);
        }
    }
    /** Chamado com SpriteBatch aberto; tintura é restaurada ao terminar. */
    void floor(BridgeEncounter encounter) {
        GardenData data=encounter.getData().garden;GardenGateSystem garden=encounter.garden();
        for(float[] bed:data.barriers) flowerbed(bed);
        pedal(data.helperPlate,garden.helperPressed());pedal(data.weightPlate,garden.weightPressed());
        if(garden.hasTransfer()) { pedal(data.transferPlate,garden.transferPressed());gate(data.transferGate,garden.transferOpening()); }
        gate(data.gate,garden.getOpening());
    }
    private void gate(float[] box,float opening) {
        float baseX=box[0]-39,baseY=box[1]-6;
        if(opening<1) {
            TextureRegion panel=assets.gardenGatePanel;
            int cut=Math.min(panel.getRegionHeight()-1,(int)(opening*panel.getRegionHeight()));
            batch.draw(panel.getTexture(),baseX+20,baseY+opening*90,61,90*(1-opening),
                panel.getRegionX(),panel.getRegionY()+cut,panel.getRegionWidth(),panel.getRegionHeight()-cut,false,false);
        }
        batch.draw(assets.gardenGateFrame,baseX,baseY,101,118);
    }
    private void pedal(float[] box,boolean down) {
        batch.setColor(down?pressed:Color.WHITE);
        batch.draw(assets.gardenPedal,box[0]-3,box[1]-(down?2:0),box[2]+6,box[3]);batch.setColor(Color.WHITE);
    }
    private void flowerbed(float[] box) {
        // Canteiros estreitos têm uma silhueta própria, sem cortar as bordas da aquarela.
        TextureRegion art=box[2]<box[3]*1.5f?assets.gardenNarrowBed:assets.gardenFlowerbed;
        batch.setColor(Color.WHITE);
        batch.draw(art,box[0]-3,box[1]-4,box[2]+6,box[3]+15);
    }
}
