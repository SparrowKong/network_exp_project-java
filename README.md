# 网络实验项目

这是一个用于计算机网络课程的教学实验项目，包含多个网络协议和通信系统的实验，旨在帮助学生理解计算机网络的基本原理。

## 实验列表

### 实验1: 停等协议模拟 (Stop-and-Wait Protocol)
简化版的停等协议模拟系统，演示可靠数据传输的基本原理。

### 实验2: CRC校验算法 (Cyclic Redundancy Check)
循环冗余校验算法实现，展示数据错误检测的工作原理。

### 实验3: TCP Socket聊天系统
基于Socket TCP协议的客户端-服务器聊天系统，演示TCP网络编程核心概念。

### 实验4: UDP Socket聊天系统
基于UDP协议实现的客户端-服务端聊天系统，演示UDP网络编程和无连接通信。

---

## 实验1: 停等协议模拟

### 项目简介

停等协议是计算机网络中最基本的可靠数据传输协议之一。发送方发送一个数据包后，必须等待接收方的确认（ACK）才能发送下一个数据包。本项目通过Java实现了该协议的模拟，包括：

- 数据包的发送和接收
- ACK确认机制
- 超时重传机制
- 网络环境模拟（延迟、丢包）
- 交替序列号（0和1）

### 项目结构

```
src/sliding_window_protocol/
├── core/                          # 核心实现逻辑
│   └── StopAndWaitProtocol.java   # 停等协议主要实现
├── frontend/                      # 交互界面
│   └── ProtocolDemo.java          # 用户演示程序
├── test/                          # 测试用例  
│   └── SimpleTest.java            # 基本功能测试
└── build/                         # 编译输出目录（自动生成）
    └── *.class                    # 编译后的字节码文件
```

### 文件说明

#### 核心实现（core目录）

- **StopAndWaitProtocol.java**: 停等协议的完整实现
  - `Packet` 内部类：数据包结构定义
  - `NetworkSimulator` 内部类：网络环境模拟
  - 发送方逻辑：数据发送、等待ACK、超时重传
  - 接收方逻辑：数据接收、ACK发送

#### 交互界面（frontend目录）

- **ProtocolDemo.java**: 控制台交互程序
  - 菜单驱动的用户界面
  - 支持单个/批量消息发送
  - 网络参数调整功能
  - 统计信息显示

#### 测试用例（test目录）

- **SimpleTest.java**: 自动化功能测试
  - 基本发送接收测试
  - 网络参数变化测试
  - 统计信息验证

### 编译和运行

#### 编译项目

```bash
# 编译所有Java文件到build目录
javac -d src/sliding_window_protocol/build src/sliding_window_protocol/core/*.java src/sliding_window_protocol/frontend/*.java src/sliding_window_protocol/test/*.java
```

#### 运行演示程序

```bash
# 运行交互式演示
cd src && java -cp sliding_window_protocol/build sliding_window_protocol.frontend.ProtocolDemo
```

#### 运行测试程序

```bash
# 运行功能测试
cd src && java -cp sliding_window_protocol/build sliding_window_protocol.test.SimpleTest
```

### 功能特性

#### 协议特性
- ✅ 停等协议核心机制
- ✅ 交替序列号（0,1）
- ✅ 超时重传（最多3次）
- ✅ ACK确认机制
- ✅ 重复包检测

#### 网络模拟
- ✅ 可配置的丢包率（默认10%）
- ✅ 可配置的网络延迟（默认100ms）
- ✅ 数据包损坏模拟
- ✅ 传输统计信息

#### 用户界面
- ✅ 控制台菜单界面
- ✅ 实时传输过程显示
- ✅ 网络参数动态调整
- ✅ 预定义演示场景

### 使用示例

#### 基本使用

1. 启动演示程序
2. 选择"发送单个消息"
3. 输入要发送的文本
4. 观察协议执行过程

#### 高级测试

1. 选择"调整网络参数"
2. 设置高丢包率（如30%）
3. 发送消息观察重传过程
4. 查看统计信息分析性能

### 教学要点

本项目适合用于演示以下网络协议概念：

1. **可靠数据传输基础**
   - 确认应答机制
   - 超时重传原理
   - 序列号的作用

2. **协议状态机**
   - 发送方状态转换
   - 接收方行为模式

3. **网络环境影响**
   - 丢包对性能的影响
   - 网络延迟的处理
   - 重传开销分析

### 扩展建议

如需进一步学习，可以考虑以下扩展：

- 实现滑动窗口协议（Go-Back-N）
- 添加选择重传（Selective Repeat）
- 实现流量控制机制
- 增加网络拥塞模拟

### 技术说明

- **开发语言**: Java 8+
- **编程模式**: 面向对象设计
- **线程模型**: 多线程模拟网络异步性
- **设计原则**: 教学友好，代码简洁

---

