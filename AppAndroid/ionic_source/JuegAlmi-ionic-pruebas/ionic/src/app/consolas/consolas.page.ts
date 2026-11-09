import { Component, OnInit, signal } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent,
} from '@ionic/angular';
import { ApiService, Consola } from '../api.service';
import { OperacionesService } from '../operaciones.service';

@Component({
  standalone: true,
  selector: 'app-consolas',
  templateUrl: './consolas.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner,
    IonRefresher, IonRefresherContent],
})
export class ConsolasPage implements OnInit {
  // Usamos signals: la app no lleva zone.js y asi Angular sabe cuando repintar
  datos = signal<Consola[]>([]);
  cargando = signal(false);
  error = signal('');

  enviando = signal(false); // true mientras se compra/alquila (botones desactivados)

  constructor(public api: ApiService, private operaciones: OperacionesService) {}

  ngOnInit() {
    this.cargar();
  }

  // Pide las consolas a la API (event = el "tirar para recargar", si lo hay)
  cargar(event?: any) {
    this.cargando.set(true);
    this.error.set('');
    this.api.obtenerConsolas().subscribe({
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

  // Compra 1 unidad y despues recarga la lista para ver el stock nuevo
  async comprar(producto: Consola) {
    this.enviando.set(true);
    const enviado = await this.operaciones.comprar(producto);
    this.enviando.set(false);
    if (enviado) {
      this.cargar();
    }
  }
}
