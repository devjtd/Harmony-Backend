package com.harmony.sistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.repository.HorarioRepository;

/**
 * Servicio especializado para la gestión de vacantes en horarios.
 * Responsabilidad única: Reservar y liberar vacantes.
 */
@Service
public class ServicioVacantes {

    @Autowired
    private HorarioRepository horarioRepository;

    /**
     * Reserva una vacante en el horario especificado.
     * Decrementa el contador de vacantes disponibles.
     * 
     * @param horario El horario en el que se reservará la vacante
     * @throws RuntimeException si no hay vacantes disponibles
     */
    @Transactional
    public void reservarVacante(Horario horario) {
        System.out.println("[INFO] [VACANTES] Reservando vacante en horario ID: " + horario.getId());

        if (horario.getVacantesDisponibles() <= 0) {
            System.err.println(
                    "[ERROR] [VACANTES] No hay vacantes disponibles en horario ID: " + horario.getId());
            throw new RuntimeException(
                    "No hay vacantes disponibles en el horario seleccionado (ID: " + horario.getId() + ")");
        }

        horario.setVacantesDisponibles(horario.getVacantesDisponibles() - 1);
        horarioRepository.save(horario);

        System.out.println("[SUCCESS] [VACANTES] Vacante reservada. Vacantes restantes: "
                + horario.getVacantesDisponibles());
    }

    /**
     * Libera una vacante en el horario especificado.
     * Incrementa el contador de vacantes disponibles.
     * 
     * @param horario El horario en el que se liberará la vacante
     */
    @Transactional
    public void liberarVacante(Horario horario) {
        System.out.println("[INFO] [VACANTES] Liberando vacante en horario ID: " + horario.getId());

        horario.setVacantesDisponibles(horario.getVacantesDisponibles() + 1);
        horarioRepository.save(horario);

        System.out.println("[SUCCESS] [VACANTES] Vacante liberada. Vacantes disponibles: "
                + horario.getVacantesDisponibles());
    }

    /**
     * Valida si hay vacantes disponibles en el horario.
     * 
     * @param horario El horario a validar
     * @return true si hay vacantes disponibles, false en caso contrario
     */
    public boolean hayVacantesDisponibles(Horario horario) {
        return horario.getVacantesDisponibles() > 0;
    }
}
