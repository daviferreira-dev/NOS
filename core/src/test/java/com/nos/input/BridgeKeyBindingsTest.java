package com.nos.input;

import com.badlogic.gdx.Input;
import org.junit.Test;
import static org.junit.Assert.*;

public class BridgeKeyBindingsTest {
    @Test public void remappedPhysicalKeyProducesCanonicalActionAndOldKeyIsReleased() {
        BridgeKeyBindings keys=new BridgeKeyBindings();
        assertEquals(BridgeKeyBindings.Assignment.ACCEPTED,keys.assign(BridgeKeyBindings.Action.INTERACT,Input.Keys.F));
        assertEquals(Input.Keys.E,keys.translate(Input.Keys.F));assertEquals(-1,keys.translate(Input.Keys.E));
    }
    @Test public void collisionsAndSystemKeysCannotSilentlyReplaceAnotherAction() {
        BridgeKeyBindings keys=new BridgeKeyBindings();
        assertEquals(BridgeKeyBindings.Assignment.IN_USE,keys.assign(BridgeKeyBindings.Action.INTERACT,Input.Keys.W));
        assertEquals(BridgeKeyBindings.Assignment.RESERVED,keys.assign(BridgeKeyBindings.Action.INTERACT,Input.Keys.M));
        assertEquals(Input.Keys.E,keys.key(BridgeKeyBindings.Action.INTERACT));
    }
    @Test public void arrowAndEscapeAliasesWorkAfterRemappingAndResetRestoresDefaults() {
        BridgeKeyBindings keys=new BridgeKeyBindings();keys.assign(BridgeKeyBindings.Action.UP,Input.Keys.Z);
        assertEquals(Input.Keys.W,keys.translate(Input.Keys.UP));assertEquals(Input.Keys.SPACE,keys.translate(Input.Keys.ESCAPE));
        assertEquals(-1,keys.translate(Input.Keys.TAB));keys.reset();assertEquals(Input.Keys.W,keys.translate(Input.Keys.W));
    }
}
