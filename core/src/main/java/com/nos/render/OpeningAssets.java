package com.nos.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;

/** A abertura carrega somente seus quadros, a marca e a tipografia. */
public final class OpeningAssets implements Disposable {
    private final AssetManager manager=new AssetManager();
    public final Texture wide,reflection,logo;
    public final BitmapFont body,title;
    public final I18NBundle text;
    public OpeningAssets() {
        for(String file:new String[]{"art/opening-bank-v1.png","art/opening-reflection-v1.png","art/logo.png"}) manager.load(file,Texture.class);
        manager.load("ui/body.fnt",BitmapFont.class);manager.load("ui/title.fnt",BitmapFont.class);manager.load("ui/strings",I18NBundle.class);
        manager.finishLoading();
        wide=manager.get("art/opening-bank-v1.png",Texture.class);reflection=manager.get("art/opening-reflection-v1.png",Texture.class);
        logo=manager.get("art/logo.png",Texture.class);body=manager.get("ui/body.fnt",BitmapFont.class);title=manager.get("ui/title.fnt",BitmapFont.class);
        text=manager.get("ui/strings",I18NBundle.class);
        for(Texture image:new Texture[]{wide,reflection,logo,body.getRegion().getTexture(),title.getRegion().getTexture()}) image.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
    }
    @Override public void dispose() { manager.dispose(); }
}
