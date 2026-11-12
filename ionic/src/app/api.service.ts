import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../environments/environment';

export interface Usuario {
  id: number;
  nombre_usuario: string;
  email: string;
  roles: string[];
}

export interface LoginRespuesta {
  token: string;
  usuario: Usuario;
}

export interface Producto {
  id: number;
  nombre: string;
  marca: string;
  descripcion: string | null;
  precio: number;
  stock: number;
  foto_url: string | null;
}

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

export interface CompraRespuesta {
  compra_id: number;
  total: number;
}

export interface AlquilerRespuesta {
  alquiler_id: number;
  total: number;
}

export interface Operacion {
  tipo: 'compra' | 'alquiler' | 'reparacion';
  id: number;
  fecha: string;
  estado: string;
  total: number | null;
  detalle?: { nombre: string; cantidad?: number; precio_unidad?: number }[];
  dispositivo?: string;
  solicitud_id?: number;
  reparacion_id?: number | null;
  descripcion?: string;
  url_foto?: string | null;
  motivo_rechazo?: string | null;
}

export interface Marca {
  id: number;
  nombre: string;
}

export interface Departamento {
  id: number;
  nombre: string;
  descripcion: string | null;
}

export interface Tienda {
  id: number;
  nombre: string;
  direccion: string;
  historia: string | null;
  latitud: number;
  longitud: number;
  fotos: string[];
  departamentos?: { id: number; nombre: string }[];
}

export interface Aparato {
  id: number;
  marca_id: number;
  marca: string;
  departamento_id: number;
  departamento: string;
  modelo: string;
}

export interface SolicitudAdmin {
  id: number;
  fecha: string;
  estado: 'pendiente' | 'aceptada' | 'rechazada';
  usuario: string;
  dispositivo: string;
  tipo: string;
  tienda: string;
  descripcion: string;
  url_foto: string | null;
  latitud: number;
  longitud: number;
  motivo_rechazo: string | null;
  reparacion_id: number | null;
  reparacion_estado?: 'en_proceso' | 'completada';
  precio_estimado?: number | null;
  precio_final?: number | null;
}

export interface SolicitudRespuesta {
  solicitud_id: number;
  estado: string;
  url_foto: string | null;
}

export interface LineaCompra {
  id: number;
  cantidad: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private url = `${environment.apiUrl}/api`;

  constructor(private http: HttpClient) {}


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


  urlFoto(ruta: string | null): string {
    if (!ruta) {
      return 'assets/icon/favicon.png';
    }
    if (ruta.startsWith('http')) {
      return ruta;
    }
    return environment.apiUrl + (ruta.startsWith('/') ? '' : '/') + ruta;
  }

  fotoRota(evento: Event) {
    const img = evento.target as HTMLIonImgElement;
    if (!img.src?.endsWith('assets/icon/favicon.png')) {
      img.src = 'assets/icon/favicon.png';
    }
  }

  mensajeError(e: any): string {
    return e?.error?.error ?? 'No se pudo conectar con el servidor.';
  }

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


  comprar(productos: LineaCompra[], latitud: number, longitud: number): Observable<CompraRespuesta> {
    return this.http.post<CompraRespuesta>(`${this.url}/operaciones/comprar`, { productos, latitud, longitud });
  }

  alquilar(idsVideojuegos: number[], fecha_inicio: string, fecha_fin: string,
           latitud: number, longitud: number): Observable<AlquilerRespuesta> {
    const videojuegos = idsVideojuegos.map(id => ({ id }));
    return this.http.post<AlquilerRespuesta>(`${this.url}/operaciones/alquilar`,
      { videojuegos, fecha_inicio, fecha_fin, latitud, longitud });
  }

  devolverAlquiler(id: number): Observable<any> {
    return this.http.put(`${this.url}/operaciones/alquileres/${id}/devolver`, {});
  }

  obtenerMisOperaciones(): Observable<Operacion[]> {
    return this.http.get<Operacion[]>(`${this.url}/operaciones/mis-operaciones`);
  }


  obtenerMisDispositivos(): Observable<Aparato[]> {
    return this.http.get<Aparato[]>(`${this.url}/soporte/dispositivos`);
  }

  crearDispositivo(marca_id: number, departamento_id: number, modelo: string): Observable<Aparato> {
    return this.http.post<Aparato>(`${this.url}/soporte/dispositivos`, { marca_id, departamento_id, modelo });
  }

  crearSolicitud(datos: {
    dispositivo_id: number;
    tienda_id: number;
    descripcion: string;
    latitud: number;
    longitud: number;
    foto_base64?: string;
  }): Observable<SolicitudRespuesta> {
    return this.http.post<SolicitudRespuesta>(`${this.url}/soporte/solicitudes`, datos);
  }

  editarSolicitud(id: number, descripcion: string): Observable<any> {
    return this.http.put(`${this.url}/soporte/solicitudes/${id}`, { descripcion });
  }

  cancelarSolicitud(id: number): Observable<any> {
    return this.http.delete(`${this.url}/soporte/solicitudes/${id}`);
  }


  obtenerSolicitudesAdmin(estado?: string): Observable<SolicitudAdmin[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }
    return this.http.get<SolicitudAdmin[]>(`${this.url}/admin/solicitudes`, { params });
  }

  aceptarSolicitud(id: number, precio_estimado: number): Observable<any> {
    return this.http.put(`${this.url}/admin/solicitudes/${id}/aceptar`, { precio_estimado });
  }

  rechazarSolicitud(id: number, motivo: string): Observable<any> {
    return this.http.put(`${this.url}/admin/solicitudes/${id}/rechazar`, { motivo });
  }

  actualizarReparacion(id: number, estado: 'en_proceso' | 'completada', precio_final?: number): Observable<any> {
    return this.http.put(`${this.url}/admin/reparaciones/${id}`, { estado, precio_final });
  }


  obtenerTiendas(): Observable<Tienda[]> {
    return this.http.get<Tienda[]>(`${this.url}/info/tiendas`);
  }

  obtenerDepartamentos(): Observable<Departamento[]> {
    return this.http.get<Departamento[]>(`${this.url}/info/departamentos`);
  }

  obtenerMarcas(): Observable<Marca[]> {
    return this.http.get<Marca[]>(`${this.url}/info/marcas`);
  }
}
