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

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import com.hartrusion.control.ValveState;
import com.hartrusion.mvc.ActionReceiver;
import com.hartrusion.mvc.UpdateReceiver;

/**
 * Base class for panels that are used with the MVC pattern with switches. 
 * Provides some color constants, methods to set those to the buttons and
 * an instance of an action receiver (usually the controller instance).
 *
 * @author Viktor Alexander Hartung
 */
public abstract class AbstractPanelWidget extends javax.swing.JPanel
        implements UpdateReceiver {

    protected static final Color GREEN = new Color(0, 128, 0);
    protected static final Color LIME = new Color(0, 255, 0);
    protected static final Color DARKRED = new Color(128, 0, 0);
    protected static final Color RED = new Color(255, 0, 0);
    protected static final Color DARKYELLOW = new Color(128, 128, 0);
    protected static final Color YELLOW = new Color(208, 208, 0);

    protected ActionReceiver controller;

    public void registerActionReceiver(ActionReceiver controller) {
        this.controller = controller;
    }
    
    /**
     * Updates a set of two buttons when receiving a property change event 
     * that contains a ValveState. This gets created in ValveActuatorMonitor
     * and this method can be called to process the event to display the new 
     * valves state with an open and close button (red and green)
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
     * Used to set green buttons either to highlight or non-highlited state.
     *
     * @param button
     * @param val
     */
    protected void setGreenButtonColor(JButton button, boolean val) {
        if (val) {
            button.setBackground(LIME);
        } else {
            button.setBackground(GREEN);
        }
    }

    /**
     * Used to set red buttons either to highlight or non-highlited state.
     *
     * @param button
     * @param val
     */
    protected void setRedButtonColor(JButton button, boolean val) {
        if (val) {
            button.setBackground(RED);
        } else {
            button.setBackground(DARKRED);
        }
    }

    /**
     * Used to set yellow buttons either to highlight or non-highlited state.
     *
     * @param button
     * @param val
     */
    protected void setYellowButtonColor(JButton button, boolean val) {
        if (val) {
            button.setBackground(YELLOW);
        } else {
            button.setBackground(DARKYELLOW);
        }
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
            toggleButton.setText("—");
            toggleButton.setSelected(true);
        } else {
            toggleButton.setText("|");
            toggleButton.setSelected(false);
        }
    }

    /**
     * Sets the text icon accordingly for a JToggleButton
     *
     * @param evt
     */
    protected void updateToggleButtonSwitchText(java.awt.event.ActionEvent evt) {
        JToggleButton jToggleButton = (JToggleButton) evt.getSource();
        if (jToggleButton.isSelected()) {
            jToggleButton.setText("|");
        } else {
            jToggleButton.setText("—");
        }
    }
}
