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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import com.hartrusion.modeling.solvers.SuperPosition;
import com.hartrusion.mvc.AwtUpdater;
import com.hartrusion.mvc.Controller;
import com.hartrusion.rbmksim.gui.elements.ChornobylMetalTheme;
import com.hartrusion.util.SimpleLogOut;

/**
 *
 * @author Viktor Alexander Hartung
 */
public class RbmkSimulator {

    MainLoop model;

    private final ScheduledExecutorService scheduler;
    private final ExecutorService threadPool;

    RbmkSimulator() {
        scheduler = Executors.newSingleThreadScheduledExecutor(
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

        // Initialize Multithreading for SuperPosition solver
        int cores = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(cores);
        SuperPosition.setThreadPool(threadPool);

        model = new MainLoop();
    }

    private void run() {
        /* Set the Chornobyl look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting ">
        try {
            for (UIManager.LookAndFeelInfo info
                    : UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    MetalLookAndFeel.setCurrentTheme(new ChornobylMetalTheme());
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(RbmkSimulator.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        // Create control panel instance
        ControlPanel view = new ControlPanel();

        // Create a new controller instance with an AWT updater
        Controller contr = new Controller();
        AwtUpdater updt = new AwtUpdater();

        // Connect model, view (gui) and controller
        contr.registerModel(model);
        contr.registerUpdater(updt);
        updt.registerView(view);
        view.registerController(contr);
        model.registerController(contr);

        // Build and initialize the model
        model.init();

        // Make the created view terminate everything on close
        view.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                scheduler.shutdown();
                evt.getWindow().dispose(); // Get rid of window
                System.exit(0); // Terminate java vm
            }
        });

        // Start the GUI
        java.awt.EventQueue.invokeLater(() -> {
            view.setVisible(true);
        });

        // Call the model run-method once here in main thread as this generates
        // a lot of objects on the first run. Further calls will be faster
//        model.run();
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException ex) {
//            System.getLogger(RbmkSimulator.class.getName()).log(
//                    System.Logger.Level.ERROR, (String) null, ex);
//        }
        // Start the 100 ms cyclic thread
        scheduler.scheduleAtFixedRate(model, 200, 200, TimeUnit.MILLISECONDS);
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
