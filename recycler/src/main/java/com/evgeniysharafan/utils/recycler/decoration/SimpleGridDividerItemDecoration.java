package com.evgeniysharafan.utils.recycler.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SimpleGridDividerItemDecoration extends RecyclerView.ItemDecoration {

    private int itemOffset;

    public SimpleGridDividerItemDecoration(int itemOffset) {
        this.itemOffset = itemOffset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(itemOffset, itemOffset, itemOffset, itemOffset);
    }
}
