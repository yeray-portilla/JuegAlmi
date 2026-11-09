import { Component } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent,
} from '@ionic/angular';
import { ApiService } from '../api.service';
import { TiendasService } from '../tiendas.service';

// Fotos de cada tienda, con desplazamiento horizontal
@Component({
  standalone: true,
  selector: 'app-galeria',
  templateUrl: './galeria.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
    IonRefresher, IonRefresherContent],
})
export class GaleriaPage {
  constructor(public tiendas: TiendasService, public api: ApiService) {}

  // Cada vez que se entra en la pantalla, recargamos
  ionViewWillEnter() {
    this.tiendas.cargar();
  }
}
