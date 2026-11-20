package cl.duoc.huertoapi.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración de toda la API: levanta Spring Boot con H2 en memoria,
 * carga los datos de DataInitializer y llama a los endpoints como lo haría la app.
 * Cada prueba es @Transactional: al terminar se deshacen sus cambios.
 *
 * Datos de ejemplo: 4 usuarios, 4 sectores (S-01..S-04), 5 cultivos, 8 tareas, 3 problemas.
 */
@SpringBootTest
@Transactional
class ApiIntegracionTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private Number crear(String url, String json) throws Exception {
        String respuesta = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.id");
    }

    // ---------------- Usuarios ----------------

    @Test
    void usuariosListarYFiltrar() throws Exception {
        mvc.perform(get("/usuarios")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/usuarios").param("perfil", "ESTUDIANTE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void usuarioBuscarNoDevuelveLaClave() throws Exception {
        mvc.perform(get("/usuarios/EST-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Estudiante Uno"))
                .andExpect(jsonPath("$.clave").doesNotExist());
        mvc.perform(get("/usuarios/NO-EXISTE")).andExpect(status().isNotFound());
    }

    @Test
    void usuarioRegistrarActualizarYBorrar() throws Exception {
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"est-09\",\"nombre\":\"Nueve\",\"clave\":\"1234\",\"perfil\":\"ESTUDIANTE\",\"grupo\":\"Taller A\",\"activo\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("EST-09"));

        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"ADM-01\",\"nombre\":\"Repetido\",\"clave\":\"1\",\"perfil\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isConflict());

        mvc.perform(put("/usuarios/EST-09").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nueve Editado\",\"perfil\":\"ESTUDIANTE\",\"grupo\":\"Taller B\",\"activo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupo").value("Taller B"));

        mvc.perform(delete("/usuarios/EST-09")).andExpect(status().isNoContent());
    }

    @Test
    void login() throws Exception {
        mvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"adm-01\",\"clave\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMINISTRADOR"));

        mvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"ADM-01\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------- Sectores ----------------

    @Test
    void sectoresListarBuscarYCultivos() throws Exception {
        mvc.perform(get("/sectores")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/sectores/1")).andExpect(status().isOk()).andExpect(jsonPath("$.codigo").value("S-01"));
        mvc.perform(get("/sectores/999")).andExpect(status().isNotFound());
        mvc.perform(get("/sectores/1/cultivos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/sectores/999/cultivos")).andExpect(status().isNotFound());
    }

    @Test
    void sectorRegistrarActualizarYBorrar() throws Exception {
        Number id = crear("/sectores", "{\"codigo\":\"S-09\",\"nombre\":\"Nuevo\",\"estado\":\"DISPONIBLE\"}");

        mvc.perform(post("/sectores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"S-01\",\"nombre\":\"Repetido\",\"estado\":\"DISPONIBLE\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/sectores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"S-10\",\"nombre\":\"\",\"estado\":\"DISPONIBLE\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/sectores/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"S-09\",\"nombre\":\"Editado\",\"estado\":\"EN_USO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Editado"));

        mvc.perform(delete("/sectores/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/sectores/1")).andExpect(status().isConflict());
    }

    // ---------------- Cultivos ----------------

    @Test
    void cultivosListarYBuscar() throws Exception {
        mvc.perform(get("/cultivos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(5));
        mvc.perform(get("/cultivos").param("sectorId", "3"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/cultivos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Lechuga"));
        mvc.perform(get("/cultivos/999")).andExpect(status().isNotFound());
    }

    @Test
    void cultivoRegistrarActualizarYBorrar() throws Exception {
        Number id = crear("/cultivos",
                "{\"nombre\":\"Papa\",\"descripcion\":\"\",\"sectorId\":2,\"frecuenciaRiegoDias\":3,\"estado\":\"PLANIFICADO\"}");

        mvc.perform(post("/cultivos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Papa\",\"sectorId\":2,\"frecuenciaRiegoDias\":0}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/cultivos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Papa nativa\",\"sectorId\":2,\"frecuenciaRiegoDias\":4,\"estado\":\"SEMBRADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frecuenciaRiegoDias").value(4));

        mvc.perform(delete("/cultivos/" + id)).andExpect(status().isNoContent());
        // La lechuga (id 1) tiene tareas: no se puede borrar
        mvc.perform(delete("/cultivos/1")).andExpect(status().isConflict());
    }

    // ---------------- Tareas ----------------

    @Test
    void tareasListarConFiltros() throws Exception {
        mvc.perform(get("/tareas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(8));
        mvc.perform(get("/tareas").param("asignadoId", "EST-01"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/tareas").param("asignadoId", "EST-01").param("grupo", "Taller A"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/tareas").param("estado", "PENDIENTE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/tareas/1")).andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("RIEGO"));
        mvc.perform(get("/tareas/999")).andExpect(status().isNotFound());
    }

    @Test
    void tareaRegistrarActualizarCambiarEstadoYBorrar() throws Exception {
        String json = "{\"tipo\":\"RIEGO\",\"descripcion\":\"Regar\",\"sectorId\":1,\"fechaProgramada\":1000,"
                + "\"asignadoId\":\"EST-01\",\"creadaPorId\":\"ADM-01\",\"estado\":\"VALIDADA\"}";
        String respuesta = mvc.perform(post("/tareas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(respuesta, "$.id");

        mvc.perform(post("/tareas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"RIEGO\",\"descripcion\":\"Regar\",\"sectorId\":999,\"fechaProgramada\":1000,\"asignadoId\":\"EST-01\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/tareas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("Regar", "Regar temprano")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Regar temprano"));

        mvc.perform(delete("/tareas/" + id)).andExpect(status().isNoContent());

        mvc.perform(patch("/tareas/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_DESARROLLO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_DESARROLLO"));

        // La tarea 3 ya está EN_DESARROLLO: no se puede borrar
        mvc.perform(delete("/tareas/3")).andExpect(status().isConflict());
    }

    // ---------------- Problemas ----------------

    @Test
    void problemasListarYBuscar() throws Exception {
        mvc.perform(get("/problemas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/problemas").param("reportadoPorId", "EST-01"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/problemas/1")).andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("FALTA_AGUA"));
        mvc.perform(get("/problemas/999")).andExpect(status().isNotFound());
    }

    @Test
    void problemaReportarAtenderYBorrar() throws Exception {
        String respuesta = mvc.perform(post("/problemas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"OTRO\",\"descripcion\":\"Manguera rota\",\"sectorId\":1,\"reportadoPorId\":\"EST-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("INFORMADO"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(respuesta, "$.id");

        mvc.perform(patch("/problemas/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ATENDIDO\",\"comentarioDocente\":\"Cambiada\",\"atendidoPorId\":\"ADM-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDO"));

        mvc.perform(patch("/problemas/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comentarioDocente\":\"sin estado\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(delete("/problemas/" + id)).andExpect(status().isNoContent());
    }
}
