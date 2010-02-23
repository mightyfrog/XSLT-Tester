package org.mightyfrog.util.xslttester;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 *
 */
class ExtensionPanel extends JPanel {
    //
    private final JSplitPane SP = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private final TextArea CODE_TA = new TextArea();
    private final TextArea MESSAGE_TA = new TextArea() {
            /** */
            @Override
            public void setText(String text) {
                super.setText(text);
                setCaretPosition(0);
            }
        };

    //
    private final File USER_DIR = new File(System.getProperty("user.dir"));

    /**
     *
     */
    public ExtensionPanel() {
        setLayout(new BorderLayout());

        SP.setTopComponent(new JScrollPane(CODE_TA));
        SP.setBottomComponent(new JScrollPane(MESSAGE_TA));
        SP.setDividerLocation(400);

        add(SP);
    }

    //
    //
    //

    /**
     *
     */
    String getText() {
        return CODE_TA.getText();
    }

    /**
     * Returns the package name.
     *
     */
    String getPackageName() {
        String s = CODE_TA.getText();
        int index = s.indexOf("package");
        if (index == -1) {
            return null;
        }
        index += "package".length();
        s = s.substring(index, s.indexOf(";"));

        return s.trim();
    }

    /**
     * Returns the class name.
     *
     */
    String getClassName() {
        String s= CODE_TA.getText();
        int index = s.indexOf("class");
        if (index == -1) {
            return "dummy";
        }
        try {
            index += "class".length();
            s = s.substring(index, s.indexOf("{", index)).trim();
            if (s.indexOf(" ") != -1) {
                s = s.substring(0, s.indexOf(" "));
            }
        } catch (StringIndexOutOfBoundsException e) {
            return "dummy";
        }

        return s.trim();
    }

    /**
     * Compiles in-memory source code.
     *
     */
    void compile() {
        // TODO: move me
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            MESSAGE_TA.append(I18N.get("message.2") + "\n");
            return;
        }

        String[] args = new String[]{"-verbose"};
        StandardJavaFileManager fm =
            compiler.getStandardFileManager(null, null, null);
        try {
            fm.setLocation(StandardLocation.CLASS_OUTPUT,
                           Arrays.asList(new File[]{USER_DIR}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringWriter out = new StringWriter();
        CompilationTask task = compiler.getTask(out,
                                                fm,
                                                null,
                                                Arrays.asList(args),
                                                null,
                                                getJavaFileObject());
        boolean success = task.call();
        MESSAGE_TA.append(out.toString() + "\n");
        if (success) {
            installClass();
        } else {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                          I18N.get("dialog.2"),
                                          UIManager.getString("OptionPane.messageDialogTitle"),
                                          JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     *
     */
    List<JavaFileObject> getJavaFileObject() {
        List<JavaFileObject> list = new ArrayList<JavaFileObject>();
        String packageName = getPackageName();
        if (packageName !=  null) {
            packageName = packageName.replace(".", "/") + "/";
        } else {
            packageName = "";
        }
        list.add(new SimpleJavaFileObject(URI.create("string:///" + packageName +
                                                     getClassName() + ".java"),
                                          JavaFileObject.Kind.SOURCE) {
                /** */
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return CODE_TA.getText();
                }
            });

        return list;
    }

    //
    //
    //

    /**
     *
     */
    private void installClass() {
        String packageName = getPackageName();
        if (packageName != null) {
            packageName = packageName.replace(".", "/") + "/";
        } else {
            packageName = "";
        }
        File classFile = new File(USER_DIR,
                                  packageName + getClassName() + ".class");
        Util.installClass(classFile);
        try {
            if (packageName.length() == 0) {
                Class.forName(getClassName());
            } else {
                Class.forName(getPackageName() + "." + getClassName());
            }
            ((XSLTTester) JOptionPane.getRootFrame()).
                appendMessage(I18N.get("message.1", classFile.getName()) + "\n");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
