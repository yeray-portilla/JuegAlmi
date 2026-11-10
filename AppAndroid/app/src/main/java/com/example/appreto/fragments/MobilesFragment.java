package com.example.appreto.fragments;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.example.appreto.adapters.MobileAdapter;
import com.example.appreto.api.ApiService;
import com.example.appreto.api.LocationHelper;
import com.example.appreto.api.PrecioHelper;
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

/**
 * Catalogo de la barra de abajo. La misma pantalla sirve para las tres categorias:
 *  - "Dispositivos": moviles y tablets, con filtros Todos / Smartphones / Tablets. Solo se compran.
 *  - "Videojuegos": se compran y se alquilan.
 *  - "Consolas": solo se compran.
 */
public class MobilesFragment extends Fragment implements MobileAdapter.OnProductClickListener {

    public static final String DISPOSITIVOS = "Dispositivos";
    public static final String VIDEOJUEGOS = "Videojuegos";
    public static final String CONSOLAS = "Consolas";

    private static final String ARG_CATEGORIA = "categoria";

    // Pestanas de filtro (solo en Dispositivos), en el mismo orden que en fragment_mobiles.xml
    private static final int FILTRO_TODOS = 0;
    private static final int FILTRO_SMARTPHONES = 1;
    private static final int FILTRO_TABLETS = 2;

    // Las fechas se manejan como texto "yyyy-MM-dd" (lo que pide la API); asi se pueden comparar como texto
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private String categoria = DISPOSITIVOS;
    private ApiService api;

    private TabLayout pestanasFiltro;
    private ProgressBar barraCargando;
    private LinearLayout bloqueError;
    private TextView textoError;
    private TextView textoVacio;
    private RecyclerView listaProductos;

    // Todos los productos que manda la API, y los que se ven con el filtro elegido
    private final List<Product> todosLosProductos = new ArrayList<>();
    private final List<Product> productosVisibles = new ArrayList<>();
    private MobileAdapter adaptador;

    // true mientras se compra o se alquila (para no mandarlo dos veces)
    private boolean enviando = false;

    // Fechas elegidas en la ventana de alquiler
    private String fechaInicio = "";
    private String fechaFin = "";

    // Crea la pantalla de una categoria (DISPOSITIVOS, VIDEOJUEGOS o CONSOLAS)
    public static MobilesFragment nuevaInstancia(String categoria) {
        MobilesFragment fragment = new MobilesFragment();
        Bundle argumentos = new Bundle();
        argumentos.putString(ARG_CATEGORIA, categoria);
        fragment.setArguments(argumentos);
        return fragment;
    }

