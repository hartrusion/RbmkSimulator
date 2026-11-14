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
