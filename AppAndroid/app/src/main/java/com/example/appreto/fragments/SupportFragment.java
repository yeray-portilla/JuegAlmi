package com.example.appreto.fragments;

import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreto.MainActivity;
import com.example.appreto.R;
import com.example.appreto.adapters.AparatoAdapter;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.FotoHelper;
import com.example.appreto.api.LocationHelper;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.Aparato;
import com.example.appreto.models.OpcionCombo;
import com.example.appreto.models.Tienda;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupportFragment extends Fragment implements AparatoAdapter.AlPedirReparacion {

    private static final int PESTANA_APARATOS = 0;
    private static final int PESTANA_PEDIR = 1;

    private ApiService api;
    private TabLayout pestanas;
    private View vistaMisAparatos;
    private View vistaPedirReparacion;

    private final List<Aparato> aparatos = new ArrayList<>();
    private final List<Tienda> tiendas = new ArrayList<>();

    private RecyclerView listaAparatos;
    private AparatoAdapter adaptadorAparatos;
    private TextView textoSinAparatos;
    private Spinner spinnerMarcas;
    private Spinner spinnerDepartamentos;
    private TextInputEditText campoModelo;
    private Button botonGuardarAparato;

    private View avisoSinAparatos;
    private View formularioPedir;
    private Spinner spinnerAparatos;
    private Spinner spinnerTiendas;
    private TextView textoSinTiendas;
    private TextInputEditText campoDescripcion;
    private ImageView imagenFotoPrevia;
    private Button botonQuitarFoto;
    private Button botonHacerFoto;
    private Button botonEnviar;

    private String fotoBase64 = "";

    private ActivityResultLauncher<Uri> lanzadorCamara;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lanzadorCamara = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean fotoHecha) {
                        alVolverDeLaCamara(fotoHecha);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_support, container, false);
        api = RetrofitClient.getApiService(requireContext());

        pestanas = vista.findViewById(R.id.tab_layout);
        ViewGroup contenedor = vista.findViewById(R.id.content_frame);
        vistaMisAparatos = inflater.inflate(R.layout.view_mis_aparatos, contenedor, false);
        vistaPedirReparacion = inflater.inflate(R.layout.view_pedir_reparacion, contenedor, false);
        contenedor.addView(vistaMisAparatos);
        contenedor.addView(vistaPedirReparacion);

        buscarVistas();
        conectarBotones();
        escucharCambioDePestana();
        escucharCambioDeAparato();

        mostrarPestana(PESTANA_APARATOS);
        cargarDatos();
        return vista;
    }

    private void buscarVistas() {
        listaAparatos = vistaMisAparatos.findViewById(R.id.rv_aparatos);
        textoSinAparatos = vistaMisAparatos.findViewById(R.id.tv_no_aparatos);
        spinnerMarcas = vistaMisAparatos.findViewById(R.id.spinner_marcas);
        spinnerDepartamentos = vistaMisAparatos.findViewById(R.id.spinner_departamentos);
        campoModelo = vistaMisAparatos.findViewById(R.id.et_modelo);
        botonGuardarAparato = vistaMisAparatos.findViewById(R.id.btn_guardar_aparato);

        avisoSinAparatos = vistaPedirReparacion.findViewById(R.id.ll_aviso_aparatos);
        formularioPedir = vistaPedirReparacion.findViewById(R.id.ll_formulario_pedir);
        spinnerAparatos = vistaPedirReparacion.findViewById(R.id.spinner_aparatos_pedir);
        spinnerTiendas = vistaPedirReparacion.findViewById(R.id.spinner_tiendas);
        textoSinTiendas = vistaPedirReparacion.findViewById(R.id.tv_sin_tiendas);
        campoDescripcion = vistaPedirReparacion.findViewById(R.id.et_descripcion_problema);
        imagenFotoPrevia = vistaPedirReparacion.findViewById(R.id.iv_foto_previa);
        botonQuitarFoto = vistaPedirReparacion.findViewById(R.id.btn_quitar_foto);
        botonHacerFoto = vistaPedirReparacion.findViewById(R.id.btn_hacer_foto);
        botonEnviar = vistaPedirReparacion.findViewById(R.id.btn_enviar_solicitud);

        adaptadorAparatos = new AparatoAdapter(aparatos, this);
        listaAparatos.setLayoutManager(new LinearLayoutManager(requireContext()));
        listaAparatos.setAdapter(adaptadorAparatos);
    }

    private void conectarBotones() {
        botonGuardarAparato.setOnClickListener(v -> guardarAparato());
        botonHacerFoto.setOnClickListener(v -> hacerFoto());
        botonQuitarFoto.setOnClickListener(v -> quitarFoto());
        botonEnviar.setOnClickListener(v -> enviarSolicitud());
        vistaPedirReparacion.findViewById(R.id.btn_ir_mis_aparatos)
                .setOnClickListener(v -> seleccionarPestana(PESTANA_APARATOS));
    }

    private void escucharCambioDePestana() {
        pestanas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab pestana) {
                mostrarPestana(pestana.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab pestana) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab pestana) {
            }
        });
    }

    private void seleccionarPestana(int numero) {
        TabLayout.Tab pestana = pestanas.getTabAt(numero);
        if (pestana != null) {
            pestana.select();
        }
    }

    private void mostrarPestana(int numero) {
        if (numero == PESTANA_APARATOS) {
            vistaMisAparatos.setVisibility(View.VISIBLE);
            vistaPedirReparacion.setVisibility(View.GONE);
        } else {
            vistaMisAparatos.setVisibility(View.GONE);
            vistaPedirReparacion.setVisibility(View.VISIBLE);
            actualizarAvisoSinAparatos();
        }
    }

    private void actualizarAvisoSinAparatos() {
        if (aparatos.isEmpty()) {
            avisoSinAparatos.setVisibility(View.VISIBLE);
            formularioPedir.setVisibility(View.GONE);
        } else {
            avisoSinAparatos.setVisibility(View.GONE);
            formularioPedir.setVisibility(View.VISIBLE);
        }
    }

    private void cargarDatos() {
        cargarMarcas();
        cargarDepartamentos();
        cargarTiendas();
        cargarMisAparatos();
    }

    private void cargarMarcas() {
        api.getMarcas().enqueue(new Callback<List<OpcionCombo>>() {
            @Override
            public void onResponse(Call<List<OpcionCombo>> call, Response<List<OpcionCombo>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    ponerOpcionesEnSpinner(spinnerMarcas, response.body());
                } else {
                    mostrarAviso(RetrofitClient.leerMensajeError(response, "No se pudieron cargar las marcas."));
                }
            }

            @Override
            public void onFailure(Call<List<OpcionCombo>> call, Throwable t) {
                avisarSinConexion();
            }
        });
    }

    private void cargarDepartamentos() {
        api.getDepartamentos().enqueue(new Callback<List<OpcionCombo>>() {
            @Override
            public void onResponse(Call<List<OpcionCombo>> call, Response<List<OpcionCombo>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    ponerOpcionesEnSpinner(spinnerDepartamentos, response.body());
                } else {
                    mostrarAviso(RetrofitClient.leerMensajeError(response, "No se pudieron cargar los tipos de aparato."));
                }
            }

            @Override
            public void onFailure(Call<List<OpcionCombo>> call, Throwable t) {
                avisarSinConexion();
            }
        });
    }

    private void cargarTiendas() {
        api.getTiendas().enqueue(new Callback<List<Tienda>>() {
            @Override
            public void onResponse(Call<List<Tienda>> call, Response<List<Tienda>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    tiendas.clear();
                    tiendas.addAll(response.body());
                    actualizarTiendasDisponibles();
                } else {
                    mostrarAviso(RetrofitClient.leerMensajeError(response, "No se pudieron cargar las tiendas."));
                }
            }

            @Override
            public void onFailure(Call<List<Tienda>> call, Throwable t) {
                avisarSinConexion();
            }
        });
    }

    private void cargarMisAparatos() {
        api.getMisDispositivos().enqueue(new Callback<List<Aparato>>() {
            @Override
            public void onResponse(Call<List<Aparato>> call, Response<List<Aparato>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    mostrarMisAparatos(response.body());
                } else {
                    mostrarAviso(RetrofitClient.leerMensajeError(response, "No se pudieron cargar tus aparatos."));
                }
            }

            @Override
            public void onFailure(Call<List<Aparato>> call, Throwable t) {
                avisarSinConexion();
            }
        });
    }

    private void mostrarMisAparatos(List<Aparato> nuevos) {
        aparatos.clear();
        aparatos.addAll(nuevos);
        adaptadorAparatos.notifyDataSetChanged();
        listaAparatos.scheduleLayoutAnimation();

        if (aparatos.isEmpty()) {
            textoSinAparatos.setVisibility(View.VISIBLE);
            listaAparatos.setVisibility(View.GONE);
        } else {
            textoSinAparatos.setVisibility(View.GONE);
            listaAparatos.setVisibility(View.VISIBLE);
        }

        ponerOpcionesEnSpinner(spinnerAparatos, aparatos);
        actualizarTiendasDisponibles();
        actualizarAvisoSinAparatos();
    }

    private void guardarAparato() {
        OpcionCombo marca = (OpcionCombo) spinnerMarcas.getSelectedItem();
        OpcionCombo departamento = (OpcionCombo) spinnerDepartamentos.getSelectedItem();
        String modelo = leerTexto(campoModelo).trim();

        if (marca == null || departamento == null || modelo.isEmpty()) {
            mostrarAviso("Elige marca, tipo y escribe el modelo.");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("marca_id", marca.getId());
        body.addProperty("departamento_id", departamento.getId());
        body.addProperty("modelo", modelo);

        botonGuardarAparato.setEnabled(false);
        api.crearDispositivoCliente(body).enqueue(new Callback<Aparato>() {
            @Override
            public void onResponse(Call<Aparato> call, Response<Aparato> response) {
                if (!isAdded()) {
                    return;
                }
                botonGuardarAparato.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    avisarAparatoGuardado(response.body());
                    campoModelo.setText("");
                    cargarMisAparatos();
                } else {
                    mostrarAviso(RetrofitClient.leerMensajeError(response, "No se pudo guardar el aparato."));
                }
            }

            @Override
            public void onFailure(Call<Aparato> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                botonGuardarAparato.setEnabled(true);
                avisarSinConexion();
            }
        });
    }

    private void avisarAparatoGuardado(Aparato guardado) {
        for (Aparato aparato : aparatos) {
            if (aparato.getId() == guardado.getId()) {
                mostrarAviso("Ya tenías ese aparato guardado.");
                return;
            }
        }
        mostrarAviso("Aparato guardado.");
    }

    @Override
    public void pedirReparacion(Aparato aparato) {
        seleccionarPestana(PESTANA_PEDIR);
        int posicion = aparatos.indexOf(aparato);
        if (posicion >= 0) {
            spinnerAparatos.setSelection(posicion);
        }
    }

    private void escucharCambioDeAparato() {
        spinnerAparatos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> desplegable, View vista, int posicion, long id) {
                actualizarTiendasDisponibles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> desplegable) {
                actualizarTiendasDisponibles();
            }
        });
    }

    private void actualizarTiendasDisponibles() {
        Aparato aparato = (Aparato) spinnerAparatos.getSelectedItem();
        List<Tienda> disponibles = buscarTiendasParaAparato(aparato);
        ponerOpcionesEnSpinner(spinnerTiendas, disponibles);

        boolean hayTiendas = !disponibles.isEmpty();
        spinnerTiendas.setEnabled(hayTiendas);
        botonEnviar.setEnabled(hayTiendas);
        boolean tiendasCargadas = !tiendas.isEmpty();
        if (aparato != null && tiendasCargadas && !hayTiendas) {
            textoSinTiendas.setVisibility(View.VISIBLE);
        } else {
            textoSinTiendas.setVisibility(View.GONE);
        }
    }

    private List<Tienda> buscarTiendasParaAparato(Aparato aparato) {
        List<Tienda> resultado = new ArrayList<>();
        if (aparato == null) {
            return resultado;
        }
        if (!apiMandaDepartamentos()) {
            resultado.addAll(tiendas);
            return resultado;
        }
        for (Tienda tienda : tiendas) {
            if (tiendaReparaDepartamento(tienda, aparato.getDepartamentoId())) {
                resultado.add(tienda);
            }
        }
        return resultado;
    }

    private boolean apiMandaDepartamentos() {
        for (Tienda tienda : tiendas) {
            if (!tienda.getDepartamentos().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean tiendaReparaDepartamento(Tienda tienda, int departamentoId) {
        for (OpcionCombo departamento : tienda.getDepartamentos()) {
            if (departamento.getId() == departamentoId) {
                return true;
            }
        }
        return false;
    }

    private void hacerFoto() {
        Uri archivoFoto = FotoHelper.crearUriParaFoto(requireContext());
        try {
            lanzadorCamara.launch(archivoFoto);
        } catch (ActivityNotFoundException e) {
            mostrarAviso("No hay ninguna app de cámara.");
        }
    }

    private void alVolverDeLaCamara(Boolean fotoHecha) {
        if (fotoHecha == null || !fotoHecha) {
            return;
        }
        File archivo = FotoHelper.obtenerArchivoFoto(requireContext());
        Bitmap foto = FotoHelper.leerFotoReducida(archivo);
        if (foto == null) {
            mostrarAviso("No se pudo leer la foto.");
            return;
        }
        fotoBase64 = FotoHelper.convertirABase64(foto);
        mostrarFotoPrevia(foto);
    }

    private void mostrarFotoPrevia(Bitmap foto) {
        imagenFotoPrevia.setImageBitmap(foto);
        imagenFotoPrevia.setVisibility(View.VISIBLE);
        botonQuitarFoto.setVisibility(View.VISIBLE);
        botonHacerFoto.setText("Repetir foto");
    }

    private void quitarFoto() {
        fotoBase64 = "";
        imagenFotoPrevia.setImageBitmap(null);
        imagenFotoPrevia.setVisibility(View.GONE);
        botonQuitarFoto.setVisibility(View.GONE);
        botonHacerFoto.setText("Hacer foto (opcional)");
    }

    private void enviarSolicitud() {
        Aparato aparato = (Aparato) spinnerAparatos.getSelectedItem();
        Tienda tienda = (Tienda) spinnerTiendas.getSelectedItem();
        String descripcion = leerTexto(campoDescripcion).trim();

        if (aparato == null || tienda == null || descripcion.isEmpty()) {
            mostrarAviso("Elige aparato, tienda y describe el problema.");
            return;
        }

        ponerEnviando(true);
        LocationHelper.getLocation(requireActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitud, double longitud) {
                if (!isAdded()) {
                    return;
                }
                JsonObject body = crearCuerpoSolicitud(aparato, tienda, descripcion, latitud, longitud);
                mandarSolicitud(body);
            }
        });
    }

    private JsonObject crearCuerpoSolicitud(Aparato aparato, Tienda tienda, String descripcion,
                                            double latitud, double longitud) {
        JsonObject body = new JsonObject();
        body.addProperty("dispositivo_id", aparato.getId());
        body.addProperty("tienda_id", tienda.getId());
        body.addProperty("descripcion", descripcion);
        body.addProperty("latitud", latitud);
        body.addProperty("longitud", longitud);
        if (!fotoBase64.isEmpty()) {
            body.addProperty("foto_base64", fotoBase64);
        }
        return body;
    }

    private void mandarSolicitud(JsonObject body) {
        api.crearSolicitudJson(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded()) {
                    return;
                }
                ponerEnviando(false);
                if (response.isSuccessful()) {
                    mostrarAviso("Solicitud enviada. Un administrador la revisará.");
                    limpiarFormulario();
                    abrirMisSolicitudes();
                } else {
                    mostrarAviso(RetrofitClient.leerMensajeError(response, "No se pudo enviar la solicitud."));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                ponerEnviando(false);
                avisarSinConexion();
            }
        });
    }

    private void limpiarFormulario() {
        campoDescripcion.setText("");
        quitarFoto();
    }

    private void abrirMisSolicitudes() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).abrirMisSolicitudes();
        }
    }

    private void ponerEnviando(boolean enviando) {
        botonEnviar.setEnabled(!enviando);
        if (enviando) {
            botonEnviar.setText("Enviando...");
        } else {
            botonEnviar.setText("Enviar solicitud");
        }
    }

    private void ponerOpcionesEnSpinner(Spinner spinner, List<?> opciones) {
        List<Object> listaOpciones = new ArrayList<>(opciones);
        ArrayAdapter<Object> adaptador = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, listaOpciones);
        adaptador.setDropDownViewResource(R.layout.item_spinner_desplegable);
        spinner.setAdapter(adaptador);
    }

    private String leerTexto(TextInputEditText campo) {
        if (campo.getText() == null) {
            return "";
        }
        return campo.getText().toString();
    }

    private void mostrarAviso(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    private void avisarSinConexion() {
        if (!isAdded()) {
            return;
        }
        mostrarAviso("No se pudo conectar con el servidor.");
    }
}
