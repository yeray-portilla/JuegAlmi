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

public class UbicacionFragment extends TiendasBaseFragment {

    private static final double ZOOM_MAPA = 16.0;

    private static final String USER_AGENT_MAPA = "JuegAlmi-Android/1.0";

    private final List<MapView> mapas = new ArrayList<>();

    private boolean posicionPedida = false;
    private boolean hayPosicion = false;
    private double miLatitud = 0;
    private double miLongitud = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarOsmdroid();
    }

    private void configurarOsmdroid() {
        Context contexto = requireContext();
        Configuration.getInstance().load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(USER_AGENT_MAPA);
    }

    @Override
    protected void cargarTiendas() {
        quitarMapas();
        super.cargarTiendas();
    }

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

    private void prepararMapa(MapView mapa, Tienda tienda) {
        GeoPoint posicionTienda = new GeoPoint(tienda.getLatitud(), tienda.getLongitud());

        mapa.setTileSource(new FuenteTeselasEsri());
        mapa.getController().setZoom(ZOOM_MAPA);
        mapa.getController().setCenter(posicionTienda);
        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapa.setMultiTouchControls(false);
        bloquearToquesEnMapa(mapa);

        Marker marca = new Marker(mapa);
        marca.setPosition(posicionTienda);
        marca.setTitle(tienda.getNombre());
        marca.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.marcador_tienda));
        marca.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        mapa.getOverlays().add(marca);
    }

    private void bloquearToquesEnMapa(MapView mapa) {
        mapa.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View vista, MotionEvent evento) {
                return true;
            }
        });
    }

    @Override
    protected void alTerminarDePintar(List<Tienda> tiendas) {
        if (posicionPedida) {
            mostrarDistancias(tiendas);
            return;
        }
        posicionPedida = true;
        pedirPosicion(tiendas);
    }

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

    private void mostrarDistancias(List<Tienda> tiendas) {
        if (!hayPosicion) {
            return;
        }
        for (int i = 0; i < tiendas.size(); i++) {
            View tarjeta = listaTiendas.getChildAt(i);
            TextView textoDistancia = tarjeta.findViewById(R.id.tv_distancia_tienda);
            textoDistancia.setText(calcularTextoDistancia(tiendas.get(i)));
            textoDistancia.setVisibility(View.VISIBLE);
        }
    }

    private String calcularTextoDistancia(Tienda tienda) {
        float[] resultado = new float[1];
        Location.distanceBetween(miLatitud, miLongitud, tienda.getLatitud(), tienda.getLongitud(), resultado);
        double kilometros = resultado[0] / 1000.0;
        return String.format(new Locale("es", "ES"), "a %.1f km", kilometros);
    }

    private void abrirComoLlegar(Tienda tienda) {
        String url = String.format(Locale.US, "https://www.google.com/maps/dir/?api=1&destination=%f,%f",
                tienda.getLatitud(), tienda.getLongitud());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No hay ninguna app para abrir el mapa.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        for (MapView mapa : mapas) {
            mapa.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (MapView mapa : mapas) {
            mapa.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        quitarMapas();
        super.onDestroyView();
    }

    private void quitarMapas() {
        for (MapView mapa : mapas) {
            mapa.onDetach();
        }
        mapas.clear();
    }
}
