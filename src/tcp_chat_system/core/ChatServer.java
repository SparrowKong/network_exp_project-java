package tcp_chat_system.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TCP聊天服务器核心实现
 * 
 * 功能特性：
 * 1. 支持多客户端并发连接
 * 2. 消息广播功能
 * 3. 客户端连接状态管理
 * 4. 线程安全的客户端列表维护
 * 5. 优雅的客户端断开处理
 * 
 * @author TCP聊天系统实验
 * @version 1.0
 */
public class ChatServer {
    
    private static final int DEFAULT_PORT = 8888;  // 默认端口号
    
    private int port;                                           // 服务器端口
    private ServerSocket serverSocket;                          // 服务器套接字
    private boolean isRunning;                                  // 服务器运行状态
    
    // 使用线程安全的集合存储客户端信息
    private Map<String, ClientHandler> clients;                // 客户端处理器映射 <用户名, 处理器>
    private List<ChatMessage> messageHistory;                  // 消息历史记录
    
    /**
     * 构造函数 - 使用默认端口
     */
    public ChatServer() {
        this(DEFAULT_PORT);
    }
    
    /**
     * 构造函数 - 指定端口
     * 
     * @param port 服务器监听端口
     */
    public ChatServer(int port) {
        this.port = port;
        this.isRunning = false;
        this.clients = new ConcurrentHashMap<>();
        this.messageHistory = new CopyOnWriteArrayList<>();
    }
    
    /**
     * 启动服务器
     * 
     * @throws IOException 启动失败时抛出异常
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        
        System.out.println("========================================");
        System.out.println("         TCP聊天服务器已启动");
        System.out.println("         监听端口: " + port);
        System.out.println("         等待客户端连接...");
        System.out.println("========================================");
        
        // 主循环 - 接受客户端连接
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // 为每个客户端创建独立的处理线程
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
                
                System.out.println("新客户端连接: " + clientSocket.getInetAddress().getHostAddress());
                
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("接受客户端连接时发生错误: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        isRunning = false;
        
        // 通知所有客户端服务器关闭
        broadcastMessage(ChatMessage.createSystemMessage("服务器即将关闭"));
        
        // 关闭所有客户端连接
        for (ClientHandler client : clients.values()) {
            client.disconnect();
        }
        clients.clear();
        
        // 关闭服务器套接字
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭服务器套接字时发生错误: " + e.getMessage());
        }
        
        System.out.println("服务器已停止");
    }
    
    /**
     * 广播消息给所有连接的客户端
     * 
     * @param message 要广播的消息
     */
    public void broadcastMessage(ChatMessage message) {
        // 添加到消息历史
        messageHistory.add(message);
        
        // 如果消息历史过多，保留最近1000条
        if (messageHistory.size() > 1000) {
            messageHistory.remove(0);
        }
        
        // 广播给所有客户端
        List<String> disconnectedClients = new ArrayList<>();
        
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            ClientHandler client = entry.getValue();
            if (!client.sendMessage(message)) {
                // 发送失败的客户端标记为断开
                disconnectedClients.add(entry.getKey());
            }
        }
        
        // 清理断开的客户端
        for (String username : disconnectedClients) {
            removeClient(username);
        }
        
