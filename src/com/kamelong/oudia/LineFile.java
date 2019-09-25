package com.kamelong.oudia;


import com.kamelong.tool.Color;
import com.kamelong.tool.Font;
import com.kamelong.tool.SDlog;
import com.kamelong.tool.ShiftJISBufferedReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
/*
 * Copyright (c) 2019 KameLong
 * contact:kamelong.com
 *
 * This source code is released under GNU GPL ver3.
 */

/**
 * 一つの路線ファイルを表す。
 */
public class LineFile implements Cloneable {
    /**
     * このファイルが保存されていたパス
     */
    public String filePath="";

    /**
     * 路線名
     */
    public String name="";

    /**
     * ダイヤグラム開始時間
     */
    public int diagramStartTime=3600*3;
    /**
     ダイヤグラムの既定の駅間幅。

     列車設定のない駅間の、ダイヤグラムビュー上での
     縦方向の幅を『ダイヤグラムエンティティY座標』単位(秒)で指定します。

     既定値は 60 です。
     */
    public int stationSpaceDefault =60;

    /**
     * コメント
     */
    public String comment="";
    /**
     運用機能の有効無効
     路線ファイルのプロパティで変更できます
     0:運用機能は無効です。
     1:運用機能は簡易モードです。
     列車の接続のみを行い、運用番号は設定しません。
     2:運用機能は通常モードです
     規定値は0です
     */
    public int operationStyle=0;
    /**
     ダイヤの別名です。
     （例） "北行" など
     */
    public String[] diaNameDefault={"下り時刻表","上り時刻表"};

    /**
     * 駅一覧
     */
    public ArrayList<Station> station=new ArrayList<>();
    /**
     * 列車種別一覧
     */
    public ArrayList<TrainType> trainType=new ArrayList<>();
    /**
     * ダイヤ一覧
     */
    public ArrayList<Diagram> diagram=new ArrayList<>();
    /**
     * OuDiaバージョン
     */
    public String version="";

    //DispProp
    /**
     * 時刻表フォント
     */
    public ArrayList<Font> timeTableFont=new ArrayList<>();
    /**
     * 時刻表Vフォント
     * 縦書きに使う？
     */
    public Font timeTableVFont= Font.OUDIA_DEFAULT;
    /**
     * ダイヤ駅名フォント
     */
    public Font diaStationNameFont= Font.OUDIA_DEFAULT;
    /**
     * ダイヤ時刻フォント
     */
    public Font diaTimeFont= Font.OUDIA_DEFAULT;

    /**
     * ダイヤ列車フォント
     */
    public Font diaTrainFont= Font.OUDIA_DEFAULT;
    /**
     * コメントフォント
     */
    public Font commentFont= Font.OUDIA_DEFAULT;
    /**
     * ダイヤ文字色
     */
    public Color diaTextColor =new Color("#000000");
    /**
     * ダイヤ背景色
     */
    public Color diaBackColor=new Color("#FFFFFF");
    /**
     * ダイヤ列車色
     */
    public Color diaTrainColor=new Color("#000000");
    /**
     * ダイヤ軸色
     */
    public Color diaAxicsColor=new Color("C0C0C0");
    /**
     * 時刻表背景色
     */
    public ArrayList<Color> timeTableBackColor=new ArrayList<>();
    /**
     * StdOpeTimeLowerColor
     */
    public Color stdOpeTimeLowerColor=new Color();
    /**
     * StdOpeTimeHigherColor
     */
    public Color stdOpeTimeHigherColor=new Color();
    /**
     * StdOpeTimeUndefColor
     */
    public Color stdOpeTimeUndefColor=new Color();
    /**
     * StdOpeTimeIllegalColor
     */
    public Color stdOpeTimeIllegalColor=new Color();
    /**
     * 駅名の長さ
     */
    public int stationNameWidth=6;
    /**
     * 時刻用列車幅
     */
    public int trainWidth=5;
    /**
     * 秒単位移動量
     * 二つある
     */
    public int[] secondShift = {10, 15};
    /**
     * 列車名欄表示
     */
    public boolean showTrainName=true;
    /**
     * 路線外終着駅を起点側に表示する
     */
    public boolean showOuterTerminalOrigin=false;
    /**
     * 路線外終着駅を終点側に表示する
     */
    public boolean showOuterTerminalTerminal=false;
    /**
     * 路線外終着駅を表示するか
     */
    public boolean showOuterTerminal=false;

    /**
     * デフォルトコンストラクタ
     */
    public LineFile(){

    }

    /**
     * ファイルからダイヤを開く
     * @param file　入力ファイル
     * @throws Exception ファイルが読み込めなかった時に返す
     */
    public LineFile(File file)throws Exception {
        filePath=file.getPath();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        version=br.readLine().split("=",-1)[1];
        double v=1.02;
        try {
            v = Double.parseDouble(version.substring(version.indexOf(".") + 1));
        }catch(Exception e){
            e.printStackTrace();
        }

        if(version.startsWith("OuDia.")||v<1.03){
            //Shift-Jisのファイル
            loadShiftJis(file);
        }else{
            //utf-8のファイル
            loadDiaFile(br);
        }
        checkBorderStation();
        System.out.println("読み込み終了");

    }

