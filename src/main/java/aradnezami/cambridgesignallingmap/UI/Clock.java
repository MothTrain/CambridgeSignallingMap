package aradnezami.cambridgesignallingmap.UI;

import aradnezami.cambridgesignallingmap.UI.DiagramElements.ElementCollection;
import aradnezami.cambridgesignallingmap.UI.DiagramElements.Text;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A display clock showing the current time and the time of the last message received from
 * the feed. The time is set to GMT for both clocks.
 */
public class Clock extends JComponent {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static final int WIDTH = 160;
    private static final int HEIGHT = 65;

    private static final int MAIN_FONT_SIZE = 24;
    private static final int LAST_MSG_FONT_SIZE = 8;

    private final Text mainClockText;
    private final Text lastMsgClockText;

    private String currentTime;
    private String lastMsgTime = "Last Message: --:--:--";


    /**
     * Creates an instance of the clock
     */
    public Clock() {
        currentTime = TIME_FORMAT.format(new Date());

        mainClockText = new Text(
                currentTime,
                20, 7,
                Color.GRAY,
                MAIN_FONT_SIZE,
                Text.GENERAL_FONT
        );

        lastMsgClockText = new Text(
                lastMsgTime,
                23, 42,
                Color.GRAY,
                LAST_MSG_FONT_SIZE,
                Text.GENERAL_FONT
        );

        // Update main clock every second
        Timer timer = new Timer(1000, e -> {
            currentTime = TIME_FORMAT.format(new Date());
            mainClockText.text = currentTime;
            repaint();
        });
        timer.start();
    }


    /**
     * Sets the time of the "Last Message" clock to the current time in GMT
     */
    public void updateLastMsgTime() {
        lastMsgTime = TIME_FORMAT.format(new Date());
        if (lastMsgClockText != null) {
            lastMsgClockText.text = "Last Message: " + lastMsgTime;
        }
        repaint();
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, (int) (WIDTH* ElementCollection.scale), (int) (HEIGHT*ElementCollection.scale));

        if (mainClockText != null) mainClockText.draw(g2);
        if (lastMsgClockText != null) lastMsgClockText.draw(g2);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) (WIDTH*ElementCollection.scale), (int) (HEIGHT*ElementCollection.scale));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }


}
