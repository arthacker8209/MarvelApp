package com.example.marvelapp.ui.characters;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.marvelapp.R;
import com.example.marvelapp.data.model.Character;
import com.example.marvelapp.data.model.Comics;
import com.example.marvelapp.databinding.FragmentCharacterBinding;
import com.example.marvelapp.ui.characterdetails.CharacterDetailsFragment;
import com.example.marvelapp.ui.listeners.OnBackPressed;
import com.example.marvelapp.ui.listeners.OnClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Observable;

@AndroidEntryPoint
public class CharactersFragment extends Fragment implements OnClickListener, OnBackPressed {

    private CharactersViewModel viewModel;
    private RecyclerView charactersRecyclerView;
    private CharacterAdapter charactersAdapter;
    private FragmentCharacterBinding binding;
    private int offset = 20;
    private boolean loading = true;
    private boolean checking0, checking1, checking2;
    private final String STATE_OFFSET = "Current Offset";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(CharactersViewModel.class);
        binding = FragmentCharacterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadMoreOnRecyclerView();
        pullToRefresh();
        setupCharacterRecyclerView();
        updateDataForUI();
        updateProgressBarForUI();
        updateSwipeRefreshLayoutForUI();
        handleOnBackPressed();
        viewModel.fetchCharacters(offset);
        searchCharacter();
        observeSearchResult();


    }

    private void searchCharacter() {
        binding.etxtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    viewModel.searchCharacterFromDb(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void updateDataForUI(){
        viewModel.charactersListLiveData.observe(getViewLifecycleOwner(), new Observer<List<Character>>() {
            @Override
            public void onChanged(List<Character> characters) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            charactersAdapter.refreshCharacterList(characters);
                        }
                    }
                });
            }
        });
    }

    private void updateProgressBarForUI() {
        viewModel.progressBarLiveData.observe(getViewLifecycleOwner(), aBoolean -> {
                   requireActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           if (checking0 && aBoolean != null) {
                               if (aBoolean) {
                                   binding.progressBar.setVisibility(View.VISIBLE);
                               } else {
                                   binding.progressBar.setVisibility(View.GONE);
                               }
                           } else {
                               checking0 = true;
                           }
                       }
                   });
            });
    }

    private void updateSwipeRefreshLayoutForUI() {
        viewModel._swipeRefreshLayoutLiveData.observe(getViewLifecycleOwner(), aBoolean -> {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (aBoolean != null) {
                            if (checking1 && aBoolean) {
                                loading = true;
                                binding.swipeRefreshLayout.setRefreshing(false);
                            } else {
                                checking1 = true;
                            }
                        }
                    }
                });
            });
    }


    private void loadMoreOnRecyclerView() {
        binding.characterRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // dy > 0 = check for scroll down
                    if (!recyclerView.canScrollVertically(1)) {
                        offset += 20;
                        viewModel.fetchCharacters(offset);
                        Log.d("RECYCLERVIEW", "FETCHING");
                    }
                }
            }
        });
        updateDataForUI();
    }

    private void pullToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            offset= 0;
            viewModel.fetchCharacters(offset); // the loadMoreOnRecyclerView method stored the value of the offset from last time scrolled
        });
    }

    void setupCharacterRecyclerView(){
        charactersRecyclerView = binding.characterRecyclerView;
        charactersRecyclerView.setHasFixedSize(true);
        charactersAdapter = new CharacterAdapter(getActivity(),this, new CharacterAdapter.CharacterDiff());
        charactersRecyclerView.setAdapter(charactersAdapter);
        charactersRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

    }

    private void observeSearchResult(){
        viewModel.searchCharacterFromDbLiveData.observe(getViewLifecycleOwner(), new Observer<List<Character>>() {
            @Override
            public void onChanged(List<Character> characters) {
                charactersAdapter.submitList(characters);
            }
        });
    }

    @Override
    public void onCharacterClicked(Character character) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("character",character);
            Fragment fragment = new CharacterDetailsFragment();
            fragment.setArguments(bundle);
            FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();

                ft.setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    );
            ft.replace(R.id.fragmentContainerView, fragment);
            ft.setReorderingAllowed(true);
            ft.addToBackStack("CharacterDetails");
            ft.commit();
        }


    @Override
    public void handleOnBackPressed() {
            requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    viewModel.resetValuesLiveData();
                    requireActivity().onBackPressed();
                }
            });
        }
}