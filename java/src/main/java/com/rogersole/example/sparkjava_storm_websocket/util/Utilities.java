package com.rogersole.example.sparkjava_storm_websocket.util;

public class Utilities {

    /**
     * Loads the environment variable requested, and if not found returns the default value.
     * 
     * @param envVar: Environment variable to be loaded
     * @param defaultReturn: String to be returned if env var not found
     * @return String
     */
    public static String loadEnvOrDefault(String envVar, String defaultReturn) {
        String value = System.getenv(envVar);
        if (value == null)
            value = defaultReturn;
        return value;
    }
}
