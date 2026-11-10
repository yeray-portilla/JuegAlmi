package com.example.appreto;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.appreto.api.LocationHelper;
import com.example.appreto.api.SharedPrefManager;
import com.example.appreto.fragments.AdminFragment;
import com.example.appreto.fragments.GaleriaFragment;
import com.example.appreto.fragments.HistoriaFragment;
import com.example.appreto.fragments.LoginFragment;
import com.example.appreto.fragments.UbicacionFragment;
import com.example.appreto.fragments.MisOperacionesFragment;
import com.example.appreto.fragments.MobilesFragment;
import com.example.appreto.fragments.RegistroFragment;
import com.example.appreto.fragments.SupportFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNav = findViewById(R.id.bottom_navigation);
        appBarLayout = findViewById(R.id.app_bar_layout);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            abrirOpcionMenuLateral(item.getItemId());
            return true;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_devices) {
                toolbar.setTitle(R.string.bottom_nav_devices);
                loadFragment(MobilesFragment.nuevaInstancia(MobilesFragment.DISPOSITIVOS));
                return true;
            } else if (id == R.id.nav_videogames) {
                toolbar.setTitle(R.string.bottom_nav_videogames);
                loadFragment(MobilesFragment.nuevaInstancia(MobilesFragment.VIDEOJUEGOS));
                return true;
            } else if (id == R.id.nav_consoles) {
                toolbar.setTitle(R.string.bottom_nav_consoles);
                loadFragment(MobilesFragment.nuevaInstancia(MobilesFragment.CONSOLAS));
                return true;
            } else if (id == R.id.nav_support) {
                toolbar.setTitle(R.string.bottom_nav_support);
                loadFragment(new SupportFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            String token = SharedPrefManager.getInstance(this).getToken();
            if (token != null && !token.isEmpty()) {
                onLoginSuccess();
            } else {
                showLogin();
            }
        }
    }

    // Abre la pantalla de la opcion elegida en el menu lateral
    private void abrirOpcionMenuLateral(int idOpcion) {
        if (idOpcion == R.id.nav_services) {
            // Servicios = el catalogo (primera pestana de la barra de abajo)
            bottomNav.setSelectedItemId(R.id.nav_devices);
        } else if (idOpcion == R.id.nav_location) {
            toolbar.setTitle(R.string.nav_location);
            loadFragment(new UbicacionFragment());
        } else if (idOpcion == R.id.nav_gallery) {
            toolbar.setTitle(R.string.nav_gallery);
            loadFragment(new GaleriaFragment());
        } else if (idOpcion == R.id.nav_history) {
            toolbar.setTitle(R.string.nav_history);
            loadFragment(new HistoriaFragment());
        } else if (idOpcion == R.id.nav_operations) {
            toolbar.setTitle(R.string.nav_operations);
            loadFragment(new MisOperacionesFragment());
        } else if (idOpcion == R.id.nav_admin) {
            // Doble seguridad: la opcion solo se ve si es admin, y la API devuelve 403 si no lo es
            if (SharedPrefManager.getInstance(this).esAdmin()) {
                toolbar.setTitle(R.string.nav_admin);
                loadFragment(new AdminFragment());
            }
        } else if (idOpcion == R.id.nav_logout) {
            SharedPrefManager.getInstance(this).clear();
            showLogin();
        }
    }

    // Abre Mis operaciones directamente en la pestana "Mis solicitudes" (lo usa Soporte al enviar una solicitud)
    public void abrirMisSolicitudes() {
        toolbar.setTitle(R.string.nav_operations);
        loadFragment(MisOperacionesFragment.nuevaInstancia(MisOperacionesFragment.PESTANA_SOLICITUDES));
    }

    // Respuesta del usuario al permiso de ubicacion: LocationHelper continua con la accion que estaba esperando
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LocationHelper.alRecibirRespuestaPermiso(requestCode);
    }

    private void showLogin() {
        appBarLayout.setVisibility(View.GONE);
        bottomNav.setVisibility(View.GONE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        loadFragment(new LoginFragment());
    }

    // Lo usa RegistroFragment para volver al login
    public void mostrarLogin() {
        showLogin();
    }

    // Pantalla de registro: igual que el login, sin barras ni menu lateral
    public void mostrarRegistro() {
        appBarLayout.setVisibility(View.GONE);
        bottomNav.setVisibility(View.GONE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        loadFragment(new RegistroFragment());
    }

    public void onLoginSuccess() {
        appBarLayout.setVisibility(View.VISIBLE);
        bottomNav.setVisibility(View.VISIBLE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        // Actualizar header del navigation view con los datos del usuario logueado
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_name);
        TextView navEmail = headerView.findViewById(R.id.nav_header_email);

        String username = SharedPrefManager.getInstance(this).getUsername();
        String email = SharedPrefManager.getInstance(this).getEmail();

        navUsername.setText(username);
        navEmail.setText(email);

        mostrarOpcionAdminSiEsAdmin(navigationView);

        bottomNav.setSelectedItemId(R.id.nav_devices);
    }

    // La opcion "Administracion" del menu lateral solo se ve si el usuario tiene ROLE_ADMIN
    private void mostrarOpcionAdminSiEsAdmin(NavigationView navigationView) {
        boolean esAdmin = SharedPrefManager.getInstance(this).esAdmin();
        navigationView.getMenu().findItem(R.id.nav_admin).setVisible(esAdmin);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
