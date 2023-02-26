package com.github.graworks.radar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RadarGame extends ApplicationAdapter {

    ShapeRenderer shapeRenderer;

    private SpriteBatch batch;
    private Texture redPlaneImage;
    private Texture greenPlaneImage;
    private Texture explosionImage;
    private Sound shotSound;
    private BitmapFont font;
    private BitmapFont smallFont;

    private final List<ArrayList<PlanePosition>> ringPositionArrays = new ArrayList<>();
    private int circleRadius;
    private int ringWidth;
    private int screenCenterX;
    private int screenCenterY;
    private int planeRadius;
    private long startTime = 0;
    private int currentGroupIndex = 0;
    private final List<PlaneGroup> planeGroups = new ArrayList<>();
    private Rectangle rect;
    private final List<Plane> explodedPlanes = new ArrayList<>();
    private long deltaTime;
    private boolean justLaunched;
    private boolean gameOver;
    private int score;
    private List<Integer> angles;


    @Override
    public void create () {
        shapeRenderer = new ShapeRenderer();

        greenPlaneImage = new Texture("green_plane.png");
        redPlaneImage = new Texture("red_plane.png");
        explosionImage = new Texture("explosion.png");
        shotSound = Gdx.audio.newSound(Gdx.files.internal("shot.wav"));
        batch = new SpriteBatch();

        screenCenterX = Gdx.graphics.getWidth() / 2;
        screenCenterY = Gdx.graphics.getHeight() / 2;
        circleRadius = (screenCenterX > screenCenterY) ? screenCenterY - Config.MARGIN :
                                        screenCenterX - Config.MARGIN;
        ringWidth = circleRadius / Config.CIRCLES_NUMBER;

        planeRadius = Math.round(ringWidth * (float) Math.sin(Math.toRadians(Config.DELTA_ANGLE)) );
        if (planeRadius > ringWidth / 2) {
            planeRadius = ringWidth / 2;
        }
        rect = new Rectangle(0,0,planeRadius * 2,planeRadius * 2);

        angles = new ArrayList<>();
        int currentAngle = 0;
        while ((currentAngle + Config.DELTA_ANGLE) <= 360) {
            angles.add(currentAngle);
            currentAngle += Config.DELTA_ANGLE;
        }

        int distanceFromCenter = 0;
        for (int i = 0; i < Config.CIRCLES_NUMBER; i++) {
            distanceFromCenter = ringWidth * i + ringWidth / 2;
            ArrayList<PlanePosition> planePositions = new ArrayList<>();
            for (int j = 0; j < angles.size(); j++) {
                float planeXPos = screenCenterX +
                                    distanceFromCenter * (float) Math.cos(Math.toRadians(angles.get(j)));
                float planeYPos = screenCenterY +
                                    distanceFromCenter * (float) Math.sin(Math.toRadians(angles.get(j)));
                planePositions.add(new PlanePosition(planeXPos, planeYPos));
            }
            ringPositionArrays.add(planePositions);
        }
        Collections.reverse(ringPositionArrays);

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(3);
        smallFont = new BitmapFont();
        smallFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1.0f));
        smallFont.getData().setScale(2f);

        justLaunched = true;
        gameOver = false;

    }

    @Override
    public void render () {

        Gdx.gl.glClearColor(.25f, .25f, .25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 0, 1);
        int currentRadius = circleRadius;
        for (int i = 0; i < Config.CIRCLES_NUMBER; i++) {
            shapeRenderer.circle((float) screenCenterX, (float) screenCenterY, currentRadius);
            currentRadius -= ringWidth;
        }
        shapeRenderer.end();

        if(!gameOver && !justLaunched) {

            if (Gdx.input.isTouched()) {

                int inputXPos = Gdx.input.getX();
                int inputYPos = Gdx.graphics.getHeight() - Gdx.input.getY();
                rect.setPosition(inputXPos, inputYPos);
                boolean hitFound = false;

                for (int i = currentGroupIndex; i < planeGroups.size(); i++) {
                    PlaneGroup planeGroup = planeGroups.get(i);
                    int level = planeGroup.getLevel();
                    List<Plane> planes = planeGroup.getPlanes();
                    for (int j = 0; j < planes.size(); j++) {
                        Plane plane = planes.get(j);
                        float planeXPos = ringPositionArrays.get(level).get(plane.getAnglePositionIndex()).getxPos();
                        float planeYPos = ringPositionArrays.get(level).get(plane.getAnglePositionIndex()).getyPos();
                        Rectangle planeRect = new Rectangle(planeXPos - planeRadius,
                                                            planeYPos - planeRadius,
                                                         2 * planeRadius,
                                                        2 * planeRadius);

                        if (planeRect.overlaps(rect)) {
                            planes.remove(j);
                            plane.setExplosionTime(TimeUtils.nanoTime());
                            plane.setCurrentXPos(planeXPos);
                            plane.setCurrentYPos(planeYPos);
                            explodedPlanes.add(plane);
                            shotSound.play();
                            if (plane.getType() == 1) {
                                score--;
                            } else {
                                score++;
                            }

                            hitFound = true;
                            break;
                        }
                    }
                    if (hitFound) break;
                }
            }

            if (TimeUtils.timeSinceNanos(startTime) > deltaTime) {
                deltaTime -= Config.ACCEL_TIME;
                for (int i = currentGroupIndex; i < planeGroups.size(); i++) {
                    PlaneGroup planeGroup = planeGroups.get(i);
                    int currentGroupLevel = planeGroup.getLevel();
                    if (currentGroupLevel < ringPositionArrays.size() - 1) {
                        planeGroup.setLevel(currentGroupLevel + 1);
                    } else {
                        planeGroup.getPlanes().clear();
                        if (i == 0) {
                            gameOver = true;
                        }
                    }
                }
                startTime = TimeUtils.nanoTime();
                currentGroupIndex--;
                if (currentGroupIndex < 0) {
                    currentGroupIndex = 0;
                }
            }

        }

        batch.begin();


        smallFont.draw(batch, "Tap green planes to increase score.", 50, Gdx.graphics.getHeight() - 100);
        smallFont.draw(batch, "Avoid red planes.", 50, Gdx.graphics.getHeight() - 140);


        if (justLaunched) {
            if(Gdx.input.justTouched()) {
                int inputXPos = Gdx.input.getX();
                int inputYPos = Gdx.graphics.getHeight() - Gdx.input.getY();

                if (inputXPos >= 50 && inputYPos <= 150) {
                    startGame();
                }
            }

            font.draw(batch, "Welcome to radar game!", 50, 150);
            font.draw(batch, "Tap here to start.", 50, 100);
        }  else {
            if (!gameOver) {
                font.draw(batch, "Your score: " + score, 50, 150);

                for (int i = explodedPlanes.size() - 1; i > -1; i--) {
                    Plane plane = explodedPlanes.get(i);
                    if (TimeUtils.timeSinceNanos(plane.getExplosionTime()) > Config.EXPLOSION_DURATION) {
                        explodedPlanes.remove(i);
                    } else {
                        batch.draw(explosionImage, plane.getCurrentXPos() - planeRadius, plane.getCurrentYPos() - planeRadius, planeRadius * 2, planeRadius * 2);
                    }
                }

                for (int i = currentGroupIndex; i < planeGroups.size(); i++) {
                    PlaneGroup planeGroup = planeGroups.get(i);
                    int level = planeGroup.getLevel();
                    if (level < ringPositionArrays.size()) {
                        List<Plane> planes = planeGroup.getPlanes();
                        for (int j = 0; j < planes.size(); j++) {
                            Plane plane = planes.get(j);
                            float planeXPos = ringPositionArrays.get(level).get(plane.getAnglePositionIndex()).getxPos();
                            float planeYPos = ringPositionArrays.get(level).get(plane.getAnglePositionIndex()).getyPos();
                            Texture planeImage =  (plane.getType() == 1) ? redPlaneImage : greenPlaneImage;
                            batch.draw(planeImage, planeXPos - planeRadius, planeYPos - planeRadius, planeRadius * 2, planeRadius * 2);
                        }
                    }
                }

            } else {
                if(Gdx.input.justTouched()) {
                    int inputXPos = Gdx.input.getX();
                    int inputYPos = Gdx.graphics.getHeight() - Gdx.input.getY();

                    if (inputXPos >= 50 && inputYPos <= 150) {
                        startGame();
                    }
                }
                font.draw(batch, "Game Over! Your score was: " + score, 50, 150);
                font.draw(batch, "Tap here to restart.", 50, 100);
            }
        }
        batch.end();
    }

    @Override
    public void dispose () {
        shapeRenderer.dispose();
        batch.dispose();
        redPlaneImage.dispose();
        greenPlaneImage.dispose();
        font.dispose();
        smallFont.dispose();
    }

    private void startGame()
    {
        score = 0;
        gameOver = false;
        justLaunched = false;

        planeGroups.clear();
        for (int i = 0; i < Config.GROUP_NUMBER; i++) {
            PlaneGroup planeGroup = new PlaneGroup(3, angles.size());
            planeGroup.setLevel(0);
            planeGroups.add(planeGroup);
        }

        currentGroupIndex = planeGroups.size() - 1;

        startTime = TimeUtils.nanoTime();
        deltaTime = Config.DELTA_TIME;
    }
}