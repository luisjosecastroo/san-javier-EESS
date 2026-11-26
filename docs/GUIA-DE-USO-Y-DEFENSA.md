# Guía de uso y defensa — EESS San Javier

Esta guía parte de que ya entiendes variables, condiciones, bucles y funciones en C++, y estás aprendiendo Java. Empieza ejecutando y observando el programa; después relaciona cada operación con su código.

## 1. Abrir el programa

Abre la carpeta `EESS-SAN-JAVIER-JAVA` como proyecto independiente en VS Code. En macOS puedes abrir `San Javier.app` con doble clic. También está `Ejecutar.command`, que compila antes de ejecutar usando el JDK instalado.

En la primera pantalla tienes dos opciones:

- **Abrir demostración:** utiliza datos ficticios guardados en `datos/estacion.mv.db`. Funciona sin internet. Puedes cerrar y volver a abrir: los registros se conservan.
- **Conectar a Supabase:** utiliza las tablas de tu proyecto EESSSANJAVIER. Escribe la contraseña de PostgreSQL, no la de tu cuenta de Supabase. El servidor y el usuario ya están precargados. La contraseña no se escribe en archivos.

El indicador inferior del menú dice cuál de las dos bases estás usando. Usa la demostración para practicar.

## 2. Recorrido por las pantallas

| Pantalla | Para qué sirve |
|---|---|
| Resumen | Ver ventas y litros de hoy, turnos abiertos y tanques con stock bajo. |
| Ventas | Registrar una venta con cliente, factura, placa, manguera, litros y pago. |
| Tanques y cisternas | Consultar stock y capacidad, agregar tanques y recibir combustible. |
| Clientes | Registrar o editar NIT/CI y razón social. |
| Turnos | Abrir turnos, asignar mangueras a despachadores y cerrar la operación. |
| Personal | Registrar administradores y despachadores y cambiar su estado activo. |
| Catálogos | Agregar combustibles, precios, islas y mangueras. |

En las tablas puedes ordenar pulsando la cabecera, buscar y exportar a CSV. Para editar o cerrar un registro, selecciona antes su fila. Se cargan hasta 500 registros por consulta. La exportación incluye las filas que ves después de filtrar.

Si todavía no tienes datos en la base remota, el orden de preparación es: personal, combustibles, precios, tanques, islas, mangueras, recepción de combustible, clientes, apertura de turno y asignación de mangueras. Después puedes vender.

## 3. Demostración para presentar al docente

Practica este recorrido completo. Los precios de la demostración son ejemplos académicos.

1. **Abre la demostración.** Explica que tiene persistencia local, separada de la conexión PostgreSQL.
2. **Registra un cliente.** En Clientes pulsa `+ Cliente`. Usa NIT/CI `PRUEBA-100` y razón social `Cliente para presentación`.
3. **Registra una venta.** Selecciona al cliente y la manguera de Gasolina Especial. Introduce una factura sin repetir, una placa y `10` litros. Con precio de ejemplo `3.74`, el total es `37.40` Bs. Guarda.
4. **Muestra el cambio de stock.** El tanque de Gasolina Especial debe bajar exactamente 10 litros.
5. **Recibe una cisterna.** Selecciona el mismo tanque, el administrador de demostración, una guía nueva y `100` litros. El stock debe subir exactamente 100 litros.
6. **Provoca un rechazo controlado.** Intenta recibir `999999` litros. El mensaje explica que se supera la capacidad. Corrige el campo o cancela. Muestra que el stock no cambió por el intento fallido.
7. **Cierra las asignaciones.** En Turnos entra a Ver asignaciones. Selecciona cada fila y pulsa Cerrar asignación. La lectura propuesta ya incluye sus ventas. Deja calibración en cero si no hubo calibración.
8. **Cierra el turno.** Regresa a Ver turnos, selecciona el turno abierto y ciérralo. El total se calcula desde sus ventas.
9. **Muestra persistencia.** Cierra y vuelve a abrir la aplicación: tus operaciones siguen guardadas.

