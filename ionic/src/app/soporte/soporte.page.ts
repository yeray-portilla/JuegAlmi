import { Component, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonIcon, IonSpinner, IonCard,
  IonSegment, IonSegmentButton, IonLabel, IonTextarea, IonSelect, IonSelectOption, IonInput,
  IonRefresher, IonRefresherContent,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { cameraOutline, trashOutline } from 'ionicons/icons';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { firstValueFrom, forkJoin } from 'rxjs';
import { ApiService, Aparato, Departamento, Marca, Tienda } from '../api.service';
import { AvisoService } from '../aviso.service';
import { UbicacionService } from '../ubicacion.service';

type Vista = 'aparatos' | 'pedir';

@Component({
  standalone: true,
  selector: 'app-soporte',
  templateUrl: './soporte.page.html',
  imports: [
    IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonIcon, IonSpinner, IonCard,
    IonSegment, IonSegmentButton, IonLabel, IonTextarea, IonSelect, IonSelectOption, IonInput,
    IonRefresher, IonRefresherContent,
  ],
})
export class SoportePage {
  readonly LIMITE_DESCRIPCION = 255;
  readonly LIMITE_MODELO = 100;

  vista = signal<Vista>('aparatos');
  cargando = signal(false);
  error = signal('');
  enviando = signal(false);

  marcas = signal<Marca[]>([]);
  departamentos = signal<Departamento[]>([]);
  tiendas = signal<Tienda[]>([]);
  aparatos = signal<Aparato[]>([]);

  nuevaMarca = signal<number | null>(null);
  nuevoDepartamento = signal<number | null>(null);
  nuevoModelo = signal('');

  aparatoElegido = signal<number | null>(null);
  tiendaElegida = signal<number | null>(null);
  descripcion = signal('');
  fotoBase64 = signal('');
  fotoFormato = signal('jpeg');

  hayFiltroTiendas = computed(() => this.tiendas().some(t => Array.isArray(t.departamentos)));

  tiendasDisponibles = computed(() => {
    const aparato = this.aparatos().find(a => a.id === this.aparatoElegido());
    if (!aparato) {
      return [];
    }
    if (!this.hayFiltroTiendas()) {
      return this.tiendas();
    }
    return this.tiendas().filter(t => (t.departamentos ?? []).some(d => d.id === aparato.departamento_id));
  });

  constructor(
    public api: ApiService,
    private aviso: AvisoService,
    private ubicacion: UbicacionService,
    private router: Router,
  ) {
    addIcons({ cameraOutline, trashOutline });
  }

  ionViewWillEnter() {
    this.cargar();
  }

  cargar(event?: any) {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({
      marcas: this.api.obtenerMarcas(),
      departamentos: this.api.obtenerDepartamentos(),
      tiendas: this.api.obtenerTiendas(),
      aparatos: this.api.obtenerMisDispositivos(),
    }).subscribe({
      next: (r) => {
        this.marcas.set(r.marcas);
        this.departamentos.set(r.departamentos);
        this.tiendas.set(r.tiendas);
        this.aparatos.set(r.aparatos);
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

  private recargarAparatos() {
    this.api.obtenerMisDispositivos().subscribe({
      next: (lista) => this.aparatos.set(lista),
      error: (e) => this.aviso.mostrar(this.api.mensajeError(e), 'danger'),
    });
  }

  cambiarVista(evento: any) {
    this.vista.set(evento.detail.value);
  }


  async anadirAparato() {
    const marca = this.nuevaMarca();
    const departamento = this.nuevoDepartamento();
    const modelo = this.nuevoModelo().trim();
    if (!marca || !departamento || !modelo) {
      this.aviso.mostrar('Elige marca, tipo y escribe el modelo.', 'danger');
      return;
    }
    this.enviando.set(true);
    try {
      const aparato = await firstValueFrom(this.api.crearDispositivo(marca, departamento, modelo));
      const yaEstaba = this.aparatos().some(a => a.id === aparato.id);
      this.aviso.mostrar(yaEstaba ? 'Ya tenías ese aparato guardado.' : 'Aparato guardado.');
      this.nuevoModelo.set('');
      this.recargarAparatos();
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.enviando.set(false);
  }

  repararAparato(aparato: Aparato) {
    this.elegirAparato(aparato.id);
    this.vista.set('pedir');
  }


  elegirAparato(id: number | null) {
    this.aparatoElegido.set(id);
    const sigueValiendo = this.tiendasDisponibles().some(t => t.id === this.tiendaElegida());
    if (!sigueValiendo) {
      this.tiendaElegida.set(null);
    }
  }

  async hacerFoto() {
    try {
      const foto = await Camera.getPhoto({
        quality: 65,
        width: 1280,
        resultType: CameraResultType.Base64,
        source: CameraSource.Camera,
      });
      this.fotoBase64.set(foto.base64String ?? '');
      this.fotoFormato.set(foto.format || 'jpeg');
    } catch {
    }
  }

  quitarFoto() {
    this.fotoBase64.set('');
  }

  get fotoUrl(): string {
    return `data:image/${this.fotoFormato()};base64,${this.fotoBase64()}`;
  }

  async enviarSolicitud() {
    const aparato = this.aparatoElegido();
    const tienda = this.tiendaElegida();
    const descripcion = this.descripcion().trim();
    if (!aparato || !tienda || !descripcion) {
      this.aviso.mostrar('Elige aparato, tienda y describe el problema.', 'danger');
      return;
    }
    this.enviando.set(true);
    const gps = await this.ubicacion.obtener();
    try {
      await firstValueFrom(this.api.crearSolicitud({
        dispositivo_id: aparato,
        tienda_id: tienda,
        descripcion,
        latitud: gps.latitud,
        longitud: gps.longitud,
        ...(this.fotoBase64() ? { foto_base64: this.fotoBase64() } : {}),
      }));
      this.aviso.mostrar('Solicitud enviada. Un administrador la revisará.');
      this.aparatoElegido.set(null);
      this.tiendaElegida.set(null);
      this.descripcion.set('');
      this.fotoBase64.set('');
      this.router.navigateByUrl('/mis-operaciones?vista=solicitudes');
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.enviando.set(false);
  }
}
