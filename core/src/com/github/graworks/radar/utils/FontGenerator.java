package com.github.graworks.radar.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.Map;

public class FontGenerator {

    private static final Map<String, String> languageCharacters;
    static {
        languageCharacters = new HashMap<>();
        languageCharacters.put("ru", "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
                                        + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
                                        + "1234567890.,:;_¡!¿?\"'+-*/()[]={}");
    }

    public static BitmapFont getFont(String fontName, String lang, int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontName));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        float ratio = Gdx.graphics.getWidth() / 960f;
        fontParameter.size = (int)(size * ratio);
        fontParameter.color = color;
        fontParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        if (languageCharacters.containsKey(lang)) {
            fontParameter.characters += languageCharacters.get(lang);
        }
        BitmapFont font = generator.generateFont(fontParameter);
        generator.dispose();
        return font;
    }
}
