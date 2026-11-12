import { Component, OnInit, signal } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
  IonRefresher, IonRefresherContent, IonModal, IonInput, IonTitle,
} from '@ionic/angular';
import { ApiService, Videojuego } from '../api.service';
import { OperacionesService, fechaTexto } from '../operaciones.service';

@Component({
  standalone: true,
  selector: 'app-videojuegos',
  templateUrl: './videojuegos.page.html',
  imports: [IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonSpinner, IonCard, IonImg,
    IonRefresher, IonRefresherContent, IonModal, IonInput, IonTitle],
})
export class VideojuegosPage implements OnInit {
  datos = signal<Videojuego[]>([]);
  cargando = signal(false);
  error = signal('');
  enviando = signal(false);

  juegoAlquiler = signal<Videojuego | null>(null);
  fechaInicio = signal('');
  fechaFin = signal('');
  errorFechas = signal('');
  hoy = fechaTexto(new Date());

  constructor(public api: ApiService, private operaciones: OperacionesService) {}

  ngOnInit() {
    this.cargar();
  }

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

  async comprar(videojuego: Videojuego) {
    this.enviando.set(true);
    const enviado = await this.operaciones.comprar(videojuego);
    this.enviando.set(false);
    if (enviado) {
      this.cargar();
    }
  }

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
    this.cargar();
  }
}
