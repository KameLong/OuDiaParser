package com.kamelong.tool;

/*
 * Copyright (c) 2019 KameLong
 * contact:kamelong.com
 *
 * This source code is released under GNU GPL ver3.
 */

/**
 * Logを出力させるためのstatic class
 */

public class Logger {
    /**
     * Logger実態クラスを登録する
     */
    private static LoggerInterface loggerInterface=null;

    /**
     * LoggerInterfaceを継承するクラスを登録することで、任意の形でLogを出力する事ができます。
     */
    public static void setOnLoggerInterface(LoggerInterface onLoggerInterface){
        loggerInterface=onLoggerInterface;
    }

    /**
     * ユーザーに見せるコメント
     */
    public static void toast(String value){
        if(loggerInterface!=null){
            loggerInterface.showToUser(value);
        }else{
            System.out.println(value);
        }
    }
    public static void log(Exception e){
        if(loggerInterface!=null){
            loggerInterface.stackTrace(e);
        }else{
            e.printStackTrace();
        }
    }
    public static void log(Object value) {
        if(loggerInterface!=null){
            loggerInterface.printToConsole(value.toString());
        }else{
            System.out.println(value);
        }
    }
    public static void log(Object value1,Object value2) {
        if(loggerInterface!=null){
            loggerInterface.printToConsole(""+value1+","+value2);
        }else{
            System.out.println(""+value1+","+value2);
        }
    }

}
