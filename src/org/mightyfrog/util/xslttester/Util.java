package org.mightyfrog.util.xslttester;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *
 */
class Util {
    /**
     *
     * @param file
     */
    static boolean isJarFile(File file) { // 
        boolean b = false;
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(file));
            int magic = in.readInt();
            b = magic == 0x504b0304;
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

        return b;
    }

    /**
     *
     * @param file
     */
    static boolean isClassFile(File file) {
        boolean b = false;
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(file));
            int magic = in.readInt();
            b = magic == 0xCAFEBABE;
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

        return b;
    }

    /**
     *
     * @param file
     */
    static void installClass(File file) {
        String name = file.getName();
        if (!name.toLowerCase().endsWith(".class")) {
            return;
        }
        try {
            final URL url = file.getParentFile().toURI().toURL();
            ClassLoader cl =
                AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                    /** */
                    @Override
                    public ClassLoader run() {
                        return new URLClassLoader(new URL[]{url});
                    }
                });

            name = name.substring(0, name.indexOf(".class"));
            Class c = cl.loadClass(name);
            Method method =
                URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(Util.class.getClassLoader(), new Object[]{url});
        } catch (MalformedURLException e) {
            // shouldn't happen
        } catch (Throwable e) {
            // TODO: hack
            if (e instanceof NoClassDefFoundError) {
                String s = e.getMessage();
                int index = s.indexOf("wrong name: ");
                s = s.substring(index + "wrong name: ".length(), s.length() - 1);
                for (int i = 0; i < s.split("/").length; i++) {
                    file = file.getParentFile();
                }
                try {
                    final URL url = file.toURI().toURL();
                    ClassLoader cl =
                        AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                            /** */
                            @Override
                            public ClassLoader run() {
                                return new URLClassLoader(new URL[]{url});
                            }
                        });
                    s = s.replace("/", ".");
                    cl.loadClass(s);
                    Method method =
                        URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(Util.class.getClassLoader(), new Object[]{url});
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     *
     * @param file
     * @throws java.net.MalformedURLException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException;
     */
    static void installJar(File file) throws NoSuchMethodException,
                                             IllegalAccessException,
                                             InvocationTargetException,
                                             MalformedURLException {
        try {
            Method method =
                URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(Util.class.getClassLoader(),
                          new Object[]{file.toURI().toURL()});
        } catch (NoSuchMethodException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (MalformedURLException e) {
            throw e;
        }
    }
}
