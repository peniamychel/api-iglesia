package com.mcmm.controller;

import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.entity.ParticipacionEvento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
public class CertificadoVerificationController {

    @Autowired
    private ParticipacionEventoDao participacionEventoDao;

    @GetMapping(value = "/verificar-certificado/{codigoUnico}", produces = MediaType.TEXT_HTML_VALUE)
    public String verificarCertificado(@PathVariable("codigoUnico") String codigoUnico) {
        Optional<ParticipacionEvento> participacionOpt = participacionEventoDao.findByCodigoUnicoWithRelations(codigoUnico);

        String statusBadge;
        String detailsRows;

        if (participacionOpt.isPresent()) {
            ParticipacionEvento part = participacionOpt.get();
            boolean isEntregado = part.getEntregado() != null && part.getEntregado();

            if (isEntregado) {
                statusBadge = "<span class=\"status-badge\">" +
                        "<span class=\"status-icon\">✓</span> Certificado Entregado y Válido" +
                        "</span>";
            } else {
                statusBadge = "<span class=\"status-badge\" style=\"background: #fff9c4; color: #f57f17; border: 1px solid rgba(245, 127, 23, 0.2); box-shadow: 0 4px 10px rgba(245, 127, 23, 0.08);\">" +
                        "<span class=\"status-icon\">⚠</span> Registro Válido (Entrega Pendiente)" +
                        "</span>";
            }

            String miembroNombre = part.getMiembro() != null 
                    ? part.getMiembro().getNombre() + " " + part.getMiembro().getApellido() 
                    : "N/A";
            String iglesiaNombre = part.getEvento() != null && part.getEvento().getIglesia() != null 
                    ? part.getEvento().getIglesia().getNombre() 
                    : "N/A";
            String eventoNombre = part.getEvento() != null 
                    ? part.getEvento().getNombre() 
                    : "N/A";
            String tipoCertificado = part.getCertificado() != null && part.getCertificado().getTipoCertificado() != null 
                    ? part.getCertificado().getTipoCertificado().getNombre() 
                    : "Certificado de Participación";
            String motivo = part.getCertificado() != null 
                    ? part.getCertificado().getMotivoCertificado() 
                    : (part.getEvento() != null ? "Participación en el evento: " + part.getEvento().getNombre() : "N/A");

            String fechaEntregaStr = "No registrado";
            if (part.getFechaEntrega() != null) {
                fechaEntregaStr = part.getFechaEntrega().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            } else if (part.getFecha() != null) {
                fechaEntregaStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(part.getFecha());
            }

            detailsRows = "<div class=\"detail-row\">" +
                    "    <span class=\"detail-label\">Código de Verificación</span>" +
                    "    <span class=\"detail-value\" style=\"font-family: monospace; font-weight: bold; letter-spacing: 1px;\">" + part.getCodigoUnico() + "</span>" +
                    "</div>" +
                    "<div class=\"detail-row\">" +
                    "    <span class=\"detail-label\">Participante / Miembro</span>" +
                    "    <span class=\"detail-value\">" + miembroNombre + "</span>" +
                    "</div>" +
                    "<div class=\"detail-row\">" +
                    "    <span class=\"detail-label\">Iglesia</span>" +
                    "    <span class=\"detail-value\">" + iglesiaNombre + "</span>" +
                    "</div>" +
                    "<div class=\"detail-row\">" +
                    "    <span class=\"detail-label\">Evento</span>" +
                    "    <span class=\"detail-value\">" + eventoNombre + "</span>" +
                    "</div>" +
                    "<div class=\"detail-row\">" +
                    "    <span class=\"detail-label\">Tipo de Certificación</span>" +
                    "    <span class=\"detail-value\">" + tipoCertificado + "</span>" +
                    "</div>" +
                    "<div class=\"detail-row\">" +
                    "    <span class=\"detail-label\">Detalle / Motivo</span>" +
                    "    <span class=\"detail-value\">" + motivo + "</span>" +
                    "</div>" +
                    "<div class=\"detail-row\">" +
                    "    <span class=\"detail-label\">Fecha de Registro</span>" +
                    "    <span class=\"detail-value\">" + fechaEntregaStr + "</span>" +
                    "</div>";
        } else {
            statusBadge = "<span class=\"status-badge status-badge-error\">" +
                    "<span class=\"status-icon\">✗</span> Código no Válido" +
                    "</span>";

            detailsRows = "<div style=\"text-align: center; padding: 20px 0; color: #c62828;\">" +
                    "    <p style=\"font-weight: 600; margin-bottom: 8px;\">El código de verificación \"" + codigoUnico + "\" no existe en nuestro sistema.</p>" +
                    "    <p style=\"font-size: 14px; color: #718096;\">Por favor, escanee el código QR correcto o verifique el identificador.</p>" +
                    "</div>";
        }

        return getHtmlTemplate(statusBadge, detailsRows);
    }

