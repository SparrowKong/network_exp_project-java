package udp_chat_system.core;

import java.net.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UDP聊天系统服务端核心类
 * 
 * 主要功能：
 * - 监听客户端连接和消息
 * - 维护在线客户端列表
 * - 消息广播和转发
 * - 多线程并发处理
 * - 客户端状态管理
 * 
 * 技术特点：
 * - 使用DatagramSocket实现UDP通信
 * - 多线程处理并发连接
 * - 线程安全的客户端管理
 * - 心跳检测和超时处理
 */
public class UDPServer {
    
    // 服务端配置
    private static final int DEFAULT_PORT = 8888;
    private static final int BUFFER_SIZE = 1024;
    private static final int THREAD_POOL_SIZE = 10;
    private static final long CLIENT_TIMEOUT = 30000; // 30秒客户端超时
    
    // 网络组件
    private DatagramSocket socket;
    private boolean isRunning;
    private int port;
    private ExecutorService threadPool;
    
    // 客户端管理
    private final ConcurrentHashMap<String, ClientInfo> clients;
    private final Object clientLock = new Object();
    
    // 统计信息
    private long messageCount;
    private long startTime;
    
    /**
     * 客户端信息类
     * 存储客户端的网络地址和状态信息
     */
    public static class ClientInfo {
        private final String username;
        private final InetAddress address;
        private final int port;
        private long lastHeartbeat;
        private long connectTime;
        
        public ClientInfo(String username, InetAddress address, int port) {
            this.username = username;
            this.address = address;
            this.port = port;
            this.lastHeartbeat = System.currentTimeMillis();
            this.connectTime = System.currentTimeMillis();
        }
        
        public String getUsername() { return username; }
        public InetAddress getAddress() { return address; }
        public int getPort() { return port; }
        public long getLastHeartbeat() { return lastHeartbeat; }
        public long getConnectTime() { return connectTime; }
        
        public void updateHeartbeat() {
            this.lastHeartbeat = System.currentTimeMillis();
        }
        
        public boolean isTimeout() {
            return (System.currentTimeMillis() - lastHeartbeat) > CLIENT_TIMEOUT;
        }
        
        @Override
        public String toString() {
            return username + "@" + address.getHostAddress() + ":" + port;
        }
    }
    
    /**
     * 默认构造函数，使用默认端口
     */
    public UDPServer() {
        this(DEFAULT_PORT);
    }
    
