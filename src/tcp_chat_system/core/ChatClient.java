package tcp_chat_system.core;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TCP聊天客户端核心实现
 * 
 * 功能特性：
 * 1. 连接到聊天服务器
 * 2. 发送和接收消息的独立线程处理
 * 3. 自动重连机制（可选）
 * 4. 连接状态监控
 * 5. 消息队列管理
 * 
 * @author TCP聊天系统实验
 * @version 1.0
 */
public class ChatClient {
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;
    
    private String serverHost;                          // 服务器地址
    private int serverPort;                             // 服务器端口
    private String username;                            // 用户名
    
    private Socket socket;                              // 客户端套接字
    private BufferedReader reader;                      // 输入流读取器
    private PrintWriter writer;                         // 输出流写入器
    
    private boolean connected;                          // 连接状态
    private Thread receiveThread;                       // 消息接收线程
    
    // 消息队列用于线程间通信
    private BlockingQueue<String> messageQueue;         // 待发送消息队列
    private Thread sendThread;                          // 消息发送线程
    
    // 回调接口用于处理接收到的消息
    private MessageCallback messageCallback;
    
    /**
     * 消息回调接口
     */
    public interface MessageCallback {
        /**
         * 处理接收到的消息
         * 
         * @param message 接收到的消息
         */
        void onMessageReceived(ChatMessage message);
        
        /**
         * 处理系统通知（非消息类型）
         * 
         * @param notification 系统通知字符串
         */
        void onSystemNotification(String notification);
        
        /**
         * 处理连接状态变化
         * 
         * @param connected 新的连接状态
         */
        void onConnectionStatusChanged(boolean connected);
    }
    
    /**
     * 构造函数 - 使用默认服务器地址和端口
     */
    public ChatClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }
    
    /**
     * 构造函数 - 指定服务器地址和端口
     * 
     * @param host 服务器地址
     * @param port 服务器端口
     */
    public ChatClient(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        this.connected = false;
        this.messageQueue = new LinkedBlockingQueue<>();
    }
    
    /**
     * 设置消息回调处理器
     * 
     * @param callback 消息回调处理器
     */
    public void setMessageCallback(MessageCallback callback) {
        this.messageCallback = callback;
    }
    
    /**
     * 连接到服务器
     * 
     * @param username 用户名
     * @return 连接成功返回true
     */
    public boolean connect(String username) {
        if (connected) {
            System.out.println("客户端已连接，无需重复连接");
            return true;
        }
        
        this.username = username;
        
        try {
            // 创建Socket连接
            socket = new Socket(serverHost, serverPort);
            
            // 初始化输入输出流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            
            connected = true;
            
            // 启动消息接收线程
            startReceiveThread();
            
            // 启动消息发送线程
            startSendThread();
            
            // 发送用户名进行身份验证
            writer.println(username);
            
            System.out.println("成功连接到服务器: " + serverHost + ":" + serverPort);
            
            // 通知连接状态变化
            if (messageCallback != null) {
                messageCallback.onConnectionStatusChanged(true);
            }
            
            return true;
            
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            connected = false;
            
            // 通知连接失败
            if (messageCallback != null) {
                messageCallback.onConnectionStatusChanged(false);
            }
            
            return false;
        }
    }
    
    /**
     * 断开与服务器的连接
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        connected = false;
        
        // 停止发送线程
        if (sendThread != null && sendThread.isAlive()) {
            sendThread.interrupt();
        }
        
        // 停止接收线程
        if (receiveThread != null && receiveThread.isAlive()) {
            receiveThread.interrupt();
        }
        
        // 关闭资源
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("关闭连接时发生错误: " + e.getMessage());
        }
        
        // 通知连接状态变化
        if (messageCallback != null) {
            messageCallback.onConnectionStatusChanged(false);
        }
        
        System.out.println("已断开与服务器的连接");
    }
    
    /**
     * 发送聊天消息
     * 
     * @param content 消息内容
     */
    public void sendMessage(String content) {
        if (!connected) {
            System.err.println("未连接到服务器，无法发送消息");
            return;
        }
        
        if (content == null || content.trim().isEmpty()) {
            System.err.println("消息内容不能为空");
            return;
        }
        
        // 创建聊天消息
        ChatMessage message = new ChatMessage(username, content.trim());
        
        // 将消息添加到发送队列
        try {
            messageQueue.put(message.serialize());
        } catch (InterruptedException e) {
            System.err.println("消息发送被中断: " + e.getMessage());
        }
    }
    
    /**
     * 启动消息接收线程
     */
    private void startReceiveThread() {
        receiveThread = new Thread(() -> {
            try {
                String receivedData;
                while (connected && !Thread.currentThread().isInterrupted() 
                       && (receivedData = reader.readLine()) != null) {
                    
                    // 处理接收到的数据
                    handleReceivedData(receivedData);
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("接收消息时发生错误: " + e.getMessage());
                    // 连接中断，尝试重连或断开
                    handleConnectionLoss();
                }
            } finally {
                System.out.println("消息接收线程已停止");
            }
        });
        
        receiveThread.setName("ReceiveThread-" + username);
        receiveThread.start();
    }
    
    /**
     * 启动消息发送线程
     */
    private void startSendThread() {
        sendThread = new Thread(() -> {
            try {
                while (connected && !Thread.currentThread().isInterrupted()) {
                    // 从队列中取出待发送的消息
                    String messageData = messageQueue.take();
                    
                    // 发送消息
                    writer.println(messageData);
                    
                    // 检查发送是否成功
                    if (writer.checkError()) {
                        System.err.println("消息发送失败，连接可能已断开");
                        handleConnectionLoss();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("消息发送线程被中断");
            } finally {
                System.out.println("消息发送线程已停止");
            }
        });
        
        sendThread.setName("SendThread-" + username);
        sendThread.start();
    }
    
    /**
     * 处理接收到的数据
     * 
     * @param receivedData 接收到的原始数据
     */
    private void handleReceivedData(String receivedData) {
        // 尝试解析为聊天消息
        ChatMessage message = ChatMessage.deserialize(receivedData);
        
        if (message != null) {
            // 这是一个有效的聊天消息
            if (messageCallback != null) {
                messageCallback.onMessageReceived(message);
            } else {
                // 如果没有设置回调，直接打印到控制台
                System.out.println(message.getDisplayString());
            }
        } else {
            // 这可能是系统通知或其他信息
            if (messageCallback != null) {
                messageCallback.onSystemNotification(receivedData);
            } else {
                System.out.println(receivedData);
            }
        }
    }
    
    /**
     * 处理连接丢失
     */
    private void handleConnectionLoss() {
        if (connected) {
            System.out.println("检测到连接丢失，正在断开连接...");
            disconnect();
        }
    }
    
    /**
     * 检查连接状态
     * 
     * @return true表示已连接
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * 获取服务器地址
     * 
     * @return 服务器地址
     */
    public String getServerHost() {
        return serverHost;
    }
    
    /**
     * 获取服务器端口
     * 
     * @return 服务器端口
     */
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * 获取待发送消息队列大小
     * 
     * @return 队列中待发送消息数量
     */
    public int getPendingMessageCount() {
        return messageQueue.size();
    }
    
    /**
     * 清空待发送消息队列
     */
    public void clearPendingMessages() {
        messageQueue.clear();
        System.out.println("已清空待发送消息队列");
    }
}