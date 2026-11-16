package cl.duoc.huertoapi.model;

import cl.duoc.huertoapi.model.enums.Perfil;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {

    /** Código del usuario, igual que en la app: "ADM-01", "EST-01"... */
    @Id
    @Column(name = "id_usuario", length = 10)
    private String id;

    @Column(nullable = false, length = 60)
    private String nombre;

    /** Solo se recibe (registro/login); nunca se devuelve en las respuestas. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 60)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    /** Taller o grupo del estudiante (null para el docente). */
    @Column(length = 40)
    private String grupo;

    @Column(nullable = false)
    private boolean activo = true;
}
