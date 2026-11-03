import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { IonContent, IonButton, IonInput } from '@ionic/angular';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  imports: [IonContent, IonButton, IonInput, FormsModule],
})
export class LoginPage {
  email = '';
  password = '';
  error = '';
  cargando = false;

  constructor(private router: Router, private auth: AuthService) {}

  entrar() {
    if (!this.email || !this.password) {
      this.error = 'Rellena el correo y la contraseña.';
      return;
    }
    this.error = '';
    this.cargando = true;
    this.auth.login(this.email, this.password).subscribe({
      next: () => this.router.navigateByUrl('/tabs/moviles'),
      error: (e) => {
        this.cargando = false;
        this.error = e.status === 401
          ? 'Correo o contraseña incorrectos.'
          : 'No se pudo conectar con el servidor.';
      },
    });
  }
}
