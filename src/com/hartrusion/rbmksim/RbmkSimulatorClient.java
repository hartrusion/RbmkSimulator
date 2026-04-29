/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hartrusion.rbmksim;

import com.hartrusion.mvc.AwtUpdater;
import com.hartrusion.mvc.net.ClassBlueprints;
import com.hartrusion.mvc.net.NetViewAdapter;
import com.hartrusion.rbmksim.gui.ExceptionPopup;
import com.hartrusion.util.SimpleLogOut;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.hartrusion.rbmksim.gui.elements.ChornobylMetalTheme;

/**
 * Standalone network client for the RBMK simulator.
 * Connects to a remote simulator server and displays the control room GUI.
 *
 * @author Viktor Alexander Hartung
 */
public class RbmkSimulatorClient {

    private static final Logger LOGGER
            = Logger.getLogger(RbmkSimulatorClient.class.getName());

    private static final String DEFAULT_ADDRESS = "127.0.0.1:26486";

    private static final class HostPort {
        final String host;
        final int port;

        HostPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    private void run() throws Exception {
        setLookAndFeel();

        HostPort target = promptForServerAddress();
        if (target == null) {
            return; // user cancelled
        }

        // Create GUI manager
        ControlPanelManager view = new ControlPanelManager();

        // Create network controller and AWT updater
        ClassBlueprints registry = CommBlueprints.createCommBlueprints();
        NetViewAdapter controller = new NetViewAdapter(registry);
        AwtUpdater updater = new AwtUpdater();

        // Connect view/controller/updater
        controller.registerUpdater(updater);
        updater.registerView(view);
        view.registerController(controller);

        // Connect to remote server
        controller.connect(target.host, target.port);

        LOGGER.log(Level.INFO, "Connected to {0}:{1}",
                new Object[]{target.host, target.port});
        
        // mark as client to make it clear that this is a client.
        view.setAsClient();

        // Start GUI
        java.awt.EventQueue.invokeLater(() -> {
            view.displayNewControlPanel();
        });
    }

    private void setLookAndFeel() {
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
            throw new RuntimeException("Failed to initialize look and feel.", ex);
        }
    }

    private HostPort promptForServerAddress() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    null,
                    "Enter server address (host:port)",
                    DEFAULT_ADDRESS);

            if (input == null) {
                return null;
            }

            return parseHostPort(input);
        }
    }

    private HostPort parseHostPort(String input) {
        input = input.trim();
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Address must not be empty.");
        }

        int idx = input.lastIndexOf(':');
        if (idx <= 0 || idx >= input.length() - 1) {
            throw new IllegalArgumentException(
                    "Address must be in the form host:port");
        }

        String host = input.substring(0, idx).trim();
        String portString = input.substring(idx + 1).trim();

        if (host.isEmpty()) {
            throw new IllegalArgumentException("Host must not be empty.");
        }

        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port must be a number.", e);
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Port must be between 1 and 65535.");
        }

        return new HostPort(host, port);
    }

    public static void main(String[] args) {
        SimpleLogOut.configureLoggingToStdOut();

        try {
            new RbmkSimulatorClient().run();
        } catch (Exception e) {
            ExceptionPopup.show(e);
        }
    }
}