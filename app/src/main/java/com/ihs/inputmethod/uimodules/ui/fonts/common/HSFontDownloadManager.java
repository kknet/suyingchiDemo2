package com.ihs.inputmethod.uimodules.ui.fonts.common;

import android.os.Build;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontModel;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.kc.commons.configfile.KCList;
import com.kc.commons.configfile.KCParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by guonan.lv on 17/8/21.
 */

public class HSFontDownloadManager {
    public static final String JSON_SUFFIX = ".json";
    public static final String ASSETS_FONT_FILE_PATH = "Fonts";
    private static final String DOWNLOADED_FONT_NAME_JOIN = "download_font_name_join";
    private static HSFontDownloadManager instance;

    private HSFontDownloadManager() {
        loadDownloadedFont();
    }

    public static HSFontDownloadManager getInstance() {
        if (instance == null) {
            synchronized (HSFontDownloadManager.class) {
                if (instance == null) {
                    instance = new HSFontDownloadManager();
                }
            }
        }
        return instance;
    }

    private void loadDownloadedFont() {
        File file = new File(getDownloadedFontNameList());
        KCList kcList = KCParser.parseList(file);
        if (kcList == null) {
            return;
        }
        for (int i = kcList.size(); i > 0; i--) { //order of adding opposite to the order of download
            String downloadFontName = kcList.getString(i);
            HSSpecialCharacter downloadSpecialCharacter = readSpecialCharacterFromFile(downloadFontName);
            if (downloadSpecialCharacter != null && !HSSpecialCharacterManager.getSpecialCharacterList().isEmpty()) {
                HSSpecialCharacterManager.addSpecilCharacter(1, downloadSpecialCharacter, Build.VERSION_CODES.ICE_CREAM_SANDWICH);
            }
        }
    }

    public String getFontDownloadFilePath(String fontName) {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_FONT_FILE_PATH + "/" + fontName + JSON_SUFFIX;
    }

    public String getDownloadedFontNameList() {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_FONT_FILE_PATH + "/" + DOWNLOADED_FONT_NAME_JOIN + JSON_SUFFIX;
    }

    public void updateFontModel(FontModel fontModel) {
        HSSpecialCharacter hsSpecialCharacter = readSpecialCharacterFromFile(fontModel.getFontName());
        if (hsSpecialCharacter == null) {
            return;
        }
        updateSpecialCharacterList(hsSpecialCharacter);
    }

    public void updateSpecialCharacterList(HSSpecialCharacter hsSpecialCharacter) {
        HSSpecialCharacterManager.addSpecilCharacter(1, hsSpecialCharacter, Build.VERSION_CODES.ICE_CREAM_SANDWICH);

        DownloadUtils.getInstance().writeJsonToFile(hsSpecialCharacter.name, getDownloadedFontNameList());
    }


    private HSSpecialCharacter readSpecialCharacterFromFile(String fontName) {
        HSSpecialCharacter hsNewSpecialCharacter = new HSSpecialCharacter();
        JSONObject supportFont = new JSONObject();
        String fontFileName = getFontDownloadFilePath(fontName);
        if (fontFileName != null) {
            supportFont = readJsonFromFile(fontFileName);
        }
        try {
            hsNewSpecialCharacter.name = fontName;
            hsNewSpecialCharacter.example = supportFont.getString("FontExample");
            hsNewSpecialCharacter.mapNormalToFont = supportFont.getJSONObject("Content");
            return hsNewSpecialCharacter;
        } catch (JSONException eJson) {
            eJson.printStackTrace();
            return null;
        } finally {

        }
    }

    private JSONObject readJsonFromFile(String fileName) {
        JSONObject jsonObject = new JSONObject();
        BufferedInputStream bufferedInputStream = null;
        File jsonFile = new File(fileName);
        int size = (int) jsonFile.length();
        byte[] jsonByte = new byte[size];
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(jsonFile));
            bufferedInputStream.read(jsonByte, 0, jsonByte.length);
            try {
                jsonObject = (new JSONObject(new String(jsonByte)));

            } catch (JSONException eJson) {
                eJson.printStackTrace();
            }
        } catch (IOException eIO) {
            eIO.printStackTrace();
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return jsonObject;
        }
    }
}
