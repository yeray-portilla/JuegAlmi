import { Component, computed, signal } from '@angular/core';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonIcon, IonSpinner,
  IonSegment, IonSegmentButton, IonLabel, IonTextarea, IonSelect, IonSelectOption, IonInput,
  IonRefresher, IonRefresherContent, AlertController,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { cameraOutline, trashOutline } from 'ionicons/icons';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { firstValueFrom, forkJoin } from 'rxjs';
import { ApiService, Aparato, Departamento, Marca, Operacion, Tienda } from '../api.service';
import { AvisoService } from '../aviso.service';
import { UbicacionService } from '../ubicacion.service';

type Vista = 'aparatos' | 'pedir' | 'solicitudes';

@Component({
  standalone: true,
  selector: 'app-soporte',
  templateUrl: './soporte.page.html',
  imports: [
    IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonIcon, IonSpinner,
    IonSegment, IonSegmentButton, IonLabel, IonTextarea, IonSelect, IonSelectOption, IonInput,
    IonRefresher, IonRefresherContent,
  ],
})
export class SoportePage {
  // ---- Estado general (todo con signals: la app no lleva zone.js) ----
  vista = signal<Vista>('aparatos');
  cargando = signal(false);
  error = signal('');
  enviando = signal(false); // true mientras se guarda algo (botones desactivados)

  // ---- Datos que vienen de la API ----
  marcas = signal<Marca[]>([]);
  departamentos = signal<Departamento[]>([]);
  tiendas = signal<Tienda[]>([]);
  aparatos = signal<Aparato[]>([]);
  solicitudes = signal<Operacion[]>([]); // las reparaciones de /operaciones/mis-operaciones

  // ---- Formulario "Mis aparatos" ----
  nuevaMarca = signal<number | null>(null);
  nuevoDepartamento = signal<number | null>(null);
  nuevoModelo = signal('');

  // ---- Formulario "Pedir reparacion" ----
  aparatoElegido = signal<number | null>(null);
  tiendaElegida = signal<number | null>(null);
  descripcion = signal('');
  fotoBase64 = signal(''); // sin el prefijo "data:image/...;base64,"
  fotoFormato = signal('jpeg');

  // true si la API ya manda los departamentos de cada tienda (cambio del backend)
  hayFiltroTiendas = computed(() => this.tiendas().some(t => Array.isArray(t.departamentos)));