    /**
     * Shift-JISで書かれたファイルを読み込む
     */
    private void loadShiftJis(File file)throws Exception {
        BufferedReader br = new ShiftJISBufferedReader(new InputStreamReader(new FileInputStream(file),"Shift-JIS"));
        version=br.readLine().split("=",-1)[1];
        loadDiaFile(br);
    }

    /**
     * ダイヤファイルを読み込む
     * @param br 入力ファイル
     * @throws Exception 読み込み失敗
     */
    protected void loadDiaFile(BufferedReader br)throws Exception {
        int direction=0;
        station=new ArrayList<>();
        trainType=new ArrayList<>();
        diagram=new ArrayList<>();
        String property="";
        Stack<String> propertyStack=new Stack<>();
        String line=br.readLine();
        Station tempStation=new Station(this);
        StationTrack tempTrack=new StationTrack();
        OuterTerminal tempOuterTerminal=new OuterTerminal();
        TrainType tempType=new TrainType();
        Diagram tempDia=new Diagram(this);
        Train tempTrain=new Train(this,0);
        while(line!=null){
            if(line.equals(".")) {
                //読み込みプロパティ終了
                property = propertyStack.pop();
                line = br.readLine();
                continue;
            }
            if(line.endsWith(".")){
                propertyStack.push(property);
                property=line.substring(0,line.length()-1);
                switch(property){
                    case "Eki":
                        tempStation=new Station(this);
                        station.add(tempStation);
                        break;
                    case "EkiTrack2":
                        tempTrack=new StationTrack();
                        tempStation.tracks.add(tempTrack);
                        break;
                    case "OuterTerminal":
                        tempOuterTerminal=new OuterTerminal();
                        tempStation.outerTerminals.add(tempOuterTerminal);
                        break;
                    case "Ressyasyubetsu":
                        tempType=new TrainType();
                        trainType.add(tempType);
                        break;
                    case "Dia":
                        tempDia=new Diagram(this);
                        diagram.add(tempDia);
                        break;
                    case "Kudari":
                        direction=0;
                        break;
                    case "Nobori":
                        direction=1;
                        break;
                    case "Ressya":

                        tempTrain=new Train(this,direction);
                        tempDia.trains[direction].add(tempTrain);
                        break;
                }
                line=br.readLine();
                continue;
            }
            if(line.contains("=")){
                String title=line.substring(0,line.indexOf("="));
                String value=line.substring(line.indexOf("=")+1);
                switch(property){
                    case "Eki":
                        tempStation.setValue(title,value);
                        break;
                    case "EkiTrack2":
                        tempTrack.setValue(title,value);
                        break;
                    case "OuterTerminal":
                        tempOuterTerminal.setValue(title,value);
                        break;
                    case "Ressyasyubetsu":
                        tempType.setValue(title,value);
                        break;
                    case "Dia":
                        tempDia.setValue(title,value);
                        break;
                    case "Ressya":
                        tempTrain.setValue(title,value);
                        break;
                    case "Rosen":
                        this.setValue(title,value);
                        break;
                    case "DispProp":
                        this.setValue(title,value);
                        break;
                }
            }

            line=br.readLine();
        }
    }

    /**
     * OuDiaファイルを読み込んだ場合は番線情報などが含まれていないため、
     * OuDia2nd形式として保存できるよう、番線情報を付加します。
     */
    public void ConvertOudToOud2nd(){
        for(Station s :station){
            if(s.tracks.size()<1){
                s.tracks.add(new StationTrack("1番線","1"));
            }
            if(s.tracks.size()<2){
                s.tracks.add(new StationTrack("2番線","2"));
            }
        }

    }

