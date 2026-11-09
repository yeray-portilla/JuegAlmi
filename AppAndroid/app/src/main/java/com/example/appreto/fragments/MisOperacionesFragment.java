package com.example.appreto.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appreto.R;
import com.example.appreto.adapters.OperacionAdapter;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.Operacion;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisOperacionesFragment extends Fragment {

    private RecyclerView recyclerView;
    private OperacionAdapter adapter;
    private List<Operacion> dataList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_operaciones, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_operaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new OperacionAdapter(dataList);
        recyclerView.setAdapter(adapter);

        loadOperaciones();
        return view;
    }

    private void loadOperaciones() {
        ApiService api = RetrofitClient.getApiService(getContext());
        api.getMisOperaciones().enqueue(new Callback<List<Operacion>>() {
            @Override
            public void onResponse(Call<List<Operacion>> call, Response<List<Operacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dataList.clear();
                    dataList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error al cargar operaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Operacion>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