Si intentas cerrar el turno antes que sus asignaciones, el programa lo rechaza. Si no hay turnos o asignaciones disponibles, primero debes abrir uno y asignar una manguera. Los identificadores que aparecen en la demostración pueden variar después de practicar.

## 4. Primero entiende estas seis clases

Lee en este orden:

1. `modelo/Cliente.java`
2. `modelo/MovimientoCombustible.java`
3. `modelo/Venta.java`
4. `modelo/Recepcion.java`
5. `modelo/Tanque.java`
6. `datos/RepositorioJDBC.java`, empezando por `registrarVenta()` y `registrarRecepcion()`.

Las clases están en paquetes: `eess.modelo` contiene reglas del negocio; `eess.datos` consulta y guarda; `eess.ui` dibuja ventanas y recibe las acciones del usuario. `Main` inicia el programa.

## 5. Clase y objeto, usando algo conocido

Una clase es el tipo; un objeto es una instancia de ese tipo. En C++ conoces `Cliente cliente(...)`. En Java escribes:

```java
Cliente cliente = new Cliente(0, "123456", "Cliente de prueba");
```

`new` construye el objeto y devuelve una referencia. `0` significa que es un cliente nuevo: la base asignará su ID al insertarlo. Se usa `cliente.getNitCi()` para consultar el documento. Java administra la memoria; aquí no escribimos `delete`.

El constructor de `Cliente` recibe los datos y los valida. `Validacion.texto()` rechaza `null`, cadenas vacías y espacios. Así el objeto nace con datos válidos.

## 6. Encapsulamiento, `private`, `final` y métodos

En `Cliente`:

```java
private final String nitCi;
```

`private` impide que otra clase acceda directamente al campo. `final` hace que la referencia se asigne una sola vez. `String` es inmutable; el texto de ese objeto tampoco cambia. Para editar un cliente, la interfaz construye otro objeto y actualiza su fila por ID.

Un método público permite consultar un dato:

```java
public String getNitCi() {
    return nitCi;
}
```

`public` indica acceso; `String` es el tipo de retorno; `getNitCi` es el nombre del método. No es una clase: falta la palabra `class`.

`Tanque.stock` no es `final` porque cambia. Sin embargo, sigue siendo `private`: solo `aplicarMovimiento()` puede cambiarlo. Ese método comprueba que el resultado no sea negativo ni supere la capacidad antes de asignarlo. No hay un `setStock()` que permita saltarse esas reglas.

## 7. Herencia y clase abstracta

Una venta y una recepción tienen algo en común: ambas mueven litros y cambian el stock. Esa parte común se expresa en:

```java
public abstract class MovimientoCombustible {
    private final BigDecimal litros;
    public abstract BigDecimal variacionStock();
}
```

El fragmento omite el constructor y otros métodos para mostrar la idea. `abstract` significa que no se construye un movimiento genérico con `new MovimientoCombustible(...)`. Se construye uno concreto: una venta o una recepción.

```java
public final class Venta extends MovimientoCombustible
```

`extends` indica herencia, parecido a `class Venta : public MovimientoCombustible` en C++. `super(litros)` llama al constructor de la clase padre. Aunque el campo `litros` es privado, la clase hija puede consultar su valor mediante `getLitros()`.

## 8. Polimorfismo: el punto principal de la defensa final

En `Venta`:

```java
@Override
public BigDecimal variacionStock() {
    return getLitros().negate();
}
```

En `Recepcion`:

```java
@Override
public BigDecimal variacionStock() {
    return getLitros();
}
```

`@Override` indica que el método implementa uno declarado en el padre. Java comprueba que su firma coincida. La venta devuelve litros negativos; la recepción, positivos.

`Tanque` recibe el tipo base:

```java
public void aplicarMovimiento(MovimientoCombustible movimiento) {
    BigDecimal nuevoStock = stock.add(movimiento.variacionStock());
    // Después valida límites y guarda el resultado.
}
```

