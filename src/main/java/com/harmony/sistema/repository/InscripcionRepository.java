package com.harmony.sistema.repository;

import com.harmony.sistema.model.Inscripcion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    Optional<Inscripcion> findByClienteIdAndHorarioId(Long clienteId, Long horarioId);

    java.util.List<Inscripcion> findByClienteId(Long clienteId);
}
