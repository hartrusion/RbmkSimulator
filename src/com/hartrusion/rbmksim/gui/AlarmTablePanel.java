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

import com.hartrusion.alarm.AlarmObject;
import com.hartrusion.alarm.AlarmState;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Displays the alarm list of the simulator as a table with the columns
 * description and alarm state, which is the content of InternalFrameAlarmTable
 * that adds the buttons for acknowledging and muting below it. The frame
 * passes the alarm objects to setAlarms() on each GUI update, and this panel
 * decides which of them are shown, in which order and how they are rendered.
 * <p>
 * An alarm that has returned to the state NONE and is acknowledged is no
 * longer of interest and is therefore not shown. Active alarms are listed
 * above returned alarms, so that a returned but not yet acknowledged alarm
 * never hides an active one. Within both groups the unacknowledged alarms come
 * first and are then ordered by the severity of their alarm state, from MAX2
 * down to LOW1, so that the most urgent alarm is always at the top.
 * <p>
 * The table only uses the background and foreground colors that the active
 * GUI theme assigns to tables, so that it fits both the light and the dark
 * theme without own color definitions. An unacknowledged alarm is indicated
 * by its text, which blinks with a period of one second by being hidden in
 * every second phase of the blink timer. Active alarms are written in bold,
 * whereas returned alarms are written in the regular font, so that both can
 * still be distinguished while they blink. The blink timer only runs while
 * the panel is part of a displayed window, because ControlPanelManager
 * closes and recreates all windows when the GUI theme is switched and a
 * running Swing timer would otherwise keep the discarded panel alive.
 * <p>
 * LLM code generated with Claude Opus 5.5, replacing a previous version that
 * was generated with GPT 5.2.
 *
 * @author Viktor Alexander Hartung
 */
public class AlarmTablePanel extends JPanel {

    /**
     * Duration of one blink phase in milliseconds, during which the text of
     * unacknowledged alarms is either visible or hidden.
     */
    private static final int BLINK_PHASE_MILLIS = 500;

    /**
     * Fixed width in pixels of the column that holds the alarm state.
     */
    private static final int STATE_COLUMN_WIDTH = 50;

    /**
     * Index of the column that holds the description of the alarm.
     */
    private static final int COLUMN_DESCRIPTION = 0;

    /**
     * Index of the column that holds the alarm state.
     */
    private static final int COLUMN_STATE = 1;

    /**
     * Sort rank of each alarm state, where a lower rank is placed further up
     * in the table. States which are not listed, like ACTIVE and NONE, are
     * placed below the listed ones.
     */
    private static final Map<AlarmState, Integer> STATE_SORT_RANK
            = createStateSortRank();

    /**
     * Orders the shown alarms as described in the class documentation, where
     * description and component are only used to keep the order of equally
     * ranked alarms stable between updates.
     */
    private static final Comparator<AlarmObject> ALARM_ORDER
            = Comparator.comparing(AlarmTablePanel::isReturned)
                    .thenComparing(AlarmObject::isAcknowledged)
                    .thenComparingInt(AlarmTablePanel::getStateSortRank)
                    .thenComparing(alarm -> getTextOrEmpty(alarm.getDescription()))
                    .thenComparing(alarm -> getTextOrEmpty(alarm.getComponent()));

    private final AlarmTableModel alarmTableModel = new AlarmTableModel();

    private final JTable alarmTable = new JTable(alarmTableModel);

    /**
     * True during the blink phase in which the text of unacknowledged alarms
     * is visible. It is toggled by blinkTimer and read by the renderer, which
     * both run on the event dispatch thread.
     */
    private boolean blinkTextVisible = true;

    private final Timer blinkTimer;

