package crc_algorithm.test;

import crc_algorithm.core.CRCAlgorithm;
import crc_algorithm.core.CRCAlgorithm.CRCType;
import crc_algorithm.core.CRCAlgorithm.CRCResult;

/**
 * CRC校验算法测试用例类
 * 
 * 这个测试类包含了全面的CRC算法功能测试，用于验证实现的正确性。
 * 测试涵盖：
 * 1. 基本功能测试 - 各种CRC算法的正确性
 * 2. 边界值测试 - 极端情况处理
 * 3. 错误检测测试 - 验证错误检测能力
 * 4. 性能测试 - 不同数据量的处理性能
 * 5. 兼容性测试 - 与标准CRC实现对比
 * 
 * @author 网络实验项目
 * @version 1.0
 */
public class CRCTest {
    
    private static final CRCAlgorithm crcCalculator = new CRCAlgorithm();
    private static int testsPassed = 0;
    private static int testsTotal = 0;
    
    // 预定义的测试数据
    private static final String[] TEST_STRINGS = {
        "A",                           // 单字符
        "AB",                          // 双字符
        "Hello",                       // 短字符串
        "Hello, World!",               // 常见测试数据
        "1234567890",                  // 数字字符串
        "Network Protocol Test",       // 网络协议测试
        "",                           // 空字符串（边界测试）
        "The quick brown fox jumps over the lazy dog" // 长字符串
    };
    
    // 已知的标准CRC值（用于验证实现正确性）
    private static final TestVector[] KNOWN_VECTORS = {
        // 数据: "123456789"
        new TestVector("123456789", CRCType.CRC8, 0xF4L),
        new TestVector("123456789", CRCType.CRC16, 0xBB3DL),
        new TestVector("123456789", CRCType.CRC32, 0xCBF43926L),
        
        // 数据: "A"  
        new TestVector("A", CRCType.CRC8, 0x48L),
        new TestVector("A", CRCType.CRC16, 0xB915L),
        
        // 数据: "Hello"
        new TestVector("Hello", CRCType.CRC16, 0x4A17L)
    };
    
    /**
     * 测试向量数据结构
     */
    private static class TestVector {
        String data;
        CRCType crcType;
        long expectedCRC;
        
        TestVector(String data, CRCType crcType, long expectedCRC) {
            this.data = data;
            this.crcType = crcType;
            this.expectedCRC = expectedCRC;
        }
    }
    
    /**
     * 测试统计信息
     */
    private static class TestStats {
        int totalTests = 0;
        int passedTests = 0;
        long totalTime = 0;
        int errorsDetected = 0;
        int falsePositives = 0;
        
        double getPassRate() {
            return totalTests == 0 ? 0 : (passedTests * 100.0) / totalTests;
        }
        
        double getAvgTime() {
            return totalTests == 0 ? 0 : totalTime / (double) totalTests;
        }
    }
    
    /**
     * 主测试函数
     */
    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("      CRC校验算法测试程序");
        System.out.println("      Comprehensive Test Suite");
        System.out.println("=====================================\n");
        
        // 执行所有测试
        runAllTests();
        
