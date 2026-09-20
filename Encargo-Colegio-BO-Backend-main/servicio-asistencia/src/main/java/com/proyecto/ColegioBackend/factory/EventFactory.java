package com.proyecto.ColegioBackend.factory;

public interface EventFactory<T, E> {
    E buildEvent(T sourceEntity);
}
