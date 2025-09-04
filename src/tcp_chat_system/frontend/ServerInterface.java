package tcp_chat_system.frontend;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import tcp_chat_system.core.ChatServer;
import tcp_chat_system.core.ChatMessage;

/**
 * TCP聊天服务器管理界面
 * 
 * 提供简单的控制台界面，让管理员可以：
 * 1. 启动/停止服务器
 * 2. 查看在线用户列表
 * 3. 查看消息历史
 * 4. 发送系统消息
 * 5. 查看服务器统计信息
 * 
 * @author TCP聊天系统实验
 * @version 1.0
 */
public class ServerInterface {
    
    private static ChatServer chatServer;
    private static Scanner scanner;
    private static boolean serverRunning = false;
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      TCP聊天服务器管理控制台");
        System.out.println("========================================");
        
        scanner = new Scanner(System.in);
        
        // 显示欢迎信息
        printWelcomeInfo();
        
        // 主循环
        runMainLoop();
        
        scanner.close();
        System.out.println("服务器管理程序已退出");
    }
    
    /**
     * 打印欢迎信息和说明
     */
    private static void printWelcomeInfo() {
        System.out.println("\nTCP聊天服务器特点：");
        System.out.println("• 支持多客户端并发连接");
        System.out.println("• 实时消息广播功能");
        System.out.println("• 客户端连接状态管理");
        System.out.println("• 消息历史记录保存");
        System.out.println("• 基于Socket TCP协议通信");
        System.out.println();
    }
    
    /**
     * 主循环 - 显示菜单并处理用户选择
     */
    private static void runMainLoop() {
        while (true) {
            printMainMenu();
            
            String choice = getUserInput("请选择操作 (1-7): ").trim();
            System.out.println();
            
            switch (choice) {
                case "1":
                    startServer();
                    break;
                case "2":
                    stopServer();
                    break;
                case "3":
                    showOnlineUsers();
                    break;
                case "4":
                    showMessageHistory();
                    break;
                case "5":
                    sendSystemMessage();
                    break;
                case "6":
                    showServerStatus();
                    break;
                case "7":
                    if (confirmExit()) {
                        if (serverRunning) {
                            stopServer();
                        }
                        return;
                    }
                    break;
                default:
                    System.out.println("无效选择，请输入1-7之间的数字");
            }
            
            System.out.println("\n按Enter键继续...");
            scanner.nextLine();
        }
    }
    
    /**
     * 打印主菜单
     */
    private static void printMainMenu() {
        System.out.println("========================================");
        System.out.println("            服务器管理菜单");
        System.out.println("========================================");
        System.out.println("1. 启动服务器");
        System.out.println("2. 停止服务器");
        System.out.println("3. 查看在线用户");
        System.out.println("4. 查看消息历史");
        System.out.println("5. 发送系统消息");
        System.out.println("6. 查看服务器状态");
        System.out.println("7. 退出程序");
        System.out.println("========================================");
        System.out.println("服务器状态: " + (serverRunning ? "运行中" : "已停止"));
        System.out.println("========================================");
    }
    
    /**
     * 启动服务器
     */
    private static void startServer() {
        if (serverRunning) {
            System.out.println("服务器已经在运行中！");
            return;
        }
        
        try {
            // 获取端口配置
            String portInput = getUserInput("请输入端口号 (直接回车使用默认端口8888): ").trim();
            int port = 8888; // 默认端口
            
            if (!portInput.isEmpty()) {
                try {
                    port = Integer.parseInt(portInput);
                    if (port < 1024 || port > 65535) {
                        System.out.println("端口号应在1024-65535范围内，使用默认端口8888");
                        port = 8888;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("端口号格式错误，使用默认端口8888");
                    port = 8888;
                }
            }
            
            // 创建并启动服务器
            chatServer = new ChatServer(port);
            
            // 在新线程中启动服务器，避免阻塞UI线程
            Thread serverThread = new Thread(() -> {
                try {
                    chatServer.start();
                } catch (IOException e) {
                    System.err.println("启动服务器失败: " + e.getMessage());
                    serverRunning = false;
                }
            });
            
            serverThread.setDaemon(true); // 设置为守护线程
            serverThread.start();
            
            // 等待一下确保服务器启动成功
            Thread.sleep(500);
            
            if (chatServer.isRunning()) {
                serverRunning = true;
                System.out.println("服务器启动成功！");
                System.out.println("监听端口: " + port);
                System.out.println("客户端可以通过以下方式连接:");
                System.out.println("  java -cp build tcp_chat_system.frontend.ClientInterface");
            } else {
                System.out.println("服务器启动失败，请检查端口是否被占用");
            }
            
        } catch (Exception e) {
            System.err.println("启动服务器时发生错误: " + e.getMessage());
            serverRunning = false;
        }
    }
    
    /**
     * 停止服务器
     */
    private static void stopServer() {
        if (!serverRunning || chatServer == null) {
            System.out.println("服务器未运行");
            return;
        }
        
        System.out.println("正在停止服务器...");
        chatServer.stop();
        serverRunning = false;
        chatServer = null;
        System.out.println("服务器已停止");
    }
    
    /**
     * 显示在线用户列表
     */
    private static void showOnlineUsers() {
        if (!serverRunning || chatServer == null) {
            System.out.println("服务器未运行，无法查看在线用户");
            return;
        }
        
        List<String> onlineUsers = chatServer.getOnlineUsers();
        
        System.out.println("========================================");
        System.out.println("           在线用户列表");
        System.out.println("========================================");
        System.out.println("当前在线用户数: " + onlineUsers.size());
        System.out.println();
        
        if (onlineUsers.isEmpty()) {
            System.out.println("暂无在线用户");
        } else {
            for (int i = 0; i < onlineUsers.size(); i++) {
                System.out.println((i + 1) + ". " + onlineUsers.get(i));
            }
        }
        System.out.println("========================================");
    }
    
    /**
     * 显示消息历史
     */
    private static void showMessageHistory() {
        if (!serverRunning || chatServer == null) {
            System.out.println("服务器未运行，无法查看消息历史");
            return;
        }
        
        List<ChatMessage> messageHistory = chatServer.getMessageHistory();
        
        System.out.println("========================================");
        System.out.println("           消息历史记录");
        System.out.println("========================================");
        System.out.println("历史消息总数: " + messageHistory.size());
        System.out.println();
        
        if (messageHistory.isEmpty()) {
            System.out.println("暂无消息记录");
        } else {
            // 显示最近50条消息
            int startIndex = Math.max(0, messageHistory.size() - 50);
            System.out.println("显示最近" + (messageHistory.size() - startIndex) + "条消息:");
            System.out.println();
            
            for (int i = startIndex; i < messageHistory.size(); i++) {
                ChatMessage msg = messageHistory.get(i);
                System.out.println(msg.getDisplayString());
            }
        }
        System.out.println("========================================");
    }
    
    /**
     * 发送系统消息
     */
    private static void sendSystemMessage() {
        if (!serverRunning || chatServer == null) {
            System.out.println("服务器未运行，无法发送系统消息");
            return;
        }
        
        String content = getUserInput("请输入系统消息内容: ").trim();
        
        if (content.isEmpty()) {
            System.out.println("消息内容不能为空");
            return;
        }
        
        // 创建并广播系统消息
        ChatMessage systemMessage = ChatMessage.createSystemMessage(content);
        chatServer.broadcastMessage(systemMessage);
        
        System.out.println("系统消息已发送: " + content);
    }
    
    /**
     * 显示服务器状态
     */
    private static void showServerStatus() {
        System.out.println("========================================");
        System.out.println("           服务器状态信息");
        System.out.println("========================================");
        System.out.println("运行状态: " + (serverRunning ? "运行中" : "已停止"));
        
        if (serverRunning && chatServer != null) {
            System.out.println("监听端口: " + chatServer.getPort());
            System.out.println("在线用户数: " + chatServer.getOnlineUsers().size());
            System.out.println("消息总数: " + chatServer.getMessageHistory().size());
            
            // JVM状态信息
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            long freeMemory = runtime.freeMemory() / 1024 / 1024;
            long usedMemory = totalMemory - freeMemory;
            
            System.out.println("内存使用: " + usedMemory + "MB / " + totalMemory + "MB");
            System.out.println("可用处理器: " + runtime.availableProcessors());
        }
        System.out.println("========================================");
    }
    
    /**
     * 确认退出
     * 
     * @return 确认退出返回true
     */
    private static boolean confirmExit() {
        if (serverRunning) {
            String confirm = getUserInput("服务器正在运行，确定要退出吗？(y/N): ").trim().toLowerCase();
            return confirm.equals("y") || confirm.equals("yes");
        } else {
            String confirm = getUserInput("确定要退出程序吗？(y/N): ").trim().toLowerCase();
            return confirm.equals("y") || confirm.equals("yes");
        }
    }
    
    /**
     * 获取用户输入
     * 
     * @param prompt 提示信息
     * @return 用户输入的字符串
     */
    private static String getUserInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}