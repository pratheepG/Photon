package com.photon.utils;

import java.lang.reflect.Field;

public class EntityUpdater {

    public static <T> T updateFields(T existingEntity, T newEntity, String... fieldsToSkip) throws IllegalAccessException {
        Class<?> clazz = existingEntity.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object newValue = field.get(newEntity);

            boolean skipField = false;
            for (String fieldName : fieldsToSkip) {
                if (field.getName().equals(fieldName)) {
                    skipField = true;
                    break;
                }
            }

            if (!skipField && newValue != null) {
                field.set(existingEntity, newValue);
            }
        }
        return existingEntity;
    }

    public static <T, D> T updateEntityFromDto(T entity, D dto) throws IllegalAccessException {
        Class<?> entityClass = entity.getClass();
        Class<?> dtoClass = dto.getClass();

        for (Field dtoField : dtoClass.getDeclaredFields()) {
            dtoField.setAccessible(true);
            Object newValue = dtoField.get(dto);
            if (newValue != null) {
                try {
                    Field entityField = entityClass.getDeclaredField(dtoField.getName());
                    entityField.setAccessible(true);
                    entityField.set(entity, newValue);
                } catch (NoSuchFieldException ignored) {
                    // Ignore if the DTO has a field that doesn't exist in the entity
                }
            }
        }
        return entity;
    }
}