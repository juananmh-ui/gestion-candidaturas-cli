# JobTrack · Sistema de Gestión de Candidaturas

Proyecto desarrollado para los módulos de **Acceso a Datos** y **Desarrollo de Interfaces** (2º DAM).

JobTrack permite gestionar candidaturas, empresas y entrevistas desde una aplicación de escritorio. La v2.0 usa Java 17 o superior, Maven, JavaFX 21, vistas FXML compatibles con Scene Builder y MySQL mediante JDBC.

## Integrantes del Equipo
- **Óscar Ramírez Toro**
- **Juan Antonio Moreno Hevilla**

---

## Aportación de Óscar Ramírez Toro a la v2.0

Óscar implementó la gestión de candidaturas en JavaFX y la migración de las ofertas a MySQL mediante JDBC. El listado permite combinar filtros por empresa, estado, salario mínimo y fecha de postulación. Desde él se puede abrir el detalle, añadir y editar ofertas, cambiar su estado, eliminar registros e importar el CSV inicial.

| Commit | Aportación |
| --- | --- |
| [`25a75a1`](https://github.com/OscarRamirezToro/gestion-candidaturas-cli/commit/25a75a175f5cd0d2b25f0b18ec27dc34240cebc2) | Configuración de MySQL y JDBC con HikariCP, esquema relacional y modelo de oferta. |
| [`1e2113f`](https://github.com/OscarRamirezToro/gestion-candidaturas-cli/commit/1e2113f37e1cec8e1d040be781a97d9bdbe0850a) | Repositorio JDBC de ofertas, consultas con `JOIN` y filtros combinables. |
| [`c847aa5`](https://github.com/OscarRamirezToro/gestion-candidaturas-cli/commit/c847aa5037cf1487ef9d2596198f9108d553ff46) | Servicio de ofertas e importación del CSV con una transacción para empresas y ofertas. |
| [`2e703f2`](https://github.com/OscarRamirezToro/gestion-candidaturas-cli/commit/2e703f25422f25a913d03cb0a27923c848bcbc57) | Pantalla principal en JavaFX/FXML, estilos CSS y listado con `TableView`. |
| [`7e422ee`](https://github.com/OscarRamirezToro/gestion-candidaturas-cli/commit/7e422eeeeb9a8ade525f0a9389eaab390b2f4cbf) | Ventana de detalle con los datos de la empresa y el historial de entrevistas de la oferta. |
| [`e8169a6`](https://github.com/OscarRamirezToro/gestion-candidaturas-cli/commit/e8169a6d0a9c9a1d8cee15bd2175e3b9fd0d9908) | Formulario de alta y edición de ofertas con validación de campos. |

## Aportación de Juan Antonio Moreno Hevilla a la v2.0

| Módulo | Aportación |
| --- | --- |
| Empresas | Listado con búsqueda por nombre o sector y formulario de alta y edición. Se pueden borrar las empresas sin candidaturas vinculadas. |
| Entrevistas | Alta, edición y borrado desde el listado general y desde el detalle de una candidatura. Al cerrar la ventana, el detalle actualiza el historial. |
| Integración | Vistas FXML, validación de formularios y repositorios JDBC. Los módulos se abren desde el menú de la aplicación. |

[Cambios de Empresas y Entrevistas](https://github.com/juananmh-ui/gestion-candidaturas-cli/commit/0b18800) · [Plan de desarrollo asistido](docs/plan-desarrollo-v2.md)

## Ejecutar la v2.0

1. Abre el proyecto Maven en IntelliJ IDEA con un JDK 17 o superior.
2. Ejecuta [schema.sql](src/main/resources/com/dam/jobtrack/schema.sql) en MySQL.
3. En `src/main/resources/com/dam/jobtrack/`, copia `config.properties.example` como `config.properties`. Configura la URL, el usuario y la contraseña locales. Guarda el archivo como UTF-8.
4. En la Terminal integrada de IntelliJ, desde la raíz del proyecto, ejecuta `mvn javafx:run`.
5. En el listado de ofertas, pulsa **Importar CSV** y selecciona [candidaturas_externas.csv](candidaturas_externas.csv). Guarda los CSV que prepares como UTF-8 para conservar caracteres como la ñ y las tildes.

El [prototipo de JobTrack de la v1.0](https://www.figma.com/design/jGDqg0K7VzvOMhwHnP2V9z/Gesti%C3%B3n-de-Candidaturas---Prototipo-v1.0?node-id=3-4) sirvió de referencia para las pantallas.

## Rama de trabajo de la v3.0

La rama `codex/v3-estadisticas-contactos` incorpora tres aportaciones asociadas a Juan Antonio Moreno Hevilla. La primera lleva a Hibernate y JPQL las consultas de estadísticas. La segunda muestra los recuentos y la tasa de éxito en un panel JavaFX. La tercera incorpora el alta, la edición y el borrado de contactos vinculados a empresas.

Para probar esta rama, usa un JDK 24 o superior y sigue la configuración de MySQL de la v2.0. Ejecuta también [schema-v3.sql](src/main/resources/com/dam/jobtrack/schema-v3.sql) sobre la base configurada. Arranca con `mvn javafx:run` y abre **Estadísticas** o **Contactos** desde el menú.

Quedan pendientes la migración del resto de operaciones a Hibernate, los vínculos directos de contactos con ofertas, las notas por candidatura y el asistente con Ollama4j.

Para estas aportaciones se utilizó un asistente de código. El encargo fue «haz los commits que me faltan» en continuidad con la v2.0. El plan fue implementar primero las consultas JPQL, conectarlas a una vista FXML y añadir después Contactos con su esquema y formulario. Las comprobaciones se hicieron en una base MySQL de prueba.

---

## Documentación de la v1.0 (CLI)

La guía de comandos que sigue corresponde a la versión anterior, con persistencia en archivos de texto. Su [código fuente está en la revisión `34dfa42`](https://github.com/OscarRamirezToro/gestion-candidaturas-cli/commit/34dfa42a68868aba5ad7ce3808d6f0744cf3fb2c).

---

## Descripción del Proyecto
Herramienta de línea de comandos (CLI) desarrollada en Java para la gestión integral de candidaturas de empleo. El sistema opera directamente sobre persistencia en ficheros de texto plano (delimitados por punto y coma `;`), ejecutando cada orden de forma atómica sin menús interactivos ni bloqueos de ejecución.

El diseño sigue estrictamente los principios **SOLID**, desacoplando la lógica de dominio, la persistencia de datos y la interfaz de usuario por consola.

---

## Requisitos de Ejecución
- **Java JDK:** 17 o superior.
- **IDE recomendado:** IntelliJ IDEA.
- **Formato del archivo de datos:** `id;titulo;empresa;estado;fecha` (fecha en formato `dd/MM/yyyy`).

---

## Guía de Comandos

Cada comando se invoca pasando los argumentos correspondientes en la ejecución:

### 1. Listar todas las candidaturas
```
java -jar app.jar listar
```
### 2. Listar con Filtros (estado y/o empresa)
```
java -jar app.jar listar --estado "En proceso" --empresa "Google"
java -jar app.jar listar --estado "Aceptada"
java -jar app.jar listar --empresa "Microsoft"
```
### 3. Añadir una nueva candidatura
Genera un ID incremental automáticamente y registra la candidatura en el fichero.
```
java -jar app.jar añadir --titulo "Desarrollador Java Junior" --empresa "Indra" --estado "Enviada" --fecha 15/10/2026
```
### 4. Consultar detalle de una candidatura
```
java -jar app.jar ver 13
```
### 5. Eliminar una candidatura por ID
```
java -jar app.jar borrar 13
```
### 6. Importar candidaturas desde archivo CSV
Carga registros desde un fichero externo delimitado por comas o punto y coma y los consolida en el archivo del sistema.
```
java -jar app.jar importar candidaturas_externas.csv
```
## Tratamiento de Errores

El sistema valida todas las entradas por línea de comandos y la integridad de los ficheros:

- **Argumentos incompletos o desconocidos:** Muestra mensaje descriptivo con la sintaxis correcta.
- **Registro no encontrado:** Avisa si un ID consultado o a borrar no existe en el fichero.
- **Fichero no encontrado:** Controla la ausencia del fichero CSV a importar sin exponer la traza de error (`stackTrace`).
- **Formato de datos erróneo:** Valida que las fechas sigan el patrón `dd/MM/yyyy` y los IDs sean valores numéricos válidos.
## Arquitectura y principios SOLID
- **S (Single Responsibility):** Separación nítida entre el modelo de dominio, el repositorio de acceso a archivos, el analizador de comandos (Command Parser) y el formateador de salida.
- **O (Open/Closed):** Comandos estructurados mediante el patrón Command, facilitando la adición de nuevas operaciones sin modificar las existentes.
- **L (Liskov Substitution):** Abstracciones e interfaces claras para el repositorio y la ejecución de comandos.
- **I (Interface Segregation):** Interfaces específicas y acotadas según las necesidades de cada componente.
- **D (Dependency Inversion):** Los servicios de alto nivel dependen de abstracciones (interfaces de repositorio y vista), no de implementaciones concretas.
