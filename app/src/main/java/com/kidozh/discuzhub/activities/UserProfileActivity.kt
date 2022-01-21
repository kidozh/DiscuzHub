package com.kidozh.discuzhub.activities


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.kidozh.discuzhub.BuildConfig
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ui.UserFriend.UserFriendFragment
import com.kidozh.discuzhub.activities.ui.UserGroup.UserGroupInfoFragment
import com.kidozh.discuzhub.activities.ui.UserMedal.MedalFragment
import com.kidozh.discuzhub.activities.ui.UserProfileList.UserProfileInfoListFragment
import com.kidozh.discuzhub.daos.ViewHistoryDao
import com.kidozh.discuzhub.database.ViewHistoryDatabase.Companion.getInstance
import com.kidozh.discuzhub.databinding.ActivityShowPersonalInfoBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.entities.UserProfileItem
import com.kidozh.discuzhub.entities.ViewHistory
import com.kidozh.discuzhub.results.UserProfileResult
import com.kidozh.discuzhub.utilities.*
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod.Companion.onLinkClicked
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod.OnLinkClickedListener
import com.kidozh.discuzhub.utilities.bbsParseUtils.privateMessage
import com.kidozh.discuzhub.viewModels.UserProfileViewModel
import java.io.InputStream
import java.text.DateFormat
import java.util.*