Si recibe una `Venta`, Java ejecuta la versión de `Venta`. Si recibe una `Recepcion`, ejecuta la de `Recepcion`. La decisión depende del objeto real, aunque el parámetro tenga el tipo base. Esto es despacho dinámico, comparable a métodos virtuales en C++.

Puedes explicarlo así: **«El tanque trabaja con la abstracción MovimientoCombustible. Cada operación sabe cuánto cambia el stock; por eso el tanque no necesita preguntar si recibió una venta o una recepción».**

Las pruebas llaman al mismo método con ambos objetos y verifican la suma y la resta.

## 9. Interface y repositorio

`EstacionRepositorio` es una interfaz: declara operaciones como `guardarCliente`, `registrarVenta` y `consultar`. Define qué debe ofrecer la persistencia.

```java
public final class RepositorioJDBC implements EstacionRepositorio
```

`implements` significa que `RepositorioJDBC` cumple ese contrato. La ventana usa una referencia `EstacionRepositorio`; no necesita saber cómo se abre la conexión ni cómo se escribe cada SQL. En esta versión hay una implementación JDBC que funciona con PostgreSQL remoto o H2 local, según la conexión elegida.

La interfaz tiene un propósito de separación. La demostración más directa de polimorfismo del negocio es la jerarquía de movimientos descrita antes.

## 10. Por qué usamos `BigDecimal`

`double` representa números en binario y puede introducir diferencias en operaciones decimales. Los importes y los litros se representan con `BigDecimal`.

```java
getLitros().multiply(precio).setScale(2, RoundingMode.HALF_UP)
```

Multiplica los litros por el precio y redondea el importe a dos decimales. Se construyen valores desde texto: `new BigDecimal("3.74")`. Para comparar cantidades se usa `compareTo()`; `equals()` también compara la escala y podría distinguir `3.0` de `3.00`.

Los litros se normalizan a tres decimales. La base remota puede tener otra precisión: si su redondeo provoca que el stock guardado difiera del previsto, la transacción se revierte.

## 11. Qué sucede al pulsar Guardar en una venta

1. `Acciones.venta()` lee los campos y construye un objeto `Venta`.
2. El constructor valida que existan factura, placa, cliente, asignación, litros y precio positivo.
3. `RepositorioJDBC.registrarVenta()` abre una transacción.
4. Bloquea el turno mientras comprueba que siga abierto. Comprueba que la asignación no esté cerrada y el despachador siga activo.
5. Consulta el precio vigente. Si cambió desde que abriste el formulario, rechaza la operación para que la revises.
6. Lee y bloquea el tanque. `Tanque.aplicarMovimiento(venta)` calcula el stock esperado y valida disponibilidad.
7. Inserta la venta mediante SQL parametrizado.
8. En Supabase, el trigger existente descuenta el stock. En H2, el repositorio lo actualiza explícitamente. No hace ambas cosas sobre la misma base.
9. Comprueba que el stock guardado coincida con el esperado.
10. Hace `commit`. Si algo falla, hace `rollback` y muestra un mensaje conservando los campos del formulario.

Una recepción sigue el mismo principio, sumando litros y comprobando capacidad. La clase `Tanque` valida el objeto; la transacción y el bloqueo protegen los datos persistidos ante operaciones simultáneas.

## 12. SQL y recursos: lo que debes poder explicar

Una consulta parametrizada usa `?`:

```java
INSERT INTO cliente(nit_ci, razon_social) VALUES (?, ?)
```

`PreparedStatement.setObject()` envía cada valor por separado. Un nombre con apóstrofe se trata como un dato, no como una parte del SQL. Los nombres de las consultas vienen del enum `Catalogo`, no de lo que escriba el usuario.

`ResultSet` permite recorrer las filas obtenidas. `try (...)` es un bloque con cierre automático: cierra la conexión, el statement o el resultado al salir, incluso si se produce una excepción.

Una transacción agrupa cambios: `commit()` los confirma; `rollback()` los revierte. `FOR UPDATE` bloquea filas hasta terminar la transacción. En una venta se protege el turno y el tanque para evitar que otro proceso cierre el turno durante la venta o venda combustible que ya se consumió.

