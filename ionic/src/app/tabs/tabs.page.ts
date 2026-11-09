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

// Opciones del menu lateral. Ubicación, Galería e Historia tienen cada una su pagina.
abrir(opcion: string) {
  if (opcion === 'Servicios') {
    this.router.navigateByUrl('/tabs');
  }
  if (opcion === 'Ubicación') {
    this.router.navigateByUrl('/ubicacion');
  }
  if (opcion === 'Galería') {
    this.router.navigateByUrl('/galeria');
  }
  if (opcion === 'Historia') {
    this.router.navigateByUrl('/historia');
  }
  if (opcion === 'Mis operaciones') {
    this.router.navigateByUrl('/mis-operaciones');
  }
  if (opcion === 'Administración') {
    this.router.navigateByUrl('/admin');
  }
}

salir() {
  this.auth.logout();
  this.router.navigateByUrl('/login');
}

}
