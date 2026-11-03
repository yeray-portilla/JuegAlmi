import { Component } from '@angular/core';
import { IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton } from '@ionic/angular';

@Component({
  standalone: true,
  selector: 'app-moviles',
  templateUrl: './moviles.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton],
})
export class MovilesPage {
  filtros = ['Todos', 'Smartphones', 'Tablets'];
  filtro = 'Todos';

  // Datos de ejemplo. TODO: sustituir por GET /api/dispositivos
  datos = [
    { id: 1, nombre: 'Smartphone X', tipo: 'Smartphones', precio: 449, foto: 'assets/icon/favicon.png' },
    { id: 2, nombre: 'iPhone 14', tipo: 'Smartphones', precio: 799, foto: 'assets/icon/favicon.png' },
    { id: 3, nombre: 'Tablet Y', tipo: 'Tablets', precio: 329, foto: 'assets/icon/favicon.png' },
  ];

  get lista() {
    return this.filtro === 'Todos' ? this.datos : this.datos.filter(d => d.tipo === this.filtro);
  }
}