  // Tiendas que reparan el tipo de aparato elegido.
  // Si la API todavia no manda "departamentos", salen todas (y la API avisara si no la repara).
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
    private alertas: AlertController,
  ) {
    addIcons({ cameraOutline, trashOutline });
  }

  // Cada vez que se entra en la pestaña recargamos (por si ha cambiado algo)
  ionViewWillEnter() {
    this.cargar();
  }

  // Pide todo lo que necesita la pagina a la vez
  cargar(event?: any) {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({
      marcas: this.api.obtenerMarcas(),
      departamentos: this.api.obtenerDepartamentos(),
      tiendas: this.api.obtenerTiendas(),
      aparatos: this.api.obtenerMisDispositivos(),
      operaciones: this.api.obtenerMisOperaciones(),
    }).subscribe({
      next: (r) => {
        this.marcas.set(r.marcas);
        this.departamentos.set(r.departamentos);
        this.tiendas.set(r.tiendas);
        this.aparatos.set(r.aparatos);
        this.solicitudes.set(r.operaciones.filter(o => o.tipo === 'reparacion'));
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

  // Recarga solo aparatos y solicitudes (despues de guardar algo)
  private recargarMisDatos() {
    forkJoin({
      aparatos: this.api.obtenerMisDispositivos(),
      operaciones: this.api.obtenerMisOperaciones(),
    }).subscribe({
      next: (r) => {
        this.aparatos.set(r.aparatos);
        this.solicitudes.set(r.operaciones.filter(o => o.tipo === 'reparacion'));
      },
      error: (e) => this.aviso.mostrar(this.api.mensajeError(e), 'danger'),
    });
  }

  cambiarVista(evento: any) {
    this.vista.set(evento.detail.value);
  }

  // =================== Mis aparatos ===================

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
      // La API no duplica: si ya existia devuelve el mismo id
      const yaEstaba = this.aparatos().some(a => a.id === aparato.id);
      this.aviso.mostrar(yaEstaba ? 'Ya tenías ese aparato guardado.' : 'Aparato guardado.');
      this.nuevoModelo.set('');
      this.recargarMisDatos();
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.enviando.set(false);
  }

  // Desde "Mis aparatos": ir directamente a pedir la reparacion de ese aparato
  repararAparato(aparato: Aparato) {
    this.elegirAparato(aparato.id);
    this.vista.set('pedir');
  }

  // =================== Pedir reparacion ===================

  // Al cambiar de aparato, la tienda elegida puede dejar de valer
  elegirAparato(id: number | null) {
    this.aparatoElegido.set(id);
    const sigueValiendo = this.tiendasDisponibles().some(t => t.id === this.tiendaElegida());
    if (!sigueValiendo) {
      this.tiendaElegida.set(null);
    }
  }

  // Foto con la camara, calidad 65 y en base64 (lo que pide la API en foto_base64).
  // getPhoto esta marcado como obsoleto en Camera 8, pero es el que devuelve la foto entera en base64.
  async hacerFoto() {
    try {
      const foto = await Camera.getPhoto({
        quality: 65,
        width: 1280, // asi la foto no pasa de los 5 MB que admite la API
        resultType: CameraResultType.Base64,
        source: CameraSource.Camera,
      });
      this.fotoBase64.set(foto.base64String ?? '');
      this.fotoFormato.set(foto.format || 'jpeg');
    } catch {
      // El usuario ha cancelado o no ha dado permiso: no hacemos nada
    }
  }

  quitarFoto() {
    this.fotoBase64.set('');
  }

  // Para ver la foto en la pantalla
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
    const gps = await this.ubicacion.obtener(); // 0, 0 si no hay permiso
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
      // Limpiamos el formulario y vamos a "Mis solicitudes"
      this.aparatoElegido.set(null);
      this.tiendaElegida.set(null);
      this.descripcion.set('');
      this.fotoBase64.set('');
      this.vista.set('solicitudes');
      this.recargarMisDatos();
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.enviando.set(false);
  }

  // =================== Mis solicitudes ===================

  // Texto que se ve para cada estado
  textoEstado(estado: string): string {
    const textos: Record<string, string> = {
      pendiente: 'Pendiente de revisar',
      rechazada: 'Rechazada',
      en_proceso: 'En reparación',
      completada: 'Reparada',
    };
    return textos[estado] ?? estado;
  }

  async editarDescripcion(solicitud: Operacion) {
    const alerta = await this.alertas.create({
      header: 'Editar descripción',
      inputs: [{ name: 'descripcion', type: 'textarea', value: solicitud.descripcion ?? '' }],
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        { text: 'Guardar', role: 'confirm' },
      ],
    });
    await alerta.present();
    const { role, data } = await alerta.onDidDismiss();
    const texto = (data?.values?.descripcion ?? '').trim();
    if (role !== 'confirm') {
      return;
    }
    if (!texto) {
      this.aviso.mostrar('La descripción no puede estar vacía.', 'danger');
      return;
    }
    this.enviando.set(true);
    try {
      await firstValueFrom(this.api.editarSolicitud(solicitud.solicitud_id ?? solicitud.id, texto));
      this.aviso.mostrar('Descripción actualizada.');
      this.recargarMisDatos();
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.enviando.set(false);
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
    this.enviando.set(true);
    try {
      await firstValueFrom(this.api.cancelarSolicitud(solicitud.solicitud_id ?? solicitud.id));
      this.aviso.mostrar('Solicitud cancelada.');
      this.recargarMisDatos();
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
    }
    this.enviando.set(false);
  }
}
