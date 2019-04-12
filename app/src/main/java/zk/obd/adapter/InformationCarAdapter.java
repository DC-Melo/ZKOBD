package zk.obd.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import zk.obd.R;
import zk.obd.been.CarInformationBean;

public class InformationCarAdapter extends RecyclerView.Adapter <InformationCarAdapter.VH>{

    private List<CarInformationBean> mDatas;

    public InformationCarAdapter(List<CarInformationBean> data) {
        this.mDatas = data;
    }
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carlist, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.sigLable.setText(mDatas.get(position).getSignallable());
        holder.sigValue.setText(mDatas.get(position).getASCValue());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //item 点击事件
            }
        });

    }



    @Override
    public int getItemCount() {
        Log.e("--------","--------"+mDatas.size());
        return mDatas.size();
    }
    //② 创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder{
        public final TextView sigLable;
        public final TextView sigValue;
        public VH(View v) {
            super(v);
            sigLable = (TextView) v.findViewById(R.id.sig_Lable);
            sigValue = (TextView) v.findViewById(R.id.sig_Value);

        }
    }
}
