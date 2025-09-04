package crc_algorithm.core;

import java.util.HashMap;
import java.util.Map;

/**
 * CRC校验算法实现类 - 教学演示版本
 * 
 * CRC (Cyclic Redundancy Check) 循环冗余校验是一种数据错误检测方法，
 * 广泛应用于数字网络和存储设备中，用于检测原始数据的意外改变。
 * 
 * 本实现包含：
 * 1. 常用CRC标准算法 (CRC-8, CRC-16, CRC-32)
 * 2. 多项式除法运算
 * 3. 错误检测和定位
 * 4. 教学演示功能
 * 
 * @author 网络实验项目
 * @version 1.0
 */
public class CRCAlgorithm {
    
    /**
     * CRC标准类型枚举
     * 定义了常用的CRC算法标准和对应的生成多项式
     */
    public enum CRCType {
        CRC8("CRC-8", 8, 0x07, "x^8 + x^2 + x^1 + 1"),           // CRC-8标准
        CRC16("CRC-16", 16, 0x8005, "x^16 + x^15 + x^2 + 1"),    // CRC-16 IBM标准  
        CRC32("CRC-32", 32, 0x04C11DB7L, "x^32 + x^26 + x^23 + x^22 + x^16 + x^12 + x^11 + x^10 + x^8 + x^7 + x^5 + x^4 + x^2 + x^1 + 1"); // CRC-32 IEEE标准
        
        private final String name;           // CRC标准名称
        private final int width;             // CRC位宽度
        private final long polynomial;       // 生成多项式
        private final String polynomialDesc; // 多项式描述
        
        CRCType(String name, int width, long polynomial, String polynomialDesc) {
            this.name = name;
            this.width = width;
            this.polynomial = polynomial;
            this.polynomialDesc = polynomialDesc;
        }
        
        public String getName() { return name; }
        public int getWidth() { return width; }
        public long getPolynomial() { return polynomial; }
        public String getPolynomialDesc() { return polynomialDesc; }
    }
    
    /**
     * CRC计算结果类
     * 封装CRC计算的详细信息，便于教学演示
     */
    public static class CRCResult {
        private final byte[] originalData;    // 原始数据
        private final byte[] encodedData;     // 编码后数据（原始数据+CRC）
        private final long crcValue;          // CRC校验值
        private final CRCType crcType;        // CRC类型
        private final String binaryRepresentation; // 二进制表示
        private final boolean hasError;       // 是否检测到错误
        
        public CRCResult(byte[] originalData, byte[] encodedData, long crcValue, 
                        CRCType crcType, String binaryRepresentation, boolean hasError) {
            this.originalData = originalData;
            this.encodedData = encodedData;
            this.crcValue = crcValue;
            this.crcType = crcType;
            this.binaryRepresentation = binaryRepresentation;
            this.hasError = hasError;
        }
        
        // Getter方法
        public byte[] getOriginalData() { return originalData; }
        public byte[] getEncodedData() { return encodedData; }
        public long getCrcValue() { return crcValue; }
        public CRCType getCrcType() { return crcType; }
        public String getBinaryRepresentation() { return binaryRepresentation; }
        public boolean hasError() { return hasError; }
        
