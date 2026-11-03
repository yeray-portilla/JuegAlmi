import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
import { environment } from '../environments/environment';

export interface LoginRespuesta {
  token: string;
  expira?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<LoginRespuesta> {
    const peticion = environment.mock
      ? of({ token: 'demo' })
      : this.http.post<LoginRespuesta>(`${environment.apiUrl}/login`, { email, password });
    return peticion.pipe(tap(r => localStorage.setItem('token', r.token)));
  }

  logout() {
    localStorage.removeItem('token');
  }

  get logueado(): boolean {
    return !!localStorage.getItem('token');
  }
}