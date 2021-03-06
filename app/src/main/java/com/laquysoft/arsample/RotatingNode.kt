package com.laquysoft.arsample

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3

class RotatingNode : Node(), Node.OnTapListener {
    override fun onTap(p0: HitTestResult?, p1: MotionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    // We'll use Property Animation to make this node rotate.

    private var rotationAnimation: ObjectAnimator? = null
    private var degreesPerSecond = 90.0f

    private var lastSpeedMultiplier = 1.0f

    private val animationDuration: Long
        get() = (1000 * 360 / (degreesPerSecond * speedMultiplier)).toLong()

    private val speedMultiplier: Float
        get() = 1.0f

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)

        // Animation hasn't been set up.
        if (rotationAnimation == null) {
            return
        }

        // Check if we need to change the speed of rotation.
        val speedMultiplier = speedMultiplier

        // Nothing has changed. Continue rotating at the same speed.
        if (lastSpeedMultiplier == speedMultiplier) {
            return
        }

        if (speedMultiplier == 0.0f) {
            rotationAnimation!!.pause()
        } else {
            rotationAnimation?.let {
                it.resume()
                it.duration = animationDuration
                it.setCurrentFraction(it.animatedFraction)
            }
        }
        lastSpeedMultiplier = speedMultiplier
    }

    /** Sets rotation speed  */
    fun setDegreesPerSecond(degreesPerSecond: Float) {
        this.degreesPerSecond = degreesPerSecond
    }

    override fun onActivate() = startAnimation()

    override fun onDeactivate() = stopAnimation()

    private fun startAnimation() {
        if (rotationAnimation != null) {
            return
        }
        rotationAnimation = createAnimator().apply {
            target = this@RotatingNode
            duration = animationDuration
            start()
        }
    }

    private fun stopAnimation() {
        rotationAnimation?.cancel()
        rotationAnimation = null
    }

    /** Returns an ObjectAnimator that makes this node rotate.  */
    private fun createAnimator(): ObjectAnimator {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.
        val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f)
        val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f)
        val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f)
        val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)

        return ObjectAnimator().apply {
            setObjectValues(orientation1, orientation2, orientation3, orientation4)
            // Next, give it the localRotation property.
            propertyName = "localRotation"

            // Use Sceneform's QuaternionEvaluator.
            setEvaluator(QuaternionEvaluator())

            //  Allow rotationAnimation to repeat forever
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
            setAutoCancel(true)
        }
    }
}