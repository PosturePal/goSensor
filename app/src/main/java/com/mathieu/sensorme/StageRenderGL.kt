package com.mathieu.sensorme

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.opengl.Matrix
import android.os.SystemClock
import java.util.concurrent.TimeUnit


class StageRenderGL(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private var w: Float = 0.0f
    private var h: Float = 0.0f
    private var screenW: Float = 0.0f
    private var screenH: Float = 0.0f

    public var mStageRenderer: StageRenderer = StageRenderer()

    init {
//            super(context, attrs)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)

        setEGLContextClientVersion(2)

        setRenderer(mStageRenderer)
    }

    class StageRenderer : GLSurfaceView.Renderer {

//
//        var color = 0f
//        var colorVelocity = 1f / 60f
//
//
//        private val mCube = Cube()
//        private var mCubeRotation: Float = 0.0f
//        override fun onDrawFrame(gl: GL10?) {
//            // Redraw background color
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//
//            if (color > 1 || color < 0) {
//                colorVelocity = -colorVelocity
//            }
//            color += colorVelocity
//
//            gl?.glClearColor(color * 0.5f, color, color, 1f)
//            gl?.glClear(GL_COLOR_BUFFER_BIT)
////
////
////            gl?.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
////            gl?.glLoadIdentity()
////
////            gl?.glTranslatef(0.0f, 0.0f, 100.0f);
////            gl?.glRotatef(mCubeRotation, 1.0f, 1.0f, 1.0f);
////
//            mCube.draw();
////
////            gl.glLoadIdentity();
////
////            mCubeRotation -= 0.15f;
//        }
//
//        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
////            GLES20.glViewport(0, 0, width, height);
//            gl?.glViewport(0, 0, width, height)
//            gl?.glMatrixMode(GL10.GL_PROJECTION)
//            gl?.glLoadIdentity()
//            GLU.gluPerspective(gl, 45.0f, (width.toFloat() / height.toFloat()), 0.1f, 100.0f)
//            gl?.glViewport(0, 0, width, height)
//
//            gl?.glMatrixMode(GL10.GL_MODELVIEW)
//            gl?.glLoadIdentity()
//        }
//
//        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//            // Set the background frame color
//            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//            gl?.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
//
//            gl?.glClearDepthf(1.0f);
//            gl?.glEnable(GL10.GL_DEPTH_TEST);
//            gl?.glDepthFunc(GL10.GL_LEQUAL);
//
//            gl?.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
//                    GL10.GL_NICEST);
//        }


        /** Rotation increment per frame.  */
        private val CUBE_ROTATION_INCREMENT = 0.6f

        /** The refresh rate, in frames per second.  */
        private val REFRESH_RATE_FPS = 60

        /** The duration, in milliseconds, of one frame.  */
        private val FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS

        private var mMVPMatrix: FloatArray = FloatArray(16)
        private var mProjectionMatrix: FloatArray = FloatArray(16)
        private var mViewMatrix: FloatArray = FloatArray(16)
        private var mRotationMatrix: FloatArray = FloatArray(16)
        private var mFinalMVPMatrix: FloatArray = FloatArray(16)

        private var mCubeRotation: Float = 0.toFloat()
        private var mLastUpdateMillis: Long = 0


        private var color = 1f
        private var colorVelocity = 1f / 60f

        public var mCube: Cube? = null

        var line = Line()

        init {
            // Set the fixed camera position (View matrix).
            Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            // Set the background frame color
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glClearDepthf(1.0f)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glDepthFunc(GLES20.GL_LEQUAL)
            mCube = Cube()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            val ratio = width.toFloat() / height

            GLES20.glViewport(0, 0, width, height)
            // This projection matrix is applied to object coordinates in the onDrawFrame() method.
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 3.0f, 7.0f)
            // modelView = projection x view
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
//            glTranslatef(roll, pitch, yaw)
        }


        private var roll: Float = 25.0f
        private var pitch: Float = 23.0f
        private var yaw: Float = -12.0f

        public fun setRotation(r: Float, p: Float, y: Float) {
            this.roll = r;
            this.pitch = p;
            this.yaw = y;
        }


        public fun rotate(r: Float, p: Float, y: Float) {
            roll = roll + r;
            pitch = pitch + p;
            yaw = yaw + y;
        }


        override fun onDrawFrame(gl: GL10?) {
            // Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

//            if (color > 1 || color < 0) {
//                colorVelocity = -colorVelocity
//            }
//            color += colorVelocity

//            gl?.glClearColor(color * 0.5f, color, color, 1f)
            gl?.glClearColor(color, color, color, 1f)
            gl?.glClear(GL_COLOR_BUFFER_BIT)

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

            // TODO: ROTATE THIS CUBE
//        Log.i("ROT","Witth pitch: " + pitch.toString() + ", yaw: " + yaw.toString() + ", roll" + roll.toString())
//            val x = cos (pitch.toDouble()) * cos ( yaw.toDouble())
//            val y = sin ( yaw.toDouble() )
//            val z = sin ( pitch.toDouble() ) * cos ( yaw.toDouble() )
//        Log.i("ROT","Witth ----x: " + x.toString() + ", y: " + y.toString() + ", z" + z.toString())
//        glTranslatef(ROL`.toFloat(), y.toFloat(), z.toFloat())

//            glRotatef(roll.toFloat(), 1.0f, 0.0f, 0.0f)
//            glRotatef(pitch.toFloat(), 0.0f, 1.0f, 0.0f)
//            glRotatef(yaw.toFloat(), 0.0f, 0.0f, 1.0f)

            // Apply the rotation.
            Matrix.setRotateM(mRotationMatrix, 0, 0.0f, 1.0f, 1.0f, 1.0f)
            Matrix.rotateM(mRotationMatrix, 0, roll, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mRotationMatrix, 0, pitch, 0.0f, 1.0f, 0.0f)
            Matrix.rotateM(mRotationMatrix, 0, yaw, 0.0f, 0.0f, 1.0f)
            // Combine the rotation matrix with the projection and camera view
            Matrix.multiplyMM(mFinalMVPMatrix, 0, mMVPMatrix, 0, mRotationMatrix, 0)

            // Draw cube.
            mCube!!.draw(mFinalMVPMatrix)
//            updateCubeRotation()
//            val vertLine = Line()
//            vertLine.SetVerts(-0.5f, 0.5f, 0f, -0.5f, -0.5f, 0f)
//            vertLine.SetColor(.8f, .8f, 0f, 1.0f)
//            vertLine.draw(mMVPMatrix)
//            vertLine.SetVerts(-0.5f, 0.5f, 0f, -0.5f, -0.5f, 0f)
//            vertLine.SetColor(.8f, .8f, 0f, 1.0f)
//            vertLine.draw(mMVPMatrix)

//            line = Line()
            line.clear()
            line.SetVerts(-0.5f, -0.5f, 0f, -0.5f, -0.5f, -1.0f)
            line.SetColor(0f, 0f, 0.8f, 1.0f)
            line.draw(mFinalMVPMatrix)
            line.SetVerts(-0.5f, -0.5f, 0f, -0.5f, 0.5f, 0f)
            line.SetColor(0.8f, 0f, 0f, 1.0f)
            line.draw(mFinalMVPMatrix)
            line.SetVerts(-0.5f, -0.5f, 0f, 0.5f, -0.5f, 0f)
            line.SetColor(0f, 0.8f, 0f, 1.0f)
            line.draw(mFinalMVPMatrix)

            line.SetColor(0.7f, 0.7f, 0.7f, 1.0f)

            line.SetVerts(0.5f, 0.5f, 0f, 0.5f, 0.5f, -1.0f)
            line.draw(mFinalMVPMatrix)
            line.SetVerts(0.5f, 0.5f, 0f, 0.5f, -0.5f, 0f)
            line.draw(mFinalMVPMatrix)
            line.SetVerts(0.5f, 0.5f, 0f, -0.5f, 0.5f, 0f)
            line.draw(mFinalMVPMatrix)


            line.SetVerts(0.5f, -.5f, 0f, 0.5f, -.5f, -1.0f) // rb
            line.draw(mFinalMVPMatrix)
            line.SetVerts(0.5f, 0.5f, -1.0f, 0.5f, -0.5f, -1.0f)
            line.draw(mFinalMVPMatrix)
            line.SetVerts(0.5f, 0.5f, -1.0f, -0.5f, 0.5f, -1.0f)
            line.draw(mFinalMVPMatrix)


            line.SetVerts(-0.5f, 0.5f, 0f, -.5f, 0.5f, -1.0f)
            line.draw(mFinalMVPMatrix)


            line.SetVerts(-0.5f, -0.5f, -1f, .5f, -.5f, -1f)
            line.draw(mFinalMVPMatrix)

            line.SetVerts(-0.5f, 0.5f, -1f, -.5f, -.5f, -1f)
            line.draw(mFinalMVPMatrix)

        }

        /** Updates the cube rotation.  */
        private fun updateCubeRotation() {
            if (mLastUpdateMillis != 0L) {
                val factor = (SystemClock.elapsedRealtime() - mLastUpdateMillis) / FRAME_TIME_MILLIS
                mCubeRotation += CUBE_ROTATION_INCREMENT * factor
            }
            mLastUpdateMillis = SystemClock.elapsedRealtime()
        }

    }
}
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Renders a 3D Cube using OpenGL ES 2.0.
 *
 * For more information on how to use OpenGL ES 2.0 on Android, see the
 * [
 * Displaying Graphics with OpenGL ES](//developer.android.com/training/graphics/opengl/index.html) developer guide.
 */
class Cube {

    /** Vertex size in bytes.  */
    private val VERTEX_STRIDE = COORDS_PER_VERTEX * 4

    /** Color size in bytes.  */
    private val COLOR_STRIDE = VALUES_PER_COLOR * 4


    private val mVertexBuffer: FloatBuffer
    private val mColorBuffer: FloatBuffer
    private val mIndexBuffer: ByteBuffer
    private val mProgram: Int
    private val mPositionHandle: Int
    private val mColorHandle: Int
    private val mMVPMatrixHandle: Int


    init {
        var byteBuffer = ByteBuffer.allocateDirect(VERTICES.size * 4)

        byteBuffer.order(ByteOrder.nativeOrder())
        mVertexBuffer = byteBuffer.asFloatBuffer()
        mVertexBuffer.put(VERTICES)
        mVertexBuffer.position(0)

        byteBuffer = ByteBuffer.allocateDirect(COLORS.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        mColorBuffer = byteBuffer.asFloatBuffer()
        mColorBuffer.put(COLORS)
        mColorBuffer.position(0)

        mIndexBuffer = ByteBuffer.allocateDirect(INDICES.size)
        mIndexBuffer.put(INDICES)
        mIndexBuffer.position(0)

        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE))
        GLES20.glAttachShader(
                mProgram, loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE))
        GLES20.glLinkProgram(mProgram)

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor")
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix The Model View Project matrix in which to draw this shape
     */
    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL environment.
        GLES20.glUseProgram(mProgram)

        // Prepare the cube coordinate data.
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(
                mPositionHandle, 3, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer)

        // Prepare the cube color data.
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(
                mColorHandle, 4, GLES20.GL_FLOAT, false, COLOR_STRIDE, mColorBuffer)

        // Apply the projection and view transformation.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

// Draw the cube.

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, INDICES.size, GLES20.GL_UNSIGNED_BYTE, mIndexBuffer)

        // Disable vertex arrays.
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
    }

    companion object {

        /** Cube vertices  */
        private val VERTICES = floatArrayOf(
                -0.5f, -0.5f, -1.0f,
                0.5f, -0.5f, -1.0f,
                0.5f, 0.5f, -1.0f,
                -0.5f, 0.5f, -1.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
                -0.5f, 0.5f, 0.0f)
//        -0.5f, -0.5f, -0.5f,
//        0.5f, -0.5f, -0.5f,
//        0.5f, 0.5f, -0.5f,
//        -0.5f, 0.5f, -0.5f,
//        -0.5f, -0.5f, 0.5f,
//        0.5f, -0.5f, 0.5f,
//        0.5f, 0.5f, 0.5f,
//        -0.5f, 0.5f, 0.5f)

        /** Vertex colors.  */
        private val COLORS = floatArrayOf(
                .5f, .5f, .5f, 1f,
                .5f, .5f, .5f, 1f,
                .5f, .5f, .5f, 1f,
                .5f, .5f, .5f, 1f,
                .5f, .5f, .5f, 1f,
                .5f, .5f, .5f, 1f,
                .5f, .5f, .5f, 1f,
                .5f, .5f, .5f, 1f)
//        1.0f,1.0f,1.0f,1.0f,//0.0f, 1.0f, 1.0f, 1.0f,
//        1.0f,1.0f,1.0f,1.0f,//1.0f, 0.0f, 0.0f, 1.0f,
//        1.0f,1.0f,1.0f,1.0f,//1.0f, 1.0f, 0.0f, 1.0f,
//        1.0f,1.0f,1.0f,1.0f,//0.0f, 1.0f, 0.0f, 1.0f,
//        1.0f,1.0f,1.0f,1.0f,//0.0f, 0.0f, 1.0f, 1.0f,
//        1.0f,1.0f,1.0f,1.0f,//1.0f, 0.0f, 1.0f, 1.0f,
//        1.0f,1.0f,1.0f,1.0f,//1.0f, 1.0f, 1.0f, 1.0f,
//        0.5f,1.0f,1.0f,1.0f)//0.0f, 1.0f, 1.0f, 1.0f)


        /** Order to draw vertices as triangles.  */

//        2   1  2
//        3   0  3z
        private val INDICES = byteArrayOf(
                0, 1, 3, 3, 1, 2, // Front face.
                0, 1, 4, 4, 5, 1, // Bottom face.
                1, 2, 5, 5, 6, 2, // Right face.
                2, 3, 6, 6, 7, 3, // Top face.
                3, 7, 4, 4, 3, 0, // Left face.
                4, 5, 7, 7, 6, 5)// Rear face.

        /** Number of coordinates per vertex in [VERTICES].  */
        private val COORDS_PER_VERTEX = 3

        /** Number of values per colors in [COLORS].  */
        private val VALUES_PER_COLOR = 4

        /** Shader code for the vertex.  */
        private val VERTEX_SHADER_CODE = "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "attribute vec4 vColor;" +
                "varying vec4 _vColor;" +
                "void main() {" +
                "  _vColor = vColor;" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

        /** Shader code for the fragment.  */
        private val FRAGMENT_SHADER_CODE = (
                "precision mediump float;" +
                        "varying vec4 _vColor;" +
                        "void main() {" +
                        "  gl_FragColor = _vColor;" +
                        "}")

        /** Loads the provided shader in the program.  */
        private fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)

            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }
    }
}