        /**
         * 格式化输出CRC结果信息
         */
        public String getDetailedInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== CRC计算结果详情 ===\n");
            sb.append("CRC标准: ").append(crcType.getName()).append("\n");
            sb.append("生成多项式: ").append(crcType.getPolynomialDesc()).append("\n");
            sb.append("原始数据长度: ").append(originalData.length).append(" 字节\n");
            sb.append("CRC校验码: 0x").append(Long.toHexString(crcValue).toUpperCase()).append("\n");
            sb.append("CRC位宽: ").append(crcType.getWidth()).append(" 位\n");
            sb.append("错误状态: ").append(hasError ? "检测到错误" : "数据完整").append("\n");
            sb.append("二进制表示: ").append(binaryRepresentation).append("\n");
            return sb.toString();
        }
    }
    
    private CRCType currentCrcType;  // 当前使用的CRC类型
    private Map<CRCType, Long[]> crcTables; // CRC查表法预计算表
    
    /**
     * 构造函数 - 默认使用CRC-16
     */
    public CRCAlgorithm() {
        this(CRCType.CRC16);
    }
    
    /**
     * 构造函数 - 指定CRC类型
     */
    public CRCAlgorithm(CRCType crcType) {
        this.currentCrcType = crcType;
        this.crcTables = new HashMap<>();
        // 预计算所有CRC类型的查找表
        for (CRCType type : CRCType.values()) {
            crcTables.put(type, generateCRCTable(type));
        }
    }
    
    /**
     * 生成CRC查找表 - 提高计算效率
     * 使用查表法可以将CRC计算的时间复杂度从O(n*w)降低到O(n)，其中n是数据长度，w是CRC位宽
     */
    private Long[] generateCRCTable(CRCType crcType) {
        Long[] table = new Long[256];
        long polynomial = crcType.getPolynomial();
        int width = crcType.getWidth();
        
        for (int i = 0; i < 256; i++) {
            long crc = i;
            if (width > 8) {
                crc <<= (width - 8); // 左移对齐到最高位
            }
            
            for (int j = 0; j < 8; j++) {
                if (width <= 8) {
                    // 8位CRC处理
                    if ((crc & 0x80) != 0) {
                        crc = (crc << 1) ^ polynomial;
                    } else {
                        crc <<= 1;
                    }
                    crc &= 0xFF; // 保持8位
                } else if (width <= 16) {
                    // 16位CRC处理  
                    if ((crc & 0x8000) != 0) {
                        crc = (crc << 1) ^ polynomial;
                    } else {
                        crc <<= 1;
                    }
                    crc &= 0xFFFF; // 保持16位
                } else {
                    // 32位CRC处理
                    if ((crc & 0x80000000L) != 0) {
                        crc = (crc << 1) ^ polynomial;
                    } else {
                        crc <<= 1;
                    }
                    crc &= 0xFFFFFFFFL; // 保持32位
                }
            }
            table[i] = crc;
        }
        return table;
    }
    
    /**
     * 计算数据的CRC校验码
     * @param data 输入数据
     * @param crcType CRC算法类型
     * @return CRC计算结果
     */
    public CRCResult calculateCRC(byte[] data, CRCType crcType) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("输入数据不能为空");
        }
        
        long crc = calculateCRCValue(data, crcType);
        
        // 构造编码后的数据（原始数据 + CRC校验码）
        byte[] encodedData = new byte[data.length + crcType.getWidth() / 8];
        System.arraycopy(data, 0, encodedData, 0, data.length);
        
        // 将CRC值添加到数据末尾
        int crcBytes = crcType.getWidth() / 8;
        for (int i = 0; i < crcBytes; i++) {
            encodedData[data.length + i] = (byte) (crc >>> (8 * (crcBytes - 1 - i)));
        }
        
        // 生成二进制表示用于教学演示
        String binaryRep = generateBinaryRepresentation(data, crc, crcType);
        
        return new CRCResult(data, encodedData, crc, crcType, binaryRep, false);
    }
    
    /**
     * 使用当前设置的CRC类型计算CRC
     */
    public CRCResult calculateCRC(byte[] data) {
        return calculateCRC(data, currentCrcType);
    }
    
    /**
     * 核心CRC计算方法 - 使用查表法实现
     */
    private long calculateCRCValue(byte[] data, CRCType crcType) {
        Long[] table = crcTables.get(crcType);
        long crc = 0;
        int width = crcType.getWidth();
        
        // 初始化CRC寄存器
        if (width <= 8) {
            crc = 0;
        } else if (width <= 16) {
            crc = 0;
        } else {
            crc = 0xFFFFFFFFL; // CRC-32通常初始化为全1
        }
        
        // 逐字节处理数据
        for (byte b : data) {
            int tableIndex;
            if (width <= 8) {
                tableIndex = (int) ((crc ^ (b & 0xFF)) & 0xFF);
                crc = table[tableIndex];
            } else if (width <= 16) {
                tableIndex = (int) (((crc >>> 8) ^ (b & 0xFF)) & 0xFF);
                crc = ((crc << 8) ^ table[tableIndex]) & 0xFFFF;
            } else {
                tableIndex = (int) (((crc >>> 24) ^ (b & 0xFF)) & 0xFF);
                crc = ((crc << 8) ^ table[tableIndex]) & 0xFFFFFFFFL;
            }
        }
        
        // CRC-32需要最终异或
        if (width > 16) {
            crc ^= 0xFFFFFFFFL;
        }
        
        return crc;
    }
    
    /**
     * 验证接收到的数据是否完整（包含CRC的完整数据）
     * @param receivedData 接收到的数据（原始数据+CRC）
     * @param crcType CRC算法类型
     * @return 验证结果
     */
    public CRCResult verifyData(byte[] receivedData, CRCType crcType) {
        if (receivedData == null || receivedData.length <= crcType.getWidth() / 8) {
            throw new IllegalArgumentException("接收数据长度不足");
        }
        
        int crcBytes = crcType.getWidth() / 8;
        int dataLength = receivedData.length - crcBytes;
        
        // 分离原始数据和CRC
        byte[] originalData = new byte[dataLength];
        System.arraycopy(receivedData, 0, originalData, 0, dataLength);
        
        // 提取接收到的CRC值
        long receivedCRC = 0;
        for (int i = 0; i < crcBytes; i++) {
            receivedCRC = (receivedCRC << 8) | (receivedData[dataLength + i] & 0xFF);
        }
        
        // 计算原始数据的CRC
        long calculatedCRC = calculateCRCValue(originalData, crcType);
        
        // 检查是否有错误
        boolean hasError = (receivedCRC != calculatedCRC);
        
        String binaryRep = generateBinaryRepresentation(originalData, calculatedCRC, crcType);
        
        return new CRCResult(originalData, receivedData, calculatedCRC, crcType, binaryRep, hasError);
    }
    
    /**
     * 使用当前CRC类型验证数据
     */
    public CRCResult verifyData(byte[] receivedData) {
        return verifyData(receivedData, currentCrcType);
    }
    
    /**
     * 模拟单比特错误 - 用于教学演示
     * @param data 原始数据
     * @param bitPosition 错误位置（从0开始）
     * @return 包含错误的数据
     */
    public byte[] simulateError(byte[] data, int bitPosition) {
        if (bitPosition < 0 || bitPosition >= data.length * 8) {
            throw new IllegalArgumentException("错误位置超出数据范围");
        }
        
        byte[] errorData = data.clone();
        int byteIndex = bitPosition / 8;
        int bitIndex = bitPosition % 8;
        
        // 翻转指定位
        errorData[byteIndex] ^= (1 << (7 - bitIndex));
        
        return errorData;
    }
    
    /**
     * 生成二进制表示字符串 - 用于教学演示
     */
    private String generateBinaryRepresentation(byte[] data, long crc, CRCType crcType) {
        StringBuilder sb = new StringBuilder();
        
        // 数据部分的二进制表示
        sb.append("数据位: ");
        for (byte b : data) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')).append(" ");
        }
        
        sb.append("\nCRC位: ");
        String crcBinary = Long.toBinaryString(crc);
        // 补齐前导零
        while (crcBinary.length() < crcType.getWidth()) {
            crcBinary = "0" + crcBinary;
        }
        sb.append(crcBinary);
        
        return sb.toString();
    }
    
    /**
     * 获取支持的CRC类型列表
     */
    public CRCType[] getSupportedCRCTypes() {
        return CRCType.values();
    }
    
    /**
     * 设置当前使用的CRC类型
     */
    public void setCurrentCRCType(CRCType crcType) {
        this.currentCrcType = crcType;
    }
    
    /**
     * 获取当前使用的CRC类型
     */
    public CRCType getCurrentCRCType() {
        return currentCrcType;
    }
    
    /**
     * 获取CRC算法的详细信息 - 用于教学说明
     */
    public String getAlgorithmInfo(CRCType crcType) {
        StringBuilder info = new StringBuilder();
        info.append("=== ").append(crcType.getName()).append(" 算法信息 ===\n");
        info.append("位宽: ").append(crcType.getWidth()).append(" 位\n");
        info.append("生成多项式: ").append(crcType.getPolynomialDesc()).append("\n");
        info.append("十六进制表示: 0x").append(Long.toHexString(crcType.getPolynomial()).toUpperCase()).append("\n");
        info.append("应用场景: ");
        
        switch (crcType) {
            case CRC8:
                info.append("简单的数据校验，如传感器数据传输\n");
                break;
            case CRC16:
                info.append("串行通信协议，如Modbus、USB等\n");
                break;
            case CRC32:
                info.append("以太网帧校验、文件完整性检查、ZIP压缩等\n");
                break;
        }
        
        info.append("检错能力: 可检测所有单比特错误和大部分多比特错误\n");
        
        return info.toString();
    }
}