package cl.duoc.huertoapi.dto;

import cl.duoc.huertoapi.model.enums.EstadoProblema;

/**
 * Cuerpo del PATCH /problemas/{id}/estado:
 * { "estado": "EN_REVISION", "comentarioDocente": "Lo vemos el jueves", "atendidoPorId": "ADM-01" }
 */
public record AtencionProblemaRequest(EstadoProblema estado, String comentarioDocente, String atendidoPorId) {
}
