package udp_chat_system.frontend;

import udp_chat_system.core.UDPClient;
import udp_chat_system.core.Message;

import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * UDP聊天系统客户端演示程序
 * 
 * 提供控制台界面用于：
 * - 连接到聊天服务端
 * - 发送和接收聊天消息
 * - 查看连接状态
 * - 用户交互体验
 * 
 * 使用方法：
 * 1. 确保服务端已启动
 * 2. 编译: javac -d build core/*.java frontend/*.java
 * 3. 运行: java -cp build udp_chat_system.frontend.ClientDemo
 */
public class ClientDemo {
    
    private static UDPClient client;
    private static Scanner scanner;
    private static String currentUsername;
    private static boolean isInChat = false;
    
    // 默认服务端配置
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("         UDP聊天系统 - 客户端演示");
        System.out.println("===========================================");
        System.out.println("欢迎使用UDP聊天客户端程序");
        System.out.println("本程序用于连接UDP聊天服务端进行实时聊天");
        System.out.println();
        
        scanner = new Scanner(System.in);
        
        // 添加关闭钩子
        addShutdownHook();
        
        // 显示主菜单
        showMainMenu();
    }
    
    /**
     * 显示主菜单
     */
    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== 客户端主菜单 ===");
            System.out.println("1. 连接到服务端");
            System.out.println("2. 断开连接");
            System.out.println("3. 进入聊天模式");
            System.out.println("4. 发送单条消息");
            System.out.println("5. 查看连接状态");
            System.out.println("6. 测试连接");
            System.out.println("7. 帮助信息");
            System.out.println("0. 退出程序");
            System.out.println("=====================");
            
            System.out.print("请选择操作 [0-7]: ");
            
            String input = scanner.nextLine().trim();
            
            switch (input) {
                case "1":
                    connectToServer();
                    break;
                case "2":
                    disconnectFromServer();
                    break;
                case "3":
                    enterChatMode();
                    break;
                case "4":
                    sendSingleMessage();
                    break;
                case "5":
                    showConnectionStatus();
                    break;
                case "6":
                    testConnection();
                    break;
                case "7":
                    showHelp();
                    break;
                case "0":
                    exitProgram();
                    return;
                default:
                    System.out.println("无效选择，请输入0-7之间的数字");
                    break;
            }
        }
    }
    
    /**
     * 连接到服务端
     */
    private static void connectToServer() {
        if (client != null && client.isConnected()) {
            System.out.println("已连接到服务端: " + client.getServerInfo());
            return;
        }
        
        // 获取服务端信息
        String host = getServerHost();
        int port = getServerPort();
        String username = getUsername();
        
        try {
            System.out.println("正在连接服务端...");
            client = new UDPClient(host, port, username);
            currentUsername = username;
            
            // 设置消息处理器
            client.setMessageHandler(new ChatMessageHandler());
            
            // 尝试连接
            if (client.connect()) {
                System.out.println("连接成功！");
                System.out.println("用户名: " + username);
                System.out.println("服务端: " + client.getServerInfo());
                System.out.println("现在可以开始聊天了");
            } else {
                System.out.println("连接失败，请检查服务端是否正在运行");
                client = null;
                currentUsername = null;
            }
            
        } catch (UnknownHostException e) {
            System.err.println("无法解析主机地址: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("连接时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 断开与服务端的连接
     */
    private static void disconnectFromServer() {
        if (client == null || !client.isConnected()) {
            System.out.println("当前未连接到服务端");
            return;
        }
        
        System.out.println("正在断开连接...");
        client.disconnect();
        client = null;
        currentUsername = null;
        isInChat = false;
        System.out.println("已断开连接");
    }
    
    /**
     * 进入聊天模式
     */
    private static void enterChatMode() {
        if (client == null || !client.isConnected()) {
            System.out.println("请先连接到服务端");
            return;
        }
        
        isInChat = true;
        System.out.println("\n=== 进入聊天模式 ===");
        System.out.println("现在可以开始聊天了！");
        System.out.println("输入消息后按回车发送");
        System.out.println("输入 '/quit' 退出聊天模式");
        System.out.println("输入 '/help' 查看聊天命令");
        System.out.println("====================\n");
        
        while (isInChat && client != null && client.isConnected()) {
            System.out.print("[" + currentUsername + "] ");
            String input = scanner.nextLine();
            
            if (input == null) {
                break;
            }
            
            input = input.trim();
            
            // 处理聊天命令
            if (input.startsWith("/")) {
                if (!handleChatCommand(input)) {
                    break;
                }
                continue;
            }
            
            // 发送聊天消息
            if (!input.isEmpty()) {
                if (!client.sendChatMessage(input)) {
                    System.out.println("消息发送失败");
                }
            }
        }
        
        isInChat = false;
        System.out.println("已退出聊天模式");
    }
    
    /**
     * 处理聊天命令
     * @param command 命令字符串
     * @return true表示继续聊天，false表示退出聊天
     */
    private static boolean handleChatCommand(String command) {
        switch (command.toLowerCase()) {
            case "/quit":
            case "/exit":
                return false;
                
            case "/help":
                showChatHelp();
                break;
                
            case "/status":
                System.out.println("连接状态: " + (client.isConnected() ? "已连接" : "未连接"));
                System.out.println("用户名: " + currentUsername);
                System.out.println("服务端: " + client.getServerInfo());
                break;
                
            case "/test":
                boolean testResult = client.testConnection();
                System.out.println("连接测试: " + (testResult ? "正常" : "异常"));
                break;
                
            default:
                System.out.println("未知命令: " + command);
                System.out.println("输入 '/help' 查看可用命令");
                break;
        }
        
        return true;
    }
    
    /**
     * 显示聊天模式帮助信息
     */
    private static void showChatHelp() {
        System.out.println("\n=== 聊天命令帮助 ===");
        System.out.println("/quit   - 退出聊天模式");
        System.out.println("/exit   - 退出聊天模式");
        System.out.println("/help   - 显示此帮助信息");
        System.out.println("/status - 显示连接状态");
        System.out.println("/test   - 测试连接");
        System.out.println("====================\n");
    }
    
    /**
     * 发送单条消息
     */
    private static void sendSingleMessage() {
        if (client == null || !client.isConnected()) {
            System.out.println("请先连接到服务端");
            return;
        }
        
        System.out.print("请输入要发送的消息: ");
        String message = scanner.nextLine();
        
        if (message != null && !message.trim().isEmpty()) {
            if (client.sendChatMessage(message.trim())) {
                System.out.println("消息发送成功");
            } else {
                System.out.println("消息发送失败");
            }
        } else {
            System.out.println("消息内容不能为空");
        }
    }
    
    /**
     * 显示连接状态
     */
    private static void showConnectionStatus() {
        System.out.println("\n=== 连接状态信息 ===");
        if (client == null) {
            System.out.println("状态: 未初始化");
        } else {
            System.out.println("状态: " + (client.isConnected() ? "已连接" : "未连接"));
            System.out.println("用户名: " + (currentUsername != null ? currentUsername : "未设置"));
            System.out.println("服务端: " + client.getServerInfo());
        }
        System.out.println("===================");
    }
    
    /**
     * 测试连接
     */
    private static void testConnection() {
        if (client == null || !client.isConnected()) {
            System.out.println("当前未连接到服务端");
            return;
        }
        
        System.out.println("正在测试连接...");
        boolean result = client.testConnection();
        System.out.println("连接测试结果: " + (result ? "正常" : "异常"));
        
        if (!result) {
            System.out.println("建议检查网络连接或重新连接服务端");
        }
    }
    
    /**
     * 获取服务端主机地址
     */
    private static String getServerHost() {
        System.out.print("请输入服务端地址 [默认: " + DEFAULT_HOST + "]: ");
        String host = scanner.nextLine().trim();
        return host.isEmpty() ? DEFAULT_HOST : host;
    }
    
    /**
     * 获取服务端端口
     */
    private static int getServerPort() {
        while (true) {
            System.out.print("请输入服务端端口 [默认: " + DEFAULT_PORT + "]: ");
            String portStr = scanner.nextLine().trim();
            
            if (portStr.isEmpty()) {
                return DEFAULT_PORT;
            }
            
            try {
                int port = Integer.parseInt(portStr);
                if (port > 1024 && port < 65535) {
                    return port;
                } else {
                    System.out.println("端口号必须在1025-65534范围内");
                }
            } catch (NumberFormatException e) {
                System.out.println("请输入有效的端口号");
            }
        }
    }
    
    /**
     * 获取用户名
     */
    private static String getUsername() {
        while (true) {
            System.out.print("请输入用户名: ");
            String username = scanner.nextLine().trim();
            
            if (!username.isEmpty() && username.length() <= 20) {
                if (!username.contains("|")) {
                    return username;
                } else {
                    System.out.println("用户名不能包含 '|' 字符");
                }
            } else {
                System.out.println("用户名不能为空且不能超过20个字符");
            }
        }
    }
    
    /**
     * 显示帮助信息
     */
    private static void showHelp() {
        System.out.println("\n=== 帮助信息 ===");
        System.out.println("UDP聊天系统客户端使用说明:");
        System.out.println();
        System.out.println("1. 基本使用流程:");
        System.out.println("   a) 确保服务端已启动");
        System.out.println("   b) 选择'连接到服务端'并输入连接信息");
        System.out.println("   c) 选择'进入聊天模式'开始聊天");
        System.out.println("   d) 输入消息后按回车发送");
        System.out.println();
        System.out.println("2. 聊天模式命令:");
        System.out.println("   /quit   - 退出聊天模式");
        System.out.println("   /help   - 显示命令帮助");
        System.out.println("   /status - 显示连接状态");
        System.out.println("   /test   - 测试网络连接");
        System.out.println();
        System.out.println("3. 注意事项:");
        System.out.println("   - 用户名不能包含特殊字符 '|'");
        System.out.println("   - 确保网络连接正常");
        System.out.println("   - 服务端必须先启动");
        System.out.println("   - 使用Ctrl+C或选择退出安全关闭");
        System.out.println("==================");
    }
    
    /**
     * 安全退出程序
     */
    private static void exitProgram() {
        System.out.println("\n正在退出程序...");
        
        if (client != null && client.isConnected()) {
            System.out.println("正在断开连接...");
            client.disconnect();
        }
        
        if (scanner != null) {
            scanner.close();
        }
        
        System.out.println("程序已安全退出，感谢使用！");
        System.exit(0);
    }
    
    /**
     * 添加程序关闭钩子
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n检测到程序退出信号...");
            if (client != null && client.isConnected()) {
                System.out.println("正在断开连接...");
                client.disconnect();
            }
            System.out.println("清理完成");
        }));
    }
    
    /**
     * 聊天消息处理器
     * 实现UDPClient.MessageHandler接口
     */
    private static class ChatMessageHandler implements UDPClient.MessageHandler {
        @Override
        public void onMessageReceived(Message message) {
            // 根据消息类型处理
            switch (message.getType()) {
                case HEARTBEAT:
                    // 心跳消息不显示
                    break;
                    
                case CONNECT:
                case DISCONNECT:
                case SERVER_INFO:
                    // 系统消息以特殊格式显示
                    String formatted = message.getFormattedMessage();
                    if (!formatted.isEmpty()) {
                        if (isInChat) {
                            System.out.println("\r" + formatted);
                            System.out.print("[" + currentUsername + "] ");
                        } else {
                            System.out.println(formatted);
                        }
                    }
                    break;
                    
                case CHAT:
                    // 聊天消息显示
                    String chatMessage = message.getFormattedMessage();
                    if (!chatMessage.isEmpty()) {
                        if (isInChat) {
                            // 在聊天模式下，清除当前输入提示符并显示消息
                            System.out.println("\r" + chatMessage);
                            System.out.print("[" + currentUsername + "] ");
                        } else {
                            System.out.println("收到消息: " + chatMessage);
                        }
                    }
                    break;
                    
                default:
                    System.out.println("收到未知类型消息: " + message.toString());
                    break;
            }
        }
        
        @Override
        public void onConnectionStatusChanged(boolean connected) {
            String status = connected ? "已连接到服务端" : "与服务端断开连接";
            
            if (isInChat) {
                System.out.println("\r*** " + status + " ***");
                if (connected && currentUsername != null) {
                    System.out.print("[" + currentUsername + "] ");
                }
            } else {
                System.out.println("*** " + status + " ***");
            }
            
            if (!connected) {
                isInChat = false;
            }
        }
        
        @Override
        public void onError(String error) {
            if (isInChat) {
                System.out.println("\r错误: " + error);
                if (currentUsername != null) {
                    System.out.print("[" + currentUsername + "] ");
                }
            } else {
                System.out.println("错误: " + error);
            }
        }
    }
}