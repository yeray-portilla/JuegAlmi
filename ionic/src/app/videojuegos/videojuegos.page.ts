import { Component } from '@angular/core';
import { IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton } from '@ionic/angular';

@Component({
  standalone: true,
  selector: 'app-videojuegos',
  templateUrl: './videojuegos.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton],
})
export class VideojuegosPage {
  // Datos de ejemplo. TODO: sustituir por GET /api/videojuegos
  datos = [
    { id: 1, titulo: 'Juego de ejemplo 1', plataforma: 'PS5', venta: 60, alquiler: 4, foto: 'assets/icon/favicon.png' },
    { id: 2, titulo: 'Juego de ejemplo 2', plataforma: 'Switch', venta: 50, alquiler: 3, foto: 'assets/icon/favicon.png' },
  ];
}
