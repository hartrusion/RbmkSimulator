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
package com.hartrusion.rbmksim;

import com.hartrusion.rbmksim.gui.diagrams.*;
import java.beans.PropertyChangeEvent;
import com.hartrusion.values.ValueHandler;
import com.hartrusion.mvc.ActionCommand;
import com.hartrusion.mvc.InteractiveView;
import com.hartrusion.mvc.UpdateReceiver;
import com.hartrusion.mvc.ViewerController;
import com.hartrusion.rbmksim.gui.*;
import com.hartrusion.rbmksim.gui.mnemonic.*;
import com.hartrusion.rbmksim.gui.widgets.AbstractPanelWidget;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The main GUI manager, manages the creation of ControlPanel windows and 
 * distributes the data towards them.
 *
 * @author Viktor Alexander Hartung
 */
public class ControlRoom implements InteractiveView {

    private ViewerController controller;

    private FrameRodPositions frameRodPositions;
    private FrameCoreActivity frameCoreActivity;
    private FrameAlarmTable frameAlarms;
    private List alarmList;

    private ValueHandler plotData;

    /**
     * Holds a list of all open control panel windows that are attached to this
     * control room instance directly.
     */
    private final List<ControlPanel> controlPanels = new ArrayList<>();

    /**
     * Creates new form ControlPanel
     */
    public ControlRoom() {
        
    }

    public void displayRodPositionFrame() {
        if (frameRodPositions == null) {
            frameRodPositions = new FrameRodPositions();
            java.awt.EventQueue.invokeLater(() -> {
                frameRodPositions.setVisible(true);
            });
            frameRodPositions.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    frameRodPositions = null;
                }
            });
        }
    }

    public void displayCoreMatrixFrame(java.awt.event.ActionEvent evt) {
        if (frameCoreActivity == null) {
            frameCoreActivity = new FrameCoreActivity();
            java.awt.EventQueue.invokeLater(() -> {
                frameCoreActivity.setVisible(true);
            });
            frameCoreActivity.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    frameCoreActivity = null;
                }
            });
        }
    }

    public void displayAlarmsFrame() {
        if (frameAlarms == null) {
            frameAlarms = new FrameAlarmTable();
            frameAlarms.registerActionReceiver(controller);
            // frameAlarms.setAlarmList(alarmList);
            java.awt.EventQueue.invokeLater(() -> {
                frameAlarms.setVisible(true);
            });
            frameAlarms.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    frameAlarms = null;
                }
            });
        }
    }

    public void displayNewControlPanel() {
        // Generate a new control panel object and register it in the list of 
        // active panels.
        ControlPanel p = new ControlPanel();
        p.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                removeControlPanel((ControlPanel) evt.getComponent());
            }
        });
        p.registerController(controller);
        p.setParent(this);
        p.setVisible(true); // we are on the EDT already
        if (controlPanels.size() == 0) {
            // The first frame gets the reactor control panel displayed.
            p.openReactorControlPanel();
        }
        controlPanels.add(p);
    }
    
    public void removeControlPanel(ControlPanel panel) {
        controlPanels.remove(panel);
        if (controlPanels.size() == 0) {
            // last closed control panel terminates the application.
            System.exit(0); // Terminate java vm
        }
    }

    @Override // Called on startup
    public void registerController(ViewerController controller) {
        this.controller = controller;
        // Use this event to display at least the core controls
    }

    @Override
    public void updateComponent(PropertyChangeEvent evt) {
        // Put all events on the log output for easier monitoring
        Logger.getLogger(ControlRoom.class.getName())
                .log(Level.INFO, "Received PropertyChangeEvent "
                        + evt.getPropertyName() + ", value: "
                        + evt.getNewValue());

        for (UpdateReceiver ur : controlPanels) {
            ur.updateComponent(evt);
        }
    }

    @Override
    public void updateComponent(String propertyName, Object newValue) {
        if (propertyName.equals("OutputValues")) {
            ((ValueHandler) newValue).fireAllToMvcView(this);
            plotData = (ValueHandler) newValue;

            // use this event to update the alarm list also.
            if (frameAlarms != null) {
                frameAlarms.setAlarms(alarmList);
            }
        }

        for (UpdateReceiver ur : controlPanels) {
            ur.updateComponent(propertyName, newValue);
        }

        if (frameCoreActivity != null && propertyName.equals("CoreIndicator#1")) {
            frameCoreActivity.updateDisplay((CoreIndicator) newValue);
        }
    }

    @Override
    public void updateComponent(String propertyName, double newValue) {
        if (frameRodPositions != null) {
            frameRodPositions.updateComponent(propertyName, newValue);
        }

        for (UpdateReceiver ur : controlPanels) {
            ur.updateComponent(propertyName, newValue);
        }        
    }

    @Override
    public void updateComponent(String propertyName, boolean newValue) {
        if (frameRodPositions != null) {
            frameRodPositions.updateComponent(propertyName, newValue);
        }

        for (UpdateReceiver ur : controlPanels) {
            ur.updateComponent(propertyName, newValue);
        }
    }

    public void setAlarmList(List alarmList) {
        // Todo: maybe there's a better way of organizing this.
        this.alarmList = alarmList;
    }
    
    public List getAlarmList() {
        return alarmList;
    }
}
