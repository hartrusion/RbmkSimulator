/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hartrusion.rbmksim;
import com.hartrusion.alarm.AlarmListSnapshot;
import com.hartrusion.alarm.AlarmState;
import com.hartrusion.control.ControlCommand;
import com.hartrusion.control.ValveState;
import com.hartrusion.modeling.automated.PumpState;
import com.hartrusion.mvc.net.ClassBlueprints;
import com.hartrusion.values.ValueSnapshot;

/**
 * Central registration of all allowed network types for the RBMK simulator.
 *
 * Server and client must execute the same registrations in the same order so
 * the assigned byte IDs remain identical.
 *
 * @author Viktor Alexander Hartung
 */
public final class CommBlueprints {

    private CommBlueprints() {
    }

    public static ClassBlueprints createCommBlueprints() {
        ClassBlueprints registry = new ClassBlueprints();

        registerBasicTypes(registry);
        registerEnums(registry);
        registerProjectTypes(registry);

        return registry;
    }

    private static void registerBasicTypes(ClassBlueprints registry) {
        registry.registerType(String.class,
                (dos, value) -> dos.writeUTF(value),
                (dis) -> dis.readUTF()
        );

        registry.registerType(Double.class,
                (dos, value) -> dos.writeDouble(value),
                (dis) -> dis.readDouble()
        );

        registry.registerType(Boolean.class,
                (dos, value) -> dos.writeBoolean(value),
                (dis) -> dis.readBoolean()
        );

        registry.registerType(Integer.class,
                (dos, value) -> dos.writeInt(value),
                (dis) -> dis.readInt()
        );
    }

    private static void registerEnums(ClassBlueprints registry) {
        registry.registerEnum(ControlCommand.class);
        registry.registerEnum(ValveState.class);
        registry.registerEnum(PumpState.class);
        registry.registerEnum(SpeedSelect.class);
        registry.registerEnum(AlarmState.class);

        // Weitere Enums hier ergänzen, sobald sie beim Test benötigt werden.
    }

    private static void registerProjectTypes(ClassBlueprints registry) {
        ValueSnapshot.registerToRegistry(registry);
        AlarmListSnapshot.registerToRegistry(registry);
        CoreStatusDisplay.registerToRegistry(registry);
    }
}