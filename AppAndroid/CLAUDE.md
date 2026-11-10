# JuegAlmi - app Android nativa (Java)

Proyecto Android (Java, XML, Fragments, Retrofit, Material). Es la version nativa de una tienda de videojuegos, moviles,
tablets y consolas (reto DAM2). Existe una app Ionic YA TERMINADA que consume la MISMA API: el diseno y el comportamiento
de esta app deben ser IDENTICOS a los de Ionic. La referencia esta en `docs/ionic_referencia/` (copia de `src/app` y
`src/theme/juegalmi.scss` de la app Ionic). Mira ahi como se ve y que hace cada pantalla.

## Reglas
- Java + XML + Fragments, como ya esta hecho. No pases a Kotlin ni a Compose. Codigo SIMPLE (es de alumnos), comentado en
  espanol, nombres de metodos en espanol en lo nuevo. Reutiliza lo que ya existe (ApiService, SharedPrefManager, colores, estilos).
- Antes de anadir una dependencia en build.gradle.kts, preguntame. No borres nada sin decirmelo (salvo las carpetas de la Tarea 0).
- No hagas git commit/push. No toques nada fuera de esta carpeta.
- Despues de cada tarea ejecuta `./gradlew assembleDebug` y arregla los errores. Dime que cambiaste en 3-5 lineas.
- Si algo de la API no esta claro, lee `docs/ApiController.php` (controlador real del backend).
- Codigo FACIL DE ENTENDER, no corto. Es un proyecto de alumnos de DAM que tienen que explicarlo en una defensa. Reglas:
  * Metodos pequenos con una sola tarea y nombres claros en espanol (ej. cargarTiendas, mostrarError, pedirPermisoUbicacion).
  * Nada de lambdas encadenadas, genericos raros ni trucos: prefiere un codigo largo y claro a uno corto e ingenioso.
  * Variables con nombres completos (nada de a, b, x). Un comentario corto en espanol antes de cada metodo diciendo que hace.
  * Si algo se puede hacer con lo que ya hay en el proyecto (ApiService, SharedPrefManager, adaptadores), reutilizalo.
  * Una clase por pantalla; la logica de red en el Fragment con enqueue/Callback, como ya esta hecho.

