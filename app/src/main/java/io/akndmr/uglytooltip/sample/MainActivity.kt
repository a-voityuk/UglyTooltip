package io.akndmr.uglytooltip.sample

import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import io.akndmr.ugly_tooltip.*
import io.akndmr.ugly_tooltip.R.*
import io.akndmr.uglytooltip.R

class MainActivity : AppCompatActivity() {

    var tooltipDialog: TooltipDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUglyTooltip()

        findViewById<ImageView>(R.id.iv3).setOnClickListener {
            startUglyTooltips()
        }
    }

    private fun initUglyTooltip() {
        tooltipDialog = TooltipBuilder()
            .setPackageName(packageName)
            .titleTextColorRes(R.color.black)
            .textColorRes(R.color.black)
            .shadowColorRes(color.shadow)
            .titleTextSizeRes(dimen.title_size)
            .textSizeRes(dimen.text_normal)
            .spacingRes(dimen.spacing_normal)
            .backgroundContentColorRes(color.white)
//            .circleIndicatorBackgroundDrawableRes(drawable.selector_circle)
//            .prevString(string.previous)
//            .nextString(nextStringText = "Sonraki")
            .finishString(finishStringText = "Ок")
            .nextTextColorRes(R.color.green)
            .finishTextColorRes(R.color.white)
            .finishTextSizeRes(dimen.title_size)
            .finishBackgroundColorRes(R.color.shadow)
            .finishBackgroundDrawableRes(R.drawable.bg_button_blue)
//            .useCircleIndicator(true)
            .showBottomContainer(true)
            .clickable(true)
            .useArrow(true)
            .useSkipWord(false)
            .setFragmentManager(this.supportFragmentManager)
//            .lineColorRes(color.line_color)
//            .lineWidthRes(dimen.line_width)
            .shouldShowIcons(false)
            .setTooltipRadius(dimen.tooltip_radius)
            .showSpotlight(false)
            .showViewBitmap(false)
            .build();

        tooltipDialog?.setTooltipListener(object : TooltipDialogListener {
            override
            fun onPrevious(tooltip: TooltipObject?) {
                if (tooltip != null) {

                }
            }

            override
            fun onNext(tooltip: TooltipObject?) {
                if (tooltip != null) {

                }
            }

            override
            fun onComplete(tooltip: TooltipObject?) {
                if (tooltip != null) {

                }
            }
        })
    }

    fun startUglyTooltips() {
        val tooltips: ArrayList<TooltipObject> = ArrayList()

        var view : ImageView = findViewById<ImageView>(R.id.iv3);
        val location = IntArray(2)
        view.getLocationInWindow(location)

        val height : Int = view.height
        val width : Int = view.width

        tooltips.add(
            TooltipObject(
                0,
                null,
                "Довідка",
                "Якщо у Вас виникли питання або проблеми - напишіть нам в службу підтримки",
                TooltipContentPosition.BOTTOM,
                0,
                null
            ).withCustomTarget(location, width, height, 0)
        )

//        tooltipDialog?.show(this, supportFragmentManager, "SHOWCASE_TAG", tooltips)
        tooltipDialog?.showWithCallback(this, supportFragmentManager, "", tooltips, onStep = {
            if (it == 0) {

            }
        })

    }

}