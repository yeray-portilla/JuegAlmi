import { Injectable } from '@angular/core';
import { AlertController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService, Producto, Videojuego } from './api.service';
import { AvisoService } from './aviso.service';
import { UbicacionService } from './ubicacion.service';

@Injectable({ providedIn: 'root' })
export class OperacionesService {
  constructor(
    private api: ApiService,
    private aviso: AvisoService,
    private ubicacion: UbicacionService,
    private alertas: AlertController,
  ) {}

  async comprar(producto: Producto): Promise<boolean> {
    const confirmado = await this.confirmar('Comprar', `¿Comprar "${producto.nombre}" por ${producto.precio} €?`);
    if (!confirmado) {
      return false;
    }
    const gps = await this.ubicacion.obtener();
    try {
      const r = await firstValueFrom(
        this.api.comprar([{ id: producto.id, cantidad: 1 }], gps.latitud, gps.longitud));
      this.aviso.mostrar(`Compra realizada. Total: ${r.total} €`);
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    return true;
  }

  async alquilar(videojuego: Videojuego, inicio: string, fin: string): Promise<boolean> {
    const gps = await this.ubicacion.obtener();
    try {
      const r = await firstValueFrom(
        this.api.alquilar([videojuego.id], inicio, fin, gps.latitud, gps.longitud));
      this.aviso.mostrar(`Alquiler realizado. Total: ${r.total} €`);
      return true;
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
      return true;
    }
  }

  private async confirmar(titulo: string, mensaje: string): Promise<boolean> {
    const alerta = await this.alertas.create({
      header: titulo,
      message: mensaje,
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        { text: 'Aceptar', role: 'confirm' },
      ],
    });
    await alerta.present();
    const { role } = await alerta.onDidDismiss();
    return role === 'confirm';
  }
}

export function fechaTexto(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}
