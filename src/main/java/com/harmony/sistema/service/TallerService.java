package com.harmony.sistema.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.model.Taller;
import com.harmony.sistema.repository.TallerRepository;

@Service
public class TallerService {
    @Autowired
    private TallerRepository tallerRepository;

    // ✅ CORREGIDO: Usa query con FETCH JOIN
    public List<Taller> encontrarTalleresActivos() {
        System.out.println("🔍 [TALLER SERVICE] Buscando todos los talleres activos.");
        List<Taller> talleres = tallerRepository.findByActivoTrue();
        System.out.println("✅ [TALLER SERVICE] " + talleres.size() + " talleres activos encontrados");
        return talleres;
    }

    // ✅ CORREGIDO: Usa query con FETCH JOIN
    public List<Taller> listarTalleres() {
        System.out.println("🔍 [TALLER SERVICE] Listando todos los talleres (activos e inactivos).");
        List<Taller> talleres = tallerRepository.findAllWithHorariosAndProfesores();
        System.out.println("✅ [TALLER SERVICE] " + talleres.size() + " talleres encontrados");
        return talleres;
    }

    @Transactional
    public Taller crearTallerSolo(Taller taller) {
        System.out.println("🔵 [TALLER SERVICE] Iniciando creación de nuevo taller: " + taller.getNombre());
        
        if (taller.getNombre() == null || taller.getNombre().trim().isEmpty()) {
            System.out.println("❌ [TALLER SERVICE ERROR] Nombre del taller vacío");
            throw new RuntimeException("El nombre del taller es obligatorio");
        }
        
        if (taller.getDescripcion() == null || taller.getDescripcion().trim().isEmpty()) {
            System.out.println("❌ [TALLER SERVICE ERROR] Descripción del taller vacía");
            throw new RuntimeException("La descripción del taller es obligatoria");
        }
        
        taller.setActivo(true);
        
        if (taller.getPrecio() == null) {
            System.out.println("⚠️ [TALLER SERVICE] Precio null, asignando 0");
            taller.setPrecio(BigDecimal.ZERO);
        }
        
        if (taller.getDuracionSemanas() == null || taller.getDuracionSemanas() <= 0) {
            System.out.println("⚠️ [TALLER SERVICE] Duración semanas inválida, asignando 12");
            taller.setDuracionSemanas(12);
        }
        
        if (taller.getClasesPorSemana() == null || taller.getClasesPorSemana() <= 0) {
            System.out.println("⚠️ [TALLER SERVICE] Clases por semana inválidas, asignando 2");
            taller.setClasesPorSemana(2);
        }
        
        System.out.println("✔️ [TALLER SERVICE] Taller marcado como activo");
        System.out.println("💰 [TALLER SERVICE] Precio: " + taller.getPrecio());
        System.out.println("📅 [TALLER SERVICE] Duración: " + taller.getDuracionSemanas() + " semanas");
        System.out.println("📚 [TALLER SERVICE] Clases/semana: " + taller.getClasesPorSemana());
        System.out.println("🖼️ [TALLER SERVICE] Imagen Taller: " + taller.getImagenTaller());
        System.out.println("🖼️ [TALLER SERVICE] Imagen Inicio: " + taller.getImagenInicio());
        
        Taller nuevoTaller = tallerRepository.save(taller);
        System.out.println("✅ [TALLER SERVICE SUCCESS] Taller creado y guardado con ID: " + nuevoTaller.getId());
        return nuevoTaller;
    }

    @Transactional
    public Taller editarTaller(Taller tallerActualizado) { 
        System.out.println("🔵 [TALLER SERVICE] Iniciando edición de taller con ID: " + tallerActualizado.getId());
        
        Optional<Taller> tallerOpt = tallerRepository.findById(tallerActualizado.getId());
        
        if (tallerOpt.isEmpty()) {
            System.out.println("❌ [TALLER SERVICE ERROR] Taller no encontrado con ID: " + tallerActualizado.getId());
            throw new RuntimeException("Taller con ID " + tallerActualizado.getId() + " no encontrado.");
        }
        
        Taller tallerExistente = tallerOpt.get();
        System.out.println("✔️ [TALLER SERVICE] Taller existente encontrado: " + tallerExistente.getNombre());
        
        if (tallerActualizado.getNombre() != null && !tallerActualizado.getNombre().trim().isEmpty()) {
            tallerExistente.setNombre(tallerActualizado.getNombre());
        }
        
        if (tallerActualizado.getDescripcion() != null && !tallerActualizado.getDescripcion().trim().isEmpty()) {
            tallerExistente.setDescripcion(tallerActualizado.getDescripcion());
        }
        
        if (tallerActualizado.getDuracionSemanas() != null && tallerActualizado.getDuracionSemanas() > 0) {
            tallerExistente.setDuracionSemanas(tallerActualizado.getDuracionSemanas());
        }
        
        if (tallerActualizado.getClasesPorSemana() != null && tallerActualizado.getClasesPorSemana() > 0) {
            tallerExistente.setClasesPorSemana(tallerActualizado.getClasesPorSemana());
        }
        
        if (tallerActualizado.getImagenTaller() != null) {
            tallerExistente.setImagenTaller(tallerActualizado.getImagenTaller());
        }
        
        if (tallerActualizado.getImagenInicio() != null) {
            tallerExistente.setImagenInicio(tallerActualizado.getImagenInicio());
        }
        
        if (tallerActualizado.getPrecio() != null) {
            tallerExistente.setPrecio(tallerActualizado.getPrecio());
        }
        
        tallerExistente.setActivo(tallerActualizado.isActivo());
        
        if (tallerActualizado.getTemas() != null) {
            tallerExistente.setTemas(tallerActualizado.getTemas());
        }
        
        System.out.println("📝 [TALLER SERVICE] Campos del taller actualizados");
        System.out.println("🔍 [TALLER SERVICE] Activo: " + tallerActualizado.isActivo());

        Taller updatedTaller = tallerRepository.save(tallerExistente);
        System.out.println("✅ [TALLER SERVICE SUCCESS] Taller ID " + updatedTaller.getId() + " actualizado y guardado.");
        return updatedTaller;
    }
    
    // ✅ CORREGIDO: Asegurar carga de horarios
    public Taller obtenerTallerPorId(Long tallerId) {
        System.out.println("🔍 [TALLER SERVICE] Buscando taller por ID: " + tallerId);
        Taller taller = tallerRepository.findById(tallerId)
            .orElseThrow(() -> {
                System.out.println("❌ [TALLER SERVICE ERROR] Taller con ID " + tallerId + " no encontrado.");
                return new RuntimeException("Taller con ID " + tallerId + " no encontrado.");
            });
        
        // Forzar carga de horarios
        if (taller.getHorarios() != null) {
            taller.getHorarios().size();
        }
        
        System.out.println("✅ [TALLER SERVICE] Taller encontrado: " + taller.getNombre());
        return taller;
    }
    
    @Transactional
    public void eliminarTaller(Long tallerId) {
        System.out.println("🔵 [TALLER SERVICE] Iniciando eliminación de taller con ID: " + tallerId);
        
        if (!tallerRepository.existsById(tallerId)) {
            System.out.println("❌ [TALLER SERVICE ERROR] Taller no encontrado con ID: " + tallerId);
            throw new RuntimeException("Taller con ID " + tallerId + " no encontrado.");
        }
        
        tallerRepository.deleteById(tallerId);
        System.out.println("✅ [TALLER SERVICE SUCCESS] Taller ID " + tallerId + " eliminado exitosamente.");
    }
}