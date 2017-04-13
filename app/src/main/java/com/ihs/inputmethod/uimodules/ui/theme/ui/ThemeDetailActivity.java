package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acb.adadapter.AcbInterstitialAd;
import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSInstallationUtils;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSImageLoader;
import com.ihs.inputmethod.api.utils.HSNetworkConnectionUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.theme.download.ApkUtils;
import com.ihs.inputmethod.theme.download.ThemeDownloadManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.CommonThemeCardAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.ihs.inputmethod.uimodules.ui.theme.utils.ThemeMenuUtils;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;
import com.ihs.inputmethod.uimodules.widget.MdProgressBar;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jixiang on 16/8/24.
 */
public class ThemeDetailActivity extends HSAppCompatActivity implements View.OnClickListener, CommonThemeCardAdapter.ThemeCardItemClickListener, TrialKeyboardDialog.OnTrialKeyboardStateChanged {
    private NestedScrollView rootView;
    private View screenshotContainer;
    private ImageView keyboardThemeScreenShotImageView;
    private MdProgressBar screenshotLoading;
    //    private TextView themeNameText;
//    private TextView themeDescText;
    private TextView leftBtn;
    private TextView rightBtn;
    private RecyclerView recommendRecyclerView;
    private CommonThemeCardAdapter themeCardAdapter;
    private TrialKeyboardDialog trialKeyboardDialog;
    private List<View> nativeAdAlreadyLoadedList;

    private String themeName;
    private HSKeyboardTheme.ThemeType themeType;
    private HSKeyboardTheme keyboardTheme;
    private NativeAdView nativeAdView;

    public final static String INTENT_KEY_THEME_NAME = "themeName";

    private static final int keyboardActivationFromDetail = 6;
    private static final int MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR = 1;
    private static final int MSG_CHANGE_AD_BUTTON_BACKGROUND_ORIGEN_COLOR = 2;

