package com.nos.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.nos.world.MirrorPair;
import com.nos.world.Trail;

/** Carrega os dados da fase pelas APIs compatíveis com desktop e web. */
public final class LevelLoader {
    public MirrorPair load(String path) {
        LevelData data = read(path);
        return new MirrorPair(new Trail(data.trailA), new Trail(data.trailB));
    }
    public LevelData read(String path) {
        return new Json().fromJson(LevelData.class, Gdx.files.internal(path));
    }
}
