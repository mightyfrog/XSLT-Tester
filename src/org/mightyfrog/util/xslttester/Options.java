package org.mightyfrog.util.xslttester;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Shigehiro Soejima
 */
class Options {
    //
    static final String FILE_NAME = "xslttester.xml";

    //
    static final String TOOLS_JAR_PATH = "toolsJarPath";

    //
    private static final Properties PROP = new Properties() {
            {
                BufferedInputStream in = null;
                try {
                    File f = new File(FILE_NAME);
                    if (!f.exists() && !f.createNewFile()) {
                        // TODO: do something here
                    } else {
                        in = new BufferedInputStream(new FileInputStream(f));
                        loadFromXML(in);
                    }
                } catch (IOException e) {
                    // ignore
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        };

    /**
     *
     */
    private Options() {
        // this is a singleton class.
    }

    /**
     *
     */
    static File getToolsJarPath() {
        String path = PROP.getProperty(TOOLS_JAR_PATH);
        if (path == null) {
            return null;
        }
        return new File(path);
    }

    /**
     *
     * @param toolsJarPath
     */
    static void setToolsJarPath(String toolsJarPath) {
        PROP.setProperty(TOOLS_JAR_PATH, toolsJarPath);
    }

    /**
     *
     * @param property
     */
    static void setProperty(String key, String value) {
        PROP.setProperty(key, value);
    }

    /**
     *
     */
    static void store() {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(FILE_NAME));
            PROP.storeToXML(out, null, "UTF-8");
        } catch (IOException e) { // UnsupportedEncodingException shadowed
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     *
     * @param key
     */
    static Object remove(String key) {
        return PROP.remove(key);
    }

    //
    //
    //

    /**
     *
     * @param s
     */
    private static boolean toBoolean(String s) {
        return Boolean.parseBoolean(s);
    }
}
