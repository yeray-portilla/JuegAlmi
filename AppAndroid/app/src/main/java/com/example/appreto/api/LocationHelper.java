package com.example.appreto.api;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.appreto.R;

public class LocationHelper {
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static final long LIMITE_ESPERA_MS = 5000;

    private static final long EDAD_MAXIMA_POSICION_MS = 60000;

    public interface LocationCallback {
        void onLocationResult(double lat, double lon);
    }

    private static Activity actividadPendiente;
    private static LocationCallback callbackPendiente;

    public static void getLocation(Activity activity, LocationCallback callback) {
        if (tienePermiso(activity)) {
            obtenerPosicion(activity, callback);
            return;
        }
        actividadPendiente = activity;
        callbackPendiente = callback;
        pedirPermisoUbicacion(activity);
    }

    public static void alRecibirRespuestaPermiso(int requestCode) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE || callbackPendiente == null) {
            return;
        }
        Activity activity = actividadPendiente;
        LocationCallback callback = callbackPendiente;
        actividadPendiente = null;
        callbackPendiente = null;

        if (tienePermiso(activity)) {
            obtenerPosicion(activity, callback);
        } else {
            callback.onLocationResult(0.0, 0.0);
        }
    }

    private static boolean tienePermiso(Context context) {
        boolean precisa = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean aproximada = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        return precisa || aproximada;
    }

    private static void pedirPermisoUbicacion(Activity activity) {
        String[] permisos = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(activity, permisos, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private static void obtenerPosicion(Activity activity, LocationCallback callback) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            callback.onLocationResult(0.0, 0.0);
            return;
        }

        Location ultimaPosicion = buscarUltimaPosicion(locationManager);
        if (ultimaPosicion != null && esReciente(ultimaPosicion)) {
            callback.onLocationResult(ultimaPosicion.getLatitude(), ultimaPosicion.getLongitude());
            return;
        }

        esperarPosicionNueva(activity, locationManager, ultimaPosicion, callback);
    }

    @SuppressLint("MissingPermission")
    private static Location buscarUltimaPosicion(LocationManager locationManager) {
        Location posicion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (posicion == null) {
            posicion = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return posicion;
    }

    private static boolean esReciente(Location posicion) {
        long edad = System.currentTimeMillis() - posicion.getTime();
        return edad < EDAD_MAXIMA_POSICION_MS;
    }

    @SuppressLint("MissingPermission")
    private static void esperarPosicionNueva(Activity activity, LocationManager locationManager,
                                             Location posicionAntigua, LocationCallback callback) {
        AlertDialog aviso = mostrarAvisoObteniendo(activity);
        Handler temporizador = new Handler(Looper.getMainLooper());
        EscuchaPosicion escucha = new EscuchaPosicion(locationManager, aviso, temporizador, callback);

        boolean algunoEncendido = false;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, escucha, Looper.getMainLooper());
            algunoEncendido = true;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, escucha, Looper.getMainLooper());
            algunoEncendido = true;
        }

        if (!algunoEncendido) {
            escucha.terminarSinPosicionNueva(posicionAntigua);
            return;
        }

        temporizador.postDelayed(new Runnable() {
            @Override
            public void run() {
                escucha.terminarSinPosicionNueva(posicionAntigua);
            }
        }, LIMITE_ESPERA_MS);
    }

    private static AlertDialog mostrarAvisoObteniendo(Activity activity) {
        View vista = LayoutInflater.from(activity).inflate(R.layout.dialog_cargando, null);
        AlertDialog aviso = new AlertDialog.Builder(activity)
                .setView(vista)
                .setCancelable(false)
                .create();
        aviso.show();
        return aviso;
    }

    private static class EscuchaPosicion implements LocationListener {
        private final LocationManager locationManager;
        private final AlertDialog aviso;
        private final Handler temporizador;
        private final LocationCallback callback;
        private boolean terminado = false;

        EscuchaPosicion(LocationManager locationManager, AlertDialog aviso, Handler temporizador, LocationCallback callback) {
            this.locationManager = locationManager;
            this.aviso = aviso;
            this.temporizador = temporizador;
            this.callback = callback;
        }

        @Override
        public void onLocationChanged(Location posicion) {
            terminar(posicion.getLatitude(), posicion.getLongitude());
        }

        void terminarSinPosicionNueva(Location posicionAntigua) {
            if (posicionAntigua != null) {
                terminar(posicionAntigua.getLatitude(), posicionAntigua.getLongitude());
            } else {
                terminar(0.0, 0.0);
            }
        }

        private void terminar(double latitud, double longitud) {
            if (terminado) {
                return;
            }
            terminado = true;
            locationManager.removeUpdates(this);
            temporizador.removeCallbacksAndMessages(null);
            if (aviso.isShowing()) {
                aviso.dismiss();
            }
            callback.onLocationResult(latitud, longitud);
        }

        @Override
        public void onStatusChanged(String proveedor, int estado, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String proveedor) {
        }

        @Override
        public void onProviderDisabled(String proveedor) {
        }
    }
}
