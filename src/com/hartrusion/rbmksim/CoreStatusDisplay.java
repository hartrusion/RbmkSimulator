/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hartrusion.rbmksim;

import com.hartrusion.mvc.net.ClassBlueprints;
import java.io.IOException;

/**
 * Flat network transfer object for the core activity display.
 * Stores a fixed 23x23 boolean highlight matrix.
 *
 * @author Viktor Alexander Hartung
 */
public class CoreStatusDisplay {

    public static final int SIZE = 23;

    private final boolean[][] highlighted = new boolean[SIZE][SIZE];

    public CoreStatusDisplay() {
    }

    public void setHighlighted(int x, int y, boolean value) {
        highlighted[x][y] = value;
    }

    public boolean isHighlighted(int x, int y) {
        return highlighted[x][y];
    }

    public static void registerToRegistry(ClassBlueprints registry) {
        registry.registerType(CoreStatusDisplay.class,
                (dos, display) -> {
                    for (int x = 0; x < SIZE; x++) {
                        for (int y = 0; y < SIZE; y++) {
                            dos.writeBoolean(display.highlighted[x][y]);
                        }
                    }
                },
                (dis) -> {
                    CoreStatusDisplay display = new CoreStatusDisplay();
                    for (int x = 0; x < SIZE; x++) {
                        for (int y = 0; y < SIZE; y++) {
                            display.setHighlighted(x, y, dis.readBoolean());
                        }
                    }
                    return display;
                });
    }
}