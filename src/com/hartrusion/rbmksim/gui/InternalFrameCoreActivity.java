/*
 * Copyright (C) 2026 Viktor Alexander Hartung
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
package com.hartrusion.rbmksim.gui;

import com.hartrusion.mvc.UpdateReceiver;
import java.beans.PropertyChangeEvent;
import javax.swing.DefaultComboBoxModel;

/**
 * Holds the core activity display. The drop down in the upper left corner sits
 * in the same GridBagLayout cell as the core display and therefore floats above
 * it, where the display has no channels.
 *
 * @author Viktor Alexander Hartung
 */
public class InternalFrameCoreActivity extends javax.swing.JInternalFrame
        implements UpdateReceiver {

    /**
     * One selectable entry of the mode drop down. It couples the text shown in
     * the drop down with the arguments handed over to
     * {@link PanelCoreActivity#initMode(String, double)}.
     */
    private static final class Mode {

        private final String text;
        private final String suffix;
        private final double threshold;

        Mode(String text, String suffix, double threshold) {
            this.text = text;
            this.suffix = suffix;
            this.threshold = threshold;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    /**
     * The presets offered by the drop down. The first entry is the one the
     * display starts with, it has to match the defaults of
     * {@link PanelCoreActivity}.
     * <p>
     * Dropdown-Menue vibecoded with Opus 5
     */
    private static final Mode[] MODES = {
        new Mode("a > 0.1", "Affection", 0.1),
        new Mode("a > 0.15", "Affection", 0.15),
        new Mode("a > 0.2", "Affection", 0.2),
        new Mode("a > 0.3", "Affection", 0.3),
        new Mode("a > 0.5", "Affection", 0.5),
        new Mode("P_th > 0.15 MW", "FissionPower", 0.15),
        new Mode("P_th > 0.5 MW", "FissionPower", 0.5),
        new Mode("P_th > 1.0 MW", "FissionPower", 1.0),
        new Mode("P_th > 1.5 MW", "FissionPower", 1.5),
        new Mode("P_th > 2.0 MW", "FissionPower", 2.0),
        new Mode("P_th > 3.0 MW", "FissionPower", 3.0),
        new Mode("P_th > 4.0 MW", "FissionPower", 4.0),
        new Mode("P_th > 5.0 MW", "FissionPower", 5.0),
        new Mode("P_th > 6.0 MW", "FissionPower", 6.0),
        new Mode("P_th > 7.0 MW", "FissionPower", 7.0),
        new Mode("P_th > 8.0 MW", "FissionPower", 8.0),
        new Mode("P_th > 9.0 MW", "FissionPower", 9.0),
        new Mode("P_th > 10.0 MW", "FissionPower", 10.0),
        new Mode("T_fuel > 250 °C", "Temperature", 250.0),
        new Mode("T_fuel > 275 °C", "Temperature", 275.0),
        new Mode("T_fuel > 300 °C", "Temperature", 300.0),
        new Mode("T_fuel > 325 °C", "Temperature", 325.0),
        new Mode("T_fuel > 350 °C", "Temperature", 350.0),
        new Mode("T_fuel > 375 °C", "Temperature", 375.0),
        new Mode("T_fuel > 400 °C", "Temperature", 400.0),
        new Mode("T_fuel > 425 °C", "Temperature", 425.0),
        new Mode("T_fuel > 450 °C", "Temperature", 450.0),
        new Mode("T_fuel > 475 °C", "Temperature", 475.0),
        new Mode("T_fuel > 500 °C", "Temperature", 500.0),
        new Mode("T_fuel > 550 °C", "Temperature", 550.0),
        new Mode("T_fuel > 600 °C", "Temperature", 600.0),
        new Mode("T_fuel > 650 °C", "Temperature", 650.0),
//        new Mode("Voiding > 0.05", "Voiding", 0.05),
//        new Mode("Voiding > 0.1", "Voiding", 0.1),
//        new Mode("Voiding > 0.3", "Voiding", 0.3),
//        new Mode("Voiding > 0.5", "Voiding", 0.5)
    };

    /**
     * Creates new form InternalFrameCoreActivity
     */
    public InternalFrameCoreActivity() {
        initComponents();
        initModes();
    }

    /**
     * Replaces the place holder entry the form editor puts into the drop down
     * with the real presets. The form keeps a plain text entry so the drop down
     * can be seen and sized in the GUI builder, the running program works with
     * the typed {@link Mode} objects instead.
     */
    @SuppressWarnings("unchecked")
    private void initModes() {
        // Raw on purpose: the form editor declares the combo box without type
        // parameters, this keeps working no matter what it generates.
        jComboBoxMode.setModel(new DefaultComboBoxModel(MODES));
    }

    /**
     * Sets which parameter the core display evaluates. If a preset of the drop
     * down matches, the drop down follows the selection.
     *
     * @param suffix for example with Fuel2134#Temperature it would be
     * "Temperature"
     * @param threshold the value on which a channel will light up
     */
    public void initMode(String suffix, double threshold) {
        panelCoreActivity1.initMode(suffix, threshold);

        Mode match = null;
        for (Mode mode : MODES) {
            if (mode.suffix.equals(suffix) && mode.threshold == threshold) {
                match = mode;
                break;
            }
        }
        // Selecting fires the listener again, which is harmless because it
        // applies the very same mode. A mode without preset clears the
        // selection instead.
        jComboBoxMode.setSelectedItem(match);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jComboBoxMode = new javax.swing.JComboBox();
        panelCoreActivity1 = new com.hartrusion.rbmksim.gui.PanelCoreActivity();

        setClosable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("Core Activity");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jComboBoxMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "a > 0.1" }));
        jComboBoxMode.setToolTipText("Value shown by the core display");
        jComboBoxMode.setPreferredSize(new java.awt.Dimension(130, 24));
        jComboBoxMode.addActionListener(this::jComboBoxModeActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jComboBoxMode, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(panelCoreActivity1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxModeActionPerformed
        Object selected = jComboBoxMode.getSelectedItem();
        if (selected instanceof Mode) {
            Mode mode = (Mode) selected;
            panelCoreActivity1.initMode(mode.suffix, mode.threshold);
        }
    }//GEN-LAST:event_jComboBoxModeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxMode;
    private com.hartrusion.rbmksim.gui.PanelCoreActivity panelCoreActivity1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updateComponent(PropertyChangeEvent evt) {
        panelCoreActivity1.updateComponent(evt);
    }

    @Override
    public void updateComponent(String propertyName, Object newValue) {
        panelCoreActivity1.updateComponent(propertyName, newValue);
    }

    @Override
    public void updateComponent(String propertyName, double newValue) {
        panelCoreActivity1.updateComponent(propertyName, newValue);
    }

    @Override
    public void updateComponent(String propertyName, boolean newValue) {
        panelCoreActivity1.updateComponent(propertyName, newValue);
    }
}
