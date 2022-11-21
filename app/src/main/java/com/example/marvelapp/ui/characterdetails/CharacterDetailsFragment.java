package com.example.marvelapp.ui.characterdetails;

import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.marvelapp.data.model.Character;
import com.example.marvelapp.data.model.Comics;
import com.example.marvelapp.databinding.FragmentCharacterDetailsBinding;
import com.example.marvelapp.ui.listeners.OnBackPressed;
import com.example.marvelapp.ui.listeners.OnClickListener;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CharacterDetailsFragment extends Fragment implements OnClickListener, OnBackPressed {

    private CharacterDetailsViewModel mViewModel;
    private FragmentCharacterDetailsBinding binding;
    private RecyclerView comicsRecyclerView;
    private ComicsAdapter comicsAdapter;
    private boolean loading = true;
    private boolean checking0, checking1, checking2;
    private Bundle bundle;
    private Character character;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(CharacterDetailsViewModel.class);
        binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bundle=this.getArguments();
        if(bundle!=null){
            character = bundle.getParcelable("character");
        }
        addCharacterImage();
        addCharacterName();
        addCharacterDesc();
        updateProgressBarForUI();
        mViewModel.loadComics(character);
        setupCharacterRecyclerView();
        observeComicsData();

    }

    private void addCharacterDesc(){
        binding.characterDescription.setText(character.getDescription());
    }

    private void addCharacterName(){
        binding.characterName.setText(character.getName());
    }

    private void addCharacterImage(){
        String imageURl = character.getThumbnail().getPath()+"."+character.getThumbnail().getExtension();
        Glide.with(this).load(imageURl).into(binding.characterImg);
    }

    void setupCharacterRecyclerView(){
        comicsRecyclerView = binding.comicsRecyclerView;
        comicsRecyclerView.setHasFixedSize(true);
        comicsAdapter = new ComicsAdapter(requireContext(), new ComicsAdapter.ComicsDiff());
        comicsRecyclerView.setAdapter(comicsAdapter);
        comicsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false));

    }

    private void updateProgressBarForUI() {
        mViewModel.progressBarLiveData.observe(getViewLifecycleOwner(), aBoolean -> {
            if (checking0 && aBoolean != null) {
                if (aBoolean) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            } else {
                checking0 = true;
            }
        });
    }

    private void observeComicsData(){
            mViewModel.comicsListLiveData.observe(getViewLifecycleOwner(), new Observer<List<Comics>>() {
                @Override
                public void onChanged(List<Comics> comics) {
                    if (isAdded()) {
                        comicsAdapter.refreshComicsList(comics);
                    }
                }
            });
    }

    @Override
    public void handleOnBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(!mViewModel.databaseDisposableObserver.isDisposed()){
                    mViewModel.disposeElements();
                }
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!mViewModel.databaseDisposableObserver.isDisposed()){
            mViewModel.disposeElements();
        }
    }

    @Override
    public void onCharacterClicked(Character character) {

    }
}