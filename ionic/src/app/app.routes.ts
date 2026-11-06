import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./login/login.page').then(m => m.LoginPage) },
  {
    path: 'tabs',
    canActivate: [authGuard],

    loadComponent: () => import('./tabs/tabs.page').then(m => m.TabsPage),
    children: [
      { path: 'moviles', loadComponent: () => import('./moviles/moviles.page').then(m => m.MovilesPage) },
      { path: 'consolas', loadComponent: () => import('./consolas/consolas.page').then(m => m.ConsolasPage) },
      { path: 'videojuegos', loadComponent: () => import('./videojuegos/videojuegos.page').then(m => m.VideojuegosPage) },
      { path: 'soporte', loadComponent: () => import('./soporte/soporte.page').then(m => m.SoportePage) },
      { path: '', redirectTo: 'moviles', pathMatch: 'full' },
    ],
  },
];
