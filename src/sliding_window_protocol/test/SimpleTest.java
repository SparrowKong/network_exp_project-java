package sliding_window_protocol.test;

import sliding_window_protocol.core.StopAndWaitProtocol;

/**
 * 简单测试类 - 用于验证停等协议的基本功能
 */
public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("========== 停等协议功能测试 ==========");
        
        // 创建协议实例
        StopAndWaitProtocol protocol = new StopAndWaitProtocol();
        
        // 测试1：发送单个消息
        System.out.println("\n【测试1】发送单个消息");
        protocol.sendData("Hello World");
        
        // 测试2：发送多个消息
        System.out.println("\n【测试2】发送多个消息");
        String[] messages = {"消息1", "消息2", "消息3"};
        protocol.sendMessages(messages);
        
        // 测试3：调整网络参数后测试
        System.out.println("\n【测试3】高丢包率环境测试");
        protocol.setNetworkParameters(0.3, 200);  // 30%丢包率，200ms延迟
        protocol.sendData("挑战消息");
        
        // 显示统计信息
        protocol.printStatistics();
        
        System.out.println("\n========== 测试完成 ==========");
    }
}