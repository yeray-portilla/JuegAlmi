<?php

namespace App\Controller\Api;

use App\Entity\Alquiler;
use App\Entity\Compra;
use App\Entity\Consola;
use App\Entity\Departamento;
use App\Entity\DispositivoCliente;
use App\Entity\LineaAlquiler;
use App\Entity\LineaCompra;
use App\Entity\Marca;
use App\Entity\PeticionReparacion;
use App\Entity\Producto;
use App\Entity\Reparacion;
use App\Entity\Tablet;
use App\Entity\Telefono;
use App\Entity\Tienda;
use App\Entity\TiendaDepartamento;
use App\Entity\Usuario;
use App\Entity\Videojuego;
use App\Exception\ApiException;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;

/**
 * API de JuegAlmi (version sencilla, v2: reparaciones con solicitudes y administrador).
 *
 * CAMBIOS respecto a la version anterior:
 *   - Ya no se repara un "producto" del catalogo: el cliente registra SU aparato (DispositivoCliente).
 *   - El cliente envia una SOLICITUD (PeticionReparacion). Un admin la acepta o la rechaza.
 *     Al aceptarla se crea automaticamente la REPARACION.
 *   - Desaparece la constante de precios: el precio estimado lo pone el admin al aceptar.
 *   - La marca de un producto es ahora una relacion con la tabla Marca (opcional): fabricante en aparatos,
 *     editora en videojuegos. Sin marca: "Indie" (videojuego) o "Por definir" (otro producto).
 *   - El login devuelve tambien los roles, para que la app sepa si es administrador.
 *
 * Rutas (todas empiezan por /api):
 *   --- Usuarios ---
 *   POST   /auth/login                              iniciar sesion
 *   POST   /auth/register                           crear usuario
 *   --- Catalogo ---
 *   GET    /productos/dispositivos?tipo=telefono    telefonos y tablets (tipo opcional: telefono o tablet)
 *   GET    /productos/videojuegos
 *   GET    /productos/consolas
 *   --- Compras y alquileres ---
 *   POST   /operaciones/comprar
 *   POST   /operaciones/alquilar
 *   PUT    /operaciones/alquileres/{id}/devolver
 *   GET    /operaciones/mis-operaciones             compras, alquileres y reparaciones del usuario
 *   --- Reparaciones: usuario ---
 *   GET    /soporte/dispositivos                    mis aparatos
 *   POST   /soporte/dispositivos                    registrar un aparato nuevo
 *   POST   /soporte/solicitudes                     pedir una reparacion
 *   PUT    /soporte/solicitudes/{id}                cambiar la descripcion (solo si esta pendiente)
 *   DELETE /soporte/solicitudes/{id}                cancelar la solicitud (solo si esta pendiente)
 *   --- Reparaciones: administrador (rol ROLE_ADMIN) ---
 *   GET    /admin/solicitudes?estado=pendiente      ver solicitudes
 *   PUT    /admin/solicitudes/{id}/aceptar          aceptar (crea la reparacion)
 *   PUT    /admin/solicitudes/{id}/rechazar         rechazar (con motivo)
 *   PUT    /admin/reparaciones/{id}                 cambiar estado y precio final
 *   --- Informacion ---
 *   GET    /info/tiendas
 *   GET    /info/departamentos
 *   GET    /info/marcas
 *
 * Estados:
 *   alquiler:    pendiente -> activo -> finalizado
 *   solicitud:   pendiente -> aceptada | rechazada
 *   reparacion:  en_proceso -> completada
 */
#[Route('/api')]
final class ApiController extends AbstractController
{
    // Tamano maximo de las fotos: 5 MB.
    private const MAX_FOTO = 5242880;

    private EntityManagerInterface $em;
    private UserPasswordHasherInterface $hasher;

    public function __construct(EntityManagerInterface $em, UserPasswordHasherInterface $hasher)
    {
        $this->em = $em;
        $this->hasher = $hasher;
    }

    // =====================================================================
    // 1. USUARIOS
    // =====================================================================

    #[Route('/auth/login', methods: ['POST'])]
    public function iniciarSesion(Request $request): JsonResponse
    {
        $datos = $this->leerJson($request);
        $login = $datos['login'] ?? null;       // puede ser el email o el nombre de usuario
        $password = $datos['password'] ?? null;

        if (!is_string($login) || !is_string($password)) {
            throw new ApiException('Debes enviar login y password.', 400);
        }

        // Buscamos primero por email y, si no aparece, por nombre de usuario.
        $repositorio = $this->em->getRepository(Usuario::class);
        $usuario = $repositorio->findOneBy(['email' => $login]);
        if ($usuario === null) {
            $usuario = $repositorio->findOneBy(['nombreUsuario' => $login]);
        }

        if ($usuario === null || !$this->hasher->isPasswordValid($usuario, $password)) {
            throw new ApiException('Credenciales incorrectas.', 401);
        }

        // Token nuevo SOLO si no tiene o ya ha caducado (asi Android e Ionic pueden usar el mismo usuario a la vez).
        $hoy = new \DateTime('today');
        $caducaEl = $usuario->getExpiracionToken();
        if ($usuario->getApiToken() === null || $caducaEl === null || $caducaEl <= $hoy) {
            $usuario->setApiToken(bin2hex(random_bytes(32)));
            $usuario->setExpiracionToken(new \DateTime('+30 days'));
            $this->em->flush();
        }

        return $this->json([
            'token' => $usuario->getApiToken(),
            'usuario' => $this->datosUsuario($usuario),
        ]);
    }

