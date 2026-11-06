import { Component, computed, signal } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel,
} from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService, Operacion } from '../api.service';
import { AvisoService } from '../aviso.service';

@Component({
  standalone: true,
  selector: 'app-mis-operaciones',
  templateUrl: './mis-operaciones.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
    IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel],
})
export class MisOperacionesPage {
  // Signals: la app no lleva zone.js
  datos = signal<Operacion[]>([]);
  cargando = signal(false);
  error = signal('');
  vista = signal<'compra' | 'alquiler'>('compra'); // pestaña elegida arriba
  devolviendo = signal<number | null>(null); // id del alquiler que se esta devolviendo

  // Solo compras o solo alquileres, segun la pestaña (las reparaciones iran en Soporte)
  lista = computed(() => this.datos().filter(o => o.tipo === this.vista()));

  constructor(public api: ApiService, private aviso: AvisoService) {}

  // Cada vez que se entra en la pantalla, recargamos
  ionViewWillEnter() {
    this.cargar();
  }

  cargar(event?: any) {
    this.cargando.set(true);
    this.error.set('');
    this.api.obtenerMisOperaciones().subscribe({
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

  cambiarVista(evento: any) {
    this.vista.set(evento.detail.value);
  }

  // Texto con lo que incluye la operacion: "Juego A x2, Juego B"
  textoDetalle(op: Operacion): string {
    return (op.detalle ?? [])
      .map(d => (d.cantidad && d.cantidad > 1 ? `${d.nombre} x${d.cantidad}` : d.nombre))
      .join(', ');
  }

  // Devolver un alquiler activo o cancelar uno pendiente (la API usa la misma ruta para los dos)
  async devolver(op: Operacion) {
    this.devolviendo.set(op.id);
    try {
      await firstValueFrom(this.api.devolverAlquiler(op.id));
      this.aviso.mostrar(op.estado === 'pendiente' ? 'Alquiler cancelado.' : 'Alquiler devuelto.');
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.devolviendo.set(null);
    this.cargar();
  }
}
