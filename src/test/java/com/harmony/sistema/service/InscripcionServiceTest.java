package com.harmony.sistema.service;

import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Inscripcion;
import com.harmony.sistema.repository.InscripcionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test unitario simple para InscripcionService
@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private ServicioVacantes servicioVacantes;

    @InjectMocks
    private InscripcionService inscripcionService;

    @Test
    void testCrearInscripcion() {
        // Crear datos de prueba
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombreCompleto("Test Cliente")
                .correo("test@test.com")
                .telefono("123456789")
                .build();

        Horario horario = new Horario();
        horario.setId(1L);
        horario.setVacantesDisponibles(10);

        // Configurar mocks
        when(inscripcionRepository.findByClienteIdAndHorarioId(1L, 1L)).thenReturn(Optional.empty());

        // Ejecutar el método
        inscripcionService.crearInscripcion(cliente, horario);

        // Verificar que funciona
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
        verify(servicioVacantes, times(1)).reservarVacante(horario);
    }
}
