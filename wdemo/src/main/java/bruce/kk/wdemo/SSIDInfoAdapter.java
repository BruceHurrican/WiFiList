/*
 * BruceHurrican
 * Copyright (c) 2016.
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    This document is Bruce's individual learning the android demo, wherein the use of the code from the Internet, only to use as a learning exchanges.
 *    And where any person can download and use, but not for commercial purposes.
 *    Author does not assume the resulting corresponding disputes.
 *    If you have good suggestions for the code, you can contact BurrceHurrican@foxmail.com
 *    本文件为Bruce's个人学习android的作品, 其中所用到的代码来源于互联网，仅作为学习交流使用。
 *    任和何人可以下载并使用, 但是不能用于商业用途。
 *    作者不承担由此带来的相应纠纷。
 *    如果对本代码有好的建议，可以联系BurrceHurrican@foxmail.com
 */

package bruce.kk.wdemo;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bruceutils.utils.logdetails.LogDetails;

import java.util.List;

import bruce.kk.wdemo.model.WFinfo;

/**
 * Created by BruceHurrican on 2017/4/26.
 */
public class SSIDInfoAdapter extends RecyclerView.Adapter<SSIDInfoAdapter.SSIDViewHolder> {
    private List<WFinfo> dataList;
    private CardViewItemClickListener itemClickListener;
    private CardViewItemLongClickListener itemLongClickListener;

    public SSIDInfoAdapter() {
    }

    public void setDataList(List<WFinfo> dataList) {
        LogDetails.i("dataList->" + dataList);
        this.dataList = dataList;
    }

    public void setItemClickListener(CardViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setItemLongClickListener(CardViewItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    @Override
    public SSIDViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item, parent, false);
        return new SSIDViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SSIDViewHolder holder, final int position) {
        holder.tvSsid.setText(dataList.get(position).ssid);
        holder.tvUploaded.setText(dataList.get(position).isUploaded ? "是" : "否");
//        holder.cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                itemClickListener.itemClick(v, position);
//            }
//        });
//        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                itemLongClickListener.itemLongClick(v, position);
//                return true;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public interface CardViewItemClickListener {
        void itemClick(View view, int position);
    }

    public interface CardViewItemLongClickListener {
        void itemLongClick(View view, int position);
    }

    public static class SSIDViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSsid, tvUploaded;
        private CardView cardView;

        public SSIDViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            tvSsid = (TextView) itemView.findViewById(R.id.tv_ssid);
            tvUploaded = (TextView) itemView.findViewById(R.id.tv_uploaded);
        }
    }
}
