package com.nos.data;

/** Escolhe o trecho solicitado sem carregar mapas, telas ou recursos gráficos. */
public final class JourneyProgress {
    public enum Stage { BRIDGE, GARDEN, WATER_TWO, WATER_THREE, WATER_FOUR }

    private JourneyProgress() {}

    /** Recomeçar é uma escolha de sessão e nunca apaga o avanço salvo. */
    public static Stage start(SaveData save,boolean continueSaved) {
        if(!continueSaved||save==null) return Stage.BRIDGE;
        if(save.isFourthJourneyAvailable()) return Stage.WATER_FOUR;
        if(save.isThirdJourneyAvailable()) return Stage.WATER_THREE;
        if(save.isSecondJourneyAvailable()) return Stage.WATER_TWO;
        return save.canResumeJourney()?Stage.GARDEN:Stage.BRIDGE;
    }
}
