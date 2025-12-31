package com.photon.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationModeUtil {

    private static final boolean IS_REACTIVE;

    static {
        IS_REACTIVE = detectWebFlux();
        log.info("Application running in {} mode", IS_REACTIVE ? "Reactive (WebFlux)" : "MVC (Servlet)");
    }

    private ApplicationModeUtil() {}

    private static boolean detectWebFlux() {
        try {
            Class.forName("org.springframework.web.reactive.config.WebFluxConfigurer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isReactive() {
        return IS_REACTIVE;
    }
}
