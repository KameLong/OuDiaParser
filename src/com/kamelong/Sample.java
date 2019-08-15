package com.kamelong;

import com.kamelong.OuDia.DiaFile;
import java.io.File;

public class Sample {
    public static void main(String[] args){
        //入力ファイルパス
        String inputOudPath="C:\\Users\\kame\\Documents\\sample.oud";
        //出力ファイルパス(今回はoud形式とoud2形式の2つを用意した)
        String outputOudPath="C:\\Users\\kame\\Documents\\sample2.oud";
        String outputOud2Path="C:\\Users\\kame\\Documents\\sample2.oud2";
        try{

            //読み込み
            DiaFile diaFile=new DiaFile(new File(inputOudPath));

            //路線名表示テスト
            System.out.println("路線名:"+diaFile.name);
            //列車数表示テスト
            if(diaFile.diagram.size()!=0){
                System.out.println(diaFile.diagram.get(0).name+" の下り列車数は"+diaFile.diagram.get(0).trains[0].size()+"です");
            }else{
                System.out.println("路線ファイル内にダイヤが存在しません");
            }

            //OuDia形式で出力
            diaFile.saveToOuDiaFile(outputOudPath);

            //読み込みがOuDiaファイルで書き込みがOuDia2nd形式の時に必要
            diaFile.ConvertOudToOud2nd();

            //OuDia2nd形式で出力
            diaFile.saveToFile(outputOud2Path);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
