import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../environments/environment';

// ---- Tipos basicos que devuelve la API ----
export interface Usuario {
  id: number;
  nombre_usuario: string;
  email: string;
  roles: string[]; // si contiene 'ROLE_ADMIN' es administrador
}

export interface LoginRespuesta {
  token: string;
  usuario: Usuario;
}

// Datos comunes de cualquier producto del catalogo
export interface Producto {
  id: number;
  nombre: string;
  marca: string; // fabricante o editora ("Indie" / "Por definir" si no tiene)
  descripcion: string | null;
  precio: number;
  stock: number;
  foto_url: string | null;
}

// Telefono o tablet
export interface Dispositivo extends Producto {
  modelo: string;
  pulgadas: number;
  almacenamiento: string;
  tipo: 'telefono' | 'tablet';
}

export interface Videojuego extends Producto {
  precio_diario: number;
  precio_semanal: number;
  precio_mensual: number;
  copias_alquiler: number;
}

export interface Consola extends Producto {
  modelo: string;
  almacenamiento: string;
}

// Respuestas de comprar y alquilar
export interface CompraRespuesta {
  compra_id: number;
  total: number;
}

export interface AlquilerRespuesta {
  alquiler_id: number;
  total: number;
}

// Una fila de GET /operaciones/mis-operaciones
export interface Operacion {
  tipo: 'compra' | 'alquiler' | 'reparacion';
  id: number;
  fecha: string; // yyyy-MM-dd (en alquileres, la fecha de inicio)
  estado: string; // compra: completada; alquiler: pendiente | activo | finalizado
  total: number | null;
  // compra: [{nombre, cantidad, precio_unidad}]; alquiler: [{nombre}]
  detalle?: { nombre: string; cantidad?: number; precio_unidad?: number }[];
  dispositivo?: string; // solo reparaciones
}

// Linea de una compra: producto y unidades
export interface LineaCompra {
  id: number;
  cantidad: number;
}

