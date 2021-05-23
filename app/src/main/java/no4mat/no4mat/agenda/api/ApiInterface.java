package no4mat.no4mat.agenda.api;


import java.util.List;

import no4mat.no4mat.agenda.AgendaEntry;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiInterface {
    public final static String BASE_URL = "http://192.168.1.19:3000/";

    @GET("agenda")
    Call<List<AgendaEntry>> getEntries();

    @POST("agenda")
    Call<AgendaEntry> addEntry(@Body AgendaEntry agendaEntry);

    @DELETE("agenda/{id}")
    Call<AgendaEntry> deleteEntry(@Path("id") int id);
}
