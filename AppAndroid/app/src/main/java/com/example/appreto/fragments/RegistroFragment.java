package com.example.appreto.fragments;

import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appreto.MainActivity;
import com.example.appreto.R;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.LoginResponse;
import com.example.appreto.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroFragment extends Fragment {

    private static final int MAX_USUARIO = 180;
    private static final int MIN_PASSWORD = 6;

    private TextInputLayout tilNombre;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilRepetir;
    private TextInputEditText etNombre;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etRepetir;
    private MaterialButton btnCrearCuenta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        tilNombre = view.findViewById(R.id.til_nombre);
        tilEmail = view.findViewById(R.id.til_email);
        tilPassword = view.findViewById(R.id.til_password);
        tilRepetir = view.findViewById(R.id.til_repetir);
        etNombre = view.findViewById(R.id.et_nombre);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etRepetir = view.findViewById(R.id.et_repetir);
        btnCrearCuenta = view.findViewById(R.id.btn_crear_cuenta);

        btnCrearCuenta.setOnClickListener(v -> crearCuenta());
        view.findViewById(R.id.btn_ir_login).setOnClickListener(v -> irALogin());
        activarTeclaHecho();
        activarBotonAtras();

        return view;
    }

    private void activarTeclaHecho() {
        etRepetir.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView campo, int accion, KeyEvent evento) {
                if (accion == EditorInfo.IME_ACTION_DONE) {
                    crearCuenta();
                    return true;
                }
                return false;
            }
        });
    }

    private void activarBotonAtras() {
        OnBackPressedCallback alPulsarAtras = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                irALogin();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), alPulsarAtras);
    }

    private String leerTexto(TextInputEditText campo) {
        if (campo.getText() == null) {
            return "";
        }
        return campo.getText().toString();
    }

    private void crearCuenta() {
        String nombre = leerTexto(etNombre).trim();
        String email = leerTexto(etEmail).trim();
        String password = leerTexto(etPassword);
        String repetir = leerTexto(etRepetir);

        if (!validarFormulario(nombre, email, password, repetir)) {
            return;
        }
        registrarUsuario(nombre, email, password);
    }

    private boolean validarFormulario(String nombre, String email, String password, String repetir) {
        boolean nombreBien = validarNombre(nombre);
        boolean emailBien = validarEmail(email);
        boolean passwordBien = validarPassword(password);
        boolean repetirBien = validarRepetir(password, repetir);
        return nombreBien && emailBien && passwordBien && repetirBien;
    }

    private boolean validarNombre(String nombre) {
        if (nombre.isEmpty()) {
            tilNombre.setError("Escribe un nombre de usuario.");
            return false;
        }
        if (nombre.length() > MAX_USUARIO) {
            tilNombre.setError("Máximo " + MAX_USUARIO + " caracteres.");
            return false;
        }
        tilNombre.setError(null);
        return true;
    }

    private boolean validarEmail(String email) {
        if (email.isEmpty()) {
            tilEmail.setError("Escribe tu correo.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("El correo no tiene un formato válido.");
            return false;
        }
        tilEmail.setError(null);
        return true;
    }

    private boolean validarPassword(String password) {
        if (password.length() < MIN_PASSWORD) {
            tilPassword.setError("La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres.");
            return false;
        }
        tilPassword.setError(null);
        return true;
    }

    private boolean validarRepetir(String password, String repetir) {
        if (!repetir.equals(password)) {
            tilRepetir.setError("Las contraseñas no coinciden.");
            return false;
        }
        tilRepetir.setError(null);
        return true;
    }

    private void registrarUsuario(String nombre, String email, String password) {
        ponerCargando(true);

        JsonObject body = new JsonObject();
        body.addProperty("nombre_usuario", nombre);
        body.addProperty("email", email);
        body.addProperty("password", password);

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.register(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful()) {
                    iniciarSesion(nombre, password);
                } else {
                    String mensaje = RetrofitClient.leerMensajeError(response, "No se pudo crear la cuenta.");
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    ponerCargando(false);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                ponerCargando(false);
            }
        });
    }

    private void iniciarSesion(String nombre, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("login", nombre);
        body.addProperty("password", password);

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.login(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!isAdded()) {
                    return;
                }
                boolean sesionGuardada = response.isSuccessful()
                        && LoginFragment.guardarSesion(requireContext(), response.body());
                if (sesionGuardada) {
                    entrarEnLaApp(nombre);
                } else {
                    avisarCuentaCreadaSinLogin();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                avisarCuentaCreadaSinLogin();
            }
        });
    }

    private void entrarEnLaApp(String nombre) {
        Toast.makeText(requireContext(), "¡Bienvenido, " + nombre + "!", Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onLoginSuccess();
        }
    }

    private void avisarCuentaCreadaSinLogin() {
        Toast.makeText(requireContext(), "Cuenta creada, pero no se pudo iniciar sesión. Entra desde el login.",
                Toast.LENGTH_LONG).show();
        irALogin();
    }

    private void ponerCargando(boolean cargando) {
        btnCrearCuenta.setEnabled(!cargando);
        if (cargando) {
            btnCrearCuenta.setText(R.string.registro_creando);
        } else {
            btnCrearCuenta.setText(R.string.btn_register);
        }
    }

    private void irALogin() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).mostrarLogin();
        }
    }
}
