package com.kamelong;

import com.kamelong.OuDia.DiaFile;
import java.io.File;

public class Main {
    public static void main(String[] args){
        try{
            //読み込み
            DiaFile diaFile=new DiaFile(new File("C:\\Users\\kame\\Documents\\sample.oud"));

            //OuDia形式で出力
            diaFile.saveToOuDiaFile("C:\\Users\\kame\\Documents\\sample2.oud");

            //読み込みがOuDiaファイルで書き込みがOuDia2nd形式の時に必要
            diaFile.ConvertOudToOud2nd();

            //OuDia2nd形式で出力
            diaFile.saveToFile("C:\\Users\\kame\\Documents\\sample2.oud2");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
