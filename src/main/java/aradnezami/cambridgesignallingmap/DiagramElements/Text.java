package aradnezami.cambridgesignallingmap.DiagramElements;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Text class is used to conveniently draw text multiple lines of text on the diagram
 * using 2 fonts found in the resource folder. Lines are delimited by {@code \n} characters
 */
public class Text {
    public static final int HEADCODE_FONT = 0;
    public static final int GENERAL_FONT = 1;
    public static final int ARIAL_FONT = 2;

    public static final Color DEFAULT_COLOUR = new Color(100,100,100);

    private static boolean areFontsInitialized = false;


    private final Color fontColour;
    private final int fontSize;
    @MagicConstant(intValues = {HEADCODE_FONT, GENERAL_FONT, ARIAL_FONT})
    private final int font;

    public final String name;
    @NotNull
    public String text;

    public final int x;
    public final int y;

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
     * @throws FontLoadingException If the fonts could not be successfully loaded
     * @throws IllegalArgumentException If the fontType is not {@link #HEADCODE_FONT} or {@link #GENERAL_FONT}
     */
    public Text(
            @NotNull String text,
            int x,
            int y,
            Color colour,
            int fontSize,
            @MagicConstant(intValues = {HEADCODE_FONT, GENERAL_FONT, ARIAL_FONT}) int fontType) {

        this.x = x;
        this.y = y;
        this.fontColour = colour;
        this.fontSize = fontSize;
        this.name = text;
        this.text = text;
        if (fontType != HEADCODE_FONT && fontType != GENERAL_FONT && fontType != ARIAL_FONT) {
            throw new IllegalArgumentException("Invalid font type: " + fontType + " text="+text);
        }
        this.font = fontType;


        initialiseFonts();
    }



    private void initialiseFonts() throws FontLoadingException {
        if (areFontsInitialized) {
            return;
        }

        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        ClassLoader classLoader = getClass().getClassLoader();

        InputStream headcodeFontStream = classLoader.getResourceAsStream("PixeloidMono-d94EV.ttf");
        if (headcodeFontStream == null) {
            throw new FontLoadingException("Could not find headcode font file");
        }
        Font headcodeFont;
        try {
            headcodeFont = Font.createFont(Font.TRUETYPE_FONT, headcodeFontStream);
        } catch (FontFormatException | IOException e) {
            throw new FontLoadingException("Could not load headcode font", e);
        }
        try {
            headcodeFontStream.close();
        } catch (IOException e) {
            throw new FontLoadingException("Could not close headcode font file loader", e);
        }
        graphicsEnvironment.registerFont(headcodeFont);

        InputStream generalFontStream = classLoader.getResourceAsStream("HomeVideo-BLG6G.ttf");
        if (generalFontStream == null) {
            throw new FontLoadingException("Could not find general font file");
        }
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, generalFontStream);
        } catch (FontFormatException | IOException e) {
            throw new FontLoadingException("Could not load general font", e);
        }
        graphicsEnvironment.registerFont(font);

        areFontsInitialized = true;
    }


    /**
     * Draws the text on the given graphics context
     * @param g2d The graphics to draw on
     */
    public void draw(Graphics2D g2d) {
        g2d.setColor(fontColour);
        String fontName = switch(font)  {
            case HEADCODE_FONT -> "Pixeloid Mono";
            case GENERAL_FONT-> "Home Video";
            case ARIAL_FONT -> "Arial";
            default -> "Arial";
        };
        g2d.setFont(new Font(fontName, Font.PLAIN, (int) Math.ceil(fontSize * ElementCollection.scale)));

        String[] lines = text.split("\n");

        int lineY = fontSize;
        for (String line : lines) {
            g2d.drawString(
                    line,
                    (int) Math.ceil((x) * ElementCollection.scale),
                    (int) Math.ceil((y + lineY) * ElementCollection.scale)
            );
            lineY += fontSize;
        }
    }

}