        // 显示最终结果
        showFinalResults();
    }
    
    /**
     * 执行所有测试
     */
    private static void runAllTests() {
        System.out.println("开始执行CRC算法测试套件...\n");
        
        // 1. 基本功能测试
        System.out.println("=== 基本功能测试 ===");
        testBasicFunctionality();
        
        // 2. 标准向量测试
        System.out.println("\n=== 标准向量测试 ===");
        testKnownVectors();
        
        // 3. 边界值测试
        System.out.println("\n=== 边界值测试 ===");
        testBoundaryConditions();
        
        // 4. 错误检测测试
        System.out.println("\n=== 错误检测测试 ===");
        testErrorDetection();
        
        // 5. 性能测试
        System.out.println("\n=== 性能测试 ===");
        testPerformance();
        
        // 6. 数据完整性测试
        System.out.println("\n=== 数据完整性测试 ===");
        testDataIntegrity();
        
        // 7. 算法对比测试
        System.out.println("\n=== 算法对比测试 ===");
        testAlgorithmComparison();
    }
    
    /**
     * 基本功能测试
     */
    private static void testBasicFunctionality() {
        TestStats stats = new TestStats();
        
        for (CRCType crcType : CRCType.values()) {
            System.out.println("测试 " + crcType.getName() + " 算法:");
            
            for (String testData : TEST_STRINGS) {
                if (testData.isEmpty()) continue; // 跳过空字符串
                
                stats.totalTests++;
                long startTime = System.nanoTime();
                
                try {
                    // 测试CRC计算
                    byte[] data = testData.getBytes("UTF-8");
                    CRCResult result = crcCalculator.calculateCRC(data, crcType);
                    
                    // 验证基本属性
                    boolean passed = true;
                    
                    if (result.getCrcValue() < 0) {
                        passed = false;
                        System.out.println("  ❌ CRC值为负数: " + testData);
                    }
                    
                    if (result.getEncodedData().length != data.length + crcType.getWidth() / 8) {
                        passed = false;
                        System.out.println("  ❌ 编码数据长度错误: " + testData);
                    }
                    
                    // 测试验证功能
                    CRCResult verification = crcCalculator.verifyData(result.getEncodedData(), crcType);
                    if (verification.hasError()) {
                        passed = false;
                        System.out.println("  ❌ 验证失败: " + testData);
                    }
                    
                    if (passed) {
                        stats.passedTests++;
                        System.out.println("  ✓ " + testData + " -> 0x" + 
                                         Long.toHexString(result.getCrcValue()).toUpperCase());
                    }
                    
                } catch (Exception e) {
                    System.out.println("  ❌ 异常: " + testData + " - " + e.getMessage());
                }
                
                long endTime = System.nanoTime();
                stats.totalTime += (endTime - startTime);
            }
            System.out.println();
        }
        
        System.out.println("基本功能测试结果: " + stats.passedTests + "/" + stats.totalTests + 
                          " 通过 (" + String.format("%.1f", stats.getPassRate()) + "%)");
        
        testsTotal += stats.totalTests;
        testsPassed += stats.passedTests;
    }
    
    /**
     * 已知向量测试
     */
    private static void testKnownVectors() {
        TestStats stats = new TestStats();
        
        for (TestVector vector : KNOWN_VECTORS) {
            stats.totalTests++;
            
            try {
                byte[] data = vector.data.getBytes("UTF-8");
                CRCResult result = crcCalculator.calculateCRC(data, vector.crcType);
                
                if (result.getCrcValue() == vector.expectedCRC) {
                    stats.passedTests++;
                    System.out.println("✓ " + vector.crcType.getName() + 
                                     " \"" + vector.data + "\" -> 0x" + 
                                     Long.toHexString(vector.expectedCRC).toUpperCase());
                } else {
                    System.out.println("❌ " + vector.crcType.getName() + 
                                     " \"" + vector.data + "\" 期望: 0x" + 
                                     Long.toHexString(vector.expectedCRC).toUpperCase() +
                                     " 实际: 0x" + 
                                     Long.toHexString(result.getCrcValue()).toUpperCase());
                }
                
            } catch (Exception e) {
                System.out.println("❌ " + vector.crcType.getName() + 
                                 " \"" + vector.data + "\" 异常: " + e.getMessage());
            }
        }
        
        System.out.println("标准向量测试结果: " + stats.passedTests + "/" + stats.totalTests + 
                          " 通过 (" + String.format("%.1f", stats.getPassRate()) + "%)");
        
        testsTotal += stats.totalTests;
        testsPassed += stats.passedTests;
    }
    
    /**
     * 边界条件测试
     */
    private static void testBoundaryConditions() {
        TestStats stats = new TestStats();
        
        // 测试空数据
        stats.totalTests++;
        try {
            byte[] emptyData = new byte[0];
            crcCalculator.calculateCRC(emptyData);
            System.out.println("❌ 空数据应该抛出异常");
        } catch (IllegalArgumentException e) {
            stats.passedTests++;
            System.out.println("✓ 空数据正确抛出异常: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ 空数据抛出错误类型异常: " + e.getMessage());
        }
        
        // 测试null数据
        stats.totalTests++;
        try {
            crcCalculator.calculateCRC(null);
            System.out.println("❌ null数据应该抛出异常");
        } catch (IllegalArgumentException e) {
            stats.passedTests++;
            System.out.println("✓ null数据正确抛出异常: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ null数据抛出错误类型异常: " + e.getMessage());
        }
        
        // 测试单字节数据
        stats.totalTests++;
        try {
            byte[] singleByte = {0x42};
            CRCResult result = crcCalculator.calculateCRC(singleByte);
            stats.passedTests++;
            System.out.println("✓ 单字节数据: 0x" + 
                             Long.toHexString(result.getCrcValue()).toUpperCase());
        } catch (Exception e) {
            System.out.println("❌ 单字节数据处理失败: " + e.getMessage());
        }
        
        // 测试大数据块（性能边界）
        stats.totalTests++;
        try {
            byte[] largeData = new byte[10000];
            for (int i = 0; i < largeData.length; i++) {
                largeData[i] = (byte) (i % 256);
            }
            
            long startTime = System.currentTimeMillis();
            CRCResult result = crcCalculator.calculateCRC(largeData);
            long endTime = System.currentTimeMillis();
            
            stats.passedTests++;
            System.out.println("✓ 大数据块处理 (10KB): 0x" + 
                             Long.toHexString(result.getCrcValue()).toUpperCase() +
                             " 用时: " + (endTime - startTime) + "ms");
        } catch (Exception e) {
            System.out.println("❌ 大数据块处理失败: " + e.getMessage());
        }
        
        System.out.println("边界条件测试结果: " + stats.passedTests + "/" + stats.totalTests + 
                          " 通过 (" + String.format("%.1f", stats.getPassRate()) + "%)");
        
        testsTotal += stats.totalTests;
        testsPassed += stats.passedTests;
    }
    
    /**
     * 错误检测测试
     */
    private static void testErrorDetection() {
        TestStats stats = new TestStats();
        
        String testData = "CRC Error Detection Test";
        byte[] originalData = testData.getBytes();
        
        for (CRCType crcType : CRCType.values()) {
            System.out.println("测试 " + crcType.getName() + " 错误检测能力:");
            
            try {
                // 计算原始CRC
                CRCResult original = crcCalculator.calculateCRC(originalData, crcType);
                
                // 测试单比特错误检测
                int totalBits = original.getEncodedData().length * 8;
                int detectedErrors = 0;
                int testIterations = Math.min(totalBits, 100); // 限制测试次数
                
                for (int i = 0; i < testIterations; i++) {
                    stats.totalTests++;
                    
                    // 模拟单比特错误
                    byte[] errorData = crcCalculator.simulateError(original.getEncodedData(), i);
                    CRCResult verification = crcCalculator.verifyData(errorData, crcType);
                    
                    if (verification.hasError()) {
                        detectedErrors++;
                        stats.passedTests++;
                    } else {
                        stats.falsePositives++;
                    }
                }
                
                double detectionRate = (detectedErrors * 100.0) / testIterations;
                System.out.println("  单比特错误检测率: " + String.format("%.1f", detectionRate) + 
                                 "% (" + detectedErrors + "/" + testIterations + ")");
                
            } catch (Exception e) {
                System.out.println("  ❌ 错误检测测试失败: " + e.getMessage());
            }
        }
        
        System.out.println("错误检测测试结果: " + stats.passedTests + "/" + stats.totalTests + 
                          " 通过 (" + String.format("%.1f", stats.getPassRate()) + "%)");
        
        testsTotal += stats.totalTests;
        testsPassed += stats.passedTests;
    }
    
    /**
     * 性能测试
     */
    private static void testPerformance() {
        TestStats stats = new TestStats();
        
        int[] dataSizes = {10, 100, 1000, 10000}; // 不同数据大小
        
        for (int size : dataSizes) {
            // 生成测试数据
            byte[] testData = new byte[size];
            for (int i = 0; i < size; i++) {
                testData[i] = (byte) (i % 256);
            }
            
            System.out.println("测试数据大小: " + size + " 字节");
            
            for (CRCType crcType : CRCType.values()) {
                stats.totalTests++;
                
                try {
                    // 多次测试取平均值
                    int iterations = size < 1000 ? 1000 : 100;
                    long totalTime = 0;
                    
                    for (int i = 0; i < iterations; i++) {
                        long startTime = System.nanoTime();
                        crcCalculator.calculateCRC(testData, crcType);
                        long endTime = System.nanoTime();
                        totalTime += (endTime - startTime);
                    }
                    
                    double avgTimeMs = (totalTime / iterations) / 1000000.0;
                    double throughput = (size / avgTimeMs) * 1000.0; // 字节/秒
                    
                    System.out.println("  " + crcType.getName() + ": " + 
                                     String.format("%.3f", avgTimeMs) + "ms, " +
                                     String.format("%.1f", throughput) + " B/s");
                    
                    stats.passedTests++;
                    
                } catch (Exception e) {
                    System.out.println("  ❌ " + crcType.getName() + " 性能测试失败: " + e.getMessage());
                }
            }
            System.out.println();
        }
        
        System.out.println("性能测试结果: " + stats.passedTests + "/" + stats.totalTests + 
                          " 通过 (" + String.format("%.1f", stats.getPassRate()) + "%)");
        
        testsTotal += stats.totalTests;
        testsPassed += stats.passedTests;
    }
    
    /**
     * 数据完整性测试
     */
    private static void testDataIntegrity() {
        TestStats stats = new TestStats();
        
        String[] testCases = {
            "完整性测试数据1",
            "Integrity Test Data 2",
            "数据完整性验证测试用例3",
            "1234567890abcdefghijklmnopqrstuvwxyz"
        };
        
        for (String testCase : testCases) {
            for (CRCType crcType : CRCType.values()) {
                stats.totalTests++;
                
                try {
                    byte[] data = testCase.getBytes("UTF-8");
                    
                    // 编码
                    CRCResult encoded = crcCalculator.calculateCRC(data, crcType);
                    
                    // 验证
                    CRCResult verification = crcCalculator.verifyData(encoded.getEncodedData(), crcType);
                    
                    // 检查原始数据是否一致
                    boolean dataIntact = java.util.Arrays.equals(data, verification.getOriginalData());
                    boolean crcCorrect = !verification.hasError();
                    
                    if (dataIntact && crcCorrect) {
                        stats.passedTests++;
                    } else {
                        System.out.println("❌ " + crcType.getName() + " 数据完整性失败: \"" + testCase + "\"");
                    }
                    
                } catch (Exception e) {
                    System.out.println("❌ " + crcType.getName() + " 完整性测试异常: " + e.getMessage());
                }
            }
        }
        
        System.out.println("数据完整性测试结果: " + stats.passedTests + "/" + stats.totalTests + 
                          " 通过 (" + String.format("%.1f", stats.getPassRate()) + "%)");
        
        testsTotal += stats.totalTests;
        testsPassed += stats.passedTests;
    }
    
    /**
     * 算法对比测试
     */
    private static void testAlgorithmComparison() {
        TestStats stats = new TestStats();
        
        String testData = "Algorithm Comparison Test Data";
        byte[] data = testData.getBytes();
        
        System.out.println("使用测试数据: \"" + testData + "\"");
        System.out.println(String.format("%-8s %-12s %-12s %-10s", "算法", "CRC值", "编码长度", "计算时间"));
        System.out.println("============================================");
        
        for (CRCType crcType : CRCType.values()) {
            stats.totalTests++;
            
            try {
                long startTime = System.nanoTime();
                CRCResult result = crcCalculator.calculateCRC(data, crcType);
                long endTime = System.nanoTime();
                
                double timeMs = (endTime - startTime) / 1000000.0;
                String crcValue = "0x" + Long.toHexString(result.getCrcValue()).toUpperCase();
                int encodedLength = result.getEncodedData().length;
                
                System.out.println(String.format("%-8s %-12s %-12d %-10s", 
                    crcType.getName(), crcValue, encodedLength, 
                    String.format("%.3fms", timeMs)));
                
                stats.passedTests++;
                
            } catch (Exception e) {
                System.out.println("❌ " + crcType.getName() + " 对比测试失败: " + e.getMessage());
            }
        }
        
        System.out.println("\n算法对比测试结果: " + stats.passedTests + "/" + stats.totalTests + 
                          " 通过 (" + String.format("%.1f", stats.getPassRate()) + "%)");
        
        testsTotal += stats.totalTests;
        testsPassed += stats.passedTests;
    }
    
    /**
     * 显示最终测试结果
     */
    private static void showFinalResults() {
        System.out.println("\n=====================================");
        System.out.println("         测试结果汇总");
        System.out.println("=====================================");
        System.out.println("总测试用例: " + testsTotal);
        System.out.println("通过测试: " + testsPassed);
        System.out.println("失败测试: " + (testsTotal - testsPassed));
        System.out.println("通过率: " + String.format("%.2f", (testsPassed * 100.0) / testsTotal) + "%");
        
        if (testsPassed == testsTotal) {
            System.out.println("\n🎉 所有测试通过！CRC算法实现正确。");
        } else {
            System.out.println("\n⚠️  部分测试失败，请检查实现。");
        }
        
        System.out.println("\n测试完成时间: " + new java.util.Date());
        System.out.println("=====================================");
    }
}