/**
 * Servicio con UN metodo por cada ruta de la API (ver docs/ApiController.php).
 * El token lo anade auth.interceptor.ts, aqui no hace falta tocarlo.
 * Solo el login respeta environment.mock (para poder entrar sin API).
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  // Todas las rutas empiezan por /api
  private url = `${environment.apiUrl}/api`;

  constructor(private http: HttpClient) {}

  // ===================== Usuarios =====================

  // login puede ser el email o el nombre de usuario
  iniciarSesion(login: string, password: string): Observable<LoginRespuesta> {
    if (environment.mock) {
      return of({
        token: 'demo',
        usuario: { id: 0, nombre_usuario: login, email: login, roles: ['ROLE_USER'] },
      });
    }
    return this.http.post<LoginRespuesta>(`${this.url}/auth/login`, { login, password });
  }

  registrarUsuario(nombre_usuario: string, email: string, password: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.url}/auth/register`, { nombre_usuario, email, password });
  }

  // ===================== Catalogo =====================

  // Foto de un producto: si no tiene, el icono de la app; si la ruta es relativa, le ponemos el servidor delante
  urlFoto(ruta: string | null): string {
    if (!ruta) {
      return 'assets/icon/favicon.png';
    }
    if (ruta.startsWith('http')) {
      return ruta;
    }
    return environment.apiUrl + (ruta.startsWith('/') ? '' : '/') + ruta;
  }

  // Si una foto no carga, ponemos el icono de la app (solo una vez, para no entrar en bucle)
  fotoRota(evento: Event) {
    const img = evento.target as HTMLImageElement;
    if (!img.src.endsWith('assets/icon/favicon.png')) {
      img.src = 'assets/icon/favicon.png';
    }
  }

  // Texto del error para el usuario: la API manda {"error": "mensaje"}
  mensajeError(e: any): string {
    return e?.error?.error ?? 'No se pudo conectar con el servidor.';
  }

  // tipo opcional: 'telefono' o 'tablet' (sin tipo salen los dos)
  obtenerDispositivos(tipo?: 'telefono' | 'tablet'): Observable<Dispositivo[]> {
    let params = new HttpParams();
    if (tipo) {
      params = params.set('tipo', tipo);
    }
    return this.http.get<Dispositivo[]>(`${this.url}/productos/dispositivos`, { params });
  }

  obtenerVideojuegos(): Observable<Videojuego[]> {
    return this.http.get<Videojuego[]>(`${this.url}/productos/videojuegos`);
  }

  obtenerConsolas(): Observable<Consola[]> {
    return this.http.get<Consola[]>(`${this.url}/productos/consolas`);
  }

  // ===================== Compras y alquileres =====================

  // Si el usuario niega el GPS, mandar latitud y longitud = 0
  comprar(productos: LineaCompra[], latitud: number, longitud: number): Observable<CompraRespuesta> {
    return this.http.post<CompraRespuesta>(`${this.url}/operaciones/comprar`, { productos, latitud, longitud });
  }

  // Fechas con formato yyyy-MM-dd. videojuegos = lista de ids
  alquilar(idsVideojuegos: number[], fecha_inicio: string, fecha_fin: string,
           latitud: number, longitud: number): Observable<AlquilerRespuesta> {
    const videojuegos = idsVideojuegos.map(id => ({ id }));
    return this.http.post<AlquilerRespuesta>(`${this.url}/operaciones/alquilar`,
      { videojuegos, fecha_inicio, fecha_fin, latitud, longitud });
  }

  devolverAlquiler(id: number): Observable<any> {
    return this.http.put(`${this.url}/operaciones/alquileres/${id}/devolver`, {});
  }

  // Compras, alquileres y reparaciones del usuario
  obtenerMisOperaciones(): Observable<Operacion[]> {
    return this.http.get<Operacion[]>(`${this.url}/operaciones/mis-operaciones`);
  }

  // ===================== Soporte (usuario) =====================

  obtenerMisDispositivos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/soporte/dispositivos`);
  }

  crearDispositivo(marca_id: number, departamento_id: number, modelo: string): Observable<any> {
    return this.http.post(`${this.url}/soporte/dispositivos`, { marca_id, departamento_id, modelo });
  }

  // foto_base64 es opcional (foto de la camara, calidad ~65%)
  crearSolicitud(datos: {
    dispositivo_id: number;
    tienda_id: number;
    descripcion: string;
    latitud: number;
    longitud: number;
    foto_base64?: string;
  }): Observable<any> {
    return this.http.post(`${this.url}/soporte/solicitudes`, datos);
  }

  // Solo se puede si la solicitud esta pendiente
  editarSolicitud(id: number, descripcion: string): Observable<any> {
    return this.http.put(`${this.url}/soporte/solicitudes/${id}`, { descripcion });
  }

  // Solo si esta pendiente. La API responde 204 (sin contenido)
  cancelarSolicitud(id: number): Observable<any> {
    return this.http.delete(`${this.url}/soporte/solicitudes/${id}`);
  }

  // ===================== Administrador (ROLE_ADMIN) =====================

  // estado opcional: pendiente, aceptada o rechazada (sin estado salen todas)
  obtenerSolicitudesAdmin(estado?: string): Observable<any[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }
    return this.http.get<any[]>(`${this.url}/admin/solicitudes`, { params });
  }

  aceptarSolicitud(id: number, precio_estimado: number): Observable<any> {
    return this.http.put(`${this.url}/admin/solicitudes/${id}/aceptar`, { precio_estimado });
  }

  rechazarSolicitud(id: number, motivo: string): Observable<any> {
    return this.http.put(`${this.url}/admin/solicitudes/${id}/rechazar`, { motivo });
  }

  // Al pasar a 'completada' el precio_final es obligatorio
  actualizarReparacion(id: number, estado: 'en_proceso' | 'completada', precio_final?: number): Observable<any> {
    return this.http.put(`${this.url}/admin/reparaciones/${id}`, { estado, precio_final });
  }

  // ===================== Informacion =====================

  obtenerTiendas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/info/tiendas`);
  }

  obtenerDepartamentos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/info/departamentos`);
  }

  // Marcas fabricantes ("Otra" va al final)
  obtenerMarcas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/info/marcas`);
  }
}
