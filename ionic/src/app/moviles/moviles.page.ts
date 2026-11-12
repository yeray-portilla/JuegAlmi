import { Component, OnInit, signal, computed } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
  IonRefresher, IonRefresherContent,
} from '@ionic/angular';
import { ApiService, Dispositivo } from '../api.service';
import { OperacionesService } from '../operaciones.service';

@Component({
  standalone: true,
  selector: 'app-moviles',
  templateUrl: './moviles.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
    IonRefresher, IonRefresherContent],
})
export class MovilesPage implements OnInit {
  filtros = ['Todos', 'Smartphones', 'Tablets'];
  filtro = signal('Todos');

  datos = signal<Dispositivo[]>([]);
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
    this.api.obtenerDispositivos().subscribe({
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

  lista = computed(() => {
    if (this.filtro() === 'Smartphones') {
      return this.datos().filter(d => d.tipo === 'telefono');
    }
    if (this.filtro() === 'Tablets') {
      return this.datos().filter(d => d.tipo === 'tablet');
    }
    return this.datos();
  });

  async comprar(producto: Dispositivo) {
    this.enviando.set(true);
    const enviado = await this.operaciones.comprar(producto);
    this.enviando.set(false);
    if (enviado) {
      this.cargar();
    }
  }
}
