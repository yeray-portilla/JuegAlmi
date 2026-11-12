package com.example.appreto.fragments;

import android.content.Context;
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
import com.example.appreto.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private TextInputEditText etLogin;
    private TextInputEditText etPassword;
    private MaterialButton botonEntrar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etLogin = view.findViewById(R.id.et_login);
        etPassword = view.findViewById(R.id.et_password);

        botonEntrar = view.findViewById(R.id.btn_login);
        botonEntrar.setOnClickListener(v -> pulsarEntrar());
        view.findViewById(R.id.btn_ir_registro).setOnClickListener(v -> irARegistro());

        return view;
    }

    private void pulsarEntrar() {
        String login = leerTexto(etLogin).trim();
        String password = leerTexto(etPassword).trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Rellena el usuario y la contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }
        iniciarSesion(login, password);
    }

    private void iniciarSesion(String login, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("login", login);
        body.addProperty("password", password);

        ponerCargando(true);
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.login(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!isAdded()) {
                    return;
                }
                ponerCargando(false);
                boolean sesionGuardada = response.isSuccessful() && guardarSesion(requireContext(), response.body());
                if (sesionGuardada) {
                    entrarEnLaApp();
                } else {
                    String mensaje = RetrofitClient.leerMensajeError(response, "Credenciales incorrectas");
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                ponerCargando(false);
                Toast.makeText(requireContext(), "No se pudo conectar con el servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ponerCargando(boolean cargando) {
        botonEntrar.setEnabled(!cargando);
        if (cargando) {
            botonEntrar.setText(R.string.login_entrando);
        } else {
            botonEntrar.setText(R.string.btn_login);
        }
    }

    private void entrarEnLaApp() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onLoginSuccess();
        }
    }

    private void irARegistro() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).mostrarRegistro();
        }
    }

    private String leerTexto(TextInputEditText campo) {
        if (campo.getText() == null) {
            return "";
        }
        return campo.getText().toString();
    }

    public static boolean guardarSesion(Context context, LoginResponse datos) {
        if (datos == null || datos.getToken() == null || datos.getUser() == null) {
            return false;
        }
        User usuario = datos.getUser();
        SharedPrefManager.getInstance(context).saveUser(
                datos.getToken(), usuario.getNombreUsuario(), usuario.getEmail(), usuario.getRoles());
        return true;
    }
}
