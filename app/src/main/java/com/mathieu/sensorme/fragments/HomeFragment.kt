package com.mathieu.sensorme.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import com.mathieu.sensorme.R
import android.view.*

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * [HomeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), View.OnClickListener {

    val TAG = "home"

    public val title = "Home"
    override fun onAttach(context: Context?) {
        Log.d(TAG, "onAttach")
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        var inf = inflater!!.inflate(R.layout.fragment_home, container, false)
        //inf.home_logo_element.setOnClickListener(this)
        return inf;
    }


    fun changeFragment(newFragment: Fragment) {
        val ft = this.activity.supportFragmentManager.beginTransaction()
        ft.replace(R.id.home_fragments_layout, newFragment)
        ft.addToBackStack(null)
        ft.commit()
    }


    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach();
    }
}