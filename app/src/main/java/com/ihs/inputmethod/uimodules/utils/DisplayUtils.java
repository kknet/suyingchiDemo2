package com.ihs.inputmethod.uimodules.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.Window;

/**
 * Created by xu.zhang on 3/5/16.
 */
public class DisplayUtils {

    public static int getScreenWidthForContent(){
       return Resources.getSystem().getDisplayMetrics().widthPixels;


    }

    public static int getScreenHeightForContent(){
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getStatusBarHeight(Window window){
        Rect rectangle = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarTop = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return Math.abs(statusBarTop - contentViewTop);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 将px值转换为dip或dp值，保证尺寸大小不变
//     *
//     * @param pxValue
//     * @param scale
//     *            （DisplayMetrics类中属性density）
//     * @return
//     */
//    public static int px2dip(Context context, float pxValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (pxValue / scale + 0.5f);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @param scale
     *            （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 将px值转换为sp值，保证文字大小不变
//     *
//     * @param pxValue
//     * @param fontScale
//     *            （DisplayMetrics类中属性scaledDensity）
//     * @return
//     */
//    public static int px2sp(Context context, float pxValue) {
//        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
//        return (int) (pxValue / fontScale + 0.5f);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 将sp值转换为px值，保证文字大小不变
//     *
//     * @param spValue
//     * @param fontScale
//     *            （DisplayMetrics类中属性scaledDensity）
//     * @return
//     */
//    public static int sp2px(Context context, float spValue) {
//        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
//        return (int) (spValue * fontScale + 0.5f);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}
