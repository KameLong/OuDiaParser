package com.kamelong.tool;

public interface LoggerInterface {
    void showToUser(String value);
    void printToConsole(String value);
    void stackTrace(Exception e);

}
