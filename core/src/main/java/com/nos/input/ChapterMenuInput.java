package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nos.data.OpeningTuning;

/** Seleção explícita por número, clique ou toque. Escape retorna ao título. */
public final class ChapterMenuInput extends InputAdapter {
    public static final int BACK=-1;
    private final Viewport viewport;
    private final Vector2 point=new Vector2();
    private final int unlocked;
    private int selection;

    public ChapterMenuInput(Viewport viewport,int progress) {
        this.viewport=viewport;
        unlocked=Math.min(4,progress);
    }
    @Override public boolean keyDown(int key) {
        if(key==Input.Keys.ESCAPE) selection=BACK;
        else if(key>=Input.Keys.NUM_1 && key<Input.Keys.NUM_1+unlocked) selection=1+key-Input.Keys.NUM_1;
        return true;
    }
    @Override public boolean touchDown(int x,int y,int pointer,int button) {
        if(pointer!=0 || button!=Input.Buttons.LEFT) return false;
        viewport.unproject(point.set(x,y));
        selection=hit(point.x,point.y,unlocked);
        return true;
    }
    public static int hit(float x,float y,int unlocked) {
        float[] box=OpeningTuning.CHAPTER_BACK;
        if(x<box[0]||x>box[0]+box[2]) return 0;
        if(y>=box[1]&&y<=box[1]+box[3]) return BACK;
        for(int phase=1;phase<=Math.min(4,unlocked);phase++) {
            float bottom=OpeningTuning.chapterBottom(phase);
            if(y>=bottom&&y<=bottom+OpeningTuning.ROW_HEIGHT) return phase;
        }
        return 0;
    }
    public int take() { int value=selection;selection=0;return value; }
}
