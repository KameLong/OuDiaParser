package com.kamelong.oudia;

import com.kamelong.tool.SDlog;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
/*
 * Copyright (c) 2019 KameLong
 * contact:kamelong.com
 *
 * This source code is released under GNU GPL ver3.
 */

/**
 * １つのダイヤを表します。
 * ダイヤは１つの路線に複数作る事ができますが、同一路線中のダイヤは全て同じ駅順を持ちます。
 */
public class Diagram implements Cloneable {
    public LineFile lineFile;
    public static final int TIMETABLE_BACKCOLOR_NUM = 4;
    /**
     ダイヤの名称です。
     （例） "平日ダイヤ" など
     CentDedRosen に包含される CentDedDia では、
     この属性は一意でなくてはなりません。
     */
    public String name="";
    /**
     時刻表画面における基本背景色のIndexです
     単色時、種別色時の空行、縦縞・横縞・市松模様時
     および、基準運転時分機能有効時に用います。
     範囲は0以上JIKOKUHYOUCOLOR_COUNT未満です。
     */
    public int mainBackColorIndex=0;
    /**
     時刻表画面における補助背景色のIndexです
     縦縞・横縞・市松模様時に用います。
     範囲は0以上JIKOKUHYOUCOLOR_COUNT未満です。
     */
    public int subBackColorIndex=0;
    /**
     時刻表画面における背景色パターンのIndexです
     0:単色
     1:種別色
     2:縦縞
     3:横縞
     4:市松模様
     */
    public int timeTableBackPatternIndex=0;
    /**
     * ダイヤにふくまれる列車
     * [0]下り時刻表
     * [1]上り時刻表
     */
    public ArrayList<Train>[] trains=new ArrayList[2];


    /**
     * 推奨コンストラクタ
     * @param diaFile 親ダイヤファイル
     */
    public Diagram(LineFile diaFile){
        this.lineFile =diaFile;
        name="新しいダイヤ";
        trains[0]=new ArrayList<>();
        trains[1]=new ArrayList<>();
    }
    protected void setValue(String title, String value){
        switch (title){
            case "DiaName":
                name=value;
                break;
            case "MainBackColorIndex":
                mainBackColorIndex= Integer.parseInt(value);
                break;
            case "SubBackColorIndex":
                subBackColorIndex= Integer.parseInt(value);
                break;
            case "BackPatternIndex":
                timeTableBackPatternIndex= Integer.parseInt(value);
                break;
        }
    }

    /**
     * OuDia2nd形式で出力します
     */
    void saveToFile(PrintWriter out) {
        out.println("Dia.");
        out.println("DiaName="+name);
        out.println("MainBackColorIndex="+mainBackColorIndex);
        out.println("SubBackColorIndex="+subBackColorIndex);
        out.println("BackPatternIndex="+timeTableBackPatternIndex);
        out.println("Kudari.");
        for(Train t:trains[0]){
            t.saveToFile(out);
        }
        out.println(".");
        out.println("Nobori.");
        for(Train t:trains[1]){
            t.saveToFile(out);
        }
        out.println(".");
        out.println(".");
    }
    /**
     * OuDia形式で出力します
     */

    void saveToOuDiaFile(PrintWriter out){
        out.println("Dia.");
        out.println("DiaName="+name);
        out.println("Kudari.");
        for(Train t:trains[0]){
            t.saveToOuDiaFile(out);
        }
        out.println(".");
        out.println("Nobori.");
        for(Train t:trains[1]){
            t.saveToOuDiaFile(out);
        }
        out.println(".");
        out.println(".");
    }

    public Diagram clone(LineFile lineFile){
        try {
            Diagram result = (Diagram) super.clone();
            result.trains=new ArrayList[2];
            result.trains[0] = new ArrayList<>();
            for (Train train : trains[0]) {
                result.trains[0].add(train.clone(lineFile));

            }
            result.trains[1] = new ArrayList<>();
            for (Train train : trains[1]) {
                result.trains[1].add(train.clone(lineFile));
            }
            return result;
        }catch (CloneNotSupportedException e){
            SDlog.log(e);
            return new Diagram(lineFile);
        }
    }
    /*
    =================================
    ここまでOuDiaライブラリ共通の処理
    =================================
     */


