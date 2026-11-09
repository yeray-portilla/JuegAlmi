import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService, LoginRespuesta, Usuario } from './api.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Usuario con sesion iniciada (o null). Es un signal: el menu se repinta solo al entrar/salir.
  usuario = signal<Usuario | null>(this.leerUsuarioGuardado());

  // true si el usuario tiene el rol de administrador
  esAdmin = computed(() => this.usuario()?.roles.includes('ROLE_ADMIN') ?? false);

  constructor(private api: ApiService) {}

  // Llama a la API y guarda token, roles y usuario en localStorage
  login(login: string, password: string): Observable<LoginRespuesta> {
    return this.api.iniciarSesion(login, password).pipe(
      tap(r => {
        localStorage.setItem('token', r.token);
        localStorage.setItem('roles', JSON.stringify(r.usuario.roles));
        localStorage.setItem('usuario', JSON.stringify(r.usuario));
        this.usuario.set(r.usuario);
      }),
    );
  }

  // Cerrar sesion: borramos todo lo guardado
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('usuario');
    this.usuario.set(null);
  }

  get logueado(): boolean {
    return !!localStorage.getItem('token');
  }

  // Al abrir la app, recuperamos el usuario de la ultima sesion
  private leerUsuarioGuardado(): Usuario | null {
    const texto = localStorage.getItem('usuario');
    return texto ? JSON.parse(texto) : null;
  }
}
