package cl.duoc.huertoapi.dto;

import cl.duoc.huertoapi.model.enums.EstadoTarea;

/** Cuerpo del PATCH /tareas/{id}/estado: { "estado": "VALIDADA" } */
public record CambioEstadoTareaRequest(EstadoTarea estado) {
}
