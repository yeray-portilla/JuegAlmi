import { Component, computed, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
  IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel, AlertController,
} from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService, Operacion } from '../api.service';
import { AvisoService } from '../aviso.service';

@Component({
  standalone: true,
  selector: 'app-mis-operaciones',
  templateUrl: './mis-operaciones.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
    IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel],
})
export class MisOperacionesPage {
  datos = signal<Operacion[]>([]);
  cargando = signal(false);
  error = signal('');
  vista = signal<'compra' | 'alquiler' | 'reparacion'>('compra');
  devolviendo = signal<number | null>(null);
  cancelando = signal<number | null>(null);

  lista = computed(() => this.datos().filter(o => o.tipo === this.vista()));

  constructor(
    public api: ApiService,
    private aviso: AvisoService,
    private alertas: AlertController,
    ruta: ActivatedRoute,
  ) {
    ruta.queryParamMap.subscribe(params => {
      if (params.get('vista') === 'solicitudes') {
        this.vista.set('reparacion');
      }
    });
  }

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

  textoDetalle(op: Operacion): string {
    return (op.detalle ?? [])
      .map(d => (d.cantidad && d.cantidad > 1 ? `${d.nombre} x${d.cantidad}` : d.nombre))
      .join(', ');
  }

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


  textoEstado(estado: string): string {
    const textos: Record<string, string> = {
      pendiente: 'Pendiente de revisar',
      rechazada: 'Rechazada',
      en_proceso: 'En reparación',
      completada: 'Reparada',
    };
    return textos[estado] ?? estado;
  }

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
