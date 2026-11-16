package cl.duoc.huertoapi.model;

import cl.duoc.huertoapi.model.enums.EstadoTarea;
import cl.duoc.huertoapi.model.enums.TipoTarea;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarea")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoTarea tipo;

    /** Instrucciones de la tarea. */
    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(nullable = false)
    private Long sectorId;

    /** Opcional: no todas las tareas son de un cultivo. */
    private Long cultivoId;

    private Long fechaCreacion;

    @Column(nullable = false)
    private Long fechaProgramada;

    /** Se asigna a un estudiante (asignadoId) o a un grupo completo (grupo). */
    @Column(length = 10)
    private String asignadoId;

    @Column(length = 40)
    private String grupo;

    @Column(nullable = false, length = 10)
    private String creadaPorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoTarea estado = EstadoTarea.PENDIENTE;
}
