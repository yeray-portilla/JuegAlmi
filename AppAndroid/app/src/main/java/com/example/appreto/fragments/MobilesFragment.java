package com.example.appreto.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appreto.R;
import com.example.appreto.adapters.MobileAdapter;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.LocationHelper;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.Product;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MobilesFragment extends Fragment {

    private String currentCategory;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private List<Product> currentDataList = new ArrayList<>();
    private MobileAdapter adapter;

    public MobilesFragment() {
        this.currentCategory = "Móviles";
    }

    public MobilesFragment(String category) {
        this.currentCategory = category;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobiles, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        tabLayout = view.findViewById(R.id.tab_layout);
        
        adapter = new MobileAdapter(currentDataList, new MobileAdapter.OnProductClickListener() {
            @Override
            public void onBuyClick(Product product) {
                comprarProducto(product);
            }

            @Override
            public void onRentClick(Product product) {
                alquilarProducto(product);
            }
        });
        recyclerView.setAdapter(adapter);

        loadDataFromApi();

        if (currentCategory.equals("Dispositivos")) {
            tabLayout.setVisibility(View.VISIBLE);
            
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    filterList(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
            
        } else {
            tabLayout.setVisibility(View.GONE);
        }
        
        return view;
    }

    private void filterList(int position) {
        List<Product> filteredList = new ArrayList<>();
        if (position == 0) { // Todos
            filteredList.addAll(currentDataList);
        } else if (position == 1) { // Smartphones
            for (Product p : currentDataList) {
                if ("Smartphones".equals(p.getCategory())) {
                    filteredList.add(p);
                }
            }
        } else if (position == 2) { // Tablets
            for (Product p : currentDataList) {
                if ("Tablets".equals(p.getCategory())) {
                    filteredList.add(p);
                }
            }
        }
        
        recyclerView.setAdapter(new MobileAdapter(filteredList, new MobileAdapter.OnProductClickListener() {
            @Override
            public void onBuyClick(Product product) {
                comprarProducto(product);
            }

            @Override
            public void onRentClick(Product product) {
                alquilarProducto(product);
            }
        }));
    }

    private void comprarProducto(Product product) {
        new AlertDialog.Builder(getContext())
            .setTitle("Comprar")
            .setMessage("¿Comprar \"" + product.getTitle() + "\" por " + product.getPrice() + " €?")
            .setPositiveButton("Aceptar", (dialog, which) -> {
                LocationHelper.getLocation(getActivity(), (lat, lon) -> {
                    ApiService api = RetrofitClient.getApiService(getContext());
                    JsonObject body = new JsonObject();
                    body.addProperty("latitud", lat);
                    body.addProperty("longitud", lon);
                    JsonArray productos = new JsonArray();
                    JsonObject p = new JsonObject();
                    p.addProperty("id", product.getId());
                    p.addProperty("cantidad", 1);
                    productos.add(p);
                    body.add("productos", productos);

                    api.comprar(body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                double total = response.body().get("total").getAsDouble();
                                Toast.makeText(getContext(), "Compra realizada. Total: " + total + " €", Toast.LENGTH_LONG).show();
                                loadDataFromApi();
                            } else {
                                Toast.makeText(getContext(), "Error en compra", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void alquilarProducto(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rent, null);
        TextView tvGameName = dialogView.findViewById(R.id.tv_game_name);
        TextView tvPrices = dialogView.findViewById(R.id.tv_game_prices);
        Button btnStart = dialogView.findViewById(R.id.btn_start_date);
        Button btnEnd = dialogView.findViewById(R.id.btn_end_date);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_rent);

        tvGameName.setText(product.getTitle());
        tvPrices.setText(product.getPrecioDiario() + " €/día");

        final String[] startDate = {""};
        final String[] endDate = {""};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        btnStart.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                startDate[0] = sdf.format(selected.getTime());
                btnStart.setText("Inicio: " + startDate[0]);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnEnd.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                endDate[0] = sdf.format(selected.getTime());
                btnEnd.setText("Fin: " + endDate[0]);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnConfirm.setOnClickListener(v -> {
            if (startDate[0].isEmpty() || endDate[0].isEmpty()) {
                Toast.makeText(getContext(), "Elige ambas fechas", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            LocationHelper.getLocation(getActivity(), (lat, lon) -> {
                ApiService api = RetrofitClient.getApiService(getContext());
                JsonObject body = new JsonObject();
                body.addProperty("fecha_inicio", startDate[0]);
                body.addProperty("fecha_fin", endDate[0]);
                body.addProperty("latitud", lat);
                body.addProperty("longitud", lon);
                JsonArray juegos = new JsonArray();
                JsonObject j = new JsonObject();
                j.addProperty("id", product.getId());
                juegos.add(j);
                body.add("videojuegos", juegos);

                api.alquilar(body).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            double total = response.body().get("total").getAsDouble();
                            Toast.makeText(getContext(), "Alquiler realizado. Total: " + total + " €", Toast.LENGTH_LONG).show();
                            loadDataFromApi();
                        } else {
                            Toast.makeText(getContext(), "Error al alquilar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    private void loadDataFromApi() {
        ApiService api = RetrofitClient.getApiService(getContext());
        Call<List<Product>> call = null;

        if (currentCategory.equals("Dispositivos")) {
            call = api.getDispositivos(null);
        } else if (currentCategory.equals("Videojuegos")) {
            call = api.getVideojuegos();
        } else if (currentCategory.equals("Consolas")) {
            call = api.getConsolas();
        }

        if (call != null) {
            call.enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentDataList.clear();
                        currentDataList.addAll(response.body());
                        // Ajustar la categoría localmente para que el adaptador muestre bien los botones
                        for (Product p : currentDataList) {
                            if (!currentCategory.equals("Dispositivos")) {
                                p.setCategory(currentCategory);
                            } else if (p.getCategory() == null) {
                                // Fallback por si la API no manda el tipo en algún dispositivo
                                p.setCategory("Producto");
                            }
                        }
                        adapter.notifyDataSetChanged();
                        
                        // Si estamos en dispositivos y hay algún tab seleccionado, re-filtrar
                        if (currentCategory.equals("Dispositivos") && tabLayout != null) {
                            filterList(tabLayout.getSelectedTabPosition());
                        }
                    } else {
                        Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Product>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
