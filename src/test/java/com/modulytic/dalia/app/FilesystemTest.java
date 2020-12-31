package com.modulytic.dalia.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilesystemTest {
    @Test
    void checkCorrectPrefix() {
        String env = System.getenv(Constants.ENV_PREFIX);
        if (env == null) {
            assertEquals(Constants.PREFIX_FALLBACK, Filesystem.getPrefix());
        }
        else {
            assertEquals(env, Filesystem.getPrefix());
        }
    }

    @Test
    void getPrefixFile() {
        String prefix = Filesystem.getPrefix();

        assertEquals(prefix + "/testfile", Filesystem.getPrefixFile("testfile"));
        assertEquals(prefix + "/s/testfile", Filesystem.getPrefixFile("testfile", "s"));
        assertEquals(prefix + "/s/p/testfile", Filesystem.getPrefixFile("testfile", "s/p"));
    }
}