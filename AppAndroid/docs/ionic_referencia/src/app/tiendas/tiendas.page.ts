import { Component, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel, IonIcon,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { navigateOutline, locationOutline } from 'ionicons/icons';
import { ApiService, Tienda } from '../api.service';
import { Ubicacion, UbicacionService } from '../ubicacion.service';
import * as L from 'leaflet';

// Las tres partes del selector de arriba
type Seccion = 'historia' | 'ubicacion' | 'galeria';

@Component({
  standalone: true,
  selector: 'app-tiendas',
  templateUrl: './tiendas.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
    IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel, IonIcon],
})
export class TiendasPage {
  // Signals: la app no lleva zone.js
  datos = signal<Tienda[]>([]);
  cargando = signal(false);
  error = signal('');
  seccion = signal<Seccion>('historia');
  // Posicion del usuario; null = sin permiso o sin GPS (entonces no se ve la distancia)
  miPosicion = signal<Ubicacion | null>(null);

  private posicionPedida = false; // para pedir el GPS solo una vez por visita
  private mapas: L.Map[] = []; // mapas de Leaflet que hay ahora en pantalla

  constructor(public api: ApiService, private ubicacion: UbicacionService, private ruta: ActivatedRoute) {
    addIcons({ navigateOutline, locationOutline });
    // El menu abre /tiendas?seccion=ubicacion (o galeria, historia): elegimos esa parte
    this.ruta.queryParamMap.subscribe(params => {
      const s = params.get('seccion');
      if (s === 'historia' || s === 'ubicacion' || s === 'galeria') {
        this.seccion.set(s);
        this.pintarMapas();
      }
    });
  }

  // Cada vez que se entra en la pantalla, recargamos
  ionViewWillEnter() {
    this.posicionPedida = false;
    this.cargar();
  }

  // Al salir de la pantalla quitamos los mapas
  ionViewDidLeave() {
    this.borrarMapas();
  }

  // Pide las tiendas a la API (event = el "tirar para recargar", si lo hay)
  cargar(event?: any) {
    this.cargando.set(true);
    this.error.set('');
    this.api.obtenerTiendas().subscribe({
      next: (lista) => {
        this.datos.set(lista);
        this.cargando.set(false);
        event?.target.complete();
        this.pintarMapas();
        this.pedirPosicion();
      },
      error: (e) => {
        this.error.set(this.api.mensajeError(e));
        this.cargando.set(false);
        event?.target.complete();
      },
    });
  }

  cambiarSeccion(evento: any) {
    this.seccion.set(evento.detail.value);
    this.pintarMapas();
  }

  // Crea un mapa de OpenStreetMap por tienda, con la tienda marcada (solo en la parte Ubicacion).
  // Se espera un momento para que Angular haya pintado los <div id="mapa-X"> en la pagina.
  private pintarMapas() {
    this.borrarMapas();
    if (this.seccion() !== 'ubicacion') {
      return;
    }
    setTimeout(() => {
      this.borrarMapas();
      for (const t of this.datos()) {
        const div = document.getElementById('mapa-' + t.id);
        if (!div) {
          continue;
        }
        // Sin arrastrar el mapa con el dedo, para que la lista se pueda desplazar bien
        const mapa = L.map(div, { dragging: false, scrollWheelZoom: false, attributionControl: true })
          .setView([t.latitud, t.longitud], 16);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '© OpenStreetMap',
        }).addTo(mapa);
        // Marca redonda con los colores de la app (asi no hacen falta las imagenes de Leaflet)
        L.circleMarker([t.latitud, t.longitud], {
          radius: 10, color: '#FFFFFF', weight: 2, fillColor: '#FF5A36', fillOpacity: 1,
        }).addTo(mapa).bindPopup(t.nombre);
        this.mapas.push(mapa);
        // Si la pagina aun se estaba animando, el mapa recalcula su tamano
        setTimeout(() => mapa.invalidateSize(), 300);
      }
    }, 0);
  }

  private borrarMapas() {
    this.mapas.forEach(m => m.remove());
    this.mapas = [];
  }

  // Pide el GPS con el servicio de ubicacion. Si devuelve 0, 0 es que no hay permiso.
  private async pedirPosicion() {
    if (this.posicionPedida) {
      return;
    }
    this.posicionPedida = true;
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
