package com.example.appreto.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreto.R;
import com.example.appreto.adapters.OperacionAdapter;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.LocationHelper;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.Aparato;
import com.example.appreto.models.OpcionCombo;
import com.example.appreto.models.Operacion;
import com.example.appreto.models.Tienda;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupportFragment extends Fragment {

    private TabLayout tabLayout;
    private View viewMisAparatos;
    private View viewPedirReparacion;
    private View viewMisSolicitudes;

    private ApiService api;
    private String base64Image = "";

    // Data lists
    private List<Aparato> aparatosList = new ArrayList<>();
    private List<OpcionCombo> marcasList = new ArrayList<>();
    private List<OpcionCombo> departamentosList = new ArrayList<>();
    private List<Tienda> tiendasList = new ArrayList<>();
    private List<Operacion> solicitudesList = new ArrayList<>();

    // Mis Aparatos
    private RecyclerView rvAparatos;
    private TextView tvNoAparatos;
    private Spinner spinnerMarcas;
    private Spinner spinnerDepartamentos;
    private TextInputEditText etModelo;
    private Button btnGuardarAparato;

    // Pedir Reparación
    private TextView tvAvisoAparatos;
    private View llFormularioPedir;
    private Spinner spinnerAparatosPedir;
    private Spinner spinnerTiendas;
    private TextInputEditText etDescripcionProblema;
    private ImageView ivFotoPrevia;
    private Button btnQuitarFoto;
    private Button btnHacerFoto;
    private Button btnEnviarSolicitud;

    // Mis Solicitudes
    private RecyclerView rvSolicitudes;
    private OperacionAdapter solicitudesAdapter;

    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            ivFotoPrevia.setImageBitmap(imageBitmap);
                            ivFotoPrevia.setVisibility(View.VISIBLE);
                            btnQuitarFoto.setVisibility(View.VISIBLE);
                            btnHacerFoto.setText("Repetir foto");

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 65, baos);
                            byte[] imageBytes = baos.toByteArray();
                            base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support, container, false);
        api = RetrofitClient.getApiService(getContext());

        tabLayout = view.findViewById(R.id.tab_layout);
        ViewGroup contentFrame = view.findViewById(R.id.content_frame);

        // Inflate all 3 views
        viewMisAparatos = inflater.inflate(R.layout.view_mis_aparatos, contentFrame, false);
        viewPedirReparacion = inflater.inflate(R.layout.view_pedir_reparacion, contentFrame, false);
        
        // Recycle fragment_mis_operaciones for the requests tab but we just need a recyclerview
        viewMisSolicitudes = new RecyclerView(getContext());
        viewMisSolicitudes.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        contentFrame.addView(viewMisAparatos);
        contentFrame.addView(viewPedirReparacion);
        contentFrame.addView(viewMisSolicitudes);

        initViews();
        setupListeners();
        
        // Initial state
        showView(0);
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showView(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadAllData();

        return view;
    }

    private void initViews() {
        // Mis aparatos
        rvAparatos = viewMisAparatos.findViewById(R.id.rv_aparatos);
        tvNoAparatos = viewMisAparatos.findViewById(R.id.tv_no_aparatos);
        spinnerMarcas = viewMisAparatos.findViewById(R.id.spinner_marcas);
        spinnerDepartamentos = viewMisAparatos.findViewById(R.id.spinner_departamentos);
        etModelo = viewMisAparatos.findViewById(R.id.et_modelo);
        btnGuardarAparato = viewMisAparatos.findViewById(R.id.btn_guardar_aparato);

        rvAparatos.setLayoutManager(new LinearLayoutManager(getContext()));
        // Note: For simplicity, I will reuse OperacionAdapter structure or create a small adapter for devices, 
        // but wait, I can just use a simple ArrayAdapter or build a small adapter. Let's create AparatoAdapter later.

        // Pedir Reparación
        tvAvisoAparatos = viewPedirReparacion.findViewById(R.id.tv_aviso_aparatos);
        llFormularioPedir = viewPedirReparacion.findViewById(R.id.ll_formulario_pedir);
        spinnerAparatosPedir = viewPedirReparacion.findViewById(R.id.spinner_aparatos_pedir);
        spinnerTiendas = viewPedirReparacion.findViewById(R.id.spinner_tiendas);
        etDescripcionProblema = viewPedirReparacion.findViewById(R.id.et_descripcion_problema);
        ivFotoPrevia = viewPedirReparacion.findViewById(R.id.iv_foto_previa);
        btnQuitarFoto = viewPedirReparacion.findViewById(R.id.btn_quitar_foto);
        btnHacerFoto = viewPedirReparacion.findViewById(R.id.btn_hacer_foto);
        btnEnviarSolicitud = viewPedirReparacion.findViewById(R.id.btn_enviar_solicitud);

        // Mis Solicitudes
        rvSolicitudes = (RecyclerView) viewMisSolicitudes;
        rvSolicitudes.setLayoutManager(new LinearLayoutManager(getContext()));
        solicitudesAdapter = new OperacionAdapter(solicitudesList, new OperacionAdapter.OnReparacionActionListener() {
            @Override
            public void onEdit(Operacion op) {
                // Show edit dialog
                showEditDialog(op);
            }

            @Override
            public void onCancel(Operacion op) {
                // Show cancel dialog
                showCancelDialog(op);
            }
        });
        rvSolicitudes.setAdapter(solicitudesAdapter);
    }

    private void showView(int position) {
        viewMisAparatos.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        viewPedirReparacion.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        viewMisSolicitudes.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        
        if (position == 1) {
            if (aparatosList.isEmpty()) {
                tvAvisoAparatos.setVisibility(View.VISIBLE);
                llFormularioPedir.setVisibility(View.GONE);
            } else {
                tvAvisoAparatos.setVisibility(View.GONE);
                llFormularioPedir.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupListeners() {
        btnGuardarAparato.setOnClickListener(v -> {
            if (spinnerMarcas.getSelectedItem() == null || spinnerDepartamentos.getSelectedItem() == null) return;
            OpcionCombo marca = (OpcionCombo) spinnerMarcas.getSelectedItem();
            OpcionCombo dep = (OpcionCombo) spinnerDepartamentos.getSelectedItem();
            String modelo = etModelo.getText().toString().trim();
            if (modelo.isEmpty()) {
                Toast.makeText(getContext(), "Escribe el modelo", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("marca_id", marca.getId());
            body.addProperty("departamento_id", dep.getId());
            body.addProperty("modelo", modelo);

            btnGuardarAparato.setEnabled(false);
            api.crearDispositivoCliente(body).enqueue(new Callback<Aparato>() {
                @Override
                public void onResponse(Call<Aparato> call, Response<Aparato> response) {
                    btnGuardarAparato.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Aparato guardado", Toast.LENGTH_SHORT).show();
                        etModelo.setText("");
                        loadMisAparatos();
                    } else {
                        Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Aparato> call, Throwable t) {
                    btnGuardarAparato.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnHacerFoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                takePictureLauncher.launch(takePictureIntent);
            }
        });

        btnQuitarFoto.setOnClickListener(v -> {
            base64Image = "";
            ivFotoPrevia.setImageBitmap(null);
            ivFotoPrevia.setVisibility(View.GONE);
            btnQuitarFoto.setVisibility(View.GONE);
            btnHacerFoto.setText("Hacer foto (opcional)");
        });

        btnEnviarSolicitud.setOnClickListener(v -> {
            if (spinnerAparatosPedir.getSelectedItem() == null || spinnerTiendas.getSelectedItem() == null) return;
            AparatoAOption aparato = (AparatoAOption) spinnerAparatosPedir.getSelectedItem();
            Tienda tienda = (Tienda) spinnerTiendas.getSelectedItem();
            String desc = etDescripcionProblema.getText().toString().trim();

            if (desc.isEmpty()) {
                Toast.makeText(getContext(), "Describe el problema", Toast.LENGTH_SHORT).show();
                return;
            }

            btnEnviarSolicitud.setEnabled(false);
            LocationHelper.getLocation(getActivity(), (lat, lon) -> {
                JsonObject body = new JsonObject();
                body.addProperty("dispositivo_id", aparato.ap.getId());
                body.addProperty("tienda_id", tienda.getId());
                body.addProperty("descripcion", desc);
                body.addProperty("latitud", lat);
                body.addProperty("longitud", lon);
                if (!base64Image.isEmpty()) {
                    body.addProperty("foto_base64", base64Image);
                }

                api.crearSolicitudJson(body).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        btnEnviarSolicitud.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Solicitud enviada", Toast.LENGTH_LONG).show();
                            etDescripcionProblema.setText("");
                            btnQuitarFoto.performClick();
                            tabLayout.selectTab(tabLayout.getTabAt(2));
                            loadMisSolicitudes();
                        } else {
                            Toast.makeText(getContext(), "Error al enviar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        btnEnviarSolicitud.setEnabled(true);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void loadAllData() {
        api.getMarcas().enqueue(new Callback<List<OpcionCombo>>() {
            @Override
            public void onResponse(Call<List<OpcionCombo>> call, Response<List<OpcionCombo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    marcasList = response.body();
                    spinnerMarcas.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, marcasList));
                }
            }
            @Override public void onFailure(Call<List<OpcionCombo>> call, Throwable t) {}
        });

        api.getDepartamentos().enqueue(new Callback<List<OpcionCombo>>() {
            @Override
            public void onResponse(Call<List<OpcionCombo>> call, Response<List<OpcionCombo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    departamentosList = response.body();
                    spinnerDepartamentos.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, departamentosList));
                }
            }
            @Override public void onFailure(Call<List<OpcionCombo>> call, Throwable t) {}
        });

        api.getTiendas().enqueue(new Callback<List<Tienda>>() {
            @Override
            public void onResponse(Call<List<Tienda>> call, Response<List<Tienda>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tiendasList = response.body();
                    spinnerTiendas.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, tiendasList));
                }
            }
            @Override public void onFailure(Call<List<Tienda>> call, Throwable t) {}
        });

        loadMisAparatos();
        loadMisSolicitudes();
    }

    private void loadMisAparatos() {
        api.getMisDispositivos().enqueue(new Callback<List<Aparato>>() {
            @Override
            public void onResponse(Call<List<Aparato>> call, Response<List<Aparato>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    aparatosList = response.body();
                    if (aparatosList.isEmpty()) {
                        tvNoAparatos.setVisibility(View.VISIBLE);
                        rvAparatos.setVisibility(View.GONE);
                    } else {
                        tvNoAparatos.setVisibility(View.GONE);
                        rvAparatos.setVisibility(View.VISIBLE);
                        // Setup simple adapter
                        rvAparatos.setAdapter(new AparatosAdapter(aparatosList));
                    }
                    
                    List<AparatoAOption> opciones = new ArrayList<>();
                    for (Aparato a : aparatosList) opciones.add(new AparatoAOption(a));
                    spinnerAparatosPedir.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, opciones));
                    
                    if (tabLayout.getSelectedTabPosition() == 1) showView(1);
                }
            }
            @Override public void onFailure(Call<List<Aparato>> call, Throwable t) {}
        });
    }

    private void loadMisSolicitudes() {
        api.getMisOperaciones().enqueue(new Callback<List<Operacion>>() {
            @Override
            public void onResponse(Call<List<Operacion>> call, Response<List<Operacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    solicitudesList.clear();
                    for (Operacion op : response.body()) {
                        if ("reparacion".equals(op.getTipo())) {
                            solicitudesList.add(op);
                        }
                    }
                    solicitudesAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Operacion>> call, Throwable t) {}
        });
    }

    class AparatoAOption {
        Aparato ap;
        AparatoAOption(Aparato a) { ap = a; }
        @Override public String toString() { return ap.getMarca() + " " + ap.getModelo() + " (" + ap.getDepartamento() + ")"; }
    }

    class AparatosAdapter extends RecyclerView.Adapter<AparatosAdapter.VH> {
        List<Aparato> list;
        AparatosAdapter(List<Aparato> l) { list = l; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Aparato a = list.get(position);
            holder.t1.setText(a.getMarca() + " " + a.getModelo());
            holder.t1.setTextColor(getResources().getColor(R.color.text_primary));
            holder.t2.setText(a.getDepartamento());
            holder.t2.setTextColor(getResources().getColor(R.color.text_secondary));
            holder.itemView.setOnClickListener(v -> {
                tabLayout.selectTab(tabLayout.getTabAt(1));
                for(int i=0; i<spinnerAparatosPedir.getCount(); i++) {
                    if (((AparatoAOption)spinnerAparatosPedir.getItemAtPosition(i)).ap.getId() == a.getId()) {
                        spinnerAparatosPedir.setSelection(i);
                        break;
                    }
                }
            });
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(@NonNull View itemView) {
                super(itemView);
                t1 = itemView.findViewById(android.R.id.text1);
                t2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
    
    private void showEditDialog(Operacion op) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_desc, null);
        TextView tvDevice = dialogView.findViewById(R.id.tv_edit_device);
        TextInputEditText etDesc = dialogView.findViewById(R.id.et_edit_desc);
        Button btnSave = dialogView.findViewById(R.id.btn_save_desc);

        tvDevice.setText(op.getDispositivo());
        etDesc.setText(op.getDescripcion());

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        btnSave.setOnClickListener(v -> {
            String newDesc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
            if (newDesc.isEmpty()) {
                Toast.makeText(getContext(), "Escribe una descripción", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            JsonObject body = new JsonObject();
            body.addProperty("descripcion", newDesc);
            
            // Si la API no lo manda y no coincide, id
            int idToUpdate = op.getId();
            
            api.editarSolicitud(idToUpdate, body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Descripción actualizada.", Toast.LENGTH_SHORT).show();
                        loadMisSolicitudes();
                    } else {
                        Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    private void showCancelDialog(Operacion op) {
        new AlertDialog.Builder(getContext())
            .setTitle("Cancelar solicitud")
            .setMessage("¿Seguro que quieres cancelar la reparación de \"" + op.getDispositivo() + "\"?")
            .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                api.cancelarSolicitud(op.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Solicitud cancelada.", Toast.LENGTH_SHORT).show();
                            loadMisSolicitudes();
                        } else {
                            Toast.makeText(getContext(), "Error al cancelar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("No", null)
            .show();
    }
}
