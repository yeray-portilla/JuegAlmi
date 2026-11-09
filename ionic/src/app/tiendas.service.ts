import { Injectable, signal } from '@angular/core';
import { ApiService, Tienda } from './api.service';
import { Ubicacion, UbicacionService } from './ubicacion.service';

// Datos de las tiendas compartidos por las paginas Historia, Ubicacion y Galeria.
// Asi la carga de /info/tiendas y el calculo de la distancia estan en un solo sitio.
@Injectable({ providedIn: 'root' })
export class TiendasService {
  // Signals: la app no lleva zone.js
  datos = signal<Tienda[]>([]);
  cargando = signal(false);
  error = signal('');
  // Posicion del usuario; null = sin permiso o sin GPS (entonces no se ve la distancia)
  miPosicion = signal<Ubicacion | null>(null);

  constructor(private api: ApiService, private ubicacion: UbicacionService) {}

  // Pide las tiendas a la API (event = el "tirar para recargar", si lo hay).
  // Devuelve una promesa que termina cuando ha llegado la respuesta (bien o mal).
  cargar(event?: any): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    return new Promise(terminar => {
      this.api.obtenerTiendas().subscribe({
        next: (lista) => {
          this.datos.set(lista);
          this.cargando.set(false);
          event?.target.complete();
          terminar();
        },
        error: (e) => {
          this.error.set(this.api.mensajeError(e));
          this.cargando.set(false);
          event?.target.complete();
          terminar();
        },
      });
    });
  }

  // Pide el GPS con el servicio de ubicacion. Si devuelve 0, 0 es que no hay permiso.
  async pedirPosicion() {
    const pos = await this.ubicacion.obtener();
    this.miPosicion.set(pos.latitud === 0 && pos.longitud === 0 ? null : pos);
  }

  // Texto "a 3,2 km" desde el usuario hasta la tienda ('' si no sabemos donde esta el usuario)
  distancia(tienda: Tienda): string {
    const yo = this.miPosicion();
    if (!yo) {
      return '';
    }
    const km = this.calcularKm(yo.latitud, yo.longitud, tienda.latitud, tienda.longitud);
    return 'a ' + km.toLocaleString('es-ES', { maximumFractionDigits: 1 }) + ' km';
  }

  // Formula de Haversine: distancia en km entre dos puntos de la Tierra
  private calcularKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const radio = 6371; // radio de la Tierra en km
    const aRad = (g: number) => (g * Math.PI) / 180;
    const dLat = aRad(lat2 - lat1);
    const dLon = aRad(lon2 - lon1);
    const a = Math.sin(dLat / 2) ** 2 + Math.cos(aRad(lat1)) * Math.cos(aRad(lat2)) * Math.sin(dLon / 2) ** 2;
    return radio * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  // Nombres de los departamentos separados por comas
  nombresDepartamentos(tienda: Tienda): string {
    return (tienda.departamentos ?? []).map(d => d.nombre).join(', ');
  }

  // Abre Google Maps con la ruta hasta la tienda
  comoLlegar(tienda: Tienda) {
    const url = `https://www.google.com/maps/dir/?api=1&destination=${tienda.latitud},${tienda.longitud}`;
    window.open(url, '_system');
  }
}
