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
    try {
      for (int j = 0; j < sessions.length; j++) {
        if (sessions[j] != null) {
          try {
            sessions[j].getBasicRemote().sendText("j:" + flag + ":" + j);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      Thread.sleep(5000);
      for (Session session : sessions) {
        try {
          session.getBasicRemote().sendText("s:");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
