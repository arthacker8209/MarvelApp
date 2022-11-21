package com.example.marvelapp.ui.characterdetails;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.marvelapp.data.model.Character;
import com.example.marvelapp.data.model.Comics;
import com.example.marvelapp.data.repository.CharacterRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class CharacterDetailsViewModel extends ViewModel {
    private CharacterRepository repository;

    @Inject
    CharacterDetailsViewModel(CharacterRepository repository){
        this.repository = repository;
    }

    CharacterDetailsViewModel(){

    }

    private MutableLiveData<List<Comics>> comicsResult = new MutableLiveData<List<Comics>>();
    private MutableLiveData<String> comicsError = new MutableLiveData<String>();
    private MutableLiveData<Boolean>comicsLoading = new MutableLiveData<Boolean>();

    LiveData<List<Comics>> comicsListLiveData = getComicsResult();
    LiveData<Boolean> progressBarLiveData = getComicsLoading();
    LiveData<String> comicsErrorLiveData = getComicsError();
    DisposableObserver<List<Comics>> databaseDisposableObserver ;

     void loadComics(Character character) {
        comicsLoading.postValue(true);
        databaseDisposableObserver = new DisposableObserver<List<Comics>>() {
            @Override
            public void onNext(@NonNull List<Comics> comics) {
                onResponseSuccess(comics);
                Log.d("CharacterDetailsViewModel:LoadCharacters" , comics.size()+"");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                onResponseError(e);
            }

            @Override
            public void onComplete() {

            }
        };

        repository.loadComics(character)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(databaseDisposableObserver);
    }

    public void disposeElements(){
        if(null != databaseDisposableObserver && !databaseDisposableObserver.isDisposed()) databaseDisposableObserver.dispose();
    }

    public void resetValuesLiveData() {
        comicsLoading.postValue(null);
        comicsResult.postValue(null);
    }


    private void onResponseSuccess(List<Comics> comics) {
        comicsLoading.postValue(false);
        comicsResult.postValue(comics);
    }

    private void onResponseError(Throwable e){
        comicsLoading.postValue(false);
        comicsError.postValue(e.getMessage());
    }


    public MutableLiveData<List<Comics>> getComicsResult() {
        return comicsResult;
    }

    public MutableLiveData<String> getComicsError() {
        return comicsError;
    }

    public MutableLiveData<Boolean> getComicsLoading() {
        return comicsLoading;
    }
}