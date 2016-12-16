package com.lee.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.lee.util.EndTime;

/**
 * Created by Lee on 2016/12/16.
 */
@ServerEndpoint("/websocket")

public class WebService {

  /* 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。 */
  private static int onlineCount = 0;

  /*
   * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，
   * 其中Key可以为用户标识
   */
  // private static CopyOnWriteArraySet<Session> sessions = new
  // CopyOnWriteArraySet<Session>();

  private static Map<String, Session[]> room = new HashMap<String, Session[]>();

  private static Set<String> flags = new HashSet<String>();

  private static List<Session> waitRoom = new ArrayList<Session>();

  private static Session[] waitsessions = new Session[2];

  /* 与某个客户端的连接会话，需要通过它来给客户端发送数据 */
  private Session session;

  public WebService() {
    System.out.println("构造函数!");
  }

  @OnOpen
  public void onOpen(Session session) {
    /* 当有玩家加入,先将玩家加入等候室,等候室满5人后再加入创建房间开始 */
    addOnlineCount();
    waitRoom.add(session);
    if (waitRoom.size() == waitsessions.length) {
      for (int i = 0; i < waitsessions.length; i++) {
        waitsessions[i] = waitRoom.get(i);
      }
      String flag = makeRoom(waitsessions);
      flags.add(flag);
      waitRoom.clear();
      // sendMessage(room.get(flag),"s");
      Thread t = new Thread(new EndTime(room.get(flag), flag));
      t.start();
    }
    System.out
        .println("有新玩家加入,当前人数为:" + getOnlineCount() + "当前房间数为:" + room.size());

    /*
     * this.session = session; sessions.add(this.session); // 加入set中
     * addOnlineCount(); // 在线数加1 String flag = ""; int num = 0; StringBuffer
     * planes = new StringBuffer(); if (haveNoRoom()) { flag =
     * makeRoom(session); } else { for (String string : flags) { Session[] ss =
     * room.get(string); for (int i = 0; i < ss.length; i++) { if (ss[i] !=
     * null) { planes.append(i + ","); } } for (int i = 0; i < ss.length; i++) {
     * if (ss[i] == null) { ss[i] = session; flag = string; num = i; break; } }
     * } } sendMessageToSession(session, "s:" + flag + ":" + num + ":" +
     * planes.toString()); System.err.println(room.get(flag)[0]);
     * sendMessage(room.get(flag), "j:" + num);
     * System.out.println("有新连接加入！当前在线人数为:" + getOnlineCount() + "::当前房间数:" +
     * room.size());
     */
  }

  @OnClose
  public void onClose() {
    subOnlineCount();
    System.out.println("一连接断开!当前人数为:" + getOnlineCount());
  }

  @OnMessage
  public void onMessage(String string, Session session) {
    System.out.println("接受到消息:" + string);
  }

  @OnError
  public void onError(Session session, Throwable error) {
    System.err.println("发送错误!连接中断");
    error.printStackTrace();
  }

  public static synchronized int getOnlineCount() {
    return onlineCount;
  }

  public static synchronized void addOnlineCount() {
    WebService.onlineCount++;
  }

  public static synchronized void subOnlineCount() {
    WebService.onlineCount--;
  }

  /*
   * private boolean haveNoRoom() { if (flags.size() == 0) { return true; } for
   * (String string : flags) { Session[] ss = room.get(string); for (Session
   * session : ss) { if (session == null) { return false; } } } return true; }
   */

  private String makeRoom(Session[] sessions) {
    String flag = createFlag();
    room.put(flag, sessions);
    flags.add(flag);
    return flag;
  }

  private String createFlag() {
    String str = "qwertyuiopasdfghjklzxcvbnm0123456789";
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < 5; i++) {
      sb.append(str.charAt((int) (Math.random() * 36)));
    }
    return sb.toString();
  }

  /**
   * sendMessageToSession:(单个发消息). <br/>
   * 单个发消息.<br/>
   *
   * @author lhb
   * @param session
   * @param text
   *          void
   * @since JDK 1.7
   */
  private void sendMessageToSession(Session session, String text) {
    synchronized (this) {
      try {
        session.getBasicRemote().sendText(text);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * sendMessage:(批量发消息). <br/>
   * 批量发消息.<br/>
   *
   * @author lhb
   * @param sessions
   * @param message
   *          void
   * @since JDK 1.7
   */
  private void sendMessage(Session[] sessions, String message) {
    synchronized (this) {
      for (Session session : sessions) {
        if (session != null) {
          try {
            session.getBasicRemote().sendText(message);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

}
