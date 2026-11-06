import { Component, OnInit, signal } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner,
  IonRefresher, IonRefresherContent, IonModal, IonInput, IonTitle,
} from '@ionic/angular';
import { ApiService, Videojuego } from '../api.service';
import { OperacionesService, fechaTexto } from '../operaciones.service';

@Component({
  standalone: true,
  selector: 'app-videojuegos',
  templateUrl: './videojuegos.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner,
    IonRefresher, IonRefresherContent, IonModal, IonInput, IonTitle],
})
export class VideojuegosPage implements OnInit {
  // Usamos signals: la app no lleva zone.js y asi Angular sabe cuando repintar
  datos = signal<Videojuego[]>([]);
  cargando = signal(false);
  error = signal('');
  enviando = signal(false); // true mientras se compra/alquila (botones desactivados)

  // Ventana para elegir las fechas del alquiler
  juegoAlquiler = signal<Videojuego | null>(null); // null = ventana cerrada
  fechaInicio = signal('');
  fechaFin = signal('');
  errorFechas = signal('');
  hoy = fechaTexto(new Date());

  constructor(public api: ApiService, private operaciones: OperacionesService) {}

  ngOnInit() {
    this.cargar();
  }

  // Pide los videojuegos a la API (event = el "tirar para recargar", si lo hay)
  cargar(event?: any) {
    this.cargando.set(true);
    this.error.set('');
    this.api.obtenerVideojuegos().subscribe({
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

  // Compra 1 unidad y despues recarga la lista para ver el stock nuevo
  async comprar(videojuego: Videojuego) {
    this.enviando.set(true);
    const enviado = await this.operaciones.comprar(videojuego);
    this.enviando.set(false);
    if (enviado) {
      this.cargar();
    }
  }

  // Abre la ventana de fechas: por defecto, de hoy a dentro de 7 dias
  abrirAlquiler(videojuego: Videojuego) {
    const dentroDeUnaSemana = new Date();
    dentroDeUnaSemana.setDate(dentroDeUnaSemana.getDate() + 7);
    this.fechaInicio.set(this.hoy);
    this.fechaFin.set(fechaTexto(dentroDeUnaSemana));
    this.errorFechas.set('');
    this.juegoAlquiler.set(videojuego);
  }

  cerrarAlquiler() {
    this.juegoAlquiler.set(null);
  }

  async confirmarAlquiler() {
    const juego = this.juegoAlquiler();
    const inicio = this.fechaInicio();
    const fin = this.fechaFin();
    // Las fechas yyyy-MM-dd se pueden comparar como texto
    if (!inicio || !fin) {
      this.errorFechas.set('Elige las dos fechas.');
      return;
    }
    if (inicio < this.hoy) {
      this.errorFechas.set('La fecha de inicio no puede ser anterior a hoy.');
      return;
    }
    if (fin < inicio) {
      this.errorFechas.set('La fecha de fin no puede ser anterior a la de inicio.');
      return;
    }
    if (!juego) {
      return;
    }
    this.cerrarAlquiler();
    this.enviando.set(true);
    await this.operaciones.alquilar(juego, inicio, fin);
    this.enviando.set(false);
    this.cargar(); // recargamos para ver las copias que quedan
  }
}
