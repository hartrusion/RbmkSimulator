/*
 * The MIT License
 *
 * Copyright 2026 Viktor Alexander Hartung.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.hartrusion.util;

import java.awt.LayoutManager;
import java.beans.Beans;
import javax.swing.JPanel;
import org.netbeans.lib.awtextra.AbsoluteLayout;

/**
 * A JPanel whose AbsoluteLayout zooms the content to the size of the panel.
 * The NetBeans GUI editor only supports the plain AbsoluteLayout, so a form
 * that extends this class (or a panel of this class dropped from the palette)
 * is designed with the normal AbsoluteLayout and its generated code calls
 * setLayout(new AbsoluteLayout()) as usual. This class replaces that layout
 * at runtime by a {@link ScaleableAbsoluteLayout}, which places the components
 * at the same positions at a scale factor of 1.0 and zooms them when the panel
 * gets larger or smaller.
 *
 * <p>
 * The replacement does not happen at design time, where the GUI editor of
 * NetBeans sets the design time flag of java.beans.Beans, so that the editor
 * keeps showing and editing the plain AbsoluteLayout. It only happens while
 * the panel has no child components yet, which is the case in
 * initComponents(), because the constraints of components already added to
 * the old layout would be lost. Other layouts are set unchanged.
 *
 * <p>
 * LLM code generated with Claude Opus 5.5.
 *
 * @author Viktor Alexander Hartung
 */
public class JPanelScaleable extends JPanel {

    /**
     * Creates a panel with the default FlowLayout, which the form code
     * replaces by setLayout(new AbsoluteLayout()).
     */
    public JPanelScaleable() {
        super();
    }

    @Override
    public void setLayout(LayoutManager layout) {
        if (layout instanceof AbsoluteLayout
                && !(layout instanceof ScaleableAbsoluteLayout)
                && !Beans.isDesignTime()
                && getComponentCount() == 0) {
            super.setLayout(new ScaleableAbsoluteLayout());
        } else {
            super.setLayout(layout);
        }
    }
}
