package aradnezami.cambridgesignallingmap.UI;

import aradnezami.cambridgesignallingmap.UI.DiagramElements.*;
import aradnezami.cambridgesignallingmap.UI.DiagramElements.Point;
import org.intellij.lang.annotations.MagicConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelListener;

public class DiagramPanel extends JScrollPane {
    private ElementCollection elementCollection;
    private final DrawingSurface drawingSurface;

    public DiagramPanel(ElementCollection elementCollection) {
        this.elementCollection = elementCollection;

        drawingSurface = new DrawingSurface();
        setViewportView(drawingSurface);

        // Flips shift scroll behaviour. Regular scroll now moves horizontally
        for (MouseWheelListener listener : getMouseWheelListeners()) {
            removeMouseWheelListener(listener);
        }

        addMouseWheelListener(e -> {
            JScrollBar toScroll;

            if (e.isShiftDown()) {
                toScroll = getVerticalScrollBar();
            } else {
                toScroll = getHorizontalScrollBar();
            }

            if (toScroll == null || !toScroll.isVisible()) {
                return;
            }

            int rotation = e.getUnitsToScroll();
            int increment = toScroll.getUnitIncrement(rotation > 0 ? 1 : -1);

            toScroll.setValue(toScroll.getValue() + rotation * increment);
            e.consume();
        });
        getHorizontalScrollBar().setUnitIncrement(20);
        getVerticalScrollBar().setUnitIncrement(20);

        setBorder(BorderFactory.createEmptyBorder());
    }

    /**
     * Sets the train describer stored in the berth, which the berthName corresponds
     * to, to the given describer. If the berthName does not refer to a berth that
     * exists, no action is taken and false is returned
     * @param berthName Name of the berth
     * @param description New train describer
     * @return True if a berth with the corresponding name was found, false otherwise
     * @throws IllegalArgumentException If the train describer was not 0 or 4 characters
     * 
     * @see Berth#setDescriber(String)
     */
    public boolean setBerth(String berthName, String description) {
        Berth berth = elementCollection.getBerth(berthName);
        if (berth == null) {
            return false;
        }
        berth.setDescriber(description);
        return true;
    }

    /**
     * Sets the aspect of the signal, which the signal name corresponds to, to
     * the given state. If no signal with the given name is found, no action is
     * taken and false is returned
     * @param name Name of the signal
     * @param state New state of the signal. Allowed values: {@link Signal#ON},
     * {@link Signal#MAIN_OFF}, {@link Signal#SHUNT_OFF}, {@link Signal#BOTH_OFF}
     * @return True if a signal with the corresponding name was found, false otherwise
     * @throws IllegalArgumentException If the state was not valid for the type of
     * signal
     */
    public boolean setSignalAspect(String name,
                             @MagicConstant(intValues = {Signal.ON, Signal.MAIN_OFF, Signal.SHUNT_OFF, Signal.BOTH_OFF}) int state) {
        Signal signal = elementCollection.getSignal(name);
        if (signal == null) {
            return false;
        }
        signal.setAspectState(state);
        return true;
    }

    /**
     * Sets the route state of the signal, which the signal name corresponds to, to
     * the given state. If no signal with the given name is found, no action is
     * taken and false is returned
     * @param name Name of the signal
     * @param state New routing state of the signal. Allowed values: {@link Signal#ROUTE_SET}, {@link Signal#ROUTE_NOT_SET}
     * @return True if a signal with the corresponding name was found, false otherwise 
     * @throws IllegalArgumentException If an invalid state was provided
     * */
    public boolean setSignalRouting(String name,
                                    @MagicConstant(intValues = {Signal.ROUTE_SET, Signal.ROUTE_NOT_SET}) int state) {
        Signal signal = elementCollection.getSignal(name);
        if (signal == null) {
            return false;
        }
        signal.setRoutedState(state);
        return true;
    }

    /**
     * Sets the point state of the point which the name corresponds to, to
     * the given state. If no point with the given name is found, no action is
     * taken and false is returned
     * @param name Name of the point
     * @param state New state of the point. Allowed values: {@link Point#REVERSE}, {@link Point#NORMAL},
     * {@link Point#BOTH}, {@link Point#NEITHER}
     * @return True if a point with the corresponding name was found, false otherwise 
     * @throws IllegalStateException If an invalid state was provided
     * */
    public boolean setPointState(String name,
                                 @MagicConstant(intValues = {Point.NORMAL, Point.REVERSE,
                                 Point.BOTH, Point.NEITHER}) int state) {
        Point point = elementCollection.getPoint(name);
        if (point == null) {
            return false;
        }
        point.setState(state);
        return true;
    }

    /**
     * Sets the route state of the route which the name corresponds to, to
     * the given state. If no route with the given name is found, no action is
     * taken and false is returned
     * @param name Name of the route
     * @param state New state of the route. Allowed values: {@link Route#SET}, {@link Route#NOTSET}
     * @return True if a route with the corresponding name was found, false otherwise 
     * @throws IllegalStateException If an invalid state was provided
     * */
    public boolean setRouteState(String name,
                                 @MagicConstant(intValues = {Route.SET, Route.NOTSET}) int state) {
        Route route = elementCollection.getRoute(name);
        if (route == null) {
            return false;
        }
        route.setState(state);
        return true;
    }

    /**
     * Sets the track circuit state of the track circuit which the name corresponds to, to
     * the given state. If no track circuit with the given name is found, no action is
     * taken and false is returned
     * @param name Name of the track circuit
     * @param state New state of the track circuit. Allowed values: {@link TrackCircuit#OCCUPIED},
     * {@link TrackCircuit#OCCUPIED}
     * @return True if a track circuit with the corresponding name was found, false otherwise 
     * @throws IllegalStateException If an invalid state was provided
     * */
    public boolean setTrackCircuitState(String name,
                                        @MagicConstant(intValues = {TrackCircuit.OCCUPIED, TrackCircuit.UNOCCUPIED}) int state) {
        TrackCircuit trackCircuit = elementCollection.getTrackCircuit(name);
        if (trackCircuit == null) {
            return false;
        }
        trackCircuit.setState(state);
        return true;
    }


    public void setElements(ElementCollection newElements) {
        elementCollection = newElements;
    }



    private class DrawingSurface extends JPanel {
        public DrawingSurface() {
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (elementCollection != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                elementCollection.draw(g2d);
                g2d.dispose();
            }
        }

        @Override
        public Dimension getPreferredSize() {
             return elementCollection.getSize();
        }
    }
}