    /**
     * OuDia形式の1行を読み込みます。
     * Rosen.とDispProp.に関する情報をここで読み込みます。
     * @param title
     * @param value
     */
    protected void setValue(String title, String value){
        switch(title){
            case "Rosenmei":
                name=value;
                break;
            case "KudariDiaAlias":
                if(value.length()!=0){
                    diaNameDefault[0]=value;
                }
                break;
            case "NoboriDiaAlias":
                if(value.length()!=0){
                    diaNameDefault[1]=value;
                }
                break;
            case "KitenJikoku":
                switch(value.length()){
                    case 3:
                        diagramStartTime=3600* Integer.parseInt(value.substring(0,1))+60* Integer.parseInt(value.substring(1,3));
                        break;
                    case 4:
                        diagramStartTime=3600* Integer.parseInt(value.substring(0,2))+60* Integer.parseInt(value.substring(2,4));
                        break;
                    default:
                        diagramStartTime=60* Integer.parseInt(value);
                }
                break;
            case "EnableOperation":
                operationStyle= Integer.parseInt(value);
                break;
            case "Comment":
                comment=value.replace("\\n","\n");
                break;
            case "JikokuhyouFont":
                timeTableFont.add(new Font(value));
                break;
            case "JikokuhyouVFont":
                timeTableVFont=new Font(value);
                break;
            case "DiaEkimeiFont":
                diaStationNameFont=new Font(value);
                break;
            case "DiaJikokuFont":
                diaTimeFont=new Font(value);
                break;
            case "DiaRessyaFont":
                diaTrainFont=new Font(value);
                break;
            case "CommentFont":
                commentFont=new Font(value);
                break;
            case "DiaMojiColor":
                diaTextColor.setOuDiaColor(value);
                break;
            case "DiaHaikeiColor":
                diaBackColor.setOuDiaColor(value);
                break;
            case "DiaRessyaColor":
                diaTrainColor.setOuDiaColor(value);
                break;
            case "DiaJikuColor":
                diaAxicsColor.setOuDiaColor(value);
                break;
            case "JikokuhyouBackColor":
                Color color=new Color();
                color.setOuDiaColor(value);
                timeTableBackColor.add(color);
                break;
            case "StdOpeTimeLowerColor":
                stdOpeTimeLowerColor.setOuDiaColor(value);
                break;
            case "StdOpeTimeHigherColor":
                stdOpeTimeHigherColor.setOuDiaColor(value);
                break;
            case "StdOpeTimeUndefColor":
                stdOpeTimeUndefColor.setOuDiaColor(value);
                break;
            case "StdOpeTimeIllegalColor":
                stdOpeTimeIllegalColor.setOuDiaColor(value);
                break;
            case "EkimeiLength":
                stationNameWidth= Integer.parseInt(value);
                break;
            case "JikokuhyouRessyaWidth":
                trainWidth= Integer.parseInt(value);
                break;
            case "AnySecondIncDec1":
                secondShift[0]= Integer.parseInt(value);
                break;
            case "AnySecondIncDec2":
                secondShift[1]= Integer.parseInt(value);
                break;
            case "DisplayRessyamei":
                showTrainName=value.equals("1");
                break;
            case "DisplayOuterTerminalEkimeiOriginSide":
                showOuterTerminalOrigin=value.equals("1");
                break;
            case "DisplayOuterTerminalEkimeiTerminalSide":
                showOuterTerminalTerminal=value.equals("1");
                break;
            case "DiagramDisplayOuterTerminal":
                showOuterTerminal=value.equals("1");
                break;
        }
    }


    /**
     * OuDiaSecond形式で保存します。
     * 現在はOuDiaSecond.1.07形式で保存します。保存形式は今後のアップデートで変更する可能性があります。
     * @param fileName
     * @throws Exception
     */
    public void saveToFile(String fileName) throws Exception {
        convertToOud2();
        new File(fileName).createNewFile();
        FileOutputStream fos = new FileOutputStream(fileName);
        //BOM付与
        fos.write(0xef);
        fos.write(0xbb);
        fos.write(0xbf);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
        out.println("FileType=OuDiaSecond.1.07");
        out.println("Rosen.");
        out.println("Rosenmei="+name);
        if(!diaNameDefault[0].equals("下り時刻表")) {
            out.println("KudariDiaAlias=" + diaNameDefault[0]);
        }
        if(!diaNameDefault[1].equals("上り時刻表")) {
            out.println("NoboriDiaAlias=" + diaNameDefault[1]);
        }
        for(Station s:station){
            s.saveToFile(out);
        }
        for(TrainType type:trainType){
            type.saveToFile(out);
        }
        for(Diagram dia:diagram){
            dia.saveToFile(out);
        }

        out.println("KitenJikoku="+(diagramStartTime/3600)+ String.format("%02d",(diagramStartTime/60)%60));
        out.println("DiagramDgrYZahyouKyoriDefault="+ stationSpaceDefault );
        out.println("EnableOperation="+operationStyle);
        out.println("Comment="+comment.replace("\n","\\n"));
        out.println(".");

        out.println("DispProp.");
        for(Font font:timeTableFont){
            out.println("JikokuhyouFont="+font.getOuDiaString());
        }
        for(int i=timeTableFont.size();i<8;i++){
            out.println("JikokuhyouFont="+ Font.OUDIA_DEFAULT.getOuDiaString());
        }
        out.println("JikokuhyouVFont="+timeTableVFont.getOuDiaString());
        out.println("DiaEkimeiFont="+diaStationNameFont.getOuDiaString());
        out.println("DiaJikokuFont="+diaTimeFont.getOuDiaString());
        out.println("DiaRessyaFont="+diaTrainFont.getOuDiaString());
        out.println("CommentFont="+commentFont.getOuDiaString());
        out.println("DiaMojiColor="+diaTextColor.getOudiaString());
        out.println("DiaHaikeiColor="+diaBackColor.getOudiaString());
        out.println("DiaRessyaColor="+diaTrainColor.getOudiaString());
        out.println("DiaJikuColor="+diaAxicsColor.getOudiaString());
        for(Color color :timeTableBackColor){
            out.println("JikokuhyouBackColor="+color.getOudiaString());
        }
        out.println("StdOpeTimeLowerColor="+stdOpeTimeLowerColor.getOudiaString());
        out.println("StdOpeTimeHigherColor="+stdOpeTimeHigherColor.getOudiaString());
        out.println("StdOpeTimeUndefColor="+stdOpeTimeUndefColor.getOudiaString());
        out.println("StdOpeTimeIllegalColor="+stdOpeTimeIllegalColor.getOudiaString());
        out.println("EkimeiLength="+(stationNameWidth));
        out.println("JikokuhyouRessyaWidth="+trainWidth);
        out.println("AnySecondIncDec1="+secondShift[0]);
        out.println("AnySecondIncDec2="+secondShift[1]);
        out.println("DisplayRessyamei="+(showTrainName?"1":"0"));
        out.println("DisplayOuterTerminalEkimeiOriginSide="+(showOuterTerminalOrigin?"1":"0"));
        out.println("DisplayOuterTerminalEkimeiTerminalSide="+(showOuterTerminalTerminal?"1":"0"));
        out.println("DiagramDisplayOuterTerminal="+(showOuterTerminal?"1":"0"));
        out.println(".");
        out.println("FileTypeAppComment=AOdia V3.0a.0");
        out.close();
    }

