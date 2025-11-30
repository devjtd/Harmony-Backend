package com.harmony.sistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Crea y envía un correo electrónico simple utilizando el JavaMailSender.
    public void enviarCorreo(String para, String asunto, String cuerpo) {
        System.out.println("[INFO] [EMAIL] Iniciando proceso de envío de correo.");
        // 1. Crea un nuevo objeto SimpleMailMessage.
        SimpleMailMessage mensaje = new SimpleMailMessage();
        System.out.println("[INFO] [EMAIL] Objeto SimpleMailMessage creado.");
        // 2. Establece el destinatario.
        mensaje.setTo(para);
        System.out.println("[INFO] [EMAIL] Destinatario establecido: " + para);
        // 3. Establece el asunto.
        mensaje.setSubject(asunto);
        System.out.println("[INFO] [EMAIL] Asunto establecido: " + asunto);
        // 4. Establece el cuerpo del mensaje.
        mensaje.setText(cuerpo);
        // 5. Envía el mensaje utilizando el JavaMailSender.
        try {
            mailSender.send(mensaje);
            System.out.println("[SUCCESS] [EMAIL] Correo enviado exitosamente a: " + para);
        } catch (MailException e) {
            System.err.println("[ERROR] [EMAIL] Fallo al enviar correo a: " + para + ". Error: " + e.getMessage());
        }
    }
}