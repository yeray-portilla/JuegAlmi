package com.example.appreto.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.appreto.R;
import com.example.appreto.MainActivity;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.api.SharedPrefManager;
import com.example.appreto.models.LoginResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        
        TextInputEditText etLogin = view.findViewById(R.id.et_login);
        TextInputEditText etPassword = view.findViewById(R.id.et_password);

        view.findViewById(R.id.btn_login).setOnClickListener(v -> {
            String login = etLogin.getText() != null ? etLogin.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("login", login);
            body.addProperty("password", password);

            ApiService api = RetrofitClient.getApiService(getContext());
            api.login(body).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String token = response.body().getToken();
                        if (token != null) {
                            String username = response.body().getUser().getNombreUsuario();
                            String savedEmail = response.body().getUser().getEmail();
                            SharedPrefManager.getInstance(getContext()).saveUser(token, username, savedEmail);
                        }
                        
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).onLoginSuccess();
                        }
                    } else {
                        Toast.makeText(getContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        return view;
    }
}