## API
- URL base: `RetrofitClient.BASE_URL` = https://juegalmi-shop.duckdns.org/api/ (HTTPS). Todas las rutas menos /auth/* exigen
  `Authorization: Bearer <token>` (ya lo anade RetrofitClient). /admin/* solo para ROLE_ADMIN (403 si no). Sin token -> 401.
- Errores: JSON `{"error": "mensaje"}`. Mostrar ese mensaje al usuario (Toast o Snackbar).
- ApiService.java ya tiene todas las rutas que usa la app (se quito `editarSolicitud`: ya no se edita la descripcion). Latitud y longitud son obligatorias en comprar, alquilar y solicitudes: si no hay
  permiso o posicion, mandar 0 y 0.
- Limites: descripcion de solicitud max 255, modelo de aparato max 100, nombre de usuario max 180.
- /info/tiendas devuelve por tienda: id, nombre, direccion, historia, latitud, longitud, fotos (lista de URLs) y
  departamentos [{id, nombre}]. Los departamentos son Consolas, Moviles y Tablets (los videojuegos no se reparan).
- La foto de la camara se manda en base64 en `foto_base64` (JPEG, calidad ~65).

## Diseno
Colores ya definidos en colors.xml (fondo #0A0F22, superficie #151E3A, acento coral #FF5A36, ambar #FFC53D). Menu lateral:
Servicios, Ubicacion, Galeria, Historia, Mis operaciones, (Administracion solo admin), Cerrar sesion. Barra inferior
(mismo orden que Ionic): Dispositivos, Consolas, Videojuegos, Soporte. Compara con docs/ionic_referencia y deja el aspecto igual.

## Decisiones tomadas (confirmadas)
- Tema oscuro fijo (Theme.MaterialComponents.NoActionBar), como Ionic (color-scheme: dark). Dialogos: fondo #131A33, titulo
  blanco, texto #C9D1EA, botones ambar (ThemeOverlay.App.Dialogo). Spinner con estilo global Widget.App.Spinner.
- Solo los videojuegos se alquilan; consolas, moviles y tablets solo se compran (MobileAdapter.esAlquilable).
- Iconos de la barra: Videojuegos = mando (ic_mando), Consolas = tele (ic_tv). ic_videogame.xml se deja aunque no se usa.
- Mis operaciones tiene 3 pestanas: Compras, Alquileres y Mis solicitudes (las reparaciones). Soporte solo tiene Mis aparatos
  y Pedir reparacion. Las solicitudes no se editan; las pendientes se pueden cancelar.
- Mapa (osmdroid): teselas de Esri World Street Map (api/FuenteTeselasEsri, XYTileSource con getTileURLString propio:
  orden zoom/y/x y sin extension; zoom 0-19, 256 px). Descartados: tile.openstreetmap.org (403 "Access blocked" incluso
  con user agent propio) y Carto (pide API key). Debajo de cada mapa: "Tiles © Esri".
  User agent propio "JuegAlmi-Android/1.0" (UbicacionFragment.USER_AGENT_MAPA), puesto DESPUES de Configuration.load().
- Ubicacion: si a los 5 s no llega posicion nueva se usa la ultima conocida y, si no hay, 0 y 0.
- Recarga con boton Recargar (sin "tirar para recargar": necesitaria la dependencia swiperefreshlayout).
- Dependencias anadidas (aprobadas): osmdroid-android 6.1.20 y glide 4.16.0.
- Ayudantes en api/: LocationHelper, FotoHelper (camara), PrecioHelper (formato de precios), RetrofitClient.construirUrlFoto
  y RetrofitClient.leerMensajeError.
- Compilar en este equipo: JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr" (no esta en el PATH).

## Estado actual (revisado tras la Tarea 10)
HECHAS las tareas 0 a 10. Compila con `./gradlew assembleDebug`; APK en `app/build/outputs/apk/debug/app-debug.apk`.
PENDIENTE: probar en un movil real (nada se ha probado en dispositivo ni contra la API). Lo de mas riesgo: camara (foto
derecha y enviada), permiso de ubicacion la primera vez, mapa sin "Access blocked", 401 -> login, Administracion con un
usuario admin (cerrar sesion y volver a entrar para que se guarden los roles).

## Tareas (todas HECHAS; se dejan como referencia de lo que se pidio)
0. [HECHA] Limpieza: borra `app/src/main/res/drawable/JuegAlmi-ionic-pruebas/` (es una copia de la app Ionic metida por error y rompe
   el build) y confirma que `./gradlew assembleDebug` pasa. Pon `app_name` = "JuegAlmi".
1. [HECHA] Guardar los roles del usuario en SharedPrefManager al hacer login (metodo `esAdmin()`), y tratar el 401 (cerrar sesion y
   volver al login) en el interceptor de RetrofitClient.
2. [HECHA] Pantalla de Registro (`POST auth/register` con nombre_usuario, email, password; no devuelve token: hacer login despues).
   Mismo diseno que el login, con validacion (usuario max 180, email valido, password minimo 6 y repetirla).
3. [HECHA] Modelo Tienda completo (historia, latitud, longitud, fotos, departamentos) y tres pantallas separadas: Historia, Ubicacion
   y Galeria, abiertas desde las entradas del menu lateral (Servicios abre el catalogo). Cada una muestra solo lo suyo, por
   cada tienda. Ubicacion: mapa con la tienda marcada (usa osmdroid con OpenStreetMap, sin clave; preguntame antes de anadir
   la dependencia), distancia desde el usuario y boton "Como llegar" (intent geo: / Google Maps). Galeria: fotos con
   desplazamiento horizontal. Con carga, error + reintentar y recarga.
4. [HECHA] Arreglar LocationHelper: tras conceder el permiso debe continuar con la accion (ahora la primera vez no hace nada). Limite
   de espera de 5 s y aviso "Obteniendo ubicacion...". Si no hay posicion, 0 y 0.
5. [HECHA] Camara: ahora se usa la miniatura (`extras.get("data")`), que sale diminuta. Usa foto a tamano completo con
   ActivityResultContracts.TakePicture + FileProvider, reduce a max 1280 px de ancho y comprime a JPEG 65 antes de pasar a base64.
6. [HECHA] Imagenes de productos: cargarlas desde `foto_url` (Glide; preguntame antes de anadirlo) con icono de respaldo si fallan.
   En Mis operaciones, las de `url_foto`.
7. [HECHA] Pantalla de Administracion (solo si esAdmin): solicitudes por estado (pendiente/aceptada/rechazada) con aparato, tienda,
   cliente, descripcion, foto y ubicacion; aceptar (pide precio estimado > 0), rechazar (pide motivo) y marcar la reparacion
   completada (pide precio final >= 0). El GET trae reparacion_id, reparacion_estado, precio_estimado y precio_final.
8. [HECHA] Soporte: filtrar las tiendas del desplegable segun los departamentos del aparato elegido; maxlength 255 en la descripcion y
   100 en el modelo, con contador; en Mis operaciones el alquiler "pendiente" tiene boton Cancelar (mismo PUT devolver) y el
   "activo" Devolver.
9. [HECHA] Repaso de diseno: pantalla por pantalla contra la app Ionic (colores, textos, iconos, orden del menu, estados de carga y
   error). Animacion sencilla de aparicion en las listas. Arreglar contrastes de dialogos oscuros.
10. [HECHA] APK de depuracion final y lista de lo que no se ha podido probar.
