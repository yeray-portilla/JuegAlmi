import { Component } from '@angular/core';
import { IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton } from '@ionic/angular';

@Component({
  selector: 'app-consolas',
  templateUrl: './consolas.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton],
})
export class ConsolasPage {
  // Datos de ejemplo. TODO: sustituir por GET /api/productos?tipo=consola
  datos = [
    { id: 7, nombre: 'PlayStation 5', marca: 'Sony', almacenamiento: '825 GB', precio: 549.99, foto: 'assets/icon/favicon.png' },
    { id: 8, nombre: 'Xbox Series X', marca: 'Microsoft', almacenamiento: '1 TB', precio: 499.99, foto: 'assets/icon/favicon.png' },
    { id: 9, nombre: 'Nintendo Switch OLED', marca: 'Nintendo', almacenamiento: '64 GB', precio: 349.99, foto: 'assets/icon/favicon.png' },
  ];
}
