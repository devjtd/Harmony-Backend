/*package com.harmony.sistema.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.harmony.sistema.model.Taller;

public interface TallerRepository extends JpaRepository<Taller, Long> {

    

    // Obtiene una lista de todos los talleres cuyo campo 'activo' es verdadero.
    List<Taller> findByActivoTrue();

    
    
}*/
package com.harmony.sistema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.harmony.sistema.model.Taller;

@Repository
public interface TallerRepository extends JpaRepository<Taller, Long> {
    // Busca un taller por su nombre.
    Optional<Taller> findByNombre(String nombre);

    // ✅ CORREGIDO: Agregar FETCH JOIN para cargar horarios y profesores
    @Query("SELECT DISTINCT t FROM Taller t " +
           "LEFT JOIN FETCH t.horarios h " +
           "LEFT JOIN FETCH h.profesor " +
           "WHERE t.activo = true")
    List<Taller> findByActivoTrue();
    
    // ✅ OPCIONAL: Para listarTalleres() también con fetch
    @Query("SELECT DISTINCT t FROM Taller t " +
           "LEFT JOIN FETCH t.horarios h " +
           "LEFT JOIN FETCH h.profesor")
    List<Taller> findAllWithHorariosAndProfesores();
}