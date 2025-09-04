package tcp_chat_system.core;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 聊天消息实体类
 * 
 * 用于封装聊天系统中传递的消息信息，包括：
 * 1. 用户名信息
 * 2. 消息内容
 * 3. 时间戳
 * 4. 消息类型
 * 
 * @author TCP聊天系统实验
 * @version 1.0
 */
public class ChatMessage {
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        USER_MESSAGE,    // 用户普通消息
        USER_JOIN,       // 用户加入提示
        USER_LEAVE,      // 用户离开提示
        SYSTEM_INFO      // 系统信息
    }
    
    private String username;        // 用户名
    private String content;         // 消息内容
    private long timestamp;         // 时间戳
    private MessageType type;       // 消息类型
    
    /**
     * 构造普通用户消息
     * 
     * @param username 用户名
     * @param content 消息内容
     */
    public ChatMessage(String username, String content) {
        this.username = username;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.type = MessageType.USER_MESSAGE;
    }
    
    /**
     * 构造指定类型的消息
     * 
     * @param username 用户名
     * @param content 消息内容
     * @param type 消息类型
     */
    public ChatMessage(String username, String content, MessageType type) {
        this.username = username;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.type = type;
    }
    
    /**
     * 创建系统消息
     * 
     * @param content 系统消息内容
     * @return ChatMessage对象
     */
    public static ChatMessage createSystemMessage(String content) {
        return new ChatMessage("系统", content, MessageType.SYSTEM_INFO);
    }
    
    /**
     * 创建用户加入消息
     * 
     * @param username 加入的用户名
     * @return ChatMessage对象
     */
    public static ChatMessage createJoinMessage(String username) {
        return new ChatMessage(username, username + " 加入了聊天室", MessageType.USER_JOIN);
    }
    
    /**
     * 创建用户离开消息
     * 
     * @param username 离开的用户名
     * @return ChatMessage对象
     */
    public static ChatMessage createLeaveMessage(String username) {
        return new ChatMessage(username, username + " 离开了聊天室", MessageType.USER_LEAVE);
    }
    
    // Getter方法
    public String getUsername() {
        return username;
    }
    
    public String getContent() {
        return content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
    
    /**
     * 获取格式化的时间字符串
     * 
     * @return 格式化后的时间（HH:mm:ss格式）
     */
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * 将消息转换为传输格式的字符串
     * 简单的分隔符格式：type|username|content|timestamp
     * 
     * @return 序列化字符串
     */
    public String serialize() {
        return type.name() + "|" + username + "|" + content + "|" + timestamp;
    }
    
    /**
     * 从传输格式的字符串解析消息
     * 
     * @param serialized 序列化字符串
     * @return ChatMessage对象，解析失败返回null
     */
    public static ChatMessage deserialize(String serialized) {
        try {
            String[] parts = serialized.split("\\|", 4);
            if (parts.length != 4) {
                return null;
            }
            
            MessageType type = MessageType.valueOf(parts[0]);
            String username = parts[1];
            String content = parts[2];
            long timestamp = Long.parseLong(parts[3]);
            
            ChatMessage message = new ChatMessage(username, content, type);
            message.timestamp = timestamp; // 保持原始时间戳
            return message;
        } catch (Exception e) {
            System.err.println("消息解析失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取显示格式的消息字符串
     * 
     * @return 格式化的显示字符串
     */
    public String getDisplayString() {
        switch (type) {
            case USER_MESSAGE:
                return String.format("[%s] %s: %s", getFormattedTime(), username, content);
            case USER_JOIN:
            case USER_LEAVE:
                return String.format("[%s] >> %s", getFormattedTime(), content);
            case SYSTEM_INFO:
                return String.format("[%s] [系统] %s", getFormattedTime(), content);
            default:
                return String.format("[%s] %s", getFormattedTime(), content);
        }
    }
    
    @Override
    public String toString() {
        return getDisplayString();
    }
}