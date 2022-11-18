package com.example.marvelapp.data.repository;import com.example.marvelapp.data.callbacks.LoadCharacterCallback;import com.example.marvelapp.data.callbacks.SaveCharacterCallback;import com.example.marvelapp.data.model.Character;import com.example.marvelapp.data.model.CharacterDataWrapper;import com.example.marvelapp.data.model.CharacterPage;import com.example.marvelapp.data.network.NetworkModule;import com.example.marvelapp.data.network.service.MarvelApiService;import java.util.List;import javax.inject.Inject;import okhttp3.OkHttpClient;import retrofit2.Call;import retrofit2.Callback;import retrofit2.Response;import retrofit2.Retrofit;public class CharactersRemoteDataSource implements CharacterRepository {    private MarvelApiService service;    @Inject    CharactersRemoteDataSource(Retrofit retrofit){        service = retrofit.create(MarvelApiService.class);    }    @Override    public void loadCharacters(CharacterPage page, LoadCharacterCallback callback) {        service.loadCharacters(page.getLimit() , page.getOffset()).enqueue(new Callback<CharacterDataWrapper>() {            @Override            public void onResponse(Call<CharacterDataWrapper> call, Response<CharacterDataWrapper> response) {                if (response.body() != null){                    callback.onSuccess(response.body().getData().getResults());                }else {                    callback.onError(new Error());                }            }            @Override            public void onFailure(Call<CharacterDataWrapper> call, Throwable t) {                callback.onError(new Error());            }        });    }    @Override    public void loadCharacter(String id, LoadCharacterCallback callback) {        service.loadCharacter(id).enqueue(new Callback<CharacterDataWrapper>() {            @Override            public void onResponse(Call<CharacterDataWrapper> call, Response<CharacterDataWrapper> response) {                if (response.body() != null)                    callback.onSuccess(response.body().getData().getResults());                else callback.onError(new Error());            }            @Override            public void onFailure(Call<CharacterDataWrapper> call, Throwable t) {                callback.onError(new Error());            }        });    }    @Override    public void saveCharacters(List<Character> characters, SaveCharacterCallback callback) {    }}