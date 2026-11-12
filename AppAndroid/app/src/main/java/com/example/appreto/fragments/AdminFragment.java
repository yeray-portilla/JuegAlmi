package com.example.appreto.fragments;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.appreto.adapters.SolicitudAdminAdapter;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.SolicitudAdmin;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFragment extends Fragment implements SolicitudAdminAdapter.AccionesSolicitud {

    private static final String[] ESTADOS = {"pendiente", "aceptada", "rechazada"};

    private interface AlEscribirDato {
        void datoEscrito(String texto);
    }

    private ApiService api;
    private TabLayout pestanasEstado;
    private ProgressBar barraCargando;
    private LinearLayout bloqueError;
    private TextView textoError;
    private TextView textoVacio;
    private RecyclerView listaSolicitudes;

    private final List<SolicitudAdmin> solicitudes = new ArrayList<>();
    private SolicitudAdminAdapter adaptador;

    private boolean enviando = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_admin, container, false);
        api = RetrofitClient.getApiService(requireContext());

        pestanasEstado = vista.findViewById(R.id.tab_estados_admin);
        barraCargando = vista.findViewById(R.id.pb_admin);
        bloqueError = vista.findViewById(R.id.ll_error_admin);
        textoError = vista.findViewById(R.id.tv_error_admin);
        textoVacio = vista.findViewById(R.id.tv_vacio_admin);
        listaSolicitudes = vista.findViewById(R.id.rv_solicitudes_admin);

        adaptador = new SolicitudAdminAdapter(solicitudes, this);
        listaSolicitudes.setLayoutManager(new LinearLayoutManager(requireContext()));
        listaSolicitudes.setAdapter(adaptador);

        vista.findViewById(R.id.btn_recargar_admin).setOnClickListener(v -> cargarSolicitudes());
        vista.findViewById(R.id.btn_reintentar_admin).setOnClickListener(v -> cargarSolicitudes());
        escucharCambioDePestana();

        cargarSolicitudes();
        return vista;
    }

    private void escucharCambioDePestana() {
        pestanasEstado.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab pestana) {
                cargarSolicitudes();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab pestana) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab pestana) {
            }
        });
    }

    private String obtenerEstadoElegido() {
        int posicion = pestanasEstado.getSelectedTabPosition();
        if (posicion < 0) {
            posicion = 0;
        }
        return ESTADOS[posicion];
    }

    private void cargarSolicitudes() {
        mostrarCargando();
        String estado = obtenerEstadoElegido();

        api.getSolicitudesAdmin(estado).enqueue(new Callback<List<SolicitudAdmin>>() {
            @Override
            public void onResponse(Call<List<SolicitudAdmin>> call, Response<List<SolicitudAdmin>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    mostrarSolicitudes(response.body(), estado);
                } else {
                    String mensaje = RetrofitClient.leerMensajeError(response, "No se pudieron cargar las solicitudes.");
                    mostrarError(mensaje);
                }
            }

            @Override
            public void onFailure(Call<List<SolicitudAdmin>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                mostrarError("No se pudo conectar con el servidor.");
            }
        });
    }

    private void mostrarSolicitudes(List<SolicitudAdmin> nuevas, String estado) {
        solicitudes.clear();
        solicitudes.addAll(nuevas);
        adaptador.notifyDataSetChanged();

        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.GONE);
        if (solicitudes.isEmpty()) {
            textoVacio.setText("No hay solicitudes " + estado + "s.");
            textoVacio.setVisibility(View.VISIBLE);
            listaSolicitudes.setVisibility(View.GONE);
        } else {
            textoVacio.setVisibility(View.GONE);
            listaSolicitudes.setVisibility(View.VISIBLE);
            listaSolicitudes.scheduleLayoutAnimation();
        }
    }

    private void mostrarCargando() {
        barraCargando.setVisibility(View.VISIBLE);
        bloqueError.setVisibility(View.GONE);
        textoVacio.setVisibility(View.GONE);
        listaSolicitudes.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.VISIBLE);
        textoVacio.setVisibility(View.GONE);
        listaSolicitudes.setVisibility(View.GONE);
    }

    @Override
    public void aceptar(SolicitudAdmin solicitud) {
        String mensaje = "Precio estimado para \"" + solicitud.getDispositivo() + "\" (€):";
        pedirDato("Aceptar solicitud", mensaje, "0.00", true, "Aceptar", new AlEscribirDato() {
            @Override
            public void datoEscrito(String texto) {
                Double precio = leerPrecio(texto);
                if (precio == null || precio <= 0) {
                    mostrarAviso("Escribe un precio mayor que 0.");
                    return;
                }
                JsonObject body = new JsonObject();
                body.addProperty("precio_estimado", precio);
                enviar(api.aceptarSolicitudAdmin(solicitud.getId(), body), "Solicitud aceptada. Reparación creada.");
            }
        });
    }

    @Override
    public void rechazar(SolicitudAdmin solicitud) {
        String mensaje = "Motivo del rechazo para \"" + solicitud.getDispositivo() + "\":";
        pedirDato("Rechazar solicitud", mensaje, "Ej.: no hay recambios para este modelo", false, "Rechazar",
                new AlEscribirDato() {
                    @Override
                    public void datoEscrito(String texto) {
                        String motivo = texto.trim();
                        if (motivo.isEmpty()) {
                            mostrarAviso("Debes indicar el motivo del rechazo.");
                            return;
                        }
                        JsonObject body = new JsonObject();
                        body.addProperty("motivo", motivo);
                        enviar(api.rechazarSolicitudAdmin(solicitud.getId(), body), "Solicitud rechazada.");
                    }
                });
    }

    @Override
    public void completar(SolicitudAdmin solicitud) {
        if (solicitud.getReparacionId() == null) {
            return;
        }
        int reparacionId = solicitud.getReparacionId();
        String mensaje = "Precio final de la reparación de \"" + solicitud.getDispositivo() + "\" (€):";
        pedirDato("Completar reparación", mensaje, "0.00", true, "Aceptar", new AlEscribirDato() {
            @Override
            public void datoEscrito(String texto) {
                Double precio = leerPrecio(texto);
                if (precio == null || precio < 0) {
                    mostrarAviso("Escribe un precio válido (0 o más).");
                    return;
                }
                JsonObject body = new JsonObject();
                body.addProperty("estado", "completada");
                body.addProperty("precio_final", precio);
                enviar(api.actualizarReparacionAdmin(reparacionId, body), "Reparación completada.");
            }
        });
    }

    @Override
    public void verUbicacion(SolicitudAdmin solicitud) {
        String url = String.format(Locale.US, "https://www.google.com/maps?q=%f,%f",
                solicitud.getLatitud(), solicitud.getLongitud());
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            mostrarAviso("No hay ninguna app para abrir el mapa.");
        }
    }

    private void pedirDato(String titulo, String mensaje, String pista, boolean esPrecio,
                           String textoBoton, AlEscribirDato alEscribir) {
        View vistaDialogo = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pedir_dato, null);
        TextView textoMensaje = vistaDialogo.findViewById(R.id.tv_mensaje_dato);
        TextInputLayout recuadroDato = vistaDialogo.findViewById(R.id.til_dato);
        TextInputEditText campoDato = vistaDialogo.findViewById(R.id.et_dato);

        textoMensaje.setText(mensaje);
        recuadroDato.setHint(pista);
        if (esPrecio) {
            campoDato.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            campoDato.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            campoDato.setMinLines(3);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(titulo)
                .setView(vistaDialogo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(textoBoton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogo, int boton) {
                        String texto = "";
                        if (campoDato.getText() != null) {
                            texto = campoDato.getText().toString();
                        }
                        alEscribir.datoEscrito(texto);
                    }
                })
                .show();
    }

    private Double leerPrecio(String texto) {
        String limpio = texto.trim().replace(',', '.');
        if (limpio.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void enviar(Call<JsonObject> peticion, String mensajeOk) {
        if (enviando) {
            return;
        }
        enviando = true;

        peticion.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                enviando = false;
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful()) {
                    mostrarAviso(mensajeOk);
                } else {
                    mostrarAviso(RetrofitClient.leerMensajeError(response, "No se pudo guardar."));
                }
                cargarSolicitudes();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                enviando = false;
                if (!isAdded()) {
                    return;
                }
                mostrarAviso("No se pudo conectar con el servidor.");
            }
        });
    }

    private void mostrarAviso(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
