package crc_algorithm.frontend;

import crc_algorithm.core.CRCAlgorithm;
import crc_algorithm.core.CRCAlgorithm.CRCType;
import crc_algorithm.core.CRCAlgorithm.CRCResult;

import java.io.*;
import java.util.Scanner;
import java.util.Random;

/**
 * CRC校验算法交互演示程序
 * 
 * 这是一个控制台应用程序，提供友好的用户界面来演示CRC校验算法的功能。
 * 用户可以通过菜单选项体验不同的CRC操作和算法比较。
 * 
 * 主要功能：
 * 1. 手动输入数据计算CRC
 * 2. 文件数据CRC校验  
 * 3. 错误检测演示
 * 4. 不同CRC标准对比
 * 5. 教学演示模式
 * 
 * @author 网络实验项目
 * @version 1.0
 */
public class CRCDemo {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final CRCAlgorithm crcCalculator = new CRCAlgorithm();
    private static final Random random = new Random();
    
    // 统计信息
    private static int totalCalculations = 0;
    private static int errorsDetected = 0;
    
    /**
     * 程序主入口
     */
    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("    CRC校验算法演示程序");
        System.out.println("    Computer Network Laboratory");
        System.out.println("=====================================");
        System.out.println();
        
        showWelcomeMessage();
        
