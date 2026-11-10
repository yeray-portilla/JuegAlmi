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

/**
 * Obtiene la posicion GPS del usuario para comprar, alquilar, pedir reparaciones y ver la distancia a las tiendas.
 *
 * Como se usa:  LocationHelper.getLocation(getActivity(), callback)
 *  1. Si no hay permiso, se pide. Cuando el usuario contesta, MainActivity llama a alRecibirRespuestaPermiso()
 *     y seguimos con la misma accion (antes la primera vez no hacia nada).
 *  2. Se muestra "Obteniendo ubicacion..." y se espera como mucho 5 segundos a que llegue una posicion.
 *  3. Siempre se llama al callback una vez: con la posicion, o con 0 y 0 si no hay permiso o no hay posicion.
 */
public class LocationHelper {
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Tiempo maximo esperando al GPS
    private static final long LIMITE_ESPERA_MS = 5000;

    // Una posicion guardada de hace menos de 1 minuto nos vale (no hace falta esperar al GPS)
    private static final long EDAD_MAXIMA_POSICION_MS = 60000;

    public interface LocationCallback {
        void onLocationResult(double lat, double lon);
    }

    // Accion que estaba esperando a que el usuario contestara al permiso
    private static Activity actividadPendiente;
    private static LocationCallback callbackPendiente;

    // Punto de entrada: pide el permiso si hace falta y despues obtiene la posicion
    public static void getLocation(Activity activity, LocationCallback callback) {
        if (tienePermiso(activity)) {
            obtenerPosicion(activity, callback);
            return;
        }
        // Guardamos lo que queriamos hacer para continuar cuando llegue la respuesta del permiso
        actividadPendiente = activity;
        callbackPendiente = callback;
        pedirPermisoUbicacion(activity);
    }

    // MainActivity llama a este metodo desde onRequestPermissionsResult
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
            callback.onLocationResult(0.0, 0.0); // permiso denegado: mandamos 0 y 0
        }
    }

    // true si el usuario ya nos dio permiso de ubicacion (precisa o aproximada)
    private static boolean tienePermiso(Context context) {
        boolean precisa = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean aproximada = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        return precisa || aproximada;
    }

    // Muestra el aviso del sistema pidiendo el permiso de ubicacion
    private static void pedirPermisoUbicacion(Activity activity) {
        String[] permisos = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(activity, permisos, LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Obtiene la posicion (ya con permiso). Usa una reciente si la hay; si no, espera al GPS hasta 5 s.
    @SuppressLint("MissingPermission") // el permiso ya se ha comprobado antes de llegar aqui
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

    // Ultima posicion que conoce el movil (primero GPS, luego red). Puede ser null.
    @SuppressLint("MissingPermission")
    private static Location buscarUltimaPosicion(LocationManager locationManager) {
        Location posicion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (posicion == null) {
            posicion = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return posicion;
    }

    // true si la posicion tiene menos de 1 minuto
    private static boolean esReciente(Location posicion) {
        long edad = System.currentTimeMillis() - posicion.getTime();
        return edad < EDAD_MAXIMA_POSICION_MS;
    }

    // Muestra "Obteniendo ubicacion..." y espera la primera posicion que llegue, como mucho 5 segundos
    @SuppressLint("MissingPermission")
    private static void esperarPosicionNueva(Activity activity, LocationManager locationManager,
                                             Location posicionAntigua, LocationCallback callback) {
        AlertDialog aviso = mostrarAvisoObteniendo(activity);
        Handler temporizador = new Handler(Looper.getMainLooper());
        EscuchaPosicion escucha = new EscuchaPosicion(locationManager, aviso, temporizador, callback);

        // Pedimos posiciones a los proveedores que esten encendidos (GPS y/o red)
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
            escucha.terminarSinPosicionNueva(posicionAntigua); // ubicacion apagada en el movil
            return;
        }

        // Si a los 5 segundos no ha llegado nada, usamos la posicion antigua (o 0 y 0)
        temporizador.postDelayed(new Runnable() {
            @Override
            public void run() {
                escucha.terminarSinPosicionNueva(posicionAntigua);
            }
        }, LIMITE_ESPERA_MS);
    }

    // Crea y muestra el aviso "Obteniendo ubicacion..." (no se puede cerrar tocando fuera)
    private static AlertDialog mostrarAvisoObteniendo(Activity activity) {
        View vista = LayoutInflater.from(activity).inflate(R.layout.dialog_cargando, null);
        AlertDialog aviso = new AlertDialog.Builder(activity)
                .setView(vista)
                .setCancelable(false)
                .create();
        aviso.show();
        return aviso;
    }

    /**
     * Recibe las posiciones del LocationManager. Termina una sola vez: con la primera posicion que llega
     * o cuando salta el limite de 5 s. Al terminar deja de escuchar, cierra el aviso y llama al callback.
     */
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

        // Ha llegado una posicion nueva
        @Override
        public void onLocationChanged(Location posicion) {
            terminar(posicion.getLatitude(), posicion.getLongitude());
        }

        // No ha llegado ninguna a tiempo: usamos la antigua si la hay, si no 0 y 0
        void terminarSinPosicionNueva(Location posicionAntigua) {
            if (posicionAntigua != null) {
                terminar(posicionAntigua.getLatitude(), posicionAntigua.getLongitude());
            } else {
                terminar(0.0, 0.0);
            }
        }

        // Deja de escuchar, cierra el aviso y devuelve la posicion (solo la primera vez)
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

        // Estos tres metodos son obligatorios en moviles con Android antiguo (antes de Android 11). No hacen nada.
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
