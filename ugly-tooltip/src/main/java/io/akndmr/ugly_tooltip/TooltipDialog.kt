package io.akndmr.ugly_tooltip

import android.R
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ScrollView
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


/**
 * Created by Akın DEMİR on 2.06.2021.
 * Copyright (c) 2021
 */


class TooltipDialog : DialogFragment() {

    val DELAY_SCROLLING = 350
    val LOG_TAG: String = TooltipDialog::class.java.simpleName
    val MAX_RETRY_LAYOUT = 3

    private var tutorsList: ArrayList<TooltipObject>? = null
    private var currentTutorIndex = -1
    private var builder: TooltipBuilder? = null
    private var dialogTag: String? = null

    var hasViewGroupHandled = false
    private var mFragmentManager: FragmentManager? = null

    private var retryCounter = 0

    // listener
    private var tooltipListener: TooltipDialogListener? = null

    companion object {
        private val ARG_BUILDER = "BUILDER"

        fun newInstance(builder: TooltipBuilder?): TooltipDialog? {
            val args = Bundle()
            val fragment = TooltipDialog()
            args.putParcelable(ARG_BUILDER, builder)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getArgs(arguments)
    }

    private fun getArgs(args: Bundle?) {
        builder = args!![ARG_BUILDER] as TooltipBuilder?
    }

    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog =
            object : Dialog(requireContext(), io.akndmr.ugly_tooltip.R.style.UglyTooltip) {
                override
                fun onBackPressed() {
                    if (builder != null) {
                        if (builder!!.isClickable()) {
                            previous()
                        }
                    }
                }
            }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = TooltipLayout(requireActivity(), builder)
        initViews(view as TooltipLayout)
        return view
    }

    private fun initViews(view: TooltipLayout) {
        view.setTooltipListener(object : TooltipListener {
            override
            fun onPrevious() {
                if (tooltipListener != null) {
                    val tooltipObject: TooltipObject = tutorsList?.get(currentTutorIndex) as TooltipObject

                    this@TooltipDialog.tooltipListener!!.onPrevious(tooltipObject)
                }

                previous()
            }

            override
            fun onNext() {
                if (tooltipListener != null) {
                    val tooltipObject: TooltipObject = tutorsList?.get(currentTutorIndex) as TooltipObject

                    this@TooltipDialog.tooltipListener!!.onNext(tooltipObject)
                }

                next()
            }

            override
            fun onComplete() {
                if (tooltipListener != null) {
                    val tooltipObject: TooltipObject = tutorsList?.get(currentTutorIndex) as TooltipObject

                    this@TooltipDialog.tooltipListener!!.onComplete(tooltipObject)
                }

                if (!TextUtils.isEmpty(dialogTag)) {
                    TooltipPreference.setShown(requireContext(), dialogTag, true)
                }

                this@TooltipDialog.close()
            }
        })

        if (builder != null) {
            isCancelable = builder!!.isClickable()
        }
    }

    fun setTooltipListener(showCaseListener: TooltipDialogListener?) {
        this.tooltipListener = showCaseListener
    }

    operator fun next() {
        if (currentTutorIndex + 1 >= tutorsList!!.size) {
            close()
        } else {
            if (tutorsList != null) {
                this@TooltipDialog.show(
                    activity,
                    mFragmentManager!!,
                    dialogTag,
                    tutorsList!!,
                    currentTutorIndex + 1
                )
            }
        }
    }

