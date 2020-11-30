package com.modulytic.dalia.local;

import com.modulytic.dalia.Constants;

import java.io.File;

/**
 * Manage Dalia configuration
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class Config {
    /**
     * Get prefix directory, check {@link Constants#ENV_PREFIX environment variable}, fall back to {@link Constants#PREFIX_FALLBACK}
     * @return  String with path to Dalia prefix
     */
    public static String getPrefix() {
        String prefix = System.getenv(Constants.ENV_PREFIX);
        if (prefix == null)
            prefix = Constants.PREFIX_FALLBACK;

        return prefix;
    }

    /**
     * Get full path to file in prefix directory
     * @param file  Name of file, not relative to any other directory
     * @return      Full path as string
     */
    public static String getPrefixFile(String file) {
        return getPrefixFile(file, "");
    }

    /**
     * Get full path to file in prefix directory
     * @param file          Name of file, not relative to any other directory
     * @param subdirectory  If the file is in a subdirectory of the prefix
     * @return              Full path as string
     */
    public static String getPrefixFile(String file, String subdirectory) {
        File flatPath = new File(file);
        if (!subdirectory.isEmpty())
            flatPath = new File(subdirectory, file);

        File joinedPath = new File(getPrefix(), flatPath.getPath());
        return joinedPath.getAbsolutePath();
    }
}
