package cl.duoc.huertoapi.model;

import cl.duoc.huertoapi.model.enums.EstadoCultivo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cultivos")
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cultivo")
    private Long id;

    @Column(nullable = false, length = 40)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    /** Sector al que pertenece (obligatorio). */
    @Column(nullable = false)
    private Long sectorId;

    /** Fechas en milisegundos (epoch); pueden ser null si aún no ocurren. */
    private Long fechaSiembra;
    private Long fechaTrasplante;
    private Long fechaCosechaEstimada;

    @Column(nullable = false)
    private int frecuenciaRiegoDias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoCultivo estado = EstadoCultivo.PLANIFICADO;

    @Column(length = 300)
    private String observaciones;
}
