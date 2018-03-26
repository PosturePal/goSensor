//SEgoeUI

package com.mathieu.sensorme


import android.graphics.Color
import android.graphics.ColorFilter
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import com.mathieu.sensorme.fragments.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.nav_action.*
import kotlinx.android.synthetic.main.nav_header.view.*

class MainActivity() : AppCompatActivity() {

    private var mToogle: ActionBarDrawerToggle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // NAVIGATION
        setSupportActionBar(nav_action)

        mToogle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(mToogle!!)
        mToogle!!.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

//        bottom_navigation.setOnNavigationItemSelectedListener { item -> onBottomNavigationItemSelected(item) }
        nav_view.setNavigationItemSelectedListener { item ->
            onNavigationItemSelected(item)
        }
        collapsingtoolbarly.setTitleEnabled(false);
//        bottom_nav.bottom_nav_track_button.setOnClickListener {view ->
//            nav_action.setTitle("Under Construction")
////            bottom_nav.bottom_nav_track_button.textView.setTextColor(R.color.colorAccent)
//            changeFragment(UnderConstructionFragment())
//        }
        // initial fragment
        changeFragment(BottomNavFragment())

    }

    fun changeFragment(newFragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()

        ft.replace(R.id.fragments_layout, newFragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun onBottomNavigationItemSelected(item:MenuItem):Boolean
    {
        when (item.itemId) {
            R.id.bottom_nav_quality_item -> {
                collapsingtoolbarly.setTitle("Quality");
                changeFragment(HomeFragment())
            }
            R.id.bottom_nav_intensity_item -> {
                collapsingtoolbarly.setTitle("Intensity")
                changeFragment(IntensityFagment())
            }
            else -> {
                collapsingtoolbarly.setTitle("Under Construction")
                changeFragment(UnderConstructionFragment())
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }


    private fun onNavigationItemSelected(item: MenuItem):Boolean {
        when (item.itemId) {
            R.id.nav_home_item -> {
                nav_action.setTitle(R.string.app_name);
                changeFragment(BottomNavFragment())
            }
            R.id.nav_devices_item -> {
                nav_action.setTitle("Devices");
                changeFragment(DevicesFragment())
            }
//            R.id.nav_intensity_item -> {
//                nav_action.setTitle("Intensity")
//                changeFragment(IntensityFagment())
//            }
//            R.id.nav_settings_item -> {
//                nav_action.setTitle("Settings");
//                changeFragment(SettingsFragment())
//            }
            else -> {
                nav_action.setTitle("Under Construction")
                changeFragment(UnderConstructionFragment())
            }
//            R.id.nav_help_item -> //open-somehow-link(getString(R.string.help_link))
//            R.id.nav_share_item -> //share somehow
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(mToogle!!.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item)
    }
}
