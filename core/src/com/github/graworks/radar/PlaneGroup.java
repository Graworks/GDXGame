package com.github.graworks.radar;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class PlaneGroup {
    private int level = 0;
    private final List<Plane> planes = new ArrayList<>();

    public PlaneGroup(int size, int totalPositions) {
        ArrayList<Integer> randomPositionsGreens = new ArrayList<>();
        ArrayList<Integer> randomPositionsReds = new ArrayList<>();
        int halfSize = size / 2;
        while (randomPositionsGreens.size() < halfSize) {
            int a = MathUtils.random(0, totalPositions - 1);
            if (!randomPositionsGreens.contains(a)) {
                randomPositionsGreens.add(a);
            }
        }

        while (randomPositionsReds.size() < halfSize) {
            int a = MathUtils.random(0, totalPositions - 1);
            if (!randomPositionsReds.contains(a) && !randomPositionsGreens.contains(a)) {
                randomPositionsReds.add(a);
            }
        }
        for (int i = 0; i < halfSize; i++) {
            planes.add(new Plane(randomPositionsGreens.get(i), 0));
            planes.add(new Plane(randomPositionsReds.get(i), 1));
        }

        System.out.println(3);
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
