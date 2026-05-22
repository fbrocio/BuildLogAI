# BuildLog AI API

Backend de una aplicacion de gestión y análisis de obra asistida por inteligencia artificial.

La API permite registrar usuarios, autenticar peticiones con JWT, gestionar proyectos, crear registros de obra, adjuntar imagenes, transformar texto libre en registros estructurados mediante IA y generar informes técnicos en Markdown o PDF.

## Tecnologias principales

- Java 21
- Spring Boot 4
- Maven
- Spring Data JPA
- MySQL
- JWT
- Google Gemini API
- Generación de PDF con OpenPDF / OpenHTMLToPDF

## Requisitos

- JDK 21
- Maven, o el wrapper incluido `mvnw`
- MySQL en local
- Base de datos `gestorobras`
- Claves de IA configuradas en `.env`º1

## Configuracion

La configuracion principal esta en `src/main/resources/application.properties`.

Por defecto la API usa:

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/gestorobras
spring.datasource.username=root
spring.datasource.password=root
```

Tambien carga variables desde un archivo `.env` en la raiz del proyecto:

```properties
OPENAI_API_KEY=...
GOOGLE_GEMINI_API_KEY=...
GOOGLE_GEMINI_PROJECT_ID=...
GOOGLE_GEMINI_LOCATION=...
```

El proyecto valida el esquema de base de datos al arrancar:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Por tanto, las tablas deben existir antes de ejecutar la aplicacion.

## Ejecucion

Desde la raiz del proyecto:

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

La API quedará disponible en:

```text
http://localhost:8080
```

## Autenticación

El login devuelve un token JWT. Las rutas que dependen del usuario autenticado esperan la cabecera:

```http
Authorization: Bearer <token>
```

El filtro `JwtFilter` valida el token y guarda el `userId` en la request para que los controladores puedan usarlo.

## Funcionalidades

## Usuarios

Permite registrar usuarios, iniciar sesion y consultar informacion basica.

### Registrar usuario

```http
POST /api/users/register
```

Body:

```json
{
  "name": "Usuario Demo",
  "email": "demo@example.com",
  "password": "secret"
}
```

### Login

```http
POST /api/users/login
```

Body:

```json
{
  "email": "demo@example.com",
  "password": "secret"
}
```

Respuesta:

```json
{
  "token": "jwt-token",
  "name": "Usuario Demo",
  "email": "demo@example.com"
}
```

### Usuario autenticado

```http
GET /api/users/me
Authorization: Bearer <token>
```

### Buscar usuario por email

```http
GET /api/users/email?email=demo@example.com
```

## Proyectos

Permite crear proyectos, listar los proyectos asociados al usuario autenticado, editar proyectos, eliminarlos y asociar usuarios.

### Crear proyecto

```http
POST /api/projects
Authorization: Bearer <token>
```

Body:

```json
{
  "name": "Reforma local comercial",
  "description": "Seguimiento de obra y control de incidencias"
}
```

### Listar proyectos del usuario

```http
GET /api/projects
Authorization: Bearer <token>
```

### Actualizar proyecto

```http
PUT /api/projects/{id}
Authorization: Bearer <token>
```

### Eliminar proyecto

```http
DELETE /api/projects/{id}
Authorization: Bearer <token>
```

### Ver usuarios de un proyecto

```http
GET /api/projects/{id}/users
```

### Anadir usuario a un proyecto

```http
POST /api/projects/{projectId}/users/{userId}
```

## Registros de obra

Los registros representan incidencias, pendientes o avances de un proyecto.

Tipos disponibles:

- `INCIDENCIA`
- `PENDIENTE`
- `AVANCE`

Estados disponibles:

- `ABIERTA`
- `CERRADA`

### Crear registro

```http
POST /records
Authorization: Bearer <token>
```

Body:

```json
{
  "title": "Luminarias defectuosas",
  "description": "Han llegado 5 luminarias rotas",
  "type": "INCIDENCIA",
  "status": "ABIERTA",
  "projectId": 1
}
```

### Listar todos los registros

```http
GET /records
```

### Obtener registro por ID

```http
GET /records/{id}
```

### Listar registros de un proyecto

```http
GET /records/project/{projectId}
```

### Actualizar registro

```http
PUT /records/{id}
```

### Cambiar estado de un registro

```http
PATCH /records/{id}/status
```

Body:

```json
{
  "status": "CERRADA"
}
```

### Eliminar registro

```http
DELETE /records/{id}
```

## Imagenes de registros

Permite subir imagenes asociadas a un registro. Los archivos se guardan en `upload/records` y se sirven publicamente desde `/upload/**`.

### Subir imagen

```http
POST /records/{id}/images
Content-Type: multipart/form-data
```

Campo del formulario:

```text
image=<archivo>
```

La respuesta contiene la entidad `RecordImage` con la URL generada, por ejemplo:

```text
/upload/records/<uuid>.jpg
```

### Listar imagenes de un registro

```http
GET /records/{id}/images
```

## IA para estructurar texto

La API puede transformar texto libre en registros de obra estructurados usando Gemini.

### Parsear texto con IA

```http
POST /ai/parse
```

Body:

```json
{
  "text": "Han llegado rotas 5 luminarias de ArkosLight. Pedir reposicion antes del viernes."
}
```

Respuesta esperada:

```json
{
  "records": [
    {
      "title": "Recepcion de luminarias defectuosas",
      "description": "Han llegado 5 luminarias rotas de ArkosLight",
      "type": "INCIDENCIA",
      "status": "ABIERTA",
      "structuredData": {
        "company": "ArkosLight",
        "quantity": 5,
        "unit": "uds",
        "subject": "Luminarias defectuosas"
      }
    }
  ]
}
```

### Parsear texto para un proyecto

```http
POST /records/parse
```

Body:

```json
{
  "text": "Han llegado rotas 5 luminarias de ArkosLight.",
  "projectId": 1
}
```

### Confirmar registros generados

```http
POST /records/confirm
Authorization: Bearer <token>
```

Body:

```json
{
  "rawText": "Han llegado rotas 5 luminarias de ArkosLight.",
  "projectId": 1,
  "source": "texto",
  "records": [
    {
      "title": "Recepcion de luminarias defectuosas",
      "description": "Han llegado 5 luminarias rotas de ArkosLight",
      "type": "INCIDENCIA",
      "status": "ABIERTA",
      "projectId": 1
    }
  ]
}
```

## Informes

La API puede buscar registros por tema, generar un informe técnico con IA y devolverlo como texto Markdown o como PDF.

### Generar informe en Markdown

```http
POST /reports/generate
```

Body:

```json
{
  "topic": "luminarias"
}
```

Respuesta:

```json
{
  "report": "# Informe tecnico..."
}
```

### Generar informe PDF

```http
POST /reports/generate-pdf
Accept: application/pdf
```

Body:

```json
{
  "topic": "luminarias"
}
```

La respuesta devuelve un PDF inline con nombre similar a:

```text
informe_<timestamp>.pdf
```

## Endpoints principales

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| POST | `/api/users/register` | Registra un usuario |
| POST | `/api/users/login` | Autentica y devuelve JWT |
| GET | `/api/users/me` | Devuelve el usuario autenticado |
| GET | `/api/users/email` | Busca usuario por email |
| POST | `/api/projects` | Crea un proyecto |
| GET | `/api/projects` | Lista proyectos del usuario autenticado |
| PUT | `/api/projects/{id}` | Actualiza un proyecto |
| DELETE | `/api/projects/{id}` | Elimina un proyecto |
| GET | `/api/projects/{id}/users` | Lista usuarios de un proyecto |
| POST | `/api/projects/{projectId}/users/{userId}` | Asocia un usuario a un proyecto |
| POST | `/records` | Crea un registro |
| GET | `/records` | Lista registros |
| GET | `/records/{id}` | Obtiene un registro |
| GET | `/records/project/{projectId}` | Lista registros de un proyecto |
| PUT | `/records/{id}` | Actualiza un registro |
| PATCH | `/records/{id}/status` | Cambia el estado de un registro |
| DELETE | `/records/{id}` | Elimina un registro |
| POST | `/records/{id}/images` | Sube imagen a un registro |
| GET | `/records/{id}/images` | Lista imagenes de un registro |
| POST | `/ai/parse` | Convierte texto libre en registros estructurados |
| POST | `/records/parse` | Genera registros desde texto para un proyecto |
| POST | `/records/confirm` | Persiste registros generados por IA |
| POST | `/reports/generate` | Genera informe en Markdown |
| POST | `/reports/generate-pdf` | Genera informe en PDF |

## Notas de desarrollo

- El limite de subida de archivos esta configurado en 10 MB.
- Los archivos subidos se almacenan en la carpeta local `upload/records`.
- La ruta publica de archivos es `/upload/**`.
- Algunas rutas usan autenticacion JWT de forma explicita en el controlador, principalmente usuarios y proyectos.
- La creacion directa y la confirmacion de registros usan el usuario autenticado mediante JWT como autor.
- El servicio de IA intenta usar `gemini-2.5-flash` y, si falla, `gemini-1.5-flash`.

## Tests

El proyecto incluye actualmente pruebas basicas:

- carga del contexto de Spring Boot,
- generacion de PDF desde Markdown.

Quedan pendientes pruebas mas completas para controladores, autenticacion JWT, gestion de usuarios, proyectos, registros, subida de imagenes y flujos de IA.

Para ejecutar las pruebas:

```bash
./mvnw test
```

En Windows:

```powershell
.\mvnw.cmd test
```
