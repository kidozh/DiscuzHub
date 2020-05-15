package com.kidozh.discuzhub.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class AddBBSViewModel extends AndroidViewModel {
    private final static String TAG = AddBBSViewModel.class.getSimpleName();

    public MutableLiveData<String> currentURLLiveData, errorTextLiveData;
    public MutableLiveData<Boolean> useSafeClientLiveData, isLoadingLiveData;

    public AddBBSViewModel(@NonNull Application application) {
        super(application);
        currentURLLiveData = new MutableLiveData<>("");
        useSafeClientLiveData = new MutableLiveData<>(true);
        isLoadingLiveData = new MutableLiveData<>(false);
        errorTextLiveData = new MutableLiveData<>("");
    }
}