## 实验2: CRC校验算法

### 项目简介

CRC (Cyclic Redundancy Check) 循环冗余校验是一种广泛应用于数字网络和数据存储中的错误检测方法。本实验实现了完整的CRC算法体系，包括多种标准CRC算法、数据编码与校验功能。

### 项目结构

```
src/crc_algorithm/
├── core/                      # 核心实现逻辑
│   └── CRCAlgorithm.java     # CRC算法核心实现
├── frontend/                  # 交互界面
│   └── CRCDemo.java          # CRC演示程序
├── test/                      # 测试用例
│   └── CRCTest.java          # CRC功能测试
└── build/                     # 编译输出目录（自动生成）
    └── *.class               # 编译后的字节码文件
```

### 功能特性

#### 算法支持
- ✅ CRC-8, CRC-16, CRC-32标准算法
- ✅ 查表法优化计算
- ✅ 错误检测与定位
- ✅ 教学演示界面

### 编译和运行

```bash
# 编译CRC算法模块
javac -d src/crc_algorithm/build src/crc_algorithm/core/*.java src/crc_algorithm/frontend/*.java src/crc_algorithm/test/*.java

# 运行演示程序
cd src && java -cp crc_algorithm/build crc_algorithm.frontend.CRCDemo

# 运行测试
cd src && java -cp crc_algorithm/build crc_algorithm.test.CRCTest
```

---

## 实验3: TCP Socket聊天系统

TCP Socket聊天系统是一个多用户实时聊天应用，基于Java Socket API实现，演示TCP网络编程和多线程概念。

### 项目结构

```
src/tcp_chat_system/
├── core/                           # 核心实现逻辑
│   ├── ChatMessage.java            # 消息实体类
│   ├── ChatServer.java             # TCP服务器实现
│   └── ChatClient.java             # TCP客户端实现
├── frontend/                       # 交互界面
│   ├── ServerInterface.java        # 服务器管理界面
│   └── ClientInterface.java        # 客户端聊天界面
├── test/                           # 测试用例
│   └── ChatSystemTest.java         # 系统功能测试
└── build/                          # 编译输出目录（自动生成）
    └── *.class                     # 编译后的字节码文件
```

### 功能特性

#### 核心功能
- ✅ TCP Socket多客户端并发连接
- ✅ 实时消息广播
- ✅ 用户连接状态管理
- ✅ 消息历史记录
- ✅ 线程安全通信

### 编译和运行

```bash
# 编译TCP聊天系统
javac -d src/tcp_chat_system/build src/tcp_chat_system/core/*.java src/tcp_chat_system/frontend/*.java src/tcp_chat_system/test/*.java

# 启动服务器
cd src && java -cp tcp_chat_system/build tcp_chat_system.frontend.ServerInterface

# 启动客户端（可启动多个）
cd src && java -cp tcp_chat_system/build tcp_chat_system.frontend.ClientInterface

# 运行测试
cd src && java -cp tcp_chat_system/build tcp_chat_system.test.ChatSystemTest
```

---

## 实验4: UDP Socket聊天系统

UDP聊天系统演示了基于UDP协议的客户端-服务端通信方式，展示UDP网络编程和无连接通信特点。

### 项目结构

```
src/udp_chat_system/
├── core/                          # 核心实现逻辑
│   ├── Message.java              # 消息封装类
│   ├── UDPServer.java            # UDP服务端核心实现
│   └── UDPClient.java            # UDP客户端核心实现
├── frontend/                      # 交互界面
│   ├── ServerDemo.java           # 服务端管理界面
│   └── ClientDemo.java           # 客户端聊天界面
├── test/                          # 测试用例
│   └── ChatSystemTest.java       # 自动化测试程序
└── build/                         # 编译输出目录（自动生成）
    └── *.class                   # 编译后的字节码文件
```

### 功能特性

#### 核心功能
- ✅ UDP DatagramSocket通信
- ✅ 多客户端并发处理
- ✅ 消息广播转发
- ✅ 心跳保活机制
- ✅ 自定义消息协议

### 编译和运行

```bash
# 编译UDP聊天系统
javac -d src/udp_chat_system/build src/udp_chat_system/core/*.java src/udp_chat_system/frontend/*.java src/udp_chat_system/test/*.java

# 启动服务端
cd src && java -cp udp_chat_system/build udp_chat_system.frontend.ServerDemo

# 启动客户端
cd src && java -cp udp_chat_system/build udp_chat_system.frontend.ClientDemo

# 运行测试
cd src && java -cp udp_chat_system/build udp_chat_system.test.ChatSystemTest
```

### 技术说明

- **开发语言**: Java 8+
- **网络协议**: UDP (用户数据报协议)
- **架构模式**: 客户端-服务端 (C/S)
- **并发模式**: 多线程异步处理
- **设计原则**: 教学导向，功能完整
