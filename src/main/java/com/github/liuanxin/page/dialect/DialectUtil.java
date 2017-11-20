package com.github.liuanxin.page.dialect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author https://github.com/liuanxin
 */
public class DialectUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DialectUtil.class);

    /** auto load file with "impl directory", file end with Dialect suffix and child with Dialect class */
    private static final Map<String, Class<? extends Dialect>> DIALECT_MAP
            = new HashMap<String, Class<? extends Dialect>>();
    static {
        Class<Dialect> clazz = Dialect.class;
        String classPackage = clazz.getPackage().getName() + ".impl";
        URL url = clazz.getClassLoader().getResource("");
        if (url != null) {
            if ("file".equals(url.getProtocol())) {
                File parent = new File(url.getPath() + classPackage.replace(".", "/"));
                if (parent.isDirectory()) {
                    File[] files = parent.listFiles();
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            Class<? extends Dialect> aClass = getClass(file.getName(), classPackage);
                            if (aClass != null) {
                                DIALECT_MAP.put(dialectName(file.getName()), aClass);
                            }
                        }
                    }
                }
            } else if ("jar".equals(url.getProtocol())) {
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.endsWith(".class")) {
                            name = name.substring(name.lastIndexOf("/") + 1);
                            Class<? extends Dialect> aClass = getClass(name, classPackage);
                            if (aClass != null) {
                                DIALECT_MAP.put(dialectName(name), aClass);
                            }
                        }
                    }
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("can't load jar: " + url, e);
                    }
                } finally {
                    if (jarFile != null) {
                        try {
                            jarFile.close();
                        } catch (IOException e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("can't close jar: " + url, e);
                            }
                        }
                    }
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    private static Class<? extends Dialect> getClass(String name, String classPackage) {
        if (name != null && !"".equals(name)) {
            String className = classPackage + "." + name.replace(".class", "");
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz != null && Dialect.class.isAssignableFrom(clazz)) {
                    return (Class<? extends Dialect>) clazz;
                }
            } catch (ClassNotFoundException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(String.format("can't load class file (%s): ", className), e);
                }
            }
        }
        return null;
    }
    private static String dialectName(String dialect) {
        return dialect.substring(0, dialect.indexOf(Dialect.class.getSimpleName())).toLowerCase();
    }

    public static Class<? extends Dialect> getDialect(String dialect) {
        return DIALECT_MAP.get(dialect.toLowerCase());
    }

    public static void main(String[] args) {
        System.out.println(DIALECT_MAP);
    }
}
