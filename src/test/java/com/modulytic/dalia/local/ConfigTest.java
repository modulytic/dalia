package com.modulytic.dalia.local;

import com.modulytic.dalia.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @Test
    void checkCorrectPrefix() {
        String env = System.getenv(Constants.ENV_PREFIX);
        if (env == null) {
            assertEquals(Constants.PREFIX_FALLBACK, Config.getPrefix());
        }
        else {
            assertEquals(env, Config.getPrefix());
        }
    }

    @Test
    void getPrefixFile() {
        String prefix = Config.getPrefix();

        assertEquals(prefix + "/testfile", Config.getPrefixFile("testfile"));
        assertEquals(prefix + "/s/testfile", Config.getPrefixFile("testfile", "s"));
        assertEquals(prefix + "/s/p/testfile", Config.getPrefixFile("testfile", "s/p"));
    }
}