        // 服务器控制台显示消息
        System.out.println("广播消息: " + message.getDisplayString());
    }
    
    /**
     * 添加客户端
     * 
     * @param username 用户名
     * @param clientHandler 客户端处理器
     * @return 添加成功返回true，用户名已存在返回false
     */
    private synchronized boolean addClient(String username, ClientHandler clientHandler) {
        if (clients.containsKey(username)) {
            return false; // 用户名已存在
        }
        
        clients.put(username, clientHandler);
        
        // 发送用户加入消息
        ChatMessage joinMessage = ChatMessage.createJoinMessage(username);
        broadcastMessage(joinMessage);
        
        System.out.println("用户 '" + username + "' 加入聊天室，当前在线人数: " + clients.size());
        return true;
    }
    
    /**
     * 移除客户端
     * 
     * @param username 用户名
     */
    private synchronized void removeClient(String username) {
        ClientHandler removed = clients.remove(username);
        if (removed != null) {
            // 发送用户离开消息
            ChatMessage leaveMessage = ChatMessage.createLeaveMessage(username);
            broadcastMessage(leaveMessage);
            
            System.out.println("用户 '" + username + "' 离开聊天室，当前在线人数: " + clients.size());
        }
    }
    
    /**
     * 获取在线用户列表
     * 
     * @return 在线用户名列表
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(clients.keySet());
    }
    
    /**
     * 获取消息历史
     * 
     * @return 消息历史列表的副本
     */
    public List<ChatMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
    
    /**
     * 获取服务器运行状态
     * 
     * @return true表示服务器正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 获取服务器端口
     * 
     * @return 端口号
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 客户端处理器内部类
     * 负责处理单个客户端的所有通信
     */
    private class ClientHandler implements Runnable {
        
        private Socket socket;                  // 客户端套接字
        private BufferedReader reader;          // 输入流读取器
        private PrintWriter writer;             // 输出流写入器
        private String username;                // 用户名
        private boolean connected;              // 连接状态
        
        /**
         * 构造函数
         * 
         * @param socket 客户端套接字
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.connected = true;
            
            try {
                // 初始化输入输出流
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                
            } catch (IOException e) {
                System.err.println("初始化客户端处理器失败: " + e.getMessage());
                disconnect();
            }
        }
        
        /**
         * 客户端处理主循环
         */
        @Override
        public void run() {
            try {
                // 首先进行用户身份验证
                if (!authenticateUser()) {
                    disconnect();
                    return;
                }
                
                // 发送历史消息给新用户（最近20条）
                sendRecentMessages();
                
                // 消息处理循环
                String receivedData;
                while (connected && (receivedData = reader.readLine()) != null) {
                    
                    // 解析接收到的消息
                    ChatMessage message = ChatMessage.deserialize(receivedData);
                    if (message != null && message.getType() == ChatMessage.MessageType.USER_MESSAGE) {
                        // 验证消息发送者
                        if (username.equals(message.getUsername())) {
                            // 广播用户消息
                            broadcastMessage(message);
                        } else {
                            System.err.println("用户名不匹配，拒绝消息: " + username + " vs " + message.getUsername());
                        }
                    }
                }
                
            } catch (IOException e) {
                System.err.println("客户端 '" + username + "' 连接异常: " + e.getMessage());
            } finally {
                disconnect();
            }
        }
        
        /**
         * 用户身份验证
         * 
         * @return 验证成功返回true
         */
        private boolean authenticateUser() throws IOException {
            // 发送欢迎消息
            writer.println("欢迎连接TCP聊天服务器！请输入您的用户名:");
            
            // 等待用户名输入
            String inputUsername = reader.readLine();
            if (inputUsername == null || inputUsername.trim().isEmpty()) {
                writer.println("用户名不能为空，连接关闭");
                return false;
            }
            
            inputUsername = inputUsername.trim();
            
            // 检查用户名是否合法
            if (inputUsername.length() > 20 || inputUsername.contains("|")) {
                writer.println("用户名不合法（长度不超过20字符，不能包含'|'字符），连接关闭");
                return false;
            }
            
            // 尝试添加用户
            if (addClient(inputUsername, this)) {
                this.username = inputUsername;
                writer.println("登录成功！欢迎 " + username + " 加入聊天室");
                writer.println("当前在线用户: " + String.join(", ", getOnlineUsers()));
                return true;
            } else {
                writer.println("用户名 '" + inputUsername + "' 已被使用，请重新连接并选择其他用户名");
                return false;
            }
        }
        
        /**
         * 向客户端发送最近的消息记录
         */
        private void sendRecentMessages() {
            int historySize = messageHistory.size();
            int startIndex = Math.max(0, historySize - 20); // 最近20条消息
            
            if (startIndex < historySize) {
                writer.println("=== 最近的聊天记录 ===");
                for (int i = startIndex; i < historySize; i++) {
                    ChatMessage msg = messageHistory.get(i);
                    writer.println(msg.serialize());
                }
                writer.println("=== 聊天记录结束 ===");
            }
        }
        
        /**
         * 向客户端发送消息
         * 
         * @param message 要发送的消息
         * @return 发送成功返回true
         */
        public boolean sendMessage(ChatMessage message) {
            if (!connected) {
                return false;
            }
            
            try {
                writer.println(message.serialize());
                return !writer.checkError(); // 检查是否有写入错误
            } catch (Exception e) {
                System.err.println("向客户端 '" + username + "' 发送消息失败: " + e.getMessage());
                return false;
            }
        }
        
        /**
         * 断开客户端连接
         */
        public void disconnect() {
            if (!connected) {
                return;
            }
            
            connected = false;
            
            // 从客户端列表中移除
            if (username != null) {
                removeClient(username);
            }
            
            // 关闭资源
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.err.println("关闭客户端连接时发生错误: " + e.getMessage());
            }
            
            System.out.println("客户端 '" + username + "' 已断开连接");
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
         * 检查连接状态
         * 
         * @return 连接状态
         */
        public boolean isConnected() {
            return connected && !socket.isClosed();
        }
    }
}