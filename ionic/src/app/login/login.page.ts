import { Component, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { IonContent, IonButton, IonInput, ToastController } from '@ionic/angular';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  imports: [IonContent, IonButton, IonInput, FormsModule],
})
export class LoginPage {
  login = ''; // email o nombre de usuario
  password = '';
  error = '';
  cargando = signal(false); // signal: sin zone.js, asi el boton se repinta

  constructor(private router: Router, private auth: AuthService, private toast: ToastController) {}

  // Si ya hay sesion, vamos directos al catalogo
  ionViewWillEnter() {
    if (this.auth.logueado) {
      this.router.navigateByUrl('/tabs/moviles');
    }
  }

  entrar() {
    if (!this.login || !this.password) {
      this.error = 'Rellena el usuario y la contraseña.';
      return;
    }
    this.error = '';
    this.cargando.set(true);
    this.auth.login(this.login, this.password).subscribe({
      next: () => {
        this.cargando.set(false);
        this.password = '';
        this.router.navigateByUrl('/tabs/moviles');
      },
      error: (e) => {
        this.cargando.set(false);
        // La API manda {"error": "mensaje"}; si no hay respuesta, es problema de conexion
        const mensaje = e.error?.error ?? 'No se pudo conectar con el servidor.';
        this.mostrarToast(mensaje);
      },
    });
  }

  irARegistro() {
    this.router.navigateByUrl('/registro');
  }

  async mostrarToast(mensaje: string) {
    const t = await this.toast.create({ message: mensaje, duration: 2500, color: 'danger', position: 'bottom' });
    await t.present();
  }
}
