package com.nos.systems;

/** Um único vínculo pode estar ativo; repetir a tecla solta o nó. */
public final class AnchorSystem {
    public enum Target { NONE, SHADOW, REFLECTION }
    private Target target=Target.NONE;
    public Target getTarget() { return target; }
    public void toggle(Target requested) {
        target=target==requested?Target.NONE:requested;
    }
    public boolean isShadowBound() { return target==Target.SHADOW; }
    public boolean isReflectionBound() { return target==Target.REFLECTION; }
}
