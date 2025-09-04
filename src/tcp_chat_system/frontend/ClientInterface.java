package tcp_chat_system.frontend;

import java.util.Scanner;
import tcp_chat_system.core.ChatClient;
import tcp_chat_system.core.ChatMessage;

/**
 * TCP聊天客户端用户界面
 * 
 * 提供简单的控制台界面，让用户可以：
 * 1. 连接到聊天服务器
 * 2. 发送和接收聊天消息
 * 3. 查看连接状态
 * 4. 优雅地断开连接
 * 
 * @author TCP聊天系统实验
 * @version 1.0
 */
public class ClientInterface {
    
    private static ChatClient chatClient;
    private static Scanner scanner;
    private static boolean clientRunning = true;
    private static String currentUsername;
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("        TCP聊天客户端");
        System.out.println("========================================");
        
        scanner = new Scanner(System.in);
        
        // 显示欢迎信息
        printWelcomeInfo();
        
        // 连接到服务器
        if (!connectToServer()) {
            System.out.println("连接失败，程序退出");
            scanner.close();
            return;
        }
        
        // 主聊天循环
        runChatLoop();
        
        scanner.close();
        System.out.println("聊天客户端已退出");
    }
    
    /**
     * 打印欢迎信息
     */
    private static void printWelcomeInfo() {
        System.out.println("\nTCP聊天客户端特点：");
        System.out.println("• 实时聊天消息收发");
        System.out.println("• 多用户聊天室功能");
        System.out.println("• 基于Socket TCP协议");
        System.out.println("• 支持中文消息");
        System.out.println();
    }
    
    /**
     * 连接到服务器
     * 
     * @return 连接成功返回true
     */
    private static boolean connectToServer() {
        // 获取服务器连接信息
        String serverHost = getUserInput("请输入服务器地址 (直接回车使用localhost): ").trim();
        if (serverHost.isEmpty()) {
            serverHost = "localhost";
        }
        
        String portInput = getUserInput("请输入服务器端口 (直接回车使用8888): ").trim();
        int serverPort = 8888;
        
        if (!portInput.isEmpty()) {
            try {
                serverPort = Integer.parseInt(portInput);
            } catch (NumberFormatException e) {
                System.out.println("端口格式错误，使用默认端口8888");
                serverPort = 8888;
            }
        }
        
        // 获取用户名
        while (true) {
            currentUsername = getUserInput("请输入您的用户名: ").trim();
            if (!currentUsername.isEmpty() && currentUsername.length() <= 20 && !currentUsername.contains("|")) {
                break;
            }
            System.out.println("用户名不合法（不能为空，长度不超过20字符，不能包含'|'字符）");
        }
        
        // 创建客户端并设置消息回调
        chatClient = new ChatClient(serverHost, serverPort);
        setupMessageCallback();
        
        System.out.println("正在连接到服务器 " + serverHost + ":" + serverPort + "...");
        
        // 尝试连接
        if (chatClient.connect(currentUsername)) {
            System.out.println("连接成功！欢迎进入聊天室，" + currentUsername + "！");
            System.out.println();
            System.out.println("聊天使用说明：");
            System.out.println("• 直接输入消息并按回车发送");
            System.out.println("• 输入 '/help' 查看帮助");
            System.out.println("• 输入 '/quit' 退出聊天室");
            System.out.println("========================================");
            return true;
        } else {
            System.out.println("连接失败！请检查服务器地址和端口是否正确，或服务器是否已启动");
            return false;
        }
    }
    
    /**
     * 设置消息回调处理器
     */
    private static void setupMessageCallback() {
        chatClient.setMessageCallback(new ChatClient.MessageCallback() {
            @Override
            public void onMessageReceived(ChatMessage message) {
                // 显示接收到的聊天消息
                System.out.println(message.getDisplayString());
                System.out.print("> "); // 重新显示输入提示符
            }
            
            @Override
            public void onSystemNotification(String notification) {
                // 显示系统通知
                System.out.println(notification);
                if (!notification.startsWith("===")) {
                    System.out.print("> "); // 重新显示输入提示符
                }
            }
            
            @Override
            public void onConnectionStatusChanged(boolean connected) {
                if (!connected) {
                    System.out.println("\n[系统] 与服务器的连接已断开");
                    clientRunning = false;
                }
            }
        });
    }
    
    /**
     * 主聊天循环
     */
    private static void runChatLoop() {
        System.out.println("您现在可以开始聊天了！");
        
        while (clientRunning && chatClient.isConnected()) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            // 处理特殊命令
            if (input.startsWith("/")) {
                handleCommand(input);
            } else {
                // 发送普通聊天消息
                chatClient.sendMessage(input);
            }
        }
        
        // 断开连接
        if (chatClient.isConnected()) {
            chatClient.disconnect();
        }
    }
    
    /**
     * 处理用户命令
     * 
     * @param command 用户输入的命令
     */
    private static void handleCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        
        switch (cmd) {
            case "/help":
            case "/h":
                showHelpInfo();
                break;
                
            case "/quit":
            case "/exit":
            case "/q":
                System.out.println("正在退出聊天室...");
                clientRunning = false;
                break;
                
            case "/status":
                showClientStatus();
                break;
                
            case "/clear":
                clearScreen();
                break;
                
            case "/username":
            case "/whoami":
                System.out.println("当前用户名: " + currentUsername);
                break;
                
            default:
                System.out.println("未知命令: " + cmd);
                System.out.println("输入 '/help' 查看可用命令");
        }
    }
    
    /**
     * 显示帮助信息
     */
    private static void showHelpInfo() {
        System.out.println("========================================");
        System.out.println("           可用命令列表");
        System.out.println("========================================");
        System.out.println("/help, /h        - 显示此帮助信息");
        System.out.println("/quit, /exit, /q - 退出聊天室");
        System.out.println("/status          - 显示连接状态");
        System.out.println("/clear           - 清屏");
        System.out.println("/username        - 显示当前用户名");
        System.out.println();
        System.out.println("聊天说明：");
        System.out.println("• 直接输入文字发送消息");
        System.out.println("• 消息会实时广播给所有在线用户");
        System.out.println("• 支持中文和特殊字符");
        System.out.println("========================================");
    }
    
    /**
     * 显示客户端状态
     */
    private static void showClientStatus() {
        System.out.println("========================================");
        System.out.println("           客户端状态信息");
        System.out.println("========================================");
        System.out.println("用户名: " + currentUsername);
        System.out.println("连接状态: " + (chatClient.isConnected() ? "已连接" : "已断开"));
        System.out.println("服务器地址: " + chatClient.getServerHost() + ":" + chatClient.getServerPort());
        System.out.println("待发送消息: " + chatClient.getPendingMessageCount() + " 条");
        System.out.println("客户端状态: " + (clientRunning ? "运行中" : "准备退出"));
        System.out.println("========================================");
    }
    
    /**
     * 清屏功能
     */
    private static void clearScreen() {
        // 简单的清屏实现 - 输出多行空行
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        
        System.out.println("========================================");
        System.out.println("        TCP聊天客户端");
        System.out.println("        用户: " + currentUsername);
        System.out.println("        连接状态: " + (chatClient.isConnected() ? "已连接" : "已断开"));
        System.out.println("========================================");
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
    
    /**
     * 程序退出时的清理工作
     */
    static {
        // 添加关闭钩子，确保程序退出时断开连接
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (chatClient != null && chatClient.isConnected()) {
                System.out.println("\n正在断开连接...");
                chatClient.disconnect();
            }
        }));
    }
}