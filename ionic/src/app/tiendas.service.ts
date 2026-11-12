import { Injectable, signal } from '@angular/core';
import { ApiService, Tienda } from './api.service';
import { Ubicacion, UbicacionService } from './ubicacion.service';

@Injectable({ providedIn: 'root' })
export class TiendasService {
  datos = signal<Tienda[]>([]);
  cargando = signal(false);
  error = signal('');
  miPosicion = signal<Ubicacion | null>(null);

  constructor(private api: ApiService, private ubicacion: UbicacionService) {}

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

  async pedirPosicion() {
    const pos = await this.ubicacion.obtener();
    this.miPosicion.set(pos.latitud === 0 && pos.longitud === 0 ? null : pos);
  }

  distancia(tienda: Tienda): string {
    const yo = this.miPosicion();
    if (!yo) {
      return '';
    }
    const km = this.calcularKm(yo.latitud, yo.longitud, tienda.latitud, tienda.longitud);
    return 'a ' + km.toLocaleString('es-ES', { maximumFractionDigits: 1 }) + ' km';
  }

  private calcularKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const radio = 6371;
    const aRad = (g: number) => (g * Math.PI) / 180;
    const dLat = aRad(lat2 - lat1);
    const dLon = aRad(lon2 - lon1);
    const a = Math.sin(dLat / 2) ** 2 + Math.cos(aRad(lat1)) * Math.cos(aRad(lat2)) * Math.sin(dLon / 2) ** 2;
    return radio * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  nombresDepartamentos(tienda: Tienda): string {
    return (tienda.departamentos ?? []).map(d => d.nombre).join(', ');
  }

  comoLlegar(tienda: Tienda) {
    const url = `https://www.google.com/maps/dir/?api=1&destination=${tienda.latitud},${tienda.longitud}`;
    window.open(url, '_system');
  }
}
