package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.MyFacemojiActivity;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDownloadManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager.STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION;
import static com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils.STICKER_DOWNLOAD_ZIP_SUFFIX;

/**
 * Created by guonan.lv on 17/8/10.
 */

public class StickerHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeStickerAdapter stickerCardAdapter;
    private List<StickerHomeModel> stickerModelList = new ArrayList<>();

    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (CameraActivity.FACEMOJI_SAVED.equals(s) || StickerDataManager.STICKER_DATA_LOAD_FINISH_NOTIFICATION.equals(s)
                    || (CameraActivity.FACE_DELETED.equals(s) && FacemojiManager.getDefaultFacePicUri() == null) /** face被删除光了，才重新加载页面数据 */ ){
                loadDatas();
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //随便设置一个，修复按Home键会crash的问题，方法来自https://stackoverflow.com/questions/14516804/nullpointerexception-android-support-v4-app-fragmentmanagerimpl-savefragmentbasi
        outState.putString("xxx",  "xxx");
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sticker, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        initView();
        HSGlobalNotificationCenter.addObserver(CameraActivity.FACEMOJI_SAVED, observer);
        HSGlobalNotificationCenter.addObserver(StickerDataManager.STICKER_DATA_LOAD_FINISH_NOTIFICATION, observer);
        HSGlobalNotificationCenter.addObserver(CameraActivity.FACE_DELETED, observer);
        return view;
    }

    private void initView() {
        stickerCardAdapter = new HomeStickerAdapter(new CommonStickerAdapter.OnStickerItemClickListener() {
            @Override
            public void onFacemojiClick(FacemojiSticker facemojiSticker) {
                goMyFacemojiActivity(facemojiSticker.getCategoryName());
            }

            @Override
            public void onCardClick(StickerHomeModel stickerHomeModel, Drawable drawable) {
                HSAnalytics.logEvent(stickerHomeModel.stickerGroup.getStickerGroupName(), "sticker_download_clicked");
                onDownloadClick(stickerHomeModel, drawable);
            }

            @Override
            public void onDownloadClick(final StickerHomeModel stickerHomeModel, Drawable drawable) {
                final StickerGroup stickerGroup = stickerHomeModel.stickerGroup;
                final String stickerGroupName =stickerGroup.getStickerGroupName();
                final String stickerGroupDownloadedFilePath = StickerUtils.getStickerFolderPath(stickerGroupName) + STICKER_DOWNLOAD_ZIP_SUFFIX;

                // 移除点击过的new角标
                StickerDataManager.getInstance().removeNewTipOfStickerGroup(stickerGroup);
                stickerCardAdapter.notifyItemChanged(stickerModelList.indexOf(stickerHomeModel));


                DownloadUtils.getInstance().startForegroundDownloading(HSApplication.getContext(), stickerGroupName,
                        stickerGroupDownloadedFilePath, stickerGroup.getStickerGroupDownloadUri(),
                        new BitmapDrawable(ImageLoader.getInstance().loadImageSync(stickerGroup.getStickerGroupDownloadPreviewImageUri())), new AdLoadingView.OnAdBufferingListener() {
                            @Override
                            public void onDismiss(boolean success) {
                                if (success) {
                                    HSAnalytics.logEvent("sticker_download_succeed", "StickerGroupName", stickerGroupName);
                                    StickerDownloadManager.getInstance().unzipStickerGroup(stickerGroupDownloadedFilePath, stickerGroup);

                                    int position = stickerModelList.indexOf(stickerHomeModel);
                                    if (position > 0 && position < stickerModelList.size()) {
                                        stickerModelList.remove(position);
                                        stickerCardAdapter.notifyItemRemoved(position);
                                    }
                                }
                            }

                        });
            }

        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 6);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return stickerCardAdapter.getSpanSize(position);
            }
        });
        recyclerView.setAdapter(stickerCardAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        loadDatas();
    }

    private void loadDatas() {
        stickerModelList.clear();

        StickerHomeModel stickerHomeModel = new StickerHomeModel();
        stickerHomeModel.isTitle = true;
        if (FacemojiManager.getDefaultFacePicUri() != null){
            stickerHomeModel.title = HSApplication.getContext().getResources().getString(R.string.sticker_title_my_facemojis);
            stickerHomeModel.rightButton = HSApplication.getContext().getResources().getString(R.string.theme_store_more);
            stickerHomeModel.titleClickable = true;
            stickerHomeModel.titleClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goMyFacemojiActivity(null);
                }
            };
            stickerModelList.add(stickerHomeModel);

            stickerHomeModel = new StickerHomeModel();
            stickerHomeModel.isSmallCreateFacemoji = true;
            stickerModelList.add(stickerHomeModel);

            stickerHomeModel = new StickerHomeModel();
            stickerHomeModel.isFacemoji = true;
            stickerHomeModel.facemojiSticker = FacemojiManager.getInstance().getStickerList(0).get(0);
            stickerModelList.add(stickerHomeModel);
            if (FacemojiManager.getFaceList().size() >= 2){
                stickerHomeModel = new StickerHomeModel();
                stickerHomeModel.isFacemoji = true;
                stickerHomeModel.facemojiSticker = FacemojiManager.getInstance().getStickerList(1).get(0);
                stickerModelList.add(stickerHomeModel);
            }
        }else {
            stickerHomeModel.title = HSApplication.getContext().getResources().getString(R.string.sticker_title_create_my_facemojis);
            stickerHomeModel.titleClickable = false;
            stickerModelList.add(stickerHomeModel);

            stickerHomeModel = new StickerHomeModel();
            stickerHomeModel.isBigCreateFacemoji = true;
            stickerModelList.add(stickerHomeModel);
        }

        stickerHomeModel = new StickerHomeModel();
        stickerHomeModel.isTitle = true;
        stickerHomeModel.title = HSApplication.getContext().getResources().getString(R.string.sticker_title_funny_stickers);
        stickerModelList.add(stickerHomeModel);

        List<StickerGroup> stickerGroupList = StickerDataManager.getInstance().getStickerGroupList();
        for (StickerGroup stickerGroup : stickerGroupList) {
            if (!stickerGroup.isStickerGroupDownloaded()) {
                stickerHomeModel = new StickerHomeModel();
                stickerHomeModel.stickerGroup = stickerGroup;
                stickerModelList.add(stickerHomeModel);
            }
        }

        stickerHomeModel = new StickerHomeModel();
        stickerHomeModel.isMoreComing = true;
        stickerModelList.add(stickerHomeModel);

        stickerCardAdapter.setItems(stickerModelList);
        stickerCardAdapter.notifyDataSetChanged();
    }

    private void goMyFacemojiActivity(String categoryName){
        Intent intent = new Intent(getActivity(), MyFacemojiActivity.class);
        if(!TextUtils.isEmpty(categoryName)){
            intent.putExtra(MyFacemojiActivity.INIT_SHOW_TAB_CATEGORY,categoryName);
        }
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION, observer);
    }

}
