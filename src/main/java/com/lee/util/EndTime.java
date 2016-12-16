package com.lee.util;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by Lee on 2016/12/16.
 */
public class EndTime implements Runnable {

    private Session[] sessions;

    private String flag;

    public EndTime(Session[] sessions, String flag) {
        this.sessions = sessions;
        this.flag = flag;
    }

    public void run() {
        for (int i = 3; i > 0; i--) {
            try {
                for (int j = 0; j < sessions.length; j++) {
                    if (sessions[j] != null) {
                        try {
                            sessions[j].getBasicRemote().sendText("e:" + flag + ":" + j + ":" + i);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