    fun previous() {
        if (currentTutorIndex - 1 < 0) {
            currentTutorIndex = 0
        } else {
            if (tutorsList != null) {
                this@TooltipDialog.show(
                    activity,
                    mFragmentManager!!,
                    dialogTag,
                    tutorsList!!,
                    currentTutorIndex - 1
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val window: Window? = dialog!!.window
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.transparent)
            window.setDimAmount(0f)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    fun hasShown(activity: Activity, tag: String): Boolean {
        return TooltipPreference.hasShown(requireActivity(), tag)
    }

    fun show(
        activity: Activity?,
        fm: FragmentManager,
        sharedPrefTag: String? = null,
        tutorList: ArrayList<TooltipObject>
    ) {
        mFragmentManager = fm
        show(activity, fm, sharedPrefTag, tutorList, 0)
    }

    fun showWithCallback(
        activity: Activity?,
        fm: FragmentManager,
        sharedPrefTag: String? = null,
        tutorList: ArrayList<TooltipObject>,
        onStep: (Int) -> Unit
    ) {
        mFragmentManager = fm
        show(activity, fm, sharedPrefTag, tutorList, 0, onStep)
    }

    private fun show(
        activity: Activity?,
        fm: FragmentManager,
        sharedPrefTag: String? = null,
        tutorList: ArrayList<TooltipObject>,
        index: Int,
        onStep: ((Int) -> Unit)? = null
    ) {
        if (activity == null || activity.isFinishing) {
            return
        }

        var indexToShow = index

        try {
            tutorsList = tutorList
            this.dialogTag = sharedPrefTag

            if (indexToShow < 0 || indexToShow >= tutorList.size) {
                indexToShow = 0
            }

            val previousIndex = currentTutorIndex
            currentTutorIndex = indexToShow
            hasViewGroupHandled = false

            onStep?.invoke(currentTutorIndex)

            if (currentTutorIndex == tutorList.lastIndex + 1) {
                hasViewGroupHandled = true
            }

            // has been handled by listener
            if (hasViewGroupHandled) {
                return
            }

            val tooltipObject: TooltipObject = tutorList[currentTutorIndex]
            val viewGroup: ViewGroup? = tooltipObject.scrollView

            if (viewGroup != null) {
                val viewToFocus: View? = tooltipObject.view

                hasViewGroupHandled = if (viewToFocus != null) {
                    hideLayout()

                    viewGroup.post {
                        if (viewGroup is ScrollView) {
                            val scrollView = viewGroup
                            val relativeLocation = IntArray(2)
                            TooltipViewHelper.getRelativePositionRec(
                                viewToFocus,
                                viewGroup,
                                relativeLocation
                            )
                            scrollView.smoothScrollTo(0, relativeLocation[1])
                            scrollView.postDelayed(
                                {
                                    showLayout(activity, fm, tooltipObject)
                                },
                                DELAY_SCROLLING.toLong()
                            )
                        } else if (viewGroup is NestedScrollView) {
                            val scrollView = viewGroup
                            val relativeLocation = IntArray(2)
                            TooltipViewHelper.getRelativePositionRec(
                                viewToFocus,
                                viewGroup,
                                relativeLocation
                            )
                            scrollView.smoothScrollTo(0, relativeLocation[1])
                            scrollView.postDelayed(
                                {
                                    showLayout(activity, fm, tooltipObject)
                                },
                                DELAY_SCROLLING.toLong()
                            )
                        }
                    }
                    true
                } else {
                    false
                }
            }

            if (!hasViewGroupHandled) {
                showLayout(activity, fm, tutorsList!![currentTutorIndex])
            }
        } catch (e: Exception) {
            // to Handle the unknown exception.
            // Since this only for first guide, if any error appears, just don't show the guide
            Log.e(LOG_TAG, e.stackTraceToString())

            try {
                this@TooltipDialog.dismiss()
            } catch (e2: Exception) {
                // no op
                Log.e(LOG_TAG, e2.stackTraceToString())
            }
        }
    }

    fun showLayout(activity: Activity?, fm: FragmentManager?, tooltipObject: TooltipObject) {
        if (activity == null || activity.isFinishing) {
            return
        }

        //val fm: FragmentManager? = childFragmentManager
        if (!isVisible) {
            try {
                if (fm != null) {
                    if (!isAdded) {
                        show(fm, LOG_TAG)
                    } else if (isHidden) {
                        val ft: FragmentTransaction = fm.beginTransaction()
                        ft.show(this@TooltipDialog)
                        ft.commit()
                    }
                }
            } catch (e: IllegalStateException) {
                // called in illegal state. just return.
                return
            }
        }

        val view: View? = tooltipObject.view
        val title: String? = tooltipObject.title
        val text: String? = tooltipObject.text
        val tooltipContentPosition: TooltipContentPosition =
            tooltipObject.tooltipContentPosition
        val tintBackgroundColor: Int = tooltipObject.tintBackgroundColor
        val location: IntArray? = tooltipObject.getLocation()
        val radius: Int = tooltipObject.getRadius()
        val viewWidth: Int = tooltipObject.getViewWidth()
        val viewHeight: Int = tooltipObject.getViewHeight()

        if (view == null) {
            layoutShowTutorial(
                null, title, text, tooltipContentPosition,
                tintBackgroundColor, location, radius, viewWidth, viewHeight
            )
        } else {
            view.post(Runnable {
                layoutShowTutorial(
                    view, title, text, tooltipContentPosition,
                    tintBackgroundColor, location, radius, viewWidth, viewHeight
                )
            })
        }
    }

    fun hideLayout() {
        var layout: TooltipLayout? = null//this@TooltipDialog.view as TooltipLayout

        if (this@TooltipDialog.view != null) {
            layout = this@TooltipDialog.view as TooltipLayout
        } else {
            return;
        }

        layout.hideTutorial()
    }

    private fun layoutShowTutorial(
        view: View?,
        title: String?,
        text: String?,
        showCaseContentPosition: TooltipContentPosition,
        tintBackgroundColor: Int,
        customTarget: IntArray?,
        radius: Int,
        viewWidth: Int,
        viewHeight: Int
    ) {
        try {
            var layout: TooltipLayout? = null//this@TooltipDialog.view as TooltipLayout

            if (this@TooltipDialog.view != null) {
                layout = this@TooltipDialog.view as TooltipLayout
            }

            if (layout == null) {
                if (retryCounter >= MAX_RETRY_LAYOUT) {
                    retryCounter = 0
                    return
                }

                // wait until the layout is ready, and call itself
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    retryCounter++

                    layoutShowTutorial(
                        view, title, text,
                        showCaseContentPosition, tintBackgroundColor, customTarget, radius, viewWidth, viewHeight
                    )
                }, 1000)
                return
            }

            retryCounter = 0

            layout.showTutorial(
                view, title, text, currentTutorIndex, tutorsList!!.size,
                showCaseContentPosition, tintBackgroundColor, customTarget, radius, viewWidth, viewHeight
            )
        } catch (t: Throwable) {
            // do nothing
        }
    }

    fun close() {
        try {
            dismiss()

            var layout: TooltipLayout? = null//this@TooltipDialog.view as TooltipLayout

            if (this@TooltipDialog.view != null) {
                layout = this@TooltipDialog.view as TooltipLayout
            } else {
                return;
            }

            layout.closeTutorial()
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.stackTraceToString())
        }
    }
}