    public AlarmTablePanel() {
        super(new BorderLayout());

        alarmTable.setFont(alarmTable.getFont().deriveFont(Font.PLAIN, 13f));
        alarmTable.setRowHeight(Math.max(18, alarmTable.getRowHeight()));
        alarmTable.setFillsViewportHeight(true);
        alarmTable.getTableHeader().setReorderingAllowed(false);
        alarmTable.setRowSelectionAllowed(false);
        alarmTable.setColumnSelectionAllowed(false);
        alarmTable.setFocusable(false);
        alarmTable.setDefaultRenderer(Object.class, new AlarmCellRenderer());

        TableColumn stateColumn
                = alarmTable.getColumnModel().getColumn(COLUMN_STATE);
        stateColumn.setPreferredWidth(STATE_COLUMN_WIDTH);
        stateColumn.setMinWidth(STATE_COLUMN_WIDTH);
        stateColumn.setMaxWidth(STATE_COLUMN_WIDTH);

        add(new JScrollPane(alarmTable), BorderLayout.CENTER);

        blinkTimer = new Timer(BLINK_PHASE_MILLIS, e -> {
            blinkTextVisible = !blinkTextVisible;
            alarmTable.repaint();
        });
    }

    /**
     * Replaces the displayed alarms with the given list, which is filtered
     * and sorted as described in the class documentation. Called from
     * InternalFrameAlarmTable on each GUI update on the event dispatch thread.
     *
     * @param alarms All alarm objects of the simulator, may be null
     */
    public void setAlarms(List<AlarmObject> alarms) {
        alarmTableModel.setAlarms(alarms);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        blinkTimer.start();
    }

    @Override
    public void removeNotify() {
        blinkTimer.stop();
        super.removeNotify();
    }

    private static Map<AlarmState, Integer> createStateSortRank() {
        Map<AlarmState, Integer> rank = new EnumMap<>(AlarmState.class);
        rank.put(AlarmState.MAX2, 0);
        rank.put(AlarmState.MIN2, 1);
        rank.put(AlarmState.MAX1, 2);
        rank.put(AlarmState.MIN1, 3);
        rank.put(AlarmState.HIGH2, 4);
        rank.put(AlarmState.LOW2, 5);
        rank.put(AlarmState.HIGH1, 6);
        rank.put(AlarmState.LOW1, 7);
        return rank;
    }

    private static int getStateSortRank(AlarmObject alarm) {
        return STATE_SORT_RANK.getOrDefault(alarm.getState(),
                STATE_SORT_RANK.size());
    }

    /**
     * Checks whether the alarm has returned to normal, which is the state NONE
     * or a missing state.
     */
    private static boolean isReturned(AlarmObject alarm) {
        return alarm.getState() == null || alarm.getState() == AlarmState.NONE;
    }

    private static String getTextOrEmpty(String text) {
        if (text == null) {
            return "";
        }
        return text;
    }

    /**
     * Holds the alarms that are currently shown in their display order and
     * provides their description and state as the two table columns.
     */
    private static final class AlarmTableModel extends AbstractTableModel {

        private static final String[] COLUMN_NAMES = {"Description", "State"};

        /**
         * Alarms that are shown in the table, in the order of the rows.
         */
        private final List<AlarmObject> shownAlarms = new ArrayList<>();

        private void setAlarms(List<AlarmObject> alarms) {
            shownAlarms.clear();
            if (alarms != null) {
                for (AlarmObject alarm : alarms) {
                    if (alarm == null) {
                        continue;
                    }
                    if (isReturned(alarm) && alarm.isAcknowledged()) {
                        continue;
                    }
                    shownAlarms.add(alarm);
                }
            }
            shownAlarms.sort(ALARM_ORDER);
            fireTableDataChanged();
        }

        private AlarmObject getAlarmAt(int row) {
            return shownAlarms.get(row);
        }

        @Override
        public int getRowCount() {
            return shownAlarms.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AlarmObject alarm = shownAlarms.get(rowIndex);
            if (columnIndex == COLUMN_DESCRIPTION) {
                return getTextOrEmpty(alarm.getDescription());
            }
            if (alarm.getState() == null) {
                return "";
            }
            return alarm.getState().toString();
        }
    }

    /**
     * Renders the cells in the colors of the active GUI theme and applies the
     * font and the blinking that indicate the alarm condition of the row. The
     * renderer never sets own colors, because DefaultTableCellRenderer would
     * keep them and no longer take the colors of the table.
     */
    private final class AlarmCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            AlarmObject alarm = alarmTableModel.getAlarmAt(
                    table.convertRowIndexToModel(row));
            if (isReturned(alarm)) {
                setFont(table.getFont());
            } else {
                setFont(table.getFont().deriveFont(Font.BOLD));
            }
            if (!alarm.isAcknowledged() && !blinkTextVisible) {
                setText("");
            }
            return this;
        }
    }
}
