package com.project.deporturnos.service.implementation;

import com.project.deporturnos.entity.domain.Turno;
import com.project.deporturnos.entity.domain.TurnoState;
import com.project.deporturnos.repository.ITurnoRepository;
import com.project.deporturnos.service.IReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportsService implements IReportsService {

    private final ITurnoRepository turnoRepository;

    public BigDecimal calcularGananciasGenerales(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Turno> turnosCompletados = turnoRepository.findAllByEstadoAndDeletedFalseAndFechaBetween(
                TurnoState.COMPLETADO, fechaDesde, fechaHasta);

        BigDecimal gananciasTotales = BigDecimal.ZERO;

        for (Turno turno : turnosCompletados) {
            long duracionEnMinutos = ChronoUnit.MINUTES.between(turno.getHoraInicio(), turno.getHoraFin());

            BigDecimal duracionEnHoras = BigDecimal.valueOf(duracionEnMinutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            BigDecimal gananciaPorTurno = duracionEnHoras.multiply(turno.getCancha().getPrecioHora());

            gananciasTotales = gananciasTotales.add(gananciaPorTurno);
        }

        return gananciasTotales;
    }

}
