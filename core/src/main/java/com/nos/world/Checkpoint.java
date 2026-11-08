package com.nos.world;

/** Fotografia do progresso e dos relógios locais. */
public final class Checkpoint {
    private final float distanceA, distanceB;
    private final double timeA, timeB;
    public Checkpoint(MirrorPair pair) {
        distanceA = pair.getWorldA().getHero().getDistance();
        distanceB = pair.getWorldB().getHero().getDistance();
        timeA = pair.getWorldA().getElapsedTime();
        timeB = pair.getWorldB().getElapsedTime();
    }
    public void restore(MirrorPair pair) {
        pair.getWorldA().restore(distanceA, timeA);
        pair.getWorldB().restore(distanceB, timeB);
        pair.setBalance(0);
    }
}
