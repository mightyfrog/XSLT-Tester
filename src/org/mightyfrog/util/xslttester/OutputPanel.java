package org.mightyfrog.util.xslttester;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

/**
 *
 */
class OutputPanel extends JPanel {
    //
    private final JSplitPane SP = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private final TextArea TA = new TextArea();
    private final JTextPane TP = new JTextPane() {
            /** */
            @Override
            public void setText(String text) {
                super.setText(text);
                setCaretPosition(0);
            }
        };

    /**
     *
     */
    public OutputPanel() {
        setLayout(new BorderLayout());

        TP.setEditable(false);
        TP.setContentType("text/html");
        SP.setTopComponent(new JScrollPane(TA));
        SP.setBottomComponent(new JScrollPane(TP));
        SP.setDividerLocation(200);

        add(SP);
    }

    //
    //
    //

    /**
     *
     * @param contentType
     */
    void setContentType(String contentType) {
        TP.setContentType(contentType);
        TP.setText(TA.getText());
    }

    /**
     *
     */
    String getText() {
        return TA.getText();
    }

    /**
     *
     * @param text
     */
    void setText(String text) {
        TA.setText(text);
        TP.setText(text);
    }

    /**
     *
     */
    void clear() {
        TA.setText(null);
        TP.setText(null);

        // setting null doesn't clear up JTextPane, workaround
        String ct = TP.getContentType();
        TP.setContentType("html/plain");
        TP.setContentType(ct);
    }
}
