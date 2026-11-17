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

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import com.hartrusion.values.ValueHandler;
import com.hartrusion.mvc.InteractiveView;
import com.hartrusion.mvc.UpdateReceiver;
import com.hartrusion.mvc.ViewerController;
import com.hartrusion.rbmksim.gui.AlarmSound;
import com.hartrusion.rbmksim.gui.GuiTheme;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main GUI manager, manages the creation of ControlPanel windows and
 * distributes the data towards them.
 *
 * @author Viktor Alexander Hartung
 */
public class ControlPanelManager implements InteractiveView {

    private ViewerController controller;

    private List alarmList;

    private ValueHandler plotData;

    private boolean isClient;

    /**
     * The application that runs the simulation, which starts the network
     * server on request. It stays null on a client, which has no server.
     */
    private RbmkSimulator application;

    /**
     * Holds a list of all open control panel windows that are attached to this
     * control room instance directly.
     */
    private final List<ControlPanel> controlPanels = new ArrayList<>();

    /**
     * Creates new form ControlPanel
     */
    public ControlPanelManager() {
        plotData = new ValueHandler();
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
        p.setPlotData(plotData);
        if (alarmList != null) {
            p.setAlarmList(alarmList);
        }
        if (isClient) {
            p.setAsClient();
        }
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
        if (controlPanels.isEmpty()) {
            // The alarm sound is only updated by the control panels, so it
            // would keep sounding without one. It is started again by the
            // next control panel that receives the alarm list.
            AlarmSound.update(null);
        }
        // A started server keeps the application running for its network
        // clients, it is then terminated from the ServerView.
        if (controlPanels.size() == 0 && !isServerStarted()) {
            // last closed control panel terminates the application.
            System.exit(0); // Terminate java vm
        }
    }

    /**
     * Switches the GUI to another GUI theme. Swing only applies a look and feel
     * to newly created components, so all windows of the application (control
     * panels, debug frames and dialogs) are disposed and a single new control
     * panel is opened afterwards, which receives the simulation data like the
     * first one did on startup. Disposing does not fire the windowClosing
     * event, so the closing of the last control panel does not terminate the
     * application here. If the GUI theme cannot be installed, the windows are
     * kept open. Called from the theme menu of ControlPanel on the event
     * dispatch thread.
     * <p>
     * LLM code generated with Claude Opus 5.5
     *
     * @param theme GUI theme to switch to
     */
    public void switchTheme(GuiTheme theme) {
        if (!theme.apply()) {
            return;
        }
        controlPanels.clear();
        for (Window window : Window.getWindows()) {
            // The ServerView must stay open because it is the main window of
            // a server session. It contains only standard Swing components,
            // which can be updated to the new GUI theme reliably.
            if (window instanceof ServerView) {
                SwingUtilities.updateComponentTreeUI(window);
                continue;
            }
            window.dispose();
        }
        displayNewControlPanel();
    }

    /**
     * Connects this manager with the application, which is needed to start the
     * network server from a control panel. Called on startup of the
     * standalone and server application, but not on a client.
     * <p>
     * LLM code generated with Claude Opus 5.5
     *
     * @param application application that runs the simulation
     */
    public void registerApplication(RbmkSimulator application) {
        this.application = application;
    }

    /**
     * Starts the network server of the application and opens its ServerView.
     * Called from the menu of ControlPanel.
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    public void startServer() {
        if (application != null) {
            application.startServer();
        }
    }

    /**
     * LLM code generated with Claude Opus 5.5
     *
     * @return true if the network server of the application was started
     */
    private boolean isServerStarted() {
        return application != null && application.isServerStarted();
    }

    public void setAsClient() {
        this.isClient = true;
        for (ControlPanel p : controlPanels) {
            p.setAsClient();
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
        Logger.getLogger(ControlPanelManager.class.getName())
                .log(Level.INFO, "Received PropertyChangeEvent "
                        + evt.getPropertyName() + ", value: "
                        + evt.getNewValue());

        for (UpdateReceiver ur : controlPanels) {
            ur.updateComponent(evt);
        }
    }

    @Override
    public void updateComponent(String propertyName, Object newValue) {
        if (propertyName.equals("OutputSnapshot")) {
            plotData.processSnapshot((com.hartrusion.values.ValueSnapshot) newValue);
            plotData.fireAllToMvcView(this);

            for (ControlPanel p : controlPanels) {
                p.updatePlots();
                p.updateSnapshot((com.hartrusion.values.ValueSnapshot) newValue);
            }
            return;
        }
        // Alarms are getting sent each cycle as a list of alarms. This also 
        // allows recceivin them via network.
        if (propertyName.equals("AlarmListSnapshot")) {
            alarmList = ((com.hartrusion.alarm.AlarmListSnapshot) newValue).toAlarmList();
            for (ControlPanel p : controlPanels) {
                p.setAlarmList(alarmList);
            }
            return;
        }
        for (UpdateReceiver ur : controlPanels) {
            ur.updateComponent(propertyName, newValue);
        }
    }

    @Override
    public void updateComponent(String propertyName, double newValue) {
        for (UpdateReceiver ur : controlPanels) {
            ur.updateComponent(propertyName, newValue);
        }
    }

    @Override
    public void updateComponent(String propertyName, boolean newValue) {
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
