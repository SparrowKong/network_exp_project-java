package tcp_chat_system.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import tcp_chat_system.core.ChatServer;
import tcp_chat_system.core.ChatClient;
import tcp_chat_system.core.ChatMessage;

/**
 * TCP聊天系统测试用例
 * 
 * 测试内容包括：
 * 1. 服务器启动和停止
 * 2. 客户端连接和断开
 * 3. 消息发送和接收
 * 4. 多客户端聊天测试
 * 5. 系统消息测试
 * 
 * @author TCP聊天系统实验
 * @version 1.0
 */
public class ChatSystemTest {
    
    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 9999; // 使用不同端口避免冲突
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      TCP聊天系统功能测试");
        System.out.println("========================================");
        
        ChatSystemTest test = new ChatSystemTest();
        
        int passedTests = 0;
        int totalTests = 6;
        
        // 运行各项测试
        if (test.testMessageSerialization()) {
            System.out.println("✓ 消息序列化测试 - 通过");
            passedTests++;
        } else {
            System.out.println("✗ 消息序列化测试 - 失败");
        }
        
        if (test.testServerStartStop()) {
            System.out.println("✓ 服务器启动停止测试 - 通过");
            passedTests++;
        } else {
            System.out.println("✗ 服务器启动停止测试 - 失败");
        }
        
        if (test.testClientConnection()) {
            System.out.println("✓ 客户端连接测试 - 通过");
            passedTests++;
        } else {
            System.out.println("✗ 客户端连接测试 - 失败");
        }
        
        if (test.testMessageTransfer()) {
            System.out.println("✓ 消息传输测试 - 通过");
            passedTests++;
        } else {
            System.out.println("✗ 消息传输测试 - 失败");
        }
        
        if (test.testMultiClientChat()) {
            System.out.println("✓ 多客户端聊天测试 - 通过");
            passedTests++;
        } else {
            System.out.println("✗ 多客户端聊天测试 - 失败");
        }
        
        if (test.testSystemMessages()) {
            System.out.println("✓ 系统消息测试 - 通过");
            passedTests++;
        } else {
            System.out.println("✗ 系统消息测试 - 失败");
        }
        
        // 输出测试结果
        System.out.println("========================================");
        System.out.println("测试完成！");
        System.out.println("通过: " + passedTests + "/" + totalTests + " 项测试");
        System.out.println("成功率: " + (passedTests * 100 / totalTests) + "%");
        System.out.println("========================================");
        