    #[Route('/auth/register', methods: ['POST'])]
    public function registrarUsuario(Request $request): JsonResponse
    {
        $datos = $this->leerJson($request);
        $nombre = $datos['nombre_usuario'] ?? null;
        $email = $datos['email'] ?? null;
        $password = $datos['password'] ?? null;

        if (!is_string($nombre) || trim($nombre) === '' || mb_strlen($nombre) > 180) {
            throw new ApiException('El nombre de usuario es obligatorio y no puede pasar de 180 caracteres.', 400);
        }
        $nombre = trim($nombre);
        if (!is_string($email) || mb_strlen($email) > 255 || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
            throw new ApiException('El email no es valido.', 400);
        }
        if (!is_string($password) || strlen($password) < 6) {
            throw new ApiException('La contrasena debe tener al menos 6 caracteres.', 400);
        }

        $repositorio = $this->em->getRepository(Usuario::class);
        if ($repositorio->findOneBy(['nombreUsuario' => $nombre]) !== null
            || $repositorio->findOneBy(['email' => $email]) !== null) {
            throw new ApiException('El nombre de usuario o el email ya existe.', 409);
        }

        $usuario = new Usuario();
        $usuario->setNombreUsuario($nombre);
        $usuario->setEmail($email);
        $usuario->setRoles(['ROLE_USER']);
        $usuario->setPassword($this->hasher->hashPassword($usuario, $password));

        $this->em->persist($usuario);
        $this->em->flush();

        return $this->json($this->datosUsuario($usuario), 201);
    }

    // =====================================================================
    // 2. CATALOGO (productos que vende la tienda)
    // =====================================================================

    #[Route('/productos/dispositivos', methods: ['GET'])]
    public function listarDispositivos(Request $request): JsonResponse
    {
        $tipo = $request->query->get('tipo');   // opcional: "telefono" o "tablet"

        $telefonos = $this->em->getRepository(Telefono::class)->findAll();
        $tablets = $this->em->getRepository(Tablet::class)->findAll();

        if ($tipo === 'telefono') {
            $productos = $telefonos;
        } elseif ($tipo === 'tablet') {
            $productos = $tablets;
        } elseif ($tipo === null || $tipo === '') {
            $productos = array_merge($telefonos, $tablets);
        } else {
            throw new ApiException('El tipo debe ser telefono o tablet.', 400);
        }

        $resultado = [];
        foreach ($productos as $producto) {
            $item = $this->datosProducto($producto);
            $item['modelo'] = $producto->getModelo();
            $item['pulgadas'] = (float) $producto->getPulgadas();
            $item['almacenamiento'] = $producto->getAlmacenamiento();
            if ($producto instanceof Telefono) {
                $item['tipo'] = 'telefono';
            } else {
                $item['tipo'] = 'tablet';
            }
            $resultado[] = $item;
        }

        return $this->json($resultado);
    }

    #[Route('/productos/videojuegos', methods: ['GET'])]
    public function listarVideojuegos(): JsonResponse
    {
        $videojuegos = $this->em->getRepository(Videojuego::class)->findAll();

        $resultado = [];
        foreach ($videojuegos as $videojuego) {
            $item = $this->datosProducto($videojuego);
            $item['precio_diario'] = (float) $videojuego->getPrecioDiario();
            $item['precio_semanal'] = (float) $videojuego->getPrecioSemanal();
            $item['precio_mensual'] = (float) $videojuego->getPrecioMensual();
            $item['copias_alquiler'] = $videojuego->getCopiasAlquiler();
            $resultado[] = $item;
        }

        return $this->json($resultado);
    }

    #[Route('/productos/consolas', methods: ['GET'])]
    public function listarConsolas(): JsonResponse
    {
        $consolas = $this->em->getRepository(Consola::class)->findAll();

        $resultado = [];
        foreach ($consolas as $consola) {
            $item = $this->datosProducto($consola);
            $item['modelo'] = $consola->getModelo();
            $item['almacenamiento'] = $consola->getAlmacenamiento();
            $resultado[] = $item;
        }

        return $this->json($resultado);
    }

    // =====================================================================
    // 3. COMPRAS Y ALQUILERES
    // =====================================================================

    /**
     * { "latitud": 41.38, "longitud": 2.17,
     *   "productos": [ {"id": 15, "cantidad": 1}, {"id": 9, "cantidad": 2} ] }
     */
    #[Route('/operaciones/comprar', methods: ['POST'])]
    public function comprar(Request $request): JsonResponse
    {
        $usuario = $this->usuarioActual();
        $datos = $this->leerJson($request);

        $lista = $datos['productos'] ?? null;
        if (!is_array($lista) || count($lista) === 0) {
            throw new ApiException('Debes indicar al menos un producto.', 400);
        }
        $latitud = $this->leerNumero($datos, 'latitud');
        $longitud = $this->leerNumero($datos, 'longitud');

        // PASO 1: revisar la lista y sumar las cantidades si un producto viene repetido.
        $cantidades = [];   // clave = id del producto, valor = unidades
        foreach ($lista as $linea) {
            if (!is_array($linea) || !isset($linea['id']) || !isset($linea['cantidad'])) {
                throw new ApiException('Cada producto necesita id y cantidad.', 400);
            }
            $id = $linea['id'];
            $cantidad = $linea['cantidad'];
            if (!is_int($id) || !is_int($cantidad) || $cantidad <= 0) {
                throw new ApiException('El id y la cantidad deben ser numeros enteros positivos.', 400);
            }
            if (isset($cantidades[$id])) {
                $cantidades[$id] = $cantidades[$id] + $cantidad;
            } else {
                $cantidades[$id] = $cantidad;
            }
        }

        // PASO 2: comprobar que existen y que hay stock. Todavia no se guarda nada.
        $productos = [];
        foreach ($cantidades as $id => $cantidad) {
            $producto = $this->em->getRepository(Producto::class)->find($id);
            if ($producto === null) {
                throw new ApiException('No existe el producto con id ' . $id . '.', 404);
            }
            if ($producto->getStock() < $cantidad) {
                throw new ApiException('No hay stock suficiente de "' . $producto->getNombre() . '".', 400);
            }
            $productos[$id] = $producto;
        }

        // PASO 3: crear la compra, sus lineas, y restar el stock.
        $compra = new Compra();
        $compra->setUsuario($usuario);
        $compra->setFechaCompra(new \DateTime());
        $compra->setLatitud($latitud);
        $compra->setLongitud($longitud);

        $total = 0.0;
        foreach ($cantidades as $id => $cantidad) {
            $producto = $productos[$id];
            $producto->setStock($producto->getStock() - $cantidad);

            $linea = new LineaCompra();
            $linea->setProducto($producto);
            $linea->setCantidad($cantidad);
            $linea->setPrecioUnidad($producto->getPrecio());   // guardamos el precio de HOY
            $compra->addLinea($linea);
            $this->em->persist($linea);

            $total = $total + ((float) $producto->getPrecio() * $cantidad);
        }

        $this->em->persist($compra);
        // flush() guarda todo de golpe y en una sola transaccion: o se guarda todo o no se guarda nada.
        $this->em->flush();

        return $this->json(['compra_id' => $compra->getId(), 'total' => round($total, 2)], 201);
    }