    // Crea la vista, prepara la lista y carga los productos
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_mobiles, container, false);
        api = RetrofitClient.getApiService(requireContext());
        leerCategoria();

        TextView textoTitulo = vista.findViewById(R.id.tv_titulo_catalogo);
        pestanasFiltro = vista.findViewById(R.id.tab_layout);
        barraCargando = vista.findViewById(R.id.pb_catalogo);
        bloqueError = vista.findViewById(R.id.ll_error_catalogo);
        textoError = vista.findViewById(R.id.tv_error_catalogo);
        textoVacio = vista.findViewById(R.id.tv_vacio_catalogo);
        listaProductos = vista.findViewById(R.id.recycler_view);

        textoTitulo.setText(obtenerTitulo());

        adaptador = new MobileAdapter(productosVisibles, this);
        listaProductos.setLayoutManager(new LinearLayoutManager(requireContext()));
        listaProductos.setAdapter(adaptador);

        vista.findViewById(R.id.btn_recargar_catalogo).setOnClickListener(v -> cargarProductos());
        vista.findViewById(R.id.btn_reintentar_catalogo).setOnClickListener(v -> cargarProductos());
        prepararFiltros();

        cargarProductos();
        return vista;
    }

    // Lee la categoria que nos han pasado en nuevaInstancia()
    private void leerCategoria() {
        if (getArguments() != null) {
            categoria = getArguments().getString(ARG_CATEGORIA, DISPOSITIVOS);
        }
    }

    // Titulo de la pagina, igual que en Ionic
    private String obtenerTitulo() {
        if (categoria.equals(VIDEOJUEGOS)) {
            return "Videojuegos";
        }
        if (categoria.equals(CONSOLAS)) {
            return "Consolas";
        }
        return "Móviles";
    }

    // Texto cuando no hay productos
    private String obtenerTextoVacio() {
        if (categoria.equals(VIDEOJUEGOS)) {
            return "No hay videojuegos.";
        }
        if (categoria.equals(CONSOLAS)) {
            return "No hay consolas.";
        }
        return "No hay productos.";
    }

    // Los filtros Todos / Smartphones / Tablets solo salen en Dispositivos
    private void prepararFiltros() {
        if (!categoria.equals(DISPOSITIVOS)) {
            pestanasFiltro.setVisibility(View.GONE);
            return;
        }
        pestanasFiltro.setVisibility(View.VISIBLE);
        pestanasFiltro.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab pestana) {
                mostrarProductosFiltrados();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab pestana) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab pestana) {
            }
        });
    }

    // =====================================================================
    // CARGAR Y MOSTRAR
    // =====================================================================

    // Pide a la API los productos de la categoria
    private void cargarProductos() {
        mostrarCargando();

        Call<List<Product>> peticion = crearPeticionCatalogo();
        peticion.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (!isAdded()) {
                    return; // el usuario ya se fue de esta pantalla
                }
                if (response.isSuccessful() && response.body() != null) {
                    guardarProductos(response.body());
                    mostrarProductosFiltrados();
                } else {
                    mostrarError(RetrofitClient.leerMensajeError(response, "No se pudo cargar el catálogo."));
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                mostrarError("No se pudo conectar con el servidor.");
            }
        });
    }

    // La ruta de la API depende de la categoria
    private Call<List<Product>> crearPeticionCatalogo() {
        if (categoria.equals(VIDEOJUEGOS)) {
            return api.getVideojuegos();
        }
        if (categoria.equals(CONSOLAS)) {
            return api.getConsolas();
        }
        return api.getDispositivos(null); // null = telefonos y tablets juntos
    }

    // Guarda los productos. A los videojuegos y consolas les ponemos su categoria
    // (la API no la manda), asi MobileAdapter sabe que solo los videojuegos se alquilan.
    private void guardarProductos(List<Product> productos) {
        todosLosProductos.clear();
        for (Product producto : productos) {
            if (!categoria.equals(DISPOSITIVOS)) {
                producto.setCategory(categoria);
            }
            todosLosProductos.add(producto);
        }
    }

    // Deja en la lista los productos del filtro elegido (Todos, Smartphones o Tablets)
    private void mostrarProductosFiltrados() {
        int filtro = obtenerFiltroElegido();

        productosVisibles.clear();
        for (Product producto : todosLosProductos) {
            if (pasaElFiltro(producto, filtro)) {
                productosVisibles.add(producto);
            }
        }
        adaptador.notifyDataSetChanged();
        mostrarListaOVacio();
    }

    // Numero del filtro elegido (en Videojuegos y Consolas no hay filtro: "Todos")
    private int obtenerFiltroElegido() {
        if (!categoria.equals(DISPOSITIVOS) || pestanasFiltro.getSelectedTabPosition() < 0) {
            return FILTRO_TODOS;
        }
        return pestanasFiltro.getSelectedTabPosition();
    }

    // true si el producto se tiene que ver con ese filtro
    private boolean pasaElFiltro(Product producto, int filtro) {
        if (filtro == FILTRO_SMARTPHONES) {
            return "Smartphones".equals(producto.getCategory());
        }
        if (filtro == FILTRO_TABLETS) {
            return "Tablets".equals(producto.getCategory());
        }
        return true;
    }

    // Muestra la lista (con la animacion de aparicion) o el texto "No hay ..."
    private void mostrarListaOVacio() {
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.GONE);
        if (productosVisibles.isEmpty()) {
            textoVacio.setText(obtenerTextoVacio());
            textoVacio.setVisibility(View.VISIBLE);
            listaProductos.setVisibility(View.GONE);
        } else {
            textoVacio.setVisibility(View.GONE);
            listaProductos.setVisibility(View.VISIBLE);
            listaProductos.scheduleLayoutAnimation(); // las tarjetas aparecen subiendo
        }
    }

    // Muestra solo la rueda de carga
    private void mostrarCargando() {
        barraCargando.setVisibility(View.VISIBLE);
        bloqueError.setVisibility(View.GONE);
        textoVacio.setVisibility(View.GONE);
        listaProductos.setVisibility(View.GONE);
    }

    // Muestra solo el mensaje de error con el boton Reintentar
    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        barraCargando.setVisibility(View.GONE);
        bloqueError.setVisibility(View.VISIBLE);
        textoVacio.setVisibility(View.GONE);
        listaProductos.setVisibility(View.GONE);
    }

    // =====================================================================
    // COMPRAR
    // =====================================================================

    // Boton "Comprar" de una tarjeta: pide confirmacion
    @Override
    public void onBuyClick(Product producto) {
        String mensaje = "¿Comprar \"" + producto.getTitle() + "\" por "
                + PrecioHelper.formatearPrecio(producto.getPrice()) + "?";
        new AlertDialog.Builder(requireContext())
                .setTitle("Comprar")
                .setMessage(mensaje)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogo, int boton) {
                        comprarConUbicacion(producto);
                    }
                })
                .show();
    }

    // La API exige latitud y longitud: si no hay permiso o posicion, LocationHelper devuelve 0 y 0
    private void comprarConUbicacion(Product producto) {
        if (enviando) {
            return;
        }
        enviando = true;
        LocationHelper.getLocation(requireActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitud, double longitud) {
                if (!isAdded()) {
                    enviando = false;
                    return;
                }
                JsonObject body = crearCuerpoCompra(producto, latitud, longitud);
                enviarOperacion(api.comprar(body), "Compra realizada.");
            }
        });
    }

    // JSON de POST operaciones/comprar: { latitud, longitud, productos: [{id, cantidad}] }
    private JsonObject crearCuerpoCompra(Product producto, double latitud, double longitud) {
        JsonObject lineaCompra = new JsonObject();
        lineaCompra.addProperty("id", producto.getId());
        lineaCompra.addProperty("cantidad", 1);

        JsonArray productos = new JsonArray();
        productos.add(lineaCompra);

        JsonObject body = new JsonObject();
        body.addProperty("latitud", latitud);
        body.addProperty("longitud", longitud);
        body.add("productos", productos);
        return body;
    }

    // =====================================================================
    // ALQUILAR (solo videojuegos)
    // =====================================================================

    // Boton "Alquilar": ventana con las fechas. Por defecto, de hoy a dentro de 7 dias (como en Ionic).
    @Override
    public void onRentClick(Product producto) {
        if (!MobileAdapter.esAlquilable(producto)) {
            return; // las consolas y los moviles solo se compran
        }
        View vistaDialogo = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rent, null);
        TextView textoNombre = vistaDialogo.findViewById(R.id.tv_game_name);
        TextView textoPrecios = vistaDialogo.findViewById(R.id.tv_game_prices);
        Button botonInicio = vistaDialogo.findViewById(R.id.btn_start_date);
        Button botonFin = vistaDialogo.findViewById(R.id.btn_end_date);
        TextView textoErrorFechas = vistaDialogo.findViewById(R.id.tv_error_fechas);
        Button botonConfirmar = vistaDialogo.findViewById(R.id.btn_confirm_rent);

        textoNombre.setText(producto.getTitle());
        textoPrecios.setText(crearTextoPreciosAlquiler(producto));

        fechaInicio = obtenerHoy();
        fechaFin = obtenerDentroDeDias(7);
        botonInicio.setText("Inicio: " + fechaInicio);
        botonFin.setText("Fin: " + fechaFin);

        AlertDialog dialogo = new AlertDialog.Builder(requireContext()).setView(vistaDialogo).create();

        botonInicio.setOnClickListener(v -> elegirFecha(true, botonInicio));
        botonFin.setOnClickListener(v -> elegirFecha(false, botonFin));
        botonConfirmar.setOnClickListener(v -> confirmarAlquiler(producto, dialogo, textoErrorFechas));

        dialogo.show();
    }

    // "4 €/día · 20 €/semana · 60 €/mes"
    private String crearTextoPreciosAlquiler(Product producto) {
        return PrecioHelper.formatearPrecio(producto.getPrecioDiario()) + "/día · "
                + PrecioHelper.formatearPrecio(producto.getPrecioSemanal()) + "/semana · "
                + PrecioHelper.formatearPrecio(producto.getPrecioMensual()) + "/mes";
    }

    // Abre el calendario. No deja elegir dias anteriores a hoy (ni, para el fin, anteriores al inicio).
    private void elegirFecha(boolean esInicio, Button boton) {
        String fechaYaElegida = fechaFin;
        if (esInicio) {
            fechaYaElegida = fechaInicio;
        }
        Calendar fechaActual = convertirACalendar(fechaYaElegida);
        DatePickerDialog calendario = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker selector, int anio, int mes, int dia) {
                        Calendar elegida = Calendar.getInstance();
                        elegida.set(anio, mes, dia);
                        String texto = formatoFecha.format(elegida.getTime());
                        guardarFechaElegida(esInicio, texto, boton);
                    }
                },
                fechaActual.get(Calendar.YEAR), fechaActual.get(Calendar.MONTH), fechaActual.get(Calendar.DAY_OF_MONTH));

        String fechaMinima = obtenerHoy();
        if (!esInicio) {
            fechaMinima = fechaInicio;
        }
        calendario.getDatePicker().setMinDate(convertirACalendar(fechaMinima).getTimeInMillis());
        calendario.show();
    }

    // Guarda la fecha elegida y la escribe en su boton
    private void guardarFechaElegida(boolean esInicio, String fecha, Button boton) {
        if (esInicio) {
            fechaInicio = fecha;
            boton.setText("Inicio: " + fecha);
        } else {
            fechaFin = fecha;
            boton.setText("Fin: " + fecha);
        }
    }

    // Comprueba las fechas y, si estan bien, cierra la ventana y alquila
    private void confirmarAlquiler(Product producto, AlertDialog dialogo, TextView textoErrorFechas) {
        String error = comprobarFechas();
        if (error != null) {
            textoErrorFechas.setText(error);
            textoErrorFechas.setVisibility(View.VISIBLE);
            return;
        }
        dialogo.dismiss();
        alquilarConUbicacion(producto);
    }

    // Devuelve el error de las fechas, o null si estan bien. Las fechas "yyyy-MM-dd" se comparan como texto.
    private String comprobarFechas() {
        if (fechaInicio.isEmpty() || fechaFin.isEmpty()) {
            return "Elige las dos fechas.";
        }
        if (fechaInicio.compareTo(obtenerHoy()) < 0) {
            return "La fecha de inicio no puede ser anterior a hoy.";
        }
        if (fechaFin.compareTo(fechaInicio) < 0) {
            return "La fecha de fin no puede ser anterior a la de inicio.";
        }
        return null;
    }

    // Pide la ubicacion (0 y 0 si no hay) y manda el alquiler
    private void alquilarConUbicacion(Product producto) {
        if (enviando) {
            return;
        }
        enviando = true;
        LocationHelper.getLocation(requireActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitud, double longitud) {
                if (!isAdded()) {
                    enviando = false;
                    return;
                }
                JsonObject body = crearCuerpoAlquiler(producto, latitud, longitud);
                enviarOperacion(api.alquilar(body), "Alquiler realizado.");
            }
        });
    }

    // JSON de POST operaciones/alquilar: { fecha_inicio, fecha_fin, latitud, longitud, videojuegos: [{id}] }
    private JsonObject crearCuerpoAlquiler(Product producto, double latitud, double longitud) {
        JsonObject juego = new JsonObject();
        juego.addProperty("id", producto.getId());

        JsonArray videojuegos = new JsonArray();
        videojuegos.add(juego);

        JsonObject body = new JsonObject();
        body.addProperty("fecha_inicio", fechaInicio);
        body.addProperty("fecha_fin", fechaFin);
        body.addProperty("latitud", latitud);
        body.addProperty("longitud", longitud);
        body.add("videojuegos", videojuegos);
        return body;
    }

    // =====================================================================
    // AYUDAS
    // =====================================================================

    // Manda una compra o un alquiler. Si va bien, avisa con el total y recarga (cambia el stock).
    private void enviarOperacion(Call<JsonObject> peticion, String mensajeOk) {
        peticion.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                enviando = false;
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    double total = response.body().get("total").getAsDouble();
                    mostrarAviso(mensajeOk + " Total: " + PrecioHelper.formatearPrecio(total));
                    cargarProductos();
                } else {
                    mostrarAviso(RetrofitClient.leerMensajeError(response, "No se pudo completar la operación."));
                }
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

    // Hoy como texto "yyyy-MM-dd"
    private String obtenerHoy() {
        return formatoFecha.format(Calendar.getInstance().getTime());
    }

    // Hoy + N dias como texto "yyyy-MM-dd"
    private String obtenerDentroDeDias(int dias) {
        Calendar fecha = Calendar.getInstance();
        fecha.add(Calendar.DAY_OF_MONTH, dias);
        return formatoFecha.format(fecha.getTime());
    }

    // Convierte "yyyy-MM-dd" a Calendar (a las 00:00). Si el texto esta mal, devuelve hoy.
    private Calendar convertirACalendar(String fecha) {
        Calendar calendario = Calendar.getInstance();
        try {
            calendario.setTime(formatoFecha.parse(fecha));
        } catch (Exception e) {
            // texto vacio o mal escrito: nos quedamos con hoy
        }
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario;
    }

    // Mensaje corto abajo de la pantalla
    private void mostrarAviso(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