        if (passedTests == totalTests) {
            System.out.println("🎉 所有测试都通过了！TCP聊天系统运行正常。");
        } else {
            System.out.println("⚠️  部分测试失败，请检查系统实现。");
        }
    }
    
    /**
     * 测试消息序列化和反序列化
     */
    public boolean testMessageSerialization() {
        try {
            // 测试普通消息
            ChatMessage userMessage = new ChatMessage("测试用户", "这是一条测试消息");
            String serialized = userMessage.serialize();
            ChatMessage deserialized = ChatMessage.deserialize(serialized);
            
            if (deserialized == null || 
                !deserialized.getUsername().equals("测试用户") ||
                !deserialized.getContent().equals("这是一条测试消息") ||
                deserialized.getType() != ChatMessage.MessageType.USER_MESSAGE) {
                return false;
            }
            
            // 测试系统消息
            ChatMessage systemMessage = ChatMessage.createSystemMessage("系统测试消息");
            String systemSerialized = systemMessage.serialize();
            ChatMessage systemDeserialized = ChatMessage.deserialize(systemSerialized);
            
            if (systemDeserialized == null ||
                !systemDeserialized.getUsername().equals("系统") ||
                !systemDeserialized.getContent().equals("系统测试消息") ||
                systemDeserialized.getType() != ChatMessage.MessageType.SYSTEM_INFO) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("消息序列化测试异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试服务器启动和停止
     */
    public boolean testServerStartStop() {
        ChatServer server = null;
        try {
            server = new ChatServer(TEST_PORT);
            final ChatServer finalServer = server; // 创建final引用
            
            // 在独立线程中启动服务器
            Thread serverThread = new Thread(() -> {
                try {
                    finalServer.start();
                } catch (IOException e) {
                    System.err.println("服务器启动异常: " + e.getMessage());
                }
            });
            
            serverThread.setDaemon(true);
            serverThread.start();
            
            // 等待服务器启动
            Thread.sleep(1000);
            
            if (!server.isRunning()) {
                return false;
            }
            
            // 停止服务器
            server.stop();
            
            // 等待服务器停止
            Thread.sleep(500);
            
            return !server.isRunning();
            
        } catch (Exception e) {
            System.err.println("服务器启停测试异常: " + e.getMessage());
            return false;
        } finally {
            if (server != null && server.isRunning()) {
                server.stop();
            }
        }
    }
    
    /**
     * 测试客户端连接
     */
    public boolean testClientConnection() {
        ChatServer server = null;
        ChatClient client = null;
        
        try {
            // 启动服务器
            server = new ChatServer(TEST_PORT);
            final ChatServer finalServer = server; // 创建final引用
            Thread serverThread = new Thread(() -> {
                try {
                    finalServer.start();
                } catch (IOException e) {
                    System.err.println("服务器启动异常: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            Thread.sleep(1000); // 等待服务器启动
            
            // 创建客户端并连接
            client = new ChatClient(TEST_HOST, TEST_PORT);
            boolean connected = client.connect("测试客户端");
            
            if (!connected || !client.isConnected()) {
                return false;
            }
            
            Thread.sleep(500); // 等待连接稳定
            
            // 断开客户端
            client.disconnect();
            
            Thread.sleep(500); // 等待断开完成
            
            return !client.isConnected();
            
        } catch (Exception e) {
            System.err.println("客户端连接测试异常: " + e.getMessage());
            return false;
        } finally {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
            if (server != null && server.isRunning()) {
                server.stop();
            }
        }
    }
    
    /**
     * 测试消息传输
     */
    public boolean testMessageTransfer() {
        ChatServer server = null;
        ChatClient client1 = null;
        ChatClient client2 = null;
        
        try {
            // 启动服务器
            server = new ChatServer(TEST_PORT);
            final ChatServer finalServer = server; // 创建final引用
            Thread serverThread = new Thread(() -> {
                try {
                    finalServer.start();
                } catch (IOException e) {
                    System.err.println("服务器启动异常: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            Thread.sleep(1000);
            
            // 消息接收计数器
            final CountDownLatch messageLatch = new CountDownLatch(1);
            final boolean[] messageReceived = {false};
            
            // 创建接收方客户端
            client1 = new ChatClient(TEST_HOST, TEST_PORT);
            client1.setMessageCallback(new ChatClient.MessageCallback() {
                @Override
                public void onMessageReceived(ChatMessage message) {
                    if (message.getType() == ChatMessage.MessageType.USER_MESSAGE &&
                        message.getUsername().equals("发送者") &&
                        message.getContent().equals("测试消息内容")) {
                        messageReceived[0] = true;
                        messageLatch.countDown();
                    }
                }
                
                @Override
                public void onSystemNotification(String notification) {}
                
                @Override
                public void onConnectionStatusChanged(boolean connected) {}
            });
            
            client1.connect("接收者");
            Thread.sleep(500);
            
            // 创建发送方客户端
            client2 = new ChatClient(TEST_HOST, TEST_PORT);
            client2.connect("发送者");
            Thread.sleep(500);
            
            // 发送消息
            client2.sendMessage("测试消息内容");
            
            // 等待消息接收
            boolean received = messageLatch.await(3, TimeUnit.SECONDS);
            
            return received && messageReceived[0];
            
        } catch (Exception e) {
            System.err.println("消息传输测试异常: " + e.getMessage());
            return false;
        } finally {
            if (client1 != null && client1.isConnected()) {
                client1.disconnect();
            }
            if (client2 != null && client2.isConnected()) {
                client2.disconnect();
            }
            if (server != null && server.isRunning()) {
                server.stop();
            }
        }
    }
    
    /**
     * 测试多客户端聊天
     */
    public boolean testMultiClientChat() {
        ChatServer server = null;
        ChatClient[] clients = new ChatClient[3];
        
        try {
            // 启动服务器
            server = new ChatServer(TEST_PORT);
            final ChatServer finalServer = server; // 创建final引用
            Thread serverThread = new Thread(() -> {
                try {
                    finalServer.start();
                } catch (IOException e) {
                    System.err.println("服务器启动异常: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            Thread.sleep(1000);
            
            // 连接多个客户端
            for (int i = 0; i < 3; i++) {
                clients[i] = new ChatClient(TEST_HOST, TEST_PORT);
                clients[i].connect("用户" + (i + 1));
                Thread.sleep(200);
            }
            
            // 检查服务器是否记录了3个用户
            if (server.getOnlineUsers().size() != 3) {
                return false;
            }
            
            // 发送消息
            clients[0].sendMessage("来自用户1的消息");
            Thread.sleep(500);
            
            // 检查消息历史
            if (server.getMessageHistory().size() < 3) { // 至少应该有3条加入消息和1条用户消息
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("多客户端聊天测试异常: " + e.getMessage());
            return false;
        } finally {
            for (ChatClient client : clients) {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                }
            }
            if (server != null && server.isRunning()) {
                server.stop();
            }
        }
    }
    
    /**
     * 测试系统消息
     */
    public boolean testSystemMessages() {
        ChatServer server = null;
        
        try {
            server = new ChatServer(TEST_PORT);
            
            // 测试系统消息创建
            ChatMessage systemMsg = ChatMessage.createSystemMessage("测试系统消息");
            if (!systemMsg.getUsername().equals("系统") ||
                !systemMsg.getContent().equals("测试系统消息") ||
                systemMsg.getType() != ChatMessage.MessageType.SYSTEM_INFO) {
                return false;
            }
            
            // 测试加入消息
            ChatMessage joinMsg = ChatMessage.createJoinMessage("新用户");
            if (!joinMsg.getUsername().equals("新用户") ||
                joinMsg.getType() != ChatMessage.MessageType.USER_JOIN) {
                return false;
            }
            
            // 测试离开消息
            ChatMessage leaveMsg = ChatMessage.createLeaveMessage("离开用户");
            if (!leaveMsg.getUsername().equals("离开用户") ||
                leaveMsg.getType() != ChatMessage.MessageType.USER_LEAVE) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("系统消息测试异常: " + e.getMessage());
            return false;
        } finally {
            if (server != null && server.isRunning()) {
                server.stop();
            }
        }
    }
}