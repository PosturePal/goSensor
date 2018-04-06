package com.mathieu.sensorme.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Explode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mathieu.sensorme.R


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SettingsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */



//
//NOT CALIB]: ax 0.034;       ay -0.01;       az 1.015;   gx 0.682;       gy -4.602;      gz -0.42;       ts 2099381248
//[FILTERED]: ax -2.0799298; ay 0.5984628; az 0.56812847; gx -0.89335394; gy -0.36361355; gz 2.180217; ts 2099381248
//I: pitch: -25.427042roll: 24.89616yaw: 173.43637
//NOT CALIB]: ax 0.033; ay -0.012; az 1.016; gx 0.735; gy -4.567; gz -0.42; ts -1860041728
//[FILTERED]: ax -2.1052282; ay 0.6212707; az 0.5618524; gx -0.8899045; gy -0.37507278; gz 2.184692; ts -1860041728
//I: pitch: -25.416107roll: 24.940935yaw: 173.57336
//NOT CALIB]: ax 0.033; ay -0.012; az 1.012; gx 0.717; gy -4.567; gz -0.49; ts -1524497408
//[FILTERED]: ax -2.098243; ay 0.61417997; az 0.56371206; gx -0.89005494; gy -0.3705004; gz 2.1669617; ts -1524497408
//I: pitch: -25.416107roll: 24.940935yaw: 173.57336
//NOT CALIB]: ax 0.032; ay -0.011; az 1.015; gx 0.717; gy -4.532; gz -0.35; ts -1188953088
//[FILTERED]: ax -2.064069; ay 0.584679; az 0.57208276; gx -0.88519377; gy -0.37137935; gz 2.1770563; ts -1188953088
//I: pitch: -25.405048roll: 24.985703yaw: 173.70985

class SettingsFragment : Fragment() {

    val TAG = "settings"
    public val title = "Settings"
    override fun onAttach(context: Context?) {
        Log.d(TAG,"onAttach")
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG,"onCreateView")
        return inflater!!.inflate(R.layout.fragment_settings,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG,"onActivityCreated")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG,"onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG,"onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG,"onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG,"onStop")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG,"onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG,"onDetach")
        super.onDetach();
    }
}// Required empty public constructor
