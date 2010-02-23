package org.mightyfrog.util.xslttester;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

/**
 *
 */
class XMLTextArea extends TextArea {
    //
    private String parentPath = null;

    /**
     *
     */
    public XMLTextArea() {
        setTransferHandler(new XMLStringTransferHandler(getTransferHandler()));
    }

    //
    //
    //

    /**
     *
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    String getEncoding() throws IOException, SAXException {
        String s = getText().trim();
        if (s.startsWith("<?xml")) {
            int index = s.indexOf("encoding=\"");
            if (index != -1) {
                if (s.substring(0, index).indexOf(">") == -1) {
                    index = index + "encoding=\"".length();
                    return s.substring(index, s.indexOf("\"", index));
                }
            }
            return "utf-8";
        }

        return null;
    }

    /**
     *
     */
    StreamSource getStreamSource() {
        return new StreamSource(new StringReader(getText()));
    }

    /**
     *
     * @param parentPath
     */
    void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     *
     */
    String getParentPath() {
        return this.parentPath;
    }

    /**
     *
     * @param in
     * @throws java.io.IOException
     */
    void load(Reader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            int n = 0;
            char[] cbuf = new char[1024];
            while ((n = in.read(cbuf)) != -1) {
                sb.append(cbuf, 0, n);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        setText(sb.toString());
    }

    //
    //
    //

    /**
     *
     */
    private class XMLStringTransferHandler extends TransferHandler {
        //
        private TransferHandler defaultHandler = null;

        /**
         *
         * @param defaultHandler
         */
        XMLStringTransferHandler(TransferHandler defaultHandler) {
            this.defaultHandler = defaultHandler;
        }

        /** */
        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
            this.defaultHandler.exportToClipboard(comp, clip, action);
        }

        /** */
        @Override
        public boolean canImport(JComponent comp,
                                 DataFlavor[] transferFlavors) {
            return true;
        }

        /** */
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                Class c = t.getTransferDataFlavors()[0].getRepresentationClass();
                DataFlavor flavor = t.getTransferDataFlavors()[0];
                Object obj = t.getTransferData(flavor);
                Reader in = null;
                if (flavor.isFlavorJavaFileListType()) {
                    File f = (File) ((List) obj).get(0);
                    XMLTextArea.this.parentPath = f.getParent();
                    in = new FileReader(f);
                    load(new BufferedReader(in));
                } else if (c == URL.class) {
                    in = new InputStreamReader(((URL) obj).openStream());
                    load(new BufferedReader(in));
                } else {
                    this.defaultHandler.importData(comp, t);
                }
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

            return true;
        }
    }
}
