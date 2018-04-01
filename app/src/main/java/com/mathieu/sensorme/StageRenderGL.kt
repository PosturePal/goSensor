package com.mathieu.sensorme

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT

class StageRenderGL(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private var w:Float = 0.0f
    private var h:Float = 0.0f
    private var screenW:Float = 0.0f
    private var screenH:Float = 0.0f

    private var mStageRenderer:StageRenderer = StageRenderer()


    init {
//            super(context, attrs)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)

        setEGLContextClientVersion(2)

        setRenderer(mStageRenderer)
    }

    private class StageRenderer:GLSurfaceView.Renderer {


        var color = 0f
        var colorVelocity = 1f/60f

        override fun onDrawFrame(gl: GL10?) {
            // Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            if (color > 1 || color < 0){
                colorVelocity = -colorVelocity
            }
            color += colorVelocity

            gl?.glClearColor(color * 0.5f, color, color, 1f)
            gl?.glClear(GL_COLOR_BUFFER_BIT)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//            GLES20.glViewport(0, 0, width, height);
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            // Set the background frame color
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }

    }
}