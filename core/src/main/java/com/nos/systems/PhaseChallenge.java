package com.nos.systems;

import com.nos.entities.PuzzleHero;

/** Regras e sinais do encontro específico de uma fase. */
public interface PhaseChallenge {
    void update(PuzzleHero player,PuzzleHero other,float delta);
    boolean controlsCompanion();
    boolean allowsExit();
    String objectiveKey();
    int objectiveStep();
    int objectiveCount();
    default boolean interact(PuzzleHero player,PuzzleHero other) { return false; }
    default String actionKey(PuzzleHero player) { return null; }
    default float[] interactionPoint(PuzzleHero player) { return null; }
    default String companionStatus() { return null; }
    default String pollNotice() { return null; }
    default String speech() { return null; }
    default void resetNavigation() { }
}
