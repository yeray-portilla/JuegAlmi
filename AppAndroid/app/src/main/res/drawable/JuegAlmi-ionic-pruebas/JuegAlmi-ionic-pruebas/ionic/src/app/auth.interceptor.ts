import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

// Añade el token a todas las peticiones a la API y trata el 401 (sin token o caducado)
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = localStorage.getItem('token');
  const peticion = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(peticion).pipe(
    catchError((e: HttpErrorResponse) => {
      // En /auth/* un 401 es "credenciales incorrectas": lo trata la pagina de login.
      if (e.status === 401 && !req.url.includes('/api/auth/')) {
        auth.logout();
        router.navigateByUrl('/login');
      }
      return throwError(() => e);
    }),
  );
};
