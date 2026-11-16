package cl.duoc.huertoapi.model;

import cl.duoc.huertoapi.model.enums.EstadoProblema;
import cl.duoc.huertoapi.model.enums.TipoProblema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "problemas")
public class Problema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_problema")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoProblema tipo;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(nullable = false)
    private Long sectorId;

    private Long cultivoId;

    @Column(nullable = false, length = 10)
    private String reportadoPorId;

    private Long fechaReporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProblema estado = EstadoProblema.INFORMADO;

    /** Seguimiento del docente. */
    @Column(length = 300)
    private String comentarioDocente;

    @Column(length = 10)
    private String atendidoPorId;

    private Long fechaAtencion;
}
