package sliding_window_protocol.core;

import java.util.Random;

/**
 * 停等协议实现 - 简化版
 * 
 * 这是一个用于教学演示的停等协议实现，包含：
 * 1. 基本的数据包结构
 * 2. 发送方和接收方逻辑  
 * 3. 简单的网络模拟（延迟、丢包）
 * 4. 超时重传机制
 * 
 * @author 网络实验项目
 * @version 1.0
 */
public class StopAndWaitProtocol {
    
    /**
     * 数据包内部类
     */
    public static class Packet {
        private int sequenceNumber;    // 序列号
        private String data;           // 数据内容
        private boolean isAck;         // 是否为ACK包
        private long timestamp;        // 时间戳
        
        // 构造数据包
        public Packet(int seqNum, String data) {
            this.sequenceNumber = seqNum;
            this.data = data;
            this.isAck = false;
            this.timestamp = System.currentTimeMillis();
        }
        
        // 构造ACK包
        public Packet(int seqNum) {
            this.sequenceNumber = seqNum;
            this.data = "";
            this.isAck = true;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getter方法
        public int getSequenceNumber() { return sequenceNumber; }
        public String getData() { return data; }
        public boolean isAck() { return isAck; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("[%s] 序号:%d, 数据:%s", 
                               isAck ? "ACK" : "DATA", sequenceNumber, data);
        }
    }
    
    /**
     * 网络模拟器内部类
     */
    public static class NetworkSimulator {
        private Random random = new Random();
        private double lossRate = 0.1;  // 10% 丢包率
        private int delay = 100;        // 100ms 延迟
        
        /**
         * 模拟发送数据包
         * @param packet 要发送的包
         * @return 是否发送成功（未丢包）
         */
        public boolean sendPacket(Packet packet) {
            // 模拟网络延迟
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 模拟丢包
            if (random.nextDouble() < lossRate) {
                System.out.println("网络模拟: 数据包丢失 - " + packet);
                return false;
            }
            
            System.out.println("网络模拟: 数据包发送成功 - " + packet);
            return true;
        }
    }
    
    // 协议参数
    private static final int TIMEOUT = 1000;      // 超时时间1秒
    private static final int MAX_RETRIES = 3;     // 最大重传次数
    
    // 组件
    private NetworkSimulator network;
    
    // 发送方状态
    private int sendSeqNum = 0;        // 发送序列号
    private Packet waitingAck = null;  // 等待确认的包
    private int retryCount = 0;        // 重传次数
    
    // 接收方状态  
    private int expectedSeqNum = 0;    // 期望的序列号
    
    // 统计信息
    private int totalSent = 0;
    private int totalRetries = 0;
    
    /**
     * 构造函数
     */
    public StopAndWaitProtocol() {
        this.network = new NetworkSimulator();
        System.out.println("停等协议初始化完成");
    }
    
    /**
     * 发送方：发送数据
     * @param data 要发送的数据
     * @return 是否发送成功
     */
    public boolean sendData(String data) {
        System.out.println("\n=== 发送方开始发送数据: '" + data + "' ===");
        
        // 创建数据包
        Packet packet = new Packet(sendSeqNum, data);
        waitingAck = packet;
        retryCount = 0;
        
        // 尝试发送
        while (retryCount <= MAX_RETRIES) {
            System.out.printf("发送尝试 %d: 序号=%d, 数据='%s'%n", 
                             retryCount + 1, packet.getSequenceNumber(), packet.getData());
            
            totalSent++;
            if (retryCount > 0) {
                totalRetries++;
            }
            
            // 模拟发送
            if (network.sendPacket(packet)) {
                // 等待ACK
                if (waitForAck()) {
                    System.out.println("发送成功！收到确认");
                    sendSeqNum = 1 - sendSeqNum;  // 切换序列号（0和1交替）
                    waitingAck = null;
                    return true;
                }
            }
            
            // 发送失败或超时，准备重传
            retryCount++;
            if (retryCount <= MAX_RETRIES) {
                System.out.printf("发送失败，准备第 %d 次重传...%n", retryCount);
            }
        }
        
        System.out.println("发送最终失败！超过最大重传次数");
        waitingAck = null;
        return false;
    }
    