    private Handler handler = new Handler() {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            NativeAdView nativeAdView = (NativeAdView) msg.obj;
            switch (msg.what) {
                case MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR:
                    if (null != nativeAdView) {
                        TextView adButtonView = (TextView) nativeAdView.findViewById(R.id.ad_call_to_action);
                        if (null != adButtonView) {
                            adButtonView.getBackground().setColorFilter(HSApplication.getContext().getResources().getColor(R.color.ad_button_green_state), PorterDuff.Mode.SRC_ATOP);
                        }
                    } else {
                        // do nothing
                    }
                    break;
                case MSG_CHANGE_AD_BUTTON_BACKGROUND_ORIGEN_COLOR:
                    if (null != nativeAdView) {
                        TextView adButtonView = (TextView) nativeAdView.findViewById(R.id.ad_call_to_action);
                        if (null != adButtonView) {
                            adButtonView.getBackground().setColorFilter(HSApplication.getContext().getResources().getColor(R.color.ad_button_blue), PorterDuff.Mode.SRC_ATOP);
                        }
                    } else {
                        // do nothing
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getStringExtra(INTENT_KEY_THEME_NAME) != null) {
            themeName = intent.getStringExtra(INTENT_KEY_THEME_NAME);
        }
        if (themeName != null) {
            List<HSKeyboardTheme> allKeyboardThemeList = HSKeyboardThemeManager.getAllKeyboardThemeList();
            for (HSKeyboardTheme keyboardTheme : allKeyboardThemeList) {
                if (themeName.equals(keyboardTheme.mThemeName)) {
                    this.keyboardTheme = keyboardTheme;
                    this.themeType = keyboardTheme.getThemeType();
                    break;
                }
            }
        }

        if (keyboardTheme != null) {

            if (keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.CUSTOM) {
                screenshotContainer.getLayoutParams().height = (int) (getResources().getDisplayMetrics().widthPixels * (HSResourceUtils.getDefaultKeyboardHeight(getResources()) * 1.0f / HSResourceUtils.getDefaultKeyboardWidth(getResources())));
                getSupportActionBar().setTitle(R.string.theme_detail_custom_theme_title_name);
//                themeNameText.setVisibility(View.GONE);
//                themeDescText.setVisibility(View.GONE);
                keyboardThemeScreenShotImageView.setImageURI(Uri.fromFile(new File(HSKeyboardThemeManager.getKeyboardThemeScreenshotFile(keyboardTheme.mThemeName))));
            } else {
                screenshotContainer.getLayoutParams().height = (int) (getResources().getDisplayMetrics().widthPixels * 850 * 1.0f / 1080);
                String themeNameTitle = keyboardTheme.getThemeShowName();
                getSupportActionBar().setTitle(R.string.theme_detail_common_title_name);
//                themeNameText.setVisibility(View.VISIBLE);
//                themeDescText.setVisibility(View.VISIBLE);
//                themeNameText.setText(themeNameTitle);
//                themeDescText.setText(keyboardTheme.getThemeDescription());

                if (keyboardTheme.getLargePreivewImgUrl() != null) {
                    keyboardThemeScreenShotImageView.setImageDrawable(null);
                    HSImageLoader.getInstance().displayImage(keyboardTheme.getLargePreivewImgUrl(), keyboardThemeScreenShotImageView, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build()
                            , new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                    screenshotLoading.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    if(isCurrentImageUri(imageUri)) {
                                        screenshotLoading.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    if(isCurrentImageUri(imageUri)) {
                                        screenshotLoading.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onLoadingCancelled(String imageUri, View view) {
                                    if(isCurrentImageUri(imageUri)) {
                                        screenshotLoading.setVisibility(View.GONE);
                                    }
                                }

                                public boolean isCurrentImageUri(String imageUri){
                                    if(keyboardTheme!=null && keyboardTheme.getLargePreivewImgUrl()!=null && keyboardTheme.getLargePreivewImgUrl().equals(imageUri)){
                                        return true;
                                    }
                                    return false;
                                }
                            }
                    );

                }
            }

            setButtonText();

        }

        String text = rightBtn.getText().toString();
        boolean applied = text.equalsIgnoreCase(getString(R.string.theme_card_menu_applied));
        if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled() && !applied && themeName != null) {
            ThemeAnalyticsReporter.getInstance().recordThemeShownInDetailActivity(themeName);
        }
        //show all themes except custom themes and current theme
        themeCardAdapter.setItems(getKeyboardThemesExceptMe());
        themeCardAdapter.notifyDataSetChanged();

        rootView.smoothScrollTo(0, 0);
    }

    @NonNull
    private List<ThemeHomeModel> getKeyboardThemesExceptMe() {
        List<HSKeyboardTheme> keyboardThemeList = new ArrayList<>();
        keyboardThemeList.addAll(HSKeyboardThemeManager.getAllKeyboardThemeList());
        keyboardThemeList.removeAll(KCCustomThemeManager.getInstance().getAllCustomThemes());
        keyboardThemeList.removeAll(HSKeyboardThemeManager.getDownloadedThemeList());
        if (keyboardTheme != null) {
            keyboardThemeList.remove(keyboardTheme);
        }
        List<ThemeHomeModel> models=new ArrayList<>();
        for(HSKeyboardTheme keyboardTheme:keyboardThemeList){
            ThemeHomeModel model=new ThemeHomeModel();
            model.keyboardTheme=keyboardTheme;
            models.add(model);
        }
        return models;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_detail);
        nativeAdAlreadyLoadedList = new ArrayList<>();
        initView();
        onNewIntent(getIntent());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        HSGlobalNotificationCenter.addObserver(com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.NOTIFICATION_SHOW_TRIAL_KEYBOARD, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
    }

    private void initView() {
        rootView = (NestedScrollView) findViewById(R.id.root_view);
        screenshotContainer = findViewById(R.id.keyboard_theme_screenshot_container);
        keyboardThemeScreenShotImageView = (ImageView) findViewById(R.id.keyboard_theme_screenshot);
//        themeNameText = (TextView) findViewById(R.id.theme_name);
//        themeDescText = (TextView) findViewById(R.id.theme_desc);
        screenshotLoading = (MdProgressBar) findViewById(R.id.screenshot_loading);
        leftBtn = (TextView) findViewById(R.id.theme_detail_left_btn);
        rightBtn = (TextView) findViewById(R.id.theme_detail_right_btn);
        leftBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);

        recommendRecyclerView = (RecyclerView) findViewById(R.id.theme_detail_recommend_recycler_view);
        recommendRecyclerView.setNestedScrollingEnabled(false);
        recommendRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        themeCardAdapter = new CommonThemeCardAdapter(this,this,false);
//        themeCardAdapter.setThemeCardItemClickListener(this);
        recommendRecyclerView.setAdapter(themeCardAdapter);

        addNativeAdView();
    }


