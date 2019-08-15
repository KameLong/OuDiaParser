package com.kamelong.OuDia;

import com.kamelong.tool.SDlog;

import java.util.ArrayList;
/*
 * Copyright (c) 2019 KameLong
 * contact:kamelong.com
 *
 * This source code is released under GNU GPL ver3.
 */

/**
 * １つの駅停車情報を表します。
 * StationTimeはTrainに紐付けられます。
 */
public class StationTime {
    /**
     『駅扱』を表します。
     */
    public int stopType=STOP_TYPE_NOSERVICE;
    public static final int STOP_TYPE_NOSERVICE = 0;
    public static final int STOP_TYPE_STOP = 1;
    public static final int STOP_TYPE_PASS = 2;
    public static final int STOP_TYPE_NOVIA = 3;

    /**
     * 着時刻
     * 着時刻が存在しない時は負の数となります
     * 時刻は秒単位で表現し、0:00:00を0とします。
     * 1:00:00は3600、10:08:20は10*3600+8*60+20=36,500となります。
     */
    public int ariTime=-1;
    /**
     * 発時刻
     * 発時刻が存在しない時は負の数となります
     * 時刻は秒単位で表現し、0:00:00を0とします。
     */
    public int depTime=-1;
    /**
     * 番線
     * デフォルト=-1
     */
    public int stopTrack=-1;

    /**
     * 前作業一覧
     */
    public ArrayList<StationTimeOperation>beforeOperations=new ArrayList<>();
    /**
     * 後作業一覧
     */
    public ArrayList<StationTimeOperation>afterOperations=new ArrayList<>();
    /**
     * 親列車
     */
    public Train train=null;

    public StationTime(Train train){
        this.train=train;
    }

    @Override
    public StationTime clone() {
        try {
            StationTime other = (StationTime) super.clone();
            for (StationTimeOperation operation : beforeOperations) {
                other.beforeOperations.add(operation.clone());
            }
            for (StationTimeOperation operation : afterOperations) {
                other.afterOperations.add(operation.clone());
            }
            return other;
        }catch (CloneNotSupportedException e){
            SDlog.log(e);
            return new StationTime(train);
        }
    }

    void setStationTime(String value){
        if (value.length() == 0) {
            stopType=0;
            return;
        }
        if (!value.contains(";")) {
            if(value.contains("$")){
                stopTrack = Integer.parseInt(value.split("\\$", -1)[1]);
                stopType= Integer.parseInt(value.split("\\$", -1)[0]);

                return;

            }
            stopType=Integer.parseInt(value);
            return;
        }
        stopType=Integer.parseInt(value.split(";", -1)[0]);
        value=value.split(";",-1)[1];
        if(value.contains("$")){
            stopTrack = Integer.parseInt(value.split("\\$", -1)[1]);
            value=value.split("\\$")[0];
        }
        if (value.contains("/")) {
            ariTime= timeStringToInt(value.split("/", -1)[0]);
            if (value.split("/", -1)[1].length() != 0) {
                depTime=timeStringToInt(value.split("/", -1)[1]);
            }
        } else {
            depTime=timeStringToInt(value);
        }
    }
    void setTrack(String value){
        if(value.contains(";")){
            value=value.split(";")[0];
        }
        stopTrack=Integer.parseInt(value)-1;
    }

    /**
     * 秒単位の数値で表現された時刻を、文字列に変換します。
     * 秒部分が0の時は秒部分が省略されます。
     * @param time
     * @return
     */
    public static String timeIntToOuDiaString(int time){
        if (time < 0) return "";
        int ss = time % 60;
        time = time / 60;
        int mm = time % 60;
        time = time / 60;
        int hh = time % 24;
        if(ss==0){
            return hh+ String.format("%02d", mm);
        }
        return hh+ String.format("%02d", mm) + String.format("%02d", ss);

    }

    /**
     * 文字列形式の時刻を秒の数値に変える
     *
     * @param sTime 3桁から6桁の数字で構成された文字列
     */
    public static int timeStringToInt(String sTime) {
        int hh = 0;
        int mm = 0;
        int ss = 0;
        switch (sTime.length()) {
            case 3:
                hh = Integer.parseInt(sTime.substring(0, 1));
                mm = Integer.parseInt(sTime.substring(1, 3));
                break;
            case 4:
                hh = Integer.parseInt(sTime.substring(0, 2));
                mm = Integer.parseInt(sTime.substring(2, 4));
                break;
            case 5:
                hh = Integer.parseInt(sTime.substring(0, 1));
                mm = Integer.parseInt(sTime.substring(1, 3));
                ss = Integer.parseInt(sTime.substring(3, 5));
                break;
            case 6:
                hh = Integer.parseInt(sTime.substring(0, 2));
                mm = Integer.parseInt(sTime.substring(2, 4));
                ss = Integer.parseInt(sTime.substring(4, 6));
                break;
            default:
                return -1;
        }
        if (hh > 23 || hh < 0) {
            return -1;
        }
        if (mm > 59 || mm < 0) {
            return -1;
        }
        if (ss > 59 || ss < 0) {
            return -1;
        }
        return 3600 * hh + 60 * mm + ss;
    }

    String getOuDiaString(boolean oudia2ndFrag){
        String value="";
        if(stopType==0){
            return value;
        }
        if(stopType==3){
            value+=2;
        }else {
            value += stopType;
        }
        if(ariTime<0&&depTime<0){
            if(oudia2ndFrag){
                value+="$"+stopTrack;
            }
            return value;
        }
        value+=";";
        if(timeExist(1)){
            value+=timeIntToOuDiaString(ariTime)+"/";
        }
        if(timeExist(0)){
            value+=timeIntToOuDiaString(depTime);
        }
        if(oudia2ndFrag){
            value+="$"+getStopTrack();
        }
        return value;
    }


    /**
     * @param ad Train.BOUND_OUT or Train.BOUND_IN
     * @return 各時刻が存在する場合はtrue、存在しないときはfalseを返します
     */
    public boolean timeExist(int ad){
        if(ad==0){
            return depTime>=0;
        }else{
            return ariTime>=0;
        }
    }
    /**
     * @return 発着時刻のうち片方でも存在する場合はtrue、存在しないときはfalseを返します
     */
    public boolean timeExist(){
        return depTime>=0||ariTime>=0;
    }

    /**
     * データを削除し、初期状態にします
     */
    public void reset(){
        depTime=-1;
        ariTime=-1;
        stopTrack=0;
        stopType=0;
        afterOperations=new ArrayList<>();
        beforeOperations=new ArrayList<>();

    }
    /**
     * 発着番線を返します。
     * trackが-1の時はデフォルトの発着番線を返します。
     *
     */
    public int getStopTrack(){
        if(stopTrack<0){
            return train.diaFile.station.get(train.getStationIndex(train.stationTimes.indexOf(this))).stopMain[train.direction];
        }
        return stopTrack;
    }


}
