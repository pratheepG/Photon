package com.photon.endpoint.utils;

import com.photon.endpoint.dto.ModelDescriptionDto;
import com.photon.endpoint.dto.ModelFieldDto;
import com.photon.endpoint.enums.BaseType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.reflect.*;
import java.util.*;

public class ModelIntrospector {

    public static void introspectDto(Class<?> dtoClass, Set<Class<?>> seen, Set<ModelDescriptionDto> result) {
        if (dtoClass.getName().startsWith("java") || !seen.add(dtoClass)) return;

        ModelDescriptionDto model = new ModelDescriptionDto();
        model.setName(dtoClass.getSimpleName());
        model.setId(dtoClass.getCanonicalName());

        List<ModelFieldDto> fields = new ArrayList<>();

        for (Field field : dtoClass.getDeclaredFields()) {
            Class<?> type = field.getType();
            BaseType baseType = resolveBaseType(type);
            String referenceType = null;

            if (baseType == BaseType.OBJECT || baseType == BaseType.LIST || baseType == BaseType.SET) {
                referenceType = resolveReferenceType(field);
                try {
                    Class<?> nestedType = resolveGenericClass(field);
                    if (nestedType != null && !nestedType.getName().startsWith("java")) {
                        introspectDto(nestedType, seen, result);
                    }
                } catch (Exception ignored) {}
            }

            fields.add(new ModelFieldDto(field.getName(), baseType, referenceType));
        }

        model.setFields(fields);
        result.add(model);
    }

    public static Set<ModelDescriptionDto> buildDtoGraph(Class<?> root) {
        Set<Class<?>> seen = new HashSet<>();
        Set<ModelDescriptionDto> result = new HashSet<>();
        introspectDto(root, seen, result);
        return result;
    }

    private static BaseType resolveBaseType(Class<?> clazz) {
        if (clazz == String.class) return BaseType.STRING;
        if (clazz == Integer.class || clazz == int.class) return BaseType.INTEGER;
        if (clazz == Long.class || clazz == long.class) return BaseType.LONG;
        if (clazz == Float.class || clazz == float.class) return BaseType.FLOAT;
        if (clazz == Double.class || clazz == double.class) return BaseType.DOUBLE;
        if (clazz == Boolean.class || clazz == boolean.class) return BaseType.BOOLEAN;
        if (Date.class.isAssignableFrom(clazz)) return BaseType.DATE;
        if (List.class.isAssignableFrom(clazz)) return BaseType.LIST;
        if (Set.class.isAssignableFrom(clazz)) return BaseType.SET;
        if (Map.class.isAssignableFrom(clazz)) return BaseType.MAP;
        return BaseType.OBJECT;
    }

    private static String resolveReferenceType(Field field) {
        try {
            if (field.getGenericType() instanceof ParameterizedType) {
                Type[] actualTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                if (actualTypes.length > 0) {
                    return actualTypes[0].getTypeName();
                }
            }
            return field.getType().getCanonicalName();
        } catch (Exception e) {
            return null;
        }
    }

    private static Class<?> resolveGenericClass(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            if (actualTypes.length > 0 && actualTypes[0] instanceof Class) {
                return (Class<?>) actualTypes[0];
            }
        }
        return null;
    }
}