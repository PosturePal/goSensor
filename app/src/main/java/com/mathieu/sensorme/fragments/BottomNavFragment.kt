package com.mathieu.sensorme.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.transition.Explode
import android.transition.TransitionManager
import android.util.Log
import com.mathieu.sensorme.R
import kotlinx.android.synthetic.main.fragment_home.*
import android.transition.Slide
import android.view.*
import com.mathieu.sensorme.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_bottom_nav.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.nav_action.*
import kotlinx.android.synthetic.main.nav_action.view.*


/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * [HomeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomNavFragment : Fragment(), View.OnClickListener {

    val TAG = "Nav"

    public val title = "Nav"
    override fun onAttach(context: Context?) {
        Log.d(TAG, "onAttach")
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        changeFragment(DevicesFragment())
    }


    @SuppressLint("RestrictedApi")
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val inf = inflater!!.inflate(R.layout.fragment_bottom_nav, container, false)
        //inf.home_logo_element.setOnClickListener(this)
        inf.bottom_navigation?.setOnNavigationItemSelectedListener { item -> onNavigationItemSelected(item) }

        inf.bottom_navigation.enableAnimation(false)
        inf.bottom_navigation.enableShiftingMode(false)
        inf.bottom_navigation.enableItemShiftingMode(false)
                activity.collapsingtoolbarly.setVisibility(View.GONE)
        return inf;
    }


    fun changeFragment(newFragment: Fragment) {
        val ft = this.activity.supportFragmentManager.beginTransaction()
        ft.replace(R.id.home_fragments_layout, newFragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun onNavigationItemSelected(item:MenuItem):Boolean
    {
        when (item.itemId) {
            R.id.bottom_nav_connect_item -> {
                activity.nav_action.setTitle("Connect");
                changeFragment(DevicesFragment())
            }
            R.id.bottom_nav_intensity_item -> {
                activity.nav_action.setTitle("Intensity")
                changeFragment(IntensityFagment())
            }
            R.id.bottom_nav_quality_item -> {
                activity.nav_action.setTitle("Quality")
                changeFragment(QualityFragment())
            }
            R.id.bottom_nav_competition_item -> {
                activity.nav_action.setTitle("Competition")
                changeFragment(CompetitionFragment())
            }
            R.id.bottom_nav_track_item -> {
                activity.nav_action.setTitle("Track")
                changeFragment(TrackFragment())
            }
            else -> {
                activity.nav_action.setTitle("Under Construction")
                changeFragment(UnderConstructionFragment())
            }
        }
        activity.drawer.closeDrawer(GravityCompat.START);
        return true;

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
        activity.collapsingtoolbarly.setVisibility(View.VISIBLE);
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