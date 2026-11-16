package cl.duoc.huertoapi.config;

import cl.duoc.huertoapi.model.*;
import cl.duoc.huertoapi.model.enums.*;
import cl.duoc.huertoapi.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Datos ficticios iguales a los de la app (DatosDemo.kt).
 * Se cargan solo si la base de datos está vacía.
 */
@Configuration
public class DataInitializer {

    private static final long DIA = 86_400_000L;

    @Bean
    CommandLineRunner cargarDatosDemo(UsuarioRepository usuarioRepository,
                                      SectorRepository sectorRepository,
                                      CultivoRepository cultivoRepository,
                                      TareaRepository tareaRepository,
                                      ProblemaRepository problemaRepository) {
        return args -> {
            if (usuarioRepository.count() > 0) return;

            long hoy = System.currentTimeMillis();

            // ---------- Usuarios: (id, nombre, clave, perfil, grupo, activo) ----------
            usuarioRepository.saveAll(List.of(
                    new Usuario("ADM-01", "Docente Prueba", "1234", Perfil.ADMINISTRADOR, null, true),
                    new Usuario("EST-01", "Estudiante Uno", "1234", Perfil.ESTUDIANTE, "Taller A", true),
                    new Usuario("EST-02", "Estudiante Dos", "1234", Perfil.ESTUDIANTE, "Taller A", true),
                    new Usuario("EST-03", "Estudiante Tres", "1234", Perfil.ESTUDIANTE, "Taller B", true)
            ));

            // ---------- Sectores: (id, codigo, nombre, descripcion, ubicacion, estado, responsableId, fechaCreacion, fechaActualizacion) ----------
            Sector norte = sectorRepository.save(new Sector(null, "S-01", "Sector Norte", "Hortalizas de hoja",
                    "Entrada del huerto", EstadoSector.EN_USO, "ADM-01", hoy, hoy));
            Sector sur = sectorRepository.save(new Sector(null, "S-02", "Sector Sur", "Camas elevadas",
                    "Junto al invernadero", EstadoSector.DISPONIBLE, "ADM-01", hoy, hoy));
            Sector invernadero = sectorRepository.save(new Sector(null, "S-03", "Invernadero", "Tomates y aromáticas",
                    "Patio trasero", EstadoSector.EN_USO, "ADM-01", hoy, hoy));
            Sector compostera = sectorRepository.save(new Sector(null, "S-04", "Compostera", "Compost y lombricultura",
                    "Al fondo, junto a la bodega", EstadoSector.EN_MANTENIMIENTO, "ADM-01", hoy, hoy));

            // ---------- Cultivos: (id, nombre, descripcion, sectorId, siembra, trasplante, cosecha, riegoDias, estado, observaciones) ----------
            Cultivo lechuga = cultivoRepository.save(new Cultivo(null, "Lechuga", "Variedad escarola", norte.getId(),
                    hoy - 20 * DIA, null, hoy + 5 * DIA, 2, EstadoCultivo.EN_CRECIMIENTO, "Hojas firmes, buen color"));
            Cultivo rabanito = cultivoRepository.save(new Cultivo(null, "Rabanito", "Rabanito rojo", norte.getId(),
                    hoy - 28 * DIA, null, hoy - DIA, 2, EstadoCultivo.LISTO_PARA_COSECHAR, ""));
            Cultivo tomate = cultivoRepository.save(new Cultivo(null, "Tomate cherry", "Tutorado con cañas", invernadero.getId(),
                    hoy - 40 * DIA, hoy - 25 * DIA, hoy + 30 * DIA, 3, EstadoCultivo.EN_CRECIMIENTO, ""));
            Cultivo albahaca = cultivoRepository.save(new Cultivo(null, "Albahaca", "Albahaca genovesa", invernadero.getId(),
                    hoy - 10 * DIA, null, hoy + 45 * DIA, 1, EstadoCultivo.SEMBRADO, ""));
            cultivoRepository.save(new Cultivo(null, "Zanahoria", "Para el próximo ciclo", sur.getId(),
                    null, null, null, 3, EstadoCultivo.PLANIFICADO, ""));

            // ---------- Tareas: (id, tipo, descripcion, sectorId, cultivoId, creacion, programada, asignadoId, grupo, creadaPorId, estado) ----------
            tareaRepository.saveAll(List.of(
                    new Tarea(null, TipoTarea.RIEGO, "Regar las lechugas con regadera, sin mojar las hojas.",
                            norte.getId(), lechuga.getId(), hoy - 3 * DIA, hoy + DIA, "EST-01", null, "ADM-01", EstadoTarea.PENDIENTE),
                    new Tarea(null, TipoTarea.OBSERVACION, "Observar las hojas del tomate y anotar si hay manchas o insectos.",
                            invernadero.getId(), tomate.getId(), hoy - 6 * DIA, hoy - 2 * DIA, "EST-01", null, "ADM-01", EstadoTarea.PENDIENTE),
                    new Tarea(null, TipoTarea.LIMPIEZA, "Retirar hojas secas y basura del pasillo del sector sur.",
                            sur.getId(), null, hoy - 4 * DIA, hoy, "EST-02", null, "ADM-01", EstadoTarea.EN_DESARROLLO),
                    new Tarea(null, TipoTarea.RIEGO, "Regar la albahaca a primera hora.",
                            invernadero.getId(), albahaca.getId(), hoy - 3 * DIA, hoy - DIA, "EST-01", null, "ADM-01", EstadoTarea.PENDIENTE_DE_VALIDACION),
                    new Tarea(null, TipoTarea.SIEMBRA, "Sembrar albahaca en la cama 2, a 20 cm entre plantas.",
                            invernadero.getId(), albahaca.getId(), hoy - 12 * DIA, hoy - 10 * DIA, "EST-02", null, "ADM-01", EstadoTarea.VALIDADA),
                    new Tarea(null, TipoTarea.REGISTRO_CRECIMIENTO, "Medir la altura de 3 plantas de tomate y anotar cada medida.",
                            invernadero.getId(), tomate.getId(), hoy - 6 * DIA, hoy - 3 * DIA, "EST-03", null, "ADM-01", EstadoTarea.RECHAZADA),
                    new Tarea(null, TipoTarea.MANTENIMIENTO, "Reparar el borde de madera de la compostera.",
                            compostera.getId(), null, hoy - 2 * DIA, hoy + 3 * DIA, null, "Taller A", "ADM-01", EstadoTarea.PENDIENTE),
                    new Tarea(null, TipoTarea.COSECHA, "Cosechar los rabanitos que estén listos y llevarlos a la cocina.",
                            norte.getId(), rabanito.getId(), hoy - DIA, hoy + 2 * DIA, "EST-03", null, "ADM-01", EstadoTarea.PENDIENTE)
            ));

            // ---------- Problemas: (id, tipo, descripcion, sectorId, cultivoId, reportadoPorId, fechaReporte, estado, comentario, atendidoPor, fechaAtencion) ----------
            problemaRepository.saveAll(List.of(
                    new Problema(null, TipoProblema.FALTA_AGUA, "Las lechugas del borde se ven marchitas y la tierra está muy seca.",
                            norte.getId(), lechuga.getId(), "EST-01", hoy - DIA, EstadoProblema.INFORMADO, null, null, null),
                    new Problema(null, TipoProblema.PLAGA_SIMULADA, "Se observan puntos blancos bajo las hojas del tomate (caso de práctica).",
                            invernadero.getId(), tomate.getId(), "EST-03", hoy - 3 * DIA, EstadoProblema.EN_REVISION,
                            "Lo revisaremos el jueves en clase.", "ADM-01", hoy - 2 * DIA),
                    new Problema(null, TipoProblema.DETERIORO_SECTOR, "Una tabla del borde de la compostera está suelta.",
                            compostera.getId(), null, "EST-02", hoy - 5 * DIA, EstadoProblema.ATENDIDO,
                            "Se creó una tarea de mantenimiento para repararla.", "ADM-01", hoy - 2 * DIA)
            ));
        };
    }
}
