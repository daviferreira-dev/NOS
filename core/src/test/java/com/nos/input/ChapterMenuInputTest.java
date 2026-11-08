package com.nos.input;

import com.badlogic.gdx.Input;
import com.nos.data.OpeningTuning;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChapterMenuInputTest {
    @Test public void keysAndTouchRespectUnlocksAndReturnFromThePicker() {
        ChapterMenuInput menu=new ChapterMenuInput(null,2);
        menu.keyDown(Input.Keys.NUM_3);assertEquals(0,menu.take());
        menu.keyDown(Input.Keys.NUM_2);assertEquals(2,menu.take());assertEquals(0,menu.take());
        menu.keyDown(Input.Keys.ESCAPE);assertEquals(ChapterMenuInput.BACK,menu.take());
        assertEquals(0,ChapterMenuInput.hit(160,OpeningTuning.chapterBottom(3)+20,2));
        assertEquals(3,ChapterMenuInput.hit(160,OpeningTuning.chapterBottom(3)+20,3));
        assertEquals(4,ChapterMenuInput.hit(160,OpeningTuning.chapterBottom(4)+20,5));
        assertEquals(ChapterMenuInput.BACK,ChapterMenuInput.hit(160,90,2));
    }
}
