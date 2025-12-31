package com.photon.endpoint.dto.mapper;

import com.photon.endpoint.dto.ModelFieldDto;
import com.photon.endpoint.enums.BaseType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeMapper {

    public static ModelFieldDto resolveField(Field field) {
        Class<?> rawType = field.getType();
        BaseType baseType;
        String reference = null;

        if (rawType == String.class) baseType = BaseType.STRING;
        else if (rawType == int.class || rawType == Integer.class) baseType = BaseType.INTEGER;
        else if (rawType == long.class || rawType == Long.class) baseType = BaseType.LONG;
        else if (rawType == boolean.class || rawType == Boolean.class) baseType = BaseType.BOOLEAN;
        else if (rawType == float.class || rawType == Float.class) baseType = BaseType.FLOAT;
        else if (rawType == double.class || rawType == Double.class) baseType = BaseType.DOUBLE;
        else if (Date.class.isAssignableFrom(rawType)) baseType = BaseType.DATE;
        else if (List.class.isAssignableFrom(rawType) || Set.class.isAssignableFrom(rawType)) {
            baseType = BaseType.LIST;
            Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            reference = genericType.getTypeName();
        }
        else if (Map.class.isAssignableFrom(rawType)) {
            baseType = BaseType.MAP;
        } else {
            baseType = BaseType.OBJECT;
            reference = rawType.getCanonicalName();
        }

        ModelFieldDto dto = new ModelFieldDto();
        dto.setName(field.getName());
        dto.setType(baseType);
        dto.setReferenceType(reference);
        return dto;
    }
}