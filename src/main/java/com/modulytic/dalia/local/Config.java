package com.modulytic.dalia.local;

import com.modulytic.dalia.Constants;

import java.io.File;

public class Config {
    public static String getPrefix() {
        String prefix = System.getenv(Constants.PREFIX_ENV);
        if (prefix == null)
            prefix = Constants.PREFIX_FALLBACK;

        return prefix;
    }

    public static String getPrefixFile(String file) {
        return getPrefixFile(file, "");
    }

    public static String getPrefixFile(String file, String subdir) {
        File flatPath = new File(file);
        if (!subdir.isEmpty())
            flatPath = new File(subdir, file);

        File joinedPath = new File(getPrefix(), flatPath.getPath());
        return joinedPath.getAbsolutePath();
    }
}
