package aradnezami.cambridgesignallingmap.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

/**
 *
 */
public class LiveDiagramMenuBar extends JMenuBar {
    private static final URI README_MD_LINK = URI.create("https://github.com/MothTrain/CambridgeSignallingMap/blob/master/README.md");

    private final JMenu connectionMenu = new JMenu("Connection");
    private final JMenuItem disconnectAndClose = new JMenuItem("Disconnect and Close");
    private final JMenuItem resetState = new JMenuItem("Reset State");
    private final JMenuItem changeSource = new JMenuItem("Change Source");

    private final JMenu viewMenu = new JMenu("View");
    private final JMenuItem about = new JMenuItem("About");
    private final JMenuItem attribution = new JMenuItem("Attribution");

    public LiveDiagramMenuBar() {
        super();

        connectionMenu.add(disconnectAndClose);
        connectionMenu.add(resetState);
        connectionMenu.add(changeSource);
        add(connectionMenu);

        viewMenu.add(about);
        viewMenu.add(attribution);
        add(viewMenu);

        about.addActionListener((ActionEvent e) -> {
            try {
                openAboutPage();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(getRootPane(), """
                     The about page can be found at:
                     """ + README_MD_LINK, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        attribution.addActionListener((ActionEvent e) -> {AttributionDialogue.display(getRootPane());});
    }

    public void setConnectionMenuEnabled(boolean enable) {
        disconnectAndClose.setEnabled(enable);
        resetState.setEnabled(enable);
        changeSource.setEnabled(enable);
    }


    public void addDisconnectAndCloseListener(ActionListener actionListener) {
        disconnectAndClose.addActionListener(actionListener);
    }

    public void addResetStateListener(ActionListener actionListener) {
        resetState.addActionListener(actionListener);
    }

    public void addChangeSourceListener(ActionListener actionListener) {
        changeSource.addActionListener(actionListener);
    }

    private void openAboutPage() throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(README_MD_LINK);
        } else {
             JOptionPane.showMessageDialog(getRootPane(), """
                     The about page can be found at:
                     """ + README_MD_LINK, "About", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