class UserProfileActivity : BaseStatusActivity(), UserFriendFragment.OnFragmentInteractionListener,
    OnLinkClickedListener {
    private var userId = 0
    var username: String? = null
    private var viewModel: UserProfileViewModel? = null
    var adapter: personalInfoViewPagerAdapter? = null
    lateinit var binding: ActivityShowPersonalInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowPersonalInfoBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        intentInfo
        configureActionBar()
        renderFollowAndPMBtn()
        bindViewModel()
        renderUserInfo()
        configurePMBtn()
        configureViewPager()
    }

    private fun renderUserInfo() {
        // making it circle
        val factory = OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(this))
        Glide.get(this).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
        var avatar_num = userId % 16
        if (avatar_num < 0) {
            avatar_num = -avatar_num
        }
        val avatarResource = resources.getIdentifier(
            String.format("avatar_%s", avatar_num + 1),
            "drawable",
            packageName
        )
        Glide.with(this)
            .load(URLUtils.getDefaultAvatarUrlByUid(userId))
            .error(avatarResource)
            .placeholder(avatarResource)
            .centerInside()
            .into(binding.showPersonalInfoAvatar)
    }

    private fun renderFollowAndPMBtn() {
        if (user == null) {
            binding.showPersonalInfoMessageBtn.visibility = View.GONE
            binding.showPersonalInfoFocusBtn.visibility = View.GONE
        }
    }

    private fun configurePMBtn() {
        if (BuildConfig.BUILD_TYPE.contentEquals("chinaEdition")) {
            binding.showPersonalInfoMessageBtn.visibility = View.GONE
        }
        binding.showPersonalInfoMessageBtn.setOnClickListener {
            val privateM = privateMessage(
                userId,
                false, "", userId, 1, 1,
                username, "", username, ""
            )
            val intent = Intent(applicationContext, PrivateMessageActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, discuz)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra(ConstUtils.PASS_PRIVATE_MESSAGE_KEY, privateM)
            startActivity(intent)
        }
    }

    fun configureActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = username
        supportActionBar!!.subtitle = userId.toString()
    }

    private val intentInfo: Unit
        get() {
            val intent = intent
            discuz = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz?
            user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
            user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
            userId = intent.getIntExtra("UID", 0)
            if (discuz == null) {
                finishAfterTransition()
            } else {
                Log.d(TAG, "get bbs name " + discuz!!.site_name)
                URLUtils.setBBS(discuz)
                viewModel!!.setBBSInfo(discuz, user, userId)
            }
            if (supportActionBar != null) {
                supportActionBar!!.title = discuz!!.site_name
            }
            client = NetworkUtils.getPreferredClientWithCookieJarByUser(this, user)
            if (user != null && userId == user!!.uid) {
                binding.showPersonalInfoFocusBtn.visibility = View.GONE
                binding.showPersonalInfoMessageBtn.visibility = View.GONE
            }
        }

    private fun bindViewModel() {
        viewModel!!.userProfileResultLiveData.observe(
            this,
            { userProfileResult: UserProfileResult? ->
                Log.d(TAG, "User profile result $userProfileResult")
                if (userProfileResult?.userProfileVariableResult != null && userProfileResult.userProfileVariableResult.space != null) {
                    val spaceVariables = userProfileResult.userProfileVariableResult.space
                    val username = userProfileResult.userProfileVariableResult.space.username
                    if (supportActionBar != null) {
                        supportActionBar!!.subtitle = username
                    }
                    setBaseResult(userProfileResult, userProfileResult.userProfileVariableResult)


                    // for avatar rendering
                    val factory =
                        OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(application))
                    Glide.get(applicationContext).registry.replace(
                        GlideUrl::class.java, InputStream::class.java, factory
                    )
                    val uid = userProfileResult.userProfileVariableResult.space.uid
                    var avatar_num = uid % 16
                    if (avatar_num < 0) {
                        avatar_num = -avatar_num
                    }
                    val avatarResource = resources.getIdentifier(
                        String.format("avatar_%s", avatar_num + 1),
                        "drawable",
                        packageName
                    )
                    Glide.with(application)
                        .load(URLUtils.getDefaultAvatarUrlByUid(uid))
                        .error(avatarResource)
                        .placeholder(avatarResource)
                        .centerInside()
                        .into(binding.showPersonalInfoAvatar)
                    //check with verified status
                    if (spaceVariables.emailStatus) {
                        binding.userVerifiedIcon.visibility = View.VISIBLE
                    } else {
                        binding.userVerifiedIcon.visibility = View.GONE
                    }

                    // signature
                    val sigHtml = userProfileResult.userProfileVariableResult.space.sigatureHtml
                    Log.d(TAG, "Signature html $sigHtml")
                    val myTagHandler = MyTagHandler(
                        application,
                        binding.userSignatureTextview,
                        binding.userSignatureTextview
                    )
                    val myImageGetter = MyImageGetter(
                        application,
                        binding.userSignatureTextview,
                        binding.userSignatureTextview,
                        true
                    )
                    val sp = Html.fromHtml(sigHtml, HtmlCompat.FROM_HTML_MODE_COMPACT, myImageGetter, myTagHandler)
                    val spannableString = SpannableString(sp)
                    binding.userSignatureTextview.setText(
                        spannableString,
                        TextView.BufferType.SPANNABLE
                    )
                    binding.userSignatureTextview.movementMethod =
                        bbsLinkMovementMethod(this@UserProfileActivity)
                    if (userProfileResult.userProfileVariableResult.space.bio.isNotEmpty()) {
                        binding.userBioTextview.text =
                            userProfileResult.userProfileVariableResult.space.bio
                    } else {
                        binding.userBioTextview.visibility = View.GONE
                    }
                    if (userProfileResult.userProfileVariableResult.space.interest.isNotEmpty()) {
                        binding.showPersonalInfoInterestTextView.visibility = View.VISIBLE
                        binding.showPersonalInfoInterestTextView.text =
                            userProfileResult.userProfileVariableResult.space.interest
                    } else {
                        binding.showPersonalInfoInterestTextView.visibility = View.GONE
                        binding.showPersonalInfoInterestTextView.text =
                            userProfileResult.userProfileVariableResult.space.interest
                    }
                    var birthPlace =
                        userProfileResult.userProfileVariableResult.space.birthprovince +
                                userProfileResult.userProfileVariableResult.space.birthcity +
                                userProfileResult.userProfileVariableResult.space.birthdist +
                                userProfileResult.userProfileVariableResult.space.birthcommunity
                    if (birthPlace.isNotEmpty()) {
                        if (userProfileResult.userProfileVariableResult.space.birthdist.contains("汉川")) {
                            // to reflect actual name
                            birthPlace =
                                userProfileResult.userProfileVariableResult.space.birthprovince +
                                        userProfileResult.userProfileVariableResult.space.birthdist +
                                        userProfileResult.userProfileVariableResult.space.birthcommunity
                        }
                        binding.showPersonalInfoBirthplaceTextView.visibility = View.VISIBLE
                        binding.showPersonalInfoBirthplaceTextView.text = birthPlace
                    } else {
                        binding.showPersonalInfoBirthplaceTextView.visibility = View.GONE
                    }
                    binding.showPersonalInfoRegdateTextView.text =
                        userProfileResult.userProfileVariableResult.space.regdate
                    binding.showPersonalInfoLastActivityTime.text =
                        userProfileResult.userProfileVariableResult.space.lastactivity
                    binding.showPersonalInfoRecentNoteTextView.text =
                        userProfileResult.userProfileVariableResult.space.recentNote
                    if (userProfileResult.userProfileVariableResult.space.group != null && userProfileResult.userProfileVariableResult.space.group.groupTitle != null) {
                        binding.showPersonalInfoGroupInfo.setText(
                            Html.fromHtml(userProfileResult.userProfileVariableResult.space.group.groupTitle,HtmlCompat.FROM_HTML_MODE_COMPACT),
                            TextView.BufferType.SPANNABLE
                        )
                    } else {
                        binding.showPersonalInfoGroupInfo.visibility = View.GONE
                    }
                    // for detailed information
                    val prefs = PreferenceManager.getDefaultSharedPreferences(
                        applicationContext
                    )
                    val recordHistory =
                        prefs.getBoolean(getString(R.string.preference_key_record_history), false)
                    if (recordHistory && discuz != null) {
                        insertViewHistory(
                            ViewHistory(
                                URLUtils.getDefaultAvatarUrlByUid(uid),
                                username,
                                discuz!!.id,
                                userProfileResult.userProfileVariableResult.space.sigatureHtml,
                                ViewHistory.VIEW_TYPE_USER_PROFILE,
                                uid,
                                0,
                                Date()
                            )
                        )
                    }
                }
                binding.showPersonalInfoViewpager.invalidate()
                adapter!!.notifyDataSetChanged()
            })
        viewModel!!.isLoading.observe(this, { aBoolean ->
            if (aBoolean) {
                binding.showPersonalInfoProgressbar.visibility = View.VISIBLE
            } else {
                binding.showPersonalInfoProgressbar.visibility = View.GONE
            }
        })
    }

    private fun generateUserProfileItem(
        title: String,
        content: String?,
        iconId: Int,
        privateStatus: Int
    ): UserProfileItem {
        return if (content == null || content.length != 0) {
            UserProfileItem(title, content, iconId)
        } else {
            when (privateStatus) {
                0 -> {
                    UserProfileItem(title, getString(R.string.user_profile_item_not_set), iconId)
                }
                1 -> {
                    UserProfileItem(
                        title,
                        getString(R.string.user_profile_item_only_visible_to_friend),
                        R.drawable.ic_profile_private_item_24px
                    )
                }
                3 -> {
                    UserProfileItem(
                        title,
                        getString(R.string.user_profile_privacy_hidden),
                        R.drawable.ic_profile_private_item_24px
                    )
                }
                else -> {
                    UserProfileItem(
                        title,
                        getString(R.string.user_profile_item_not_set),
                        R.drawable.ic_profile_private_item_24px
                    )
                }
            }
        }
    }

    private fun generateGenderUserProfileItem(
        genderStatus: Int,
        privateStatus: Int
    ): UserProfileItem {
        return if (privateStatus == 2) {
            generateUserProfileItem(
                getString(R.string.gender),
                "",
                R.drawable.ic_profile_private_item_24px,
                privateStatus
            )
        } else {
            when (genderStatus) {
                0 -> {
                    generateUserProfileItem(
                        getString(R.string.gender),
                        getString(R.string.gender_secret),
                        R.drawable.ic_secret_24px,
                        privateStatus
                    )
                }
                1 -> {
                    generateUserProfileItem(
                        getString(R.string.gender),
                        getString(R.string.gender_male),
                        R.drawable.ic_male_24px,
                        privateStatus
                    )
                }
                2 -> {
                    generateUserProfileItem(
                        getString(R.string.gender),
                        getString(R.string.gender_female),
                        R.drawable.ic_female_24px,
                        privateStatus
                    )
                }
                else -> {
                    generateUserProfileItem(
                        getString(R.string.gender),
                        getString(R.string.item_parse_failed),
                        R.drawable.ic_error_outline_24px,
                        privateStatus
                    )
                }
            }
        }
    }// construct the date
    // birthplace

    // resident place
    // gender
    private val basicInfoList:
    // birthday
            List<UserProfileItem>
        private get() {
            val userProfileResult = viewModel!!.userProfileResultLiveData.value
                ?: return ArrayList()
            val userProfileItemList: MutableList<UserProfileItem> = ArrayList()
            val spaceVariables = userProfileResult.userProfileVariableResult.space
            val privacySetting = spaceVariables.privacySetting
            // gender
            val genderPrivate = privacySetting.profilePrivacySetting.gender
            Log.d(TAG, "Gender int " + spaceVariables.gender)
            userProfileItemList.add(
                generateGenderUserProfileItem(
                    spaceVariables.gender,
                    genderPrivate
                )
            )
            // birthday
            val birthPrivate = privacySetting.profilePrivacySetting.birthday
            val birthYear = spaceVariables.birthyear
            val birthMonth = spaceVariables.birthmonth
            val birthDay = spaceVariables.birthday
            if (birthYear == 0 || birthMonth == 0 || birthDay == 0) {
                userProfileItemList.add(
                    generateUserProfileItem(
                        getString(R.string.birthday),
                        "", R.drawable.ic_cake_outlined_24px,
                        birthPrivate
                    )
                )
            } else {
                // construct the date
                val df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                val birthCalendar = Calendar.getInstance()
                birthCalendar[birthYear, birthMonth] = birthDay
                val birthDate = birthCalendar.time
                userProfileItemList.add(
                    generateUserProfileItem(
                        getString(R.string.birthday),
                        df.format(birthDate),
                        R.drawable.ic_cake_outlined_24px,
                        birthPrivate
                    )
                )
                userProfileItemList.add(
                    generateUserProfileItem(
                        getString(R.string.constellation),
                        spaceVariables.constellation,
                        R.drawable.ic_constellation_24px,
                        birthPrivate
                    )
                )
            }
            // birthplace
            val birthPlacePrivate = privacySetting.profilePrivacySetting.birthcity
            val birthPlace =
                (spaceVariables.birthprovince + spaceVariables.birthcity + spaceVariables.birthdist
                        + spaceVariables.birthcommunity)
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.birthplace),
                    birthPlace, R.drawable.ic_child_care_24px,
                    birthPlacePrivate
                )
            )

            // resident place
            val residentPlacePrivate = privacySetting.profilePrivacySetting.residecity
            val residentPlace = (spaceVariables.resideprovince + spaceVariables.residecity
                    + spaceVariables.residedist
                    + spaceVariables.residecommunity)
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.resident_location),
                    residentPlace, R.drawable.ic_location_city_24px,
                    residentPlacePrivate
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.married_status),
                    spaceVariables.marriedStatus, R.drawable.ic_marry_status_24px,
                    privacySetting.profilePrivacySetting.affectivestatus
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.profile_looking_for),
                    spaceVariables.lookingfor, R.drawable.ic_looking_for_friend_24px,
                    privacySetting.profilePrivacySetting.lookingfor
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.blood_type),
                    spaceVariables.bloodtype, R.drawable.ic_blood_type_24px,
                    privacySetting.profilePrivacySetting.bloodtype
                )
            )
            Log.d(TAG, "Blood type " + spaceVariables.bloodtype)
            return userProfileItemList
        }
    private val eduOccupationInfoList: List<UserProfileItem>
        get() {
            val userProfileResult = viewModel!!.userProfileResultLiveData.value
                ?: return ArrayList()
            val userProfileItemList: MutableList<UserProfileItem> = ArrayList()
            val spaceVariables = userProfileResult.userProfileVariableResult.space
            val privacySetting = spaceVariables.privacySetting
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_deploma),
                    spaceVariables.education, R.drawable.ic_study_degree_24px,
                    privacySetting.profilePrivacySetting.education
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_graduate_from),
                    spaceVariables.graduateschool, R.drawable.ic_school_24px,
                    privacySetting.profilePrivacySetting.graduateschool
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_company),
                    spaceVariables.company, R.drawable.ic_company_24px,
                    privacySetting.profilePrivacySetting.company
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_occupation),
                    spaceVariables.occupation, R.drawable.ic_work_occupation_24px,
                    privacySetting.profilePrivacySetting.occupation
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_position),
                    spaceVariables.workPosition, R.drawable.ic_work_grade_24px,
                    privacySetting.profilePrivacySetting.position
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_revenue),
                    spaceVariables.revenue, R.drawable.ic_price_outlined_24px,
                    privacySetting.profilePrivacySetting.revenue
                )
            )
            return userProfileItemList
        }
    private val creditList: List<UserProfileItem>
        get() {
            val userProfileResult = viewModel!!.userProfileResultLiveData.value
            if (userProfileResult?.userProfileVariableResult == null) {
                return ArrayList()
            }
            val userProfileItemList: MutableList<UserProfileItem> = ArrayList()
            val spaceVariables = userProfileResult.userProfileVariableResult.space
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.bbs_credit), spaceVariables.credits.toString(),
                    R.drawable.ic_credit_24px,
                    0
                )
            )
            val privacySetting = spaceVariables.privacySetting
            val extCredits = userProfileResult.userProfileVariableResult.extendCredits
            for (i in extCredits.indices) {
                val extendCredit = extCredits[i]
                userProfileItemList.add(
                    generateUserProfileItem(
                        extendCredit.title, extendCredit.value.toString() + extendCredit.unit,
                        R.drawable.ic_extend_credit_24px,
                        0
                    )
                )
            }
            return userProfileItemList
        }
    private val extraInfoList: List<UserProfileItem>
        get() {
            val userProfileResult = viewModel!!.userProfileResultLiveData.value
                ?: return ArrayList()
            val userProfileItemList: MutableList<UserProfileItem> = ArrayList()
            val spaceVariables = userProfileResult.userProfileVariableResult.space
            val privacySetting = spaceVariables.privacySetting
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_homepage),
                    spaceVariables.site, R.drawable.ic_personal_site_24px,
                    privacySetting.profilePrivacySetting.site
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_interest),
                    spaceVariables.interest, R.drawable.ic_flag_24px,
                    privacySetting.profilePrivacySetting.interest
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_favorite_times),
                    spaceVariables.favtimes.toString(),
                    R.drawable.ic_favorite_24px,
                    0
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_share_times),
                    spaceVariables.sharetimes.toString(),
                    R.drawable.ic_share_outlined_24px,
                    0
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_last_visit),
                    spaceVariables.lastvisit, R.drawable.vector_drawable_clock,
                    0
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_last_post),
                    spaceVariables.lastpost, R.drawable.vector_drawable_clock,
                    0
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_last_activity),
                    spaceVariables.lastactivity, R.drawable.vector_drawable_clock,
                    0
                )
            )
            userProfileItemList.add(
                generateUserProfileItem(
                    getString(R.string.user_profile_last_send_mail),
                    spaceVariables.lastsendmail, R.drawable.ic_email_24px,
                    0
                )
            )
            return userProfileItemList
        }

    private fun configureViewPager() {
        Log.d(TAG, "Configuring friend fragment")

        binding.showPersonalInfoTabLayout.setupWithViewPager(binding.showPersonalInfoViewpager)
        adapter = personalInfoViewPagerAdapter(
            supportFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        binding.showPersonalInfoViewpager.adapter = adapter
    }

    override fun onFragmentInteraction(uri: Uri) {}
    override fun onRenderSuccessfully() {
        Log.d(TAG, "Redraw view pager")
        binding.showPersonalInfoViewpager.invalidate()
        binding.showPersonalInfoViewpager.requestLayout()
    }

    override fun onLinkClicked(url: String): Boolean {
        onLinkClicked(this, discuz!!, user, url)
        return true
    }

    inner class personalInfoViewPagerAdapter(fm: FragmentManager, behavior: Int) :
        FragmentStatePagerAdapter(fm, behavior) {
        override fun getItem(position: Int): Fragment {
            val userProfileResult = viewModel!!.userProfileResultLiveData.value
            when (position) {
                0 -> return if (userProfileResult?.userProfileVariableResult != null && userProfileResult.userProfileVariableResult.space != null) {
                    MedalFragment.newInstance(userProfileResult.userProfileVariableResult.space.medals)
                } else {
                    MedalFragment.newInstance(null)
                }
                1 -> return UserProfileInfoListFragment.newInstance(
                    getString(R.string.user_profile_extra_information),
                    creditList
                )
                2 -> return if (userProfileResult?.userProfileVariableResult != null && userProfileResult.userProfileVariableResult.space != null) {
                    UserFriendFragment.newInstance(
                        userId,
                        userProfileResult.userProfileVariableResult.space.friends
                    )
                } else {
                    UserFriendFragment.newInstance(userId, 0)
                }
                3 -> return UserProfileInfoListFragment.newInstance(
                    getString(R.string.user_profile_basic_information),
                    basicInfoList
                )
                4 -> return UserProfileInfoListFragment.newInstance(
                    getString(R.string.user_profile_edu_job),
                    eduOccupationInfoList
                )
                5 -> return UserProfileInfoListFragment.newInstance(
                    getString(R.string.user_profile_extra_information),
                    extraInfoList
                )
                6 -> return if (userProfileResult?.userProfileVariableResult != null && userProfileResult.userProfileVariableResult.space != null) {
                    UserGroupInfoFragment.newInstance(
                        userProfileResult.userProfileVariableResult.space.group,
                        userProfileResult.userProfileVariableResult.space.adminGroup
                    )
                } else {
                    UserGroupInfoFragment.newInstance(null, null)
                }
            }
            return UserFriendFragment.newInstance(userId, 0)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val userProfileResult = viewModel!!.userProfileResultLiveData.value
            return when (position) {
                0 -> if (userProfileResult?.userProfileVariableResult != null && userProfileResult.userProfileVariableResult.space != null && userProfileResult.userProfileVariableResult.space.medals != null
                ) {
                    getString(
                        R.string.bbs_medals_num,
                        userProfileResult.userProfileVariableResult.space.medals.size
                    )
                } else {
                    getString(R.string.user_profile_medal)
                }
                1 -> getString(R.string.bbs_credit)
                2 -> if (userProfileResult?.userProfileVariableResult != null && userProfileResult.userProfileVariableResult.space != null) {
                    getString(
                        R.string.user_profile_friend_number_template,
                        userProfileResult.userProfileVariableResult.space.friends
                    )
                } else {
                    getString(R.string.bbs_user_friend)
                }
                3 -> getString(R.string.user_profile_basic_information)
                4 -> getString(R.string.user_profile_edu_job)
                5 -> getString(R.string.user_profile_extra_information)
                6 -> getString(R.string.profile_group_information)
                else -> ""
            }
        }

        override fun getCount(): Int {
            return 7
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        init {
            Log.d(TAG, "refresh adapter")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {

                //返回键的id
                finishAfterTransition()
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun insertViewHistory(viewHistory: ViewHistory){
        if(viewHistory.name.isNullOrBlank()){
            Thread{
                val dao: ViewHistoryDao = getInstance(applicationContext).dao
                val viewHistories = dao
                    .getViewHistoryByBBSIdAndFid(viewHistory.belongedBBSId, viewHistory.fid)
                if (viewHistories == null || viewHistories.size == 0) {
                    dao.insert(viewHistory)
                } else {
                    for (i in viewHistories.indices) {
                        val updatedViewHistory = viewHistories[i]
                        updatedViewHistory.recordAt = Date()
                    }
                    dao.insert(viewHistories)
                }
            }.start()

        }

    }


    companion object {
        private val TAG = UserProfileActivity::class.java.simpleName
    }
}