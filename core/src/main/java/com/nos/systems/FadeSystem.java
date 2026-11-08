package com.nos.systems;

import com.nos.data.Tuning;
import com.nos.world.World;

/** A tinta evolui no relógio local, inclusive quando o herói está perdido. */
public final class FadeSystem {
    public void update(World world, float worldDelta, float difficulty) {
        if (!Float.isFinite(worldDelta) || worldDelta < 0f) throw new IllegalArgumentException("Delta inválido.");
        if (world.getHero().hasArrived()) return;
        float excess = world.getSpeed() - Tuning.BASE_SPEED;
        float change = excess > 0 ? -excess * Tuning.INK_DRAIN * difficulty
            : -excess * Tuning.INK_RECOVERY;
        world.setInk(world.getInk() + change * worldDelta);
    }
}
