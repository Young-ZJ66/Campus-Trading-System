package com.campus.service;

import com.campus.pojo.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 实时通知服务：通过 WebSocket 向指定用户推送通知
 */
@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向指定用户推送通知
     * @param userId 目标用户ID
     * @param notification 通知内容
     */
    public void sendToUser(Long userId, Notification notification) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications",
                notification
        );
    }

    /**
     * 广播通知给所有在线用户
     */
    public void broadcast(Notification notification) {
        messagingTemplate.convertAndSend("/topic/broadcast", notification);
    }
}
