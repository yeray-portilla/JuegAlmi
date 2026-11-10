import { Component, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent, IonButton, IonInput } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../api.service';
import { AuthService } from '../auth.service';
import { AvisoService } from '../aviso.service';

@Component({
  standalone: true,
  selector: 'app-registro',
  templateUrl: './registro.page.html',
  imports: [IonContent, IonButton, IonInput],
})
export class RegistroPage {
  // Limites (la contraseña minima la exige la API; el maximo del usuario es el de la columna)
  readonly MAX_USUARIO = 180;
  readonly MIN_PASSWORD = 6;

  // Campos del formulario (signals: la app no lleva zone.js)
  nombre = signal('');
  email = signal('');
  password = signal('');
  repetir = signal('');
  cargando = signal(false);
  intentado = signal(false); // los errores solo se ven despues de pulsar "Crear cuenta"

  // Un mensaje por campo ('' = todo bien)
  errorNombre = computed(() => {
    const n = this.nombre().trim();
    if (!n) return 'Escribe un nombre de usuario.';
    if (n.length > this.MAX_USUARIO) return `Máximo ${this.MAX_USUARIO} caracteres.`;
    return '';
  });

  errorEmail = computed(() => {
    const e = this.email().trim();
    if (!e) return 'Escribe tu correo.';
    // Formato sencillo: algo@algo.algo, sin espacios
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e)) return 'El correo no tiene un formato válido.';
    return '';
  });

  errorPassword = computed(() =>
    this.password().length < this.MIN_PASSWORD ? `La contraseña debe tener al menos ${this.MIN_PASSWORD} caracteres.` : '');

  errorRepetir = computed(() =>
    this.repetir() !== this.password() ? 'Las contraseñas no coinciden.' : '');

  formularioValido = computed(() =>
    !this.errorNombre() && !this.errorEmail() && !this.errorPassword() && !this.errorRepetir());

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private aviso: AvisoService,
    private router: Router,
  ) {}

  // Si ya hay sesion, no tiene sentido registrarse: al catalogo
  ionViewWillEnter() {
    if (this.auth.logueado) {
      this.router.navigateByUrl('/tabs/moviles');
    }
  }

  // Lee el valor de un ion-input
  valor(evento: any): string {
    return evento.detail.value ?? '';
  }

  async crearCuenta() {
    this.intentado.set(true);
    if (!this.formularioValido()) {
      return;
    }
    const nombre = this.nombre().trim();
    const email = this.email().trim();
    const password = this.password();

    this.cargando.set(true);

    // 1) Crear la cuenta. Si falla (409 ya existe, 400 datos mal), mostramos el mensaje de la API.
    try {
      await firstValueFrom(this.api.registrarUsuario(nombre, email, password));
    } catch (e) {
      this.aviso.mostrar(this.api.mensajeError(e), 'danger');
      this.cargando.set(false);
      return;
    }

    // 2) Login automatico con las mismas credenciales (el registro no devuelve token)
    try {
      await firstValueFrom(this.auth.login(nombre, password));
      this.aviso.mostrar(`¡Bienvenido, ${nombre}!`);
      this.limpiar();
      this.router.navigateByUrl('/tabs/moviles');
    } catch (e) {
      // La cuenta ya existe, pero no se pudo entrar: que lo intente desde el login
      this.aviso.mostrar('Cuenta creada, pero no se pudo iniciar sesión. Entra desde el login.', 'danger');
      this.router.navigateByUrl('/login');
    }
    this.cargando.set(false);
  }

  irALogin() {
    this.router.navigateByUrl('/login');
  }

  private limpiar() {
    this.nombre.set('');
    this.email.set('');
    this.password.set('');
    this.repetir.set('');
    this.intentado.set(false);
  }
}
