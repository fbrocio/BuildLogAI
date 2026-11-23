# Base de datos

Esta carpeta contiene los scripts SQL necesarios para preparar la base de datos PostgreSQL usada por la API.

## Script principal

El archivo principal es:

```text
database/schema.sql
```

Este script crea la base de datos `gestorobras` y las tablas necesarias para que la aplicacion pueda arrancar con:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Es decir, Hibernate no crea las tablas automaticamente: solo valida que el esquema existente coincida con las entidades JPA.

El archivo de datos de ejemplo es:

```text
database/seed.sql
```

Este script inserta usuarios, proyectos, registros, entradas de texto e imagenes de ejemplo para probar la API rapidamente.

## Como ejecutarlo

Desde la raiz del proyecto:

```powershell
psql "$env:SPRING_DATASOURCE_URL" -U "$env:SPRING_DATASOURCE_USERNAME" -f database/schema.sql
psql "$env:SPRING_DATASOURCE_URL" -U "$env:SPRING_DATASOURCE_USERNAME" -f database/seed.sql
```

Tambien puedes ejecutarlos desde pgAdmin, DBeaver, IntelliJ IDEA o cualquier cliente PostgreSQL abriendo los archivos `schema.sql` y `seed.sql`.

## Tablas creadas

El esquema crea estas tablas:

| Tabla | Descripcion |
| --- | --- |
| `users` | Usuarios registrados en la aplicacion |
| `projects` | Proyectos de obra |
| `project_users` | Relacion muchos-a-muchos entre proyectos y usuarios |
| `user_inputs` | Textos originales introducidos por el usuario antes de generar registros con IA |
| `records` | Registros de obra: incidencias, pendientes y avances |
| `record_images` | Imagenes asociadas a registros |

## Relaciones principales

- Un usuario puede crear proyectos.
- Un proyecto puede tener varios usuarios asociados.
- Un proyecto puede tener varios registros.
- Un registro puede tener varias imagenes.
- Un texto de entrada (`user_inputs`) puede originar varios registros.
- Un registro puede estar relacionado con otro registro mediante `related_record_id`.

## Campos relevantes

La tabla `records` usa restricciones `CHECK` de PostgreSQL para reflejar los enums Java:

```sql
type VARCHAR(20) CHECK (type IN ('INCIDENCIA', 'PENDIENTE', 'AVANCE'))
status VARCHAR(20) CHECK (status IN ('ABIERTA', 'CERRADA'))
```

Tambien incluye un campo JSONB:

```sql
structured_data JSONB
```

Este campo guarda informacion estructurada extraida por IA, como empresa, cantidad, unidad, asunto, fecha limite, porcentaje o precio.

## Tipos y codificación

El script usa:

```sql
TEXT, TIMESTAMP y JSONB
```

PostgreSQL admite claves foráneas, transacciones y Unicode de forma nativa.

## Datos iniciales

`schema.sql` solo crea estructura. `seed.sql` inserta datos demo.

Usuarios incluidos:

| Nombre | Email | Password |
| --- | --- | --- |
| Usuario Demo | `demo@example.com` | `demo1234` |
| Jefe de Obra | `jefe.obra@example.com` | `demo1234` |

El password esta guardado con BCrypt, por lo que puede usarse directamente en el endpoint de login.

La API permite crear usuarios desde:

```http
POST /api/users/register
```

Despues de crear usuarios y hacer login, se pueden crear proyectos y registros desde los endpoints de la API.

## Nota importante

Los registros se asocian al usuario autenticado mediante JWT. Para probar los flujos de creacion y confirmacion de registros, primero hay que registrar un usuario, iniciar sesion y enviar el token en la cabecera `Authorization`.
