package com.project.mywetherapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.mywetherapp.model.FcstInfo;

import java.util.ArrayList;

// 시간대별 날씨 카드뷰에 사용될 어댑터
public class WetherAdapter extends RecyclerView.Adapter<WetherAdapter.ViewHolder> {

    private ArrayList<FcstInfo> fcstInfoList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View rootView = inflater.inflate(R.layout.fcst_card, parent, false);
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

            //6(온도), 7(강수확률), 8(온도)
            textTime = itemView.findViewById(R.id.textView6);
            textPop = itemView.findViewById(R.id.textView8);
            textT3H = itemView.findViewById(R.id.textView7);
            wetherImg = itemView.findViewById(R.id.imageView3);
        }

        public void setInfo(FcstInfo fcstInfo) {
            Log.i("[W Adapter - setInfo]", "fcstInfo : " + fcstInfo.toString());

            String time = fcstInfo.getFcstTime().substring(0, 2);
            String sky = fcstInfo.getCategoryMap().get("SKY");
            String pop = fcstInfo.getCategoryMap().get("POP");
            String pty = fcstInfo.getCategoryMap().get("PTY");
            String t3h = fcstInfo.getCategoryMap().get("T3H");
            int popInt = Integer.parseInt(pop.replace("%", ""));
            textTime.setText(time + "시");
            textPop.setText("강수확률 : " + pop);
            textT3H.setText(t3h);

//            int img = R.drawable.icons_sun;
            if (popInt < 30 || sky.equals("0")) {
                wetherImg.setImageResource(R.drawable.icons_sun);
            } else if (popInt < 30 || sky.equals("흐림")) {
                wetherImg.setImageResource(R.drawable.icons_clouds);
            } else if ((popInt >= 30 && popInt < 60) || (sky.equals("흐림") || sky.equals("구름많음"))) {
                wetherImg.setImageResource(R.drawable.icons_rain_cloud);
            } else if (popInt > 60 || sky.equals("구름많음") || sky.equals("흐림")) {
                wetherImg.setImageResource(R.drawable.icons_rain);
            } else if (pty == "비/눈" || pty.equals("비") || pty.equals("눈날림")) {
                wetherImg.setImageResource(R.drawable.icons_snow);
            }
            Log.i("[W Adapter - setInfo]", "time원자값 : " + fcstInfo.getFcstTime());
            Log.i("[W Adapter - setInfo]", "강수확률(%) : " + popInt);
            Log.i("[W Adapter - setInfo]", "하늘 상태 : " + sky);
        }
    }
}
