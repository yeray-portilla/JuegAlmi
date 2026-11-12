import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () =>
  inject(AuthService).logueado || inject(Router).createUrlTree(['/login']);

export const adminGuard: CanActivateFn = () =>
  inject(AuthService).esAdmin() || inject(Router).createUrlTree(['/tabs/moviles']);
