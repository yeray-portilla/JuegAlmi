import { HttpInterceptorFn } from '@angular/common/http';

// Añade el token a todas las peticiones a la API
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  return next(token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req);
};
