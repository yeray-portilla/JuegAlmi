import { Component } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
  IonRefresher, IonRefresherContent,
} from '@ionic/angular';
import { ApiService } from '../api.service';
import { TiendasService } from '../tiendas.service';

@Component({
  standalone: true,
  selector: 'app-galeria',
  templateUrl: './galeria.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
    IonRefresher, IonRefresherContent],
})
export class GaleriaPage {
  constructor(public tiendas: TiendasService, public api: ApiService) {}

  ionViewWillEnter() {
    this.tiendas.cargar();
  }
}
