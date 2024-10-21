package com.project.deporturnos.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IReportsService {

    BigDecimal calcularGananciasGenerales(LocalDate fechaDesde, LocalDate fechaHasta);
}
