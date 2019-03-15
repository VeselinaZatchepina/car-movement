package com.github.veselinazatchepina.carmovement

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_car_movement.*
import android.graphics.*
import android.view.animation.LinearInterpolator
import android.animation.Animator
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator


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
                    val currentCarPositionX = (car?.x ?: 0f).toInt() + (car?.measuredWidth ?: 0) / 2
                    val currentCarPositionY = (car?.y ?: 0f).toInt() + (car?.measuredHeight ?: 0) / 2

                    // Собираем в список точки, по которому будет строиться путь машины.
                    val currentPathCoordinates = arrayListOf(
                        Point(currentCarPositionX, currentCarPositionY),
                        Point(targetX.toInt(), targetY.toInt())
                    )

                    // Создаем анимацию поворта машины на нужный угол.
                    car?.animate()
                        ?.rotation(
                            getAngleToRotate(
                                currentCarPositionX.toDouble(),
                                currentCarPositionY.toDouble(),
                                targetX.toDouble(),
                                targetY.toDouble()
                            )
                        )
                        ?.setDuration(1000)
                        ?.setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                defineTransitionAnimation(currentPathCoordinates)
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {

                            }
                        })
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
        // Опрделяем текущик координаты машины.
        val currentCarPositionX = (car?.x ?: 0f).toInt()
        val currentCarPositionY = (car?.y ?: 0f).toInt()

        // Запускаем анимацию перемещения машины для каждой точки пути.
        for (point in currentPathCoordinates) {
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animator ->
                    val animationFraction = animator.animatedFraction
                    val animationPositionX = animationFraction * point.x + (1 - animationFraction) * currentCarPositionX
                    val animationPositionY = animationFraction * point.y + (1 - animationFraction) * currentCarPositionY
                    // Устанавливаем новые текущие координаты машине.
                    car?.x = animationPositionX
                    car?.y = animationPositionY
                }
                addListener(object : Animator.AnimatorListener {

                    override fun onAnimationRepeat(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                       /* val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.taxi)
                         mediaPlayer.start()*/
                        // Теперь можем прослушивать нажатия на экран.
                        isCarMoving = false
                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationStart(animation: Animator?) {

                    }

                })
            }
            valueAnimator.start()
        }
    }

    /** Метод рассчитывает координаты поворота машины в зависимости от того, в какой квадрант попала целевая точка.
     *  Угол считается через арктангенс отношения двух катетов (противолежащего рассчитываемому углу и прилежащего).
     *  Квадранты:
     *  4  |   1
     *  ---|-----
     *  3  |   2
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