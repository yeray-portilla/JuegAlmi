import { Component, OnDestroy } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard,
  IonRefresher, IonRefresherContent, IonIcon,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { navigateOutline, locationOutline } from 'ionicons/icons';
import * as L from 'leaflet';
import { TiendasService } from '../tiendas.service';

const ICONO_TIENDA = L.icon({
  iconUrl: 'assets/leaflet/marker-icon.png',
  iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
  shadowUrl: 'assets/leaflet/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

@Component({
  standalone: true,
  selector: 'app-ubicacion',
  templateUrl: './ubicacion.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard,
    IonRefresher, IonRefresherContent, IonIcon],
})
export class UbicacionPage implements OnDestroy {
  private mapas = new Map<number, L.Map>();
  private dentro = false;

  constructor(public tiendas: TiendasService) {
    addIcons({ navigateOutline, locationOutline });
  }

  ionViewWillEnter() {
    this.cargar();
  }

  ionViewDidEnter() {
    this.dentro = true;
    this.pintarMapas();
    this.tiendas.pedirPosicion();
  }

  ionViewWillLeave() {
    this.dentro = false;
    this.borrarMapas();
  }

  ngOnDestroy() {
    this.borrarMapas();
  }

  async cargar(event?: any) {
    await this.tiendas.cargar(event);
    this.pintarMapas();
  }

  private pintarMapas() {
    if (!this.dentro) {
      return;
    }
    setTimeout(() => {
      for (const t of this.tiendas.datos()) {
        const div = document.getElementById('mapa-' + t.id);
        const anterior = this.mapas.get(t.id);
        if (anterior && anterior.getContainer() === div) {
          anterior.invalidateSize();
          continue;
        }
        anterior?.remove();
        this.mapas.delete(t.id);
        if (!div) {
          continue;
        }
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
