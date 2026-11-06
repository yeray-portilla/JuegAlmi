import { Component, OnInit, signal, computed } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent,
} from '@ionic/angular';
import { ApiService, Dispositivo } from '../api.service';
import { OperacionesService } from '../operaciones.service';

@Component({
  standalone: true,
  selector: 'app-moviles',
  templateUrl: './moviles.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner,
    IonRefresher, IonRefresherContent],
})
export class MovilesPage implements OnInit {
  filtros = ['Todos', 'Smartphones', 'Tablets'];
  filtro = signal('Todos');

  // Usamos signals: la app no lleva zone.js y asi Angular sabe cuando repintar
  datos = signal<Dispositivo[]>([]); // telefonos y tablets juntos
  cargando = signal(false);
  error = signal('');

  enviando = signal(false); // true mientras se compra/alquila (botones desactivados)

  constructor(public api: ApiService, private operaciones: OperacionesService) {}

  ngOnInit() {
    this.cargar();
  }

  // Pedimos telefonos y tablets de una vez y filtramos en la app
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

  // Lista filtrada: se recalcula sola cuando cambian datos o filtro
  lista = computed(() => {
    if (this.filtro() === 'Smartphones') {
      return this.datos().filter(d => d.tipo === 'telefono');
    }
    if (this.filtro() === 'Tablets') {
      return this.datos().filter(d => d.tipo === 'tablet');
    }
    return this.datos();
  });

  // Compra 1 unidad y despues recarga la lista para ver el stock nuevo
  async comprar(producto: Dispositivo) {
    this.enviando.set(true);
    const enviado = await this.operaciones.comprar(producto);
    this.enviando.set(false);
    if (enviado) {
      this.cargar();
    }
  }
}