    /**
     * 指定方向の列車数を返します
     * @param direction
     * @return
     */
    public int getTrainNum(int direction) {
        return trains[direction].size();
    }


    /**
     * 列車を取得思案す
     * @param direction 方向(0,1)
     * @param index 列車index
     */
    public Train getTrain(int direction, int index) {
        return trains[direction].get(index);
    }

    /**
     * 指定列車のindexを返します
     */
    public int getTrainIndex(int direction, Train train) {
        return trains[direction].indexOf(train);
    }
    public int getTrainIndex( Train train) {
        return trains[train.direction].indexOf(train);
    }

    /**
     * 指定indexに列車を追加します。
     * index=-1の時は、末尾に追加されます
     */
    public void addTrain(int direction,int index,Train train){
        if(train.getStationNum()!= lineFile.getStationNum()){
            new Exception("列車の駅数とダイヤの駅数が合いません").printStackTrace();
            SDlog.log("列車の駅数とダイヤの駅数が合いません");
            return;
        }
        if(index>=0&&index<getTrainNum(direction)){
            trains[direction].add(index,train);
            train.lineFile = lineFile;
            if(train.direction!=direction){
                Collections.reverse(train.stationTimes);
            }
            train.direction=direction;
        }else{
            trains[direction].add(train);
        }
    }

    /**
     * 指定列車を削除します
     * 方向とindexを指定
     */
    public void deleteTrain(int direction,int index){
        if(index>=0&&index<getTrainNum(direction)) {
            trains[direction].remove(index);
        }
    }
    /**
     * 指定列車を削除します
     */
    public void deleteTrain(Train train){
        trains[train.direction].remove(train);
    }