    /**
     * 等待ACK确认
     * @return 是否收到正确的ACK
     */
    private boolean waitForAck() {
        System.out.println("等待ACK确认中...");
        
        long startTime = System.currentTimeMillis();
        
        // 模拟等待ACK的过程
        while (System.currentTimeMillis() - startTime < TIMEOUT) {
            // 模拟接收方处理和发送ACK
            Packet ack = simulateReceiverResponse(waitingAck);
            
            if (ack != null) {
                System.out.println("收到ACK: " + ack);
                
                // 检查ACK序号是否正确
                if (ack.getSequenceNumber() == waitingAck.getSequenceNumber()) {
                    System.out.println("ACK确认成功！");
                    return true;
                } else {
                    System.out.println("收到错误序号的ACK，继续等待...");
                }
            }
            
            // 短暂休息
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("等待ACK超时！");
        return false;
    }
    
    /**
     * 模拟接收方的响应
     * @param dataPacket 接收到的数据包
     * @return ACK包，如果丢失则返回null
     */
    private Packet simulateReceiverResponse(Packet dataPacket) {
        if (dataPacket == null) return null;
        
        System.out.println("接收方: 收到数据包 - " + dataPacket);
        
        // 检查序号是否正确
        if (dataPacket.getSequenceNumber() == expectedSeqNum) {
            System.out.printf("接收方: 数据包序号正确，期望序号=%d%n", expectedSeqNum);
            
            // 交付数据给上层
            System.out.printf("接收方: 数据交付给上层应用 - '%s'%n", dataPacket.getData());
            
            // 更新期望序号
            expectedSeqNum = 1 - expectedSeqNum;  // 切换期望序号（0和1交替）
        } else {
            System.out.printf("接收方: 数据包序号错误，期望序号=%d，实际序号=%d%n", 
                             expectedSeqNum, dataPacket.getSequenceNumber());
        }
        
        // 发送ACK（始终发送最后正确接收的序号的ACK）
        int ackSeqNum = 1 - expectedSeqNum;  // ACK确认最后正确接收的包
        Packet ack = new Packet(ackSeqNum);
        
        System.out.printf("接收方: 发送ACK，序号=%d%n", ackSeqNum);
        
        // 模拟ACK传输（可能丢失）
        if (network.sendPacket(ack)) {
            return ack;
        } else {
            return null;  // ACK丢失
        }
    }
    
    /**
     * 发送多个数据包（演示用）
     * @param messages 要发送的消息列表
     */
    public void sendMessages(String[] messages) {
        System.out.println("\n========== 开始批量发送数据 ==========");
        
        for (int i = 0; i < messages.length; i++) {
            System.out.printf("\n--- 发送第 %d 个消息 ---", i + 1);
            boolean success = sendData(messages[i]);
            
            if (success) {
                System.out.println("✓ 消息发送成功");
            } else {
                System.out.println("✗ 消息发送失败");
            }
            
            // 消息间稍作停顿
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("\n========== 批量发送完成 ==========");
    }
    
    /**
     * 重置协议状态
     */
    public void reset() {
        sendSeqNum = 0;
        expectedSeqNum = 0;
        waitingAck = null;
        retryCount = 0;
        totalSent = 0;
        totalRetries = 0;
        System.out.println("协议状态已重置");
    }
    
    /**
     * 设置网络参数
     * @param lossRate 丢包率 (0.0-1.0)
     * @param delay 网络延迟 (毫秒)
     */
    public void setNetworkParameters(double lossRate, int delay) {
        network.lossRate = Math.max(0.0, Math.min(1.0, lossRate));
        network.delay = Math.max(0, delay);
        System.out.printf("网络参数设置: 丢包率=%.1f%%, 延迟=%dms%n", 
                         network.lossRate * 100, network.delay);
    }
    
    /**
     * 打印统计信息
     */
    public void printStatistics() {
        System.out.println("\n=== 传输统计信息 ===");
        System.out.printf("总发送次数: %d%n", totalSent);
        System.out.printf("重传次数: %d%n", totalRetries);
        System.out.printf("重传率: %.1f%%%n", 
                         totalSent > 0 ? (totalRetries * 100.0 / totalSent) : 0);
        System.out.println("==================");
    }
}