## 13. Turnos y lecturas

Una asignación relaciona un turno, un despachador y una manguera. La venta referencia esa asignación; así se puede identificar quién despachó y en qué turno.

Para cerrar una asignación se concilia:

```text
lectura final = lectura inicial + litros vendidos + litros de calibración
```

El programa supone que el combustible de calibración vuelve al tanque: se registra para la lectura, sin generar una venta ni alterar stock comercial. Si tu operación necesita registrar mermas o calibración no devuelta, eso requeriría otro movimiento y su persistencia.

El turno se cierra cuando todas sus asignaciones tienen lectura final. Su recaudación se obtiene sumando las ventas relacionadas, no escribiendo un total manual. Una asignación cerrada ya no admite ventas.

## 14. Cómo funciona la interfaz

Swing viene con Java. `JFrame` es una ventana principal; `JPanel` agrupa componentes; `JButton` es un botón; `JTextField` recibe texto; `JComboBox` permite elegir; `JTable` muestra registros.

```java
boton.addActionListener(evento -> mostrar(catalogo));
```

La expresión después de `->` es una lambda: una función corta que se ejecuta cuando ocurre el evento. También conoces funciones y callbacks en C++; aquí Java les da esta sintaxis.

Swing actualiza sus componentes en un hilo de eventos. `SwingWorker` ejecuta las consultas en segundo plano para que una conexión lenta no congele la ventana. `doInBackground()` hace el trabajo y `done()` actualiza el resultado en la pantalla.

`Formulario` conserva los campos cuando falla un guardado y desactiva Guardar mientras se está procesando, evitando dos envíos por un doble clic. Los mensajes de validación aparecen en el mismo formulario.

## 15. Mapa de archivos

| Archivo o grupo | Responsabilidad |
|---|---|
| `Main` | Iniciar el programa y localizar la carpeta de datos. |
| `Cliente` | Validar y transportar los datos de un cliente. |
| `MovimientoCombustible` | Abstracción común: litros y variación del stock. |
| `Venta` / `Recepcion` | Operaciones concretas con comportamientos distintos. |
| `Tanque` | Proteger capacidad y stock. |
| `Validacion` | Validaciones reutilizables de texto y números. |
| `Conexion` | Abrir conexiones JDBC para el modo seleccionado. |
| `EstacionRepositorio` | Contrato de persistencia. |
| `RepositorioJDBC` | Consultas, validaciones persistidas y transacciones. |
| `Catalogo` | Consultas fijas para los listados. |
| `Tabla` / `Opcion` | Transportar resultados y opciones a la interfaz. |
| `BaseDemostracion` | Crear la base H2 de práctica una sola vez. |
| `PantallaInicio` | Elegir demostración o conexión Supabase. |
| `VentanaEstacion` | Navegación, resumen, tablas, búsqueda y exportación. |
| `Acciones` | Construir formularios y convertir sus datos en operaciones. |
| `Formulario` / `Tema` | Componentes visuales reutilizables y apariencia. |

## 16. Preguntas de defensa para practicar en voz alta

