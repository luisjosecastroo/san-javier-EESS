# Propuesta Inicial de Proyecto – Programación Orientada a Objetos

**Carrera:** Ingeniería de Software  
**Materia:** Programación II  
**Periodo:** Segundo Parcial / Proyecto Final  
**Estudiante(s):** *Completar nombre completo*  
**Fecha de entrega:** *Completar fecha acordada con el docente*  

---

## 1. Datos Generales del Proyecto

| Campo | Descripción |
|--------|-------------|
| **Nombre del proyecto:** | San Javier – Gestión de Estación de Servicio |
| **Tipo de aplicación:** | ☒ Escritorio ☐ Web ☐ Móvil ☐ Otro: __________ |
| **Lenguaje / entorno de desarrollo:** | Java 17 o posterior, Swing y JDBC; Visual Studio Code |
| **Repositorio Git (URL):** | https://github.com/luisjosecastroo/DB1-FINALPROJECT — publicación del código pendiente |
| **Uso de Inteligencia Artificial:** | ☐ No ☒ Sí (describir a continuación) |

**Si usas IA, explica brevemente cómo y en qué etapa contribuye:**
> Codex generó la implementación inicial, la interfaz, pruebas y documentación a partir de mi base existente en Supabase y de mis instrucciones. La guía permite estudiar el código. El dominio y la adaptación por el estudiante quedan pendientes de su preparación; este documento no afirma que ya pueda defender todo el código. Véase `BITACORA-IA.md`.

---

## 2. Descripción del Proyecto

### Resumen breve
Aplicación de escritorio para organizar la operación de la estación de servicio San Javier.  
Registra clientes, personal, ventas y recepción de combustible.  
Controla la disponibilidad de los tanques y evita vender más combustible del disponible.  
Relaciona turnos, despachadores y mangueras, y calcula la recaudación de cierre.  
Utiliza la base PostgreSQL existente en Supabase.  
Incluye una demostración local persistente para practicar sin conexión.

### Objetivos principales
1. Registrar ventas y recepciones manteniendo un stock consistente.
2. Organizar clientes, personal, precios y asignaciones de turno.
3. Aplicar encapsulamiento, herencia, polimorfismo e interfaces en un caso real.

---

## 3. Diseño Técnico y Aplicación de POO

### Principios de POO aplicados
- [x] Encapsulamiento (atributos privados y métodos públicos)
- [x] Uso de constructores
- [x] Herencia
- [x] Polimorfismo
- [x] Interfaces o clases abstractas

`Venta` y `Recepcion` heredan de `MovimientoCombustible`. Cada una implementa `variacionStock()` con un signo distinto. `Tanque.aplicarMovimiento()` recibe el tipo base y utiliza ese comportamiento. `EstacionRepositorio` define el contrato de persistencia, implementado por `RepositorioJDBC`.

### Persistencia de datos
- [x] Archivos locales — base H2 de demostración en `datos/`
- [x] Base de datos — PostgreSQL existente en Supabase
- [ ] En memoria (temporal)
- [ ] Otro: __________

Las pruebas usan una base H2 aislada en memoria. Los datos de demostración y los de Supabase son independientes.

---

## 4. Funcionalidades Principales

| Nº | Nombre de la funcionalidad | Descripción breve | Estado actual |
|----|-----------------------------|-------------------|----------------|
| 1 | Resumen | Ventas y litros de hoy, turnos abiertos y nivel de los tanques. | ☐ Planeada ☒ En desarrollo — implementada y probada localmente |
| 2 | Ventas | Cliente, manguera asignada, factura, placa, precio vigente, pago y validación de stock. | ☐ Planeada ☒ En desarrollo — implementada y probada localmente |
| 3 | Recepciones | Entrada por guía de remisión, responsable y tanque, respetando capacidad. | ☐ Planeada ☒ En desarrollo — implementada y probada localmente |
| 4 | Clientes y personal | Alta y edición de clientes, alta de personal, activación y desactivación. | ☐ Planeada ☒ En desarrollo — implementada y probada localmente |
| 5 | Turnos y asignaciones | Apertura, asignación de mangueras, conciliación de lecturas y cierre de recaudación. | ☐ Planeada ☒ En desarrollo — implementada y probada localmente |
| 6 | Catálogos | Alta de combustibles, islas, tanques, mangueras y conservación del historial de precios. | ☐ Planeada ☒ En desarrollo — implementada y probada localmente |
| 7 | Búsqueda y exportación | Filtrado de tablas y exportación de las filas visibles a CSV. | ☐ Planeada ☒ En desarrollo — implementada |

