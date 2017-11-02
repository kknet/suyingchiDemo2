package com.ihs.inputmethod.uimodules.ui.facemoji;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.inputmethod.api.utils.HSNetworkConnectionUtils;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FacemojiDownloadManager {
    private static class FacemojiDownloadManagerHoler {
        static FacemojiDownloadManager instance = new FacemojiDownloadManager();
    }

    private String remoteBasePath;
    private List<String> downloadedFacemojiCateogryNameList = new ArrayList<>();

    public static FacemojiDownloadManager getInstance() {
        return FacemojiDownloadManagerHoler.instance;
    }

    private FacemojiDownloadManager() {
        remoteBasePath = HSConfig.optString("", "Application", "DownloadServer", "FacemojiBasePath");
    }

    public void onConfigChange() {
        remoteBasePath = HSConfig.optString("", "Application", "DownloadServer", "FacemojiBasePath");
    }


    private String getRemoteResourcePath(String name) {
        return remoteBasePath + name + "/" + name + ".zip";
    }

    public String getRemoteTabIconPath(String name) {
        return remoteBasePath + name + "/" + "pack_" + name + ".png";
    }

    public void startDownloadFacemojiResource(FacemojiCategory facemojiCategory) {
        if (!HSNetworkConnectionUtils.isNetworkConnected()) {
            return;
        }
        if (downloadedFacemojiCateogryNameList.contains(facemojiCategory.getName())){
            return;
        }
        downloadedFacemojiCateogryNameList.add(facemojiCategory.getName());

        HSHttpConnection connection = new HSHttpConnection(getRemoteResourcePath(facemojiCategory.getName()));
        File downloadFile = FacemojiManager.getFacemojiZipFile(facemojiCategory);
        connection.setDownloadFile(downloadFile);
        connection.setConnectTimeout(1000 * 60);
        connection.setReadTimeout(1000 * 60 * 10);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (downloadFile.exists() && downloadFile.length() > 0) {
                    boolean unzipResult = FacemojiManager.unzipFacemojiCategory(downloadFile);
                    if (unzipResult) {
                        FacemojiManager.getInstance().setDownloadedFacemojiCategoryUnzipSuccess(facemojiCategory);
                        downloadFile.delete();
                    }
                }
                downloadedFacemojiCateogryNameList.remove(facemojiCategory.getName());
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                downloadedFacemojiCateogryNameList.remove(facemojiCategory.getName());
            }
        });
        UIController.getInstance().getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                connection.startAsync();
            }
        });
    }
}