package com.example.appreto.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreto.R;
import com.example.appreto.adapters.OperacionAdapter;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.Operacion;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisOperacionesFragment extends Fragment implements OperacionAdapter.AccionesOperacion {

    public static final int PESTANA_COMPRAS = 0;
    public static final int PESTANA_ALQUILERES = 1;
    public static final int PESTANA_SOLICITUDES = 2;

    private static final String[] TIPOS = {"compra", "alquiler", "reparacion"};

    private static final String[] TEXTOS_VACIO = {
            "Todavía no has comprado nada.",
            "Todavía no has alquilado nada.",
            "No has pedido ninguna reparación."
    };

    private static final String ARG_PESTANA = "pestana_inicial";

    private ApiService api;
    private TabLayout pestanas;
    private ProgressBar barraCargando;
    private LinearLayout bloqueError;
    private TextView textoError;
    private TextView textoVacio;
    private RecyclerView listaOperaciones;

    private final List<Operacion> todasLasOperaciones = new ArrayList<>();
    private final List<Operacion> operacionesVisibles = new ArrayList<>();
    private OperacionAdapter adaptador;

    private boolean enviando = false;

    public static MisOperacionesFragment nuevaInstancia(int pestanaInicial) {
        MisOperacionesFragment fragment = new MisOperacionesFragment();
        Bundle argumentos = new Bundle();
        argumentos.putInt(ARG_PESTANA, pestanaInicial);
        fragment.setArguments(argumentos);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_mis_operaciones, container, false);
        api = RetrofitClient.getApiService(requireContext());

        pestanas = vista.findViewById(R.id.tab_operaciones);
        barraCargando = vista.findViewById(R.id.pb_operaciones);
        bloqueError = vista.findViewById(R.id.ll_error_operaciones);
        textoError = vista.findViewById(R.id.tv_error_operaciones);
        textoVacio = vista.findViewById(R.id.tv_vacio_operaciones);
        listaOperaciones = vista.findViewById(R.id.recycler_operaciones);

        adaptador = new OperacionAdapter(operacionesVisibles, this);
        listaOperaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        listaOperaciones.setAdapter(adaptador);

        vista.findViewById(R.id.btn_recargar_operaciones).setOnClickListener(v -> cargarOperaciones());
        vista.findViewById(R.id.btn_reintentar_operaciones).setOnClickListener(v -> cargarOperaciones());

        elegirPestanaInicial();
        escucharCambioDePestana();

        cargarOperaciones();
        return vista;
    }

    private void elegirPestanaInicial() {
        if (getArguments() == null) {
            return;
        }
        int pestanaInicial = getArguments().getInt(ARG_PESTANA, PESTANA_COMPRAS);
        TabLayout.Tab pestana = pestanas.getTabAt(pestanaInicial);
        if (pestana != null) {
            pestana.select();
        }
    }

    private void escucharCambioDePestana() {
        pestanas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab pestana) {
                mostrarPestanaElegida();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab pestana) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab pestana) {
            }
        });
    }

    private int obtenerPestanaElegida() {
        int posicion = pestanas.getSelectedTabPosition();
        if (posicion < 0) {
            return PESTANA_COMPRAS;
        }
        return posicion;
    }

    private void cargarOperaciones() {
        mostrarCargando();

        api.getMisOperaciones().enqueue(new Callback<List<Operacion>>() {
            @Override
            public void onResponse(Call<List<Operacion>> call, Response<List<Operacion>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    todasLasOperaciones.clear();
                    todasLasOperaciones.addAll(response.body());
                    mostrarPestanaElegida();
                } else {
                    String mensaje = RetrofitClient.leerMensajeError(response, "No se pudieron cargar tus operaciones.");
                    mostrarError(mensaje);
                }
            }

            @Override
            public void onFailure(Call<List<Operacion>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                mostrarError("No se pudo conectar con el servidor.");
            }
        });
    }

    private void mostrarPestanaElegida() {
        int pestana = obtenerPestanaElegida();
        String tipo = TIPOS[pestana];

        operacionesVisibles.clear();
        for (Operacion operacion : todasLasOperaciones) {
            if (tipo.equals(operacion.getTipo())) {
                operacionesVisibles.add(operacion);
            }
        }
        adaptador.notifyDataSetChanged();

        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.GONE);
        if (operacionesVisibles.isEmpty()) {
            textoVacio.setText(TEXTOS_VACIO[pestana]);
            textoVacio.setVisibility(View.VISIBLE);
            listaOperaciones.setVisibility(View.GONE);
        } else {
            textoVacio.setVisibility(View.GONE);
            listaOperaciones.setVisibility(View.VISIBLE);
            listaOperaciones.scheduleLayoutAnimation();
        }
    }

    private void mostrarCargando() {
        barraCargando.setVisibility(View.VISIBLE);
        bloqueError.setVisibility(View.GONE);
        textoVacio.setVisibility(View.GONE);
        listaOperaciones.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.VISIBLE);
        textoVacio.setVisibility(View.GONE);
        listaOperaciones.setVisibility(View.GONE);
    }

    @Override
    public void cancelarSolicitud(Operacion solicitud) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar solicitud")
                .setMessage("¿Seguro que quieres cancelar la reparación de \"" + solicitud.getDispositivo() + "\"?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí, cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogo, int boton) {
                        enviarCancelarSolicitud(solicitud);
                    }
                })
                .show();
    }

    private void enviarCancelarSolicitud(Operacion solicitud) {
        if (enviando) {
            return;
        }
        enviando = true;

        api.cancelarSolicitud(solicitud.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                terminarAccion(response.isSuccessful(), "Solicitud cancelada.", response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                terminarAccionSinConexion();
            }
        });
    }

    @Override
    public void devolverAlquiler(Operacion alquiler) {
        if (enviando) {
            return;
        }
        enviando = true;

        String mensajeOk;
        if ("pendiente".equals(alquiler.getEstado())) {
            mensajeOk = "Alquiler cancelado.";
        } else {
            mensajeOk = "Alquiler devuelto.";
        }

        api.devolverAlquiler(alquiler.getId()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                terminarAccion(response.isSuccessful(), mensajeOk, response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                terminarAccionSinConexion();
            }
        });
    }

    private void terminarAccion(boolean bienHecho, String mensajeOk, Response<?> respuesta) {
        enviando = false;
        if (!isAdded()) {
            return;
        }
        if (bienHecho) {
            mostrarAviso(mensajeOk);
        } else {
            mostrarAviso(RetrofitClient.leerMensajeError(respuesta, "No se pudo completar la operación."));
        }
        cargarOperaciones();
    }

    private void terminarAccionSinConexion() {
        enviando = false;
        if (!isAdded()) {
            return;
        }
        mostrarAviso("No se pudo conectar con el servidor.");
    }

    private void mostrarAviso(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
