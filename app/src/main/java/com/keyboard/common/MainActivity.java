package com.keyboard.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.accessbility.AccGALogger;
import com.ihs.inputmethod.accessbility.AccessibilityEventListener;
import com.ihs.inputmethod.accessbility.CustomViewDialog;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view.CustomProgressDrawable;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;
import com.ihs.inputmethod.utils.Constants;
import com.kc.commons.utils.KCCommonUtils;
import com.kc.utils.KCAnalytics;

import java.text.MessageFormat;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;
import static android.view.View.GONE;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_accessibility_guide_gotit_clicked;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_accessibility_guide_viewed;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_accessibility_setkey_screen_viewed;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_accessibility_setkey_success_page_viewed;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_alert_auto_setkey_showed;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_auto_setkey_clicked;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_manual_setkey_clicked;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_setting_up_page_viewed;
import static com.ihs.inputmethod.accessbility.AccGALogger.logOneTimeGA;
import static com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity.startThemeHomeActivity;


public class MainActivity extends HSAppCompatActivity {

    public final static String PREF_SHOULD_SKIP_MAINACTIVITY = "pref_theme_home_showed";

    // --Commented out by Inspection (18/1/11 下午2:41):public static final String ACTION_MAIN_ACTIVITY = HSApplication.getContext().getPackageName() + ".keyboard.main";
    private final static String INSTRUCTION_SCREEN_VIEWED = "Instruction_screen_viewed";
    private final static String APP_STEP_ONE_HINT_CLICKED = "app_step_one_hint_clicked";
    private final static String APP_STEP_ONE_HINT = "app_step_one_hint";

    private final static float BUTTON_BACKGROUND_OPACITY_DISABLED = 0.7f;
    private static final int GUIDE_DELAY = 300;

    private boolean versionFilterForRecordEvent;

    public enum CurrentUIStyle {
        UISTYLE_STEP_ONE,
        UISTYLE_STEP_TWO,
        UISTYLE_STEP_THREE_TEST,
    }

    private SharedPreferences mPrefs;
    private View bt_step_one;
    private View bt_step_two;
    private RelativeLayout accessibilityButtonContainer;
    private TextView protocolText;
    private ImageView img_enter_one;
    private ImageView img_enter_two;
    private ImageView img_choose_one;
    private ImageView img_choose_two;
    private TextView textOne;
    private TextView textTwo;
    private ImeSettingsContentObserver settingsContentObserver = new ImeSettingsContentObserver(new Handler());
    private boolean isLaunchAnimationPlayed;
    private boolean isSettingButtonAnimationPlayed;

    private static final int TYPE_MANUAL = 0;
    private static final int TYPE_AUTO = 1;
    private int currentType = TYPE_MANUAL; //根据Accessibility判断按钮内容
    private boolean shouldShowEnableDialog = false;
    private AccessibilityEventListener accessibilityEventListener;
    private int listenerKey = -1;
    private LinearLayout dialogView;
    private VideoView videoView;
    private CustomViewDialog customViewDialog;
    private boolean alertDialogShowing;
    private VideoView launchVideoView;
    private ImageView launchImageView;

    private boolean isInStepOne;
    private boolean clickStepOne;

    private CurrentUIStyle style;

    private Handler handler = new Handler();

    private ImageView ivProgress;
    private TextView tvProgress;
    private LinearLayout progressLayout;
    private boolean hasPlayed = false;


