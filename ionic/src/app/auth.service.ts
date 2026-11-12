import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService, LoginRespuesta, Usuario } from './api.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  usuario = signal<Usuario | null>(this.leerUsuarioGuardado());

  esAdmin = computed(() => this.usuario()?.roles.includes('ROLE_ADMIN') ?? false);

  constructor(private api: ApiService) {}

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

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('usuario');
    this.usuario.set(null);
  }

  get logueado(): boolean {
    return !!localStorage.getItem('token');
  }

  private leerUsuarioGuardado(): Usuario | null {
    const texto = localStorage.getItem('usuario');
    return texto ? JSON.parse(texto) : null;
  }
}
