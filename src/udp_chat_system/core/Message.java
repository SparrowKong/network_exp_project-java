package udp_chat_system.core;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UDP聊天系统消息封装类
 * 用于在客户端和服务端之间传输聊天消息和控制信息
 * 
 * 主要功能：
 * - 消息序列化和反序列化
 * - 支持多种消息类型（连接、聊天、断开等）
 * - 时间戳和用户信息管理
 * 
 * 设计说明：
 * 使用简单的字符串格式进行消息传输，格式为：
 * TYPE|USERNAME|CONTENT|TIMESTAMP
 */
public class Message {
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        CONNECT("CONNECT"),     // 客户端连接消息
        CHAT("CHAT"),           // 聊天消息
        DISCONNECT("DISCONNECT"), // 断开连接消息
        SERVER_INFO("SERVER_INFO"), // 服务端信息消息
        HEARTBEAT("HEARTBEAT"); // 心跳消息
        
        private final String value;
        
        MessageType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static MessageType fromString(String value) {
            for (MessageType type : MessageType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return CHAT; // 默认为聊天消息
        }
    }
    
    // 消息属性
    private MessageType type;
    private String username;
    private String content;
    private String timestamp;
    
    // 消息分隔符
    private static final String DELIMITER = "|";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    /**
     * 构造函数 - 创建新消息
     * @param type 消息类型
     * @param username 用户名
     * @param content 消息内容
     */
    public Message(MessageType type, String username, String content) {
        this.type = type;
        this.username = username != null ? username : "匿名用户";
        this.content = content != null ? content : "";
        this.timestamp = DATE_FORMAT.format(new Date());
    }
    
    /**
     * 私有构造函数 - 用于反序列化
     */
    private Message(MessageType type, String username, String content, String timestamp) {
        this.type = type;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
    }
    
    /**
     * 将消息序列化为字符串
     * @return 序列化后的字符串
     */
    public String serialize() {
        return type.getValue() + DELIMITER + 
               username + DELIMITER + 
               content + DELIMITER + 
               timestamp;
    }
    
    /**
     * 从字符串反序列化消息
     * @param data 序列化的字符串
     * @return Message对象，如果解析失败返回null
     */
    public static Message deserialize(String data) {
        try {
            if (data == null || data.trim().isEmpty()) {
                return null;
            }
            
            String[] parts = data.split("\\|", -1); // -1保留空字符串
            if (parts.length != 4) {
                System.err.println("消息格式错误，期望4个部分，实际：" + parts.length);
                return null;
            }
            
            MessageType type = MessageType.fromString(parts[0]);
            String username = parts[1];
            String content = parts[2];
            String timestamp = parts[3];
            
            return new Message(type, username, content, timestamp);
        } catch (Exception e) {
            System.err.println("消息反序列化失败：" + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建连接消息
     * @param username 用户名
     * @return 连接消息对象
     */
    public static Message createConnectMessage(String username) {
        return new Message(MessageType.CONNECT, username, username + " 加入了聊天室");
    }
    
    /**
     * 创建聊天消息
     * @param username 用户名
     * @param content 聊天内容
     * @return 聊天消息对象
     */
    public static Message createChatMessage(String username, String content) {
        return new Message(MessageType.CHAT, username, content);
    }
    
    /**
     * 创建断开连接消息
     * @param username 用户名
     * @return 断开连接消息对象
     */
    public static Message createDisconnectMessage(String username) {
        return new Message(MessageType.DISCONNECT, username, username + " 离开了聊天室");
    }
    
    /**
     * 创建服务端信息消息
     * @param content 服务端信息内容
     * @return 服务端信息消息对象
     */
    public static Message createServerInfoMessage(String content) {
        return new Message(MessageType.SERVER_INFO, "系统", content);
    }
    
    /**
     * 创建心跳消息
     * @param username 用户名
     * @return 心跳消息对象
     */
    public static Message createHeartbeatMessage(String username) {
        return new Message(MessageType.HEARTBEAT, username, "heartbeat");
    }
    
    // Getter方法
    public MessageType getType() {
        return type;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    /**
     * 判断是否为聊天消息
     * @return true如果是聊天消息
     */
    public boolean isChatMessage() {
        return type == MessageType.CHAT;
    }
    
    /**
     * 判断是否为系统消息（非聊天消息）
     * @return true如果是系统消息
     */
    public boolean isSystemMessage() {
        return type != MessageType.CHAT;
    }
    
    /**
     * 获取格式化的显示文本
     * @return 格式化后的消息文本
     */
    public String getFormattedMessage() {
        switch (type) {
            case CONNECT:
            case DISCONNECT:
            case SERVER_INFO:
                return String.format("[%s] %s", timestamp, content);
            case CHAT:
                return String.format("[%s] %s: %s", timestamp, username, content);
            case HEARTBEAT:
                return ""; // 心跳消息不显示
            default:
                return String.format("[%s] %s: %s", timestamp, username, content);
        }
    }
    
    /**
     * 重写toString方法
     * @return 消息的字符串表示
     */
    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", username='" + username + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
    
    /**
     * 验证消息内容是否有效
     * @return true如果消息有效
     */
    public boolean isValid() {
        if (type == null || username == null || content == null || timestamp == null) {
            return false;
        }
        
        // 聊天消息不能为空
        if (type == MessageType.CHAT && content.trim().isEmpty()) {
            return false;
        }
        
        // 用户名不能为空或包含分隔符
        if (username.trim().isEmpty() || username.contains(DELIMITER)) {
            return false;
        }
        
        return true;
    }
}