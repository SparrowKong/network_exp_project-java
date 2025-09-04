package udp_chat_system.core;

import java.net.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UDP聊天系统客户端核心类
 * 
 * 主要功能：
 * - 连接服务端并发送消息
 * - 接收服务端和其他客户端的消息
 * - 心跳保活机制
 * - 自动重连功能
 * - 消息处理回调接口
 * 
 * 技术特点：
 * - 使用DatagramSocket实现UDP通信
 * - 多线程处理消息收发
 * - 自动心跳维持连接
 * - 异步消息处理机制
 */
public class UDPClient {
    
    // 客户端配置
    private static final int BUFFER_SIZE = 1024;
    private static final int HEARTBEAT_INTERVAL = 15000; // 15秒心跳间隔
    private static final int CONNECT_TIMEOUT = 5000; // 5秒连接超时
    
    // 网络组件
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private boolean isConnected;
    private String username;
    
    // 线程管理
    private ExecutorService threadPool;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    // 消息处理接口
    private MessageHandler messageHandler;
    
    /**
     * 消息处理回调接口
     * 客户端接收到消息时会调用此接口
     */
    public interface MessageHandler {
        /**
         * 处理接收到的消息
         * @param message 接收到的消息
         */
        void onMessageReceived(Message message);
        
        /**
         * 连接状态改变时调用
         * @param connected true表示已连接，false表示已断开
         */
        void onConnectionStatusChanged(boolean connected);
        
        /**
         * 发生错误时调用
         * @param error 错误信息
         */
        void onError(String error);
    }
    
    /**
     * 构造函数
     * @param serverHost 服务端主机地址
     * @param serverPort 服务端端口
     * @param username 用户名
     * @throws UnknownHostException 如果主机地址无效
     */
    public UDPClient(String serverHost, int serverPort, String username) throws UnknownHostException {
        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.username = username;
        this.isConnected = false;
        this.threadPool = Executors.newFixedThreadPool(3);
    }
    
