package com.ihs.inputmethod.home.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;

import java.util.List;


public final class HomeThemeBannerAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    private Activity activity;
    private boolean isThemeAnalyticsEnabled;
    private HomeThemeBannerAdapter adapter;

    public HomeThemeBannerAdapterDelegate(Activity activity, boolean isThemeAnalyticsEnabled) {
        this.activity = activity;
        this.isThemeAnalyticsEnabled = isThemeAnalyticsEnabled;
    }

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isThemeBanner;
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 2;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        HomeThemeBannerViewHolder viewHolder = new HomeThemeBannerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_theme_banners, parent, false));
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();

        int pageMargin = parent.getPaddingLeft() + layoutParams.leftMargin;
        int marginRight = HSDisplayUtils.dip2px(32); //右边露出部分
        int paddingLeft = pageMargin - parent.getPaddingLeft();
        int paddingRight = pageMargin + marginRight;

        int width = parent.getMeasuredWidth() - pageMargin ;
        int bannerWidth = width - marginRight - pageMargin;
        int bannerHeight = (int) (bannerWidth * (150 / 317f));
        int height = bannerHeight;

        ViewPager viewPager = viewHolder.viewPager;
        layoutParams = new RecyclerView.LayoutParams(width,height);
        viewPager.setPadding(paddingLeft,0,paddingRight,0);
        viewPager.setLayoutParams(layoutParams);
        viewPager.setPageMargin(pageMargin);

        adapter = new HomeThemeBannerAdapter(activity);
        adapter.setThemeAnalyticsEnabled(isThemeAnalyticsEnabled);
        adapter.setViewPager(viewPager);
        adapter.initData();
        viewPager.setAdapter(adapter);
        return viewHolder;
    }


    protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (adapter != null) {
            adapter.startAutoScroll(2000);
        }
    }

    protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (adapter != null) {
            adapter.stopAutoScroll();
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
    }
}