    /**
     * OuDia形式で保存します。
     * 現在はOuDia.1.02形式で保存します。保存形式は今後のアップデートで変更する可能性があります。
     * @param fileName
     * @throws Exception
     */
    public void saveToOuDiaFile(String fileName) throws Exception {
        PrintWriter out = new PrintWriter
                (new BufferedWriter(new OutputStreamWriter
                        (new FileOutputStream(new File(fileName)),"Shift-JIS")));
        out.println("FileType=OuDia.1.02");
        out.println("Rosen.");
        out.println("Rosenmei="+name);
        for(Station s:station){
            s.saveToOuDiaFile(out);
        }
        for(TrainType type:trainType){
            type.saveToOuDiaFile(out);
        }
        for(Diagram dia:diagram){
            dia.saveToOuDiaFile(out);
        }

        out.println("KitenJikoku="+(diagramStartTime/3600)+ String.format("%02d",(diagramStartTime/60)%60));
        out.println("DiagramDgrYZahyouKyoriDefault="+ stationSpaceDefault );
        out.println("Comment="+comment.replace("\n","\\n"));
        out.println(".");

        out.println("DispProp.");
        for(Font font:timeTableFont){
            out.println("JikokuhyouFont="+font.getOuDiaString());
        }
        for(int i=timeTableFont.size();i<8;i++){
            out.println("JikokuhyouFont="+ Font.OUDIA_DEFAULT.getOuDiaString());
        }

        out.println("JikokuhyouVFont="+timeTableVFont.getOuDiaString());
        out.println("DiaEkimeiFont="+diaStationNameFont.getOuDiaString());
        out.println("DiaJikokuFont="+diaTimeFont.getOuDiaString());
        out.println("DiaRessyaFont="+diaTrainFont.getOuDiaString());
        out.println("CommentFont="+commentFont.getOuDiaString());
        out.println("DiaMojiColor="+diaTextColor.getOudiaString());
        out.println("DiaHaikeiColor="+diaBackColor.getOudiaString());
        out.println("DiaRessyaColor="+diaTrainColor.getOudiaString());
        out.println("DiaJikuColor="+diaAxicsColor.getOudiaString());
        out.println("EkimeiLength="+(stationNameWidth));
        out.println("JikokuhyouRessyaWidth="+trainWidth);
        out.println(".");
        out.println("FileTypeAppComment=AOdia V3.0a.0");

        out.close();
    }
    @Override
    public LineFile clone(){
        try{
            LineFile result=(LineFile)super.clone();
            result.commentFont=this.commentFont.clone();
            result.diaAxicsColor=this.diaAxicsColor.clone();
            result.diaBackColor=this.diaBackColor.clone();
            result.diaNameDefault=this.diaNameDefault.clone();
            result.diaStationNameFont=this.diaStationNameFont.clone();
            result.diaTextColor=this.diaTextColor.clone();
            result.diaTimeFont=this.diaTimeFont.clone();
            result.diaTrainColor=this.diaTrainColor.clone();
            result.secondShift=this.secondShift.clone();
            result.diaTrainFont=this.diaTrainFont.clone();
            result.stdOpeTimeHigherColor=this.stdOpeTimeHigherColor.clone();
            result.stdOpeTimeIllegalColor=this.stdOpeTimeIllegalColor.clone();
            result.stdOpeTimeLowerColor=this.stdOpeTimeLowerColor.clone();
            result.stdOpeTimeUndefColor=this.stdOpeTimeUndefColor.clone();
            result.timeTableVFont=this.timeTableVFont.clone();
            result.diagram=new ArrayList<>();
            for(Diagram d:diagram){
                Diagram newDia=d.clone(result);
                result.diagram.add(newDia);
            }
            result.station=new ArrayList<>();
            for(Station s:this.station){
                Station newS=s.clone(result);
                newS.lineFile=result;

                result.station.add(newS);
            }
            result.trainType=new ArrayList<>();
            for(TrainType t:this.trainType){
                result.trainType.add(t.clone());
            }
            result.timeTableBackColor=new ArrayList<>();
            for(Color c:this.timeTableBackColor){
                result.timeTableBackColor.add(c.clone());
            }
            result.timeTableFont=new ArrayList<>();
            for(Font f:this.timeTableFont){
                result.timeTableFont.add(f.clone());
            }
            return result;

        }catch (CloneNotSupportedException e){
            SDlog.log(e);
            return new LineFile();
        }
    }