    /**
     * 時刻表を並び替える。
     * 並び替えに関しては、基準駅の通過時刻をもとに並び替えた後
     *
     * @param direction     並び替え対象方向
     * @param stationNumber 並び替え基準駅
     */
    public void sortTrain(int direction, int stationNumber) {
        long startTime = System.currentTimeMillis();
        Train[] trainList = trains[direction].toArray(new Train[0]);

        //ソートする前の順番を格納したクラス
        ArrayList<Integer> sortBefore = new ArrayList<>();
        //ソートした後の順番を格納したクラス
        ArrayList<Integer> sortAfter = new ArrayList<>();
        boolean[] sorted=new boolean[lineFile.getStationNum()];
        for(boolean b:sorted){
            b=false;
        }

        for (int i = 0; i < trainList.length; i++) {
            sortBefore.add(i);
        }

        for (int i = 0; i < sortBefore.size(); i++) {
            if (trainList[sortBefore.get(i)].getPredictionTime(stationNumber) > 0 && !trainList[sortBefore.get(i)].checkDoubleDay()) {
                //今からsortAfterに追加する列車の基準駅の時間
                int baseTime = trainList[sortBefore.get(i)].getPredictionTime(stationNumber);
                int j;
                for (j = sortAfter.size(); j > 0; j--) {
                    if (trainList[sortAfter.get(j - 1)].getPredictionTime(stationNumber) < baseTime) {
                        break;
                    }
                }
                sortAfter.add(j, sortBefore.get(i));
                sortBefore.remove(i);
                i--;
            }
        }
        sorted[stationNumber]=true;
        //この時点で基準駅に予測時間を設定できるものはソートされている
        if (direction == 0) {
            //ここからは基準駅を通らない列車についてソートを行う
            upperSearch:
            for (int station = stationNumber; station > 0; station--) {
                //基準駅より上方で運行する列車に着いてソートを行う
                if(lineFile.getStation(station).brunchCoreStationIndex>station){
                    //この駅が下方駅からの分岐駅の場合
                    if(sorted[lineFile.getStation(station).brunchCoreStationIndex]){
                        //分岐駅がソートされている場合
                        addTrainInSort1(sortBefore, sortAfter, trainList, new int[]{station,lineFile.getStation(station).brunchCoreStationIndex});
                        stationNumber=station-1;
                        continue upperSearch;
                    }else{

                    }
                }
                if (lineFile.station.get(station - 1).getBorder()) {
                    searchStation:
                    for (int i = station; i > 0; i--) {
                        //境界線がある駅の次の駅が分岐駅である可能性を探る
                        if (lineFile.station.get(station).name.equals(lineFile.station.get(i - 1).name)) {
                            addTrainInSort1(sortBefore, sortAfter, trainList, new int[]{i - 1, station});
                            for (int j = i; j < station; j++) {
                                addTrainInSort2(sortBefore, sortAfter, trainList, new int[]{j});
                            }
                            station = i;
                            continue upperSearch;
                        }
                    }
                    for (int i = station; i < lineFile.getStationNum(); i++) {
                        //境界線がある駅が分岐駅である可能性を探る
                        if (lineFile.station.get(station - 1).name.equals(lineFile.station.get(i).name)) {
                            addTrainInSort1(sortBefore, sortAfter, trainList, new int[]{station - 1, i});
                            for (int j = i; j < station; j++) {
                                addTrainInSort1(sortBefore, sortAfter, trainList, new int[]{j});
                            }
                            continue upperSearch;
                        }
                    }
                }
                addTrainInSort1(sortBefore, sortAfter, trainList, new int[]{station - 1});
            }
//            基準駅より後方から出発する列車に着いてソートを行う
            downSearch:
            for (int station = stationNumber + 1; station < lineFile.getStationNum(); station++) {
                if (lineFile.station.get(station - 1).getBorder()) {
                    for (int i = station; i > 0; i--) {
                        if (lineFile.station.get(station).name.equals(lineFile.station.get(i - 1).name)) {
                            addTrainInSort2(sortBefore, sortAfter, trainList, new int[]{station, i - 1});
                            continue downSearch;
                        }
                    }
                }
                if (lineFile.station.get(station).getBorder()) {
                    for (int i = station + 1; i < lineFile.getStationNum(); i++) {
                        if (lineFile.station.get(station).name.equals(lineFile.station.get(i).name)) {
                            addTrainInSort2(sortBefore, sortAfter, trainList, new int[]{i, station});
                            continue downSearch;
                        }
                    }
                }
                addTrainInSort2(sortBefore, sortAfter, trainList, new int[]{station});

            }
        } else {
            //ここからは基準駅を通らない列車についてソートを行う
            //基準駅より前方で運行する列車に着いてソートを行う
            baseStation:
            for (int station = stationNumber; station > 0; station--) {
                if (lineFile.station.get(station - 1).getBorder()) {
                    for (int i = station; i > 0; i--) {
                        if (lineFile.station.get(station).name.equals(lineFile.station.get(i - 1).name)) {
                            addTrainInSort2(sortBefore, sortAfter, trainList, new int[]{i - 1, station});
                            continue baseStation;
                        }
                    }
                }
                if (lineFile.station.get(station).getBorder()) {
                    for (int i = station + 1; i < lineFile.getStationNum(); i++) {
                        if (lineFile.station.get(station).name.equals(lineFile.station.get(i).name)) {
                            addTrainInSort2(sortBefore, sortAfter, trainList, new int[]{station, i});
                            continue baseStation;
                        }
                    }
                }
                addTrainInSort2(sortBefore, sortAfter, trainList, new int[]{station});
            }


            //基準駅より後方から出発する列車に着いてソートを行う
            baseStation:
            for (int station = stationNumber + 1; station < lineFile.getStationNum(); station++) {
                if (lineFile.station.get(station - 1).getBorder()) {
                    for (int i = station; i > 0; i--) {
                        if (lineFile.station.get(station).name.equals(lineFile.station.get(i - 1).name)) {
                            addTrainInSort1(sortBefore, sortAfter, trainList, new int[]{station, i - 1});
                            continue baseStation;
                        }
                    }
                }
                if (lineFile.station.get(station).getBorder()) {
                    for (int i = station + 1; i < lineFile.getStationNum(); i++) {
                        if (lineFile.station.get(station).name.equals(lineFile.station.get(i).name)) {
                            addTrainInSort1(sortBefore, sortAfter, trainList, new int[]{i, station});
                            continue baseStation;
                        }
                    }

                }
                addTrainInSort1(sortBefore, sortAfter, trainList, new int[]{station});
            }

        }

        for (int i = 0; i < sortBefore.size(); i++) {
            if (trainList[sortBefore.get(i)].getPredictionTime(stationNumber) > 0) {
                //今からsortAfterに追加する列車の基準駅の時間
                int baseTime = trainList[sortBefore.get(i)].getPredictionTime(stationNumber);
                int j;
                for (j = sortAfter.size(); j > 0; j--) {
                    if (trainList[sortAfter.get(j - 1)].getPredictionTime(stationNumber) > 0 && trainList[sortAfter.get(j - 1)].getPredictionTime(stationNumber) < baseTime) {
                        break;
                    }
                }
                sortAfter.add(j, sortBefore.get(i));
                sortBefore.remove(i);
                i--;
            }
        }

        sortAfter.addAll(sortBefore);
        ArrayList<Train> trainAfter = new ArrayList<>();
        for (int i = 0; i < sortAfter.size(); i++) {
            trainAfter.add(trainList[sortAfter.get(i)]);
        }
        trains[direction] = trainAfter;
        long endTime = System.currentTimeMillis();
        long endCount = Train.count2;


    }

