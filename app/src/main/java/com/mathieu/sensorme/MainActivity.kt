package com.mathieu.sensorme


import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import com.mathieu.sensorme.fragments.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_action.*

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

        supportActionBar!!.setDisplayHomeAsUpEnabled(true);

        nav_view.setNavigationItemSelectedListener { item ->
            onNavigationItemSelected(item)
        }
        // initial fragment
        changeFragment(HomeFragment())

    }

    fun changeFragment(newFragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()

        ft.replace(R.id.fragments_layout, newFragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun onNavigationItemSelected(item: MenuItem):Boolean {
        when (item.itemId) {
            R.id.nav_home_item -> {
                nav_action.setTitle(R.string.app_name);
                changeFragment(HomeFragment())
            }
            R.id.nav_devices_item -> {
                nav_action.setTitle("Devices");
                changeFragment(DevicesFragment())
            }
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