    /**
     * OuDiaファイルで読み込むと番線情報がないため、
     * oud2として扱うには不都合があります。
     *
     * 必要な数の番線を追加する事で解結を図ります
     */
    public void convertToOud2() {
        for (Station s : station) {
            for (int i = s.tracks.size(); i <= s.stopMain[Train.DOWN] || i <= s.stopMain[Train.UP]; i++) {
                StationTrack track = new StationTrack();
                track.trackName = (i + 1) + "番線";
                track.trackShortName = (i + 1) + "";
                s.tracks.add(track);
            }
        }
    }

    /**
     * oudiaのborderをoudia2ndのbruchStationの考えに合わせます。
     */
    public void checkBorderStation(){
        //
         for(int index=0;index<getStationNum();index++){
             Station station=getStation(index);
             if(station.border){
                 for(int check=index+1;check<getStationNum();check++){
                     if(getStation(check).name.equals(station.name)){
                         station.brunchCoreStationIndex=check;
                     }
                 }
                 for(int check=0;check<index;check++){
                     if(getStation(check).name.equals(getStation(index+1).name)){
                         getStation(index+1).brunchCoreStationIndex=check;                     }
                 }
             }
         }
         //oudiaSecondのborderをoudiaに合わせる
        //OuDiaファイルとして出力する時用
        for(int index=0;index<getStationNum();index++){
            Station station=getStation(index);
            if(station.brunchCoreStationIndex>=0){
                if(station.brunchCoreStationIndex<index){
                    getStation(index-1).border=true;
                }else{
                    station.border=true;
                }

            }
        }
    }

    /*
    =================================
    ここまでOuDiaライブラリ共通の処理
    =================================
     */


    /**
     * lineFileに含まれるダイヤ数
     */
    public int getDiagramNum() {
        return diagram.size();
    }

    /**
     * Diagram取得
     */
    public Diagram getDiagram(int index) {
        return diagram.get(index);
    }

    public int getTrainNum(int diaIndex,int direction){
        return diagram.get(diaIndex).getTrainNum(direction);
    }
    /**
     * Train取得
     */
    public Train getTrain(int diaIndex, int direction, int trainIndex) {
        return diagram.get(diaIndex).getTrain(direction,trainIndex);
    }

    /**
     * TrainType取得
     * @return
     */
    public ArrayList<TrainType> getTrainType() {
        return trainType;
    }

    /**
     * ダイヤグラムに含まれる駅数を返します
     * @return
     */
    public int getStationNum(){
        return station.size();
    }

    /**
     * 駅取得
     */
    public Station getStation(int index) {
        return station.get(index);
    }

    /**
     * 駅間の最短所要時間
     * 単位は秒
     * 起点駅を0秒
     * そこから各駅までの所要時間の累計を配列の形で格納
     */
    private ArrayList<Integer> stationTime=new ArrayList<>();

    /**
     * 最短所要時刻を取得する
     */
    public ArrayList<Integer> getStationTime(){
        if(stationTime.size()!=station.size()){
            //駅数の変更があるときは再計算する。
            calcStationTime();
        }
        return stationTime;
    }

    /**
     * 最短所要時刻を再計算する。
     * この処理は比較的重いので注意
     */
    private void calcStationTime(){
        stationTime=new ArrayList<>();
        stationTime.add(0);
        for(Diagram dia:diagram){
            if(dia.name.equals("基準運転時分")){

                for(int i=1;i<getStationNum();i++){
                    int minTime=100000;
                    for(Train train:dia.trains[0]){
                        if(train.timeExist(i-1)&&train.timeExist(i)){
                            int time=train.reqTime(i-1,i);
                            if(time<0)continue;

                            if(train.getStopType(i-1)!=StationTime.STOP_TYPE_STOP){
                                time+=30;
                            }
                            if(train.getStopType(i)!=StationTime.STOP_TYPE_STOP){
                                time+=30;
                            }
                            if(time<minTime){
                                minTime=time;
                            }
                        }
                    }
                    for(Train train:dia.trains[1]){
                        if(train.timeExist(i-1)&&train.timeExist(i)){
                            int time=train.reqTime(i-1,i);
                            if(time<0)continue;
                            if(train.getStopType(i-1)!=StationTime.STOP_TYPE_STOP){
                                time+=30;
                            }
                            if(train.getStopType(i)!=StationTime.STOP_TYPE_STOP){
                                time+=30;
                            }
                            if(time<minTime){
                                minTime=time;
                            }
                        }
                    }
                    if(minTime>90000){
                        minTime=180;
                    }
                    stationTime.add(stationTime.get(stationTime.size()-1)+minTime);

                }

                return;
            }
        }
        for(int i=1;i<getStationNum();i++){
            int minTime=100000;
            for(Diagram dia:diagram) {
                for (Train train : dia.trains[0]) {
                    if (train.timeExist(i - 1) && train.timeExist(i)) {
                        int time = train.reqTime(i - 1, i);
                        if(time<0)continue;

                        if (train.getStopType(i - 1) != StationTime.STOP_TYPE_STOP) {
                            time += 30;
                        }
                        if (train.getStopType(i) != StationTime.STOP_TYPE_STOP) {
                            time += 30;
                        }
                        if (time < minTime) {
                            minTime = time;
                        }
                    }
                }
                for (Train train : dia.trains[1]) {
                    if (train.timeExist(i - 1) && train.timeExist(i)) {
                        int time = train.reqTime(i - 1, i);
                        if(time<0)continue;

                        if (train.getStopType(i - 1) != StationTime.STOP_TYPE_STOP) {
                            time += 30;
                        }
                        if (train.getStopType(i) != StationTime.STOP_TYPE_STOP) {
                            time += 30;
                        }
                        if (time < minTime) {
                            minTime = time;
                        }
                    }
                }
            }
            if(minTime>90000){
                minTime=180;
            }
            stationTime.add(stationTime.get(stationTime.size()-1)+minTime);

        }

    }

