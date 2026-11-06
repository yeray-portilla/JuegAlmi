import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService, LoginRespuesta, Usuario } from './api.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private api: ApiService) {}

  // Llama a la API y guarda token, roles y usuario en localStorage
  login(login: string, password: string): Observable<LoginRespuesta> {
    return this.api.iniciarSesion(login, password).pipe(
      tap(r => {
        localStorage.setItem('token', r.token);
        localStorage.setItem('roles', JSON.stringify(r.usuario.roles));
        localStorage.setItem('usuario', JSON.stringify(r.usuario));
      }),
    );
  }

  // Cerrar sesion: borramos todo lo guardado
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('usuario');
  }

  get logueado(): boolean {
    return !!localStorage.getItem('token');
  }

  // Datos del usuario que ha iniciado sesion (o null)
  get usuario(): Usuario | null {
    const texto = localStorage.getItem('usuario');
    return texto ? JSON.parse(texto) : null;
  }

  get roles(): string[] {
    const texto = localStorage.getItem('roles');
    return texto ? JSON.parse(texto) : [];
  }

  get esAdmin(): boolean {
    return this.roles.includes('ROLE_ADMIN');
  }
}
