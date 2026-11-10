package com.example.appreto.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appreto.R;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.Tienda;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Parte comun de las pantallas Historia, Ubicacion y Galeria.
 * Esta clase pide las tiendas a la API y se encarga de los estados "cargando", "error + reintentar" y "recargar".
 * Por cada tienda crea una tarjeta (item_tienda.xml) con nombre, direccion y departamentos.
 * Cada pantalla hija solo tiene que decir que se pone dentro de la tarjeta (metodo anadirContenido).
 */
public abstract class TiendasBaseFragment extends Fragment {

    private ProgressBar barraCargando;
    private LinearLayout bloqueError;
    private TextView textoError;
    private ScrollView scrollTiendas;
    protected LinearLayout listaTiendas;

    // Cada pantalla hija rellena la parte de abajo de la tarjeta de una tienda
    protected abstract void anadirContenido(Tienda tienda, View tarjeta, LinearLayout contenido);

    // Se llama despues de pintar todas las tarjetas. Por defecto no hace nada (Ubicacion lo usa para la distancia).
    protected void alTerminarDePintar(List<Tienda> tiendas) {
    }

    // Crea la vista y empieza a cargar las tiendas
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_tiendas, container, false);

        barraCargando = vista.findViewById(R.id.pb_tiendas);
        bloqueError = vista.findViewById(R.id.ll_error_tiendas);
        textoError = vista.findViewById(R.id.tv_error_tiendas);
        scrollTiendas = vista.findViewById(R.id.sv_tiendas);
        listaTiendas = vista.findViewById(R.id.ll_lista_tiendas);

        // Reintentar (tras un error) y Recargar (arriba a la derecha) hacen lo mismo: volver a pedir las tiendas
        vista.findViewById(R.id.btn_reintentar_tiendas).setOnClickListener(v -> cargarTiendas());
        vista.findViewById(R.id.btn_recargar_tiendas).setOnClickListener(v -> cargarTiendas());

        cargarTiendas();
        return vista;
    }

    // Pide GET info/tiendas a la API
    protected void cargarTiendas() {
        mostrarCargando();

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getTiendas().enqueue(new Callback<List<Tienda>>() {
            @Override
            public void onResponse(Call<List<Tienda>> call, Response<List<Tienda>> response) {
                if (!isAdded()) {
                    return; // el usuario ya se fue de esta pantalla
                }
                if (response.isSuccessful() && response.body() != null) {
                    pintarTiendas(response.body());
                } else {
                    String mensaje = RetrofitClient.leerMensajeError(response, "No se pudieron cargar las tiendas.");
                    mostrarError(mensaje);
                }
            }

            @Override
            public void onFailure(Call<List<Tienda>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                mostrarError("No se pudo conectar con el servidor.");
            }
        });
    }

    // Borra las tarjetas que hubiera y crea una nueva por cada tienda
    private void pintarTiendas(List<Tienda> tiendas) {
        listaTiendas.removeAllViews();

        if (tiendas.isEmpty()) {
            TextView textoVacio = new TextView(requireContext());
            textoVacio.setText("No hay tiendas.");
            textoVacio.setTextColor(getResources().getColor(R.color.text_secondary, null));
            textoVacio.setPadding(16, 48, 16, 16);
            listaTiendas.addView(textoVacio);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Tienda tienda : tiendas) {
            View tarjeta = crearTarjeta(inflater, tienda);
            listaTiendas.addView(tarjeta);
        }

        mostrarLista();
        listaTiendas.scheduleLayoutAnimation(); // las tarjetas aparecen subiendo (anim/lista_aparecer.xml)
        alTerminarDePintar(tiendas);
    }

    // Crea la tarjeta de una tienda con los datos comunes y le pide a la pantalla hija su contenido
    private View crearTarjeta(LayoutInflater inflater, Tienda tienda) {
        View tarjeta = inflater.inflate(R.layout.item_tienda, listaTiendas, false);

        TextView textoNombre = tarjeta.findViewById(R.id.tv_nombre_tienda);
        TextView textoDireccion = tarjeta.findViewById(R.id.tv_direccion_tienda);
        TextView textoDepartamentos = tarjeta.findViewById(R.id.tv_departamentos_tienda);
        LinearLayout contenido = tarjeta.findViewById(R.id.ll_contenido_tienda);

        textoNombre.setText(tienda.getNombre());
        textoDireccion.setText(tienda.getDireccion());

        String departamentos = tienda.getNombresDepartamentos();
        if (departamentos.isEmpty()) {
            textoDepartamentos.setVisibility(View.GONE);
        } else {
            textoDepartamentos.setText("Departamentos: " + departamentos);
        }

        anadirContenido(tienda, tarjeta, contenido);
        return tarjeta;
    }

    // Muestra solo la rueda de carga
    private void mostrarCargando() {
        barraCargando.setVisibility(View.VISIBLE);
        bloqueError.setVisibility(View.GONE);
        scrollTiendas.setVisibility(View.GONE);
    }

    // Muestra solo el mensaje de error con el boton Reintentar
    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.VISIBLE);
        scrollTiendas.setVisibility(View.GONE);
    }

    // Muestra solo la lista de tiendas
    private void mostrarLista() {
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.GONE);
        scrollTiendas.setVisibility(View.VISIBLE);
    }
}
