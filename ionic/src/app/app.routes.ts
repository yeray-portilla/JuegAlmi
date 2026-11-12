import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./login/login.page').then(m => m.LoginPage) },
  { path: 'registro', loadComponent: () => import('./registro/registro.page').then(m => m.RegistroPage) },
  {
    path: 'mis-operaciones',
    canActivate: [authGuard],
    loadComponent: () => import('./mis-operaciones/mis-operaciones.page').then(m => m.MisOperacionesPage),
  },
  {
    path: 'historia',
    canActivate: [authGuard],
    loadComponent: () => import('./historia/historia.page').then(m => m.HistoriaPage),
  },
  {
    path: 'ubicacion',
    canActivate: [authGuard],
    loadComponent: () => import('./ubicacion/ubicacion.page').then(m => m.UbicacionPage),
  },
  {
    path: 'galeria',
    canActivate: [authGuard],
    loadComponent: () => import('./galeria/galeria.page').then(m => m.GaleriaPage),
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./admin/admin.page').then(m => m.AdminPage),
  },
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
