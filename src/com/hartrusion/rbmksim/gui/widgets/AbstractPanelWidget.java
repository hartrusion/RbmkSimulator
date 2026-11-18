/*
 * Copyright (C) 2025 Viktor Alexander Hartung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hartrusion.rbmksim.gui.widgets;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import com.hartrusion.control.ValveState;
import com.hartrusion.mvc.ActionReceiver;
import com.hartrusion.mvc.UpdateReceiver;
import com.hartrusion.rbmksim.gui.GuiTheme;
import com.hartrusion.rbmksim.gui.InteractiveComponent;
import com.hartrusion.rbmksim.gui.elements.ChornobylMetalTheme;

/**
 * Base class for panels that are used with the MVC pattern with switches.
 * Provides methods to set the state colors of the panel buttons and an
 * instance of an action receiver (usually the controller instance).
 * <p>
 * The panel buttons exist in the types green, red, gray, yellow and white,
 * and each type has a normal and a highlighted state. The colors of these
 * states are not defined here but in the ChornobylMetalTheme of the active
 * GUI theme, so that a dark theme can provide its own button colors.
 *
 * @author Viktor Alexander Hartung
 */
public abstract class AbstractPanelWidget extends javax.swing.JPanel
        implements InteractiveComponent {

    protected ActionReceiver controller;

    @Override
    public void registerActionReceiver(ActionReceiver controller) {
        this.controller = controller;
    }

    /**
     * Updates a set of two buttons when receiving a property change event that
     * contains a ValveState. This gets created in ValveActuatorMonitor and this
     * method can be called to process the event to display the new valves state
     * with an open and close button (red and green)
     *
     * @param buttonClose Green Close-Button
     * @param buttonOpen Red Open-Button
     * @param newState Object that can be casted to ValveState
     */
    protected void setValveButtons(JButton buttonClose, JButton buttonOpen,
            Object newState) {
        if (newState instanceof ValveState) {
            switch ((ValveState) newState) {
                case CLOSED:
                    setGreenButtonColor(buttonClose, true);
                    setRedButtonColor(buttonOpen, false);
                    break;
                case OPEN:
                    setGreenButtonColor(buttonClose, false);
                    setRedButtonColor(buttonOpen, true);
                    break;
                case INTERMEDIATE:
                    setGreenButtonColor(buttonClose, false);
                    setRedButtonColor(buttonOpen, false);
                    break;
            }
        }
    }
    
    /**
     * Updates a set of two buttons when receiving a property change event that
     * contains a ValveState. This gets created in ValveActuatorMonitor and this
     * method can be called to process the event to display the new valves state
     * with an open and close button (gray and green). Its named "less" as it 
     * is less colorful, was not intended in initial architecture.
     *
     * @param buttonClose Gray Close-Button
     * @param buttonOpen Red Open-Button
     * @param newState Object that can be casted to ValveState
     */
    protected void setValveButtonsLess(JButton buttonClose, JButton buttonOpen,
            Object newState) {
        if (newState instanceof ValveState) {
            switch ((ValveState) newState) {
                case CLOSED:
                    setGrayButtonColor(buttonClose, true);
                    setRedButtonColor(buttonOpen, false);
                    break;
                case OPEN:
                    setGrayButtonColor(buttonClose, false);
                    setRedButtonColor(buttonOpen, true);
                    break;
                case INTERMEDIATE:
                    setGrayButtonColor(buttonClose, false);
                    setRedButtonColor(buttonOpen, false);
                    break;
            }
        }
    }

    /**
     * Returns the Metal theme of the active GUI theme, which holds the colors
     * of the panel buttons. The theme is read on each call instead of being
     * kept in a field, because the GUI theme can be switched while the
     * application runs.
     * <p>
     * LLM code generated with Claude Opus 5.5
     *
     * @return Metal theme of the active GUI theme
     */
    private ChornobylMetalTheme getActiveChornobylMetalTheme() {
        return GuiTheme.getActiveTheme().getChornobylMetalTheme();
    }

    /**
     * Used to set green buttons either to highlight or non-highlited state.
     * The colors of all panel button types are taken from the active theme.
     *
     * @param button
     * @param val true for the highlighted state
     */
    protected void setGreenButtonColor(JButton button, boolean val) {
        ChornobylMetalTheme theme = getActiveChornobylMetalTheme();
        if (val) {
            button.setBackground(theme.getGreenButtonHighlight());
        } else {
            button.setBackground(theme.getGreenButtonBackground());
        }
        button.setForeground(theme.getGreenButtonForeground());
    }
    
    /**
     * Used to set gray buttons either to highlight or non-highlited state.
     *
     * @param button
     * @param val true for the highlighted state
     */
    protected void setGrayButtonColor(JButton button, boolean val) {
        ChornobylMetalTheme theme = getActiveChornobylMetalTheme();
        if (val) {
            button.setBackground(theme.getGrayButtonHighlight());
        } else {
            button.setBackground(theme.getGrayButtonBackground());
        }
        button.setForeground(theme.getGrayButtonForeground());
    }

    /**
     * Used to set red buttons either to highlight or non-highlited state.
     *
     * @param button
     * @param val true for the highlighted state
     */
    protected void setRedButtonColor(JButton button, boolean val) {
        ChornobylMetalTheme theme = getActiveChornobylMetalTheme();
        if (val) {
            button.setBackground(theme.getRedButtonHighlight());
        } else {
            button.setBackground(theme.getRedButtonBackground());
        }
        button.setForeground(theme.getRedButtonForeground());
    }

    /**
     * Used to set yellow buttons either to highlight or non-highlited state.
     *
     * @param button
     * @param val true for the highlighted state
     */
    protected void setYellowButtonColor(JButton button, boolean val) {
        ChornobylMetalTheme theme = getActiveChornobylMetalTheme();
        if (val) {
            button.setBackground(theme.getYellowButtonHighlight());
        } else {
            button.setBackground(theme.getYellowButtonBackground());
        }
        button.setForeground(theme.getYellowButtonForeground());
    }
    
    /**
     * Used to set white buttons either to highlight or non-highlited state.
     *
     * @param button
     * @param val true for the highlighted state
     */
    protected void setWhiteButtonColor(JButton button, boolean val) {
        ChornobylMetalTheme theme = getActiveChornobylMetalTheme();
        if (val) {
            button.setBackground(theme.getWhiteButtonHighlight());
        } else {
            button.setBackground(theme.getWhiteButtonBackground());
        }
        button.setForeground(theme.getWhiteButtonForeground());
    }

    /**
     * To set a toggle button when a new state for the button is sent to this
     * gui.
     *
     * @param toggleButton
     * @param val
     */
    protected void setButtonSwitch(JToggleButton toggleButton, boolean val) {
        if (val) {
            toggleButton.setText("←");
            toggleButton.setSelected(true);
        } else {
            toggleButton.setText("↑");
            toggleButton.setSelected(false);
        }
    }

    /**
     * Sets the text icon accordingly for a JToggleButton and returns true or
     * false, depending on what state the button has after pressing it.
     *
     * @param evt button event provided at listener
     * @return true, if button was activated with this press.
     */
    protected boolean toggleButtonChange(java.awt.event.ActionEvent evt) {
        JToggleButton jToggleButton = (JToggleButton) evt.getSource();
        if (jToggleButton.isSelected()) {
            jToggleButton.setText("↑");
            return true;
        } else {
            jToggleButton.setText("←");
            return false;
        }
    }
}
