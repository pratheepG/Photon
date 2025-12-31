package com.photon.console.sdk;

import java.util.Map;

@FunctionalInterface
public interface ProgressListener {
    /**
     * percent: 0..100 (int)
     * message: human readable
     * meta: optional map with extra info (moduleId, lang, phase, etc)
     */
    void onProgress(int percent, String message, Map<String,Object> meta);
}