    /**
     * 新しいTrainTypeを挿入します
     * 全ての列車の列車種別を書き換えます
     */
    public void addTrainType(int index, TrainType newType) {
        if (index < 0 || index > trainType.size()) {
            index = trainType.size();
        }
        trainType.add(index, newType);
        if (index == trainType.size()) return;
        for (Diagram dia : diagram) {
            for (Train train : dia.trains[0]) {
                if (train.type >= index) {
                    train.type++;
                }
            }
            for (Train train : dia.trains[1]) {
                if (train.type >= index) {
                    train.type++;
                }
            }
        }

    }

    /**
     * 列車種別を削除する
     * return =false:路線内にこの種別を使っているやつがいるから削除できない
     * true:削除成功
     *
     * @param type
     * @return
     */
    public boolean deleteTrainType(TrainType type) {
        int index = trainType.indexOf(type);
        if (index < 0) {
            return false;
        }
        for (Diagram dia : diagram) {
            for (Train train : dia.trains[0]) {
                if (train.type == index) {
                    return false;
                }
            }
            for (Train train : dia.trains[1]) {
                if (train.type == index) {
                    return false;
                }
            }
        }
        trainType.remove(type);
        for (Diagram dia : diagram) {
            for (Train train : dia.trains[0]) {
                if (train.type >= index) {
                    train.type--;
                }
            }
            for (Train train : dia.trains[1]) {
                if (train.type >= index) {
                    train.type--;
                }
            }
        }
        return true;
    }

    /**
     * brunch:trueの時追加した駅の列車停タイプが経由なしになる。
     */
    public void addStation(int index,Station newStation,boolean brunch){
        if(index<0||index>=getStationNum()){
            index=getStationNum();
        }
        for(Station s:station){
            if(s.brunchCoreStationIndex>=index){
                s.brunchCoreStationIndex++;
            }
            if(s.loopOriginStationIndex>=index){
                s.loopOriginStationIndex++;
            }
        }
        for(Diagram dia:diagram){
            for(int direction=0;direction<2;direction++){
                for(Train train:dia.trains[direction]){
                    train.addNewStation(index,brunch);
                }
            }
        }
        station.add(index,newStation);
    }

    /**
     * 返り値
     * 0:削除成功
     * -1:削除失敗(範囲外)
     * -2:削除失敗(分岐元設定されている)
     */
    public int deleteStation(int index){
        if(index<0||index>=getStationNum()){
            return -1;
        }
        for(Station s:station){
            if(s.brunchCoreStationIndex==index){
                return -2;
            }
            if(s.loopOriginStationIndex==index){
                s.loopOriginStationIndex=-1;
            }
        }
        for(Diagram dia:diagram){
            for(int direction=0;direction<2;direction++){
                for(Train train:dia.trains[direction]){
                    train.stationTimes.remove(index);
                }
            }
        }
        station.remove(index);
        return 0;
    }

