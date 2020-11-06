package com.kidozh.discuzhub.activities.ui.UserMedal;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.MedalAdapter;
import com.kidozh.discuzhub.databinding.FragmentMedalListBinding;
import com.kidozh.discuzhub.results.UserProfileResult;

import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MedalFragment extends Fragment {

    private static final String TAG = MedalFragment.class.getSimpleName();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    MedalAdapter adapter = new MedalAdapter();
    List<UserProfileResult.Medal> medalList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MedalFragment() {
    }

    // TODO: Customize parameter initialization

    public static MedalFragment newInstance(List<UserProfileResult.Medal> medalList) {
        MedalFragment fragment = new MedalFragment();
        fragment.medalList = medalList;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }


    
    FragmentMedalListBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMedalListBinding.inflate(inflater,container,false);

        // Set the adapter
        binding.medalRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.medalRecyclerview.setAdapter(adapter);
        adapter.setMedalList(medalList);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // detect if no medal found
        if(medalList == null || medalList.size() == 0){
            binding.medalEmptyView.setVisibility(View.VISIBLE);
            binding.medalEmptyTextview.setText(getString(R.string.profile_no_medal));
            Log.d(TAG,"Get No medal "+medalList);
        }
        else {
            binding.medalEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction();
    }
}
