import { Component, computed, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel, AlertController,
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
  // Pestaña elegida arriba. 'reparacion' = "Mis solicitudes" (coincide con el tipo que manda la API)
  vista = signal<'compra' | 'alquiler' | 'reparacion'>('compra');
  devolviendo = signal<number | null>(null); // id del alquiler que se esta devolviendo
  cancelando = signal<number | null>(null); // id de la solicitud que se esta cancelando

  // Solo las operaciones de la pestaña elegida (compras, alquileres o solicitudes de reparacion)
  lista = computed(() => this.datos().filter(o => o.tipo === this.vista()));

  constructor(
    public api: ApiService,
    private aviso: AvisoService,
    private alertas: AlertController,
    ruta: ActivatedRoute,
  ) {
    // Desde Soporte, al enviar una solicitud, se abre /mis-operaciones?vista=solicitudes
    ruta.queryParamMap.subscribe(params => {
      if (params.get('vista') === 'solicitudes') {
        this.vista.set('reparacion');
      }
    });
  }

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

  // =================== Mis solicitudes (reparaciones) ===================

  // Texto que se ve para cada estado
  textoEstado(estado: string): string {
    const textos: Record<string, string> = {
      pendiente: 'Pendiente de revisar',
      rechazada: 'Rechazada',
      en_proceso: 'En reparación',
      completada: 'Reparada',
    };
    return textos[estado] ?? estado;
  }

  // Cancelar una solicitud pendiente (pregunta antes)
  async cancelarSolicitud(solicitud: Operacion) {
    const alerta = await this.alertas.create({
      header: 'Cancelar solicitud',
      message: `¿Seguro que quieres cancelar la reparación de "${solicitud.dispositivo}"?`,
      buttons: [
        { text: 'No', role: 'cancel' },
        { text: 'Sí, cancelar', role: 'confirm' },
      ],
    });
    await alerta.present();
    const { role } = await alerta.onDidDismiss();
    if (role !== 'confirm') {
      return;
    }
    this.cancelando.set(solicitud.id);
    try {
      await firstValueFrom(this.api.cancelarSolicitud(solicitud.solicitud_id ?? solicitud.id));
      this.aviso.mostrar('Solicitud cancelada.');
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.cancelando.set(null);
    this.cargar();
  }
}
