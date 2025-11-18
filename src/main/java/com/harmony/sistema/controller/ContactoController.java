package com.harmony.sistema.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.harmony.sistema.dto.ContactoFormDTO;
import com.harmony.sistema.service.EmailService;

@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class ContactoController {

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.username}")
    private String adminEmail;

    @GetMapping("/contacto")
    public String contacto() {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /contacto. Retornando vista 'contacto'.");
        return "contacto";
    }

    // Endpoint REST para Angular
    @PostMapping("/contacto/enviar")
    @ResponseBody
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> enviarContactoRest(@RequestBody ContactoFormDTO form) {
        System.out.println(" [REST REQUEST] POST a /contacto/enviar (API REST)");
        System.out.println(" [CONTACTO] Procesando mensaje de: " + form.getCorreo());
        System.out.println(" [CONTACTO] Nombre: " + form.getNombre());
        System.out.println(" [CONTACTO] Asunto: " + form.getAsunto());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String subject = "Consulta de Contacto - " + form.getAsunto() + " (Harmony)";
            System.out.println(" [EMAIL] Preparando envío de correo");
            System.out.println(" [EMAIL] Destinatario (Admin): " + adminEmail);
            System.out.println(" [EMAIL] Asunto: " + subject);
            
            String body = String.format(
                """
                ¡Has recibido un nuevo mensaje de contacto!

                Nombre: %s
                Correo del cliente: %s
                Asunto Seleccionado: %s

                Mensaje:
                %s
                """,
                form.getNombre(), form.getCorreo(), form.getAsunto(), form.getMensaje()
            );
            
            emailService.enviarCorreo(adminEmail, subject, body);
            System.out.println(" [EMAIL SUCCESS] Correo de contacto enviado exitosamente a: " + adminEmail);

            response.put("success", true);
            response.put("message", "✅ ¡Mensaje enviado! Recibimos tu consulta y te responderemos a la brevedad.");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" [EMAIL ERROR] Ocurrió un error al intentar enviar correo de contacto");
            System.err.println(" [EMAIL ERROR] Remitente: " + form.getCorreo());
            System.err.println(" [EMAIL ERROR] Detalle: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "❌ Ocurrió un error al intentar enviar tu mensaje. Por favor, verifica tu información o intenta más tarde.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Mantener endpoint Thymeleaf para compatibilidad
    @PostMapping("/contacto/enviar-thymeleaf")
    public String enviarContacto(@ModelAttribute ContactoFormDTO form, RedirectAttributes redirectAttributes) {
        System.out.println(" [REQUEST] Mapeando solicitud POST a /contacto/enviar-thymeleaf (Thymeleaf)");
        System.out.println(" [CONTACTO] Procesando mensaje de: " + form.getCorreo());
        
        try {
            String subject = "Consulta de Contacto - " + form.getAsunto() + " (Harmony)";
            System.out.println(" [EMAIL] Asunto del correo a enviar a Admin: " + subject);
            
            String body = String.format(
                """
                ¡Has recibido un nuevo mensaje de contacto!

                Nombre: %s
                Correo del cliente: %s
                Asunto Seleccionado: %s

                Mensaje:
                %s
                """,
                form.getNombre(), form.getCorreo(), form.getAsunto(), form.getMensaje()
            );
            
            emailService.enviarCorreo(adminEmail, subject, body);
            System.out.println(" [EMAIL SUCCESS] Correo de contacto enviado exitosamente a: " + adminEmail);

            redirectAttributes.addFlashAttribute("success", "✅ ¡Mensaje enviado! Recibimos tu consulta y te responderemos a la brevedad.");

        } catch (Exception e) {
            System.err.println(" [EMAIL ERROR] Ocurrió un error al intentar enviar correo de contacto de: " + form.getCorreo());
            System.err.println(" [EMAIL ERROR] Detalle: " + e.getMessage());
            
            redirectAttributes.addFlashAttribute("error", "❌ Ocurrió un error al intentar enviar tu mensaje. Por favor, verifica tu información o intenta más tarde.");
        }

        System.out.println(" [REDIRECT] Finalizado el procesamiento de contacto. Redirigiendo a /contacto.");
        return "redirect:/contacto";
    }
}