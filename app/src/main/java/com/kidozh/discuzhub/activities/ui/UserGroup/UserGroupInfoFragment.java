package com.kidozh.discuzhub.activities.ui.UserGroup;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.databinding.UserGroupInfoFragmentBinding;
import com.kidozh.discuzhub.results.UserProfileResult;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.MyTagHandler;

import okhttp3.HttpUrl;

public class UserGroupInfoFragment extends Fragment {

    private UserGroupInfoViewModel mViewModel;
    private UserProfileResult.GroupVariables groupVariables;
    private UserProfileResult.AdminGroupVariables adminGroupVariables;
    UserGroupInfoFragmentBinding binding;

    public UserGroupInfoFragment(UserProfileResult.GroupVariables groupVariables, UserProfileResult.AdminGroupVariables adminGroupVariables) {
        this.groupVariables = groupVariables;
        this.adminGroupVariables = adminGroupVariables;
    }

    public static UserGroupInfoFragment newInstance(UserProfileResult.GroupVariables groupVariables,
                                                    UserProfileResult.AdminGroupVariables adminGroupVariables) {
        return new UserGroupInfoFragment(groupVariables,adminGroupVariables);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = UserGroupInfoFragmentBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UserGroupInfoViewModel.class);
        // TODO: Use the ViewModel
        mViewModel.setAdminGroupVariables(adminGroupVariables);
        mViewModel.setGroupVariables(groupVariables);
        bindViewModel();
    }

    public void setCheckableImageview(ImageView imageview, boolean ok){
        if(ok){
            imageview.setImageDrawable(getContext().getDrawable(R.drawable.ic_profile_item_enable_24px));
        }
        else {
            imageview.setImageDrawable(getContext().getDrawable(R.drawable.ic_profile_item_disable_24px));
        }
    }
    
    public void bindViewModel(){
        mViewModel.adminGroupVariablesMutableLiveData.observe(getViewLifecycleOwner(), new Observer<UserProfileResult.AdminGroupVariables>() {
            @Override
            public void onChanged(UserProfileResult.AdminGroupVariables adminGroupVariables) {
                if(mViewModel.adminGroupVariablesMutableLiveData.getValue() == null && mViewModel.groupVariablesMutableLiveData.getValue() == null){
                    binding.userGroupEmptyView.setVisibility(View.VISIBLE);
                }
                else {
                    binding.userGroupEmptyView.setVisibility(View.GONE);
                }

                if(adminGroupVariables == null || adminGroupVariables.groupTitle.length() == 0){
                    binding.userGroupAdminInfo.setVisibility(View.GONE);
                }
                else {
                    if(adminGroupVariables.color!=null && adminGroupVariables.color.length()!=0){
                        // set background
                        //binding.userGroupAdminInfo.setAlpha(0.5f);
                        binding.userGroupAdminInfo.setCardBackgroundColor(Color.parseColor(adminGroupVariables.color));
                        //binding.userGroupAdminInfo.getBackground().setAlpha(140);
                    }
                    binding.userGroupAdminInfo.setVisibility(View.VISIBLE);
                    String userGroupAdminTitleString = adminGroupVariables.groupTitle.replaceAll("<.*?>","");

                    binding.userGroupAdminTitle.setText(Html.fromHtml(userGroupAdminTitleString), TextView.BufferType.SPANNABLE);
                    binding.userGroupAdminStar.setText(String.valueOf(adminGroupVariables.stars));
                    binding.userAdminGroupReadaccess.setText(String.valueOf(adminGroupVariables.readAccess));
                    setCheckableImageview(binding.userAdminGroupAllowAttachIcon,adminGroupVariables.allowGetAttach);
                    setCheckableImageview(binding.userAdminGroupAllowImageIcon,adminGroupVariables.allowGetImage);
                    setCheckableImageview(binding.userAdminGroupBeginCodeIcon,adminGroupVariables.allowBeginCode);
                    setCheckableImageview(binding.userAdminGroupAllowMediaCodeIcon,adminGroupVariables.allowMediaCode);
                    binding.userAdminGroupMaxSignatureSizeTextview.setText(getString(R.string.group_max_signature,adminGroupVariables.maxSignatureSize));
                    MyTagHandler myTagHandler = new MyTagHandler(getContext(),binding.userAdminGroupAdminIcon,binding.userAdminGroupAdminIcon);
                    MyImageGetter myImageGetter = new MyImageGetter(getContext(),binding.userAdminGroupAdminIcon,binding.userAdminGroupAdminIcon,true);
                    Spanned sp = Html.fromHtml(adminGroupVariables.icon,myImageGetter,myTagHandler);
                    SpannableString spannableString = new SpannableString(sp);
                    binding.userAdminGroupAdminIcon.setText(spannableString, TextView.BufferType.SPANNABLE);
                }
            }
        });

        mViewModel.groupVariablesMutableLiveData.observe(getViewLifecycleOwner(), new Observer<UserProfileResult.GroupVariables>() {
            @Override
            public void onChanged(UserProfileResult.GroupVariables groupVariables) {
                if(mViewModel.adminGroupVariablesMutableLiveData.getValue() == null && mViewModel.groupVariablesMutableLiveData.getValue() == null){
                    binding.userGroupEmptyView.setVisibility(View.VISIBLE);
                }
                else {
                    binding.userGroupEmptyView.setVisibility(View.GONE);
                }

                if(groupVariables == null){
                    binding.userGroupInfo.setVisibility(View.GONE);
                }
                else {
                    if(groupVariables.color!=null &&groupVariables.color.length()!=0){
                        // set background
                        binding.userGroupInfo.setCardBackgroundColor(Color.parseColor(groupVariables.color));
                        //binding.userGroupInfo.getBackground().setAlpha(140);
                        //binding.userGroupInfo.setBackgroundColor(Color.parseColor(groupVariables.color));
                    }
                    binding.userGroupInfo.setVisibility(View.VISIBLE);
                    if(groupVariables.groupTitle == null){
                        return;
                    }
                    String title = groupVariables.groupTitle.replaceAll("<.*?>","");
                    binding.userGroupTitle.setText(Html.fromHtml(title), TextView.BufferType.SPANNABLE);
                    binding.userGroupReadaccess.setText(String.valueOf(groupVariables.readAccess));
                    binding.userGroupStar.setText(String.valueOf(groupVariables.stars));
                    setCheckableImageview(binding.userGroupAllowAttachIcon,groupVariables.allowGetAttach);
                    setCheckableImageview(binding.userGroupAllowImageIcon,groupVariables.allowGetImage);
                    setCheckableImageview(binding.userGroupBeginCodeIcon,groupVariables.allowBeginCode);
                    setCheckableImageview(binding.userGroupAllowMediaCodeIcon,groupVariables.allowMediaCode);
                    binding.userGroupMaxSignatureSizeTextview.setText(getString(R.string.group_max_signature,groupVariables.maxSignatureSize));
                    MyTagHandler myTagHandler = new MyTagHandler(getContext(),binding.userGroupIcon,binding.userGroupIcon);
                    MyImageGetter myImageGetter = new MyImageGetter(getContext(),binding.userGroupIcon,binding.userGroupIcon,true);
                    Spanned sp = Html.fromHtml(groupVariables.icon,myImageGetter,myTagHandler);
                    SpannableString spannableString = new SpannableString(sp);
                    binding.userGroupIcon.setText(spannableString, TextView.BufferType.SPANNABLE);
                }
            }
        });
    }

}
