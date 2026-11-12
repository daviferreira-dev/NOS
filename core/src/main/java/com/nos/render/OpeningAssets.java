package com.nos.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.nos.data.OpeningTuning;
import com.nos.data.HeroSpriteTuning;

/** A abertura carrega somente seus quadros, a marca e a tipografia. */
public final class OpeningAssets implements Disposable {
    private final AssetManager manager=new AssetManager();
    public final Texture wide,reflection,logo;
    public final Texture[] scenes=new Texture[OpeningTuning.SHOT_BACKGROUNDS.length];
    public final HeroFrame[][] walk=new HeroFrame[2][4],idle=new HeroFrame[2][1];
    public final BitmapFont body,title;
    public final I18NBundle text;
    public OpeningAssets() {
        for(String file:new String[]{"art/opening-bank-v1.png","art/opening-reflection-v1.png","art/logo.png"}) manager.load(file,Texture.class);
        for(String file:OpeningTuning.SHOT_BACKGROUNDS) if(!file.equals("art/opening-reflection-v1.png")) manager.load(file,Texture.class);
        manager.load("art/walk-cycle-v1.png",Texture.class);manager.load("art/idle-hold-v1.png",Texture.class);
        manager.load("ui/body.fnt",BitmapFont.class);manager.load("ui/title.fnt",BitmapFont.class);manager.load("ui/strings",I18NBundle.class);
        manager.finishLoading();
        wide=manager.get("art/opening-bank-v1.png",Texture.class);reflection=manager.get("art/opening-reflection-v1.png",Texture.class);
        logo=manager.get("art/logo.png",Texture.class);body=manager.get("ui/body.fnt",BitmapFont.class);title=manager.get("ui/title.fnt",BitmapFont.class);
        text=manager.get("ui/strings",I18NBundle.class);
        for(int i=0;i<scenes.length;i++) { scenes[i]=manager.get(OpeningTuning.SHOT_BACKGROUNDS[i],Texture.class);scenes[i].setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear); }
        Texture sheet=manager.get("art/walk-cycle-v1.png",Texture.class),poses=manager.get("art/idle-hold-v1.png",Texture.class);
        sheet.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);poses.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        for(int row=0;row<2;row++) {
            for(int col=0;col<4;col++) walk[row][col]=new HeroFrame(new TextureRegion(sheet,col*sheet.getWidth()/4,row*sheet.getHeight()/2,sheet.getWidth()/4,sheet.getHeight()/2),HeroSpriteTuning.WALK_PIVOT,HeroSpriteTuning.WALK_SOLE[col],HeroSpriteTuning.WALK_TOP[col]);
            idle[row][0]=new HeroFrame(new TextureRegion(poses,0,row*poses.getHeight()/2,poses.getWidth()/2,poses.getHeight()/2),HeroSpriteTuning.POSE_PIVOT[0],HeroSpriteTuning.POSE_SOLE,HeroSpriteTuning.POSE_TOP[0]);
        }
        for(Texture image:new Texture[]{wide,reflection,logo,body.getRegion().getTexture(),title.getRegion().getTexture()}) image.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
    }
    @Override public void dispose() { manager.dispose(); }
}
