import { Injectable } from '@angular/core';
import { LoadingController } from '@ionic/angular';
import { Geolocation } from '@capacitor/geolocation';

export interface Ubicacion {
  latitud: number;
  longitud: number;
}

const SIN_GPS: Ubicacion = { latitud: 0, longitud: 0 };

const LIMITE_MS = 5000;

@Injectable({ providedIn: 'root' })
export class UbicacionService {
  constructor(private loading: LoadingController) {}

  async obtener(): Promise<Ubicacion> {
    const aviso = await this.loading.create({ message: 'Obteniendo ubicación…', spinner: 'crescent' });
    await aviso.present();

    const limite = new Promise<Ubicacion>(resolver => setTimeout(() => resolver(SIN_GPS), LIMITE_MS));
    try {
      return await Promise.race([this.leerGps(), limite]);
    } finally {
      await aviso.dismiss();
    }
  }

  private async leerGps(): Promise<Ubicacion> {
    try {
      const posicion = await Geolocation.getCurrentPosition({ enableHighAccuracy: false, timeout: LIMITE_MS, maximumAge: 60000 });
      return { latitud: posicion.coords.latitude, longitud: posicion.coords.longitude };
    } catch {
      return SIN_GPS;
    }
  }
}
