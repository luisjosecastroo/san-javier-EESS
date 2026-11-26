# Bibliotecas incluidas

El proyecto utiliza Swing y JDBC del JDK. No requiere Maven, Spring, Hibernate ni descargas al ejecutarlo.

| Archivo | Propósito | Fuente oficial |
|---|---|---|
| `postgresql-42.7.15.jar` | Controlador JDBC de PostgreSQL | https://jdbc.postgresql.org/download/ |
| `h2-2.5.252.jar` | Base de demostración local | https://github.com/h2database/h2database/releases/tag/version-2.5.252 |

Las bibliotecas fueron descargadas del sitio oficial de pgJDBC y de Maven Central. Sus avisos de licencia se encuentran dentro de los JAR y se copian en `lib/licencias/`.

Documentación utilizada para la conexión:

- https://supabase.com/docs/guides/database/connecting-to-postgres
- https://jdbc.postgresql.org/documentation/ssl/

Se utiliza el pooler de sesión del proyecto, puerto 5432, junto con `sslmode=verify-full` y el almacén de certificados de Java. Los parámetros de conexión y el esquema se comprobaron en el panel del proyecto.
