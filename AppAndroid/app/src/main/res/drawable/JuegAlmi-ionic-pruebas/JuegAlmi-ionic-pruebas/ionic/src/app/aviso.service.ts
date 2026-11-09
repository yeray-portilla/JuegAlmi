import { Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';

// Muestra mensajes cortos (toast) abajo de la pantalla
@Injectable({ providedIn: 'root' })
export class AvisoService {
  constructor(private toast: ToastController) {}

  async mostrar(mensaje: string, color: 'success' | 'danger' = 'success') {
    const t = await this.toast.create({ message: mensaje, duration: 2500, color, position: 'bottom' });
    await t.present();
  }
}
