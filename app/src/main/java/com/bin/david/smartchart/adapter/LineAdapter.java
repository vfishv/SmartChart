package com.bin.david.smartchart.adapter;

import androidx.annotation.Nullable;

import com.bin.david.smartchart.R;
import com.bin.david.smartchart.bean.ChartStyle;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by huang on 2017/10/13.
 */

public class LineAdapter extends BaseQuickAdapter<ChartStyle,BaseViewHolder> {


    public LineAdapter(@Nullable List<ChartStyle> data) {
        super(R.layout.item_main, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ChartStyle item) {
        helper.setText(R.id.tv_chart_name,item.value);
    }
}
