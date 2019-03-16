package com.github.veselinazatchepina.carmovement

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_car_movement.*
import android.graphics.*
import android.animation.Animator
import android.os.Handler
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator


/**
 *  Как происходит движение машины:
 *  1. Отрисовываем машину в центре экрана.
 *  2. При нажатии на экран определяем координаты начала и конца пути машины.
 *  3. Анимируем движение, при этом поворачиваем машину в нужном направлении.
 */
class CarMovementFragment : Fragment(), View.OnTouchListener {

    private var car: View? = null
    private var isCarMoving = false

    companion object {
        private const val CAR_WIDTH = 150
        private const val CAR_HEIGHT = 300

        fun createInstance(): CarMovementFragment {
            return CarMovementFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_car_movement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createCar()
    }

    /**
     *  Отрисовываем машину.
     */
    private fun createCar() {
        car = View(requireContext())
        car?.apply {
            setBackgroundResource(R.drawable.taxi)
            layoutParams = FrameLayout.LayoutParams(CAR_WIDTH, CAR_HEIGHT, Gravity.CENTER)
            carContainer?.addView(car)
        }
    }

    override fun onResume() {
        super.onResume()
        carContainer?.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.performClick()
        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {

            }

            MotionEvent.ACTION_UP -> {

                if (!isCarMoving) {
                    isCarMoving = true

                    // Определяем координаты куда надо отправить машину.
                    val targetX = event.x - (car?.measuredWidth ?: 0) / 2
                    val targetY = event.y - (car?.measuredHeight ?: 0) / 2

                    defineCarPivots()

                    // Определяем текущие координаты машины.
                    val carCoordinates = IntArray(2)
                    car?.getLocationOnScreen(carCoordinates)
                    val currentCarPositionX = car?.x ?: 0f
                    val currentCarPositionY = car?.y ?: 0f

                    // Собираем в список точки, по которому будет строиться путь машины.
                    val currentPathCoordinates = arrayListOf(
                        Point(currentCarPositionX.toInt(), currentCarPositionY.toInt()),
                        Point(targetX.toInt(), targetY.toInt())
                    )
                    defineTransitionAnimation(currentPathCoordinates)
                }
            }
            else -> {

            }
        }
        return true
    }

    private fun defineCarPivots() {
        car?.pivotX = (car?.measuredWidth ?: 0) / 2f
        car?.pivotY = (car?.measuredHeight ?: 0) / 2f
    }

    /**
     *  Метод служит для перемещения машины по заданному пути.
     *
     *  [currentPathCoordinates] список точек с координатами пути.
     */
    private fun defineTransitionAnimation(currentPathCoordinates: ArrayList<Point>) {
        var startCarPosition = Point()
        var endCarPosition = Point()
        val handler = Handler()
        var indexOfStartPoint = -1
        var indexOfEndPoint = 1

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (indexOfStartPoint < currentPathCoordinates.size - 1) {
                    indexOfStartPoint++
                    indexOfEndPoint = indexOfStartPoint + 1
                }
                // Берём из списка начальную и конечную коородинаты движения машины.
                if (indexOfStartPoint < currentPathCoordinates.size - 1) {
                    startCarPosition = currentPathCoordinates[indexOfStartPoint]
                    endCarPosition = currentPathCoordinates[indexOfEndPoint]
                }

                val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                valueAnimator.apply {
                    duration = 1500
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animator ->
                        val animationFraction = animator.animatedFraction
                        val animationPositionX =
                            animationFraction * endCarPosition.x + (1 - animationFraction) * startCarPosition.x
                        val animationPositionY =
                            animationFraction * endCarPosition.y + (1 - animationFraction) * startCarPosition.y

                        // Устанавливаем новые текущие координаты машине и поворачиваем её на вычисленный угол.
                        car?.rotation = getAngleToRotate(
                            startCarPosition.x.toDouble(),
                            startCarPosition.y.toDouble(),
                            endCarPosition.x.toDouble(),
                            endCarPosition.y.toDouble()
                        )
                        car?.x = animationPositionX
                        car?.y = animationPositionY
                    }
                    addListener(object : Animator.AnimatorListener {

                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            if (indexOfStartPoint == currentPathCoordinates.size - 1) {
                                isCarMoving = false
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {

                        }
                    })
                }
                valueAnimator.start()
                if (indexOfStartPoint != currentPathCoordinates.size - 1) {
                    handler.postDelayed(this, 30)
                }
            }
        }, 30)
    }

    /** Метод рассчитывает координаты поворота машины в зависимости от того, в какой квадрант попала целевая точка.
     *  Угол считается через арктангенс отношения двух катетов (противолежащего рассчитываемому углу и прилежащего).
     *  Квадранты:
     *  4  |  1
     *  ---|---
     *  3  |  2
     * [currentX] текущая координата X машины.
     * [currentY] текущая координата Y машины.
     * [targetX] целевая координата X машины.
     * [targetY] целевая координата Y машины.
     */
    private fun getAngleToRotate(currentX: Double, currentY: Double, targetX: Double, targetY: Double): Float {
        var angleToRotate = 0.0

        val xSide = Math.abs(currentX - targetX)
        val ySide = Math.abs(currentY - targetY)


        if (currentX < targetX && currentY <= targetY) // Если target координаты находятся во втором квадранте

            angleToRotate = (90 + Math.toDegrees(Math.atan((ySide / xSide))))
        else if (currentX >= targetX && currentY <= targetY) // Если target координаты находятся в третьем квадранте

            angleToRotate = -(90 + Math.toDegrees(Math.atan((ySide / xSide))))
        else if (currentX >= targetX && currentY > targetY) // Если target координаты находятся в четвертом квадранте

            angleToRotate = -(90 - (Math.toDegrees(Math.atan((ySide / xSide)))))
        else if (currentX < targetX && currentY > targetY) // Если target координаты находятся в первом квадранте

            angleToRotate = 90 - Math.toDegrees(Math.atan((ySide / xSide)))

        Log.d("ANGLE_", angleToRotate.toString())

        return angleToRotate.toFloat()
    }

}