package com.kidozh.discuzhub.activities.ui.smiley

import android.content.Context
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.AnimationUtils.getAnimatedAdapter
import com.kidozh.discuzhub.adapter.SmileyAdapter
import com.kidozh.discuzhub.entities.Smiley
import com.kidozh.discuzhub.entities.Discuz
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kidozh.discuzhub.databinding.FragmentSmileyBinding
import com.kidozh.discuzhub.viewModels.SmileyViewModel
import java.lang.RuntimeException
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * interface
 * to handle interaction events.
 * Use the [MarkedSmileyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MarkedSmileyFragment : Fragment() {
    lateinit var adapter: SmileyAdapter
    private var mListener: SmileyFragment.OnSmileyPressedInteraction? = null
    private lateinit var discuz: Discuz
    private lateinit var model : SmileyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            discuz = requireArguments().getSerializable(DISCUZ_PARAM) as Discuz
        }
    }

    var binding: FragmentSmileyBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSmileyBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProvider(requireActivity()).get(SmileyViewModel::class.java)
        model.configureDiscuz(discuz,null)
        configureRecyclerView()
    }

    fun configureRecyclerView() {
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(context, 6, LinearLayoutManager.VERTICAL, false)
        binding!!.smileyRecyclerview.layoutManager = layoutManager
        binding!!.smileyRecyclerview.itemAnimator = getRecyclerviewAnimation(requireContext())
        adapter = SmileyAdapter(requireContext(),discuz,
                ({ v1, position ->
                    val img = v1 as ImageView
                    smileyClick(img.drawable, position)
                }))
        binding!!.smileyRecyclerview.adapter = getAnimatedAdapter(requireContext(), adapter)
        model.latestSmileyListLiveData.observe(viewLifecycleOwner,{
            Log.d(TAG,"GET latest smiley "+it)
            adapter.setsmileys(it)

        })

    }

    private fun smileyClick(d: Drawable, position: Int) {
        if (position > adapter.smileys!!.size) {
            return
        }
        val name = adapter.smileys!![position].code
        Log.d(TAG, "get name $name")
        mListener?.onSmileyPress(name,d)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is SmileyFragment.OnSmileyPressedInteraction) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnSmileyPressedInteraction")
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
    interface OnSmileyPressedInteraction {
        // TODO: Update argument type and name
        fun onSmileyPress(str: String, a: Drawable)
    }

    companion object {
        private val TAG = MarkedSmileyFragment::class.java.simpleName

        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val DISCUZ_PARAM = "DISCUZ"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SmileyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(discuz: Discuz): MarkedSmileyFragment {
            val fragment = MarkedSmileyFragment()
            val args = Bundle()
            args.putSerializable(DISCUZ_PARAM, discuz)
            fragment.arguments = args
            return fragment
        }
    }
}