import { Component, OnDestroy } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent, IonIcon,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { navigateOutline, locationOutline } from 'ionicons/icons';
import * as L from 'leaflet';
import { TiendasService } from '../tiendas.service';

// Icono del marcador. Las imagenes de Leaflet estan copiadas en src/assets/leaflet/:
// el icono por defecto de Leaflet sale roto con Angular porque no encuentra sus imagenes.
const ICONO_TIENDA = L.icon({
  iconUrl: 'assets/leaflet/marker-icon.png',
  iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
  shadowUrl: 'assets/leaflet/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41], // la punta del marcador
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

// Ubicacion de cada tienda: mapa, distancia y boton "Como llegar"
@Component({
  standalone: true,
  selector: 'app-ubicacion',
  templateUrl: './ubicacion.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
    IonRefresher, IonRefresherContent, IonIcon],
})
export class UbicacionPage implements OnDestroy {
  private mapas = new Map<number, L.Map>(); // mapa de cada tienda (por id)
  private dentro = false; // true cuando la pagina ya se ve (sus contenedores tienen tamano)

  constructor(public tiendas: TiendasService) {
    addIcons({ navigateOutline, locationOutline });
  }

  ionViewWillEnter() {
    this.cargar();
  }

  // Aqui la pagina ya ha terminado de animarse: los contenedores del mapa tienen tamano
  ionViewDidEnter() {
    this.dentro = true;
    this.pintarMapas();
    this.tiendas.pedirPosicion();
  }

  // Al salir destruimos los mapas para no crear duplicados al volver
  ionViewWillLeave() {
    this.dentro = false;
    this.borrarMapas();
  }

  ngOnDestroy() {
    this.borrarMapas();
  }

  // Carga (o recarga al tirar hacia abajo) y despues pinta los mapas que falten
  async cargar(event?: any) {
    await this.tiendas.cargar(event);
    this.pintarMapas();
  }

  // Crea un mapa de OpenStreetMap por tienda con su marcador.
  // Un pequeno retardo para que Angular haya pintado los <div id="mapa-X">.
  private pintarMapas() {
    if (!this.dentro) {
      return;
    }
    setTimeout(() => {
      for (const t of this.tiendas.datos()) {
        const div = document.getElementById('mapa-' + t.id);
        const anterior = this.mapas.get(t.id);
        if (anterior && anterior.getContainer() === div) {
          anterior.invalidateSize(); // ya existe en este contenedor: solo recalcula el tamano
          continue;
        }
        anterior?.remove();
        this.mapas.delete(t.id);
        if (!div) {
          continue;
        }
        // Sin arrastrar con el dedo, para que la lista se pueda desplazar bien (zoom con + y -)
        const mapa = L.map(div, { dragging: false, scrollWheelZoom: false }).setView([t.latitud, t.longitud], 16);
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '© OpenStreetMap',
        }).addTo(mapa);
        L.marker([t.latitud, t.longitud], { icon: ICONO_TIENDA }).addTo(mapa).bindPopup(t.nombre);
        mapa.invalidateSize();
        this.mapas.set(t.id, mapa);
      }
    }, 100);
  }

  private borrarMapas() {
    this.mapas.forEach(m => m.remove());
    this.mapas.clear();
  }
}
