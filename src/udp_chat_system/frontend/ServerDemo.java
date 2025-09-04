package udp_chat_system.frontend;

import udp_chat_system.core.UDPServer;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * UDP聊天系统服务端演示程序
 * 
 * 提供控制台界面用于：
 * - 启动和停止服务端
 * - 查看在线客户端信息
 * - 监控服务端运行状态
 * - 查看统计信息
 * - 服务端管理功能
 * 
 * 使用方法：
 * 1. 编译: javac -d build core/*.java frontend/*.java
 * 2. 运行: java -cp build udp_chat_system.frontend.ServerDemo
 */
public class ServerDemo {
    
    private static UDPServer server;
    private static Scanner scanner;
    private static final int DEFAULT_PORT = 8888;
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("         UDP聊天系统 - 服务端演示");
        System.out.println("===========================================");
        System.out.println("欢迎使用UDP聊天服务端管理程序");
        System.out.println("本程序用于启动和管理UDP聊天服务端");
        System.out.println();
        
        scanner = new Scanner(System.in);
        
        // 解析命令行参数
        int port = parsePort(args);
        server = new UDPServer(port);
        
        // 添加关闭钩子
        addShutdownHook();
        
        // 显示主菜单
        showMainMenu();
    }
    
    /**
     * 解析命令行参数中的端口号
     * @param args 命令行参数
     * @return 端口号
     */
    private static int parsePort(String[] args) {
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                if (port > 1024 && port < 65535) {
                    System.out.println("使用指定端口: " + port);
                    return port;
                } else {
                    System.out.println("端口号无效，使用默认端口: " + DEFAULT_PORT);
                }
            } catch (NumberFormatException e) {
                System.out.println("端口号格式错误，使用默认端口: " + DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }
    
    /**
     * 显示主菜单
     */
    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== 服务端管理菜单 ===");
            System.out.println("1. 启动服务端");
            System.out.println("2. 停止服务端");
            System.out.println("3. 查看服务端状态");
            System.out.println("4. 查看在线客户端");
            System.out.println("5. 查看统计信息");
            System.out.println("6. 服务端配置信息");
            System.out.println("7. 帮助信息");
            System.out.println("0. 退出程序");
            System.out.println("========================");
            
            System.out.print("请选择操作 [0-7]: ");
            
            String input = scanner.nextLine().trim();
            
            switch (input) {
                case "1":
                    startServer();
                    break;
                case "2":
                    stopServer();
                    break;
                case "3":
                    showServerStatus();
                    break;
                case "4":
                    showOnlineClients();
                    break;
                case "5":
                    showStatistics();
                    break;
                case "6":
                    showServerConfig();
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
     * 启动服务端
     */
    private static void startServer() {
        if (server.isRunning()) {
            System.out.println("服务端已在运行中，端口: " + server.getPort());
            return;
        }
        
        try {
            System.out.println("正在启动服务端...");
            server.start();
            System.out.println("服务端启动成功！");
            System.out.println("客户端可通过以下方式连接:");
            System.out.println("- 主机: localhost 或 127.0.0.1");
            System.out.println("- 端口: " + server.getPort());
            
            // 提示如何运行客户端
            System.out.println();
            System.out.println("提示: 可以运行以下命令启动客户端测试:");
            System.out.println("java -cp build udp_chat_system.frontend.ClientDemo");
            
        } catch (IOException e) {
            System.err.println("启动服务端失败: " + e.getMessage());
            System.out.println("请检查端口是否被占用，或尝试使用其他端口");
        }
    }
    
    /**
     * 停止服务端
     */
    private static void stopServer() {
        if (!server.isRunning()) {
            System.out.println("服务端未运行");
            return;
        }
        
        System.out.print("确认停止服务端？这将断开所有客户端连接 (y/N): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            System.out.println("正在停止服务端...");
            server.stop();
            System.out.println("服务端已停止");
        } else {
            System.out.println("取消停止操作");
        }
    }
    
    /**
     * 显示服务端状态
     */
    private static void showServerStatus() {
        System.out.println("\n=== 服务端状态信息 ===");
        System.out.println("运行状态: " + (server.isRunning() ? "运行中" : "已停止"));
        System.out.println("监听端口: " + server.getPort());
        
        if (server.isRunning()) {
            System.out.println("在线用户数: " + server.getOnlineClientCount());
            System.out.println(server.getStatistics());
        }
        System.out.println("=======================");
    }
    
    /**
     * 显示在线客户端列表
     */
    private static void showOnlineClients() {
        if (!server.isRunning()) {
            System.out.println("服务端未运行");
            return;
        }
        
        List<String> clients = server.getOnlineClients();
        
        System.out.println("\n=== 在线客户端列表 ===");
        if (clients.isEmpty()) {
            System.out.println("暂无在线客户端");
        } else {
            System.out.println("共有 " + clients.size() + " 个客户端在线:");
            for (int i = 0; i < clients.size(); i++) {
                System.out.println((i + 1) + ". " + clients.get(i));
            }
        }
        System.out.println("========================");
    }
    
    /**
     * 显示统计信息
     */
    private static void showStatistics() {
        System.out.println("\n=== 服务端统计信息 ===");
        if (server.isRunning()) {
            System.out.println(server.getStatistics());
            System.out.println("服务端端口: " + server.getPort());
            System.out.println("在线客户端数: " + server.getOnlineClientCount());
        } else {
            System.out.println("服务端未运行，无统计数据");
        }
        System.out.println("========================");
    }
    
    /**
     * 显示服务端配置信息
     */
    private static void showServerConfig() {
        System.out.println("\n=== 服务端配置信息 ===");
        System.out.println("默认端口: " + DEFAULT_PORT);
        System.out.println("当前端口: " + server.getPort());
        System.out.println("缓冲区大小: 1024 字节");
        System.out.println("线程池大小: 10");
        System.out.println("客户端超时: 30 秒");
        System.out.println("心跳间隔: 15 秒");
        System.out.println("协议类型: UDP");
        System.out.println("字符编码: UTF-8");
        System.out.println("========================");
    }
    
    /**
     * 显示帮助信息
     */
    private static void showHelp() {
        System.out.println("\n=== 帮助信息 ===");
        System.out.println("UDP聊天系统服务端管理程序使用说明:");
        System.out.println();
        System.out.println("1. 启动程序:");
        System.out.println("   java -cp build udp_chat_system.frontend.ServerDemo [端口号]");
        System.out.println("   例如: java -cp build udp_chat_system.frontend.ServerDemo 9999");
        System.out.println();
        System.out.println("2. 主要功能:");
        System.out.println("   - 启动/停止UDP聊天服务端");
        System.out.println("   - 监控在线客户端状态");
        System.out.println("   - 查看服务端运行统计");
        System.out.println("   - 实时显示客户端连接信息");
        System.out.println();
        System.out.println("3. 客户端连接:");
        System.out.println("   客户端程序: java -cp build udp_chat_system.frontend.ClientDemo");
        System.out.println("   默认服务端地址: localhost:8888");
        System.out.println();
        System.out.println("4. 注意事项:");
        System.out.println("   - 确保防火墙允许UDP通信");
        System.out.println("   - 端口号范围: 1025-65534");
        System.out.println("   - 使用Ctrl+C或选择退出安全关闭");
        System.out.println("==================");
    }
    
    /**
     * 安全退出程序
     */
    private static void exitProgram() {
        System.out.println("\n正在退出程序...");
        
        if (server.isRunning()) {
            System.out.println("正在停止服务端...");
            server.stop();
        }
        
        if (scanner != null) {
            scanner.close();
        }
        
        System.out.println("程序已安全退出，感谢使用！");
        System.exit(0);
    }
    
    /**
     * 添加程序关闭钩子
     * 确保程序异常退出时能正确关闭服务端
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n检测到程序退出信号...");
            if (server != null && server.isRunning()) {
                System.out.println("正在关闭服务端...");
                server.stop();
            }
            System.out.println("清理完成");
        }));
    }
    
    /**
     * 等待用户按回车键继续
     */
    @SuppressWarnings("unused")
    private static void waitForEnter() {
        System.out.print("按回车键继续...");
        scanner.nextLine();
    }
    
    /**
     * 显示欢迎信息
     */
    @SuppressWarnings("unused")
    private static void showWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║        UDP聊天系统 - 服务端          ║");
        System.out.println("║                                      ║");
        System.out.println("║  本程序演示UDP协议实现的聊天系统     ║");
        System.out.println("║  支持多客户端并发连接和消息广播      ║");
        System.out.println("║                                      ║");
        System.out.println("║  开发目标：教学演示网络编程基础      ║");
        System.out.println("║  协议类型：UDP (用户数据报协议)      ║");
        System.out.println("║  架构模式：客户端-服务端 (C/S)       ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }
}