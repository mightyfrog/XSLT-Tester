package org.mightyfrog.util.xslttester;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Shigehiro Soejima
 */
public class XSLTTester extends JFrame implements MenuListener {
    //
    private final JDesktopPane DP = new JDesktopPane();

    private final XMLTextArea XSL_TEXT_AREA = new XMLTextArea();
    private final XMLTextArea IN_TEXT_AREA = new XMLTextArea() {
            /** */
            @Override
            public void setText(String text) {
                super.setText(text);
                if (text != null) {
                    processPI(text);
                }
            }
        };
    private final OutputPanel OUTPUT_PANEL = new OutputPanel();
    private final ExtensionPanel EXT_PANEL = new ExtensionPanel();
    private final TextArea MESSAGE_TEXT_AREA = new TextArea();

    private final InternalFrame INPUT_FRAME =
        new InternalFrame(I18N.get("iframe.input"),
                          new JScrollPane(IN_TEXT_AREA));
    private final InternalFrame XSLT_FRAME =
        new InternalFrame(I18N.get("iframe.xslt"),
                          new JScrollPane(XSL_TEXT_AREA));
    private final InternalFrame OUTPUT_FRAME =
        new InternalFrame(I18N.get("iframe.output"), OUTPUT_PANEL);
    private final InternalFrame MESSAGE_FRAME =
        new InternalFrame(I18N.get("iframe.message"),
                          new JScrollPane(MESSAGE_TEXT_AREA));
    private final InternalFrame EXTENSION_FRAME =
        new InternalFrame(I18N.get("iframe.extension"),
                          new JScrollPane(EXT_PANEL));

    // file menu
    private JMenu fileMenu = null;
    private JMenu openMenu = null;
    private JMenuItem openXMLMI = null;
    private JMenuItem openXSLMI = null;
    private JMenuItem saveAsMI = null;
    private JMenuItem clearAllMI = null;
    private JMenuItem exitMI = null;

    // xslt menu
    private JMenu xsltMenu = null;
    private JMenuItem transformMI = null;
    private JMenuItem browseMI = null;
    private JMenu contentTypeMenu = null;
    private JRadioButtonMenuItem htmlMI = null;
    private JRadioButtonMenuItem xmlMI = null;
    private JRadioButtonMenuItem plainMI = null;
    private JMenuItem compileMI = null;
    private JMenu installMenu = null;
    private JMenuItem classMI = null;
    private JMenuItem jarMI = null;
    private JMenuItem toolsJarPathMI = null;

    // about menu
    private JMenu helpMenu = null;
    private JMenuItem wikiMI = null;
    private JMenuItem aboutMI = null;

    //
    private JFileChooser fileChooser = null;
    private FileFilter defaultFilter = null;
    private FileFilter classFilter = null;
    private FileFilter jarFilter = null;

    /**
     *
     */
    public XSLTTester() {
        setTitle(I18N.get("frame.title"));
        setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());

        setContentPane(DP);

        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(3);
        setVisible(true);

        setJMenuBar(createMenuBar());

        layoutInternalFrames();
        JOptionPane.setRootFrame(this);

        try {
            File toolsJar = Options.getToolsJarPath();
            if (toolsJar != null) {
                Util.installJar(toolsJar);
                String msg = I18N.get("message.3", toolsJar.getPath());
                MESSAGE_TEXT_AREA.setText(msg + "\n\n");
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null) {
                MESSAGE_TEXT_AREA.setText(msg + "\n\n");
            }
        }

