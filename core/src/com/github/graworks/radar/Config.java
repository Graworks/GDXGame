package com.github.graworks.radar;

import com.badlogic.gdx.graphics.Color;

public class Config {
    // circles and angles constants
    public static final int CIRCLES_NUMBER = 6;
    public static final int DELTA_ANGLE = 15;

    // time constants
    public static final long DELTA_TIME = 1200000000L;
    public static final long ACCEL_TIME = 0L;
    public static final long EXPLOSION_DURATION = 300000000L;

    // plane numbers
    public static final int GROUP_NUMBER = 14;
    public static final int GROUP_SIZE = 6;

    // sizes
    public static final int CIRCLE_MARGIN = 20;
    public static final int SCREEN_BORDER = 50;
    public static final int LINE1_YPOS = 150;
    public static final int LINE2_YPOS = 100;
    public static final int SOUND_AREA_TOUCH_SIZE = 200;
    public static final int TOP_HEADER_YPOS = 55;
    public static final int TOP_LINE1_YPOS = 125;
    public static final int TOP_LINE2_YPOS = 165;

    // colors
    public static final float SCREEN_BG_COLOR_RED = 25 / 255.0f;
    public static final float SCREEN_BG_COLOR_GREEN = 43 / 255.0f;
    public static final float SCREEN_BG_COLOR_BLUE = 21 / 255.0f;
    public static final float SCREEN_BG_COLOR_ALPHA = 1;
    public static final Color CIRCLE_BORDERS_COLOR = new Color(148 / 255.0f, 254 / 255.0f, 66 / 255.0f, 1);
    public static final Color CIRCLE_BG_COLOR = new Color(43 / 255.0f, 75 / 255.0f, 36 / 255.0f, 1);
    public static final Color REGULAR_FONT_COLOR = Color.WHITE;
    public static final Color SMALL_FONT_COLOR = Color.LIGHT_GRAY;
    public static final Color HEADER_FONT_COLOR = Color.RED;
}