class Line {
    private val VertexBuffer: FloatBuffer

    private val VertexShaderCode = "uniform mat4 uMVPMatrix;" +

            "attribute vec4 vPosition;" +
            "void main() {" +
// the matrix must be included as a modifier of gl_Position
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}"

    private val FragmentShaderCode = (
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}")

    protected var GlProgram: Int = 0
    protected var PositionHandle: Int = 0
    protected var ColorHandle: Int = 0
    protected var MVPMatrixHandle: Int = 0

    private val VertexCount = LineCoords.size / COORDS_PER_VERTEX
    private val VertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    internal var color = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    protected var vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
    protected var fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
    init {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                LineCoords.size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        VertexBuffer = bb.asFloatBuffer()
        // add the coordinates to the FloatBuffer
        VertexBuffer.put(LineCoords)
        // set the buffer to read the first coordinate
        VertexBuffer.position(0)

        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(vertexShader, VertexShaderCode)
        GLES20.glCompileShader(vertexShader)
        GLES20.glShaderSource(fragmentShader, FragmentShaderCode)
        GLES20.glCompileShader(fragmentShader)




        GlProgram = GLES20.glCreateProgram()             // create empty OpenGL ES Program
        GLES20.glAttachShader(GlProgram, vertexShader)   // add the vertex shader to program
        GLES20.glAttachShader(GlProgram, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(GlProgram)                  // creates OpenGL ES program executables
    }

    fun clear() {
        // initia
//        VertexBuffer.put(LineCoords)
//        // set the buffer to read the first coordinate
//        VertexBuffer.position(0)
//
        GLES20.glDetachShader(vertexShader, GLES20.GL_VERTEX_SHADER)
        GLES20.glDetachShader(fragmentShader, GLES20.GL_FRAGMENT_SHADER)
        vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(vertexShader, VertexShaderCode)
        GLES20.glCompileShader(vertexShader)
        GLES20.glShaderSource(fragmentShader, FragmentShaderCode)
        GLES20.glCompileShader(fragmentShader)



        GLES20.x(GlProgram)
        GlProgram = GLES20.glCreateProgram()             // create empty OpenGL ES Program
        GLES20.glAttachShader(GlProgram, vertexShader)   // add the vertex shader to program
        GLES20.glAttachShader(GlProgram, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(GlProgram)                  // creates OpenGL ES program executables
    }
    fun SetVerts(v0: Float, v1: Float, v2: Float, v3: Float, v4: Float, v5: Float) {
        LineCoords[0] = v0
        LineCoords[1] = v1
        LineCoords[2] = v2
        LineCoords[3] = v3
        LineCoords[4] = v4
        LineCoords[5] = v5

        VertexBuffer.put(LineCoords)
        // set the buffer to read the first coordinate
        VertexBuffer.position(0)
    }

    fun SetColor(red: Float, green: Float, blue: Float, alpha: Float) {
        color[0] = red
        color[1] = green
        color[2] = blue
        color[3] = alpha
    }

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(GlProgram)

        // get handle to vertex shader's vPosition member
        PositionHandle = GLES20.glGetAttribLocation(GlProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(PositionHandle)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(PositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VertexStride, VertexBuffer)

        // get handle to fragment shader's vColor member
        ColorHandle = GLES20.glGetUniformLocation(GlProgram, "vColor")

        // Set color for drawing the triangle
        GLES20.glUniform4fv(ColorHandle, 1, color, 0)

        // get handle to shape's transformation matrix
        MVPMatrixHandle = GLES20.glGetUniformLocation(GlProgram, "uMVPMatrix")
//ArRenderer.checkGlError("glGetUniformLocation")

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0)
//ArRenderer.checkGlError("glUniformMatrix4fv")

        GLES20.glLineWidth(10.0f)
        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, VertexCount)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(PositionHandle)
    }

    companion object {

        // number of coordinates per vertex in this array
        internal val COORDS_PER_VERTEX = 3
        internal var LineCoords = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f)
    }
}