    private String getHtmlTemplate(String statusBadge, String detailsRows) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Verificación de Certificado</title>\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700&display=swap\" rel=\"stylesheet\">\n" +
                "    <style>\n" +
                "        :root {\n" +
                "            --primary: #7F0B85;\n" +
                "            --success: #2e7d32;\n" +
                "            --success-bg: #e8f5e9;\n" +
                "            --text-dark: #2d3748;\n" +
                "            --text-muted: #718096;\n" +
                "            --bg: #f7fafc;\n" +
                "            --card-bg: #ffffff;\n" +
                "        }\n" +
                "        * {\n" +
                "            box-sizing: border-box;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        body {\n" +
                "            font-family: 'Outfit', sans-serif;\n" +
                "            background-color: var(--bg);\n" +
                "            color: var(--text-dark);\n" +
                "            min-height: 100vh;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .verification-container {\n" +
                "            width: 100%;\n" +
                "            max-width: 550px;\n" +
                "            background: var(--card-bg);\n" +
                "            border-radius: 20px;\n" +
                "            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);\n" +
                "            overflow: hidden;\n" +
                "            border: 1px solid rgba(0, 0, 0, 0.03);\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background: linear-gradient(135deg, var(--primary) 0%, #a21caf 100%);\n" +
                "            padding: 40px 20px;\n" +
                "            color: white;\n" +
                "            position: relative;\n" +
                "        }\n" +
                "        .header h1 {\n" +
                "            font-size: 24px;\n" +
                "            font-weight: 700;\n" +
                "            letter-spacing: -0.5px;\n" +
                "            margin-bottom: 5px;\n" +
                "        }\n" +
                "        .header p {\n" +
                "            font-size: 14px;\n" +
                "            opacity: 0.9;\n" +
                "        }\n" +
                "        .status-badge {\n" +
                "            display: inline-flex;\n" +
                "            align-items: center;\n" +
                "            gap: 8px;\n" +
                "            background: var(--success-bg);\n" +
                "            color: var(--success);\n" +
                "            padding: 10px 20px;\n" +
                "            border-radius: 50px;\n" +
                "            font-weight: 600;\n" +
                "            font-size: 15px;\n" +
                "            margin-top: 25px;\n" +
                "            border: 1px solid rgba(46, 125, 50, 0.2);\n" +
                "            box-shadow: 0 4px 10px rgba(46, 125, 50, 0.08);\n" +
                "        }\n" +
                "        .status-badge-error {\n" +
                "            background: #ffebee;\n" +
                "            color: #c62828;\n" +
                "            border: 1px solid rgba(198, 40, 40, 0.2);\n" +
                "            box-shadow: 0 4px 10px rgba(198, 40, 40, 0.08);\n" +
                "        }\n" +
                "        .status-icon {\n" +
                "            font-size: 18px;\n" +
                "            font-weight: bold;\n" +
                "        }\n" +
                "        .details-section {\n" +
                "            padding: 40px 30px;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "        .details-grid {\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            gap: 20px;\n" +
                "        }\n" +
                "        .detail-row {\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            gap: 4px;\n" +
                "            border-bottom: 1px solid #edf2f7;\n" +
                "            padding-bottom: 12px;\n" +
                "        }\n" +
                "        .detail-row:last-child {\n" +
                "            border-bottom: none;\n" +
                "            padding-bottom: 0;\n" +
                "        }\n" +
                "        .detail-label {\n" +
                "            font-size: 12px;\n" +
                "            font-weight: 600;\n" +
                "            text-transform: uppercase;\n" +
                "            letter-spacing: 0.5px;\n" +
                "            color: var(--text-muted);\n" +
                "        }\n" +
                "        .detail-value {\n" +
                "            font-size: 16px;\n" +
                "            font-weight: 500;\n" +
                "            color: var(--text-dark);\n" +
                "        }\n" +
                "        .footer {\n" +
                "            padding: 25px;\n" +
                "            background: #f8fafc;\n" +
                "            border-top: 1px solid #edf2f7;\n" +
                "            font-size: 13px;\n" +
                "            color: var(--text-muted);\n" +
                "        }\n" +
                "        .footer strong {\n" +
                "            color: var(--primary);\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"verification-container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Verificación de Certificación</h1>\n" +
                "            <p>Sistema de Gestión MCMM</p>\n" +
                "            " + statusBadge + "\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"details-section\">\n" +
                "            <div class=\"details-grid\">\n" +
                "                " + detailsRows + "\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"footer\">\n" +
                "            Este es un documento verificado por la <strong>Iglesia MCMM</strong>.\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