        while (true) {
            showMainMenu();
            int choice = getMenuChoice(1, 8);
            
            switch (choice) {
                case 1:
                    demonstrateBasicCRC();
                    break;
                case 2:
                    demonstrateErrorDetection();
                    break;
                case 3:
                    compareCRCAlgorithms();
                    break;
                case 4:
                    processFileData();
                    break;
                case 5:
                    showTeachingMode();
                    break;
                case 6:
                    customCRCCalculation();
                    break;
                case 7:
                    showStatistics();
                    break;
                case 8:
                    System.out.println("感谢使用CRC校验算法演示程序！");
                    System.exit(0);
                    break;
            }
            
            System.out.println("\n按回车键继续...");
            scanner.nextLine();
        }
    }
    
    /**
     * 显示欢迎信息和程序介绍
     */
    private static void showWelcomeMessage() {
        System.out.println("CRC（循环冗余校验）是一种数据错误检测方法，");
        System.out.println("广泛应用于计算机网络、数字通信和数据存储中。");
        System.out.println("本演示程序将帮助您理解CRC的工作原理和应用。");
        System.out.println();
    }
    
    /**
     * 显示主菜单
     */
    private static void showMainMenu() {
        System.out.println("=====================================");
        System.out.println("           主菜单选项");
        System.out.println("=====================================");
        System.out.println("1. 基本CRC计算演示");
        System.out.println("2. 错误检测演示");
        System.out.println("3. CRC算法对比");
        System.out.println("4. 文件数据校验");
        System.out.println("5. 教学演示模式");
        System.out.println("6. 自定义CRC计算");
        System.out.println("7. 统计信息查看");
        System.out.println("8. 退出程序");
        System.out.println("=====================================");
        System.out.print("请选择功能 (1-8): ");
    }
    
    /**
     * 获取用户菜单选择
     */
    private static int getMenuChoice(int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.print("选择无效，请输入 " + min + "-" + max + " 之间的数字: ");
            } catch (NumberFormatException e) {
                System.out.print("输入格式错误，请输入数字: ");
            }
        }
    }
    
    /**
     * 基本CRC计算演示
     */
    private static void demonstrateBasicCRC() {
        System.out.println("\n=== 基本CRC计算演示 ===");
        
        // 让用户选择CRC类型
        CRCType selectedType = selectCRCType();
        crcCalculator.setCurrentCRCType(selectedType);
        
        System.out.print("请输入要计算CRC的数据: ");
        String inputData = scanner.nextLine();
        
        if (inputData.isEmpty()) {
            System.out.println("输入数据不能为空！");
            return;
        }
        
        byte[] data = inputData.getBytes();
        
        try {
            // 计算CRC
            CRCResult result = crcCalculator.calculateCRC(data);
            totalCalculations++;
            
            System.out.println("\n" + result.getDetailedInfo());
            
            // 显示原始数据的十六进制表示
            System.out.println("原始数据 (十六进制): " + bytesToHex(data));
            System.out.println("编码数据 (十六进制): " + bytesToHex(result.getEncodedData()));
            
            // 验证编码数据的完整性
            CRCResult verification = crcCalculator.verifyData(result.getEncodedData());
            System.out.println("数据完整性验证: " + (verification.hasError() ? "失败" : "通过"));
            
        } catch (Exception e) {
            System.out.println("CRC计算出错: " + e.getMessage());
        }
    }
    
    /**
     * 错误检测演示
     */
    private static void demonstrateErrorDetection() {
        System.out.println("\n=== 错误检测演示 ===");
        System.out.println("此演示将展示CRC如何检测数据传输中的错误");
        
        CRCType selectedType = selectCRCType();
        crcCalculator.setCurrentCRCType(selectedType);
        
        System.out.print("请输入测试数据: ");
        String inputData = scanner.nextLine();
        
        if (inputData.isEmpty()) {
            inputData = "Hello CRC!"; // 默认数据
            System.out.println("使用默认数据: " + inputData);
        }
        
        byte[] originalData = inputData.getBytes();
        
        try {
            // 1. 计算原始数据的CRC
            CRCResult original = crcCalculator.calculateCRC(originalData);
            System.out.println("\n--- 原始数据信息 ---");
            System.out.println("数据内容: " + inputData);
            System.out.println("CRC校验码: 0x" + Long.toHexString(original.getCrcValue()).toUpperCase());
            
            // 2. 模拟无错误传输
            System.out.println("\n--- 无错误传输测试 ---");
            CRCResult noErrorTest = crcCalculator.verifyData(original.getEncodedData());
            System.out.println("校验结果: " + (noErrorTest.hasError() ? "检测到错误" : "数据完整"));
            
            // 3. 模拟单比特错误
            System.out.println("\n--- 单比特错误测试 ---");
            int totalBits = originalData.length * 8;
            int errorBit = random.nextInt(totalBits);
            
            byte[] errorData = crcCalculator.simulateError(original.getEncodedData(), errorBit);
            CRCResult errorTest = crcCalculator.verifyData(errorData);
            
            System.out.println("模拟错误位置: 第 " + errorBit + " 位");
            System.out.println("校验结果: " + (errorTest.hasError() ? "检测到错误" : "未检测到错误"));
            
            if (errorTest.hasError()) {
                errorsDetected++;
                System.out.println("✓ CRC成功检测到数据错误！");
            }
            
            // 4. 多次错误测试
            System.out.println("\n--- 多次随机错误测试 ---");
            int testCount = 10;
            int detectedCount = 0;
            
            for (int i = 0; i < testCount; i++) {
                int randomBit = random.nextInt(totalBits);
                byte[] testErrorData = crcCalculator.simulateError(original.getEncodedData(), randomBit);
                CRCResult testResult = crcCalculator.verifyData(testErrorData);
                
                if (testResult.hasError()) {
                    detectedCount++;
                }
            }
            
            System.out.println("测试次数: " + testCount);
            System.out.println("检测成功: " + detectedCount + " 次");
            System.out.println("检测率: " + (detectedCount * 100.0 / testCount) + "%");
            
            totalCalculations += testCount + 2;
            errorsDetected += detectedCount;
            
        } catch (Exception e) {
            System.out.println("错误检测演示出错: " + e.getMessage());
        }
    }
    
    /**
     * CRC算法对比演示
     */
    private static void compareCRCAlgorithms() {
        System.out.println("\n=== CRC算法对比演示 ===");
        
        System.out.print("请输入测试数据 (回车使用默认数据): ");
        String inputData = scanner.nextLine();
        
        if (inputData.isEmpty()) {
            inputData = "Network Protocol CRC Test Data 2024";
            System.out.println("使用默认测试数据: " + inputData);
        }
        
        byte[] data = inputData.getBytes();
        
        System.out.println("\n" + String.format("%-8s %-12s %-16s %-8s", "算法", "CRC值", "编码长度", "位宽"));
        System.out.println("================================================");
        
        // 对比所有CRC算法
        for (CRCType crcType : CRCType.values()) {
            try {
                CRCResult result = crcCalculator.calculateCRC(data, crcType);
                
                String crcValue = "0x" + Long.toHexString(result.getCrcValue()).toUpperCase();
                int encodedLength = result.getEncodedData().length;
                int width = crcType.getWidth();
                
                System.out.println(String.format("%-8s %-12s %-16d %-8d", 
                    crcType.getName(), crcValue, encodedLength, width));
                
                totalCalculations++;
            } catch (Exception e) {
                System.out.println(crcType.getName() + ": 计算失败 - " + e.getMessage());
            }
        }
        
        // 显示算法详细信息
        System.out.println("\n=== 算法详细信息 ===");
        for (CRCType crcType : CRCType.values()) {
            System.out.println(crcCalculator.getAlgorithmInfo(crcType));
        }
    }
    
    /**
     * 文件数据处理
     */
    private static void processFileData() {
        System.out.println("\n=== 文件数据校验 ===");
        System.out.println("注意: 此功能演示文件CRC校验的概念");
        System.out.println("实际环境中请谨慎处理大文件");
        
        System.out.print("请输入文件路径 (或回车创建示例文件): ");
        String filePath = scanner.nextLine();
        
        try {
            byte[] fileData;
            
            if (filePath.isEmpty()) {
                // 创建示例数据
                String sampleData = "这是一个CRC校验演示文件\n" +
                                   "包含网络协议实验数据\n" +
                                   "用于演示文件完整性检查\n" +
                                   "创建时间: " + System.currentTimeMillis();
                fileData = sampleData.getBytes("UTF-8");
                System.out.println("使用示例文件数据 (" + fileData.length + " 字节)");
            } else {
                // 读取实际文件
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("文件不存在: " + filePath);
                    return;
                }
                
                if (file.length() > 1024 * 1024) { // 限制1MB
                    System.out.println("文件过大，演示程序限制为1MB以内");
                    return;
                }
                
                fileData = readFileBytes(file);
                System.out.println("读取文件: " + filePath + " (" + fileData.length + " 字节)");
            }
            
            // 选择CRC类型
            CRCType selectedType = selectCRCType();
            
            // 计算文件CRC
            CRCResult result = crcCalculator.calculateCRC(fileData, selectedType);
            
            System.out.println("\n=== 文件CRC校验结果 ===");
            System.out.println("文件大小: " + fileData.length + " 字节");
            System.out.println("CRC算法: " + selectedType.getName());
            System.out.println("校验码: 0x" + Long.toHexString(result.getCrcValue()).toUpperCase());
            System.out.println("编码后大小: " + result.getEncodedData().length + " 字节");
            
            // 验证完整性
            CRCResult verification = crcCalculator.verifyData(result.getEncodedData(), selectedType);
            System.out.println("完整性验证: " + (verification.hasError() ? "失败" : "通过"));
            
            totalCalculations++;
            
        } catch (Exception e) {
            System.out.println("文件处理出错: " + e.getMessage());
        }
    }
    
    /**
     * 教学演示模式
     */
    private static void showTeachingMode() {
        System.out.println("\n=== 教学演示模式 ===");
        System.out.println("本模式将逐步展示CRC算法的工作原理");
        
        // 使用简单的示例数据
        String demo = "AB"; // 简单的两字节数据便于理解
        byte[] data = demo.getBytes();
        
        System.out.println("\n演示数据: \"" + demo + "\"");
        System.out.println("ASCII码值: A=" + (int)'A' + ", B=" + (int)'B');
        System.out.println("二进制表示: A=" + Integer.toBinaryString('A') + ", B=" + Integer.toBinaryString('B'));
        
        // 演示不同CRC算法
        System.out.println("\n=== 不同CRC算法的计算过程 ===");
        
        for (CRCType crcType : CRCType.values()) {
            System.out.println("\n--- " + crcType.getName() + " 算法演示 ---");
            
            // 显示算法信息
            System.out.println(crcCalculator.getAlgorithmInfo(crcType));
            
            // 计算CRC
            CRCResult result = crcCalculator.calculateCRC(data, crcType);
            
            System.out.println("计算结果:");
            System.out.println(result.getBinaryRepresentation());
            System.out.println("CRC校验码: 0x" + Long.toHexString(result.getCrcValue()).toUpperCase());
            
            totalCalculations++;
            
            System.out.print("按回车继续下一个算法...");
            scanner.nextLine();
        }
    }
    
    /**
     * 自定义CRC计算
     */
    private static void customCRCCalculation() {
        System.out.println("\n=== 自定义CRC计算 ===");
        
        while (true) {
            CRCType selectedType = selectCRCType();
            crcCalculator.setCurrentCRCType(selectedType);
            
            System.out.print("请输入数据 (十六进制格式，如: 48656C6C6F，或直接输入文本): ");
            String input = scanner.nextLine();
            
            if (input.isEmpty()) {
                break;
            }
            
            byte[] data;
            
            try {
                // 判断是否为十六进制输入
                if (input.matches("[0-9A-Fa-f]+") && input.length() % 2 == 0) {
                    data = hexStringToBytes(input);
                    System.out.println("解析为十六进制数据: " + input);
                } else {
                    data = input.getBytes("UTF-8");
                    System.out.println("解析为文本数据: " + input);
                }
                
                // 计算CRC
                CRCResult result = crcCalculator.calculateCRC(data, selectedType);
                
                System.out.println("\n" + result.getDetailedInfo());
                System.out.println("原始数据 (十六进制): " + bytesToHex(data));
                System.out.println("编码数据 (十六进制): " + bytesToHex(result.getEncodedData()));
                
                totalCalculations++;
                
            } catch (Exception e) {
                System.out.println("数据处理出错: " + e.getMessage());
            }
            
            System.out.print("继续计算? (y/n): ");
            if (!scanner.nextLine().toLowerCase().startsWith("y")) {
                break;
            }
        }
    }
    
    /**
     * 显示统计信息
     */
    private static void showStatistics() {
        System.out.println("\n=== 程序运行统计 ===");
        System.out.println("总计算次数: " + totalCalculations);
        System.out.println("检测错误次数: " + errorsDetected);
        System.out.println("当前CRC类型: " + crcCalculator.getCurrentCRCType().getName());
        
        if (totalCalculations > 0) {
            double errorRate = (errorsDetected * 100.0) / totalCalculations;
            System.out.println("错误检测率: " + String.format("%.2f", errorRate) + "%");
        }
        
        // 显示支持的CRC类型
        System.out.println("\n支持的CRC类型:");
        for (CRCType type : crcCalculator.getSupportedCRCTypes()) {
            System.out.println("- " + type.getName() + " (" + type.getWidth() + "位)");
        }
    }
    
    /**
     * 选择CRC类型
     */
    private static CRCType selectCRCType() {
        System.out.println("\n请选择CRC算法类型:");
        CRCType[] types = CRCType.values();
        
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + types[i].getName() + " (" + types[i].getWidth() + "位)");
        }
        
        System.out.print("请选择 (1-" + types.length + "): ");
        int choice = getMenuChoice(1, types.length);
        
        return types[choice - 1];
    }
    
    /**
     * 辅助方法：将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
    
    /**
     * 辅助方法：将十六进制字符串转换为字节数组
     */
    private static byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
    /**
     * 辅助方法：读取文件字节
     */
    private static byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }
}