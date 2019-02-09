package com.inzapp.foodcam.utils;

/**
 * 커맨드 키 : 서버 접속을 위한 공중키
 * abstract 로서 객체화하지 않음을 명시하며 public static 필드를 이용해
 * 프로젝트 전역에서 사용 가능
 */
public abstract class Cmd {
    private static final String COMMAND_KEY = "[COMMAND_SERVER_FOOD_MATCHER]";
    public static final String SERVER_KEY = COMMAND_KEY + "[SERVER_KEY_SERVER_FOOD_MATCHER]";
    public static final String SERVER_IS_BUSY = COMMAND_KEY + "[SERVER_IS_BUSY]";
}