package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;

public class StickerMoreComingAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).isMoreComing;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_more_coming, parent, false)){};
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 6;
    }
}
