# Clinic Manager — Sistema de Gestión Clínica (Versión Base de Datos MariaDB)

Este proyecto es una reconstrucción moderna, interactiva y basada en bases de datos relacionales del sistema original **Clinic Manager**. Reemplaza el almacenamiento en archivos serializados (`.dat`) por un motor de base de datos **SQL (MariaDB)** y sustituye la interfaz gráfica heredada de Swing por una aplicación web de página única (SPA) con un diseño visual moderno, responsivo y de alta gama.

---

## 🚀 Características Clave y Mejoras Modernas

1. **Persistencia Relacional (SQL MariaDB)**:
   * Migración completa de archivos `.dat` a la base de datos relacional del curso: **MariaDB 11.4** (ejecutándose localmente como servicio).
   * Uso de **Spring Data JPA** e **Hibernate** para el mapeo objeto-relacional (ORM) en Java.
   * Conexión directa a través del cliente oficial de MariaDB (`mariadb-java-client`) con autocreación automática de base de datos (`createDatabaseIfNotExist=true`).

2. **Arquitectura desacoplada**:
   * **Backend**: REST API construida en **Java 17 (Spring Boot 3.2.5)**. Estructurada bajo el patrón MVC (Modelos, Repositorios, Controladores).
   * **Frontend**: Aplicación de Página Única (SPA) construida en HTML5, CSS3 moderno (Vanilla) y JavaScript (ES6). Se sirve estáticamente a través de Spring Boot en `http://localhost:8080`.

3. **Diseño Visual de Alta Gama**:
   * Estética premium oscura con detalles en degradados y acentos índigo y esmeralda.
   * Efectos de **glassmorphism** (fondos translúcidos con desenfoque de fondo).
   * Tipografía moderna **Inter** de Google Fonts.
   * Iconografía minimalista mediante **Lucide Icons**.
   * Animaciones sutiles y transiciones fluidas al navegar entre paneles o abrir ventanas modales.

4. **Reglas de Negocio Automatizadas**:
   * **Verificación de Citas**: Al seleccionar una fecha para una cita, el sistema consulta dinámicamente qué médicos tienen cupo disponible según su propiedad `maxCitas` diarias.
   * **Esquema de Vacunas**: Permite registrar el catálogo de vacunas del centro y aplicarlas de forma interactiva a cada paciente, evitando duplicados.
   * **Cuentas de Acceso Automáticas**: Al registrar un nuevo Médico en la administración, el sistema le crea automáticamente una cuenta de usuario con credenciales temporales vinculadas a su expediente.

5. **Respaldos de Base de Datos mediante Volcados SQL**:
   * Reemplazo de los respaldos binarios serializados por volcados de datos SQL de producción.
   * Integración nativa en el panel de Base de Datos para ejecutar copias de seguridad (`mariadb-dump`) y restauraciones completas en caliente utilizando las herramientas de MariaDB.

---

## 🛠️ Herramientas Modernas para Ejecutar la Aplicación

Para ejecutar y desarrollar este proyecto de forma moderna y profesional, se recomiendan las siguientes herramientas:

### 1. Entorno de Desarrollo (IDE)
* **Visual Studio Code (Recomendado)**:
  * Instale el paquete de extensiones **Extension Pack for Java** (de Microsoft) y **Spring Boot Extension Pack**.
  * Al abrir la carpeta `POO` en VS Code, las extensiones configurarán y descargarán automáticamente el compilador de Java si no lo tiene.
* **IntelliJ IDEA Community Edition**:
  * Es el estándar de oro de la industria para desarrollo en Java. Detecta el archivo `pom.xml`, descarga las dependencias de Maven y configura el JDK 17+ con un solo clic.

### 2. Ejecución Automatizada (Script de Inicio)
Dado que su sistema no posee Java o Maven configurado en la variable de entorno global `PATH`, este proyecto incluye un script automatizado para PowerShell:

* **[run.ps1](run.ps1)**:
  1. Verifica si Java está en su sistema. Si no, **descarga automáticamente un JDK 17 portable** (Eclipse Temurin) en la carpeta local `.tools/jdk`.
  2. Verifica si Maven está disponible. Si no, **descarga un Maven portable** en la carpeta `.tools/maven`.
  3. Agrega temporalmente estos binarios a la sesión activa y ejecuta `mvn clean spring-boot:run`.
  4. Todo el proceso es autónomo y no altera las variables de entorno permanentes de su sistema.

---

## ⚡ Guía de Inicio Rápido

1. Asegúrate de tener el servicio local de **MariaDB** ejecutándose en el puerto por defecto (3306).
2. Abra una terminal de **PowerShell** en la carpeta del proyecto:
   ```powershell
   cd "[Carpeta Raíz del Proyecto]"
   ```
3. Ejecute el script de inicio:
   ```powershell
   .\run.ps1
   ```
   *(Nota: La primera ejecución tomará unos minutos mientras descarga el JDK, Maven y las dependencias del proyecto. Las ejecuciones posteriores serán casi instantáneas).*
4. Abra su navegador web e ingrese a:
   * **Aplicación**: [http://localhost:8080](http://localhost:8080)

### 🔑 Credenciales de Acceso por Defecto
* **Administrador**:
  * **Usuario**: `Administrador`
  * **Contraseña**: `123456`
* **Secretaria**:
  * **Usuario**: `Secretaria`
  * **Contraseña**: `123456`

### 🗃️ Configuración de Conexión a MariaDB
La aplicación está preconfigurada en [application.properties](src/main/resources/application.properties) con las credenciales de tu sistema:
* **Host / Puerto**: `localhost:3306`
* **Base de datos**: `clinicdb` (creada automáticamente al arrancar si no existe)
* **Usuario**: `root`
* **Contraseña**: `Admin1234!`
