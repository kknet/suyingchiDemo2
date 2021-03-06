package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;

/**
 * Created by liuzhongtao on 17/7/18.
 *
 */

public class NewTipLayout extends FrameLayout {
    private View newTipView;
    private int marginLeft;
    private int marginTop;
    private int marginRight;
    private int marginBottom;

    public NewTipLayout(@NonNull Context context) {
        super(context);
    }

    public NewTipLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NewTipLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void showNewTip() {
//        if (newTipView == null) {
//            newTipView = new View(HSApplication.getContext());
//            GradientDrawable redPointDrawable = new GradientDrawable();
//            redPointDrawable.setColor(Color.RED);
//            redPointDrawable.setShape(GradientDrawable.OVAL);
//            newTipView.setBackgroundDrawable(redPointDrawable);
//
//            int width = HSDisplayUtils.dip2px(7);
//            int height = HSDisplayUtils.dip2px(7);
//            LayoutParams layoutParams = new LayoutParams(width, height);
//            layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
//            newTipView.setLayoutParams(layoutParams);
//            addView(newTipView);
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void hideNewTip() {
//        if (newTipView != null) {
//            removeView(newTipView);
//            newTipView.setVisibility(GONE);
//            newTipView = null;
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void setNewTipMargin(int left, int top, int right, int bottom) {
//        marginLeft = left;
//        marginTop = top;
//        marginRight = right;
//        marginBottom = bottom;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}
