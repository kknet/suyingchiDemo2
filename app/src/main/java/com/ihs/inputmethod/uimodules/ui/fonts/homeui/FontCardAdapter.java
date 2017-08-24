package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontCardAdapter extends RecyclerView.Adapter<FontCardViewHolder> {

    private List<FontModel> fontModelList;
    private OnFontCardClickListener onFontCardClickListener;
    private int currentSelectPosition = -1;

    public FontCardAdapter(List<FontModel> fontModels, OnFontCardClickListener onFontCardClickListener) {
        this.onFontCardClickListener = onFontCardClickListener;
        this.fontModelList = fontModels;
    }

    @Override
    public FontCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        currentSelectPosition = HSSpecialCharacterManager.getCurrentSpecialCharacterIndex();
        return new FontCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_card, parent, false));
    }

    private void fontSelected(final FontModel fontModel) {
        if(fontModel.isFontDownloaded()) {
            int position = fontModelList.indexOf(fontModel);
            if (currentSelectPosition != position) {
//            holder.radioButton.setChecked(true);
                if (currentSelectPosition != -1) {
                    notifyItemChanged(currentSelectPosition, 0);
                }
                currentSelectPosition = position;
                notifyItemChanged(position, 0);
            }
        }

        if (onFontCardClickListener != null) {
            onFontCardClickListener.onFontCardClick(fontModel);
        }
    }

    @Override
    public void onBindViewHolder(final FontCardViewHolder holder, final int position) {
        if(fontModelList == null) {
            return;
        }

        final FontModel fontModel = fontModelList.get(position);
        final HSSpecialCharacter hsSpecialCharacter = fontModel.getHsSpecialCharacter();
        holder.fontCardContent.setText(hsSpecialCharacter.example);
        if(!fontModel.isFontDownloaded()) {
            holder.downloadIcon.setImageResource(R.drawable.ic_download_icon);
            holder.radioButton.setVisibility(View.GONE);
        } else {
            holder.downloadIcon.setVisibility(View.GONE);
            holder.radioButton.setVisibility(View.VISIBLE);
            holder.radioButton.setChecked(position == currentSelectPosition);
            holder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fontSelected(fontModel);
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fontSelected(fontModel);
            }
        });
    }
    
    public int getcurrentSelectPosition() {
        return currentSelectPosition;
    }

    public void setCurrentSelectPosition(int position) {
        currentSelectPosition = position;
    }

    @Override
    public int getItemCount() {
        return fontModelList.size();
    }

    public interface OnFontCardClickListener {
        void onFontCardClick(final FontModel fontModel);
    }
}