    /**
     * 设置消息处理器
     * @param handler 消息处理器
     */
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }
    
    /**
     * 连接到服务端
     * @return true如果连接成功
     */
    public boolean connect() {
        if (isConnected) {
            notifyError("客户端已经连接");
            return false;
        }
        
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(CONNECT_TIMEOUT);
            isRunning.set(true);
            
            System.out.println("正在连接服务端: " + serverAddress.getHostAddress() + ":" + serverPort);
            
            // 发送连接消息
            Message connectMessage = Message.createConnectMessage(username);
            if (sendMessage(connectMessage)) {
                isConnected = true;
                notifyConnectionStatus(true);
                
                // 启动消息接收线程
                startMessageReceiver();
                
                // 启动心跳线程
                startHeartbeat();
                
                System.out.println("成功连接到服务端");
                return true;
            } else {
                disconnect();
                return false;
            }
            
        } catch (SocketException e) {
            notifyError("创建Socket失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 断开与服务端的连接
     */
    public void disconnect() {
        if (!isConnected) {
            return;
        }
        
        System.out.println("正在断开连接...");
        
        // 发送断开连接消息
        if (socket != null && !socket.isClosed()) {
            Message disconnectMessage = Message.createDisconnectMessage(username);
            sendMessage(disconnectMessage);
        }
        
        // 停止所有线程
        isRunning.set(false);
        isConnected = false;
        
        // 关闭Socket
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        
        // 关闭线程池
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            threadPool = Executors.newFixedThreadPool(3);
        }
        
        notifyConnectionStatus(false);
        System.out.println("已断开连接");
    }
    
    /**
     * 发送聊天消息
     * @param content 消息内容
     * @return true如果发送成功
     */
    public boolean sendChatMessage(String content) {
        if (!isConnected) {
            notifyError("客户端未连接，无法发送消息");
            return false;
        }
        
        if (content == null || content.trim().isEmpty()) {
            notifyError("消息内容不能为空");
            return false;
        }
        
        Message chatMessage = Message.createChatMessage(username, content.trim());
        return sendMessage(chatMessage);
    }
    
    /**
     * 发送消息到服务端
     * @param message 要发送的消息
     * @return true如果发送成功
     */
    private boolean sendMessage(Message message) {
        if (socket == null || socket.isClosed()) {
            notifyError("Socket已关闭，无法发送消息");
            return false;
        }
        
        try {
            String data = message.serialize();
            byte[] buffer = data.getBytes("UTF-8");
            
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, serverAddress, serverPort);
            
            socket.send(packet);
            return true;
            
        } catch (IOException e) {
            notifyError("发送消息失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 启动消息接收线程
     */
    private void startMessageReceiver() {
        threadPool.submit(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while (isRunning.get() && isConnected) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    // 异步处理接收到的消息
                    threadPool.submit(() -> handleReceivedMessage(packet));
                    
                } catch (SocketTimeoutException e) {
                    // 超时是正常的，继续等待
                    continue;
                } catch (IOException e) {
                    if (isRunning.get() && isConnected) {
                        notifyError("接收消息时发生错误: " + e.getMessage());
                        // 尝试重连
                        handleConnectionLost();
                    }
                    break;
                }
            }
        });
    }
    
    /**
     * 处理接收到的消息
     * @param packet 接收到的数据包
     */
    private void handleReceivedMessage(DatagramPacket packet) {
        try {
            String data = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            Message message = Message.deserialize(data);
            
            if (message == null || !message.isValid()) {
                System.err.println("接收到无效消息: " + data);
                return;
            }
            
            // 通知消息处理器
            if (messageHandler != null) {
                messageHandler.onMessageReceived(message);
            }
            
        } catch (Exception e) {
            notifyError("处理接收消息时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 启动心跳线程
     */
    private void startHeartbeat() {
        threadPool.submit(() -> {
            while (isRunning.get() && isConnected) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                    
                    // 发送心跳消息
                    Message heartbeatMessage = Message.createHeartbeatMessage(username);
                    if (!sendMessage(heartbeatMessage)) {
                        // 心跳发送失败，可能连接已断开
                        handleConnectionLost();
                        break;
                    }
                    
                } catch (InterruptedException e) {
                    if (isRunning.get()) {
                        System.err.println("心跳线程被中断: " + e.getMessage());
                    }
                    break;
                }
            }
        });
    }
    
    /**
     * 处理连接丢失
     */
    private void handleConnectionLost() {
        if (!isConnected) {
            return;
        }
        
        System.err.println("检测到连接丢失，尝试重连...");
        
        // 先断开当前连接
        boolean wasConnected = isConnected;
        disconnect();
        
        if (wasConnected) {
            // 尝试重连
            retryConnection();
        }
    }
    
    /**
     * 重试连接
     */
    private void retryConnection() {
        threadPool.submit(() -> {
            int retryCount = 0;
            int maxRetries = 5;
            int retryInterval = 3000; // 3秒
            
            while (retryCount < maxRetries && isRunning.get()) {
                try {
                    Thread.sleep(retryInterval);
                    retryCount++;
                    
                    System.out.println("重连尝试 " + retryCount + "/" + maxRetries);
                    
                    if (connect()) {
                        System.out.println("重连成功");
                        return;
                    }
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            notifyError("重连失败，已达到最大重试次数");
        });
    }
    
    /**
     * 通知连接状态改变
     * @param connected 连接状态
     */
    private void notifyConnectionStatus(boolean connected) {
        if (messageHandler != null) {
            messageHandler.onConnectionStatusChanged(connected);
        }
    }
    
    /**
     * 通知错误
     * @param error 错误信息
     */
    private void notifyError(String error) {
        System.err.println("客户端错误: " + error);
        if (messageHandler != null) {
            messageHandler.onError(error);
        }
    }
    
    /**
     * 检查是否已连接
     * @return true如果已连接
     */
    public boolean isConnected() {
        return isConnected && isRunning.get();
    }
    
    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * 获取服务端地址信息
     * @return 服务端地址字符串
     */
    public String getServerInfo() {
        return serverAddress.getHostAddress() + ":" + serverPort;
    }
    
    /**
     * 测试与服务端的连接
     * @return true如果连接正常
     */
    public boolean testConnection() {
        if (!isConnected) {
            return false;
        }
        
        try {
            Message testMessage = Message.createHeartbeatMessage(username);
            return sendMessage(testMessage);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 默认消息处理器实现
     * 提供简单的控制台输出功能
     */
    public static class DefaultMessageHandler implements MessageHandler {
        @Override
        public void onMessageReceived(Message message) {
            if (message.getType() == Message.MessageType.HEARTBEAT) {
                // 不显示心跳消息
                return;
            }
            
            String formatted = message.getFormattedMessage();
            if (!formatted.isEmpty()) {
                System.out.println(formatted);
            }
        }
        
        @Override
        public void onConnectionStatusChanged(boolean connected) {
            if (connected) {
                System.out.println("*** 已连接到服务端 ***");
            } else {
                System.out.println("*** 与服务端断开连接 ***");
            }
        }
        
        @Override
        public void onError(String error) {
            System.err.println("错误: " + error);
        }
    }
}