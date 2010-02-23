package org.mightyfrog.util.xslttester;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 *
 * @author Shigehiro Soejima
 */
class TextArea extends JTextArea implements MouseListener,
                                            PopupMenuListener,
                                            UndoableEditListener {
    //
    private final JPopupMenu POPUP = new JPopupMenu();
    private final JMenuItem COPY_MI =
        new JMenuItem(getActionMap().get("copy-to-clipboard"));
    private final JMenuItem CUT_MI =
        new JMenuItem(getActionMap().get("cut-to-clipboard"));
    private final JMenuItem PASTE_MI =
        new JMenuItem(getActionMap().get("paste-from-clipboard"));

    //
    private final UndoManager UNDO_MANAGER = new UndoManager();

    /**
     *
     */
    public TextArea() {
        addMouseListener(this);
        getDocument().addUndoableEditListener(this);

        COPY_MI.setText(I18N.get("popup.copy"));
        CUT_MI.setText(I18N.get("popup.cut"));
        PASTE_MI.setText(I18N.get("popup.paste"));

        POPUP.add(COPY_MI);
        POPUP.add(CUT_MI);
        POPUP.add(PASTE_MI);

        POPUP.addPopupMenuListener(this);

        getActionMap().put("undo", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        UNDO_MANAGER.undo();
                    } catch (CannotUndoException e) {
                        //
                    }
                }
            });
        getActionMap().put("redo", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        UNDO_MANAGER.redo();
                    } catch (CannotRedoException e) {
                        //
                    }
                }
            });
        InputMap im = getInputMap();
        im.put(KeyStroke.getKeyStroke("ctrl pressed Z"), "undo");
        im.put(KeyStroke.getKeyStroke("shift ctrl pressed Z"), "redo");
    }

    /** */
    @Override
    public void setText(String text) {
        super.setText(text);
        setCaretPosition(0);
        UNDO_MANAGER.discardAllEdits();
    }

    /** */
    @Override
    public void mouseEntered(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mousePressed(MouseEvent evt) {
        handlePopup(evt);
    }

    /** */
    @Override
    public void mouseClicked(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mouseReleased(MouseEvent evt) {
        handlePopup(evt);
    }

    /** */
    @Override
    public void mouseExited(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void popupMenuCanceled(PopupMenuEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
        CUT_MI.setEnabled(getCaret().getMark() != getCaret().getDot());
        COPY_MI.setEnabled(CUT_MI.isEnabled());
    }

    /** */
    @Override
    public void undoableEditHappened(UndoableEditEvent evt) {
        UNDO_MANAGER.addEdit(evt.getEdit());
    }

    /** */
    @Override
    public void append(String text) {
        if (getText() == null || getText().isEmpty()) {
            text = text.trim();
            text += System.getProperty("line.separator");
        }
        super.append(text);
    }

    //
    //
    //

    /**
     *
     */
    void removeUndoableEditListener() {
        getDocument().removeUndoableEditListener(this);
    }

    //
    //
    //

    /**
     *
     * @param evt
     */
    private void handlePopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            POPUP.show(this, evt.getX(), evt.getY());
        }
    }
}
