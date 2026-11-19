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

import com.hartrusion.modeling.solvers.DomainAnalogySolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.hartrusion.mvc.AwtUpdater;
import com.hartrusion.mvc.Controller;
import com.hartrusion.mvc.net.ClassBlueprints;
import com.hartrusion.mvc.net.SocketServer;
import com.hartrusion.rbmksim.gui.GuiThemeManager;
import com.hartrusion.util.SimpleLogOut;

/**
 * Application class of the simulator in standalone and server mode, which
 * connects the model (MainLoop), the controller and the GUI (ControlPanelManager)
 * and runs the model cyclically each 100 ms.
 * <p>
 * The application also owns the network server, which is started on request
 * from a control panel and then keeps the application running even if all
 * control panels are closed, so that network clients can continue. Because the
 * server and its client connections are registered at the controller and not
 * at the model, the MainLoop can be replaced at runtime by a new instance with
 * restartMainLoop(), which is used to recover from a crashed simulation without
 * disconnecting the clients. There is always only one MainLoop at a time.
 *
 * @author Viktor Alexander Hartung
 */
public class RbmkSimulator {

    private static final Logger LOGGER = Logger.getLogger(
            RbmkSimulator.class.getName());

    /**
     * TCP port of the network server that RbmkSimulatorClient connects to.
     */
    static final int SERVER_PORT = 26486;

    /**
     * Time in seconds that a restart waits for the cycle of the old MainLoop
     * to finish before its thread is interrupted and abandoned.
     */
    private static final long MAIN_LOOP_STOP_TIMEOUT_S = 3;

    /**
     * The current model instance. Volatile because it is replaced by a restart
     * thread while the status view reads it.
     */
    private volatile MainLoop mainLoop;

    /**
     * Executor that calls the current MainLoop each 100 ms. Each MainLoop gets
     * its own executor, so that a restart is still possible when the cycle
     * thread of the old MainLoop hangs and can not be stopped.
     */
    private ScheduledExecutorService scheduler;

    private final ExecutorService threadPool;

    /**
     * The controller stays the same for the whole runtime, so that views and
     * network connections survive a restart of the MainLoop.
     */
    private final Controller controller = new Controller();

    private final ControlPanelManager view = new ControlPanelManager();

    /**
     * The network server, which is null until it was started.
     */
    private volatile SocketServer socketServer;

    /**
     * Message of the exception that terminated the network server (for
     * example a port that is already in use), null as long as the server did
     * not fail.
     */
    private volatile String serverFailure;

    /**
     * Status window of the network server, created when the server is started
     * and only accessed on the event dispatch thread.
     */
    private ServerView serverView;

    /**
     * Number of restarts of the MainLoop since the application was started.
     */
    private volatile int restartCount;

    /**
     * True while restartMainLoop() replaces the MainLoop.
     */
    private volatile boolean restarting;

    RbmkSimulator() {
        // Initialize Multithreading for SuperPosition solver
        int cores = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(cores);
        DomainAnalogySolver.setThreadPool(threadPool);
    }

    private void run() {
        // Install the initial GUI theme before any window is created.
        GuiThemeManager.DEFAULT_THEME.apply();

        // Create an AWT updater for the control panel instances
        AwtUpdater updt = new AwtUpdater();

        // Connect view (gui) and controller, the model is connected by
        // startMainLoop.
        controller.registerUpdater(updt);
        updt.registerView(view);
        view.registerController(controller);
        view.registerApplication(this);

        startMainLoop();

        // Start the GUI
        java.awt.EventQueue.invokeLater(() -> {
            view.displayNewControlPanel();
        });
    }

