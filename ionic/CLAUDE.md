# JuegAlmi - app Ionic (parte C)

Proyecto: tienda de videojuegos, moviles, tablets y consolas (reto DAM2). Esta carpeta es la app Ionic 8 + Angular 22
(componentes standalone) + Capacitor. Hay otra app Android nativa que consume la MISMA API: el diseno debe ser identico.

## Reglas
- Codigo simple, comentado en espanol, nombres de funciones en espanol. Es un trabajo de alumnos: nada de arquitectura compleja.
- Antes de instalar paquetes npm o borrar archivos, preguntame.
- No hagas git commit/push, ni toques .env, claves ni nada fuera de esta carpeta.
- Despues de cada tarea ejecuta `npx ng build` y arregla los errores. Dime que cambiaste en 3-5 lineas.
- Si algo del contrato de la API no esta claro, lee `docs/ApiController.php` (es el controlador real del backend) en vez de adivinar.
- Esta app no usa zone.js: todo lo que cambie tras una llamada a la API (datos, cargando, error, filtros) debe ser un signal() y leerse con paréntesis en la plantilla. Usa computed() para listas filtradas.
- Si una foto falla, usa api.fotoRota($event) en el (error) de la imagen.

## API (backend Symfony)
- URL base: ver `src/environments/environment.ts` (campo `apiUrl`, sin barra final). Existe `mock: boolean` para trabajar sin API.
- Todas las rutas empiezan por /api. /api/auth/* es publico; el resto exige `Authorization: Bearer <token>` (ya lo anade `src/app/auth.interceptor.ts`).
- /api/admin/* solo ROLE_ADMIN (403 si no). Sin token -> 401: hay que llevar al usuario al login.
- Errores: JSON `{"error": "mensaje"}`; mostrar ese mensaje al usuario (toast).
- Rutas:
  - POST /auth/login {login, password} -> {token, usuario{..., roles}}   (guardar token y roles en localStorage)
  - POST /auth/register
  - GET /productos/dispositivos?tipo=telefono|tablet ; GET /productos/videojuegos ; GET /productos/consolas
  - POST /operaciones/comprar ; POST /operaciones/alquilar ; PUT /operaciones/alquileres/{id}/devolver ; GET /operaciones/mis-operaciones
  - GET /soporte/dispositivos (mis aparatos) ; POST /soporte/dispositivos {marca_id, departamento_id, modelo}
  - POST /soporte/solicitudes {dispositivo_id, tienda_id, descripcion, latitud, longitud, foto_base64?} ; PUT/DELETE /soporte/solicitudes/{id}
  - GET /admin/solicitudes?estado= ; PUT /admin/solicitudes/{id}/aceptar {precio_estimado} ; PUT /admin/solicitudes/{id}/rechazar {motivo}
  - PUT /admin/reparaciones/{id} {estado: en_proceso|completada, precio_final}
  - GET /info/tiendas ; GET /info/departamentos ; GET /info/marcas (marcas fabricantes, "Otra" al final)
- Latitud y longitud son obligatorias en compras, alquileres y solicitudes: si el usuario niega el GPS, mandar 0.
- La foto de la camara se manda en base64 (campo `foto_base64`), calidad ~60-70%.

## Diseno (identico a Android)
Tema oscuro azul #0A0F22, acento coral #FF5A36, secundario ambar #FFC53D. Esta en `src/theme/juegalmi.scss`.
Menu: pestanas Videojuegos, Moviles (telefonos+tablets), Consolas, Soporte. Mantener `provideIonicAngular({mode:'md'})`.

## Requisitos de la rubrica que hay que cumplir
Camara (Capacitor Camera), GPS (Capacitor Geolocation), carga asincrona con spinner, animaciones sencillas, token en todas las peticiones, APK generable.

## Tareas, en este orden (una cada vez, pido confirmacion al acabar cada una)
1. Crear `src/app/api.service.ts` (HttpClient) con un metodo por ruta de arriba. Que respete `environment.mock` solo en login.
2. Login real: usar api.service, guardar token+roles, `authGuard` que redirige a /login sin token, cerrar sesion, tratar el 401 en el interceptor.
3. Que Videojuegos y Moviles lean de la API (spinner mientras carga, mensaje si falla) en vez de datos fijos.
4. Pagina Consolas (ruta, pestana y lista desde /productos/consolas), con el mismo estilo que Moviles.
5. Soporte: "Mis aparatos" (listar + anadir con desplegable de marcas y departamentos) y "Pedir reparacion" (elegir aparato, tienda que ofrezca ese departamento, descripcion, foto con camara, GPS) y "Mis solicitudes" (editar descripcion / cancelar si estan pendientes).
6. Pantalla admin (solo si roles incluye ROLE_ADMIN): lista de solicitudes con filtro por estado, aceptar con precio, rechazar con motivo, y marcar reparacion completada con precio final.


