package com.inzapp.foodcam.utils;

// 커맨드 키 : 서버에서 해당 어플리케이션에서 접속을 요청하는지를 구분하기 위한 구분자
public abstract class Cmd {
    private static final String COMMAND_KEY = "[COMMAND_SERVER_FOOD_MATCHER]";
    public static final String SERVER_KEY = COMMAND_KEY + "[SERVER_KEY_SERVER_FOOD_MATCHER]";
    public static final String SERVER_IS_BUSY = COMMAND_KEY + "[SERVER_IS_BUSY]";
}