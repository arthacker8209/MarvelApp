package com.example.marvelapp.ui.characters;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.marvelapp.data.model.Character;
import com.example.marvelapp.data.model.CharacterDataWrapper;
import com.example.marvelapp.data.model.CharacterPage;
import com.example.marvelapp.data.repository.CharacterRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class CharactersViewModel extends ViewModel {
    private CharacterRepository repository;

    @Inject
    CharactersViewModel(CharacterRepository repository){
        this.repository = repository;
        loadCharacters();
    }



    private MutableLiveData<List<Character>> charactersResult = new MutableLiveData<List<Character>>();
    private MutableLiveData<String> charactersError = new MutableLiveData<String>();
    private MutableLiveData<Boolean> characterLoading = new MutableLiveData<Boolean>();
    private MutableLiveData<Boolean> swipeRefreshLayoutLiveData = new MutableLiveData<Boolean>();
    private MutableLiveData<List<Character>> searchCharacterFromDb = new MutableLiveData<List<Character>>();



    private MutableLiveData<String> errorSearchCharacterFromDb = new MutableLiveData<String>();

    LiveData<List<Character>> charactersListLiveData = getCharactersResult();
    LiveData<List<Character>> searchCharacterFromDbLiveData = getSearchCharacterFromDb();
    LiveData<String> errorSearchCharacterFromDbLiveData = getErrorSearchCharacterFromDb();
    LiveData<Boolean> progressBarLiveData = getCharacterLoading();
    LiveData<Boolean> _swipeRefreshLayoutLiveData = getSwipeRefreshLayoutLiveData();
    DisposableObserver<List<Character>> databaseDisposableObserver ;
    DisposableObserver<CharacterDataWrapper> networkDisposableObserver ;
    DisposableObserver<List<Character>> databaseSearcherDisposableObserver ;


//    public void search(String query) {
//        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
//                emitter.onNext(query);
//            }
//        });
//        observable.subscribeOn(Schedulers.io())
//                .debounce(2,TimeUnit.SECONDS)
//                .doOnNext( c->{
//                    Log.d("CharactersViewModel", "upstream"+c);
//                        }
//                )
//                .distinctUntilChanged()
//                .subscribe(o -> {
//                           repository.getSearchResultsFromDB(query)
//                                   .subscribeOn(Schedulers.io())
//                                   .observeOn(AndroidSchedulers.mainThread())
//                                   .subscribe(
//                                           c-> searchCharacterFromDb.postValue(c),
//                                           d-> errorSearchCharacterFromDb.postValue(d.getMessage())
//                                   );
//                        }
//
//                );

    public void searchCharacterFromDb(String query){
        characterLoading.postValue(true);
        databaseSearcherDisposableObserver = new DisposableObserver<List<Character>>() {
            @Override
            public void onNext(@NonNull List<Character> characters) {
                characterLoading.postValue(false);
                searchCharacterFromDb.postValue(characters);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                characterLoading.postValue(false);
                errorSearchCharacterFromDb.postValue(e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
        repository.getSearchResultsFromDB(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                       databaseSearcherDisposableObserver
                );
    }

    private void loadCharacters(){
        characterLoading.postValue(true);
        databaseDisposableObserver = new DisposableObserver<List<Character>>() {

            @Override
            public void onNext(@NonNull List<Character> characters) {
                onResponseSuccess(characters);
                Log.d("CharacterViewModel:LoadCharacters" , characters.size()+"");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                onResponseError(e);
            }

            @Override
            public void onComplete() {

            }
        };

        repository.loadCharacters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(databaseDisposableObserver);
    }

        public void fetchCharacters(int offset){
        networkDisposableObserver = new DisposableObserver<CharacterDataWrapper>() {

            @Override
            public void onNext(@NonNull CharacterDataWrapper characters) {
                Log.d("CharacterViewModel:FetchCharacters" , "character");
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {

            }
        };

        repository.loadCharactersFromApi(new CharacterPage(20,offset))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(networkDisposableObserver);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        databaseSearcherDisposableObserver.dispose();
        databaseDisposableObserver.dispose();
        networkDisposableObserver.dispose();
    }

    public void resetValuesLiveData() {
        charactersResult.postValue(null);
        characterLoading.postValue(null);
        swipeRefreshLayoutLiveData.postValue(null);
    }


    private void onResponseSuccess(List<Character> characters) {
        characterLoading.postValue(false);
        charactersResult.postValue(characters);
        swipeRefreshLayoutLiveData.postValue(true);
    }

    private void onResponseError(Throwable e){
        characterLoading.postValue(false);
        charactersError.postValue(e.getMessage());
        swipeRefreshLayoutLiveData.postValue(false);
    }

    public MutableLiveData<List<Character>> getCharactersResult() {
        return charactersResult;
    }

    public MutableLiveData<String> getCharactersError() {
        return charactersError;
    }

    public MutableLiveData<Boolean> getCharacterLoading() {
        return characterLoading;
    }

    public MutableLiveData<Boolean> getSwipeRefreshLayoutLiveData() {
        return swipeRefreshLayoutLiveData;
    }

    public MutableLiveData<List<Character>> getSearchCharacterFromDb() {
        return searchCharacterFromDb;
    }

    public MutableLiveData<String> getErrorSearchCharacterFromDb() {
        return errorSearchCharacterFromDb;
    }
}