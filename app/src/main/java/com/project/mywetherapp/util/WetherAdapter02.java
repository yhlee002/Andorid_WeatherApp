package com.project.mywetherapp.util;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.mywetherapp.R;
import com.project.mywetherapp.model.wether.FcstInfo;

import java.util.ArrayList;

// 시간대별 날씨 카드뷰에 사용될 어댑터(내일 날씨)
public class WetherAdapter02 extends RecyclerView.Adapter<WetherAdapter02.ViewHolder> {

    private ArrayList<FcstInfo> fcstInfoList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View rootView = inflater.inflate(R.layout.fcst_card_tommorow, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FcstInfo fcstInfo = fcstInfoList.get(position);
        holder.setInfo(fcstInfo);
    }

    @Override
    public int getItemCount() {
        return fcstInfoList.size();
    }

    public void addInfo(FcstInfo info) {
        fcstInfoList.add(info);
    }

    public FcstInfo getInfo(int position) {
        return fcstInfoList.get(position);
    }

    public void setInfoList(ArrayList<FcstInfo> infoList) {
        this.fcstInfoList = infoList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textTime, textPop, textT3H; //시간, 강수확률(6시간 강수량), 3시간 기온
        ImageView wetherImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textTime = itemView.findViewById(R.id.textView33);
            textPop = itemView.findViewById(R.id.textView35);
            textT3H = itemView.findViewById(R.id.textView34);
            wetherImg = itemView.findViewById(R.id.imageView21);
            Log.i("[W Adapter2 - VHolder]", "textTime : " + textTime);
        }

        public void setInfo(FcstInfo fcstInfo) {
//            Log.i("[W Adapter2 - setInfo]", "fcstInfo : " + fcstInfo.toString());
            String time = fcstInfo.getFcstTime().substring(0, 2);
            String sky = fcstInfo.getCategoryMap().get("SKY");
            String pop = fcstInfo.getCategoryMap().get("POP");
            String pty = fcstInfo.getCategoryMap().get("PTY");
            String t3h = fcstInfo.getCategoryMap().get("T3H");
            int popInt = Integer.parseInt(pop.replace("%", ""));
            textTime.setText(time + "시");
            textPop.setText("강수확률 : " + pop);
            textT3H.setText(t3h);

            if (popInt < 30 || sky.equals("맑음")) {
                wetherImg.setImageResource(R.drawable.icons_sun);
            } else if (popInt < 30 && (sky.equals("흐림") || sky.equals("구름많음"))) {
                wetherImg.setImageResource(R.drawable.icons_clouds);
            } else if ((popInt >= 30 && popInt < 60) && ((sky.equals("흐림") || sky.equals("구름많음")))) {
                wetherImg.setImageResource(R.drawable.icons_rain_cloud);
            } else if (popInt >= 60 || pty.equals("비/눈") || pty.equals("비")) {
                wetherImg.setImageResource(R.drawable.icons_rain);
            } else if (pty.equals("눈") || pty.equals("눈날림")) {
                wetherImg.setImageResource(R.drawable.icons_snow);
            }
        }
    }
}
