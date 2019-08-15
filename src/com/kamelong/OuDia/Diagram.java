package com.kamelong.OuDia;

import com.kamelong.tool.SDlog;

import java.io.PrintWriter;
import java.util.ArrayList;
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
public class Diagram implements Cloneable{
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
    public DiaFile diaFile;


    /**
     * 推奨コンストラクタ
     * @param diaFile 親ダイヤファイル
     */
    public Diagram(DiaFile diaFile){
        this.diaFile=diaFile;
        name="新しいダイヤ";
        trains[0]=new ArrayList<>();
        trains[1]=new ArrayList<>();
    }
    void setValue(String title,String value){
        switch (title){
            case "DiaName":
                name=value;
                break;
            case "MainBackColorIndex":
                mainBackColorIndex=Integer.parseInt(value);
                break;
            case "SubBackColorIndex":
                subBackColorIndex=Integer.parseInt(value);
                break;
            case "BackPatternIndex":
                timeTableBackPatternIndex=Integer.parseInt(value);
                break;
        }
    }

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
    @Override
    public Diagram clone(){
        try {
            Diagram result = (Diagram) super.clone();
            result.trains[0] = new ArrayList<>();
            for (Train train : trains[0]) {
                result.trains[0].add(train.clone());
            }
            result.trains[1] = new ArrayList<>();
            for (Train train : trains[1]) {
                result.trains[1].add(train.clone());
            }
            return result;
        }catch (CloneNotSupportedException e){
            SDlog.log(e);
            return new Diagram(diaFile);
        }
    }

}
