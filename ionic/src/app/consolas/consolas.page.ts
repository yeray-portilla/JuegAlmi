import { Component, OnInit, signal } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
  IonRefresher, IonRefresherContent,
} from '@ionic/angular';
import { ApiService, Consola } from '../api.service';
import { OperacionesService } from '../operaciones.service';

@Component({
  standalone: true,
  selector: 'app-consolas',
  templateUrl: './consolas.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
    IonRefresher, IonRefresherContent],
})
export class ConsolasPage implements OnInit {
  datos = signal<Consola[]>([]);
  cargando = signal(false);
  error = signal('');

  enviando = signal(false);

  constructor(public api: ApiService, private operaciones: OperacionesService) {}

  ngOnInit() {
    this.cargar();
  }

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

  async comprar(producto: Consola) {
    this.enviando.set(true);
    const enviado = await this.operaciones.comprar(producto);
    this.enviando.set(false);
    if (enviado) {
      this.cargar();
    }
  }
}