    public void makeSubLine(int startStation,int endStation,boolean userOuterStation){


        if(userOuterStation) {
            int startStationOuterShift = getStation(startStation).outerTerminals.size();
            int endStationOuterShift = getStation(endStation).outerTerminals.size();
            for (int i = 0; i < startStation; i++) {
                getStation(startStation).outerTerminals.add(new OuterTerminal(getStation(i).name));
            }
            for (int i = endStation + 1; i < getStationNum(); i++) {
                getStation(endStation).outerTerminals.add(new OuterTerminal(getStation(i).name));
            }
            for (Diagram dia : diagram) {
                for (Train train : dia.trains[0]) {
                    if(train.getStartStation()<0){
                        continue;
                    }
                    if(train.getEndStation()<0){
                        continue;
                    }
                    if (train.getStartStation() < startStation) {
                        StationTimeOperation operation = new StationTimeOperation();
                        operation.operationType = 4;
                        operation.intData1 = train.getStartStation() + startStationOuterShift;
                        operation.time1=train.getTime(train.getStartStation(),Train.DEPART,true);
                        train.stationTimes.get(startStation).beforeOperations.add(operation);
                    }
                    if (train.getEndStation() > endStation) {
                        StationTimeOperation operation = new StationTimeOperation();
                        operation.operationType = 4;
                        operation.intData1 = train.getEndStation() - endStation + endStationOuterShift - 1;
                        operation.time1=train.getTime(train.getEndStation(),Train.ARRIVE,true);
                        train.stationTimes.get(endStation).afterOperations.add(operation);
                    }
                }
                for (Train train : dia.trains[1]) {
                    if (train.getEndStation() < startStation) {
                        StationTimeOperation operation = new StationTimeOperation();
                        operation.operationType = 4;
                        operation.intData1 = train.getEndStation() + startStationOuterShift;
                        operation.time1=train.getTime(train.getEndStation(),Train.ARRIVE,true);

                        train.stationTimes.get(startStation).afterOperations.add(operation);
                    }
                    if (train.getStartStation() > endStation) {
                        StationTimeOperation operation = new StationTimeOperation();
                        operation.operationType = 4;
                        operation.intData1 = train.getStartStation() - endStation + endStationOuterShift - 1;
                        operation.time1=train.getTime(train.getStartStation(),Train.DEPART,true);
                        train.stationTimes.get(endStation).beforeOperations.add(operation);
                    }
                }
            }
            for(int i=startStationOuterShift;i<getStation(startStation).outerTerminals.size();i++){
                if(getStation(startStation).deleteOuterTerminal(i)){
                    i--;
                }
            }
            for(int i=endStationOuterShift;i<getStation(endStation).outerTerminals.size();i++){
                if(getStation(endStation).deleteOuterTerminal(i)){
                    i--;
                }
            }
        }
        for(int i=getStationNum()-1;i>endStation;i--){
            deleteStation(i);

        }
        for(int i=startStation-1;i>=0;i--){
            deleteStation(i);

        }
        getStation(0).showDepartureCustom[Train.DOWN]=true;
        getStation(0).showDepartureCustom[Train.UP]=false;
        getStation(0).showArrivalCustom[Train.DOWN]=false;
        getStation(0).showArrivalCustom[Train.UP]=true;
        getStation(getStationNum()-1).showDepartureCustom[Train.DOWN]=false;
        getStation(getStationNum()-1).showDepartureCustom[Train.UP]=true;
        getStation(getStationNum()-1).showArrivalCustom[Train.DOWN]=true;
        getStation(getStationNum()-1).showArrivalCustom[Train.UP]=false;


    }

