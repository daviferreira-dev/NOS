package com.nos.entities;

/** Corpo controlável; a posição representa o ponto de contato dos pés. */
public final class PuzzleHero {
    private float x, y;
    private float facing = 1f;
    private boolean moving;
    public PuzzleHero(float x,float y) { setPosition(x,y); }
    public void setPosition(float x,float y) {
        this.x=x;this.y=y;
    }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getFacing() { return facing; }
    public boolean isMoving() { return moving; }
    public void setMotion(float dx,float dy) {
        moving=Math.abs(dx)+Math.abs(dy)>.001f;
        if(Math.abs(dx)>.001f) facing=dx>0?1:-1;
    }
}
