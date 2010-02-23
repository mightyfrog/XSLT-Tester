package org.mightyfrog.util.xslttester;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;

/**
 *
 * @author Shigehiro Soejima
 */
class InternalFrame extends JInternalFrame {
    {
        UIManager.put("InternalFrame.titleFont",
                      UIManager.getFont("MenuBar.font"));
        updateUI();
    }

    /**
     *
     * @param title frame title
     * @param comp
     */
    InternalFrame(String title, JComponent comp) {
        super(title, true, false, true, true);

        add(comp);
    }
}

