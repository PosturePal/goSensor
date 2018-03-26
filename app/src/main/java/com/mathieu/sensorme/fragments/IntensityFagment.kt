package com.mathieu.sensorme.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mathieu.sensorme.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [IntensityFagment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [IntensityFagment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntensityFagment : Fragment() {

        val TAG = "intensity"
        public val title = "Intensity"
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
            return inflater!!.inflate(R.layout.fragment_intensity,container,false)
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