    private void addNativeAdView() {
        if(!IAPManager.getManager().hasPurchaseNoAds()) {
            // 添加广告
            if (nativeAdView == null) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ad_container);
                if (HSNetworkConnectionUtils.isNetworkConnected()) {
                    int width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
                    View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_1, null);
                    LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
                    LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f));
                    loadingView.setLayoutParams(loadingLP);
                    loadingView.setGravity(Gravity.CENTER);
                    nativeAdView = new NativeAdView(HSApplication.getContext(), view, loadingView);
                    nativeAdView.setOnAdLoadedListener(new NativeAdView.OnAdLoadedListener() {
                        @Override
                        public void onAdLoaded(NativeAdView nativeAdView) {
                            if (!nativeAdAlreadyLoadedList.contains(nativeAdView)) {
                                nativeAdAlreadyLoadedList.add(nativeAdView);
                            } else {
                                // do nothing
                            }
                            Message message = Message.obtain();
                            message.obj = nativeAdView;
                            message.what = MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR;
                            handler.sendMessageDelayed(message, 1500);
                        }
                    });
                    nativeAdView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                        @Override
                        public void onScrollChanged() {
                            int[] locationCoordinate = new int[2];
                            nativeAdView.getLocationOnScreen(locationCoordinate);
                            Message message = Message.obtain();
                            message.obj = nativeAdView;
                            if (nativeAdAlreadyLoadedList.contains(nativeAdView)) {
                                if (locationCoordinate[1] < HSDisplayUtils.getScreenHeightForContent() && locationCoordinate[1] > (0 - nativeAdView.getHeight())) { //在屏幕内
                                    message.what = MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR;
                                    handler.sendMessageDelayed(message, 1500);
                                }
                                if (locationCoordinate[1] < (0 - nativeAdView.getHeight()) || locationCoordinate[1] > HSDisplayUtils.getScreenHeightForContent()) { //在屏幕外
                                    handler.removeMessages(MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR);
                                    message.what = MSG_CHANGE_AD_BUTTON_BACKGROUND_ORIGEN_COLOR;
                                    handler.sendMessage(message);
                                }
                            } else {
                                // do nothing
                            }

                        }
                    });
                    nativeAdView.configParams(new NativeAdParams(HSApplication.getContext().getString(R.string.ad_placement_themedetailad), width, 1.9f));
                    CardView cardView = ViewConvertor.toCardView(nativeAdView);
                    linearLayout.addView(cardView);
                } else
                    linearLayout.setVisibility(View.GONE);
            }
        }
    }

    private long currentResumeTime;

    @Override
    protected void onResume() {
        super.onResume();
        currentResumeTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        long time = (System.currentTimeMillis() - currentResumeTime) / 1000;
        HSLog.e("app_theme_preview_show_time : " + time);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_theme_preview_show_time", time);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (homeKeyTracker.isHomeKeyPressed() && trialKeyboardDialog != null && trialKeyboardDialog.isShowing()) {
            trialKeyboardDialog.dismiss();
        }
    }

    private void setButtonText() {
        switch (themeType) {
            case NEED_DOWNLOAD:
                leftBtn.setText(R.string.theme_card_menu_share);
                if(ThemeDownloadManager.getInstance().isDownloading(keyboardTheme.mThemeName)) {
                    rightBtn.setText(HSApplication.getContext().getString(R.string.theme_card_menu_downloading));
                    rightBtn.setEnabled(false);
                }else {
                    rightBtn.setText(HSApplication.getContext().getString(R.string.theme_card_menu_download));
                    rightBtn.setEnabled(true);
                }
                break;
            case CUSTOM:
            case DOWNLOADED:
            case BUILD_IN:
                if (themeName != null && HSKeyboardThemeManager.getCurrentTheme()!=null && themeName.equals(HSKeyboardThemeManager.getCurrentTheme().mThemeName)) {
                    rightBtn.setText(R.string.theme_card_menu_applied);
                    rightBtn.setEnabled(false);
                } else {
                    rightBtn.setText(R.string.theme_card_menu_apply);
                    rightBtn.setEnabled(true);
                }
                leftBtn.setText(R.string.theme_card_menu_share);
                break;
        }
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.theme_detail_left_btn) {
            handleButtonClick(v);
        } else if (id == R.id.theme_detail_right_btn) {
            handleButtonClick(v);
        }
    }

    private void handleButtonClick(View v) {
        if (keyboardTheme == null) {
            return;
        }

        String text = ((TextView) v).getText().toString();
        if (HSApplication.getContext().getString(R.string.theme_card_menu_download).equalsIgnoreCase(text)) {
            ((TextView)v).setText(R.string.theme_card_menu_downloading);
            v.setEnabled(false);
            ThemeDownloadManager.getInstance().downloadTheme(keyboardTheme);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("themedetails_download_clicked", themeName);
            if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                ThemeAnalyticsReporter.getInstance().recordThemeDownloadInDetailActivity(themeName);
            }
        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_delete).equalsIgnoreCase(text)) {
            KCCustomThemeManager.getInstance().removeCustomTheme(keyboardTheme.getThemeId());
        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_share).equalsIgnoreCase(text)) {
            ThemeMenuUtils.shareTheme(this, keyboardTheme);
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("themedetails_share_clicked", themeName);
        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_apply).equalsIgnoreCase(text)) {
            if(keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.DOWNLOADED && !HSInstallationUtils.isAppInstalled(keyboardTheme.getThemePkName())) {
                ApkUtils.startInstall(HSApplication.getContext(),Uri.fromFile(new File(ThemeDownloadManager.getThemeDownloadLocalFile(keyboardTheme.mThemeName))));
            }else {
                if (HSKeyboardThemeManager.setKeyboardTheme(themeName)) {
                    showTrialKeyboardDialog(keyboardActivationFromDetail);
                } else {
                    String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                    HSToastUtils.toastCenterLong(String.format(failedString, keyboardTheme.getThemeShowName()));
                }
            }
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("themedetails_apply_clicked", themeName);
            if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                ThemeAnalyticsReporter.getInstance().recordThemeApplyInDetailActivity(themeName);
            }
        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_applied).equalsIgnoreCase(text)) {
        }
    }

    private void showTrialKeyboardDialog(int activationCode) {
        if (trialKeyboardDialog == null) {
            trialKeyboardDialog = new TrialKeyboardDialog.Build(ThemeDetailActivity.class.getName()).create(this, this);

        }
        trialKeyboardDialog.show(this,activationCode);
        trialKeyboardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                showInterstitialAds();
            }
        });
    }

    private void showInterstitialAds() {
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(getResources().getString(R.string.ga_fullscreen_theme_apply_load_ad));
            List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), getResources().getString(R.string.placement_full_screen_trial_keyboard), 1);
            if (interstitialAds.size() > 0) {
                final AcbInterstitialAd interstitialAd = interstitialAds.get(0);
                interstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                    long adDisplayTime = -1;

                    @Override
                    public void onAdDisplayed() {
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(getResources().getString(R.string.ga_fullscreen_theme_apply_show_ad));
                        adDisplayTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onAdClicked() {
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(getResources().getString(R.string.ga_fullscreen_theme_apply_click_ad));
                    }

                    @Override
                    public void onAdClosed() {
                        long duration = System.currentTimeMillis() - adDisplayTime;
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(getResources().getString(R.string.ga_fullscreen_theme_apply_display_ad), String.format("%fs", duration / 1000f));
                        interstitialAd.release();
                    }
                });
                interstitialAd.show();
            } else {
                showChargingEnableAlert();
            }
    }

    private void showChargingEnableAlert() {
        if (ChargingConfigManager.getManager().shouldShowEnableChargingAlert(false)) {
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_InterstitialRequestFailedAlert_prompt_show");
            CustomDesignAlert dialog = new CustomDesignAlert(HSApplication.getContext());
            dialog.setTitle(getString(R.string.charging_alert_title));
            dialog.setMessage(getString(R.string.charging_alert_message));
            dialog.setImageResource(R.drawable.enable_charging_alert_top_image);
            dialog.setCancelable(true);
            dialog.setPositiveButton(getString(R.string.enable), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChargingManagerUtil.enableCharging(false);
                    HSToastUtils.toastCenterShort(getString(R.string.charging_enable_toast));
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_InterstitialRequestFailedAlert_prompt_click");
                }
            });
            dialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.NOTIFICATION_SHOW_TRIAL_KEYBOARD.equals(s)) {
                if (hsBundle != null) {
                    String showTrialKeyboardActivityName = hsBundle.getString(TrialKeyboardDialog.BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY, "");
                    int activationCode = hsBundle.getInt(KeyboardActivationProcessor.BUNDLE_ACTIVATION_CODE);
                    if (ThemeDetailActivity.class.getSimpleName().equals(showTrialKeyboardActivityName)) {
                        showTrialKeyboardDialog(activationCode);
                    }
                }
            } else if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(s)) {
                updateCurrentThemeStatus();
            }
        }
    };

    /**
     * 更改当前主题状态,有可能当前主题对应的主题包APK被安装或者删除了
     */
    private void updateCurrentThemeStatus() {
        if (themeName != null) {
            if (themeType == HSKeyboardTheme.ThemeType.DOWNLOADED) {
                //查找是否当前显示的主题是否被删了,需要重新下载
                boolean isNeedDownload = false;
                List<HSKeyboardTheme> needDownloadThemes = HSKeyboardThemeManager.getNeedDownloadThemeList();
                for (HSKeyboardTheme keyboardTheme : needDownloadThemes) {
                    if (themeName.equals(keyboardTheme.mThemeName)) {
                        isNeedDownload = true;
                        break;
                    }
                }

                //如果需要重新需要下载,修改类型并修改按钮文字
                if (isNeedDownload) {
                    themeType = HSKeyboardTheme.ThemeType.NEED_DOWNLOAD;
                    setButtonText();
                }
            } else if (themeType == HSKeyboardTheme.ThemeType.NEED_DOWNLOAD) {
                //查找是否当前显示的主题是否已下载
                boolean isDownloaded = false;
                List<HSKeyboardTheme> downloadedThemes = HSKeyboardThemeManager.getDownloadedThemeList();
                for (HSKeyboardTheme keyboardTheme : downloadedThemes) {
                    if (themeName.equals(keyboardTheme.mThemeName)) {
                        isDownloaded = true;
                        break;
                    }
                }
                //如果已下载,修改类型并修改按钮文字
                if (isDownloaded) {
                    themeType = HSKeyboardTheme.ThemeType.DOWNLOADED;
                    setButtonText();
                }
            }
        }
        if (themeCardAdapter != null) {
            themeCardAdapter.setItems(getKeyboardThemesExceptMe());
            themeCardAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        if (nativeAdView != null) {
            nativeAdView.release();
            nativeAdView = null;
        }
        handler.removeMessages(MSG_CHANGE_AD_BUTTON_BACKGROUND_NEW_COLOR);
        handler.removeMessages(MSG_CHANGE_AD_BUTTON_BACKGROUND_ORIGEN_COLOR);
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        if (trialKeyboardDialog != null) {
            trialKeyboardDialog.dismiss();
            trialKeyboardDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCardClick(HSKeyboardTheme keyboardTheme) {
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("themedetails_themes_preview_clicked", keyboardTheme.mThemeName);
    }

    @Override
    public void onMenuApplyClick(HSKeyboardTheme keyboardTheme) {
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("themedetails_themes_apply_clicked", keyboardTheme.mThemeName);
        if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
            ThemeAnalyticsReporter.getInstance().recordThemeApplyInDetailActivity(keyboardTheme.mThemeName);
        }
    }

    @Override
    public void onMenuShareClick(HSKeyboardTheme keyboardTheme) {
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("themedetails_themes_share_clicked", keyboardTheme.mThemeName);
    }

    @Override
    public void onMenuDownloadClick(HSKeyboardTheme keyboardTheme) {
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("themedetails_themes_download_clicked", keyboardTheme.mThemeName);
        if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
            ThemeAnalyticsReporter.getInstance().recordThemeDownloadInDetailActivity(keyboardTheme.mThemeName);
        }
    }

    @Override
    public void onMenuDeleteClick(HSKeyboardTheme keyboardTheme) {

    }

    @Override
    public void onMenuAppliedClick(HSKeyboardTheme keyboardTheme) {

    }

    @Override
    public void onTrialKeyShow(int requestCode) {
        if (keyboardActivationFromDetail == requestCode || requestCode == ThemeMenuUtils.keyboardActivationFromAdapter) {
            if (themeName != null && HSKeyboardThemeManager.getCurrentTheme()!=null && themeName.equals(HSKeyboardThemeManager.getCurrentTheme().mThemeName)) {
                rightBtn.setText(R.string.theme_card_menu_applied);
                rightBtn.setEnabled(false);
            } else {
                rightBtn.setText(R.string.theme_card_menu_apply);
                rightBtn.setEnabled(true);
            }
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("themedetails_apply_clicked", themeName);
        }

        switch (requestCode){
            case keyboardActivationFromDetail:
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed","apply");
                break;
            case ThemeMenuUtils.keyboardActivationFromAdapter:
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed","apply");
                break;
        }
    }

    @Override
    public void onTrailKeyPrevented() {

    }

}