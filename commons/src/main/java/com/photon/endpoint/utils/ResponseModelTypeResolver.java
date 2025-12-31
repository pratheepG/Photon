package com.photon.endpoint.utils;

import com.photon.dto.ApiResponseDto;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ResponseModelTypeResolver {

    @Getter
    public static class Result {
        private final Class<?> modelClass;
        private final boolean isCollection;
        private final int collectionDepth;

        public Result(Class<?> modelClass, boolean isCollection, int collectionDepth) {
            this.modelClass = modelClass;
            this.isCollection = isCollection;
            this.collectionDepth = collectionDepth;
        }

    }

    public static Result resolve(Type returnType) {
        AtomicInteger collectionDepth = new AtomicInteger(0);
        Class<?> modelClass = unwrap(returnType, collectionDepth).orElse(null);

        return new Result(modelClass, collectionDepth.get() > 0, collectionDepth.get());
    }

    private static Optional<Class<?>> unwrap(Type type, AtomicInteger collectionDepth) {
        if (type instanceof ParameterizedType pt) {
            Type raw = pt.getRawType();

            if (raw instanceof Class<?> rawClass) {
                if (isWrapper(rawClass)) {
                    return unwrap(pt.getActualTypeArguments()[0], collectionDepth);
                }
                if (Collection.class.isAssignableFrom(rawClass)) {
                    collectionDepth.incrementAndGet();
                    return unwrap(pt.getActualTypeArguments()[0], collectionDepth);
                }
                return Optional.of(rawClass);
            }
        } else if (type instanceof Class<?> clazz) {
            return Optional.of(clazz);
        }

        return Optional.empty();
    }

    private static boolean isWrapper(Class<?> clazz) {
        return clazz == Mono.class ||
                clazz == Flux.class ||
                clazz == Optional.class ||
                clazz == ResponseEntity.class ||
                clazz == ApiResponseDto.class;
    }
}