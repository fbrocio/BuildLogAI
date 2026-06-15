# BuildLog AI API

Backend de gestion y analisis de obra asistido por inteligencia artificial.

La API permite registrar usuarios, autenticar peticiones con JWT, gestionar proyectos, crear registros de obra, adjuntar imagenes, transformar texto libre en registros estructurados mediante IA y generar informes tecnicos en Markdown o PDF.

## Tecnologias principales

- Java 21
- Spring Boot 4
- Maven
- Spring Data JPA
- PostgreSQL
- JWT
- Google Gemini API
- Cloudinary para almacenamiento de imagenes
- Brevo para envio de correos
- Generacion de PDF con OpenPDF / OpenHTMLToPDF

## Requisitos

- JDK 21
- Maven, o el wrapper incluido `mvnw`
- PostgreSQL local
- Base de datos `gestorobras`
- Variables de entorno configuradas para base de datos, IA, Cloudinary y correo

## Configuracion

La configuracion principal esta en `src/main/resources/application.properties`.

Por defecto la aplicacion activa el perfil `dev`:

```properties
spring.profiles.active=dev
```

En desarrollo usa PostgreSQL con esta configuracion base:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gestorobras
spring.datasource.username=postgres
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

En produccion se espera que las credenciales lleguen por variables de entorno:

```properties
SPRING_DATASOURCE_URL=...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
PORT=8080
```

La aplicacion tambien carga variables desde un archivo `.env` en la raiz del proyecto:

```properties
OPENAI_API_KEY=...
GOOGLE_GEMINI_API_KEY=...
GOOGLE_GEMINI_PROJECT_ID=...
GOOGLE_GEMINI_LOCATION=...
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
MAIL_USERNAME=...
MAIL_PASSWORD=...
BREVO_API_KEY=...
```

Ademas, la configuracion incluye:

- `spring.servlet.multipart.max-file-size=10MB`
- `spring.servlet.multipart.max-request-size=10MB`
- `jwt.secret=my-super-secret-key-my-super-secret-key`
- `jwt.expiration=86400000`

## Ejecucion

Desde la raiz del proyecto:

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8080
```

## Autenticacion

El login devuelve un token JWT. Las rutas protegidas esperan la cabecera:

```http
Authorization: Bearer <token>
```

El filtro `JwtFilter` valida el token y guarda el `userId` en la request para que los controladores puedan usarlo.

## Funcionalidades

## Usuarios

Permite registrar usuarios, iniciar sesion, verificar la cuenta y consultar informacion basica.

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

Devuelve el `userId` extraido del JWT validado por el backend.

### Buscar usuario por email

```http
GET /api/users/email?email=demo@example.com
```

### Verificar cuenta

```http
POST /api/users/verify
```

Body:

```json
{
  "email": "demo@example.com",
  "code": "123456"
}
```

### Reenviar codigo de verificacion

```http
POST /api/users/resend-verification?email=demo@example.com
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

### Endpoint de debug

```http
GET /api/projects/debug
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
Authorization: Bearer <token>
```

### Eliminar usuario de un proyecto

```http
DELETE /api/projects/{projectId}/users/{userId}
Authorization: Bearer <token>
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
Authorization: Bearer <token>
```

### Actualizar registro

```http
PUT /records/{id}
Authorization: Bearer <token>
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
Authorization: Bearer <token>
```

## Imagenes de registros

Permite subir imagenes asociadas a un registro. Los archivos se sirven publicamente desde `/upload/**`.

### Subir imagen

```http
POST /records/{id}/images
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

Campo del formulario:

```text
image=<archivo>
```

La respuesta devuelve la URL generada, por ejemplo:

```text
/upload/records/<uuid>.jpg
```

### Listar imagenes de un registro

```http
GET /records/{id}/images
```

### Eliminar imagen de un registro

```http
DELETE /records/images/{imageId}
Authorization: Bearer <token>
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

La API puede buscar registros por tema, generar un informe tecnico con IA y devolverlo como texto Markdown o como PDF.

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
| GET | `/api/users/me` | Devuelve el userId autenticado |
| GET | `/api/users/email` | Busca usuario por email |
| POST | `/api/users/verify` | Verifica la cuenta por codigo |
| POST | `/api/users/resend-verification` | Reenvia el codigo de verificacion |
| POST | `/api/projects` | Crea un proyecto |
| GET | `/api/projects` | Lista proyectos del usuario autenticado |
| GET | `/api/projects/debug` | Muestra el numero total de proyectos |
| PUT | `/api/projects/{id}` | Actualiza un proyecto |
| DELETE | `/api/projects/{id}` | Elimina un proyecto |
| GET | `/api/projects/{id}/users` | Lista usuarios de un proyecto |
| POST | `/api/projects/{projectId}/users/{userId}` | Asocia un usuario a un proyecto |
| DELETE | `/api/projects/{projectId}/users/{userId}` | Desasocia un usuario de un proyecto |
| POST | `/records` | Crea un registro |
| GET | `/records` | Lista registros |
| GET | `/records/{id}` | Obtiene un registro |
| GET | `/records/project/{projectId}` | Lista registros de un proyecto |
| PUT | `/records/{id}` | Actualiza un registro |
| PATCH | `/records/{id}/status` | Cambia el estado de un registro |
| DELETE | `/records/{id}` | Elimina un registro |
| POST | `/records/{id}/images` | Sube imagen a un registro |
| GET | `/records/{id}/images` | Lista imagenes de un registro |
| DELETE | `/records/images/{imageId}` | Elimina una imagen |
| POST | `/ai/parse` | Convierte texto libre en registros estructurados |
| POST | `/records/parse` | Genera registros desde texto para un proyecto |
| POST | `/records/confirm` | Persiste registros generados por IA |
| POST | `/reports/generate` | Genera informe en Markdown |
| POST | `/reports/generate-pdf` | Genera informe en PDF |

## Notas de desarrollo

- El limite de subida de archivos esta configurado en 10 MB.
- Los archivos subidos se almacenan en `upload/records` y se exponen desde `/upload/**`.
- Algunas rutas usan autenticacion JWT de forma explicita en el controlador, principalmente usuarios, proyectos y registros mutables.
- La creacion directa y la confirmacion de registros usan el usuario autenticado como autor.
- El servicio de IA intenta usar `gemini-2.5-flash` y, si falla, `gemini-1.5-flash`.
- El envio de verificacion por correo usa Brevo.

## Docker

Hay un `Dockerfile` en el repositorio para construir la imagen de la aplicacion.

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
