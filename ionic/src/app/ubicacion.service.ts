import { Injectable } from '@angular/core';
import { Geolocation } from '@capacitor/geolocation';

export interface Ubicacion {
  latitud: number;
  longitud: number;
}

// Lo que se manda si no hay GPS (la API exige latitud y longitud siempre)
const SIN_GPS: Ubicacion = { latitud: 0, longitud: 0 };

@Injectable({ providedIn: 'root' })
export class UbicacionService {
  // Pide la posicion GPS. Si el usuario la niega, falla o tarda mas de 10 s, devolvemos 0, 0.
  async obtener(): Promise<Ubicacion> {
    // El timeout del GPS no cuenta mientras el aviso de permiso esta sin contestar,
    // asi que ponemos nuestro propio limite para que la app no se quede esperando.
    const limite = new Promise<Ubicacion>(resolver => setTimeout(() => resolver(SIN_GPS), 10000));
    return Promise.race([this.leerGps(), limite]);
  }

  private async leerGps(): Promise<Ubicacion> {
    try {
      const posicion = await Geolocation.getCurrentPosition({ enableHighAccuracy: false, timeout: 8000, maximumAge: 60000 });
      return { latitud: posicion.coords.latitude, longitud: posicion.coords.longitude };
    } catch {
      return SIN_GPS;
    }
  }
}