    /**
     * { "latitud": 41.38, "longitud": 2.17,
     *   "fecha_inicio": "2026-11-10", "fecha_fin": "2026-11-17",
     *   "videojuegos": [ {"id": 1}, {"id": 5} ] }
     */
    #[Route('/operaciones/alquilar', methods: ['POST'])]
    public function alquilar(Request $request): JsonResponse
    {
        $usuario = $this->usuarioActual();
        $datos = $this->leerJson($request);

        $fechaInicio = $this->leerFecha($datos, 'fecha_inicio');
        $fechaFin = $this->leerFecha($datos, 'fecha_fin');
        if ($fechaFin < $fechaInicio) {
            throw new ApiException('La fecha de fin no puede ser anterior a la de inicio.', 400);
        }
        $latitud = $this->leerNumero($datos, 'latitud');
        $longitud = $this->leerNumero($datos, 'longitud');

        $lista = $datos['videojuegos'] ?? null;
        if (!is_array($lista) || count($lista) === 0) {
            throw new ApiException('Debes indicar al menos un videojuego.', 400);
        }

        // PASO 1: sacar los ids (sin repetidos).
        $ids = [];
        foreach ($lista as $elemento) {
            if (!is_array($elemento) || !isset($elemento['id']) || !is_int($elemento['id'])) {
                throw new ApiException('Cada videojuego necesita un id numerico.', 400);
            }
            if (!in_array($elemento['id'], $ids, true)) {
                $ids[] = $elemento['id'];
            }
        }

        // PASO 2: comprobar que existen y que quedan copias. Todavia no se guarda nada.
        $videojuegos = [];
        foreach ($ids as $id) {
            $videojuego = $this->em->getRepository(Videojuego::class)->find($id);
            if ($videojuego === null) {
                throw new ApiException('No existe el videojuego con id ' . $id . '.', 404);
            }
            if ($videojuego->getCopiasAlquiler() < 1) {
                throw new ApiException('No quedan copias de "' . $videojuego->getNombre() . '".', 400);
            }
            $videojuegos[] = $videojuego;
        }

        // PASO 3: crear el alquiler. Si empieza en el futuro queda "pendiente", y si no, "activo".
        $alquiler = new Alquiler();
        $alquiler->setUsuario($usuario);
        $alquiler->setLatitud($latitud);
        $alquiler->setLongitud($longitud);
        $alquiler->setFechaInicio($fechaInicio);
        $alquiler->setFechaFin($fechaFin);
        if ($fechaInicio > new \DateTime('today')) {
            $alquiler->setEstado('pendiente');
        } else {
            $alquiler->setEstado('activo');
        }

        // Dias de alquiler, contando el dia de inicio y el de fin.
        $dias = $fechaInicio->diff($fechaFin)->days + 1;

        $total = 0.0;
        foreach ($videojuegos as $videojuego) {
            $videojuego->setCopiasAlquiler($videojuego->getCopiasAlquiler() - 1);

            $precio = $this->calcularPrecioAlquiler($videojuego, $dias);

            $linea = new LineaAlquiler();
            $linea->setVideojuego($videojuego);
            $linea->setPrecioTotal(number_format($precio, 2, '.', ''));
            $alquiler->addLinea($linea);
            $this->em->persist($linea);

            $total = $total + $precio;
        }

        $this->em->persist($alquiler);
        $this->em->flush();

        return $this->json(['alquiler_id' => $alquiler->getId(), 'total' => round($total, 2)], 201);
    }

    #[Route('/operaciones/alquileres/{id}/devolver', methods: ['PUT'])]
    public function devolverAlquiler(int $id): JsonResponse
    {
        $usuario = $this->usuarioActual();

        $alquiler = $this->em->getRepository(Alquiler::class)->find($id);
        // Si no existe o es de otra persona, respondemos lo mismo: asi no se puede "curiosear".
        if ($alquiler === null || $alquiler->getUsuario()->getId() !== $usuario->getId()) {
            throw new ApiException('Alquiler no encontrado.', 404);
        }
        if ($alquiler->getEstado() === 'finalizado') {
            throw new ApiException('Este alquiler ya esta devuelto.', 409);
        }

        foreach ($alquiler->getLineas() as $linea) {
            $videojuego = $linea->getVideojuego();
            $videojuego->setCopiasAlquiler($videojuego->getCopiasAlquiler() + 1);
        }
        $alquiler->setEstado('finalizado');
        $this->em->flush();

        return $this->json(['alquiler_id' => $alquiler->getId(), 'estado' => 'finalizado']);
    }

