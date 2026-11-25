Induztek Terreno
Aplicación Android para registrar mediciones en terreno, consultar equipos e instrumentos y gestionar protocolos y actas de entrega. Proyecto de la asignatura DSY1105 de Duoc UC.

Funcionalidades
Técnico: registra y corrige mediciones, y entrega el acta.
Ingeniero: revisa y formaliza protocolos.
Jefe Técnico: administra los catálogos de equipos, instalaciones e instrumentos.
Cliente: consulta sus equipos y su historial de pruebas.

Arquitectura
La app Android se conecta a un API Gateway que enruta las solicitudes a cuatro microservicios Spring Boot: Usuarios, Equipos e Instalaciones, Instrumentos y Protocolos. El backend usa Eureka para el descubrimiento de servicios, MySQL para la persistencia y JWT para la autenticación.

Ejecución local
Con Docker Desktop iniciado y el archivo .env.defensa configurado, ejecuta:

    docker compose --env-file .env.defensa -p induztek-defensa up -d --wait

El Gateway queda disponible en http://localhost:8080. El APK académico usa http://10.0.2.2:8080 desde el emulador Android.

Consulta la [guía de ejecución](Documentacion/GUIA_DE_EJECUCION.md) para preparar el entorno o instalar el APK.

INDUZTEK TERRENO

El ZIP contiene los dos proyectos con los cambios locales y sus pruebas. Conserva las dos carpetas al extraerlo.

1. Abre Docker Desktop con contenedores Linux. Desde PowerShell en induztek-backend ejecuta:

   docker compose --env-file .env.defensa -p induztek-compartido up -d --build --wait

El archivo .env.defensa ya tiene las credenciales y secretos de este entorno de demostración. La primera compilación puede descargar dependencias y requiere acceso a internet. Cuando el backend esté listo, http://localhost:8080/actuator/health mostrará el estado UP. Usa un nombre de proyecto que no tenga un volumen MySQL anterior con otras credenciales.

2. Para trabajar con el código, abre APPMOVILES_PROTOCOLOS2 en Android Studio. Sincroniza Gradle, usa la variante academicoDebug y arráncala en un emulador Android API 37. También puedes arrastrar el APK académico firmado incluido a un emulador compatible. Si ya instalaste una variante con otra firma, usa un emulador separado para conservar los datos.

3. El APK académico se conecta a http://10.0.2.2:8080/. Concede el permiso de red local si aparece.

CUENTAS PARA UNA BASE NUEVA

Técnico de terreno   terreno@gmail.com
Segunda técnica      terreno2@gmail.com
Ingeniero            ingeniero@gmail.com
Jefe Técnico         jefe@gmail.com
Cliente              cliente@gmail.com

Clave temporal para las cinco cuentas: clave
La app pedirá cambiarla al entrar. Para activar un cliente nuevo, la clave temporal es Induztek2026!.



Para detener el backend conservando sus datos:

   docker compose --env-file .env.defensa -p induztek-compartido stop