    public void addLineFile(int insertPos,LineFile other){
        LineFile lineFile=other.clone();
        String stationName=getStation(insertPos).name;
        int type=0;
        if(lineFile.getStation(0).name.equals(stationName)){
            for(int i=0;i<insertPos;i++){
                lineFile.addStation(i,getStation(i).clone(lineFile),true);
            }
            for(int i=insertPos;i<getStationNum();i++){
                lineFile.addStation(lineFile.getStationNum(),getStation(i).clone(lineFile),true);
            }

            for(int i=0;i<other.getStationNum();i++){
                this.addStation(insertPos+i,other.getStation(i).clone(this),true);

            }
            for(Diagram dia:diagram){
                for(Train train:dia.trains[0]){
                    train.setStopType(insertPos,train.getStopType(insertPos+other.getStationNum()));
                    train.setTime(insertPos,Train.ARRIVE,train.getTime(insertPos+other.getStationNum(),Train.ARRIVE,true));
                    train.setTime(insertPos+other.getStationNum(),Train.ARRIVE,-1);
                }
                for(Train train:dia.trains[1]){
                    train.setStopType(insertPos,train.getStopType(insertPos+other.getStationNum()));
                    train.setTime(insertPos,Train.DEPART,train.getTime(insertPos+other.getStationNum(),Train.DEPART,true));
                    train.setTime(insertPos+other.getStationNum(),Train.DEPART,-1);
                }
            }
            for(Diagram dia:lineFile.diagram){
                boolean frag=false;
                for(Diagram dia2:diagram){
                    if(dia.name.equals(dia2.name)){
                        frag=true;
                        for(Train train:dia.trains[0]){
                            dia2.addTrain(0,-1,train.clone(this));
                        }
                        for(Train train:dia.trains[1]){
                            dia2.addTrain(1,-1,train.clone(this));
                        }
                        break;
                    }
                }
                if(!frag){
                    lineFile.diagram.add(dia.clone(this));
                }
            }
            station.get(insertPos).showArrivalCustom[Train.DOWN]=true;
            station.get(insertPos).showArrivalCustom[Train.UP]=true;
            station.get(insertPos).showDepartureCustom[Train.DOWN]=true;
            station.get(insertPos).showDepartureCustom[Train.UP]=true;
            station.get(insertPos+other.getStationNum()).brunchCoreStationIndex=insertPos;
            if(getStationNum()==insertPos+other.getStationNum()+1){
                deleteStation(insertPos+other.getStationNum());
            }
        }
        else if(lineFile.getStation(lineFile.getStationNum()-1).name.equals(stationName)){
            for(int i=0;i<=insertPos;i++){
                lineFile.addStation(i,getStation(i).clone(lineFile),true);
            }
            for(int i=insertPos+1;i<getStationNum();i++){
                lineFile.addStation(lineFile.getStationNum(),getStation(i).clone(lineFile),true);
            }

            for(int i=0;i<other.getStationNum();i++){
                this.addStation(insertPos+1+i,other.getStation(i).clone(this),true);
            }
            for(Diagram dia:diagram){
                for(Train train:dia.trains[0]){
                    train.setStopType(insertPos+other.getStationNum(),train.getStopType(insertPos));
                    train.setTime(insertPos+other.getStationNum(),Train.DEPART,train.getTime(insertPos,Train.DEPART,true));
                    train.setTime(insertPos,Train.DEPART,-1);
                }
                for(Train train:dia.trains[1]){
                    train.setStopType(insertPos+other.getStationNum(),train.getStopType(insertPos));
                    train.setTime(insertPos+other.getStationNum(),Train.ARRIVE,train.getTime(insertPos,Train.ARRIVE,true));
                    train.setTime(insertPos,Train.ARRIVE,-1);
                }
            }

            for(Diagram dia:lineFile.diagram){
                boolean frag=false;
                for(Diagram dia2:diagram){
                    if(dia.name.equals(dia2.name)){
                        frag=true;
                        for(Train train:dia.trains[0]){
                            dia2.addTrain(0,-1,train.clone(this));
                        }
                        for(Train train:dia.trains[1]){
                            dia2.addTrain(1,-1,train.clone(this));
                        }
                        break;
                    }
                }
                if(!frag){
                    lineFile.diagram.add(dia.clone(this));
                }
            }
            station.get(insertPos+other.getStationNum()).showArrivalCustom[Train.DOWN]=true;
            station.get(insertPos+other.getStationNum()).showArrivalCustom[Train.UP]=true;
            station.get(insertPos+other.getStationNum()).showDepartureCustom[Train.DOWN]=true;
            station.get(insertPos+other.getStationNum()).showDepartureCustom[Train.UP]=true;
            station.get(insertPos).brunchCoreStationIndex=insertPos+other.getStationNum()-1;
            if(insertPos==0){
                deleteStation(0);
            }
        }
        else{
            for(int i=0;i<=insertPos;i++){
                lineFile.addStation(i,getStation(i).clone(lineFile),true);
            }
            for(int i=insertPos;i<getStationNum();i++){
                lineFile.addStation(lineFile.getStationNum(),getStation(i).clone(lineFile),true);
            }
            this.addStation(insertPos+1,getStation(insertPos).clone(this),true);

            for(int i=0;i<other.getStationNum();i++){
                this.addStation(insertPos+1+i,other.getStation(i).clone(this),true);
            }
            for(Diagram dia:diagram){
                for(Train train:dia.trains[0]){
                    train.setStopType(insertPos+other.getStationNum()+1,train.getStopType(insertPos));
                    train.setTime(insertPos+other.getStationNum()+1,Train.DEPART,train.getTime(insertPos,Train.DEPART,true));
                    train.setTime(insertPos,Train.DEPART,-1);
                }
                for(Train train:dia.trains[1]){
                    train.setStopType(insertPos+other.getStationNum()+1,train.getStopType(insertPos));
                    train.setTime(insertPos+other.getStationNum()+1,Train.ARRIVE,train.getTime(insertPos,Train.ARRIVE,true));
                    train.setTime(insertPos,Train.ARRIVE,-1);
                }
            }

            for(Diagram dia:lineFile.diagram){
                boolean frag=false;
                for(Diagram dia2:diagram){
                    if(dia.name.equals(dia2.name)){
                        frag=true;
                        for(Train train:dia.trains[0]){
                            dia2.addTrain(0,-1,train.clone(this));
                        }
                        for(Train train:dia.trains[1]){
                            dia2.addTrain(1,-1,train.clone(this));
                        }
                        break;
                    }
                }
                if(!frag){
                    lineFile.diagram.add(dia.clone(this));
                }
            }
            station.get(insertPos+other.getStationNum()+1).brunchCoreStationIndex=insertPos;

            if(insertPos==0){
                deleteStation(0);
            }
            if(getStationNum()==insertPos+other.getStationNum()+1){
                deleteStation(insertPos+other.getStationNum());
            }

        }

    }

    public void reverse() {
        Collections.reverse(station);
        for (Station s : station) {
            s.reverse();
        }
        for (Diagram dia : diagram) {
            for (Train train : dia.trains[0]) {
                Collections.reverse(train.stationTimes);
                train.direction = 1;
            }
            for (Train train : dia.trains[1]) {
                Collections.reverse(train.stationTimes);
                train.direction = 0;
            }
            ArrayList<Train> temp = dia.trains[0];
            dia.trains[0] = dia.trains[1];
            dia.trains[1] = temp;
        }
    }

    public void sortTrain(int diaIndex, int direction, int stationIndex) {
        getDiagram(diaIndex).sortTrain(direction, stationIndex);
    }

}
