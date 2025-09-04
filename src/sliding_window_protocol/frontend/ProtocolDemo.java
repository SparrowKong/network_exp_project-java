package sliding_window_protocol.frontend;

import java.util.Scanner;
import sliding_window_protocol.core.StopAndWaitProtocol;

/**
 * 停等协议演示程序
 * 
 * 提供简单的控制台界面，让用户可以：
 * 1. 发送单个消息
 * 2. 发送多个消息 
 * 3. 调整网络参数
 * 4. 查看统计信息
 * 
 * @author 网络实验项目
 * @version 1.0
 */
public class ProtocolDemo {
    
    private static StopAndWaitProtocol protocol;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       停等协议模拟演示程序");
        System.out.println("========================================");
        
        // 初始化
        protocol = new StopAndWaitProtocol();
        scanner = new Scanner(System.in);
        
        // 显示欢迎信息
        printWelcomeInfo();
        
        // 主循环
        runMainLoop();
        
        scanner.close();
        System.out.println("程序结束，再见！");
    }
    
    /**
     * 打印欢迎信息和说明
     */
    private static void printWelcomeInfo() {
        System.out.println("\n停等协议特点：");
        System.out.println("• 发送方一次只能发送一个数据包");
        System.out.println("• 必须等待ACK确认后才能发送下一个包");
        System.out.println("• 超时未收到ACK会自动重传");
        System.out.println("• 序列号在0和1之间交替");
        System.out.println("\n当前网络环境：丢包率10%，延迟100ms");
        System.out.println("========================================");
    }
    
    /**
     * 主菜单循环
     */
    private static void runMainLoop() {
        while (true) {
            printMainMenu();
            
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    sendSingleMessage();
                    break;
                case 2:
                    sendMultipleMessages();
                    break;
                case 3:
                    runPredefinedDemo();
                    break;
                case 4:
                    adjustNetworkParameters();
                    break;
                case 5:
                    protocol.printStatistics();
                    break;
                case 6:
                    protocol.reset();
                    System.out.println("协议状态已重置");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("无效选择，请重新输入");
            }
            
            System.out.println("\n按回车键继续...");
            scanner.nextLine();
        }
    }
    
    /**
     * 打印主菜单
     */
    private static void printMainMenu() {
        System.out.println("\n========== 主菜单 ==========");
        System.out.println("1. 发送单个消息");
        System.out.println("2. 发送多个消息");
        System.out.println("3. 运行预定义演示");
        System.out.println("4. 调整网络参数");
        System.out.println("5. 查看统计信息");
        System.out.println("6. 重置协议状态");
        System.out.println("0. 退出程序");
        System.out.println("===========================");
        System.out.print("请选择: ");
    }
    
    /**
     * 获取用户选择
     */
    private static int getUserChoice() {
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            return choice;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * 发送单个消息
     */
    private static void sendSingleMessage() {
        System.out.print("请输入要发送的消息: ");
        String message = scanner.nextLine().trim();
        
        if (message.isEmpty()) {
            System.out.println("消息不能为空");
            return;
        }
        
        System.out.println("\n开始发送消息...");
        boolean success = protocol.sendData(message);
        
        if (success) {
            System.out.println("\n✓ 消息发送成功！");
        } else {
            System.out.println("\n✗ 消息发送失败！");
        }
    }
    
    /**
     * 发送多个消息
     */
    private static void sendMultipleMessages() {
        System.out.print("请输入要发送的消息数量: ");
        int count;
        try {
            count = Integer.parseInt(scanner.nextLine().trim());
            if (count <= 0) {
                System.out.println("消息数量必须大于0");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("无效的数字");
            return;
        }
        
        String[] messages = new String[count];
        
        for (int i = 0; i < count; i++) {
            System.out.printf("请输入第 %d 个消息: ", i + 1);
            messages[i] = scanner.nextLine().trim();
            if (messages[i].isEmpty()) {
                messages[i] = "消息 " + (i + 1);  // 默认消息
            }
        }
        
        // 发送所有消息
        protocol.sendMessages(messages);
    }
    
    /**
     * 运行预定义演示
     */
    private static void runPredefinedDemo() {
        System.out.println("运行预定义演示场景...");
        
        // 演示场景1：正常传输
        System.out.println("\n场景1：正常网络环境下的传输");
        protocol.setNetworkParameters(0.0, 50);  // 无丢包，低延迟
        String[] normalMessages = {"Hello", "World", "停等协议", "演示完成"};
        protocol.sendMessages(normalMessages);
        
        // 演示场景2：高丢包率环境
        System.out.println("\n场景2：高丢包率环境下的传输");
        protocol.setNetworkParameters(0.3, 100);  // 30%丢包率
        String[] challengingMessages = {"挑战消息1", "挑战消息2"};
        protocol.sendMessages(challengingMessages);
        
        // 恢复默认设置
        protocol.setNetworkParameters(0.1, 100);
        System.out.println("\n演示完成，网络参数已恢复默认值");
    }
    
    /**
     * 调整网络参数
     */
    private static void adjustNetworkParameters() {
        System.out.println("当前网络参数调整");
        
        // 设置丢包率
        System.out.print("请输入丢包率 (0-100, 默认10): ");
        String input = scanner.nextLine().trim();
        double lossRate = 0.1;  // 默认值
        
        if (!input.isEmpty()) {
            try {
                double rate = Double.parseDouble(input) / 100.0;
                if (rate >= 0 && rate <= 1) {
                    lossRate = rate;
                } else {
                    System.out.println("丢包率必须在0-100之间，使用默认值10%");
                }
            } catch (NumberFormatException e) {
                System.out.println("无效输入，使用默认值10%");
            }
        }
        
        // 设置延迟
        System.out.print("请输入网络延迟 (毫秒, 默认100): ");
        input = scanner.nextLine().trim();
        int delay = 100;  // 默认值
        
        if (!input.isEmpty()) {
            try {
                int d = Integer.parseInt(input);
                if (d >= 0) {
                    delay = d;
                } else {
                    System.out.println("延迟必须非负，使用默认值100ms");
                }
            } catch (NumberFormatException e) {
                System.out.println("无效输入，使用默认值100ms");
            }
        }
        
        // 应用设置
        protocol.setNetworkParameters(lossRate, delay);
        System.out.println("网络参数更新完成");
    }
    
    /**
     * 显示使用帮助
     */
    private static void printHelp() {
        System.out.println("\n========== 使用说明 ==========");
        System.out.println("1. 发送单个消息：");
        System.out.println("   输入任意文本消息进行发送");
        System.out.println("   观察发送过程和重传机制");
        
        System.out.println("\n2. 发送多个消息：");
        System.out.println("   批量发送多个消息");
        System.out.println("   观察停等协议的工作流程");
        
        System.out.println("\n3. 预定义演示：");
        System.out.println("   自动运行不同网络环境下的演示");
        System.out.println("   包括正常环境和高丢包率环境");
        
        System.out.println("\n4. 调整网络参数：");
        System.out.println("   修改丢包率和网络延迟");
        System.out.println("   观察不同网络条件对协议的影响");
        
        System.out.println("\n5. 统计信息：");
        System.out.println("   查看发送成功率、重传次数等统计");
        
        System.out.println("============================");
    }
    
    /**
     * 快速测试功能
     */
    public static void runQuickTest() {
        System.out.println("========== 快速功能测试 ==========");
        
        StopAndWaitProtocol testProtocol = new StopAndWaitProtocol();
        
        // 测试1：基本发送
        System.out.println("\n测试1：基本发送功能");
        testProtocol.sendData("测试消息1");
        
        // 测试2：多消息发送
        System.out.println("\n测试2：多消息发送");
        String[] messages = {"消息A", "消息B", "消息C"};
        testProtocol.sendMessages(messages);
        
        // 测试3：高丢包率测试
        System.out.println("\n测试3：高丢包率环境");
        testProtocol.setNetworkParameters(0.5, 200);  // 50%丢包率
        testProtocol.sendData("挑战消息");
        
        // 显示统计
        testProtocol.printStatistics();
        
        System.out.println("========== 测试完成 ==========");
    }
}