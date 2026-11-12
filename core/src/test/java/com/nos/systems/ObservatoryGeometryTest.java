package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ObservatoryGeometryTest {
    @Test public void distinctRingsAndCoreAreConnectedOnlyByTheMappedPassages() {
        BridgeData data=new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/iris-convergence.json"));
        ObservatoryGeometry floor=new ObservatoryGeometry(data.observatory);
        assertTrue(floor.walkable(422,270,10));assertTrue(floor.walkable(786,270,10));assertTrue(floor.walkable(640,270,10));
        assertFalse(floor.walkable(640,92,10));assertFalse(floor.walkable(640,170,10));
        assertFalse(floor.walkable(930,270,10));assertFalse(floor.walkable(640,20,10));
        assertFalse(floor.walkable(Float.NaN,270,10));assertFalse(floor.walkable(640,270,-1));
        for(float[] waypoint:data.observatory.galleryRoute) assertTrue(floor.walkable(waypoint[0],waypoint[1],10));
        for(float[] waypoint:data.observatory.returnRoute) assertTrue(floor.walkable(waypoint[0],waypoint[1],10));
    }
}
