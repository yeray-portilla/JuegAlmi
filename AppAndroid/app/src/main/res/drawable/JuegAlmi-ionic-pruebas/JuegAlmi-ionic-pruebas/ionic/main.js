import { injectQuery as __vite__injectQuery } from "/@vite/client";import { createHotContext as __vite__createHotContext } from "/@vite/client";import.meta.hot = __vite__createHotContext("/main.js");import {
  AuthService
} from "/chunk-W4B2LU7Y.js";
import "/chunk-4URRKSK4.js";
import "/chunk-PZ5AY32C.js";

// src/main.ts
import { bootstrapApplication } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_platform-browser.js?v=06de6ec1";
import { RouteReuseStrategy, provideRouter, withPreloading, PreloadAllModules } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_router.js?v=06de6ec1";
import { IonicRouteStrategy, provideIonicAngular } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@ionic_angular.js?v=06de6ec1";
import { provideHttpClient, withInterceptors } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_common_http.js?v=06de6ec1";

// src/app/auth.guard.ts
import { inject } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_core.js?v=06de6ec1";
import { Router } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_router.js?v=06de6ec1";
var authGuard = () => inject(AuthService).logueado || inject(Router).createUrlTree(["/login"]);

// src/app/app.routes.ts
var routes = [
  { path: "", redirectTo: "login", pathMatch: "full" },
  { path: "login", loadComponent: () => import("/login.page-ULXS5XRF.js").then((m) => m.LoginPage) },
  {
    path: "tabs",
    canActivate: [authGuard],
    loadComponent: () => import("/tabs.page-6SMDV42E.js").then((m) => m.TabsPage),
    children: [
      { path: "moviles", loadComponent: () => import("/moviles.page-6T7F3R6S.js").then((m) => m.MovilesPage) },
      { path: "consolas", loadComponent: () => import("/consolas.page-OHFPRTEH.js").then((m) => m.ConsolasPage) },
      { path: "videojuegos", loadComponent: () => import("/videojuegos.page-SVXQW2ZK.js").then((m) => m.VideojuegosPage) },
      { path: "soporte", loadComponent: () => import("/soporte.page-G4XDAPU3.js").then((m) => m.SoportePage) },
      { path: "", redirectTo: "moviles", pathMatch: "full" }
    ]
  }
];

// src/app/app.component.ts
import { Component } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_core.js?v=06de6ec1";
import { IonApp, IonRouterOutlet } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@ionic_angular.js?v=06de6ec1";
import * as i0 from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_core.js?v=06de6ec1";
var _AppComponent = class _AppComponent {
  constructor() {
  }
};
_AppComponent.\u0275fac = function AppComponent_Factory(__ngFactoryType__) {
  return new (__ngFactoryType__ || _AppComponent)();
};
_AppComponent.\u0275cmp = /* @__PURE__ */ i0.\u0275\u0275defineComponent({ type: _AppComponent, selectors: [["app-root"]], decls: 2, vars: 0, template: function AppComponent_Template(rf, ctx) {
  if (rf & 1) {
    i0.\u0275\u0275elementStart(0, "ion-app");
    i0.\u0275\u0275element(1, "ion-router-outlet");
    i0.\u0275\u0275elementEnd();
  }
}, dependencies: [IonApp, IonRouterOutlet], encapsulation: 2 });
var AppComponent = _AppComponent;
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && i0.\u0275setClassMetadata(AppComponent, [{
    type: Component,
    args: [{ selector: "app-root", imports: [IonApp, IonRouterOutlet], template: "<ion-app>\n  <ion-router-outlet></ion-router-outlet>\n</ion-app>\n" }]
  }], () => [], null);
})();
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && i0.\u0275setClassDebugInfo(AppComponent, { className: "AppComponent", filePath: "src/app/app.component.ts", lineNumber: 9 });
})();
(() => {
  const id = "src%2Fapp%2Fapp.component.ts%40AppComponent";
  function AppComponent_HmrLoad(t) {
    import(
      /* @vite-ignore */
      __vite__injectQuery(i0.\u0275\u0275getReplaceMetadataURL(id, t, import.meta.url), 'import')
    ).then((m) => m.default && i0.\u0275\u0275replaceMetadata(AppComponent, m.default, [i0], [IonApp, IonRouterOutlet, Component], import.meta, id));
  }
  (typeof ngDevMode === "undefined" || ngDevMode) && AppComponent_HmrLoad(Date.now());
  (typeof ngDevMode === "undefined" || ngDevMode) && (import.meta.hot && import.meta.hot.on("angular:component-update", (d) => d.id === id && AppComponent_HmrLoad(d.timestamp)));
})();

