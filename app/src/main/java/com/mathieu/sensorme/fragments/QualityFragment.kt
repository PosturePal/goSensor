package com.mathieu.sensorme.fragments

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mathieu.sensorme.R
import kotlinx.android.synthetic.main.fragment_quality.view.*
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import android.graphics.DashPathEffect
import android.graphics.Paint
//import java.awt.font.ShapeGraphicAttribute.STROKE




/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [QualityFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [QualityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QualityFragment : Fragment() {

    val TAG = "quality"
    public val title = "Quality"
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

        val inf = inflater!!.inflate(R.layout.fragment_quality, container, false)

        inf.quality_graph.gridLabelRenderer.gridColor = Color.WHITE
//        inf.quality_graph.drawingCacheBackgroundColor = R.color.colorAccent
        inf.quality_graph.titleColor = R.color.colorAccent
        inf.quality_graph.gridLabelRenderer.verticalLabelsColor = Color.WHITE
        inf.quality_graph.gridLabelRenderer.horizontalLabelsColor = Color.WHITE
        inf.quality_graph.gridLabelRenderer.horizontalAxisTitleColor = Color.WHITE
        inf.quality_graph.gridLabelRenderer.verticalAxisTitleColor = Color.WHITE
        inf.quality_graph.gridLabelRenderer.horizontalAxisTitle = "seconds"
        inf.quality_graph.gridLabelRenderer.verticalAxisTitle = "punches"

        val graph = inf.quality_graph as GraphView
        val series = LineGraphSeries<DataPoint>(arrayOf<DataPoint>(DataPoint(0.0, 1.0), DataPoint(1.0, 5.0),
                DataPoint(2.0, 3.0), DataPoint(3.0, 2.0), DataPoint(4.0, 6.0)))
        // styling series
        series.setTitle("Random Curve 1")
        series.setColor(Color.WHITE)
        series.setDrawDataPoints(true)
        series.setDataPointsRadius(10.0f)
        series.setThickness(8)

        graph.addSeries(series)

        var series2 = LineGraphSeries<DataPoint>(arrayOf<DataPoint>(DataPoint(0.0, 0.0), DataPoint(2.0, 1.0),
                DataPoint(3.0, 3.0), DataPoint(4.0, 4.0), DataPoint(4.0, 6.0)))
// custom paint to make a dotted line
        series2.setTitle("Random Curve 2")
        series2.setColor(Color.WHITE)
        series2.setDrawDataPoints(true)
        series2.setDataPointsRadius(10.0f)
        series2.setThickness(8)
//        val paint = Paint()
//        paint.setStyle(Paint.Style.STROKE)
//        paint.setStrokeWidth(10.0f)
//        paint.setPathEffect(DashPathEffect(floatArrayOf(8f, 5f), 0f))
//        series2.setCustomPaint(paint)

        graph.addSeries(series2)
        return inf
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