---

## 5. Compromiso del Estudiante

**Declaración para revisar y firmar después de comprender y adaptar la implementación. Actualmente sin firmar.**

Declaro que:
- Entiendo los criterios de evaluación establecidos en las rúbricas.
- Presentaré una demostración funcional del proyecto.
- Defenderé el código que yo mismo implementé y explicaré las clases y métodos principales.
- Si usé herramientas de IA, comprendo su funcionamiento y las adapté al contexto del proyecto.

**Firma (nombre completo):** __________________________

---

## 6. Validación del Docente *(completa el profesor)*

| Campo | Detalle |
|--------|---------|
| **Visto bueno del docente:** | ☐ Aprobado para desarrollar ☐ Requiere ajustes ☐ Rechazado |
| **Comentarios / Observaciones:** | |
| **Firma docente:** | |
| **Fecha de revisión:** | |

---

> **Instrucciones para entrega:**
> - Guarda este archivo como `README.md` dentro de tu repositorio Git.
> - Completa todas las secciones antes de tu presentación inicial.
> - No borres las casillas ni el formato para garantizar uniformidad del curso.
> - El docente revisará y aprobará esta propuesta antes del desarrollo completo.

---

## 7. Ejecución e instalación

### macOS

Abre **`San Javier.app`** con doble clic. Incluye su propio entorno Java. En la primera pantalla elige **Abrir demostración** para empezar con datos ficticios guardados localmente.

También puedes usar **`Ejecutar.command`**, que recompila los fuentes antes de ejecutar. Esta alternativa necesita un JDK 17 o posterior.

Desde una terminal, situado en esta carpeta:

```sh
bash compilar.sh
java -jar EstacionSanJavier.jar
```

### Windows y otros equipos

Con Java 17 o posterior instalado, conserva `EstacionSanJavier.jar` y la carpeta `lib` juntos. En Windows abre `Ejecutar.bat`. En cualquier sistema puedes ejecutar `java -jar EstacionSanJavier.jar`.

### Visual Studio Code

Abre **esta carpeta** como proyecto independiente. El punto de entrada es `src/eess/Main.java`. Se incluyen ajustes de rutas de fuentes, bibliotecas y depuración en `.vscode/`.

### Conectar la base existente en Supabase

En la pantalla inicial se precargan el servidor de sesión y el usuario del proyecto EESSSANJAVIER. Escribe **la contraseña PostgreSQL de la base de datos** y pulsa **Conectar a Supabase**. No es la contraseña de tu cuenta del navegador ni una API key.

La contraseña solo se conserva en memoria durante la ejecución. No hay credenciales guardadas en el código. La conexión usa TLS con verificación del servidor. La aplicación comprueba las tablas y los triggers antes de abrir la ventana principal.

Los scripts de `sql/` son exclusivamente para H2 local. El programa se adapta al esquema que ya existe en Supabase y a los triggers `fn_descontar_stock_venta` y `fn_sumar_stock_cisterna`; no ejecuta esos scripts sobre tu base remota.

**La conexión real requiere que el propietario introduzca su contraseña. Las verificaciones automatizadas se realizaron en la base local, no sobre datos de Supabase.**

## 8. Pruebas y guía

```sh
bash probar.sh
```

Resultado de integración: **73 comprobaciones aprobadas**. Incluyen validaciones, lectura de las 11 tablas, transacciones, reversión ante errores y dos ventas simultáneas sobre el mismo stock.

- [Guía de uso y defensa](docs/GUIA-DE-USO-Y-DEFENSA.md)
- [Registro de IA](BITACORA-IA.md)
- [Bibliotecas y fuentes oficiales](lib/DEPENDENCIAS.md)

El UML queda pendiente por indicación del estudiante. Para la entrega final habrá que añadirlo conforme a la rúbrica.

## 9. Alcance de esta versión

Las tablas muestran hasta 500 registros por consulta; la búsqueda y el CSV operan sobre esos registros cargados. El resumen de hoy se calcula sobre toda la base, con zona horaria de Bolivia. Los responsables se seleccionan desde personal; no hay autenticación individual de operadores. El número de factura se registra como dato; no se integra un servicio externo de facturación. La calibración se registra para conciliar lecturas y se supone devuelta al tanque, sin alterar el stock comercial. No se incluyen eliminación de operaciones ni anulación de ventas.
