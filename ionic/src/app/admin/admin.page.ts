import { Component, signal } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
  IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel, AlertController,
} from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService, SolicitudAdmin } from '../api.service';
import { AvisoService } from '../aviso.service';

type Filtro = 'pendiente' | 'aceptada' | 'rechazada';

@Component({
  standalone: true,
  selector: 'app-admin',
  templateUrl: './admin.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonBackButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
    IonRefresher, IonRefresherContent, IonSegment, IonSegmentButton, IonLabel],
})
export class AdminPage {
  filtro = signal<Filtro>('pendiente');
  datos = signal<SolicitudAdmin[]>([]);
  cargando = signal(false);
  error = signal('');
  enviando = signal<number | null>(null);

  completadas = signal<Record<number, number | null>>({});

  constructor(public api: ApiService, private aviso: AvisoService, private alertas: AlertController) {}

  ionViewWillEnter() {
    this.cargar();
  }

  cargar(event?: any) {
    this.cargando.set(true);
    this.error.set('');
    this.api.obtenerSolicitudesAdmin(this.filtro()).subscribe({
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

  cambiarFiltro(evento: any) {
    this.filtro.set(evento.detail.value);
    this.datos.set([]);
    this.cargar();
  }


  estaCompletada(s: SolicitudAdmin): boolean {
    if (s.reparacion_estado === 'completada') {
      return true;
    }
    return s.reparacion_id !== null && s.reparacion_id in this.completadas();
  }

  precioFinal(s: SolicitudAdmin): number | null {
    if (s.precio_final !== undefined && s.precio_final !== null) {
      return s.precio_final;
    }
    return s.reparacion_id !== null ? this.completadas()[s.reparacion_id] ?? null : null;
  }

  tieneUbicacion(s: SolicitudAdmin): boolean {
    return s.latitud !== 0 || s.longitud !== 0;
  }

  urlMapa(s: SolicitudAdmin): string {
    return `https://www.google.com/maps?q=${s.latitud},${s.longitud}`;
  }


  async aceptar(s: SolicitudAdmin) {
    const precio = await this.pedirNumero('Aceptar solicitud',
      `Precio estimado para "${s.dispositivo}" (€):`, 'precio', false);
    if (precio === null) {
      return;
    }
    await this.enviar(s, () => this.api.aceptarSolicitud(s.id, precio), 'Solicitud aceptada. Reparación creada.');
  }

  async rechazar(s: SolicitudAdmin) {
    const alerta = await this.alertas.create({
      header: 'Rechazar solicitud',
      message: `Motivo del rechazo para "${s.dispositivo}":`,
      inputs: [{ name: 'motivo', type: 'textarea', placeholder: 'Ej.: no hay recambios para este modelo' }],
      buttons: [{ text: 'Cancelar', role: 'cancel' }, { text: 'Rechazar', role: 'confirm' }],
    });
    await alerta.present();
    const { role, data } = await alerta.onDidDismiss();
    if (role !== 'confirm') {
      return;
    }
    const motivo = (data?.values?.motivo ?? '').trim();
    if (!motivo) {
      this.aviso.mostrar('Debes indicar el motivo del rechazo.', 'danger');
      return;
    }
    await this.enviar(s, () => this.api.rechazarSolicitud(s.id, motivo), 'Solicitud rechazada.');
  }

  async completar(s: SolicitudAdmin) {
    if (s.reparacion_id === null) {
      return;
    }
    const reparacionId = s.reparacion_id;
    const precio = await this.pedirNumero('Completar reparación',
      `Precio final de la reparación de "${s.dispositivo}" (€):`, 'precio', true);
    if (precio === null) {
      return;
    }
    this.enviando.set(s.id);
    try {
      await firstValueFrom(this.api.actualizarReparacion(reparacionId, 'completada', precio));
      this.aviso.mostrar('Reparación completada.');
      this.completadas.update(c => ({ ...c, [reparacionId]: precio }));
    } catch (e: any) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
      if (e?.status === 409) {
        this.completadas.update(c => ({ ...c, [reparacionId]: null }));
      }
    }
    this.enviando.set(null);
  }

  private async enviar(s: SolicitudAdmin, peticion: () => any, mensajeOk: string) {
    this.enviando.set(s.id);
    try {
      await firstValueFrom(peticion());
      this.aviso.mostrar(mensajeOk);
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.enviando.set(null);
    this.cargar();
  }

  private async pedirNumero(titulo: string, mensaje: string, nombre: string, permitirCero: boolean): Promise<number | null> {
    const alerta = await this.alertas.create({
      header: titulo,
      message: mensaje,
      inputs: [{ name: nombre, type: 'number', min: 0, placeholder: '0.00', attributes: { step: '0.01' } }],
      buttons: [{ text: 'Cancelar', role: 'cancel' }, { text: 'Aceptar', role: 'confirm' }],
    });
    await alerta.present();
    const { role, data } = await alerta.onDidDismiss();
    if (role !== 'confirm') {
      return null;
    }
    const texto = String(data?.values?.[nombre] ?? '').replace(',', '.');
    const numero = Number(texto);
    const valido = texto !== '' && !isNaN(numero) && (permitirCero ? numero >= 0 : numero > 0);
    if (!valido) {
      this.aviso.mostrar(permitirCero ? 'Escribe un precio válido (0 o más).' : 'Escribe un precio mayor que 0.', 'danger');
      return null;
    }
    return numero;
  }
}
