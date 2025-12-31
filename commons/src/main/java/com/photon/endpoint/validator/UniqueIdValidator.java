//package com.photon.endpoint.validator;
//
//import com.photon.endpoint.annotation.ActionInfo;
//import com.photon.endpoint.annotation.FeatureInfo;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.BeanPostProcessor;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//import java.util.HashSet;
//import java.util.Set;
//
//@Component
//public class UniqueIdValidator implements BeanPostProcessor {
//
//    private final Set<String> controllerIds = new HashSet<>();
//    private final Set<String> endpointIds = new HashSet<>();
//
//    @Override
//    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//        Class<?> clazz = bean.getClass();
//
//        if (clazz.isAnnotationPresent(FeatureInfo.class)) {
//            FeatureInfo controllerId = clazz.getAnnotation(FeatureInfo.class);
//            String id = controllerId.id();
//            if (!controllerIds.add(id)) {
//                throw new IllegalStateException("Duplicate Feature (Controller) ID found: " + id);
//            }
//        }
//
//        for (Method method : clazz.getDeclaredMethods()) {
//            if (method.isAnnotationPresent(ActionInfo.class)) {
//                ActionInfo endpointId = method.getAnnotation(ActionInfo.class);
//                String id = endpointId.id();
//                if (!endpointIds.add(id)) {
//                    throw new IllegalStateException("Duplicate Action ID found: " + id + " in class " + clazz.getName());
//                }
//            }
//        }
//
//        return bean;
//    }
//}