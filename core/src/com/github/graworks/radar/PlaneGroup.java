package com.github.graworks.radar;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class PlaneGroup {
    private int level = 0;
    private final List<Plane> planes = new ArrayList<>();

    public PlaneGroup(int size, int totalPositions) {
        ArrayList<Integer> randomPositions = new ArrayList<>();
        while (randomPositions.size() < size) {
            int a = MathUtils.random(0, totalPositions - 1);
            if (!randomPositions.contains(a)) {
                randomPositions.add(a);
            }
        }
        for (int i = 0; i < size; i++) {
            planes.add(new Plane(randomPositions.get(i), MathUtils.random(0, 1)));
        }
    }
    public List<Plane> getPlanes() {
        return planes;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
}
