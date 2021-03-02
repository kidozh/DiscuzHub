package com.kidozh.discuzhub.activities.ui.userThreads

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.adapter.ThreadAdapter
import com.kidozh.discuzhub.databinding.ContentEmptyInformationBinding
import com.kidozh.discuzhub.databinding.FragmentBbsMyThreadBinding
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.interact.BaseStatusInteract
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.bbsParseUtils
import okhttp3.*
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 */
class UserThreadFragment : Fragment() {
    private val TAG = UserThreadFragment::class.java.simpleName
    private var user: User? = null
    lateinit var bbsInfo: Discuz
    private lateinit var client: OkHttpClient
    lateinit var adapter: ThreadAdapter

    lateinit var binding: FragmentBbsMyThreadBinding
    lateinit var emptyBinding: ContentEmptyInformationBinding
    lateinit var model: UserThreadViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            bbsInfo = requireArguments().getSerializable(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
            URLUtils.setBBS(bbsInfo)
            user = requireArguments().getSerializable(ConstUtils.PASS_BBS_USER_KEY) as User?
            client = NetworkUtils.getPreferredClientWithCookieJarByUser(context, user)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentBbsMyThreadBinding.inflate(inflater, container, false)
        emptyBinding = binding.fragmentMyThreadEmptyView
        model = ViewModelProvider(this).get(UserThreadViewModel::class.java)
        model.configureInfo(bbsInfo,user)
        adapter = ThreadAdapter(null, bbsInfo, user)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecyclerview()
        configureSwipeRefreshLayout()
        configureEmptyView()
        bindViewModel()
    }

    private fun configureEmptyView() {
        emptyBinding.emptyIcon.setImageResource(R.drawable.ic_empty_my_post_list)
        emptyBinding.emptyContent.setText(R.string.empty_post_list)
    }

    private fun configureRecyclerview() {
        val linearLayoutManager = LinearLayoutManager(context)
        binding.fragmentMyThreadRecyclerview.layoutManager = linearLayoutManager
        binding.fragmentMyThreadRecyclerview.itemAnimator = getRecyclerviewAnimation(requireContext())
        binding.fragmentMyThreadRecyclerview.adapter = getAnimatedAdapter(requireContext(), adapter)
    }

    fun configureSwipeRefreshLayout() {
        binding.fragmentMyThreadSwipeRefreshLayout.setOnRefreshListener {
            model.globalPageMutableLiveData.postValue(1)
            model.getNextThread()
        }


    }

    fun bindViewModel(){
        model.totalThreadList.observe(viewLifecycleOwner,{
            adapter.updateList(it)
            if(it.isEmpty()){
                emptyBinding.emptyView.visibility = View.VISIBLE
            }
            else{
                emptyBinding.emptyView.visibility = View.GONE
            }
        })

        model.networkState.observe(viewLifecycleOwner,{
            when(it){
                ConstUtils.NETWORK_STATUS_LOADING ->{
                    binding.fragmentMyThreadSwipeRefreshLayout.isRefreshing = true
                }
                else->{
                    binding.fragmentMyThreadSwipeRefreshLayout.isRefreshing = false
                }

            }
        })

        model.getNextThread()
    }

    companion object {
        @JvmStatic
        fun newInstance(Discuz: Discuz, user: User?): UserThreadFragment {
            val fragment = UserThreadFragment()
            val args = Bundle()
            args.putSerializable(ConstUtils.PASS_BBS_ENTITY_KEY, Discuz)
            args.putSerializable(ConstUtils.PASS_BBS_USER_KEY, user)
            fragment.arguments = args
            return fragment
        }
    }
}