package com.nos.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;

/** Recursos compartilhados da fase, carregados e liberados juntos. */
public final class GameAssets implements Disposable {
    private final AssetManager manager = new AssetManager();
    public final Texture garden, logo, knot, bridgeBackground, bridgeDeck;
    public final TextureRegion[][] walk=new TextureRegion[2][4];
    public final TextureRegion shadow, reflection, driftwood, rock;
    public final BitmapFont font, title;
    public final I18NBundle text;
    public GameAssets() {
        manager.load("art/journey.png", Texture.class);
        manager.load("art/bridge-bank-v1.png",Texture.class);
        manager.load("art/bridge-deck-v1.png",Texture.class);
        manager.load("art/walk-cycle-v1.png",Texture.class);
        manager.load("art/heroes.png", Texture.class);
        manager.load("art/driftwood.png", Texture.class);
        manager.load("art/stone.png",Texture.class);
        manager.load("art/logo.png", Texture.class);
        manager.load("art/knot.png", Texture.class);
        manager.load("ui/body.fnt", BitmapFont.class);
        manager.load("ui/title.fnt", BitmapFont.class);
        manager.load("ui/strings", I18NBundle.class);
        manager.finishLoading();
        garden = manager.get("art/journey.png", Texture.class);
        bridgeBackground=manager.get("art/bridge-bank-v1.png",Texture.class);
        bridgeDeck=manager.get("art/bridge-deck-v1.png",Texture.class);
        bridgeDeck.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        bridgeBackground.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        Texture sheet=manager.get("art/walk-cycle-v1.png",Texture.class);
        sheet.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        for(int row=0;row<2;row++) for(int column=0;column<4;column++)
            walk[row][column]=new TextureRegion(sheet,column*sheet.getWidth()/4,row*sheet.getHeight()/2,sheet.getWidth()/4,sheet.getHeight()/2);
        logo = manager.get("art/logo.png", Texture.class);
        knot = manager.get("art/knot.png", Texture.class);
        knot.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Texture heroes = manager.get("art/heroes.png", Texture.class);
        Texture wood=manager.get("art/driftwood.png",Texture.class);
        wood.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        driftwood=new TextureRegion(wood);
        Texture stone=manager.get("art/stone.png",Texture.class);
        stone.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);rock=new TextureRegion(stone);
        heroes.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        garden.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        logo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        shadow = new TextureRegion(heroes, 190, 0, 480, 1024);
        reflection = new TextureRegion(heroes, 820, 0, 480, 1024);
        font = manager.get("ui/body.fnt", BitmapFont.class);
        title = manager.get("ui/title.fnt", BitmapFont.class);
        title.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        text = manager.get("ui/strings", I18NBundle.class);
    }
    @Override public void dispose() { manager.dispose(); }
}
