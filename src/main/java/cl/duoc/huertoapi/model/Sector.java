package cl.duoc.huertoapi.model;

import cl.duoc.huertoapi.model.enums.EstadoSector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sectores")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sector")
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 40)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(length = 80)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSector estado = EstadoSector.DISPONIBLE;

    /** Código del docente responsable (ej: "ADM-01"). */
    @Column(length = 10)
    private String responsableId;

    /** Fechas en milisegundos (epoch), igual que en la app Android. */
    private Long fechaCreacion;
    private Long fechaActualizacion;
}