    private static final int AD_LOAD_MAX_WAIT_TIME = HSConfig.optInteger(3000, "Application", "CurrentTheme", "LaunchDelayTime");
    private static final int NAVIGATION_MAIN_PAGE = 1;
    private int progress;
    int delayTime = AD_LOAD_MAX_WAIT_TIME / 100;
    Handler progressHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NAVIGATION_MAIN_PAGE:
                    if (progress == 0) {
                        HSLog.w("cjx", "start progress");
                    }
                    progress++;
                    if (progress <= 100) {
                        ivProgress.getDrawable().setLevel(progress);
                        tvProgress.setText(MessageFormat.format("{0}%", progress));


                        if (progress == 100) {
                            removeMessages(msg.what);

                            ivProgress.setVisibility(GONE);
                            tvProgress.setVisibility(GONE);
                            img_choose_one.setVisibility(View.VISIBLE);
                            img_choose_two.setVisibility(View.VISIBLE);
                            img_enter_one.setVisibility(View.VISIBLE);
                            img_enter_two.setVisibility(View.VISIBLE);
                            textOne.setVisibility(View.VISIBLE);
                            textTwo.setVisibility(View.VISIBLE);

                            // 开始渐变动画
                            if (isAccessibilityEnable()) {
                                playAccessibilityButtonShowAnimation();
                            } else {
                                playManualButtonShowAnimation();
                            }
                        } else {
                            sendEmptyMessageDelayed(msg.what, delayTime);
                        }
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver imeChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
                if (HSInputMethodListManager.isMyInputMethodSelected()) {
                    if (currentType == TYPE_MANUAL) {
                        if (versionFilterForRecordEvent && !isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED)) {

                            if (isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)
                                    && isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)
                                    && isEventRecorded(APP_STEP_ONE_HINT_CLICKED)
                                    && isEventRecorded(APP_STEP_ONE_HINT)
                                    && isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED)) {
                                setEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
                                logEventWithSource(Constants.GA_PARAM_ACTION_APP_STEP_TWO_ENABLED);
                            }
                        }
                        MainActivity.this.doStepTwoFinish();
                        style = CurrentUIStyle.UISTYLE_STEP_THREE_TEST;
                    } else if (currentType == TYPE_AUTO) {
                        View coverView = HSFloatWindowManager.getInstance().getAccessibilityCoverView();
                        logOneTimeGA(app_setting_up_page_viewed);

                        if (coverView != null) {
                            coverView.findViewById(R.id.progressBar).setVisibility(GONE);
                            coverView.findViewById(R.id.iv_succ).setVisibility(View.VISIBLE);
                            ((TextView) coverView.findViewById(R.id.tv_settings_item)).setText(R.string.access_set_up_success);
                            logOneTimeGA(app_accessibility_setkey_success_page_viewed);
                        }
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startThemeHomeActivity(MainActivity.this);
                            }
                        }, 200);
                    } else {
                        HSLog.e("wrong Mode.");
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(null);
        super.onCreate(savedInstanceState);
        HSLog.d("MainActivity onCreate.");
        setContentView(R.layout.activity_main);
        onNewIntent(getIntent());

        ivProgress = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.progress_text);
        CustomProgressDrawable drawable = new CustomProgressDrawable(1);
        ivProgress.setImageDrawable(drawable);

        progressLayout = findViewById(R.id.loading_layout);

        img_enter_one = this.findViewById(R.id.view_enter_one);
        img_enter_two = this.findViewById(R.id.view_enter_two);
        img_choose_one = this.findViewById(R.id.view_choose_one);
        img_choose_two = this.findViewById(R.id.view_choose_two);

        textOne = findViewById(R.id.text_one);
        textTwo = findViewById(R.id.text_two);
        textOne.setText(getString(R.string.toast_enable_keyboard, getString(R.string.app_name)));
        textTwo.setText(getString(R.string.toast_select_keyboard, getString(R.string.app_name)));
        ((TextView) (findViewById(R.id.accessibility_button_container).findViewById(R.id.accessibility_text_one))).setText(getString(R.string.toast_enable_keyboard, getString(R.string.app_name)));

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        HSApplication.HSLaunchInfo firstLaunchInfo = HSApplication.getFirstLaunchInfo();
        versionFilterForRecordEvent = (firstLaunchInfo.appVersionCode >= HSApplication.getCurrentLaunchInfo().appVersionCode);

        if (versionFilterForRecordEvent && !isEventRecorded(INSTRUCTION_SCREEN_VIEWED)) {
            setEventRecorded(INSTRUCTION_SCREEN_VIEWED);
            logEventWithSource(INSTRUCTION_SCREEN_VIEWED);
        }

        WindowManager wm = this.getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        final int screenHeight = size.y;


        if (!BuildConfig.MAIN_ACTIVITY_SHOW_VIDEO_WHEN_START) {
            ImageView logoImage = findViewById(R.id.logo_image_view);
            logoImage.setVisibility(View.VISIBLE);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!shouldSkipMainActivity() && !isSettingButtonAnimationPlayed) {
                        HSFloatWindowManager.getInstance().getAccessibilityCoverView();
                        progressLayout.setVisibility(View.VISIBLE);
                        progressHandler.sendEmptyMessage(NAVIGATION_MAIN_PAGE);
                    }
                    if (isSettingButtonAnimationPlayed) {
                        HSLog.w("setting button already showed.");
                        return;
                    }
                    if (shouldSkipMainActivity() || HSInputMethodListManager.isMyInputMethodSelected()) {
                        startThemeHomeActivity(MainActivity.this);
                    }
                }
            }, 1200);

        } else {
            launchImageView = findViewById(R.id.launch_image_view);
            launchVideoView = findViewById(R.id.launch_mp4_view);
            Uri uri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.launch_page_mp4_animation);
            launchVideoView.setVideoURI(uri);
            launchVideoView.setZOrderOnTop(true);
            launchVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    HSLog.e("MainActivity mp4 play error: what = " + what + " extra = " + extra);
                    startThemeHomeActivity(MainActivity.this);
                    return true;
                }
            });

            launchVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (Build.VERSION.SDK_INT == 19) {
                        if (mp != null) {
                            mp.setDisplay(null);
                            mp.reset();
                            mp.setDisplay(launchVideoView.getHolder());
                        }
                    }

                    if (!shouldSkipMainActivity() && !isSettingButtonAnimationPlayed) {
                        progressLayout.setVisibility(View.VISIBLE);
                        progressHandler.sendEmptyMessage(NAVIGATION_MAIN_PAGE);
                        hasPlayed = true;
                    }
                    if (isSettingButtonAnimationPlayed) {
                        HSLog.w("setting button already showed.");
                        return;
                    }
                    if (shouldSkipMainActivity() || HSInputMethodListManager.isMyInputMethodSelected()) {
                        startThemeHomeActivity(MainActivity.this);
                    }
                }
            });

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isLaunchAnimationPlayed) {
                        Log.w("cjx", "start play anim");
                        isLaunchAnimationPlayed = true;
                        launchImageView.setVisibility(GONE);
                        launchVideoView.setVisibility(View.VISIBLE);
                        launchVideoView.start();
                        if (!shouldSkipMainActivity()) {
                            HSFloatWindowManager.getInstance().getAccessibilityCoverView();
                            if (!isSettingButtonAnimationPlayed) {
                                if (!hasPlayed) {
                                    progressLayout.setVisibility(View.VISIBLE);
                                    progressHandler.sendEmptyMessage(NAVIGATION_MAIN_PAGE);
                                }
                                HSLog.w("show setting button in abnormal way");
                            }
                        }
                    }
                }
            }, 1200);

        }

        protocolText = findViewById(R.id.privacy_policy_text);
        String serviceKeyText = getString(R.string.text_terms_of_service);
        String policyKeyText = getString(R.string.text_privacy_policy);
        String policyText = getResources().getString(R.string.keyboard_guide_privacy_policy, serviceKeyText, policyKeyText);
        SpannableString ss = new SpannableString(policyText);
        ss.setSpan(new URLSpan(HSConfig.optString("", "Application", "Policy", "TermsOfService")) {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.privacy_stress_text));
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }
        }, policyText.indexOf(serviceKeyText), policyText.indexOf(serviceKeyText) + serviceKeyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new URLSpan(HSConfig.optString("", "Application", "Policy", "PrivacyPolicy")) {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.privacy_stress_text));
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }
        }, policyText.indexOf(policyKeyText), policyText.indexOf(policyKeyText) + policyKeyText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        protocolText.setText(ss);
        protocolText.setMovementMethod(LinkMovementMethod.getInstance());

        bt_step_one = this.findViewById(R.id.bt_step_one);
        bt_step_two = this.findViewById(R.id.bt_step_two);
        accessibilityButtonContainer = findViewById(R.id.accessibility_button_container);

        bt_step_one.setBackgroundDrawable(getGuideBtnBg());
        bt_step_two.setBackgroundDrawable(getGuideBtnBg());
        accessibilityButtonContainer.setBackgroundDrawable(getGuideBtnBg());

        RelativeLayout.LayoutParams stepOneLayoutParams = (RelativeLayout.LayoutParams) bt_step_one.getLayoutParams();
        RelativeLayout.LayoutParams stepTwoLayoutParams = (RelativeLayout.LayoutParams) bt_step_two.getLayoutParams();
        RelativeLayout.LayoutParams accessibilityLayoutParams = (RelativeLayout.LayoutParams) accessibilityButtonContainer.getLayoutParams();

        if (getResources().getBoolean(R.bool.isTablet)) {
            stepOneLayoutParams.width = (int) (screenWidth * 0.45f);
            stepTwoLayoutParams.width = (int) (screenWidth * 0.45f);
            accessibilityLayoutParams.width = (int) (screenWidth * 0.45f);

            Paint p1 = new Paint();
            p1.setTextSize(getResources().getDimension(R.dimen.main_logo_title_textsize));
            Rect result = new Rect();
            p1.getTextBounds("RainBowKey", 0, "RainBowKey".length(), result);
        } else {
            stepOneLayoutParams.width = (int) (screenWidth * 0.75f);
            stepTwoLayoutParams.width = (int) (screenWidth * 0.75f);
            accessibilityLayoutParams.width = (int) (screenWidth * 0.75f);
        }


        bt_step_one.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboardEnableDialog();
                if (versionFilterForRecordEvent && !isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)) {
                    setEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED);
                    logEventWithSource(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED);
                }
            }
        });
        bt_step_two.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager m = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                m.showInputMethodPicker();
                Toast toast = Toast.makeText(MainActivity.this, getString(R.string.toast_select_keyboard, getString(R.string.app_name)), Toast.LENGTH_LONG);
                toast.show();

                if (versionFilterForRecordEvent && !isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED)) {


                    //记第二步点击的时候，如果还没有记第一步点击或第一步enable, 就补上

                    if (!isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_CLICKED)) {
                        return;
                    }

                    if (!isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)) {
                        return;
                    }

                    if (!isEventRecorded(APP_STEP_ONE_HINT_CLICKED) || !isEventRecorded(APP_STEP_ONE_HINT)) {
                        return;
                    }

                    setEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED);
                    logEventWithSource(Constants.GA_PARAM_ACTION_APP_STEP_TWO_CLICKED);
                }
            }
        });

        accessibilityButtonContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                logOneTimeGA(app_auto_setkey_clicked);

                if (HSAccessibilityService.isAvailable()) {
                    try {
                        accessibilityEventListener.onAvailable();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    autoSetupKeyboard();
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        registerReceiver(imeChangeReceiver, filter);

        refreshUIState();
    }

    private Drawable getGuideBtnBg() {
        if (BuildConfig.BASS_PRODUCT) {
            return RippleDrawableUtils.getCompatGradientRippleDrawableContainDisableStatus(0xff8f56ff, 0xffae6cff, getResources().getColor(R.color.guide_bg_disable_color),
                    getResources().getDimension(R.dimen.guide_bg_radius));
        } else {
            return RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(getResources().getColor(R.color.guide_bg_normal_color), getResources().getColor(R.color.guide_bg_disable_color),
                    getResources().getDimension(R.dimen.guide_bg_radius));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        HSLog.d("MainActivity onNewIntent.");
        try {
            if (getIntent().getBooleanExtra("isInStepOne", false)) {
                isInStepOne = true;
            }
        } catch (Exception e) {
            return;
        }


        if (getIntent().getBooleanExtra("skip", false) && !isInStepOne && !HSInputMethodListManager.isMyInputMethodEnabled()) {
            clickStepOne = true;
        }
    }

    private boolean isAccessibilityEnable() {
        Log.e("access", "accessi read");
        boolean isAccessibilityEnabledInConfig = HSConfig.optBoolean(false, "Application", "AutoSetKeyEnable");
        boolean isHSAccessibilityServiceAvailable = HSAccessibilityService.isAvailable();
        boolean isSDKSatisfied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
        boolean oneTapPageViewed = AccGALogger.isOneTapPageViewed();
        return isAccessibilityEnabledInConfig && !isHSAccessibilityServiceAvailable && !oneTapPageViewed && isSDKSatisfied;
    }

    private void autoSetupKeyboard() { //播放自动设置键盘动画的Dialog
        shouldShowEnableDialog = true;

        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, 100);

        if (dialogView == null) {
            dialogView = (LinearLayout) View.inflate(getApplicationContext(), R.layout.dialog_enable_accessbility_guide, null);
            videoView = dialogView.findViewById(R.id.videoview_guide);
            ((TextView) dialogView.findViewById(R.id.alertTitle)).setText(getString(R.string.alter_enable_access_title, getString(R.string.app_name)));
            ((TextView) dialogView.findViewById(R.id.message)).setText(getString(R.string.alter_enable_access_content, getString(R.string.app_name)));
            Uri uri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.accesibility_guide);
            videoView.setVideoURI(uri);
            videoView.setZOrderOnTop(true);

            customViewDialog = new CustomViewDialog(HSApplication.getContext());
            customViewDialog.setContentView(dialogView);
            customViewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (videoView != null) {
                        videoView.stopPlayback();
                    }
                }
            });
