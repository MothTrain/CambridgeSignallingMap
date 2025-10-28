package aradnezami.cambridgesignallingmap.UI;

import javax.swing.*;
import java.awt.*;

/**
 * A panel displaying a {@link DiagramPanel}, {@link Clock} and some display text. This is meant
 * to be the primary way for users to display the live diagram application
 */
public class LiveDiagramPanel extends JPanel {
    private static final Color BACKGROUND_COLOUR = new Color(20, 20, 20);

    private DiagramPanel diagramPanel;
    private Clock clock = new Clock();


    /**
     * Creates a LiveDiagramPanel using the given {@link DiagramPanel}
     * @param diagramPanel The diagram to display
     */
    public LiveDiagramPanel(DiagramPanel diagramPanel) {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOUR);


        JPanel topLabelsPanel = new JPanel();
        topLabelsPanel.setBackground(BACKGROUND_COLOUR);
        topLabelsPanel.setLayout(new BoxLayout(topLabelsPanel, BoxLayout.Y_AXIS));

        JLabel appNameLabel = new JLabel("Cambridge Area Signalling Map");
        appNameLabel.setFont(new Font("Arial", Font.PLAIN, 34));
        appNameLabel.setForeground(Color.GRAY);

        JLabel developerLabel = new JLabel("By Arad Nezami (MothTrain)");
        developerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        developerLabel.setForeground(Color.GRAY);

        appNameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        developerLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        developerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topLabelsPanel.add(Box.createVerticalGlue());
        topLabelsPanel.add(appNameLabel);
        topLabelsPanel.add(Box.createVerticalStrut(5));
        topLabelsPanel.add(developerLabel);
        topLabelsPanel.add(Box.createVerticalGlue());

        JPanel topPanel = new JPanel(new BorderLayout());
        clock.setBorder(BorderFactory.createMatteBorder(15,15,15,15, BACKGROUND_COLOUR));

        topPanel.add(clock, BorderLayout.WEST);
        topPanel.add(topLabelsPanel, BorderLayout.CENTER);


        add(diagramPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        this.diagramPanel = diagramPanel;
        this.clock = clock;
    }


    public void setDiagramPanel(DiagramPanel diagramPanel) {
        this.diagramPanel = diagramPanel;
    }

    /**
     * Sets the "Last Message" clock to the current time. This is to be used when a message is successfully
     * received from the feed
     */
    public void updateLastMsgClock() {
        clock.updateLastMsgTime();
    }
}
