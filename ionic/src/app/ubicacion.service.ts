import { Injectable } from '@angular/core';
import { LoadingController } from '@ionic/angular';
import { Geolocation } from '@capacitor/geolocation';

export interface Ubicacion {
  latitud: number;
  longitud: number;
}

// Lo que se manda si no hay GPS (la API exige latitud y longitud siempre)
const SIN_GPS: Ubicacion = { latitud: 0, longitud: 0 };

// Tiempo maximo esperando al GPS
const LIMITE_MS = 5000;

@Injectable({ providedIn: 'root' })
export class UbicacionService {
  constructor(private loading: LoadingController) {}

  // Pide la posicion GPS. Si el usuario la niega, falla o tarda mas de 5 s, devolvemos 0, 0.
  // Mientras tanto se ve un spinner con "Obteniendo ubicación…".
  async obtener(): Promise<Ubicacion> {
    const aviso = await this.loading.create({ message: 'Obteniendo ubicación…', spinner: 'crescent' });
    await aviso.present();

    // El timeout del GPS no cuenta mientras el aviso de permiso esta sin contestar,
    // asi que ponemos nuestro propio limite para que la app no se quede esperando.
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