// src/app/auth.interceptor.ts
import { inject as inject2 } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_core.js?v=06de6ec1";
import { Router as Router2 } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/@angular_router.js?v=06de6ec1";
import { catchError, throwError } from "/@fs/home/porti/Escritorio/proyectoReto/JuegAlmi/ionic/.angular/cache/22.2.4/app/vite/deps/rxjs.js?v=06de6ec1";
var authInterceptor = (req, next) => {
  const auth = inject2(AuthService);
  const router = inject2(Router2);
  const token = localStorage.getItem("token");
  const peticion = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(peticion).pipe(catchError((e) => {
    if (e.status === 401 && !req.url.includes("/api/auth/")) {
      auth.logout();
      router.navigateByUrl("/login");
    }
    return throwError(() => e);
  }));
};

// src/main.ts
bootstrapApplication(AppComponent, {
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    provideIonicAngular({ mode: "md" }),
    // mismo aspecto Material que Android
    provideRouter(routes, withPreloading(PreloadAllModules)),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
});
//# debugId=34f19c57-e389-558c-9782-cb21156cb8a9


//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbInNyYy9tYWluLnRzIiwic3JjL2FwcC9hdXRoLmd1YXJkLnRzIiwic3JjL2FwcC9hcHAucm91dGVzLnRzIiwic3JjL2FwcC9hcHAuY29tcG9uZW50LnRzIiwic3JjL2FwcC9hcHAuY29tcG9uZW50Lmh0bWwiLCJzcmMvYXBwL2F1dGguaW50ZXJjZXB0b3IudHMiXSwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IHsgYm9vdHN0cmFwQXBwbGljYXRpb24gfSBmcm9tICdAYW5ndWxhci9wbGF0Zm9ybS1icm93c2VyJztcbmltcG9ydCB7IFJvdXRlUmV1c2VTdHJhdGVneSwgcHJvdmlkZVJvdXRlciwgd2l0aFByZWxvYWRpbmcsIFByZWxvYWRBbGxNb2R1bGVzIH0gZnJvbSAnQGFuZ3VsYXIvcm91dGVyJztcbmltcG9ydCB7IElvbmljUm91dGVTdHJhdGVneSwgcHJvdmlkZUlvbmljQW5ndWxhciB9IGZyb20gJ0Bpb25pYy9hbmd1bGFyJztcbmltcG9ydCB7IHByb3ZpZGVIdHRwQ2xpZW50LCB3aXRoSW50ZXJjZXB0b3JzIH0gZnJvbSAnQGFuZ3VsYXIvY29tbW9uL2h0dHAnO1xuaW1wb3J0IHsgcm91dGVzIH0gZnJvbSAnLi9hcHAvYXBwLnJvdXRlcyc7XG5pbXBvcnQgeyBBcHBDb21wb25lbnQgfSBmcm9tICcuL2FwcC9hcHAuY29tcG9uZW50JztcbmltcG9ydCB7IGF1dGhJbnRlcmNlcHRvciB9IGZyb20gJy4vYXBwL2F1dGguaW50ZXJjZXB0b3InO1xuXG5ib290c3RyYXBBcHBsaWNhdGlvbihBcHBDb21wb25lbnQsIHtcbiAgcHJvdmlkZXJzOiBbXG4gICAgeyBwcm92aWRlOiBSb3V0ZVJldXNlU3RyYXRlZ3ksIHVzZUNsYXNzOiBJb25pY1JvdXRlU3RyYXRlZ3kgfSxcbiAgICBwcm92aWRlSW9uaWNBbmd1bGFyKHsgbW9kZTogJ21kJyB9KSwgLy8gbWlzbW8gYXNwZWN0byBNYXRlcmlhbCBxdWUgQW5kcm9pZFxuICAgIHByb3ZpZGVSb3V0ZXIocm91dGVzLCB3aXRoUHJlbG9hZGluZyhQcmVsb2FkQWxsTW9kdWxlcykpLFxuICAgIHByb3ZpZGVIdHRwQ2xpZW50KHdpdGhJbnRlcmNlcHRvcnMoW2F1dGhJbnRlcmNlcHRvcl0pKSxcbiAgXSxcbn0pO1xuIiwiaW1wb3J0IHsgaW5qZWN0IH0gZnJvbSAnQGFuZ3VsYXIvY29yZSc7XG5pbXBvcnQgeyBDYW5BY3RpdmF0ZUZuLCBSb3V0ZXIgfSBmcm9tICdAYW5ndWxhci9yb3V0ZXInO1xuaW1wb3J0IHsgQXV0aFNlcnZpY2UgfSBmcm9tICcuL2F1dGguc2VydmljZSc7XG5cbmV4cG9ydCBjb25zdCBhdXRoR3VhcmQ6IENhbkFjdGl2YXRlRm4gPSAoKSA9PlxuICBpbmplY3QoQXV0aFNlcnZpY2UpLmxvZ3VlYWRvIHx8IGluamVjdChSb3V0ZXIpLmNyZWF0ZVVybFRyZWUoWycvbG9naW4nXSk7IiwiaW1wb3J0IHsgUm91dGVzIH0gZnJvbSAnQGFuZ3VsYXIvcm91dGVyJztcbmltcG9ydCB7IGF1dGhHdWFyZCB9IGZyb20gJy4vYXV0aC5ndWFyZCc7XG5cbmV4cG9ydCBjb25zdCByb3V0ZXM6IFJvdXRlcyA9IFtcbiAgeyBwYXRoOiAnJywgcmVkaXJlY3RUbzogJ2xvZ2luJywgcGF0aE1hdGNoOiAnZnVsbCcgfSxcbiAgeyBwYXRoOiAnbG9naW4nLCBsb2FkQ29tcG9uZW50OiAoKSA9PiBpbXBvcnQoJy4vbG9naW4vbG9naW4ucGFnZScpLnRoZW4obSA9PiBtLkxvZ2luUGFnZSkgfSxcbiAge1xuICAgIHBhdGg6ICd0YWJzJyxcbiAgICBjYW5BY3RpdmF0ZTogW2F1dGhHdWFyZF0sXG5cbiAgICBsb2FkQ29tcG9uZW50OiAoKSA9PiBpbXBvcnQoJy4vdGFicy90YWJzLnBhZ2UnKS50aGVuKG0gPT4gbS5UYWJzUGFnZSksXG4gICAgY2hpbGRyZW46IFtcbiAgICAgIHsgcGF0aDogJ21vdmlsZXMnLCBsb2FkQ29tcG9uZW50OiAoKSA9PiBpbXBvcnQoJy4vbW92aWxlcy9tb3ZpbGVzLnBhZ2UnKS50aGVuKG0gPT4gbS5Nb3ZpbGVzUGFnZSkgfSxcbiAgICAgIHsgcGF0aDogJ2NvbnNvbGFzJywgbG9hZENvbXBvbmVudDogKCkgPT4gaW1wb3J0KCcuL2NvbnNvbGFzL2NvbnNvbGFzLnBhZ2UnKS50aGVuKG0gPT4gbS5Db25zb2xhc1BhZ2UpIH0sXG4gICAgICB7IHBhdGg6ICd2aWRlb2p1ZWdvcycsIGxvYWRDb21wb25lbnQ6ICgpID0+IGltcG9ydCgnLi92aWRlb2p1ZWdvcy92aWRlb2p1ZWdvcy5wYWdlJykudGhlbihtID0+IG0uVmlkZW9qdWVnb3NQYWdlKSB9LFxuICAgICAgeyBwYXRoOiAnc29wb3J0ZScsIGxvYWRDb21wb25lbnQ6ICgpID0+IGltcG9ydCgnLi9zb3BvcnRlL3NvcG9ydGUucGFnZScpLnRoZW4obSA9PiBtLlNvcG9ydGVQYWdlKSB9LFxuICAgICAgeyBwYXRoOiAnJywgcmVkaXJlY3RUbzogJ21vdmlsZXMnLCBwYXRoTWF0Y2g6ICdmdWxsJyB9LFxuICAgIF0sXG4gIH0sXG5dO1xuIiwiaW1wb3J0IHsgQ29tcG9uZW50IH0gZnJvbSAnQGFuZ3VsYXIvY29yZSc7XG5pbXBvcnQgeyBJb25BcHAsIElvblJvdXRlck91dGxldCB9IGZyb20gJ0Bpb25pYy9hbmd1bGFyJztcblxuQENvbXBvbmVudCh7XG4gIHNlbGVjdG9yOiAnYXBwLXJvb3QnLFxuICB0ZW1wbGF0ZVVybDogJ2FwcC5jb21wb25lbnQuaHRtbCcsXG4gIGltcG9ydHM6IFtJb25BcHAsIElvblJvdXRlck91dGxldF0sXG59KVxuZXhwb3J0IGNsYXNzIEFwcENvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKCkge31cbn1cbiIsIjxpb24tYXBwPlxuICA8aW9uLXJvdXRlci1vdXRsZXQ+PC9pb24tcm91dGVyLW91dGxldD5cbjwvaW9uLWFwcD5cbiIsImltcG9ydCB7IGluamVjdCB9IGZyb20gJ0Bhbmd1bGFyL2NvcmUnO1xuaW1wb3J0IHsgSHR0cEVycm9yUmVzcG9uc2UsIEh0dHBJbnRlcmNlcHRvckZuIH0gZnJvbSAnQGFuZ3VsYXIvY29tbW9uL2h0dHAnO1xuaW1wb3J0IHsgUm91dGVyIH0gZnJvbSAnQGFuZ3VsYXIvcm91dGVyJztcbmltcG9ydCB7IGNhdGNoRXJyb3IsIHRocm93RXJyb3IgfSBmcm9tICdyeGpzJztcbmltcG9ydCB7IEF1dGhTZXJ2aWNlIH0gZnJvbSAnLi9hdXRoLnNlcnZpY2UnO1xuXG4vLyBBw7FhZGUgZWwgdG9rZW4gYSB0b2RhcyBsYXMgcGV0aWNpb25lcyBhIGxhIEFQSSB5IHRyYXRhIGVsIDQwMSAoc2luIHRva2VuIG8gY2FkdWNhZG8pXG5leHBvcnQgY29uc3QgYXV0aEludGVyY2VwdG9yOiBIdHRwSW50ZXJjZXB0b3JGbiA9IChyZXEsIG5leHQpID0+IHtcbiAgY29uc3QgYXV0aCA9IGluamVjdChBdXRoU2VydmljZSk7XG4gIGNvbnN0IHJvdXRlciA9IGluamVjdChSb3V0ZXIpO1xuXG4gIGNvbnN0IHRva2VuID0gbG9jYWxTdG9yYWdlLmdldEl0ZW0oJ3Rva2VuJyk7XG4gIGNvbnN0IHBldGljaW9uID0gdG9rZW4gPyByZXEuY2xvbmUoeyBzZXRIZWFkZXJzOiB7IEF1dGhvcml6YXRpb246IGBCZWFyZXIgJHt0b2tlbn1gIH0gfSkgOiByZXE7XG5cbiAgcmV0dXJuIG5leHQocGV0aWNpb24pLnBpcGUoXG4gICAgY2F0Y2hFcnJvcigoZTogSHR0cEVycm9yUmVzcG9uc2UpID0+IHtcbiAgICAgIC8vIEVuIC9hdXRoLyogdW4gNDAxIGVzIFwiY3JlZGVuY2lhbGVzIGluY29ycmVjdGFzXCI6IGxvIHRyYXRhIGxhIHBhZ2luYSBkZSBsb2dpbi5cbiAgICAgIGlmIChlLnN0YXR1cyA9PT0gNDAxICYmICFyZXEudXJsLmluY2x1ZGVzKCcvYXBpL2F1dGgvJykpIHtcbiAgICAgICAgYXV0aC5sb2dvdXQoKTtcbiAgICAgICAgcm91dGVyLm5hdmlnYXRlQnlVcmwoJy9sb2dpbicpO1xuICAgICAgfVxuICAgICAgcmV0dXJuIHRocm93RXJyb3IoKCkgPT4gZSk7XG4gICAgfSksXG4gICk7XG59O1xuIl0sIm1h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QUUzRixTQUFPLEtBQUssUUFBUSxFQUFFLEtBQ3BCLFdBQVcsQ0FBQyxNQUF3QjtBQUVsQyxRQUFJLEVBQUUsV0FBVyxPQUFPLENBQUMsSUFBSSxJQUFJLFNBQVMsWUFBWSxHQUFHO0FBQ3ZELFdBQUssT0FBTTtBQUNYLGFBQU8sY0FBYyxRQUFRO0lBQy9CO0FBQ0EsV0FBTyxXQUFXLE1BQU0sQ0FBQztFQUMzQixDQUFDLENBQUM7QUFFTjs7O0FMaEJBLHFCQUFxQixjQUFjO0VBQ2pDLFdBQVc7SUFDVCxFQUFFLFNBQVMsb0JBQW9CLFVBQVUsbUJBQWtCO0lBQzNELG9CQUFvQixFQUFFLE1BQU0sS0FBSSxDQUFFOztJQUNsQyxjQUFjLFFBQVEsZUFBZSxpQkFBaUIsQ0FBQztJQUN2RCxrQkFBa0IsaUJBQWlCLENBQUMsZUFBZSxDQUFDLENBQUM7O0NBRXhEOyIsIm5hbWVzIjpbImluamVjdCIsIlJvdXRlciIsImluamVjdCIsIlJvdXRlciJdLCJkZWJ1Z0lkIjoiMzRmMTljNTctZTM4OS01NThjLTk3ODItY2IyMTE1NmNiOGE5In0=