    #[Route('/operaciones/mis-operaciones', methods: ['GET'])]
    public function misOperaciones(): JsonResponse
    {
        $usuario = $this->usuarioActual();
        $resultado = [];

        // --- Compras ---
        foreach ($usuario->getCompras() as $compra) {
            $total = 0.0;
            $productos = [];
            foreach ($compra->getLineas() as $linea) {
                $total = $total + ((float) $linea->getPrecioUnidad() * $linea->getCantidad());
                $productos[] = [
                    'nombre' => $linea->getProducto()->getNombre(),
                    'cantidad' => $linea->getCantidad(),
                    'precio_unidad' => (float) $linea->getPrecioUnidad(),
                ];
            }
            $resultado[] = [
                'tipo' => 'compra',
                'id' => $compra->getId(),
                'fecha' => $compra->getFechaCompra()->format('Y-m-d'),
                'estado' => 'completada',
                'total' => round($total, 2),
                'detalle' => $productos,
            ];
        }

        // --- Alquileres ---
        foreach ($usuario->getAlquileres() as $alquiler) {
            $total = 0.0;
            $juegos = [];
            foreach ($alquiler->getLineas() as $linea) {
                $total = $total + (float) $linea->getPrecioTotal();
                $juegos[] = ['nombre' => $linea->getVideojuego()->getNombre()];
            }
            $resultado[] = [
                'tipo' => 'alquiler',
                'id' => $alquiler->getId(),
                'fecha' => $alquiler->getFechaInicio()->format('Y-m-d'),
                'estado' => $alquiler->getEstado(),
                'total' => round($total, 2),
                'detalle' => $juegos,
            ];
        }

        // --- Reparaciones: partimos de las solicitudes del usuario ---
        $solicitudes = $this->em->getRepository(PeticionReparacion::class)->findBy(['usuario' => $usuario]);
        foreach ($solicitudes as $solicitud) {
            $estado = $solicitud->getEstado();   // pendiente o rechazada
            $total = null;
            $reparacionId = null;

            // Si la solicitud fue aceptada, mostramos el estado y el precio de la reparacion.
            $reparacion = $solicitud->getReparacion();
            if ($reparacion !== null) {
                $estado = $reparacion->getEstado();
                $reparacionId = $reparacion->getId();
                $precio = $reparacion->getPrecioFinal();
                if ($precio === null) {
                    $precio = $reparacion->getPrecioEstimado();
                }
                $total = (float) $precio;
            }

            $resultado[] = [
                'tipo' => 'reparacion',
                'id' => $solicitud->getId(),
                'solicitud_id' => $solicitud->getId(),
                'reparacion_id' => $reparacionId,
                'fecha' => $solicitud->getFecha()->format('Y-m-d'),
                'estado' => $estado,
                'total' => $total,
                'dispositivo' => $this->nombreDispositivo($solicitud->getDispositivoCliente()),
                'descripcion' => $solicitud->getDescripcion(),
                'url_foto' => $solicitud->getUrlFoto(),
                'motivo_rechazo' => $solicitud->getMotivoRechazo(),
            ];
        }

        // Ordenamos de la mas reciente a la mas antigua.
        usort($resultado, function ($a, $b) {
            return strcmp($b['fecha'], $a['fecha']);
        });

        return $this->json($resultado);
    }

    // =====================================================================
    // 4. REPARACIONES - LO QUE HACE EL USUARIO
    // =====================================================================

    // Los aparatos que el usuario ya ha registrado.
    #[Route('/soporte/dispositivos', methods: ['GET'])]
    public function listarMisDispositivos(): JsonResponse
    {
        $usuario = $this->usuarioActual();
        $dispositivos = $this->em->getRepository(DispositivoCliente::class)->findBy(['usuario' => $usuario]);

        $resultado = [];
        foreach ($dispositivos as $dispositivo) {
            $resultado[] = $this->datosDispositivo($dispositivo);
        }

        return $this->json($resultado);
    }

    /**
     * Registrar un aparato nuevo: { "marca_id": 2, "departamento_id": 2, "modelo": "Galaxy S21" }
     */
    #[Route('/soporte/dispositivos', methods: ['POST'])]
    public function crearDispositivo(Request $request): JsonResponse
    {
        $usuario = $this->usuarioActual();
        $datos = $this->leerJson($request);

        $modelo = $datos['modelo'] ?? null;
        if (!is_string($modelo) || trim($modelo) === '' || mb_strlen(trim($modelo)) > 100) {
            throw new ApiException('El modelo es obligatorio y no puede pasar de 100 caracteres.', 400);
        }
        $modelo = trim($modelo);

        $marca = $this->em->getRepository(Marca::class)->find((int) ($datos['marca_id'] ?? 0));
        if ($marca === null) {
            throw new ApiException('La marca no existe.', 404);
        }
        if (!$marca->esFabricante()) {
            throw new ApiException('Esa marca no fabrica aparatos.', 400);
        }
        $departamento = $this->em->getRepository(Departamento::class)->find((int) ($datos['departamento_id'] ?? 0));
        if ($departamento === null) {
            throw new ApiException('El departamento no existe.', 404);
        }

        // Si el usuario ya tenia ese mismo aparato, devolvemos el que ya existe (no se duplica).
        $existente = $this->em->getRepository(DispositivoCliente::class)->findOneBy([
            'usuario' => $usuario,
            'marca' => $marca,
            'departamento' => $departamento,
            'modelo' => $modelo,
        ]);
        if ($existente !== null) {
            return $this->json($this->datosDispositivo($existente), 200);
        }

        $dispositivo = new DispositivoCliente();
        $dispositivo->setUsuario($usuario);
        $dispositivo->setMarca($marca);
        $dispositivo->setDepartamento($departamento);
        $dispositivo->setModelo($modelo);
        $this->em->persist($dispositivo);
        $this->em->flush();

        return $this->json($this->datosDispositivo($dispositivo), 201);
    }

