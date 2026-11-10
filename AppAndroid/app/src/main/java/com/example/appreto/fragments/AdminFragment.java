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

/**
 * Pantalla de Administracion (solo la ve el administrador, ver MainActivity).
 * Lista las solicitudes de reparacion por estado (pendiente, aceptada, rechazada) y permite:
 *  - Aceptar una pendiente (pide el precio estimado, > 0). La API crea la reparacion.
 *  - Rechazar una pendiente (pide el motivo).
 *  - Marcar completada la reparacion de una aceptada (pide el precio final, >= 0).
 */
public class AdminFragment extends Fragment implements SolicitudAdminAdapter.AccionesSolicitud {

    // Estado que pide cada pestana, en el mismo orden que en fragment_admin.xml
    private static final String[] ESTADOS = {"pendiente", "aceptada", "rechazada"};

    // Lo que hay que hacer con el texto que el administrador escribe en un dialogo
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

    // true mientras se espera la respuesta de aceptar/rechazar/completar (para no mandarlo dos veces)
    private boolean enviando = false;

    // Crea la vista, conecta pestanas y botones y carga las pendientes
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

    // Al cambiar de pestana se cargan las solicitudes de ese estado
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

    // Estado de la pestana elegida ("pendiente", "aceptada" o "rechazada")
    private String obtenerEstadoElegido() {
        int posicion = pestanasEstado.getSelectedTabPosition();
        if (posicion < 0) {
            posicion = 0;
        }
        return ESTADOS[posicion];
    }

    // =====================================================================
    // CARGAR LA LISTA
    // =====================================================================

    // GET admin/solicitudes?estado=... (403 si el usuario no es administrador)
    private void cargarSolicitudes() {
        mostrarCargando();
        String estado = obtenerEstadoElegido();

        api.getSolicitudesAdmin(estado).enqueue(new Callback<List<SolicitudAdmin>>() {
            @Override
            public void onResponse(Call<List<SolicitudAdmin>> call, Response<List<SolicitudAdmin>> response) {
                if (!isAdded()) {
                    return; // el usuario ya se fue de esta pantalla
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

    // Pone la lista nueva en pantalla (o el aviso "No hay solicitudes ...")
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
            listaSolicitudes.scheduleLayoutAnimation(); // las tarjetas aparecen subiendo
        }
    }

    // Muestra solo la rueda de carga
    private void mostrarCargando() {
        barraCargando.setVisibility(View.VISIBLE);
        bloqueError.setVisibility(View.GONE);
        textoVacio.setVisibility(View.GONE);
        listaSolicitudes.setVisibility(View.GONE);
    }

    // Muestra solo el mensaje de error con el boton Reintentar
    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.VISIBLE);
        textoVacio.setVisibility(View.GONE);
        listaSolicitudes.setVisibility(View.GONE);
    }

    // =====================================================================
    // ACCIONES (las llama SolicitudAdminAdapter al pulsar los botones)
    // =====================================================================

    // Aceptar: pide el precio estimado (mayor que 0) y llama a PUT admin/solicitudes/{id}/aceptar
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

    // Rechazar: pide el motivo (obligatorio) y llama a PUT admin/solicitudes/{id}/rechazar
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

    // Completar: pide el precio final (0 o mas: 0 = garantia) y llama a PUT admin/reparaciones/{id}
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

    // Abre la ubicacion del cliente en Google Maps (o en el navegador)
    @Override
    public void verUbicacion(SolicitudAdmin solicitud) {
        // Locale.US para que los decimales salgan con punto (36.84) y no con coma
        String url = String.format(Locale.US, "https://www.google.com/maps?q=%f,%f",
                solicitud.getLatitud(), solicitud.getLongitud());
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            mostrarAviso("No hay ninguna app para abrir el mapa.");
        }
    }

    // =====================================================================
    // AYUDAS
    // =====================================================================

    // Dialogo con un mensaje y un campo de texto. Al pulsar el boton de aceptar, pasa el texto a "alEscribir".
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

    // Convierte el texto en un precio. Acepta coma o punto decimal ("55,5" o "55.5"). Devuelve null si no es un numero.
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

    // Manda una accion a la API. Si va bien muestra "mensajeOk"; si no, el error de la API. Despues recarga la lista.
    private void enviar(Call<JsonObject> peticion, String mensajeOk) {
        if (enviando) {
            return; // ya hay una accion en marcha
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
                    // 409 = otro admin ya la cambio; 400 = dato no valido...
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

    // Mensaje corto abajo de la pantalla
    private void mostrarAviso(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
