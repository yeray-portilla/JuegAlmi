import { Component } from '@angular/core';
import { Router } from '@angular/router';
import {
  IonMenu, IonContent, IonList, IonItem, IonMenuToggle,
  IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { phonePortraitOutline, gameControllerOutline, constructOutline, tvOutline } from 'ionicons/icons';
import { AuthService } from '../auth.service';

@Component({
  standalone: true,
  selector: 'app-tabs',
  templateUrl: './tabs.page.html',
  imports: [IonMenu, IonContent, IonList, IonItem, IonMenuToggle, IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel],
})
export class TabsPage {
  opciones = ['Servicios', 'Ubicación', 'Galería', 'Historia', 'Mis operaciones'];

  constructor(private router: Router, public auth: AuthService) {
    addIcons({ phonePortraitOutline, gameControllerOutline, constructOutline, tvOutline });
  }

salir() {
  this.auth.logout();
  this.router.navigateByUrl('/login');
}

}
