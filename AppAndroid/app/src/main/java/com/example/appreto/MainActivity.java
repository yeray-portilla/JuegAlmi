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

import com.example.appreto.api.SharedPrefManager;
import com.example.appreto.fragments.LoginFragment;
import com.example.appreto.fragments.MisOperacionesFragment;
import com.example.appreto.fragments.MobilesFragment;
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
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                SharedPrefManager.getInstance(this).clear();
                showLogin();
            } else if (id == R.id.nav_operations) {
                toolbar.setTitle(R.string.nav_operations);
                loadFragment(new MisOperacionesFragment());
            }
            return true;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_devices) {
                toolbar.setTitle(R.string.bottom_nav_devices);
                loadFragment(new MobilesFragment("Dispositivos"));
                return true;
            } else if (id == R.id.nav_videogames) {
                toolbar.setTitle(R.string.bottom_nav_videogames);
                loadFragment(new MobilesFragment("Videojuegos"));
                return true;
            } else if (id == R.id.nav_consoles) {
                toolbar.setTitle(R.string.tab_consoles);
                loadFragment(new MobilesFragment("Consolas"));
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

    private void showLogin() {
        appBarLayout.setVisibility(View.GONE);
        bottomNav.setVisibility(View.GONE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        loadFragment(new LoginFragment());
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

        bottomNav.setSelectedItemId(R.id.nav_devices);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
