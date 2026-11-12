import { Component } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard,
  IonRefresher, IonRefresherContent, IonIcon,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { locationOutline } from 'ionicons/icons';
import { TiendasService } from '../tiendas.service';

@Component({
  standalone: true,
  selector: 'app-historia',
  templateUrl: './historia.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard,
    IonRefresher, IonRefresherContent, IonIcon],
})
export class HistoriaPage {
  constructor(public tiendas: TiendasService) {
    addIcons({ locationOutline });
  }

  ionViewWillEnter() {
    this.tiendas.cargar();
  }
}
