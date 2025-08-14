package aradnezami.cambridgesignallingmap.DiagramElements;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Text class is used to conveniently draw text multiple lines of text on the diagram
 * using 2 fonts found in the resource folder. Lines are delimited by {@code \n} characters
 */
public class Text {
    private static final double scale = 1.5;
    public static final int HEADCODE_FONT = 0;
    public static final int GENERAL_FONT = 1;

    public static final Color DEFAULT_COLOUR = new Color(100,100,100);

    private static boolean areFontsInitialized = false;


    private final Color fontColour;
    private int fontSize;
    @MagicConstant(intValues = {HEADCODE_FONT, GENERAL_FONT})
    private final int font;

    public final String name;
    @NotNull
    public String text;

    private final int x;
    private final int y;

    /**
     * Creates a text object with the following properties. No name is specified. Instead the text
     * also represents the name, so this constructor should only be used if the text's content is
     * unique
     * @param text The text to display. This doubles as the name of the object.
     * @param x The x coordinate of the left of the text
     * @param y The y coordinate of the top of the text
     * @param colour The colour of the text
     * @param fontSize The font size
     * @param fontType The font to be used. Either {@link #GENERAL_FONT} or {@link #HEADCODE_FONT}
     * @throws IOException If there was an error loading the font files
     * @throws FontFormatException If there was a font format issue with the font files
     */
    public Text(
            @NotNull String text,
            int x,
            int y,
            Color colour,
            int fontSize,
            @MagicConstant(intValues = {HEADCODE_FONT, GENERAL_FONT}) int fontType) throws IOException, FontFormatException {

        this.x = x;
        this.y = y;
        this.fontColour = colour;
        this.fontSize = fontSize;
        this.name = text;
        this.text = text;
        if (fontType != HEADCODE_FONT && fontType != GENERAL_FONT) {
            throw new IllegalArgumentException("Invalid font type: " + fontType + " text="+text);
        }
        this.font = fontType;


        initialiseFonts();
    }

    /**
     * Creates a text object with the following properties. A name is specified, so this constructor should
     * be used if the text's content is not unique
     * @param name The name of the object
     * @param text The text to display
     * @param x The x coordinate of the left of the text
     * @param y The y coordinate of the top of the text
     * @param colour The colour of the text
     * @param fontSize The font size
     * @param fontType The font to be used. Either {@link #GENERAL_FONT} or {@link #HEADCODE_FONT}
     * @throws IOException If there was an error loading the font files
     * @throws FontFormatException If there was a font format issue with the font files
     */
    public Text(
            @NotNull String name,
            @NotNull String text,
            int x,
            int y,
            Color colour,
            int fontSize,
            @MagicConstant(intValues = {HEADCODE_FONT, GENERAL_FONT}) int fontType) throws IOException, FontFormatException {

        this.x = x;
        this.y = y;
        this.fontColour = colour;
        this.fontSize = fontSize;
        this.name = name;
        this.text = text;
        if (fontType != HEADCODE_FONT && fontType != GENERAL_FONT) {
            throw new IllegalArgumentException("Invalid font type: " + fontType + " text="+text);
        }
        this.font = fontType;

        initialiseFonts();
    }



    private void initialiseFonts() throws FontFormatException, IOException {
        if (areFontsInitialized) {
            return;
        }

        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        ClassLoader classLoader = getClass().getClassLoader();

        InputStream headcodeFontStream = classLoader.getResourceAsStream("PixeloidMono-d94EV.ttf");
        if (headcodeFontStream == null) {
            throw new FileNotFoundException("Could not find headcode font file");
        }
        Font headcodeFont = Font.createFont(Font.TRUETYPE_FONT, headcodeFontStream);
        headcodeFontStream.close();
        graphicsEnvironment.registerFont(headcodeFont);

        InputStream generalFontStream = classLoader.getResourceAsStream("HomeVideo-BLG6G.ttf");
        if (generalFontStream == null) {
            throw new FileNotFoundException("Could not find general font file");
        }
        Font font = Font.createFont(Font.TRUETYPE_FONT, generalFontStream);
        graphicsEnvironment.registerFont(font);

        areFontsInitialized = true;
    }


    /**
     * Draws the text on the given graphics context
     * @param g2d The graphics to draw on
     */
    public void draw(Graphics2D g2d) {
        g2d.setColor(fontColour);
        String fontName = (font == HEADCODE_FONT) ? "Pixeloid Mono" : "Home Video";
        g2d.setFont(new Font(fontName, Font.PLAIN, (int) Math.ceil(fontSize * scale)));

        String[] lines = text.split("\n");

        int lineY = fontSize;
        for (String line : lines) {
            g2d.drawString(
                    line,
                    (int) Math.ceil((x) * scale),
                    (int) Math.ceil((y + lineY) * scale)
            );
            lineY += fontSize;
        }
    }

}
