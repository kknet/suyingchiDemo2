package com.ihs.inputmethod.uimodules.softgame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.*;
import android.support.v7.widget.DividerItemDecoration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import softgames.de.softgamesapilib.GameLoadedCallback;
import softgames.de.softgamesapilib.SoftgamesSDK;
import softgames.de.softgamesapilib.SoftgamesSearchConfig;


public class SoftGameDisplayActivity extends HSAppCompatActivity implements SoftGameItemAdapter.OnSoftGameItemClickListener {

    public static final String SOFT_GAME_PLACEMENT_MESSAGE = "soft_game_placement_msg";
    public static final int SOFT_GAME_LOAD_COUNT = 50;

    private ArrayList<SoftGameDisplayItem> softGameDisplayItemArrayList = new ArrayList<>();

    private RecyclerView recyclerView;
    private SoftGameItemAdapter softGameItemAdapter;
    private NativeAdView nativeAdView;
    private ProgressBar progressBar;
    private String placementName;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Hot Games");
        setContentView(R.layout.activity_soft_game_display);
        Intent intent = getIntent();
        placementName = intent.getStringExtra(SOFT_GAME_PLACEMENT_MESSAGE);

        progressBar = (ProgressBar) findViewById(R.id.soft_game_progress_bar);
        recyclerView = (RecyclerView) findViewById(R.id.soft_game_main_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(HSApplication.getContext(), LinearLayoutManager.VERTICAL, false));
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            initNativeAdView();
        }
        softGameItemAdapter = new SoftGameItemAdapter(softGameDisplayItemArrayList, this, nativeAdView);
        recyclerView.setAdapter(softGameItemAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        final SoftgamesSearchConfig sgConfig = new SoftgamesSearchConfig(SoftGameManager.getPartnerId());
        sgConfig.setSortByPopularity(true);
        sgConfig.setLimit(SOFT_GAME_LOAD_COUNT);

        final WeakReference<SoftGameDisplayActivity> weakThis = new WeakReference<>(this);
        SoftgamesSDK.loadGamesInfo(sgConfig, new GameLoadedCallback() {
            @Override
            public void onGamesLoaded(JSONObject[] jsonObjects) {
                HSLog.d("softGames loaded.");
                SoftGameDisplayActivity activity = weakThis.get();
                if (activity == null) {
                    return;
                }
                for (JSONObject jsonObject : jsonObjects) {
                    SoftGameDisplayItem softGameDisplayItem = new SoftGameDisplayItem(jsonObject);
                    activity.softGameDisplayItemArrayList.add(softGameDisplayItem);
                }
                if (activity.handler != null) {
                    activity.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (weakThis.get() != null) {
                                weakThis.get().progressBar.setVisibility(View.GONE);
                                weakThis.get().softGameItemAdapter.refreshData(softGameDisplayItemArrayList);
                            }
                        }
                    });
                }
            }
        });
    }

    private void initNativeAdView() {
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_theme_card, null);
        LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
        int width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
        LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f));
        loadingView.setLayoutParams(loadingLP);
        loadingView.setGravity(Gravity.CENTER);
        nativeAdView = new NativeAdView(HSApplication.getContext(), view, loadingView);
        nativeAdView.configParams(new NativeAdParams(placementName, width, 1.9f));
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (nativeAdView != null) {
            nativeAdView.release();
            nativeAdView = null;
        }
        super.onDestroy();
    }

    @Override
    public void OnSoftGameItemClick(SoftGameDisplayItem softGameDisplayItem) {
        HSAnalytics.logEvent("game_play_clicked", "game_play_clicked", softGameDisplayItem.getTitle());
        SoftgamesSDK.openGame(softGameDisplayItem.getJsonObject(), SoftGameDisplayActivity.this, SoftGameManager.getPartnerId(), null);
    }
}
