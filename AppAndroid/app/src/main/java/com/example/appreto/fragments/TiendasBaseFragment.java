package com.example.appreto.fragments;

import android.os.AsyncTask;
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

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

@SuppressWarnings("deprecation")
public abstract class TiendasBaseFragment extends Fragment {

    private ProgressBar barraCargando;
    private LinearLayout bloqueError;
    private TextView textoError;
    private ScrollView scrollTiendas;
    protected LinearLayout listaTiendas;

    private CargarTiendasTask tareaCarga;

    protected abstract void anadirContenido(Tienda tienda, View tarjeta, LinearLayout contenido);

    protected void alTerminarDePintar(List<Tienda> tiendas) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_tiendas, container, false);

        barraCargando = vista.findViewById(R.id.pb_tiendas);
        bloqueError = vista.findViewById(R.id.ll_error_tiendas);
        textoError = vista.findViewById(R.id.tv_error_tiendas);
        scrollTiendas = vista.findViewById(R.id.sv_tiendas);
        listaTiendas = vista.findViewById(R.id.ll_lista_tiendas);

        vista.findViewById(R.id.btn_reintentar_tiendas).setOnClickListener(v -> cargarTiendas());
        vista.findViewById(R.id.btn_recargar_tiendas).setOnClickListener(v -> cargarTiendas());

        cargarTiendas();
        return vista;
    }

    @Override
    public void onDestroyView() {
        cancelarCarga();
        super.onDestroyView();
    }

    protected void cargarTiendas() {
        cancelarCarga();
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        tareaCarga = new CargarTiendasTask(apiService);
        tareaCarga.execute();
    }

    private void cancelarCarga() {
        if (tareaCarga != null) {
            tareaCarga.cancel(true);
            tareaCarga = null;
        }
    }

    private static class ResultadoTiendas {
        List<Tienda> tiendas;
        String mensajeError;

        ResultadoTiendas(List<Tienda> tiendas, String mensajeError) {
            this.tiendas = tiendas;
            this.mensajeError = mensajeError;
        }
    }

    private class CargarTiendasTask extends AsyncTask<Void, Void, ResultadoTiendas> {

        private final ApiService apiService;

        CargarTiendasTask(ApiService apiService) {
            this.apiService = apiService;
        }

        @Override
        protected void onPreExecute() {
            mostrarCargando();
        }

        @Override
        protected ResultadoTiendas doInBackground(Void... parametros) {
            try {
                Response<List<Tienda>> respuesta = apiService.getTiendas().execute();
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    return new ResultadoTiendas(respuesta.body(), null);
                }
                String mensaje = RetrofitClient.leerMensajeError(respuesta, "No se pudieron cargar las tiendas.");
                return new ResultadoTiendas(null, mensaje);
            } catch (IOException e) {
                return new ResultadoTiendas(null, "No se pudo conectar con el servidor.");
            } catch (Exception e) {
                return new ResultadoTiendas(null, "No se pudieron cargar las tiendas.");
            }
        }

        @Override
        protected void onPostExecute(ResultadoTiendas resultado) {
            tareaCarga = null;
            if (!isAdded()) {
                return;
            }
            if (resultado.mensajeError != null) {
                mostrarError(resultado.mensajeError);
            } else {
                pintarTiendas(resultado.tiendas);
            }
        }
    }

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
        listaTiendas.scheduleLayoutAnimation();
        alTerminarDePintar(tiendas);
    }

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

    private void mostrarCargando() {
        barraCargando.setVisibility(View.VISIBLE);
        bloqueError.setVisibility(View.GONE);
        scrollTiendas.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.VISIBLE);
        scrollTiendas.setVisibility(View.GONE);
    }

    private void mostrarLista() {
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.GONE);
        scrollTiendas.setVisibility(View.VISIBLE);
    }
}
