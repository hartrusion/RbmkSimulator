# LLM History

This file documents the work of the LLM on the RbmkSimulator project, including the knowledge gained and the decisions made. It was started on 2026-11-14, so earlier LLM work on the project is not described here.

## 1 Session 2026-11-14: Scalable AbsoluteLayout for NetBeans Forms

The session moved the zooming layout of the mnemonic displays from RbmkSimulator to utils and made it usable from the NetBeans GUI editor without hand-written code. The work was done with Claude Opus 5.5.

### 1.1 Background

The NetBeans GUI editor can only edit the layouts it supports, and a layout manager of its own (even a subclass of AbsoluteLayout) is treated by NetBeans either without constraints or as the plain AbsoluteLayout, which loses the subclass in the generated code. Until now, InternalFrameMnemonic of RbmkSimulator replaced the AbsoluteLayout of each mnemonic panel after initComponents() with ScaleableAbsoluteLayout.install(panel). The user asked for a solution that is set up in the editor instead.

### 1.2 Decision

A container class is used instead of a layout class: com.hartrusion.util.JPanelScaleable extends JPanel and replaces an AbsoluteLayout that is set while the panel is still empty by com.hartrusion.util.ScaleableAbsoluteLayout, but only at runtime, because the NetBeans form editor sets java.beans.Beans.setDesignTime(true). A form therefore keeps the plain AbsoluteLayout in the editor and in the generated code, and only its superclass is changed to JPanelScaleable (or the panel is dropped from the palette). ScaleableAbsoluteLayout lost its install() method and determines its base size itself: from a preferredSize set on the container, otherwise from the size the constraints require. It is reset whenever components are added or removed. utils got the AbsoluteLayout library (${libs.absolutelayout.classpath}) in javac.classpath, which was added with the add-absolutelayout command of the netbeans-matisse-gui skill.

### 1.3 Changes in RbmkSimulator

The eight mnemonic panels that are shown in InternalFrameMnemonic (Blowdown, Loop, Feedwater, Deaerator, AuxiliaryCondenser, Turbine, Condensate, Preheaters) now extend com.hartrusion.util.JPanelScaleable, the install() call in InternalFrameMnemonic.initPanel and the old class com.hartrusion.rbmksim.gui.ScaleableAbsoluteLayout were removed. PanelMnemonicCore is not shown in such a frame and was left unchanged. Their .form files need no change, because the form type of a JPanel subclass stays JPanelFormInfo.

### 1.4 Verification

Ant is not installed on the development machine, so both projects were compiled with javac of the NetBeans JDK and the AbsoluteLayout jar of NetBeans; both compile without errors. A headless probe (p10 in the MatisseGui project) created PanelMnemonicFeedwater and showed that the runtime layout is ScaleableAbsoluteLayout, that the size at scale 1.0 is 400 x 208, that a doubled panel size gives scale 2.0 with doubled component bounds, and that with the design time flag set the plain AbsoluteLayout stays. The behaviour inside the running simulator and the display in the NetBeans GUI editor are still to be checked by the user; utils has to be rebuilt in NetBeans so that RbmkSimulator gets the new utils.jar.

## 2 Session 2026-11-14: Scaling of Components with Preferred-Size Constraints

The user moved JPanelScaleable and ScaleableAbsoluteLayout back from utils into RbmkSimulator (package com.hartrusion.util kept) because of the dependencies, and reported that the mnemonic displays then only scaled their JLabels, while valves, pumps and backgrounds kept their size. The cause was the removed install() method: it had resolved every AbsoluteConstraints width or height of -1 (preferred size) into the actual bounds before the layout was replaced, so these components were scaled like all others. The adapted layout kept the -1 values and used the current preferred size at every layout, which only grows for components whose preferred size follows the scaled font. ScaleableAbsoluteLayout.updateBaseSize() now replaces such constraints once by the preferred size of the component at scale 1.0, before the base size is determined. Probe p11 of the MatisseGui project checks all eight mnemonic panels headless: before the fix only part of the components doubled their size at a doubled panel size (for example 10 of 30 in Feedwater, Valve and Pump unchanged), after the fix all components of all panels do.

## 3 Session 2026-11-14: Switchable GUI Themes

The user asked for a dark mode and for a choice between several GUI themes in the View menu, where the existing ChornobylMetalTheme stays the primary GUI theme, FlatLaf Dark serves as dark mode and FlatLaf Light is offered in addition. The work was done with Claude Opus 5.5.

### 3.1 Decision

Swing only applies a look and feel to components that are created after it was installed, and the simulator contains many custom painted components, so updating the existing component trees with SwingUtilities.updateComponentTreeUI() would be unreliable. Following the proposal of the user, a theme switch therefore disposes all windows of the application and opens a new ControlPanel, which is possible because ControlPanelManager distributes the MVC updates to any number of control panels and the model keeps running. Because Window.dispose() does not fire windowClosing, the listener in ControlPanelManager that terminates the application on closing the last control panel is not triggered, so no change of that behaviour was necessary. The new control panel opens the reactor controls again just like on startup, whereas the previous window arrangement is lost.

### 3.2 Changes

The new enumeration com.hartrusion.rbmksim.gui.GuiTheme lists the GUI themes CHORNOBYL_METAL, FLATLAF_DARK and FLATLAF_LIGHT, installs them with apply() and remembers the active GUI theme. RbmkSimulator and RbmkSimulatorClient install GuiTheme.DEFAULT_THEME instead of their own Metal setup code. ControlPanelManager got switchTheme(GuiTheme), and ControlPanel got a submenu "Theme" at the end of the View menu with three JRadioButtonMenuItems in the ButtonGroup buttonGroupTheme, which was added to ControlPanel.form with the netbeans-matisse-gui skill and passes its validator. The constructor of ControlPanel marks the active GUI theme in this menu. FlatLaf was already on javac.classpath as file.reference.flatlaf-3.6.2.jar, which points to the jar inside the NetBeans installation (/usr/lib/apache-netbeans/platform/modules/ext). FrameRodPositions still contains an unused import of ChornobylMetalTheme and was left unchanged.

### 3.3 Additional FlatLaf Themes

On request of the user, the GUI themes INTELLIJ (FlatIntelliJLaf), DARCULA (FlatDarculaLaf), MAC_LIGHT (FlatMacLightLaf) and MAC_DARK (FlatMacDarkLaf) were added to GuiTheme and to the theme menu, because they are part of the FlatLaf core jar 3.6.2 that the project already uses, so that no further library is needed. The IntelliJ IDEA platform itself does not use a reusable Swing look and feel, which is why these FlatLaf themes only resemble its appearance. Further IntelliJ community themes would require the separate add-on library flatlaf-intellij-themes.

### 3.4 Verification

The project was compiled without errors with javac of the NetBeans JDK and the jars of utils, jmplot, PhxNetMod, AbsoluteLayout and FlatLaf. The application was not started, so the switch itself and the appearance of the custom painted components (mnemonics, panels, diagrams) under FlatLaf have to be checked by the user.
