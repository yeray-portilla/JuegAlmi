import { Component, OnInit, signal } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent,
} from '@ionic/angular';
import { ApiService, Videojuego } from '../api.service';

@Component({
  standalone: true,
  selector: 'app-videojuegos',
  templateUrl: './videojuegos.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner,
    IonRefresher, IonRefresherContent],
})
export class VideojuegosPage implements OnInit {
  // Usamos signals: la app no lleva zone.js y asi Angular sabe cuando repintar
  datos = signal<Videojuego[]>([]);
  cargando = signal(false);
  error = signal('');

  constructor(public api: ApiService) {}

  ngOnInit() {
    this.cargar();
  }

  // Pide los videojuegos a la API (event = el "tirar para recargar", si lo hay)
  cargar(event?: any) {
    this.cargando.set(true);
    this.error.set('');
    this.api.obtenerVideojuegos().subscribe({
      next: (lista) => {
        this.datos.set(lista);
        this.cargando.set(false);
        event?.target.complete();
      },
      error: (e) => {
        this.error.set(this.api.mensajeError(e));
        this.cargando.set(false);
        event?.target.complete();
      },
    });
  }
}