//            customViewDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    int width = dialogView.getMeasuredWidth();
//                    if (width != videoView.getMeasuredWidth()) {
//                        videoView.setViewSize(width, width * 588 / 948);
//                    }
//                    dialogView.forceLayout();
//                }
//            });

            dialogView.findViewById(R.id.tv_confirm).setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.selector_keyactive_enable));
            dialogView.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logOneTimeGA(app_accessibility_guide_gotit_clicked);
                    KCCommonUtils.dismissDialog(customViewDialog);
                }
            });
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                videoView.start();
                logOneTimeGA(app_accessibility_guide_viewed);
                KCCommonUtils.showDialog(customViewDialog);
            }
        }, GUIDE_DELAY);
    }

    /**
     * Show keyboard enabling dialog
     */
    private void showKeyboardEnableDialog() {
        if (!isFinishing()) {
            CustomDesignAlert dialog = new CustomDesignAlert(this);
            dialog.setTitle(getString(R.string.toast_enable_keyboard, getString(R.string.app_name)));
            dialog.setMessage(getString(R.string.alert_attention_messenger, getString(R.string.app_name)));
            dialog.setImageResource(R.drawable.enable_keyboard_alert_top_bg);
            dialog.setPositiveButton(getString(R.string.got_it), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (versionFilterForRecordEvent && !isEventRecorded(APP_STEP_ONE_HINT_CLICKED)) {
                        setEventRecorded(APP_STEP_ONE_HINT_CLICKED);
                        logEventWithSource(APP_STEP_ONE_HINT_CLICKED);
                    }

                    ImageView imageCodeProject = new ImageView(getApplicationContext());
                    imageCodeProject.setBackgroundResource(com.ihs.inputmethod.uimodules.R.drawable.toast_enable_rain);
                    final KeyboardActivationGuideActivity.CustomViewDialog customViewDialog = new KeyboardActivationGuideActivity.CustomViewDialog(imageCodeProject, 3000, Gravity.BOTTOM, 0, HSDisplayUtils.dip2px(20));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            KCCommonUtils.showDialog(customViewDialog);
                        }
                    }, 500);

                    Intent intent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
                    intent.setFlags(FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    isInStepOne = true;
                }
            });
            KCCommonUtils.showDialog(dialog);
        }

        if (versionFilterForRecordEvent && !isEventRecorded(APP_STEP_ONE_HINT)) {
            setEventRecorded(APP_STEP_ONE_HINT);
            logEventWithSource(APP_STEP_ONE_HINT);
        }
    }

    public class ImeSettingsContentObserver extends ContentObserver {

        public ImeSettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (HSInputMethodListManager.isMyInputMethodEnabled()) {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                i.putExtra("isInStepOne", true);
                startActivity(i);
                try {
                    if (settingsContentObserver != null) {
                        getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
                        HSLog.d("unregister settings content observer");
                    }
                } catch (IllegalArgumentException ex) {
                    HSLog.e("content observer not registered yet");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        HSLog.d("MainActivity onResume.");

        if (currentType == TYPE_MANUAL) {
            if (!HSInputMethodListManager.isMyInputMethodEnabled()) {
                getApplicationContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS), false,
                        settingsContentObserver);
                refreshUIState();
            } else {
                if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                    if (isInStepOne) {
                        doStepOneFinish();
                        style = CurrentUIStyle.UISTYLE_STEP_TWO;
                        if (versionFilterForRecordEvent && !isEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED)) {
                            setEventRecorded(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED);
                            logEventWithSource(Constants.GA_PARAM_ACTION_APP_STEP_ONE_ENABLED);
                        }
                    } else {
                        refreshUIState();
                    }
                } else {
                    refreshUIState();
                }
                try {
                    if (settingsContentObserver != null)
                        getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
                } catch (IllegalArgumentException ex) {
                    HSLog.e("content observer not registered yet");
                }
            }
            isInStepOne = false;
            if (clickStepOne) {
                bt_step_one.performClick();
                clickStepOne = false;
            }
        } else {
            showChooseManualAlertIfNecessary();
        }
    }


    private void showChooseManualAlertIfNecessary() {
        if (!alertDialogShowing && shouldShowEnableDialog && !HSAccessibilityService.isAvailable() && !isFinishing()) {
            AlertDialog.Builder alertDialogBuilder;
            alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompactDialogStyle);
            alertDialogBuilder.setTitle(getString(R.string.alert_enable_access_warn_title));//设置标题
            alertDialogBuilder.setMessage(getString(R.string.alert_enable_access_warn_content, getString(R.string.app_name)));//设置显示文本
            alertDialogBuilder.setPositiveButton(getString(R.string.alert_enable_access_warn_confirm), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logOneTimeGA(app_manual_setkey_clicked);
                    KCCommonUtils.dismissDialog((Dialog) dialog);
                    playManualButtonShowAnimation();
                }
            });
            alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    alertDialogShowing = false;
                }
            });

            alertDialogBuilder.show();
            alertDialogShowing = true;

            logOneTimeGA(app_alert_auto_setkey_showed);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        HSLog.d("MainActivity onStop.");
        if (BuildConfig.MAIN_ACTIVITY_SHOW_VIDEO_WHEN_START) {
            launchVideoView.stopPlayback();
            launchImageView.setVisibility(View.VISIBLE);
            launchVideoView.setVisibility(GONE);
        }
    }

    //NOTE: fix a VideoView memory leak on Android 5-7:
    // https://stackoverflow.com/questions/43280440/videoview-memory-leak
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase) {
            @Override
            public Object getSystemService(String name) {
                if (Context.AUDIO_SERVICE.equals(name)) {
                    return getApplicationContext().getSystemService(name);
                } else {
                    return super.getSystemService(name);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSLog.d("MainActivity onDestroy.");
        HSPreferenceHelper.getDefault().putBoolean(PREF_SHOULD_SKIP_MAINACTIVITY, true);
        try {
            if (settingsContentObserver != null) {
                getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
            }
            unregisterReceiver(imeChangeReceiver);
        } catch (IllegalArgumentException ex) {
            HSLog.e("content observer not registered yet");
        }

        if (videoView != null) {
            try {
                videoView.stopPlayback();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                videoView = null;
            }
        }
        if (launchVideoView != null) {
            try {
                launchVideoView.stopPlayback();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                launchVideoView = null;
            }
        }

        if (accessibilityEventListener != null) {
            accessibilityEventListener.onDestroy();
        }
        HSAccessibilityService.unregisterEvent(listenerKey);
        handler.removeCallbacksAndMessages(null);

        KCCommonUtils.fixSystemLeaks(this);
    }

    private void refreshUIState() {
        if (!HSInputMethodListManager.isMyInputMethodEnabled()) {
            if (style == CurrentUIStyle.UISTYLE_STEP_ONE)
                return;

            bt_step_one.setEnabled(true);
            bt_step_one.setAlpha(1.0f);
            bt_step_two.setEnabled(false);
            bt_step_two.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);

            img_enter_one.setAlpha(1f);
            img_enter_two.setAlpha(1f);
            img_choose_one.setAlpha(0f);
            img_choose_two.setAlpha(0f);

            style = CurrentUIStyle.UISTYLE_STEP_ONE;
        } else if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            if (style == CurrentUIStyle.UISTYLE_STEP_TWO)
                return;

            bt_step_one.setClickable(false);
            if (isInStepOne) {
                bt_step_one.setAlpha(1.0f);
                bt_step_one.setEnabled(false);
                bt_step_two.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
            } else {
                bt_step_one.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
                bt_step_one.setEnabled(false);
                bt_step_two.setAlpha(1.0f);
                bt_step_two.setEnabled(true);
            }

            img_enter_one.setAlpha(0f);
            img_enter_two.setAlpha(1f);
            img_choose_one.setAlpha(1f);
            img_choose_two.setAlpha(0f);

            style = CurrentUIStyle.UISTYLE_STEP_TWO;
        }
    }

    private void playManualButtonShowAnimation() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(bt_step_one, "alpha", 0f, 1.0f),
                ObjectAnimator.ofFloat(bt_step_two, "alpha", 0f, 1.0f),
                ObjectAnimator.ofFloat(protocolText, "alpha", 0f, 1.0f)
        );
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                isSettingButtonAnimationPlayed = true;
                currentType = TYPE_MANUAL;
                bt_step_one.setVisibility(View.VISIBLE);
                bt_step_two.setVisibility(View.VISIBLE);
                accessibilityButtonContainer.setVisibility(GONE);
                protocolText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        set.setDuration(500).start();
    }

    private void playAccessibilityButtonShowAnimation() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(accessibilityButtonContainer, "alpha", 0f, 1.0f),
                ObjectAnimator.ofFloat(protocolText, "alpha", 0f, 1.0f)
        );
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                accessibilityEventListener = new AccessibilityEventListener(AccessibilityEventListener.MODE_SETUP_KEYBOARD);
                listenerKey = HSAccessibilityService.registerEventListener(accessibilityEventListener);
                isSettingButtonAnimationPlayed = true;
                currentType = TYPE_AUTO;
                bt_step_one.setVisibility(View.GONE);
                bt_step_two.setVisibility(View.GONE);
                accessibilityButtonContainer.setVisibility(View.VISIBLE);
                protocolText.setVisibility(View.VISIBLE);
                logOneTimeGA(app_accessibility_setkey_screen_viewed);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        set.setDuration(500).start();
    }

    private void doStepOneFinish() {
        bt_step_one.setClickable(false);
        bt_step_one.setEnabled(false);
        img_enter_one.setAlpha(0f);
        img_choose_one.setVisibility(View.VISIBLE);
        img_choose_one.setAlpha(1f);
        bt_step_two.setAlpha(1.0f);
        bt_step_two.setEnabled(true);
    }

    private void doStepTwoFinish() {
        bt_step_one.setClickable(false);
        bt_step_two.setClickable(false);
        bt_step_one.setAlpha(BUTTON_BACKGROUND_OPACITY_DISABLED);
        img_enter_two.setAlpha(0f);
        img_choose_two.setVisibility(View.VISIBLE);
        img_choose_two.setAlpha(1f);
        startThemeHomeActivity(MainActivity.this);
    }

    private boolean isEventRecorded(String pref_name) {
        return mPrefs.getBoolean(pref_name, false);
    }

    private void setEventRecorded(String pref_name) {
        mPrefs.edit().putBoolean(pref_name, true).apply();
    }

    public static boolean shouldSkipMainActivity() {
        return HSPreferenceHelper.getDefault().getBoolean(PREF_SHOULD_SKIP_MAINACTIVITY, false);
    }

    private void logEventWithSource(String eventName) {
        KCAnalytics.logEvent(eventName, "source", getSourceString());
    }

    private String getSourceString() {
        switch (currentType) {
            case TYPE_AUTO:
                return "auto";
            default:
                return "manual";
        }
    }
}