    /**
     * Se puede enviar como JSON (foto en "foto_base64") o como formulario multipart (archivo "foto").
     * Campos: dispositivo_id, tienda_id, descripcion, latitud, longitud.
     */
    #[Route('/soporte/solicitudes', methods: ['POST'])]
    public function crearSolicitud(Request $request): JsonResponse
    {
        $usuario = $this->usuarioActual();

        $contentType = (string) $request->headers->get('Content-Type');
        if (str_contains($contentType, 'json')) {
            $datos = $this->leerJson($request);
        } else {
            $datos = $request->request->all();
        }

        // --- Comprobamos cada dato ---
        $descripcion = $datos['descripcion'] ?? null;
        if (!is_string($descripcion) || trim($descripcion) === '' || mb_strlen(trim($descripcion)) > 255) {
            throw new ApiException('La descripcion es obligatoria y no puede pasar de 255 caracteres.', 400);
        }
        $latitud = $this->leerNumero($datos, 'latitud');
        $longitud = $this->leerNumero($datos, 'longitud');

        // El aparato tiene que existir y ser de este usuario.
        $dispositivo = $this->em->getRepository(DispositivoCliente::class)->find((int) ($datos['dispositivo_id'] ?? 0));
        if ($dispositivo === null || $dispositivo->getUsuario()->getId() !== $usuario->getId()) {
            throw new ApiException('Aparato no encontrado.', 404);
        }

        $tienda = $this->em->getRepository(Tienda::class)->find((int) ($datos['tienda_id'] ?? 0));
        if ($tienda === null) {
            throw new ApiException('La tienda no existe.', 404);
        }

        // La tienda elegida debe ofrecer ese tipo de reparacion (tabla tienda_departamento).
        $ofrece = $this->em->getRepository(TiendaDepartamento::class)->findOneBy([
            'tienda' => $tienda,
            'departamento' => $dispositivo->getDepartamento(),
        ]);
        if ($ofrece === null) {
            throw new ApiException('Esta tienda no repara ' . $dispositivo->getDepartamento()->getNombre() . '.', 400);
        }

        // Un aparato no puede tener dos reparaciones abiertas a la vez: pendiente, o aceptada y sin completar.
        $anteriores = $this->em->getRepository(PeticionReparacion::class)->findBy(['dispositivo_cliente' => $dispositivo]);
        foreach ($anteriores as $anterior) {
            $reparacion = $anterior->getReparacion();
            $abierta = $anterior->getEstado() === 'pendiente'
                || ($reparacion !== null && $reparacion->getEstado() !== 'completada');
            if ($abierta) {
                throw new ApiException('Este aparato ya tiene una reparacion en curso o pendiente.', 409);
            }
        }

        $urlFoto = $this->guardarFoto($request, $datos);

        // --- Creamos la solicitud (queda pendiente hasta que un admin la acepte) ---
        $solicitud = new PeticionReparacion();
        $solicitud->setUsuario($usuario);
        $solicitud->setDispositivoCliente($dispositivo);
        $solicitud->setTienda($tienda);
        $solicitud->setDescripcion(trim($descripcion));
        $solicitud->setUrlFoto($urlFoto);
        $solicitud->setLatitud($latitud);
        $solicitud->setLongitud($longitud);
        $solicitud->setFecha(new \DateTime());
        $solicitud->setEstado('pendiente');

        $this->em->persist($solicitud);
        $this->em->flush();

        return $this->json([
            'solicitud_id' => $solicitud->getId(),
            'estado' => $solicitud->getEstado(),
            'url_foto' => $urlFoto,
        ], 201);
    }

    #[Route('/soporte/solicitudes/{id}', methods: ['PUT'])]
    public function editarSolicitud(int $id, Request $request): JsonResponse
    {
        $solicitud = $this->buscarSolicitudPendienteDelUsuario($id);

        $datos = $this->leerJson($request);
        $descripcion = $datos['descripcion'] ?? null;
        if (!is_string($descripcion) || trim($descripcion) === '' || mb_strlen(trim($descripcion)) > 255) {
            throw new ApiException('La descripcion es obligatoria y no puede pasar de 255 caracteres.', 400);
        }

        $solicitud->setDescripcion(trim($descripcion));
        $this->em->flush();

        return $this->json([
            'solicitud_id' => $solicitud->getId(),
            'descripcion' => $solicitud->getDescripcion(),
        ]);
    }

    #[Route('/soporte/solicitudes/{id}', methods: ['DELETE'])]
    public function cancelarSolicitud(int $id): Response
    {
        $solicitud = $this->buscarSolicitudPendienteDelUsuario($id);

        // Se borra la solicitud, pero el aparato sigue guardado por si el cliente lo quiere reparar otro dia.
        $this->em->remove($solicitud);
        $this->em->flush();

        // 204 = "hecho, y no hay nada que devolver".
        return new Response(null, 204);
    }

    // =====================================================================
    // 5. REPARACIONES - LO QUE HACE EL ADMINISTRADOR
    // =====================================================================

    // Ver solicitudes. Con ?estado=pendiente solo salen las pendientes; sin parametro salen todas.
    #[Route('/admin/solicitudes', methods: ['GET'])]
    public function listarSolicitudesAdmin(Request $request): JsonResponse
    {
        $this->adminActual();

        $estado = $request->query->get('estado');
        if ($estado === null || $estado === '') {
            $solicitudes = $this->em->getRepository(PeticionReparacion::class)->findBy([], ['id' => 'DESC']);
        } else {
            $solicitudes = $this->em->getRepository(PeticionReparacion::class)->findBy(['estado' => $estado], ['id' => 'ASC']);
        }

        $resultado = [];
        foreach ($solicitudes as $solicitud) {
            $reparacion = $solicitud->getReparacion();
            $resultado[] = [
                'id' => $solicitud->getId(),
                'fecha' => $solicitud->getFecha()->format('Y-m-d'),
                'estado' => $solicitud->getEstado(),
                'usuario' => $solicitud->getUsuario()->getNombreUsuario(),
                'dispositivo' => $this->nombreDispositivo($solicitud->getDispositivoCliente()),
                'tipo' => $solicitud->getDispositivoCliente()->getDepartamento()->getNombre(),
                'tienda' => $solicitud->getTienda()->getNombre(),
                'descripcion' => $solicitud->getDescripcion(),
                'url_foto' => $solicitud->getUrlFoto(),
                'latitud' => (float) $solicitud->getLatitud(),
                'longitud' => (float) $solicitud->getLongitud(),
                'motivo_rechazo' => $solicitud->getMotivoRechazo(),
                'reparacion_id' => $reparacion !== null ? $reparacion->getId() : null,
                // Datos de la reparacion para que el admin vea si ya esta completada (null si aun no hay reparacion).
                'reparacion_estado' => $reparacion !== null ? $reparacion->getEstado() : null,
                'precio_estimado' => $reparacion !== null ? (float) $reparacion->getPrecioEstimado() : null,
                'precio_final' => $reparacion !== null && $reparacion->getPrecioFinal() !== null ? (float) $reparacion->getPrecioFinal() : null,
            ];
        }

        return $this->json($resultado);
    }

