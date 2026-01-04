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

import com.hartrusion.values.ValueHandler;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Provides common properties of an element that is part of the reactor core.
 *
 * @author Viktor Alexander Hartung
 */
public abstract class ReactorElement {

    /**
     * Receives property changes from this reactor element.
     */
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Instance that gets values written from this element.
     */
    protected ValueHandler outputValues;

    private int x;
    private int y;

    /**
     * Merged number that identifies the element, for element 21-34 it will be
     * 2134.
     */
    protected int identifier;

    ReactorElement(int x, int y) {
        this.x = x;
        this.y = y;
        identifier = 100 * x + y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "element_" + x + "-" + y;
    }

    /**
     * Makes the signal listener instance known to this class.
     *
     * @param signalListener Instance that will receive the event changes from
     * valves and pumps.
     */
    public void registerSignalListener(PropertyChangeListener signalListener) {
        pcs.addPropertyChangeListener(signalListener);
    }

    /**
     * Sets a ParameterHandler that will get the valve position on each run
     * call.
     *
     * @param h reference to ParameterHandler
     */
    public void registerValueHandler(ValueHandler h) {
        outputValues = h;
    }
}