    /**
     * Creates a new MainLoop, connects it with the controller, initializes it
     * and starts the 100 ms cyclic thread for it. Called on startup and by
     * restartMainLoop().
     * <p>
     * LLM code generated with Claude Opus 5.5 (moved from run())
     */
    private void startMainLoop() {
        MainLoop newMainLoop = new MainLoop();

        // Connect model and controller
        controller.registerModel(newMainLoop);
        newMainLoop.registerController(controller);

        // Build and initialize the model
        newMainLoop.init();

        // Dirty workaround to not have an ConcurrentModificationException - 
        // this has to be reworked at some point. propably the whole alarm thing 
        // is not done very good.
        view.setAlarmList(new ArrayList<>(newMainLoop.alarms.getAlarmList()));

        mainLoop = newMainLoop;

        // Start the 100 ms cyclic thread
        scheduler = createScheduler();
        scheduler.scheduleAtFixedRate(newMainLoop, 100, 100,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Creates the single thread executor for the cyclic MainLoop calls, whose
     * thread gets a high priority to keep the 100 ms cycle.
     *
     * @return new executor without any scheduled task
     */
    private ScheduledExecutorService createScheduler() {
        return Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory() {
            private final ThreadFactory defaultThreadFactory
                    = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = defaultThreadFactory.newThread(r);
                t.setPriority(Thread.MAX_PRIORITY - 1);
                return t;
            }
        });
    }

    /**
     * Replaces the current MainLoop by a new instance in its initial state,
     * which is meant to recover from a crashed simulation. The cycle of the old
     * MainLoop is stopped first and its executor is abandoned if the cycle
     * does not finish in time, then the old MainLoop is unregistered from the
     * controller and a new one is created. The controller, the views and the
     * network connections stay as they are, so connected clients continue with
     * the new simulation. This blocks until the restart is finished and must
     * therefore not be called on the event dispatch thread. Called from the
     * restart button of ServerView.
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    synchronized void restartMainLoop() {
        restarting = true;
        try {
            LOGGER.log(Level.INFO, "Restarting the simulation.");
            stopMainLoop();
            controller.unregisterModel();
            mainLoop = null;
            startMainLoop();
            restartCount++;
            LOGGER.log(Level.INFO, "Simulation restarted.");
        } finally {
            restarting = false;
        }
    }

    /**
     * Stops the cyclic calls of the current MainLoop. A cycle that is running
     * gets some time to finish, afterwards its thread is interrupted. If the
     * thread still does not terminate, it is abandoned, because the new
     * MainLoop gets its own executor.
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    private void stopMainLoop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(MAIN_LOOP_STOP_TIMEOUT_S,
                    TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                LOGGER.log(Level.WARNING, "The cycle of the old MainLoop did "
                        + "not finish in time, its thread is abandoned.");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Starts the network server on SERVER_PORT in a thread of its own and opens
     * the ServerView, which displays the state of the server. If the server was
     * already started, only the ServerView is brought to the front. Called
     * from the menu of ControlPanel on the event dispatch thread.
     * <p>
     * LLM code generated with Claude Opus 5.5 (moved from MainLoop)
     */
    void startServer() {
        if (socketServer == null) {
            ClassBlueprints registry = CommBlueprints.createCommBlueprints();
            SocketServer newServer = new SocketServer(controller, registry);
            socketServer = newServer;
            Thread serverThread = new Thread(() -> {
                try {
                    newServer.startServer(SERVER_PORT);
                } catch (IOException e) {
                    serverFailure = e.getMessage();
                    LOGGER.log(Level.SEVERE, "Network server terminated.", e);
                }
            }, "SocketServer");
            serverThread.start();
        }
        if (serverView == null) {
            serverView = new ServerView(this);
            // The ServerView displays values of the model, which it receives
            // as a view of its own on the event dispatch thread.
            AwtUpdater serverViewUpdater = new AwtUpdater();
            serverViewUpdater.registerView(serverView);
            controller.registerUpdater(serverViewUpdater);
        }
        serverView.setVisible(true);
        serverView.toFront();
    }

    /**
     * Returns whether the network server was started. The application is not
     * terminated when the last control panel is closed in this case.
     *
     * @return true after startServer() was called
     */
    boolean isServerStarted() {
        return socketServer != null;
    }

    SocketServer getSocketServer() {
        return socketServer;
    }

    String getServerFailure() {
        return serverFailure;
    }

    int getServerPort() {
        return SERVER_PORT;
    }

    /**
     * @return the current MainLoop, null during a restart
     */
    MainLoop getMainLoop() {
        return mainLoop;
    }

    int getRestartCount() {
        return restartCount;
    }

    boolean isRestarting() {
        return restarting;
    }

    ControlPanelManager getControlPanelManager() {
        return view;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SimpleLogOut.configureLoggingToStdOut();

        RbmkSimulator app = new RbmkSimulator();
        app.run();
    }

}
