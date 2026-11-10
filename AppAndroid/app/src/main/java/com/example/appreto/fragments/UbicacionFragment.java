package com.example.appreto.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.appreto.R;
import com.example.appreto.api.FuenteTeselasEsri;
import com.example.appreto.api.LocationHelper;
import com.example.appreto.models.Tienda;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Pantalla Ubicacion (menu lateral): por cada tienda, un mapa con la tienda marcada,
// la distancia desde el usuario y el boton "Como llegar"
public class UbicacionFragment extends TiendasBaseFragment {

    private static final double ZOOM_MAPA = 16.0;

    // Nombre con el que la app se identifica ante los servidores de mapas
    private static final String USER_AGENT_MAPA = "JuegAlmi-Android/1.0";

    // Mapas que hay ahora en pantalla (osmdroid necesita que les avisemos de onResume/onPause)
    private final List<MapView> mapas = new ArrayList<>();

    // Posicion del usuario. Solo se pide una vez por visita a la pantalla.
    private boolean posicionPedida = false;
    private boolean hayPosicion = false;
    private double miLatitud = 0;
    private double miLongitud = 0;

    // Configura osmdroid antes de crear ningun mapa
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarOsmdroid();
    }

    // Los servidores de mapas piden que cada app se identifique con su propio "user agent".
    // Usamos un nombre propio (no el del paquete), sin emails ni datos personales.
    private void configurarOsmdroid() {
        Context contexto = requireContext();
        // load() lee la configuracion guardada y puede cambiar el user agent: por eso lo ponemos DESPUES
        Configuration.getInstance().load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(USER_AGENT_MAPA);
    }

    // Al recargar, olvidamos los mapas anteriores (se van a crear de nuevo)
    @Override
    protected void cargarTiendas() {
        quitarMapas();
        super.cargarTiendas();
    }

    // Mete el mapa y el boton "Como llegar" dentro de la tarjeta de la tienda
    @Override
    protected void anadirContenido(Tienda tienda, View tarjeta, LinearLayout contenido) {
        View vistaUbicacion = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_ubicacion_tienda, contenido, false);

        MapView mapa = vistaUbicacion.findViewById(R.id.mapa_tienda);
        prepararMapa(mapa, tienda);
        mapas.add(mapa);

        vistaUbicacion.findViewById(R.id.btn_como_llegar).setOnClickListener(v -> abrirComoLlegar(tienda));

        contenido.addView(vistaUbicacion);
    }

    // Centra el mapa en la tienda, pone la marca y lo deja quieto (para que la lista se pueda desplazar bien)
    private void prepararMapa(MapView mapa, Tienda tienda) {
        GeoPoint posicionTienda = new GeoPoint(tienda.getLatitud(), tienda.getLongitud());

        // Teselas de Esri World Street Map (OpenStreetMap daba 403 y Carto pide clave). Ver FuenteTeselasEsri.
        mapa.setTileSource(new FuenteTeselasEsri());
        mapa.getController().setZoom(ZOOM_MAPA);
        mapa.getController().setCenter(posicionTienda);
        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapa.setMultiTouchControls(false);
        bloquearToquesEnMapa(mapa);

        // Marca de la tienda
        Marker marca = new Marker(mapa);
        marca.setPosition(posicionTienda);
        marca.setTitle(tienda.getNombre());
        marca.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.marcador_tienda));
        marca.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        mapa.getOverlays().add(marca);
        // La atribucion "Tiles © Esri" esta debajo del mapa (view_ubicacion_tienda.xml)
    }

    // El mapa ignora los toques: asi al deslizar el dedo se mueve la lista y no el mapa
    private void bloquearToquesEnMapa(MapView mapa) {
        mapa.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View vista, MotionEvent evento) {
                return true; // true = "ya lo he tratado yo", el mapa no se entera
            }
        });
    }

    // Cuando ya estan todas las tarjetas, pedimos la posicion del usuario para poner la distancia
    @Override
    protected void alTerminarDePintar(List<Tienda> tiendas) {
        if (posicionPedida) {
            mostrarDistancias(tiendas); // ya la teniamos de antes: no volvemos a preguntar
            return;
        }
        posicionPedida = true;
        pedirPosicion(tiendas);
    }

    // Pide la posicion GPS. Si no hay permiso o no hay posicion, LocationHelper devuelve 0, 0.
    private void pedirPosicion(List<Tienda> tiendas) {
        LocationHelper.getLocation(requireActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitud, double longitud) {
                if (!isAdded()) {
                    return;
                }
                miLatitud = latitud;
                miLongitud = longitud;
                hayPosicion = !(latitud == 0 && longitud == 0);
                mostrarDistancias(tiendas);
            }
        });
    }

    // Escribe "a X km" en cada tarjeta. La tarjeta numero i de la lista es la de la tienda numero i.
    private void mostrarDistancias(List<Tienda> tiendas) {
        if (!hayPosicion) {
            return; // sin posicion no se ve la distancia (igual que en Ionic)
        }
        for (int i = 0; i < tiendas.size(); i++) {
            View tarjeta = listaTiendas.getChildAt(i);
            TextView textoDistancia = tarjeta.findViewById(R.id.tv_distancia_tienda);
            textoDistancia.setText(calcularTextoDistancia(tiendas.get(i)));
            textoDistancia.setVisibility(View.VISIBLE);
        }
    }

    // Distancia en linea recta desde el usuario hasta la tienda, ej. "a 3,2 km"
    private String calcularTextoDistancia(Tienda tienda) {
        float[] resultado = new float[1];
        Location.distanceBetween(miLatitud, miLongitud, tienda.getLatitud(), tienda.getLongitud(), resultado);
        double kilometros = resultado[0] / 1000.0;
        return String.format(new Locale("es", "ES"), "a %.1f km", kilometros);
    }

    // Abre Google Maps (o el navegador) con la ruta hasta la tienda
    private void abrirComoLlegar(Tienda tienda) {
        // Locale.US para que los decimales salgan con punto (37.5) y no con coma
        String url = String.format(Locale.US, "https://www.google.com/maps/dir/?api=1&destination=%f,%f",
                tienda.getLatitud(), tienda.getLongitud());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No hay ninguna app para abrir el mapa.", Toast.LENGTH_SHORT).show();
        }
    }

    // osmdroid: los mapas vuelven a funcionar al volver a la pantalla
    @Override
    public void onResume() {
        super.onResume();
        for (MapView mapa : mapas) {
            mapa.onResume();
        }
    }

    // osmdroid: los mapas paran de descargar al salir de la pantalla
    @Override
    public void onPause() {
        super.onPause();
        for (MapView mapa : mapas) {
            mapa.onPause();
        }
    }

    // Al cerrar la pantalla liberamos los mapas
    @Override
    public void onDestroyView() {
        quitarMapas();
        super.onDestroyView();
    }

    // Libera los mapas y vacia la lista
    private void quitarMapas() {
        for (MapView mapa : mapas) {
            mapa.onDetach();
        }
        mapas.clear();
    }
}
