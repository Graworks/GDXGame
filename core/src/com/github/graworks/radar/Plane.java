package com.github.graworks.radar;

public class Plane {
    private final int anglePositionIndex;

    private float currentXPos;
    private float currentYPos;
    private long explosionTime;
    private final int type;

    public Plane(int anglePositionIndex, int type) {
        this.type = type;
        this.anglePositionIndex = anglePositionIndex;
    }

    public int getType() {
        return type;
    }

    public long getExplosionTime() {
        return explosionTime;
    }
    public void setExplosionTime(long explosionTime) {
        this.explosionTime = explosionTime;
    }

    public float getCurrentXPos() {
        return currentXPos;
    }
    public void setCurrentXPos(float currentXPos) {
        this.currentXPos = currentXPos;
    }

    public float getCurrentYPos() {
        return currentYPos;
    }
    public void setCurrentYPos(float currentYPos) {
        this.currentYPos = currentYPos;
    }

    public int getAnglePositionIndex() {
        return anglePositionIndex;
    }
}
