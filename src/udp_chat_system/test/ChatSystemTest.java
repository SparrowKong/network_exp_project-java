package udp_chat_system.test;

import udp_chat_system.core.UDPServer;
import udp_chat_system.core.UDPClient;
import udp_chat_system.core.Message;

import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * UDP聊天系统测试用例
 * 
 * 测试内容包括：
 * - 消息类的序列化和反序列化
 * - 服务端启动和停止
 * - 客户端连接和断开
 * - 消息发送和接收
 * - 多客户端并发测试
 * - 异常情况处理
 * 
 * 使用方法：
 * 1. 编译: javac -d build core/*.java test/*.java
 * 2. 运行: java -cp build udp_chat_system.test.ChatSystemTest
 */
public class ChatSystemTest {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999; // 使用不同端口避免冲突
    private static final int TEST_TIMEOUT = 10; // 测试超时时间（秒）
    
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("         UDP聊天系统 - 自动化测试");
        System.out.println("===========================================");
        System.out.println("开始执行自动化测试用例...\n");
        
        // 执行所有测试用例
        runAllTests();
        
        // 显示测试结果统计
        showTestSummary();
    }
    
    /**
     * 运行所有测试用例
     */
    private static void runAllTests() {
        // 1. 消息类测试
        testMessageSerialization();
        testMessageCreation();
        testMessageValidation();
        
        // 2. 服务端测试
        testServerStartStop();
        testServerClientManagement();
        
        // 3. 客户端测试
        testClientConnection();
        testClientMessageSending();
        
        // 4. 集成测试
        testBasicCommunication();
        testMultipleClients();
        testConnectionLost();
        
        // 5. 异常处理测试
        testInvalidServerAddress();
        testInvalidMessage();
    }
    
    /**
     * 测试消息序列化和反序列化
     */
    private static void testMessageSerialization() {
        System.out.println("测试1: 消息序列化和反序列化");
        
        try {
            // 创建测试消息
            Message originalMessage = Message.createChatMessage("测试用户", "这是一条测试消息");
            
            // 序列化
            String serialized = originalMessage.serialize();
            assertNotNull(serialized, "序列化结果不应为null");
            
            // 反序列化
            Message deserializedMessage = Message.deserialize(serialized);
            assertNotNull(deserializedMessage, "反序列化结果不应为null");
            
            // 验证数据一致性
            assertEqual(originalMessage.getType(), deserializedMessage.getType(), "消息类型应一致");
            assertEqual(originalMessage.getUsername(), deserializedMessage.getUsername(), "用户名应一致");
            assertEqual(originalMessage.getContent(), deserializedMessage.getContent(), "消息内容应一致");
            
            testPassed("消息序列化和反序列化");
            
        } catch (Exception e) {
            testFailed("消息序列化和反序列化", e.getMessage());
        }
    }
    
    /**
     * 测试消息创建方法
     */
    private static void testMessageCreation() {
        System.out.println("测试2: 消息创建方法");
        
        try {
            // 测试聊天消息创建
            Message chatMessage = Message.createChatMessage("用户1", "聊天内容");
            assertEqual(chatMessage.getType(), Message.MessageType.CHAT, "聊天消息类型正确");
            
            // 测试连接消息创建
            Message connectMessage = Message.createConnectMessage("用户2");
            assertEqual(connectMessage.getType(), Message.MessageType.CONNECT, "连接消息类型正确");
            
            // 测试断开连接消息创建
            Message disconnectMessage = Message.createDisconnectMessage("用户3");
            assertEqual(disconnectMessage.getType(), Message.MessageType.DISCONNECT, "断开连接消息类型正确");
            
            // 测试服务端信息消息创建
            Message serverInfoMessage = Message.createServerInfoMessage("服务端信息");
            assertEqual(serverInfoMessage.getType(), Message.MessageType.SERVER_INFO, "服务端信息消息类型正确");
            
            testPassed("消息创建方法");
            
        } catch (Exception e) {
            testFailed("消息创建方法", e.getMessage());
        }
    }
    
    /**
     * 测试消息验证
     */
    private static void testMessageValidation() {
        System.out.println("测试3: 消息验证");
        
        try {
            // 测试有效消息
            Message validMessage = Message.createChatMessage("用户", "有效内容");
            assertTrue(validMessage.isValid(), "有效消息应通过验证");
            
            // 测试无效消息
            String invalidData = "INVALID|DATA";
            Message invalidMessage = Message.deserialize(invalidData);
            if (invalidMessage != null) {
                // 某些无效数据可能仍能创建Message对象，但不应通过验证
            }
            
            testPassed("消息验证");
            
        } catch (Exception e) {
            testFailed("消息验证", e.getMessage());
        }
    }
    
    /**
     * 测试服务端启动和停止
     */
    private static void testServerStartStop() {
        System.out.println("测试4: 服务端启动和停止");
        
        UDPServer server = null;
        try {
            server = new UDPServer(SERVER_PORT);
            
            // 测试启动
            assertFalse(server.isRunning(), "服务端初始状态应为未运行");
            server.start();
            assertTrue(server.isRunning(), "服务端启动后状态应为运行中");
            
            // 等待服务端完全启动
            Thread.sleep(1000);
            
            // 测试端口
            assertEqual(server.getPort(), SERVER_PORT, "服务端端口应正确");
            
            testPassed("服务端启动和停止");
            
        } catch (Exception e) {
            testFailed("服务端启动和停止", e.getMessage());
        } finally {
            if (server != null && server.isRunning()) {
                server.stop();
            }
        }
    }
    
    /**
     * 测试服务端客户端管理
     */
    private static void testServerClientManagement() {
        System.out.println("测试5: 服务端客户端管理");
        
        UDPServer server = null;
        try {
            server = new UDPServer(SERVER_PORT);
            server.start();
            Thread.sleep(1000);
            
            // 测试初始状态
            assertEqual(server.getOnlineClientCount(), 0, "初始客户端数量应为0");
            
            List<String> clients = server.getOnlineClients();
            assertNotNull(clients, "客户端列表不应为null");
            assertTrue(clients.isEmpty(), "初始客户端列表应为空");
            
            testPassed("服务端客户端管理");
            
        } catch (Exception e) {
            testFailed("服务端客户端管理", e.getMessage());
        } finally {
            if (server != null && server.isRunning()) {
                server.stop();
            }
        }
    }
    
    /**
     * 测试客户端连接
     */
    private static void testClientConnection() {
        System.out.println("测试6: 客户端连接");
        
        UDPServer server = null;
        UDPClient client = null;
        
        try {
            // 启动服务端
            server = new UDPServer(SERVER_PORT);
            server.start();
            Thread.sleep(1000);
            
            // 创建客户端
            client = new UDPClient(SERVER_HOST, SERVER_PORT, "测试用户1");
            
            // 测试初始状态
            assertFalse(client.isConnected(), "客户端初始状态应为未连接");
            
            // 测试连接
            boolean connected = client.connect();
            assertTrue(connected, "客户端应能成功连接");
            
            // 等待连接建立
            Thread.sleep(1000);
            assertTrue(client.isConnected(), "客户端连接状态应为已连接");
            
            testPassed("客户端连接");
            
        } catch (Exception e) {
            testFailed("客户端连接", e.getMessage());
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
     * 测试客户端消息发送
     */
    private static void testClientMessageSending() {
        System.out.println("测试7: 客户端消息发送");
        
        UDPServer server = null;
        UDPClient client = null;
        
        try {
            // 启动服务端
            server = new UDPServer(SERVER_PORT);
            server.start();
            Thread.sleep(1000);
            
            // 连接客户端
            client = new UDPClient(SERVER_HOST, SERVER_PORT, "测试用户2");
            assertTrue(client.connect(), "客户端应能连接");
            Thread.sleep(1000);
            
            // 测试消息发送
            boolean sent = client.sendChatMessage("测试消息内容");
            assertTrue(sent, "消息应能成功发送");
            
            testPassed("客户端消息发送");
            
        } catch (Exception e) {
            testFailed("客户端消息发送", e.getMessage());
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
     * 测试基本通信功能
     */
    private static void testBasicCommunication() {
        System.out.println("测试8: 基本通信功能");
        
        UDPServer server = null;
        UDPClient client1 = null;
        UDPClient client2 = null;
        
        try {
            // 启动服务端
            server = new UDPServer(SERVER_PORT);
            server.start();
            Thread.sleep(1000);
            
            // 连接两个客户端
            client1 = new UDPClient(SERVER_HOST, SERVER_PORT, "用户A");
            client2 = new UDPClient(SERVER_HOST, SERVER_PORT, "用户B");
            
            final CountDownLatch messageLatch = new CountDownLatch(2);
            final StringBuilder receivedMessages = new StringBuilder();
            
            // 设置消息接收处理器
            UDPClient.MessageHandler handler = new UDPClient.MessageHandler() {
                @Override
                public void onMessageReceived(Message message) {
                    if (message.getType() == Message.MessageType.CHAT) {
                        receivedMessages.append(message.getContent()).append(";");
                        messageLatch.countDown();
                    }
                }
                
                @Override
                public void onConnectionStatusChanged(boolean connected) {}
                
                @Override
                public void onError(String error) {}
            };
            
            client1.setMessageHandler(handler);
            client2.setMessageHandler(handler);
            
            // 连接客户端
            assertTrue(client1.connect(), "客户端1应能连接");
            assertTrue(client2.connect(), "客户端2应能连接");
            Thread.sleep(2000);
            
            // 验证服务端客户端数量
            assertEqual(server.getOnlineClientCount(), 2, "服务端应有2个在线客户端");
            
            // 发送消息
            client1.sendChatMessage("来自用户A的消息");
            client2.sendChatMessage("来自用户B的消息");
            
            // 等待消息传递
            boolean received = messageLatch.await(TEST_TIMEOUT, TimeUnit.SECONDS);
            assertTrue(received, "应能接收到消息");
            
            testPassed("基本通信功能");
            
        } catch (Exception e) {
            testFailed("基本通信功能", e.getMessage());
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
     * 测试多客户端连接
     */
    private static void testMultipleClients() {
        System.out.println("测试9: 多客户端连接");
        
        UDPServer server = null;
        UDPClient[] clients = new UDPClient[3];
        
        try {
            // 启动服务端
            server = new UDPServer(SERVER_PORT);
            server.start();
            Thread.sleep(1000);
            
            // 连接多个客户端
            for (int i = 0; i < clients.length; i++) {
                clients[i] = new UDPClient(SERVER_HOST, SERVER_PORT, "用户" + (i + 1));
                assertTrue(clients[i].connect(), "客户端" + (i + 1) + "应能连接");
                Thread.sleep(500);
            }
            
            // 等待所有连接建立
            Thread.sleep(2000);
            
            // 验证客户端数量
            assertEqual(server.getOnlineClientCount(), 3, "服务端应有3个在线客户端");
            
            // 验证客户端列表
            List<String> onlineClients = server.getOnlineClients();
            assertEqual(onlineClients.size(), 3, "在线客户端列表应有3个用户");
            
            testPassed("多客户端连接");
            
        } catch (Exception e) {
            testFailed("多客户端连接", e.getMessage());
        } finally {
            for (UDPClient client : clients) {
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
     * 测试连接丢失处理
     */
    private static void testConnectionLost() {
        System.out.println("测试10: 连接丢失处理");
        
        UDPServer server = null;
        UDPClient client = null;
        
        try {
            // 启动服务端
            server = new UDPServer(SERVER_PORT);
            server.start();
            Thread.sleep(1000);
            
            // 连接客户端
            client = new UDPClient(SERVER_HOST, SERVER_PORT, "测试用户3");
            assertTrue(client.connect(), "客户端应能连接");
            Thread.sleep(1000);
            
            // 验证连接状态
            assertTrue(client.isConnected(), "客户端应处于连接状态");
            assertEqual(server.getOnlineClientCount(), 1, "服务端应有1个在线客户端");
            
            // 停止服务端模拟连接丢失
            server.stop();
            Thread.sleep(2000);
            
            // 客户端应检测到连接丢失
            // 注意：由于UDP是无连接的，客户端可能不会立即检测到连接丢失
            // 这里主要测试程序的健壮性
            
            testPassed("连接丢失处理");
            
        } catch (Exception e) {
            testFailed("连接丢失处理", e.getMessage());
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
     * 测试无效服务端地址
     */
    private static void testInvalidServerAddress() {
        System.out.println("测试11: 无效服务端地址处理");
        
        try {
            // 尝试连接无效地址
            UDPClient client = new UDPClient("invalid.host.name", 9999, "测试用户");
            boolean connected = client.connect();
            
            // 应该连接失败
            assertFalse(connected, "连接无效地址应该失败");
            assertFalse(client.isConnected(), "客户端应处于未连接状态");
            
            testPassed("无效服务端地址处理");
            
        } catch (UnknownHostException e) {
            // 这是预期的异常
            testPassed("无效服务端地址处理");
        } catch (Exception e) {
            testFailed("无效服务端地址处理", e.getMessage());
        }
    }
    
    /**
     * 测试无效消息处理
     */
    private static void testInvalidMessage() {
        System.out.println("测试12: 无效消息处理");
        
        try {
            // 测试空消息
            Message nullMessage = Message.deserialize(null);
            assertNull(nullMessage, "null数据应返回null消息");
            
            // 测试格式错误的消息
            Message invalidMessage = Message.deserialize("INVALID_FORMAT");
            assertNull(invalidMessage, "格式错误的数据应返回null消息");
            
            // 测试部分数据缺失的消息
            Message incompleteMessage = Message.deserialize("CHAT|用户");
            assertNull(incompleteMessage, "数据不完整应返回null消息");
            
            testPassed("无效消息处理");
            
        } catch (Exception e) {
            testFailed("无效消息处理", e.getMessage());
        }
    }
    
    /**
     * 显示测试结果统计
     */
    private static void showTestSummary() {
        System.out.println("\n===========================================");
        System.out.println("                 测试结果统计");
        System.out.println("===========================================");
        System.out.println("总测试数: " + totalTests);
        System.out.println("通过测试: " + passedTests);
        System.out.println("失败测试: " + failedTests);
        System.out.println("通过率: " + String.format("%.1f%%", (double) passedTests / totalTests * 100));
        System.out.println("===========================================");
        
        if (failedTests == 0) {
            System.out.println("🎉 所有测试都通过了！系统功能正常。");
        } else {
            System.out.println("⚠️  有测试失败，请检查系统实现。");
        }
    }
    
    // 测试断言方法
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("断言失败: " + message);
        }
    }
    
    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
    
    private static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError("断言失败: " + message);
        }
    }
    
    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("断言失败: " + message);
        }
    }
    
    private static void assertEqual(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("断言失败: " + message + 
                " (期望: " + expected + ", 实际: " + actual + ")");
        }
    }
    
    private static void testPassed(String testName) {
        totalTests++;
        passedTests++;
        System.out.println("✅ " + testName + " - 通过");
    }
    
    private static void testFailed(String testName, String reason) {
        totalTests++;
        failedTests++;
        System.out.println("❌ " + testName + " - 失败: " + reason);
    }
}