    /**
     * Aceptar una solicitud. Se crea la reparacion automaticamente.
     * { "precio_estimado": 60 }
     */
    #[Route('/admin/solicitudes/{id}/aceptar', methods: ['PUT'])]
    public function aceptarSolicitud(int $id, Request $request): JsonResponse
    {
        $this->adminActual();

        $solicitud = $this->buscarSolicitud($id);
        if ($solicitud->getEstado() !== 'pendiente') {
            throw new ApiException('Esta solicitud ya esta ' . $solicitud->getEstado() . '.', 409);
        }

        $datos = $this->leerJson($request);
        $precio = $datos['precio_estimado'] ?? null;
        if (!is_numeric($precio) || (float) $precio <= 0) {
            throw new ApiException('El precio_estimado debe ser un numero mayor que 0.', 400);
        }

        // Creamos la reparacion y marcamos la solicitud como aceptada, todo en el mismo flush().
        $reparacion = new Reparacion();
        $reparacion->setPeticion($solicitud);
        $reparacion->setEstado('en_proceso');
        $reparacion->setFechaInicio(new \DateTime());
        $reparacion->setPrecioEstimado(number_format((float) $precio, 2, '.', ''));
        $reparacion->setPrecioFinal(null);

        $solicitud->setEstado('aceptada');

        $this->em->persist($reparacion);
        $this->em->flush();

        return $this->json([
            'solicitud_id' => $solicitud->getId(),
            'reparacion_id' => $reparacion->getId(),
            'estado' => $reparacion->getEstado(),
            'precio_estimado' => (float) $reparacion->getPrecioEstimado(),
        ], 201);
    }

    /**
     * Rechazar una solicitud. { "motivo": "No hay recambios para este modelo." }
     */
    #[Route('/admin/solicitudes/{id}/rechazar', methods: ['PUT'])]
    public function rechazarSolicitud(int $id, Request $request): JsonResponse
    {
        $this->adminActual();

        $solicitud = $this->buscarSolicitud($id);
        if ($solicitud->getEstado() !== 'pendiente') {
            throw new ApiException('Esta solicitud ya esta ' . $solicitud->getEstado() . '.', 409);
        }

        $datos = $this->leerJson($request);
        $motivo = $datos['motivo'] ?? null;
        if (!is_string($motivo) || trim($motivo) === '') {
            throw new ApiException('Debes indicar el motivo del rechazo.', 400);
        }

        $solicitud->setEstado('rechazada');
        $solicitud->setMotivoRechazo(trim($motivo));
        $this->em->flush();

        return $this->json(['solicitud_id' => $solicitud->getId(), 'estado' => 'rechazada']);
    }

    /**
     * Cambiar una reparacion. { "estado": "completada", "precio_final": 55 }
     * Estados validos: en_proceso y completada. Al completarla, el precio_final es obligatorio.
     */
    #[Route('/admin/reparaciones/{id}', methods: ['PUT'])]
    public function actualizarReparacion(int $id, Request $request): JsonResponse
    {
        $this->adminActual();

        $reparacion = $this->em->getRepository(Reparacion::class)->find($id);
        if ($reparacion === null) {
            throw new ApiException('Reparacion no encontrada.', 404);
        }
        if ($reparacion->getEstado() === 'completada') {
            throw new ApiException('Esta reparacion ya esta completada.', 409);
        }

        $datos = $this->leerJson($request);
        $estado = $datos['estado'] ?? null;
        if ($estado !== 'en_proceso' && $estado !== 'completada') {
            throw new ApiException('El estado debe ser en_proceso o completada.', 400);
        }

        if ($estado === 'completada') {
            $precioFinal = $datos['precio_final'] ?? null;
            if (!is_numeric($precioFinal) || (float) $precioFinal < 0) {
                throw new ApiException('Para completarla debes indicar el precio_final.', 400);
            }
            $reparacion->setPrecioFinal(number_format((float) $precioFinal, 2, '.', ''));
            $reparacion->setFechaFin(new \DateTime());
        }
        $reparacion->setEstado($estado);
        $this->em->flush();

        return $this->json([
            'reparacion_id' => $reparacion->getId(),
            'estado' => $reparacion->getEstado(),
            'precio_final' => $reparacion->getPrecioFinal() !== null ? (float) $reparacion->getPrecioFinal() : null,
        ]);
    }

    // =====================================================================
    // 6. INFORMACION DE LA TIENDA
    // =====================================================================

