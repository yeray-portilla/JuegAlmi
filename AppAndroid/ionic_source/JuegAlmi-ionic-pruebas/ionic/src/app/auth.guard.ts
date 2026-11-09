import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

// Sin token -> al login
export const authGuard: CanActivateFn = () =>
  inject(AuthService).logueado || inject(Router).createUrlTree(['/login']);

// Solo administradores (ROLE_ADMIN). Si no lo es -> al catalogo.
// Va despues de authGuard, asi que aqui ya hay sesion.
export const adminGuard: CanActivateFn = () =>
  inject(AuthService).esAdmin() || inject(Router).createUrlTree(['/tabs/moviles']);
