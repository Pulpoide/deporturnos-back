package com.project.deporturnos.scheduler;

import com.project.deporturnos.entity.domain.Reserva;
import com.project.deporturnos.entity.domain.ReservaState;
import com.project.deporturnos.repository.IReservaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaSchedulerService {

    private final IReservaRepository reservaRepository;

    public ReservaSchedulerService(IReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    // Scheduler que corre cada minuto
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void completarReservasEnProceso() {
        LocalTime ahora = LocalTime.now();
        List<Reserva> reservasEnProceso = reservaRepository.findByEstado(ReservaState.EN_PROCESO);

        for (Reserva reserva : reservasEnProceso) {
            if (reserva.getTurno().getHoraFin().isBefore(ahora)) {
                reserva.setEstado(ReservaState.COMPLETADA);
                reserva.getTurno().setDeleted(true);
                reservaRepository.save(reserva);
            }
        }
    }
}