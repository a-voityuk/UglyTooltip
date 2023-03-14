package io.akndmr.ugly_tooltip


/**
 * Created by Akın DEMİR on 2.06.2021.
 * Copyright (c) 2021
 */


interface TooltipDialogListener {
    fun onNext(tooltip: TooltipObject?)
    fun onPrevious(tooltip: TooltipObject?)
    fun onComplete(tooltip: TooltipObject?)
}