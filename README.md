# Huerto API

API REST para la app Android **Huerto Escolar** (DSY1105 · Duoc UC). Está hecha con la misma base que `miapi`: Spring Boot, JPA, MySQL y Lombok, con capas `model → repository → service → controller`.

## Cómo ejecutarla

1. Enciende MySQL (XAMPP o Laragon), con el usuario `root` sin clave.
2. Abre la carpeta en IntelliJ y ejecuta `HuertoApiApplication`, o en la terminal:
   ```
   mvnw spring-boot:run
   ```
3. La base `bd_huerto` y sus tablas se crean solas, y se cargan datos de prueba (los mismos de la app).
4. Prueba en el navegador: http://localhost:8080/sectores

Desde el emulador de Android la dirección es `http://10.0.2.2:8080/`.

## Usuarios de prueba

| Código | Clave | Perfil | Grupo |
|---|---|---|---|
| ADM-01 | 1234 | ADMINISTRADOR | — |
| EST-01 | 1234 | ESTUDIANTE | Taller A |
| EST-02 | 1234 | ESTUDIANTE | Taller A |
| EST-03 | 1234 | ESTUDIANTE | Taller B |

La clave nunca se devuelve en las respuestas.

## Endpoints

### Usuarios
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/usuarios/login` | Login. Cuerpo `{"id":"ADM-01","clave":"1234"}`. 200 con el usuario o 401 |
| GET | `/usuarios` · `/usuarios?perfil=ESTUDIANTE` | Listar |
| GET | `/usuarios/{id}` | Buscar por código |
| POST | `/usuarios` | Crear (409 si el código ya existe) |
| PUT | `/usuarios/{id}` | Actualizar |
| DELETE | `/usuarios/{id}` | Borrar |

### Sectores
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/sectores` | Listar |
| GET | `/sectores/{id}` | Buscar |
| GET | `/sectores/{id}/cultivos` | Cultivos del sector |
| POST | `/sectores` | Crear (409 si el código se repite) |
| PUT | `/sectores/{id}` | Actualizar |
| DELETE | `/sectores/{id}` | Borrar (409 si tiene cultivos, tareas o problemas) |

### Cultivos
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/cultivos` · `/cultivos?sectorId=1` | Listar |
| GET | `/cultivos/{id}` | Buscar |
| POST | `/cultivos` | Crear (400 si el sector no existe, riego ≤ 0 o cosecha antes de siembra) |
| PUT | `/cultivos/{id}` | Actualizar |
| DELETE | `/cultivos/{id}` | Borrar (409 si tiene tareas) |

### Tareas
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/tareas` | Todas |
| GET | `/tareas?asignadoId=EST-01&grupo=Taller A` | Tareas de un estudiante y de su grupo |
| GET | `/tareas?estado=PENDIENTE_DE_VALIDACION` | Por estado |
| GET | `/tareas/{id}` | Buscar |
| POST | `/tareas` | Crear (parte en `PENDIENTE`) |
| PUT | `/tareas/{id}` | Actualizar datos |
| PATCH | `/tareas/{id}/estado` | Cambiar estado. Cuerpo `{"estado":"VALIDADA"}` |
| DELETE | `/tareas/{id}` | Borrar (409 si ya fue iniciada) |

### Problemas
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/problemas` · `/problemas?reportadoPorId=EST-01` | Listar |
| GET | `/problemas/{id}` | Buscar |
| POST | `/problemas` | Reportar (parte en `INFORMADO`) |
| PATCH | `/problemas/{id}/estado` | Atender. Cuerpo `{"estado":"EN_REVISION","comentarioDocente":"...","atendidoPorId":"ADM-01"}` |
| DELETE | `/problemas/{id}` | Borrar |

## Ejemplo: crear una tarea

```
POST http://localhost:8080/tareas
Content-Type: application/json

{
  "tipo": "RIEGO",
  "descripcion": "Regar las lechugas sin mojar las hojas.",
  "sectorId": 1,
  "cultivoId": 1,
  "fechaProgramada": 1763337600000,
  "asignadoId": "EST-01",
  "creadaPorId": "ADM-01"
}
```

## Compatibilidad con la app Android

- **Mismos nombres de campos y de enums** que las entidades de la app (`EstadoTarea.VALIDADA`, `TipoProblema.FALTA_AGUA`…), así Retrofit + Gson los convierte directo.
- **Fechas en milisegundos** (`Long`), igual que en Room.
- **Errores con código HTTP y mensaje:** 400 dato inválido, 401 login incorrecto, 404 no existe, 409 regla de negocio. El mensaje viene en el campo `message` de la respuesta.

## Pruebas y cobertura (Kover)

Las pruebas usan una base **H2 en memoria** (`src/test/resources/application.properties`),
así que **no necesitan XAMPP** encendido.

```.\mvnw clean verify
```

- Corre las pruebas unitarias (servicios con Mockito) y de integración (endpoints con MockMvc).
- Genera el informe en `target/site/kover/html/index.html` (y `target/site/kover/report.xml`).
- Falla si la cobertura de líneas baja del **80 %** (regla en el `pom.xml`).

El código que genera Lombok (getters, setters, constructores) no se cuenta en la cobertura
gracias a `lombok.config` + el filtro `annotatedBy lombok.Generated` de Kover.
