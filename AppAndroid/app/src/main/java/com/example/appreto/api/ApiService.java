package com.example.appreto.api;

import com.example.appreto.models.Aparato;
import com.example.appreto.models.LoginResponse;
import com.example.appreto.models.OpcionCombo;
import com.example.appreto.models.Operacion;
import com.example.appreto.models.Product;
import com.example.appreto.models.SolicitudAdmin;
import com.example.appreto.models.Tienda;
import com.example.appreto.models.User;
import com.google.gson.JsonObject;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body JsonObject body);

    @POST("auth/register")
    Call<User> register(@Body JsonObject body);

    @GET("productos/dispositivos")
    Call<List<Product>> getDispositivos(@Query("tipo") String tipo);

    @GET("productos/videojuegos")
    Call<List<Product>> getVideojuegos();

    @GET("productos/consolas")
    Call<List<Product>> getConsolas();

    @POST("operaciones/comprar")
    Call<JsonObject> comprar(@Body JsonObject body);

    @POST("operaciones/alquilar")
    Call<JsonObject> alquilar(@Body JsonObject body);

    @PUT("operaciones/alquileres/{id}/devolver")
    Call<JsonObject> devolverAlquiler(@Path("id") int id);

    @GET("operaciones/mis-operaciones")
    Call<List<Operacion>> getMisOperaciones();

    @GET("soporte/dispositivos")
    Call<List<Aparato>> getMisDispositivos();

    @POST("soporte/dispositivos")
    Call<Aparato> crearDispositivoCliente(@Body JsonObject body);

    @POST("soporte/solicitudes")
    Call<JsonObject> crearSolicitudJson(@Body JsonObject body);

    @Multipart
    @POST("soporte/solicitudes")
    Call<JsonObject> crearSolicitudMultipart(
            @Part("dispositivo_id") RequestBody dispositivoId,
            @Part("tienda_id") RequestBody tiendaId,
            @Part("descripcion") RequestBody descripcion,
            @Part("latitud") RequestBody latitud,
            @Part("longitud") RequestBody longitud,
            @Part MultipartBody.Part foto
    );

    @DELETE("soporte/solicitudes/{id}")
    Call<Void> cancelarSolicitud(@Path("id") int id);

    @GET("admin/solicitudes")
    Call<List<SolicitudAdmin>> getSolicitudesAdmin(@Query("estado") String estado);

    @PUT("admin/solicitudes/{id}/aceptar")
    Call<JsonObject> aceptarSolicitudAdmin(@Path("id") int id, @Body JsonObject body);

    @PUT("admin/solicitudes/{id}/rechazar")
    Call<JsonObject> rechazarSolicitudAdmin(@Path("id") int id, @Body JsonObject body);

    @PUT("admin/reparaciones/{id}")
    Call<JsonObject> actualizarReparacionAdmin(@Path("id") int id, @Body JsonObject body);

    @GET("info/tiendas")
    Call<List<Tienda>> getTiendas();

    @GET("info/departamentos")
    Call<List<OpcionCombo>> getDepartamentos();

    @GET("info/marcas")
    Call<List<OpcionCombo>> getMarcas();

    @GET
    Call<ResponseBody> descargarImagen(@Url String url);
}
