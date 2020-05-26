package com.kidozh.discuzhub.activities.ui.UserGroup;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.results.UserProfileResult;
import com.kidozh.discuzhub.utilities.MyImageGetter;
import com.kidozh.discuzhub.utilities.MyTagHandler;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserGroupInfoFragment extends Fragment {

    private UserGroupInfoViewModel mViewModel;
    private UserProfileResult.GroupVariables groupVariables;
    private UserProfileResult.AdminGroupVariables adminGroupVariables;

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
        View view = inflater.inflate(R.layout.user_group_info_fragment, container, false);
        ButterKnife.bind(this,view);
        return view;
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

    @BindView(R.id.user_group_admin_info)
    CardView adminGroupCardview;
    @BindView(R.id.user_group_admin_title)
    TextView adminGroupTitle;
    @BindView(R.id.user_group_admin_star)
    TextView adminGroupStarTextview;
    @BindView(R.id.user_admin_group_admin_icon)
    TextView adminGroupIconTextview;
    @BindView(R.id.user_admin_group_readaccess)
    TextView adminGroupReadAccess;
    @BindView(R.id.user_admin_group_allow_attach_icon)
    ImageView adminGroupAllowAttachment;
    @BindView(R.id.user_admin_group_allow_image_icon)
    ImageView adminGroupAllowImage;
    @BindView(R.id.user_admin_group_allow_media_code_icon)
    ImageView adminGroupMediaCode;
    @BindView(R.id.user_admin_group_max_signature_size_textview)
    TextView adminGroupMaxSignatureSizeTextview;
    @BindView(R.id.user_admin_group_begin_code_icon)
    ImageView adminGroupBeginCode;

    @BindView(R.id.user_group_info)
    CardView groupCardview;
    @BindView(R.id.user_group_title)
    TextView groupTitle;
    @BindView(R.id.user_group_star)
    TextView groupStarTextview;
    @BindView(R.id.user_group_icon)
    TextView groupIconTextview;
    @BindView(R.id.user_group_readaccess)
    TextView groupReadAccess;
    @BindView(R.id.user_group_allow_attach_icon)
    ImageView groupAllowAttachment;
    @BindView(R.id.user_group_allow_image_icon)
    ImageView groupAllowImage;
    @BindView(R.id.user_group_allow_media_code_icon)
    ImageView groupMediaCode;
    @BindView(R.id.user_group_max_signature_size_textview)
    TextView groupMaxSignatureSizeTextview;
    @BindView(R.id.user_group_begin_code_icon)
    ImageView groupBeginCode;

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
                if(adminGroupVariables == null || adminGroupVariables.groupTitle.length() == 0){
                    adminGroupCardview.setVisibility(View.GONE);
                }
                else {
                    adminGroupCardview.setVisibility(View.VISIBLE);
                    adminGroupTitle.setText(adminGroupVariables.groupTitle);
                    adminGroupStarTextview.setText(String.valueOf(adminGroupVariables.stars));
                    adminGroupReadAccess.setText(String.valueOf(adminGroupVariables.readAccess));
                    setCheckableImageview(adminGroupAllowAttachment,adminGroupVariables.allowGetAttach);
                    setCheckableImageview(adminGroupAllowImage,adminGroupVariables.allowGetImage);
                    setCheckableImageview(adminGroupBeginCode,adminGroupVariables.allowBeginCode);
                    setCheckableImageview(adminGroupMediaCode,adminGroupVariables.allowMediaCode);
                    adminGroupMaxSignatureSizeTextview.setText(getString(R.string.group_max_signature,adminGroupVariables.maxSignatureSize));
                    MyTagHandler myTagHandler = new MyTagHandler(getContext(),adminGroupIconTextview,adminGroupIconTextview);
                    MyImageGetter myImageGetter = new MyImageGetter(getContext(),adminGroupIconTextview,adminGroupIconTextview,true);
                    Spanned sp = Html.fromHtml(adminGroupVariables.icon,myImageGetter,myTagHandler);
                    SpannableString spannableString = new SpannableString(sp);
                    adminGroupIconTextview.setText(spannableString, TextView.BufferType.SPANNABLE);
                }
            }
        });

        mViewModel.groupVariablesMutableLiveData.observe(getViewLifecycleOwner(), new Observer<UserProfileResult.GroupVariables>() {
            @Override
            public void onChanged(UserProfileResult.GroupVariables groupVariables) {
                if(groupVariables == null){
                    groupCardview.setVisibility(View.GONE);
                }
                else {
                    groupCardview.setVisibility(View.VISIBLE);
                    groupTitle.setText(groupVariables.groupTitle);
                    groupReadAccess.setText(String.valueOf(groupVariables.readAccess));
                    groupStarTextview.setText(String.valueOf(groupVariables.stars));
                    setCheckableImageview(groupAllowAttachment,groupVariables.allowGetAttach);
                    setCheckableImageview(groupAllowImage,groupVariables.allowGetImage);
                    setCheckableImageview(groupBeginCode,groupVariables.allowBeginCode);
                    setCheckableImageview(groupMediaCode,groupVariables.allowMediaCode);
                    groupMaxSignatureSizeTextview.setText(getString(R.string.group_max_signature,groupVariables.maxSignatureSize));
                    MyTagHandler myTagHandler = new MyTagHandler(getContext(),groupIconTextview,groupIconTextview);
                    MyImageGetter myImageGetter = new MyImageGetter(getContext(),groupIconTextview,groupIconTextview,true);
                    Spanned sp = Html.fromHtml(groupVariables.icon,myImageGetter,myTagHandler);
                    SpannableString spannableString = new SpannableString(sp);
                    groupIconTextview.setText(spannableString, TextView.BufferType.SPANNABLE);
                }
            }
        });
    }

}
