package com.commoncore.contanst;

public class NotificationConstant {

    // --- KAFKA TOPICS ---
    public static final String RELATION_EVENT_TOPIC = "relation-out-events";
    public static final String POST_EVENT_TOPIC = "post-out-events";

    // --- WEBSOCKET ENDPOINTS & PATHS ---
    public static final String WS_ENDPOINT = "/ws-notification";

    // Tiền tố cho các topic (Broker)
    public static final String TOPIC_PREFIX = "/topic";
    public static final String QUEUE_PREFIX = "/queue";

    // Đường dẫn cụ thể cho thông báo cá nhân
    // Client sẽ sub: /user/queue/notifications
    public static final String USER_NOTIF_DESTINATION = "/queue/notifications";

    // --- NOTIFICATION TYPES ---
    public static final String TYPE_FRIEND_REQUEST = "FRIEND_REQUEST";
    public static final String TYPE_ACCEPT_FRIEND = "ACCEPT_FRIEND";
    public static final String TYPE_LIKE_POST = "LIKE_POST";
}