    /**
     * 列車をsortAfterに時刻後方から挿入する
     * station[0]に停車する列車がソート対象
     * station[1以上]は同一駅
     */
    private void addTrainInSort1(ArrayList<Integer> sortBefore, ArrayList<Integer> sortAfter, Train[] trains, int[] station) {
        for (int i = sortBefore.size(); i > 0; i--) {
            int baseTime = trains[sortBefore.get(i - 1)].getTime(station[0],Train.ARRIVE,true);
            if (baseTime < 0 || trains[sortBefore.get(i - 1)].checkDoubleDay()) {
                continue;
            }
            int j = 0;
            boolean frag = false;

            for (j = 0; j < sortAfter.size(); j++) {

                int sortTime=-1;
                for(int s:station){
                    sortTime= Math.max(sortTime,trains[sortAfter.get(j)].getPredictionTime(s,Train.DEPART));
                }
                if (sortTime < 0) {
                    continue;
                }
                frag = true;
                if (sortTime >= baseTime) {
                    break;
                }
            }
            if (frag) {
                sortAfter.add(j, sortBefore.get(i - 1));
                sortBefore.remove(i - 1);
            }
        }
    }

    private void addTrainInSort2(ArrayList<Integer> sortBefore, ArrayList<Integer> sortAfter, Train[] trains, int[] station) {
        for (int i = 0; i < sortBefore.size(); i++) {
            int baseTime = trains[sortBefore.get(i)].getDepTime(station[0]);
            if (baseTime < 0 || trains[sortBefore.get(i)].checkDoubleDay()) {
                continue;
            }
            int j = 0;
            boolean frag = false;

            for (j = sortAfter.size(); j > 0; j--) {
                int sortTime;
                if (station.length == 2) {
                    if (trains[sortAfter.get(j - 1)].getPredictionTime(station[0], Train.ARRIVE) > 0 && trains[sortAfter.get(j - 1)].getPredictionTime(station[1], Train.ARRIVE) > 0) {
                        sortTime = Math.min(
                                trains[sortAfter.get(j - 1)].getPredictionTime(station[0], Train.ARRIVE),
                                trains[sortAfter.get(j - 1)].getPredictionTime(station[1], Train.ARRIVE));
                    } else {
                        sortTime = Math.max(
                                trains[sortAfter.get(j - 1)].getPredictionTime(station[0], Train.ARRIVE),
                                trains[sortAfter.get(j - 1)].getPredictionTime(station[1], Train.ARRIVE));

                    }
                } else {
                    sortTime = trains[sortAfter.get(j - 1)].getPredictionTime(station[0], Train.ARRIVE);
                }
                if (sortTime < 0) {
                    continue;
                }
                frag = true;
                if (sortTime <= baseTime) {
                    break;
                }
            }
            if (frag) {
                sortAfter.add(j, sortBefore.get(i));
                sortBefore.remove(i);
                i--;
            }

        }

    }
}