        addWindowListener(new WindowAdapter() {
                /** */
                @Override
                public void windowClosing(WindowEvent evt) {
                    exit();
                }
            });
    }

    /** */
    @Override
    public void menuCanceled(MenuEvent evt){
        // no-op
    }

    /** */
    @Override
    public void menuDeselected(MenuEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void menuSelected(MenuEvent evt) {
        if (IN_TEXT_AREA.getText().trim().isEmpty() ||
            XSL_TEXT_AREA.getText().trim().isEmpty()) {
            this.transformMI.setEnabled(false);
        } else {
            this.transformMI.setEnabled(true);
        }
        if (XSL_TEXT_AREA.getText().trim().isEmpty()) {
            this.saveAsMI.setEnabled(false);
        } else {
            this.saveAsMI.setEnabled(true);
        }
        if (OUTPUT_PANEL.getText().trim().isEmpty()) {
            this.browseMI.setEnabled(false);
        } else {
            this.browseMI.setEnabled(true);
        }
        if (EXT_PANEL.getText().trim().isEmpty()) {
            this.compileMI.setEnabled(false);
        } else {
            this.compileMI.setEnabled(true);
        }
    }

    /**
     *
     */
    public static void main(String[] args) {
        try {
            boolean nimbus = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getClassName().indexOf("Nimbus") != -1) {
                    nimbus = true;
                    break;
                }
            }
            if (nimbus) {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            // ClassNotFoundException, InstantiationException, IllegalAccessException
            // javax.swing.UnsupportedLookAndFeelException
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        EventQueue.invokeLater(new Runnable() {
                /** */
                @Override
                public void run() {
                    new XSLTTester();
                }
            });
    }

    /**
     *
     * @param running
     */
    void toggleCursor(boolean running) {
        Component gp = getGlassPane();
        if (running) {
            gp.addMouseListener(new MouseAdapter() {
                    /** */
                    @Override
                    public void mousePressed(MouseEvent evt) {
                        evt.consume();
                    }
                });
            gp.setVisible(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            MouseListener[] ml = gp.getMouseListeners();
            for (MouseListener l : ml) {
                gp.removeMouseListener(l);
            }
            gp.setVisible(false);
            setCursor(null);
        }
    }

    //
    //
    //

    /**
     * Processes processing instruction.
     *
     * @param text
     */
    private void processPI(String text) {
        int index = text.indexOf("<?xml-stylesheet");
        if (index != -1 && text.indexOf("text/xsl") != -1) {
            int option =
                JOptionPane.showConfirmDialog(XSLTTester.this,
                                              I18N.get("dialog.1"));
            if (option == JOptionPane.YES_OPTION) {
                index += "<?xml-stylesheet".length();
                index = text.indexOf("href", index);
                index += 6;
                String path = text.substring(index, text.indexOf("\"", index));
                try {
                    if (path.toLowerCase().startsWith("http")) {
                        toggleCursor(true);
                        URL url = new URL(path);
                        XSL_TEXT_AREA.load(new InputStreamReader(url.openStream()));
                        transform();
                    } else {
                        path = IN_TEXT_AREA.getParentPath() + "/" + path;
                        XSL_TEXT_AREA.load(new FileReader(path));
                        transform();
                    }
                } catch (IOException e) {
                    // java.net.MalformedURLException
                    // java.io.FileNotFoundException 
                    JOptionPane.showMessageDialog(XSLTTester.this,
                                                  e.getLocalizedMessage(),
                                                  e.getClass().getName(),
                                                  JOptionPane.ERROR_MESSAGE);
                } finally {
                    toggleCursor(false);
                }

            }
        }
    }

    /**
     *
     */
    private void exit() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("desktop", getBounds());

        map.put("inputFrame", INPUT_FRAME.getBounds());
        map.put("xsltFrame", XSLT_FRAME.getBounds());
        map.put("outputFrame", OUTPUT_FRAME.getBounds());
        map.put("messageFrame", MESSAGE_FRAME.getBounds());
        map.put("extensionFrame", EXTENSION_FRAME.getBounds());

        map.put("inputFrame.icon", INPUT_FRAME.isIcon());
        map.put("xsltFrame.icon", XSLT_FRAME.isIcon());
        map.put("outputFrame.icon", OUTPUT_FRAME.isIcon());
        map.put("messageFrame.icon", MESSAGE_FRAME.isIcon());
        map.put("extensionFrame.icon", EXTENSION_FRAME.isIcon());
        ObjectOutputStream oos = null;
        try {
            oos =
                new ObjectOutputStream(new FileOutputStream("xslttester.dat"));
            oos.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     *
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createFileMenu());
        menuBar.add(createXSLTMenu());
        menuBar.add(createHelpMenu());

        return menuBar;
    }

    /**
     *
     */
    private JMenu createFileMenu() {
        this.fileMenu = new JMenu(I18N.get("menu.file"));
        this.openMenu = new JMenu(I18N.get("menu.open"));
        this.openXMLMI = new JMenuItem(I18N.get("menuitem.input.xml"));
        this.openXSLMI = new JMenuItem(I18N.get("menuitem.transformer.xsl"));
        this.saveAsMI = new JMenuItem(I18N.get("menuitem.save.as"));
        this.clearAllMI = new JMenuItem(I18N.get("menuitem.clear.all"));
        this.exitMI = new JMenuItem(I18N.get("menuitem.exit"));

        this.openMenu.add(this.openXMLMI);
        this.openMenu.add(this.openXSLMI);
        this.fileMenu.add(this.openMenu);
        this.fileMenu.add(this.saveAsMI);
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.clearAllMI);
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.exitMI);

        this.clearAllMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    IN_TEXT_AREA.setText(null);
                    XSL_TEXT_AREA.setText(null);
                    OUTPUT_PANEL.clear();
                    IN_TEXT_AREA.requestFocusInWindow();
                }
            });

        this.saveAsMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    saveAs();
                }
            });

        this.openXMLMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    openXML();
                }
            });

        this.openXSLMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    openXSL();
                }
            });

        this.exitMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    System.exit(0);
                }
            });

        this.fileMenu.setMnemonic('F');
        this.openMenu.setMnemonic('O');
        this.openXMLMI.setMnemonic('I');
        this.openXSLMI.setMnemonic('T');
        this.saveAsMI.setMnemonic('S');
        this.clearAllMI.setMnemonic('C');
        this.exitMI.setMnemonic('X');

        return this.fileMenu;
    }

    /**
     *
     */
    private JMenu createXSLTMenu() {
        this.xsltMenu = new JMenu(I18N.get("menu.xslt"));
        this.transformMI = new JMenuItem(I18N.get("menuitem.transform"));
        this.contentTypeMenu = new JMenu(I18N.get("menu.output.content.type"));
        this.htmlMI = new JRadioButtonMenuItem(I18N.get("menuitem.text.html"));
        this.xmlMI = new JRadioButtonMenuItem(I18N.get("menuitem.text.xml"));
        this.plainMI = new JRadioButtonMenuItem(I18N.get("menuitem.text.plain"));
        this.browseMI = new JMenuItem(I18N.get("menuitem.open.in.browser"));
        this.compileMI = new JMenuItem(I18N.get("menuitem.compile.java.ext"));
        this.installMenu = new JMenu(I18N.get("menu.install.java.ext"));
        this.classMI = new JMenuItem(I18N.get("menuitem.class"));
        this.jarMI = new JMenuItem(I18N.get("menuitem.jar"));
        this.toolsJarPathMI =
            new JMenuItem(I18N.get("menuitem.remember.tools.jar.path"));

        ButtonGroup bg = new ButtonGroup();
        bg.add(this.htmlMI);
        bg.add(this.xmlMI);
        bg.add(this.plainMI);

        this.xsltMenu.add(this.transformMI);
        this.xsltMenu.add(this.contentTypeMenu);
        this.contentTypeMenu.add(this.htmlMI);
        this.contentTypeMenu.add(this.xmlMI);
        this.contentTypeMenu.add(this.plainMI);
        this.xsltMenu.addSeparator();
        this.xsltMenu.add(this.browseMI);
        this.xsltMenu.addSeparator();
        this.xsltMenu.add(this.compileMI);
        this.xsltMenu.add(this.installMenu);
        this.xsltMenu.addSeparator();
        this.xsltMenu.add(this.toolsJarPathMI);
        this.installMenu.add(this.classMI);
        this.installMenu.add(this.jarMI);


        this.fileMenu.addMenuListener(this);
        this.xsltMenu.addMenuListener(this);
        this.transformMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    transform();
                }
            });

        this.transformMI.setEnabled(false);

        this.browseMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    browse();
                }
            });
        this.browseMI.setEnabled(false);

        this.compileMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    EXT_PANEL.compile();
                }
            });

        this.toolsJarPathMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    File toolsJar = Options.getToolsJarPath();
                    JFileChooser fc = null;
                    if (toolsJar != null) {
                        fc = new JFileChooser(toolsJar);
                        fc.setSelectedFile(toolsJar);
                    } else {
                        fc = new JFileChooser();
                    }
                    fc.showOpenDialog(XSLTTester.this);
                    File file = fc.getSelectedFile();
                    if (file != null) {
                        Options.setToolsJarPath(file.getAbsolutePath());
                        Options.store();
                    }
                }
            });

        ActionListener l = new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    OUTPUT_PANEL.setContentType(evt.getActionCommand());
                }
            };
        this.htmlMI.setSelected(true);
        this.htmlMI.addActionListener(l);
        this.xmlMI.addActionListener(l);
        this.plainMI.addActionListener(l);

        this.classMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    getFileChooser().setFileFilter(getClassFilter());
                    int option = getFileChooser().showOpenDialog(XSLTTester.this);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File file = getFileChooser().getSelectedFile();
                        if (file != null) {
                            if (!Util.isClassFile(file)) {
                                appendMessage(I18N.get("message.4", file.getName()));
                                return;
                            }
                            try {
                                Util.installClass(file);
                            } catch (Exception e) {
                                e.printStackTrace();
                                appendMessage(I18N.get("message.0", file.getName(),
                                                       e.getMessage()));
                                return;
                            }
                            appendMessage(I18N.get("message.1", file.getName()));
                        }
                    }
                }
            });
        this.jarMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    getFileChooser().setFileFilter(getJarFilter());
                    int option = getFileChooser().showOpenDialog(XSLTTester.this);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File file = getFileChooser().getSelectedFile();
                        if (file != null) {
                            if (!Util.isJarFile(file)) {
                                appendMessage(I18N.get("message.5", file.getName()));
                                return;
                            }
                            try {
                                Util.installJar(file);
                            } catch (Exception e) {
                                appendMessage(I18N.get("message.0", file.getName(),
                                                       e.getMessage()));
                                return;
                            }
                            appendMessage(I18N.get("message.1", file.getName()));
                        }
                    }
                }
            });

        this.xsltMenu.setMnemonic('X');
        this.transformMI.setMnemonic('T');
        this.transformMI.setAccelerator(KeyStroke.getKeyStroke("pressed F5"));
        this.browseMI.setMnemonic('B');
        this.browseMI.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed B"));
        this.contentTypeMenu.setMnemonic('C');

        return this.xsltMenu;
    }

    /**
     *
     */
    private JMenu createHelpMenu() {
        this.helpMenu = new JMenu(I18N.get("menu.help"));
        this.wikiMI = new JMenuItem(I18N.get("menuitem.wiki"));
        this.aboutMI = new JMenuItem(I18N.get("menuitem.about"));

        this.aboutMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    showAboutDialog();
                }
            });

        this.wikiMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        URI uri = new URI(I18N.get("wiki.url"));
                        Desktop.getDesktop().browse(uri);
                    } catch (URISyntaxException e) {
                        // e.printStackTrace();
                    } catch (IOException e) {
                    }
                }
            });

        this.helpMenu.add(this.wikiMI);
        this.helpMenu.add(this.aboutMI);

        return this.helpMenu;
    }

    /**
     *
     */
    private JFileChooser getFileChooser() {
        if (this.fileChooser == null) {
            this.fileChooser = new JFileChooser();
            File file = new File(System.getProperty("user.dir"));
            this.fileChooser.setCurrentDirectory(file);
            this.defaultFilter = this.fileChooser.getFileFilter();
        }

        return this.fileChooser;
    }

    /**
     *
     */
    private void layoutInternalFrames() {
        boolean inputFrameIcon = false;
        boolean xsltFrameIcon = false;
        boolean outputFrameIcon = false;
        boolean messageFrameIcon = false;
        boolean extensionFrameIcon = true;

        File file = new File("xslttester.dat");
        HashMap<String, Object> map = null;
        if (!file.exists()) {
            Rectangle rect = DP.getBounds();
            int bw = rect.width;
            int bh = rect.height - 24;
            
            int inset = 3;

            int w = (bw - 3 * inset) / 2; // width
            int hl = (bh - 3 * inset) / 2; // height left
            int hrb = (bh - 3 * inset) / 3; // widht right bottom
            int hrt = hrb * 2; // width right top

            INPUT_FRAME.setSize(w, hl);
            XSLT_FRAME.setSize(w, hl - 24);
            OUTPUT_FRAME.setSize(w, hrt);
            MESSAGE_FRAME.setSize(w, hrb);
            EXTENSION_FRAME.setSize(w, bh);

            INPUT_FRAME.setLocation(inset, inset);
            XSLT_FRAME.setLocation(inset, hl + 2 * inset);
            OUTPUT_FRAME.setLocation(w + 2 * inset, inset);
            MESSAGE_FRAME.setLocation(w + 2 * inset, hrt + 2 * inset);
            EXTENSION_FRAME.setLocation((bw - w) / 2, inset);
        } else {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                map = (HashMap<String, Object>) ois.readObject();
                INPUT_FRAME.setBounds((Rectangle) map.get("inputFrame"));
                XSLT_FRAME.setBounds((Rectangle) map.get("xsltFrame"));
                OUTPUT_FRAME.setBounds((Rectangle) map.get("outputFrame"));
                MESSAGE_FRAME.setBounds((Rectangle) map.get("messageFrame"));
                EXTENSION_FRAME.setBounds((Rectangle) map.get("extensionFrame"));
            } catch (ClassNotFoundException e) {
            } catch (IOException e) {
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
                
        }

        if (map != null) {
            setBounds((Rectangle) map.get("desktop"));
        }
        DP.add(INPUT_FRAME);
        DP.add(XSLT_FRAME);
        DP.add(OUTPUT_FRAME);
        DP.add(MESSAGE_FRAME);
        DP.add(EXTENSION_FRAME);

        INPUT_FRAME.setVisible(true);
        XSLT_FRAME.setVisible(true);
        OUTPUT_FRAME.setVisible(true);
        MESSAGE_FRAME.setVisible(true);
        EXTENSION_FRAME.setVisible(true);

        //MESSAGE_TEXT_AREA.removeUndoableEditListener();

        final HashMap<String, Object> mm = map;
        EventQueue.invokeLater(new Runnable() {
                /** */
                @Override
                public void run() {
                    try {
                        INPUT_FRAME.setSelected(true);
                        if (mm != null) {
                            INPUT_FRAME.setIcon((Boolean) mm.get("inputFrame.icon"));
                            XSLT_FRAME.setIcon((Boolean) mm.get("xsltFrame.icon"));
                            OUTPUT_FRAME.setIcon((Boolean) mm.get("outputFrame.icon"));
                            MESSAGE_FRAME.setIcon((Boolean) mm.get("messageFrame.icon"));
                            EXTENSION_FRAME.setIcon((Boolean) mm.get("extensionFrame.icon"));
                        } else {
                            EXTENSION_FRAME.setIcon(true);
                        }
                    } catch (java.beans.PropertyVetoException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    /**
     *
     */
    void transform() {
        StringWriter out = new StringWriter();
        try {
            TransformerFactory f = TransformerFactory.newInstance();
            f.setErrorListener(new ErrorListener() {
                    /** */
                    @Override
                    public void error(TransformerException e) {
                        appendMessage(e.getLocalizedMessage());
                    }

                    /** */
                    @Override
                    public void fatalError(TransformerException e) {
                        appendMessage(e.getLocalizedMessage());
                    }

                    /** */
                    @Override
                    public void warning(TransformerException e) {
                        appendMessage(e.getLocalizedMessage());
                    }
                });
            f.setAttribute("indent-number", new Integer(2));
            Transformer t =
                f.newTransformer(XSL_TEXT_AREA.getStreamSource());

            String encoding = IN_TEXT_AREA.getEncoding();
            if (encoding == null) {
                t.setOutputProperty(OutputKeys.ENCODING, "utf-8");
                //t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            } else {
                t.setOutputProperty(OutputKeys.ENCODING, encoding);
            }
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            t.transform(IN_TEXT_AREA.getStreamSource(), new StreamResult(out));
        } catch (Exception e) {
            // IOException, SAXExcepion, TransformerConfigurationException,
            // TransformerException
            String message = e.getLocalizedMessage();
            if (e.getCause() != null) {
                message = e.getCause().getLocalizedMessage();
            }
            JOptionPane.showMessageDialog(this, message,
                                          e.getClass().getName(),
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
        OUTPUT_PANEL.setText(out.toString());
    }

    /**
     *
     */
    private void saveAs() {
        getFileChooser().setFileFilter(this.defaultFilter);
        int option = getFileChooser().showOpenDialog(XSLTTester.this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = getFileChooser().getSelectedFile();
            if (file != null) {
                Writer out = null;
                try {
                    out = new BufferedWriter(new FileWriter(file));
                    XMLTextArea ta = XSL_TEXT_AREA;
                    Reader in =
                        new BufferedReader(new StringReader(ta.getText()));
                    int n = 0;
                    char[] cbuf = new char[1024];
                    while ((n = in.read(cbuf)) != -1) {
                        out.write(cbuf, 0, n);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    private void openXML() {
        openFile(IN_TEXT_AREA);
    }

    /**
     *
     */
    private void openXSL() {
        openFile(XSL_TEXT_AREA);
    }

    /**
     *
     * @param ta
     */
    private void openFile(final XMLTextArea ta) {
        getFileChooser().setFileFilter(this.defaultFilter);
        int option = getFileChooser().showOpenDialog(XSLTTester.this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = getFileChooser().getSelectedFile();
            if (file != null) {
                ta.setParentPath(file.getParent());
                Reader in = null;
                StringBuilder sb = new StringBuilder();
                try {
                    in = new BufferedReader(new FileReader(file));
                    int n = 0;
                    char[] cbuf = new char[1024];
                    while ((n = in.read(cbuf)) != -1) {
                        sb.append(cbuf, 0, n);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                }
                ta.setText(sb.toString());
            }
        }
    }

    /**
     *
     * @param text
     */
    void appendMessage(String text) {
        MESSAGE_TEXT_AREA.append(text + "\n");
    }

    /**
     *
     */
    private FileFilter getClassFilter() {
        if (this.classFilter == null) {
            this.classFilter = new FileFilter() {
                    /** */
                    @Override
                    public boolean accept(File f) {
                        if (f != null) {
                            return f.isDirectory() ||
                                f.getName().toLowerCase().endsWith(".class");
                        }
                        return false;
                    }

                    /** */
                    @Override
                    public String getDescription() {
                        return I18N.get("filechooser.desc.class");
                    }
                };
        }

        return this.classFilter;
    }

    /**
     *
     */
    private FileFilter getJarFilter() {
        if (this.jarFilter == null) {
            this.jarFilter = new FileFilter() {
                    /** */
                    @Override
                    public boolean accept(File f) {
                        if (f != null) {
                            return f.isDirectory() ||
                                f.getName().toLowerCase().endsWith(".jar");
                        }
                        return false;
                    }

                    /** */
                    @Override
                    public String getDescription() {
                        return I18N.get("filechooser.desc.jar");
                    }
                };
        }

        return this.jarFilter;
    }

    /**
     *
     */
    void browse() {
        String text = OUTPUT_PANEL.getText();
        File file = null;
        Writer out = null;
        Reader in = null;
        try {
            file = File.createTempFile("xslttester", ".html");
            file.deleteOnExit();
            out = new BufferedWriter(new FileWriter(file));
            in = new BufferedReader(new StringReader(text));
            int n = 0;
            char[] cbuf = new char[1024];
            while ((n = in.read(cbuf)) != -1) {
                out.write(cbuf, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }

        try {
            Desktop.getDesktop().browse(file.toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void showAboutDialog() {
        String version = I18N.get("dialog.0",
                                  "Shigehiro Soejima",
                                  "mightyfrog.gc@gmail.com",
                                  "@TIMESTAMP@");
        JOptionPane.showMessageDialog(this, version);
    }
}
