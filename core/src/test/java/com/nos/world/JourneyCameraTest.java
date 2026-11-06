package com.nos.world;

import org.junit.Test;
import static org.junit.Assert.*;

public class JourneyCameraTest {
    @Test public void cameraAdvancesAndKeepsBothHeroesInFrame() {
        MirrorPair pair=new MirrorPair(new Trail(new float[][]{{100,220},{3100,290}}),new Trail(new float[][]{{100,330},{3100,290}}));
        JourneyCamera camera=new JourneyCamera();camera.update(pair);
        assertEquals(0,camera.left,0);
        pair.getWorldA().getHero().seek(2000);pair.getWorldB().getHero().seek(1000);
        camera.update(pair);
        assertTrue(camera.left>0);
        assertTrue(pair.getWorldA().getHero().getX()<=camera.left+camera.width);
        assertTrue(pair.getWorldB().getHero().getX()>=camera.left);
        assertEquals(camera.left,camera.worldX(0),0);
        assertEquals(camera.left+camera.width,camera.worldX(960),.001f);
    }
}
