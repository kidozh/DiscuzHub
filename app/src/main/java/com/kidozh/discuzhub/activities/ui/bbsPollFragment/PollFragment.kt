package com.kidozh.discuzhub.activities.ui.bbsPollFragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.adapter.PollOptionAdapter
import com.kidozh.discuzhub.databinding.FragmentBbsPollBinding
import com.kidozh.discuzhub.entities.Poll
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils.getPreferredClientWithCookieJarByUser
import com.kidozh.discuzhub.utilities.RecyclerItemClickListener
import com.kidozh.discuzhub.utilities.TimeDisplayUtils.Companion.getLocalePastTimeString
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.bbsParseUtils
import es.dmoral.toasty.Toasty
import okhttp3.*
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [bbsPollFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [bbsPollFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PollFragment : Fragment() {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // TODO: Rename and change types of parameters
    private lateinit var pollInfo: Poll
    private var tid = 0
    private var userBriefInfo: User? = null
    private var formhash: String = ""
    private var mListener: OnFragmentInteractionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            pollInfo = requireArguments().getSerializable(ConstUtils.PASS_POLL_KEY) as Poll
            userBriefInfo = requireArguments().getSerializable(ConstUtils.PASS_BBS_USER_KEY) as User?
            tid = requireArguments().getInt(ConstUtils.PASS_TID_KEY)
            client = getPreferredClientWithCookieJarByUser(requireContext(), userBriefInfo)
            formhash = requireArguments().getString(ConstUtils.PASS_FORMHASH_KEY) as String
        }
    }

    var binding: FragmentBbsPollBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBbsPollBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    var adapter: PollOptionAdapter? = null
    var client: OkHttpClient? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Poll $pollInfo")
        configurePollInformation()
    }

    private fun configurePollInformation() {
        if(pollInfo.expirations!=null){
            binding!!.bbsPollExpireTime.text = getString(
                R.string.poll_expire_at,
                getLocalePastTimeString(requireContext(), pollInfo.expirations!!)
            )
        }

        val res = resources
        val votersNumberString = res.getQuantityString(
            R.plurals.poll_voter_number,
            pollInfo.votersCount,
            pollInfo.votersCount
        )
        binding!!.bbsPollVoterNumber.text = votersNumberString
        // add attributes
        var chip = Chip(context)
        chip.chipBackgroundColor = requireContext().getColorStateList(R.color.chip_background_select_state)
        chip.setTextColor(requireContext().getColor(R.color.colorPrimary))
        if (!pollInfo.multiple) {
            chip.setText(R.string.poll_single_choice)
            chip.chipIcon =
                AppCompatResources.getDrawable(requireContext(),R.drawable.vector_drawable_radio_button_checked_24px)
        } else {
            chip.setText(R.string.poll_multiple_choices)
            chip.chipIcon =
                AppCompatResources.getDrawable(requireContext(),R.drawable.vector_drawable_format_list_bulleted_24px)
        }
        binding!!.bbsPollChipGroup.addView(chip)
        chip = Chip(context)
        if (pollInfo.allowVote) {
            chip.setText(R.string.poll_can_vote)
            chip.chipIcon = AppCompatResources.getDrawable(requireContext(),R.drawable.ic_check_24px)
            chip.setChipBackgroundColorResource(R.color.colorSafeStatus)
        } else {
            chip.setText(R.string.poll_cannot_vote)
            chip.chipIcon = AppCompatResources.getDrawable(requireContext(),R.drawable.ic_block_white_24px)
            chip.setChipBackgroundColorResource(R.color.colorUnSafeStatus)
        }
        chip.setTextColor(requireContext().getColor(R.color.colorPureWhite))
        binding!!.bbsPollChipGroup.addView(chip)
        chip = Chip(context)
        chip.chipBackgroundColor = requireContext().getColorStateList(R.color.chip_background_select_state)
        chip.setTextColor(requireContext().getColor(R.color.colorPrimary))
        if (pollInfo.resultVisible) {
            chip.setText(R.string.poll_visible_after_vote)
            chip.chipIcon = AppCompatResources.getDrawable(requireContext(),R.drawable.vector_drawable_how_to_vote_24px)
            binding!!.bbsPollChipGroup.addView(chip)
        }
        configurePollVoteBtn()
        configureRecyclerview()
    }

    private fun configurePollVoteBtn() {
        if (!pollInfo.allowVote) {
            binding!!.bbsPollVoteBtn.visibility = View.GONE
        } else {
            binding!!.bbsPollVoteBtn.visibility = View.VISIBLE
        }
        binding!!.bbsPollVoteBtn.isEnabled = false
        binding!!.bbsPollVoteBtn.text =
            getString(R.string.poll_vote_progress, 0, pollInfo.maxChoices)
        binding!!.bbsPollVoteBtn.setOnClickListener {
            val options = adapter!!.getPollOptions()
            val checkedNumber: Int = pollInfo.checkedOptionNumber
            //val checkedNumber: Int = 0
            if (pollInfo.allowVote && checkedNumber > 0 && checkedNumber <= pollInfo.maxChoices) {
                Log.d(TAG, "VOTING $formhash")
                binding!!.bbsPollVoteBtn.isEnabled = false
                val formBodyBuilder: FormBody.Builder = FormBody.Builder()
                    .add("formhash", formhash)
                // append pollanswers[]: id accordingly
                for (i in options.indices) {
                    val option = options[i]
                    if (option.checked) {
                        Log.d(TAG, "Option id " + option.id)
                        formBodyBuilder.add("pollanswers[]", option.id)
                    }
                }
                val request: Request = Request.Builder()
                    .url(URLUtils.getVotePollApiUrl(tid))
                    .post(formBodyBuilder.build())
                    .build()
                val mHandler = Handler(Looper.getMainLooper())
                client!!.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        mHandler.post {
                            Toasty.warning(
                                requireContext(),
                                requireContext().getString(R.string.network_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful && response.body != null) {
                            val s = response.body!!.string()
                            Log.d(TAG, "recv poll $s")
                            mHandler.post {
                                binding!!.bbsPollVoteBtn.isEnabled = true
                                // need to notify the activity if success
                                val message = bbsParseUtils.parseReturnMessage(s)
                                if (message != null) {
                                    if (message.value == "thread_poll_succeed") {
                                        // toast using
                                        Toasty.success(
                                            requireContext(),
                                            message.string,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        binding!!.bbsPollVoteBtn.isEnabled = false
                                        binding!!.bbsPollVoteBtn.text = message.string
                                    } else {
                                        Toasty.error(requireContext(), message.string, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    mListener!!.onPollResultFetched()
                                } else {
                                    Toasty.warning(
                                        requireContext(),
                                        requireContext().getString(R.string.parse_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    fun configureRecyclerview() {
        binding!!.bbsPollOptionRecyclerview.itemAnimator = getRecyclerviewAnimation(
            requireContext()
        )
        //binding.bbsPollOptionRecyclerview.setLayoutManager(new GridLayoutManager(getActivity(),2));
        binding!!.bbsPollOptionRecyclerview.layoutManager = LinearLayoutManager(activity)
        adapter = PollOptionAdapter()
        binding!!.bbsPollOptionRecyclerview.adapter = getAnimatedAdapter(
            requireContext(), adapter!!
        )
        val options = pollInfo.options
        if (options.size > 0) {
            adapter!!.setPollOptions(options)
        }
        // recyclerview check
        binding!!.bbsPollOptionRecyclerview.addOnItemTouchListener(
            RecyclerItemClickListener(
                getContext(),
                binding!!.bbsPollOptionRecyclerview,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        // if check
                        if (pollInfo.allowVote) {
                            // trigger it
                            adapter!!.pollOptions[position].checked =
                                !adapter!!.pollOptions[position].checked
                            adapter!!.notifyItemChanged(position)
                            val checkedNumber: Int = pollInfo.checkedOptionNumber
                            //val checkedNumber = 0
                            if (checkedNumber <= pollInfo.maxChoices && checkedNumber > 0) {
                                binding!!.bbsPollVoteBtn.isEnabled = true
                                binding!!.bbsPollVoteBtn.text = getString(
                                    R.string.poll_vote_progress,
                                    checkedNumber,
                                    pollInfo.maxChoices
                                )
                            } else {
                                binding!!.bbsPollVoteBtn.isEnabled = false
                                binding!!.bbsPollVoteBtn.text = getString(
                                    R.string.poll_vote_progress,
                                    checkedNumber,
                                    pollInfo.maxChoices
                                )
                            }
                        } else {
                            binding!!.bbsPollVoteBtn.visibility = View.GONE
                        }
                    }

                    override fun onItemLongClick(view: View, position: Int) {}
                })
        )
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onPollResultFetched()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener =
            if (context is OnFragmentInteractionListener) {
                context
            } else {
                throw RuntimeException(
                    context.toString()
                            + " must implement OnFragmentInteractionListener"
                )
            }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onPollResultFetched()
    }

    companion object {
        private val TAG = PollFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         *
         * @return A new instance of fragment bbsPollFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(
            pollInfo: Poll?,
            userBriefInfo: User?,
            tid: Int,
            formhash: String?
        ): PollFragment {
            val fragment = PollFragment()
            val args = Bundle()
            args.putSerializable(ConstUtils.PASS_POLL_KEY, pollInfo)
            args.putInt(ConstUtils.PASS_TID_KEY, tid)
            args.putSerializable(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
            args.putSerializable(ConstUtils.PASS_FORMHASH_KEY, formhash)
            fragment.arguments = args
            return fragment
        }
    }
}