    /**
     * 构造函数
     * @param port 服务端监听端口
     */
    public UDPServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.isRunning = false;
        this.messageCount = 0;
    }
    
    /**
     * 启动服务端
     * @throws IOException 如果启动失败
     */
    public void start() throws IOException {
        if (isRunning) {
            throw new IllegalStateException("服务端已经在运行中");
        }
        
        socket = new DatagramSocket(port);
        isRunning = true;
        startTime = System.currentTimeMillis();
        
        System.out.println("=== UDP聊天服务端启动 ===");
        System.out.println("监听端口: " + port);
        System.out.println("缓冲区大小: " + BUFFER_SIZE + " 字节");
        System.out.println("线程池大小: " + THREAD_POOL_SIZE);
        System.out.println("客户端超时时间: " + (CLIENT_TIMEOUT / 1000) + " 秒");
        System.out.println("等待客户端连接...\n");
        
        // 启动消息处理循环
        startMessageLoop();
        
        // 启动心跳检测线程
        startHeartbeatChecker();
    }
    
    /**
     * 停止服务端
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // 通知所有客户端服务端关闭
        Message shutdownMessage = Message.createServerInfoMessage("服务端即将关闭");
        broadcastMessage(shutdownMessage, null);
        
        // 关闭资源
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        
        synchronized (clientLock) {
            clients.clear();
        }
        
        long runTime = System.currentTimeMillis() - startTime;
        System.out.println("\n=== 服务端已停止 ===");
        System.out.println("运行时间: " + (runTime / 1000) + " 秒");
        System.out.println("处理消息总数: " + messageCount);
    }
    
    /**
     * 启动消息处理循环
     */
    private void startMessageLoop() {
        threadPool.submit(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while (isRunning) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    // 异步处理消息
                    threadPool.submit(() -> handleMessage(packet));
                    
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("接收消息时发生错误: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * 处理接收到的消息
     * @param packet 接收到的数据包
     */
    private void handleMessage(DatagramPacket packet) {
        try {
            String data = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            Message message = Message.deserialize(data);
            
            if (message == null || !message.isValid()) {
                System.err.println("接收到无效消息: " + data);
                return;
            }
            
            messageCount++;
            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();
            String username = message.getUsername();
            
            // 根据消息类型处理
            switch (message.getType()) {
                case CONNECT:
                    handleClientConnect(username, clientAddress, clientPort);
                    break;
                    
                case CHAT:
                    handleChatMessage(message, username);
                    break;
                    
                case DISCONNECT:
                    handleClientDisconnect(username);
                    break;
                    
                case HEARTBEAT:
                    handleHeartbeat(username);
                    break;
                    
                default:
                    System.out.println("未知消息类型: " + message.getType());
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("处理消息时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理客户端连接
     * @param username 用户名
     * @param address 客户端地址
     * @param port 客户端端口
     */
    private void handleClientConnect(String username, InetAddress address, int port) {
        synchronized (clientLock) {
            if (clients.containsKey(username)) {
                System.out.println("用户 " + username + " 重复连接，更新信息");
            } else {
                System.out.println("新客户端连接: " + username + " (" + address.getHostAddress() + ":" + port + ")");
            }
            
            ClientInfo clientInfo = new ClientInfo(username, address, port);
            clients.put(username, clientInfo);
        }
        
        // 发送欢迎消息给新客户端
        Message welcomeMessage = Message.createServerInfoMessage(
            "欢迎 " + username + " 加入聊天室！当前在线用户: " + clients.size() + " 人");
        sendMessageToClient(welcomeMessage, username);
        
        // 广播用户加入消息
        Message joinMessage = Message.createConnectMessage(username);
        broadcastMessage(joinMessage, username);
        
        // 发送在线用户列表
        sendOnlineUsersList(username);
    }
    
    /**
     * 处理聊天消息
     * @param message 聊天消息
     * @param senderUsername 发送者用户名
     */
    private void handleChatMessage(Message message, String senderUsername) {
        // 验证发送者是否在线
        if (!clients.containsKey(senderUsername)) {
            System.err.println("接收到离线用户 " + senderUsername + " 的消息");
            return;
        }
        
        // 更新发送者心跳时间
        ClientInfo sender = clients.get(senderUsername);
        if (sender != null) {
            sender.updateHeartbeat();
        }
        
        System.out.println("聊天消息: " + message.getFormattedMessage());
        
        // 广播消息给所有客户端
        broadcastMessage(message, null);
    }
    
    /**
     * 处理客户端断开连接
     * @param username 用户名
     */
    private void handleClientDisconnect(String username) {
        synchronized (clientLock) {
            ClientInfo clientInfo = clients.remove(username);
            if (clientInfo != null) {
                System.out.println("客户端断开连接: " + username);
                
                // 广播用户离开消息
                Message leaveMessage = Message.createDisconnectMessage(username);
                broadcastMessage(leaveMessage, username);
            }
        }
    }
    
    /**
     * 处理心跳消息
     * @param username 用户名
     */
    private void handleHeartbeat(String username) {
        ClientInfo clientInfo = clients.get(username);
        if (clientInfo != null) {
            clientInfo.updateHeartbeat();
        }
    }
    
    /**
     * 广播消息给所有客户端
     * @param message 要广播的消息
     * @param excludeUsername 排除的用户名（不发送给此用户）
     */
    private void broadcastMessage(Message message, String excludeUsername) {
        synchronized (clientLock) {
            for (ClientInfo client : clients.values()) {
                if (excludeUsername == null || !client.getUsername().equals(excludeUsername)) {
                    sendMessageToClient(message, client);
                }
            }
        }
    }
    
    /**
     * 发送消息给指定客户端
     * @param message 消息对象
     * @param username 目标用户名
     */
    private void sendMessageToClient(Message message, String username) {
        ClientInfo client = clients.get(username);
        if (client != null) {
            sendMessageToClient(message, client);
        }
    }
    
    /**
     * 发送消息给指定客户端
     * @param message 消息对象
     * @param client 目标客户端信息
     */
    private void sendMessageToClient(Message message, ClientInfo client) {
        try {
            String data = message.serialize();
            byte[] buffer = data.getBytes("UTF-8");
            
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, client.getAddress(), client.getPort());
            
            socket.send(packet);
            
        } catch (IOException e) {
            System.err.println("发送消息给客户端 " + client.getUsername() + " 失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送在线用户列表给指定客户端
     * @param username 目标用户名
     */
    private void sendOnlineUsersList(String username) {
        StringBuilder userList = new StringBuilder("在线用户列表: ");
        synchronized (clientLock) {
            for (String user : clients.keySet()) {
                userList.append(user).append(" ");
            }
        }
        
        Message userListMessage = Message.createServerInfoMessage(userList.toString());
        sendMessageToClient(userListMessage, username);
    }
    
    /**
     * 启动心跳检测线程
     */
    private void startHeartbeatChecker() {
        threadPool.submit(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(10000); // 每10秒检测一次
                    
                    List<String> timeoutUsers = new ArrayList<>();
                    synchronized (clientLock) {
                        for (ClientInfo client : clients.values()) {
                            if (client.isTimeout()) {
                                timeoutUsers.add(client.getUsername());
                            }
                        }
                    }
                    
                    // 移除超时客户端
                    for (String username : timeoutUsers) {
                        System.out.println("客户端 " + username + " 连接超时，自动移除");
                        handleClientDisconnect(username);
                    }
                    
                } catch (InterruptedException e) {
                    if (isRunning) {
                        System.err.println("心跳检测线程被中断: " + e.getMessage());
                    }
                    break;
                }
            }
        });
    }
    
    /**
     * 获取在线客户端数量
     * @return 在线客户端数量
     */
    public int getOnlineClientCount() {
        return clients.size();
    }
    
    /**
     * 获取在线客户端列表
     * @return 在线客户端用户名列表
     */
    public List<String> getOnlineClients() {
        synchronized (clientLock) {
            return new ArrayList<>(clients.keySet());
        }
    }
    
    /**
     * 获取服务端统计信息
     * @return 统计信息字符串
     */
    public String getStatistics() {
        long runTime = System.currentTimeMillis() - startTime;
        return String.format(
            "运行时间: %d秒 | 在线用户: %d人 | 消息总数: %d条",
            runTime / 1000, clients.size(), messageCount);
    }
    
    /**
     * 检查服务端是否正在运行
     * @return true如果服务端正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 获取服务端端口
     * @return 端口号
     */
    public int getPort() {
        return port;
    }
}