1. **¿Dónde hay encapsulamiento?** En los campos privados, y especialmente en `Tanque`, cuyo stock solo cambia después de validar un movimiento.
2. **¿Dónde hay herencia?** `Venta` y `Recepcion` extienden `MovimientoCombustible`.
3. **¿Dónde hay polimorfismo?** `Tanque` llama `variacionStock()` sobre el tipo base y se ejecuta la versión del objeto concreto.
4. **¿Por qué el padre es abstracto?** Un movimiento genérico no define si los litros entran o salen; esa decisión pertenece a la operación concreta.
5. **¿Qué hace un constructor?** Inicializa y valida el estado de un objeto al crearlo.
6. **¿Por qué no hay `setStock()`?** Permitiría saltarse el control de capacidad y disponibilidad.
7. **¿Qué diferencia hay entre clase y objeto?** La clase define el tipo y sus reglas; el objeto contiene los datos de una instancia.
8. **¿Qué diferencia hay entre un atributo y un método?** El atributo guarda estado; el método realiza una operación o consulta.
9. **¿Qué sucede si un dato es inválido?** Se lanza una excepción. La interfaz muestra el error y permite corregirlo. En una transacción se revierte lo realizado.
10. **¿Por qué una venta no descuenta dos veces en Supabase?** Porque el SQL inserta la venta y deja la actualización persistida al trigger existente. La actualización explícita solo se usa en H2 local.
11. **¿Qué ocurre si dos ventas intentan consumir el mismo combustible?** Se bloquea la fila del tanque; la segunda operación valida el stock actualizado cuando obtiene el bloqueo.
12. **¿Qué hace la interfaz `EstacionRepositorio`?** Define las operaciones que la ventana necesita, separándolas de los detalles JDBC.
13. **¿Por qué usamos `BigDecimal`?** Para operar con cantidades decimales y redondeo explícito.
14. **¿Dónde se guarda el programa localmente?** Los fuentes están en `src`, el ejecutable en el JAR y los datos de práctica en `datos`.
15. **¿Qué generó la IA?** La implementación inicial, las pruebas y la documentación. Explica con honestidad lo que estudiaste o modificaste tú.

## 17. Ejercicios de cambios en vivo

Hazlos en copias de los fuentes y comprueba que entiendes sus efectos:

1. Cambia el umbral visual de stock bajo de 20% a 15%. Busca `0.20` en `RepositorioJDBC.resumen()` y `VentanaEstacion.crearResumen()`: deben coincidir el indicador y el color.
2. Añade un límite máximo de litros por venta en el constructor de `Venta`. Comprueba una venta válida y otra que supere el límite.
3. Añade una nueva clase hija `AjusteCombustible` y úsala en una prueba de `Tanque`. Esto demuestra extensión de la jerarquía. Guardarla en la base exigiría además diseñar una tabla y su operación: crear la clase por sí sola no añade persistencia.
4. Cambia el texto de un botón y recompila. Explica por qué eso afecta la interfaz y no el cálculo de stock.
5. Agrega una prueba que intente recibir exactamente la capacidad disponible: debería permitirse, pero un litro adicional debería rechazarse.

## 18. Compilar, probar y actualizar la aplicación

Desde la carpeta del proyecto:

```sh
bash compilar.sh
bash probar.sh
java -jar EstacionSanJavier.jar
```

La compilación genera `EstacionSanJavier.jar`. Las pruebas usan bases H2 en memoria y no modifican tus datos locales de práctica ni Supabase. Comprueban 73 casos: modelos, consultas, guardados, rechazos, cierres y concurrencia.

Si cambias fuentes, el JAR se actualiza al compilar. La aplicación macOS empaquetada contiene una copia del JAR: utiliza `bash empaquetar-mac.sh` para actualizarla. Cierra la aplicación antes de actualizar sus archivos.

En Windows, `Ejecutar.bat` abre el JAR ya compilado con Java 17 o posterior. El proyecto no necesita Maven; los dos controladores están incluidos en `lib`.

## 19. Ruta de estudio sugerida

Primero domina clases, atributos, constructores y métodos con `Cliente`. Después practica el cálculo decimal con `Venta`. Sigue con la jerarquía de movimientos y `Tanque`: aquí debes poder demostrar la POO sin abrir la interfaz. Luego recorre una venta desde el formulario hasta la transacción. Por último estudia turnos, persistencia y componentes Swing.

Para cada bloque, intenta explicar una clase sin leer esta guía y realiza un cambio pequeño. Si todavía no puedes explicar una parte, anótala para practicarla. Leer el código o pasar las pruebas no sustituye esa práctica.

## 20. Pendientes antes de entregar

Completa tu nombre y la fecha en el README, revisa la propuesta con el docente y verifica la conexión real introduciendo tu contraseña en la aplicación. Publica los fuentes en el repositorio cuando decidas hacerlo, manteniendo fuera `datos/` y las credenciales. El UML se dejó pendiente por tu indicación; la rúbrica lo solicita para el final.
