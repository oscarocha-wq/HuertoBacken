package cl.duoc.huertoapi.dto;

/** Cuerpo del POST /usuarios/login: { "id": "ADM-01", "clave": "1234" } */
public record LoginRequest(String id, String clave) {
}