    #[Route('/info/tiendas', methods: ['GET'])]
    public function listarTiendas(): JsonResponse
    {
        $tiendas = $this->em->getRepository(Tienda::class)->findAll();

        $resultado = [];
        foreach ($tiendas as $tienda) {
            $fotos = [];
            foreach ($tienda->getFotos() as $foto) {
                $fotos[] = $foto->getUrl();
            }
            // Departamentos que ofrece esta tienda (tabla tienda_departamento). La app los usa para filtrar.
            $departamentos = [];
            $ofrecidos = $this->em->getRepository(TiendaDepartamento::class)->findBy(['tienda' => $tienda]);
            foreach ($ofrecidos as $ofrecido) {
                $departamento = $ofrecido->getDepartamento();
                $departamentos[] = ['id' => $departamento->getId(), 'nombre' => $departamento->getNombre()];
            }
            $resultado[] = [
                'id' => $tienda->getId(),
                'nombre' => $tienda->getNombre(),
                'direccion' => $tienda->getDireccion(),
                'historia' => $tienda->getHistoria(),
                'latitud' => (float) $tienda->getLatitud(),
                'longitud' => (float) $tienda->getLongitud(),
                'fotos' => $fotos,
                'departamentos' => $departamentos,
            ];
        }

        return $this->json($resultado);
    }

    #[Route('/info/departamentos', methods: ['GET'])]
    public function listarDepartamentos(): JsonResponse
    {
        $departamentos = $this->em->getRepository(Departamento::class)->findAll();

        $resultado = [];
        foreach ($departamentos as $departamento) {
            $resultado[] = [
                'id' => $departamento->getId(),
                'nombre' => $departamento->getNombre(),
                'descripcion' => $departamento->getDescripcion(),
            ];
        }

        return $this->json($resultado);
    }

    // Marcas para el desplegable del formulario de reparacion. "Otra" siempre sale la ultima.
    #[Route('/info/marcas', methods: ['GET'])]
    public function listarMarcas(): JsonResponse
    {
        // Solo las marcas que fabrican aparatos: las editoras de videojuegos (Electronic Arts...) no salen aqui.
        $marcas = $this->em->getRepository(Marca::class)->findBy(['esFabricante' => true], ['nombre' => 'ASC']);

        $resultado = [];
        $otra = null;
        foreach ($marcas as $marca) {
            $item = ['id' => $marca->getId(), 'nombre' => $marca->getNombre()];
            if ($marca->getNombre() === 'Otra') {
                $otra = $item;
            } else {
                $resultado[] = $item;
            }
        }
        if ($otra !== null) {
            $resultado[] = $otra;
        }

        return $this->json($resultado);
    }

    // =====================================================================
    // FUNCIONES AUXILIARES (las usan las rutas de arriba)
    // =====================================================================

    // Convierte el cuerpo de la peticion (texto JSON) en un array de PHP.
    private function leerJson(Request $request): array
    {
        $datos = json_decode($request->getContent(), true);
        if (!is_array($datos)) {
            throw new ApiException('El cuerpo de la peticion debe ser un JSON valido.', 400);
        }

        return $datos;
    }

    // Devuelve el usuario que ha enviado el token. Si no hay ninguno, error 401.
    private function usuarioActual(): Usuario
    {
        $usuario = $this->getUser();
        if (!$usuario instanceof Usuario) {
            throw new ApiException('No has iniciado sesion.', 401);
        }

        return $usuario;
    }

    // Igual que usuarioActual(), pero ademas exige ser administrador. Si no lo es, error 403.
    private function adminActual(): Usuario
    {
        $usuario = $this->usuarioActual();
        if (!$this->isGranted('ROLE_ADMIN')) {
            throw new ApiException('Solo un administrador puede hacer esto.', 403);
        }

        return $usuario;
    }

    private function datosUsuario(Usuario $usuario): array
    {
        return [
            'id' => $usuario->getId(),
            'nombre_usuario' => $usuario->getNombreUsuario(),
            'email' => $usuario->getEmail(),
            'roles' => $usuario->getRoles(),   // la app mira si contiene ROLE_ADMIN
        ];
    }

    // Los datos que tienen en comun todos los productos.
    private function datosProducto(Producto $producto): array
    {
        return [
            'id' => $producto->getId(),
            'nombre' => $producto->getNombre(),
            'marca' => $this->nombreMarca($producto),
            'descripcion' => $producto->getDescripcion(),
            'precio' => (float) $producto->getPrecio(),
            'stock' => $producto->getStock(),
            'foto_url' => $this->fotoPrincipal($producto),
        ];
    }

    // La foto principal es la primera que se subio. Si el producto no tiene fotos, devuelve null.
    private function fotoPrincipal(Producto $producto): ?string
    {
        $primera = null;
        foreach ($producto->getFotos() as $foto) {
            if ($primera === null || $foto->getId() < $primera->getId()) {
                $primera = $foto;
            }
        }
        if ($primera === null) {
            return null;
        }

        return $primera->getUrl();
    }

    // Texto de la marca de un producto. En moviles, tablets y consolas es el fabricante; en videojuegos, la editora.
    // Un videojuego sin marca se muestra como "Indie" y cualquier otro producto sin marca, como "Por definir".
    private function nombreMarca(Producto $producto): string
    {
        $marca = $producto->getMarca();
        if ($marca !== null) {
            return $marca->getNombre();
        }
        if ($producto instanceof Videojuego) {
            return 'Indie';
        }

        return 'Por definir';
    }

    private function datosDispositivo(DispositivoCliente $dispositivo): array
    {
        return [
            'id' => $dispositivo->getId(),
            'marca_id' => $dispositivo->getMarca()->getId(),
            'marca' => $dispositivo->getMarca()->getNombre(),
            'departamento_id' => $dispositivo->getDepartamento()->getId(),
            'departamento' => $dispositivo->getDepartamento()->getNombre(),
            'modelo' => $dispositivo->getModelo(),
        ];
    }

    // Texto para mostrar, por ejemplo "Samsung Galaxy S24".
    private function nombreDispositivo(DispositivoCliente $dispositivo): string
    {
        return $dispositivo->getMarca()->getNombre() . ' ' . $dispositivo->getModelo();
    }

    // Lee un numero (latitud o longitud) y lo devuelve como texto, como pide la base de datos.
    // Son obligatorios porque las columnas no admiten NULL. Si la app no tiene permiso de ubicacion, debe enviar 0.
    private function leerNumero(array $datos, string $campo): string
    {
        if (!isset($datos[$campo]) || !is_numeric($datos[$campo])) {
            throw new ApiException('El campo ' . $campo . ' es obligatorio y debe ser un numero.', 400);
        }

        return (string) $datos[$campo];
    }

