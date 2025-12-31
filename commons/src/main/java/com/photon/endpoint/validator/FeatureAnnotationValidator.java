package com.photon.endpoint.validator;

import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class FeatureAnnotationValidator implements InitializingBean {

    private final ApplicationContext context;

    public FeatureAnnotationValidator(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void afterPropertiesSet() {
        Set<String> seenFeatureIds = new HashSet<>();

        String[] beanNames = context.getBeanNamesForAnnotation(FeatureInfo.class);

        for (String name : beanNames) {
            Class<?> clazz = context.getType(name);
            FeatureInfo feature = clazz.getAnnotation(FeatureInfo.class);

            // 1. Validate unique Feature ID
            if (!seenFeatureIds.add(feature.id())) {
                throw new IllegalStateException("Duplicate Feature ID found: " + feature.id());
            }

            // 2. Must also have @RestController or @Controller
            if (!clazz.isAnnotationPresent(RestController.class) &&
                !clazz.isAnnotationPresent(Controller.class)) {
                throw new IllegalStateException("@FeatureInfo must be used with @RestController or @Controller: " + clazz.getName());
            }

            // 3. Must also have @RequestMapping
            if (!clazz.isAnnotationPresent(RequestMapping.class)) {
                throw new IllegalStateException("@FeatureInfo must also have @RequestMapping: " + clazz.getName());
            }

            Set<String> seenActionIds = new HashSet<>();

            for (Method method : clazz.getDeclaredMethods()) {
                ActionInfo action = method.getAnnotation(ActionInfo.class);
                if (action != null) {

                    if (!seenActionIds.add(action.id())) {
                        throw new IllegalStateException("Duplicate Action ID '" + action.id() + "' in class: " + clazz.getName());
                    }

                    boolean hasMapping = Arrays.stream(method.getAnnotations())
                            .anyMatch(ann -> ann.annotationType().getSimpleName().endsWith("Mapping"));

                    if (!hasMapping) {
                        throw new IllegalStateException("@ActionInfo must be used with a request mapping annotation in method: " + method.getName());
                    }
                }
            }
        }
    }
}