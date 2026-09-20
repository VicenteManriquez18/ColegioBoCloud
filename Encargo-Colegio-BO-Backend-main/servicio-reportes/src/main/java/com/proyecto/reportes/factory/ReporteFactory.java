package com.proyecto.reportes.factory;

import com.proyecto.reportes.entity.ReporteEntity;

public interface ReporteFactory<T, E extends ReporteEntity> {
    E buildReportEntity(T event);
}