    // Lee una fecha en formato yyyy-MM-dd (por ejemplo 2026-11-10).
    private function leerFecha(array $datos, string $campo): \DateTime
    {
        $texto = $datos[$campo] ?? null;
        if (!is_string($texto)) {
            throw new ApiException('El campo ' . $campo . ' es obligatorio.', 400);
        }
        $fecha = \DateTime::createFromFormat('!Y-m-d', $texto);
        if ($fecha === false || $fecha->format('Y-m-d') !== $texto) {
            throw new ApiException('El campo ' . $campo . ' debe tener el formato yyyy-MM-dd.', 400);
        }

        return $fecha;
    }

    // Calcula lo que cuesta alquilar un videojuego durante unos dias.
    // Probamos cuatro formas de pagar y nos quedamos con la mas barata para el cliente.
    private function calcularPrecioAlquiler(Videojuego $videojuego, int $dias): float
    {
        $diario = (float) $videojuego->getPrecioDiario();
        $semanal = (float) $videojuego->getPrecioSemanal();
        $mensual = (float) $videojuego->getPrecioMensual();

        // Forma 1: pagar todos los dias sueltos.
        $opcion1 = $dias * $diario;

        // Forma 2: meses completos + semanas completas + dias sueltos.
        $meses = intdiv($dias, 30);
        $resto = $dias % 30;
        $semanas = intdiv($resto, 7);
        $sueltos = $resto % 7;
        $opcion2 = ($meses * $mensual) + ($semanas * $semanal) + ($sueltos * $diario);

        // Forma 3: pagar semanas enteras (redondeando hacia arriba).
        $opcion3 = ceil($dias / 7) * $semanal;

        // Forma 4: pagar meses enteros (redondeando hacia arriba).
        $opcion4 = ceil($dias / 30) * $mensual;

        return min($opcion1, $opcion2, $opcion3, $opcion4);
    }

    // Busca una solicitud por id. Si no existe, error 404.
    private function buscarSolicitud(int $id): PeticionReparacion
    {
        $solicitud = $this->em->getRepository(PeticionReparacion::class)->find($id);
        if ($solicitud === null) {
            throw new ApiException('Solicitud no encontrada.', 404);
        }

        return $solicitud;
    }

    // Busca una solicitud y comprueba dos cosas: que es de quien la pide y que sigue pendiente.
    private function buscarSolicitudPendienteDelUsuario(int $id): PeticionReparacion
    {
        $usuario = $this->usuarioActual();

        $solicitud = $this->buscarSolicitud($id);
        if ($solicitud->getUsuario()->getId() !== $usuario->getId()) {
            throw new ApiException('Solicitud no encontrada.', 404);
        }
        if ($solicitud->getEstado() !== 'pendiente') {
            throw new ApiException('Solo se pueden modificar solicitudes pendientes.', 409);
        }

        return $solicitud;
    }

    // Guarda la foto en el servidor y devuelve su URL. Si no se envia foto, devuelve null.
    private function guardarFoto(Request $request, array $datos): ?string
    {
        $contenido = null;   // los bytes de la imagen
        $tipo = null;        // image/jpeg, image/png o image/webp

        $archivo = $request->files->get('foto');
        $base64 = $datos['foto_base64'] ?? null;

        if ($archivo !== null) {
            // Caso A: la foto llega como archivo (formulario multipart).
            $tipo = $archivo->getMimeType();
            if (!$archivo->isValid() || $archivo->getSize() > self::MAX_FOTO || !$this->tipoFotoValido($tipo)) {
                throw new ApiException('La foto debe ser JPG, PNG o WEBP y pesar menos de 5 MB.', 400);
            }
            $contenido = file_get_contents($archivo->getPathname());
        } elseif (is_string($base64) && $base64 !== '') {
            // Caso B: la foto llega como texto base64 dentro del JSON.
            // Quitamos el prefijo "data:image/jpeg;base64," si lo trae.
            $limpio = preg_replace('#^data:image/\w+;base64,#i', '', $base64);
            $contenido = base64_decode($limpio, true);
            if ($contenido === false || strlen($contenido) > self::MAX_FOTO) {
                throw new ApiException('La foto en base64 no es valida o pesa mas de 5 MB.', 400);
            }
            $info = @getimagesizefromstring($contenido);
            if ($info === false || !$this->tipoFotoValido($info['mime'])) {
                throw new ApiException('La foto debe ser JPG, PNG o WEBP.', 400);
            }
            $tipo = $info['mime'];
        }

        if ($contenido === null) {
            return null;   // no se envio foto
        }

        if ($tipo === 'image/jpeg') {
            $extension = 'jpg';
        } elseif ($tipo === 'image/png') {
            $extension = 'png';
        } else {
            $extension = 'webp';
        }

        // Carpeta donde se guardan (el parametro reparaciones_upload_dir ya estaba en vuestra configuracion).
        $carpeta = $this->getParameter('reparaciones_upload_dir');
        if (!is_dir($carpeta)) {
            mkdir($carpeta, 0775, true);
        }

        // Nombre aleatorio para que nadie pueda adivinar la URL ni se pisen archivos.
        $nombre = bin2hex(random_bytes(16)) . '.' . $extension;
        if (file_put_contents($carpeta . '/' . $nombre, $contenido) === false) {
            throw new ApiException('No se pudo guardar la imagen.', 500);
        }

        // Siempre https: detras del proxy PHP cree que es http, y la app movil (https) bloquearia las fotos http.
        return 'https://' . $request->getHost() . '/uploads/reparaciones/' . $nombre;
    }

    private function tipoFotoValido(?string $tipo): bool
    {
        return $tipo === 'image/jpeg' || $tipo === 'image/png' || $tipo === 'image/webp';
    }
}