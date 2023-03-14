package com.github.graworks.radar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.graworks.radar.utils.FontGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import space.earlygrey.shapedrawer.ShapeDrawer;

public class RadarGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture redPlaneImage;
    private Texture greenPlaneImage;
    private Texture explosionImage;
    private Texture soundOnImage;
    private Texture soundOffImage;

    private Sound shotSound;
    private Music radarSound;
    private BitmapFont regularFont;
    private BitmapFont smallFont;
    private BitmapFont headerFont;

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
    private boolean soundOn = true;
    private I18NBundle langBundle;

    @Override
    public void create () {
        greenPlaneImage = new Texture("green_plane.png");
        redPlaneImage = new Texture("red_plane.png");
        explosionImage = new Texture("explosion.png");
        shotSound = Gdx.audio.newSound(Gdx.files.internal("shot.wav"));

        radarSound = Gdx.audio.newMusic(Gdx.files.internal("radar.wav"));

        soundOnImage = new Texture("sound_on.png");
        soundOffImage = new Texture("sound_off.png");

        batch = new SpriteBatch();

        screenCenterX = Gdx.graphics.getWidth() / 2;
        screenCenterY = Gdx.graphics.getHeight() / 2;
        circleRadius = (screenCenterX > screenCenterY) ? screenCenterY - Config.CIRCLE_MARGIN :
                                        screenCenterX - Config.CIRCLE_MARGIN;
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

        prepareRingPositionArrays();
        prepareFonts();

        justLaunched = true;
        gameOver = false;

        FileHandle internal = Gdx.files.internal("i18n/lang");
        langBundle = I18NBundle.createBundle(internal, Locale.getDefault());
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(Config.SCREEN_BG_COLOR_RED,
                            Config.SCREEN_BG_COLOR_GREEN,
                            Config.SCREEN_BG_COLOR_BLUE,
                            Config.SCREEN_BG_COLOR_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(!gameOver && !justLaunched) {
            if (Gdx.input.isTouched()) {
                handleScreenTouch();
            }
            if (TimeUtils.timeSinceNanos(startTime) > deltaTime) {
                deltaTime -= Config.ACCEL_TIME;
                recalculatePlaneGroups();
                startTime = TimeUtils.nanoTime();
                currentGroupIndex--;
                if (currentGroupIndex < 0) {
                    currentGroupIndex = 0;
                }
            }
        }

        ShapeDrawer shapeDrawer = new ShapeDrawer(
                batch,
                new TextureRegion(
                        new Texture(Gdx.files.internal("white_pixel.png")),
                        1, 1, 1, 1
                ));

        batch.begin();

        Texture soundImage = (soundOn) ? soundOnImage : soundOffImage;
        batch.draw(soundImage,
                Gdx.graphics.getWidth() - Config.SCREEN_BORDER * 2,
                Gdx.graphics.getHeight() - Config.SCREEN_BORDER * 2,
                   Config.SCREEN_BORDER, Config.SCREEN_BORDER);

        shapeDrawer.setDefaultLineWidth(3.0f);

        shapeDrawer.setColor(Config.CIRCLE_BORDERS_COLOR);
        shapeDrawer.filledCircle((float) screenCenterX, (float) screenCenterY, circleRadius, Config.CIRCLE_BG_COLOR);
        int currentRadius = circleRadius;
        for (int i = 0; i < Config.CIRCLES_NUMBER; i++) {
            shapeDrawer.circle((float) screenCenterX, (float) screenCenterY, currentRadius);
            currentRadius -= ringWidth;
        }

        headerFont.draw(batch, langBundle.get("game_title"), Config.SCREEN_BORDER, Gdx.graphics.getHeight() - Config.TOP_HEADER_YPOS);
        smallFont.draw(batch, langBundle.get("tap_green_planes"), Config.SCREEN_BORDER, Gdx.graphics.getHeight() - Config.TOP_LINE1_YPOS);
        smallFont.draw(batch, langBundle.get("avoid_red_planes"), Config.SCREEN_BORDER, Gdx.graphics.getHeight() - Config.TOP_LINE2_YPOS);
        if (justLaunched) {
            if(Gdx.input.justTouched()) {
                int inputXPos = Gdx.input.getX();
                int inputYPos = Gdx.graphics.getHeight() - Gdx.input.getY();
                if (inputXPos >= Config.SCREEN_BORDER && inputYPos <= Config.LINE1_YPOS) {
                    startGame();
                } else if (inputXPos >= Gdx.graphics.getWidth() - Config.SOUND_AREA_TOUCH_SIZE
                        && inputYPos >= Gdx.graphics.getHeight() - Config.SOUND_AREA_TOUCH_SIZE) {
                    soundOn = !soundOn;
                }
            }
            regularFont.draw(batch, langBundle.get("welcome"), Config.SCREEN_BORDER, Config.LINE1_YPOS);
            regularFont.draw(batch, langBundle.get("tap_to_start"), Config.SCREEN_BORDER,  Config.LINE2_YPOS);
        }  else {
            if (!gameOver) {
                if (soundOn) {
                    if (!radarSound.isPlaying()) {
                        radarSound.setLooping(true);
                        radarSound.play();
                    }
                } else {
                    if (radarSound.isPlaying()) {
                        radarSound.stop();
                    }
                }
                regularFont.draw(batch,
                        langBundle.format("your_score", score, Config.GROUP_NUMBER * Config.GROUP_SIZE / 2),
                        Config.SCREEN_BORDER, Config.LINE2_YPOS);
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

                    if (inputXPos >= Config.SCREEN_BORDER && inputYPos <= Config.LINE1_YPOS) {
                        startGame();
                    } else if (inputXPos >= Gdx.graphics.getWidth() - Config.SOUND_AREA_TOUCH_SIZE && inputYPos >= Gdx.graphics.getHeight() - Config.SOUND_AREA_TOUCH_SIZE) {
                        soundOn = !soundOn;
                    }
                }
                regularFont.draw(batch,
                        langBundle.get("tap_to_restart"),
                        Config.SCREEN_BORDER,
                        Config.LINE2_YPOS);
                regularFont.draw(batch,
                                langBundle.format("game_over_and_score", score, Config.GROUP_NUMBER * Config.GROUP_SIZE / 2),
                                Config.SCREEN_BORDER,
                                Config.LINE1_YPOS);
            }
        }
        batch.end();
    }

    @Override
    public void dispose () {
        batch.dispose();
        redPlaneImage.dispose();
        greenPlaneImage.dispose();
        regularFont.dispose();
        smallFont.dispose();
    }

    private void startGame() {
        score = 0;
        gameOver = false;
        justLaunched = false;
        planeGroups.clear();
        for (int i = 0; i < Config.GROUP_NUMBER; i++) {
            PlaneGroup planeGroup = new PlaneGroup(Config.GROUP_SIZE, angles.size());
            planeGroup.setLevel(0);
            planeGroups.add(planeGroup);
        }
        currentGroupIndex = planeGroups.size() - 1;
        startTime = TimeUtils.nanoTime();
        deltaTime = Config.DELTA_TIME;

        if (soundOn) {
            radarSound.setLooping(true);
            radarSound.play();
        }

    }

    private void prepareRingPositionArrays() {
        int distanceFromCenter;
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
    }

    private void handleScreenTouch() {
        int inputXPos = Gdx.input.getX();
        int inputYPos = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (inputXPos >= Gdx.graphics.getWidth() - Config.SOUND_AREA_TOUCH_SIZE && inputYPos >= Gdx.graphics.getHeight() - Config.SOUND_AREA_TOUCH_SIZE) {
            soundOn = !soundOn;
        }

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
                    if (soundOn) {
                        shotSound.play();
                    }
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

    private void recalculatePlaneGroups() {
        for (int i = currentGroupIndex; i < planeGroups.size(); i++) {
            PlaneGroup planeGroup = planeGroups.get(i);
            int currentGroupLevel = planeGroup.getLevel();
            if (currentGroupLevel < ringPositionArrays.size() - 1) {
                planeGroup.setLevel(currentGroupLevel + 1);
            } else {
                planeGroup.getPlanes().clear();
                if (i == 0) {
                    gameOver = true;
                    if (radarSound.isPlaying()) {
                        radarSound.stop();
                    }
                }
            }
        }
    }

    private void prepareFonts() {
        regularFont = FontGenerator.getFont("fonts/Roboto-Regular.ttf", Locale.getDefault().getLanguage(),40, Config.REGULAR_FONT_COLOR);
        smallFont = FontGenerator.getFont("fonts/Roboto-Regular.ttf", Locale.getDefault().getLanguage(),38, Config.SMALL_FONT_COLOR);
        headerFont = FontGenerator.getFont("fonts/Roboto-Regular.ttf", Locale.getDefault().getLanguage(),68, Config.HEADER_FONT_COLOR);
    }
}