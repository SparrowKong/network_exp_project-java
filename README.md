# 网络实验项目

这是一个用于计算机网络课程的教学实验项目，包含多个网络协议和通信系统的实现，旨在帮助学生理解计算机网络的基本原理。

## 实验列表

### 实验1: 停等协议模拟
### 实验2: TCP Socket聊天系统

---

# 实验1: 停等协议模拟

这个实验实现了简化版的停等协议（Stop-and-Wait Protocol）模拟系统。

## 项目简介

停等协议是计算机网络中最基本的可靠数据传输协议之一。发送方发送一个数据包后，必须等待接收方的确认（ACK）才能发送下一个数据包。本项目通过Java实现了该协议的模拟，包括：

- 数据包的发送和接收
- ACK确认机制
- 超时重传机制
- 网络环境模拟（延迟、丢包）
- 交替序列号（0和1）

## 项目结构

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

## 文件说明

### 核心实现（core目录）

- **StopAndWaitProtocol.java**: 停等协议的完整实现
  - `Packet` 内部类：数据包结构定义
  - `NetworkSimulator` 内部类：网络环境模拟
  - 发送方逻辑：数据发送、等待ACK、超时重传
  - 接收方逻辑：数据接收、ACK发送

### 交互界面（frontend目录）

- **ProtocolDemo.java**: 控制台交互程序
  - 菜单驱动的用户界面
  - 支持单个/批量消息发送
  - 网络参数调整功能
  - 统计信息显示

### 测试用例（test目录）

- **SimpleTest.java**: 自动化功能测试
  - 基本发送接收测试
  - 网络参数变化测试
  - 统计信息验证

## 编译和运行

### 编译项目

```bash
# 编译所有Java文件到build目录
javac -d src/sliding_window_protocol/build src/sliding_window_protocol/core/*.java src/sliding_window_protocol/frontend/*.java src/sliding_window_protocol/test/*.java
```

### 运行演示程序

```bash
# 运行交互式演示
cd src && java -cp sliding_window_protocol/build sliding_window_protocol.frontend.ProtocolDemo
```

### 运行测试程序

```bash
# 运行功能测试
cd src && java -cp sliding_window_protocol/build sliding_window_protocol.test.SimpleTest
```

## 功能特性

### 协议特性
- ✅ 停等协议核心机制
- ✅ 交替序列号（0,1）
- ✅ 超时重传（最多3次）
- ✅ ACK确认机制
- ✅ 重复包检测

### 网络模拟
- ✅ 可配置的丢包率（默认10%）
- ✅ 可配置的网络延迟（默认100ms）
- ✅ 数据包损坏模拟
- ✅ 传输统计信息

### 用户界面
- ✅ 控制台菜单界面
- ✅ 实时传输过程显示
- ✅ 网络参数动态调整
- ✅ 预定义演示场景

## 使用示例

### 基本使用

1. 启动演示程序
2. 选择"发送单个消息"
3. 输入要发送的文本
4. 观察协议执行过程

### 高级测试

1. 选择"调整网络参数"
2. 设置高丢包率（如30%）
3. 发送消息观察重传过程
4. 查看统计信息分析性能

## 教学要点

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

## 扩展建议

如需进一步学习，可以考虑以下扩展：

- 实现滑动窗口协议（Go-Back-N）
- 添加选择重传（Selective Repeat）
- 实现流量控制机制
- 增加网络拥塞模拟

## 技术说明

- **开发语言**: Java 8+
- **编程模式**: 面向对象设计
- **线程模型**: 多线程模拟网络异步性
- **设计原则**: 教学友好，代码简洁

---

# 实验2: TCP Socket聊天系统

这个实验实现了基于Socket TCP协议的客户端-服务器聊天系统，演示了TCP网络编程的核心概念。

## 项目简介

TCP Socket聊天系统是一个多用户实时聊天应用，基于标准的Java Socket API实现。系统采用经典的C/S架构，支持多客户端并发连接，实现了完整的聊天室功能。主要特性包括：

- 基于TCP Socket的可靠通信
- 多客户端并发连接支持
- 实时消息广播功能
- 用户连接状态管理
- 消息历史记录功能
- 完善的错误处理机制

## 项目结构

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

## 文件说明

### 核心实现（core目录）

- **ChatMessage.java**: 聊天消息实体类
  - 消息类型枚举（用户消息、系统消息、用户进出通知）
  - 消息序列化和反序列化功能
  - 时间戳和用户信息管理
  - 格式化显示方法

- **ChatServer.java**: TCP服务器核心实现
  - ServerSocket监听客户端连接
  - 多线程并发处理客户端请求
  - 消息广播机制
  - 客户端连接状态管理
  - 消息历史记录功能

- **ChatClient.java**: TCP客户端核心实现
  - Socket连接服务器
  - 双向消息收发（独立线程）
  - 消息队列管理
  - 连接状态监控
  - 回调接口支持

### 交互界面（frontend目录）

- **ServerInterface.java**: 服务器管理控制台
  - 服务器启动/停止控制
  - 在线用户列表查看
  - 消息历史记录浏览
  - 系统消息发送功能
  - 服务器状态监控

- **ClientInterface.java**: 客户端聊天界面
  - 服务器连接管理
  - 实时聊天功能
  - 用户命令处理
  - 连接状态显示
  - 聊天记录清理

### 测试用例（test目录）

- **ChatSystemTest.java**: 自动化功能测试
  - 消息序列化测试
  - 服务器启停测试
  - 客户端连接测试
  - 消息传输测试
  - 多客户端聊天测试

## 编译和运行

### 编译项目

```bash
# 编译TCP聊天系统
javac -d src/tcp_chat_system/build src/tcp_chat_system/core/*.java src/tcp_chat_system/frontend/*.java src/tcp_chat_system/test/*.java
```

### 运行服务器

```bash
# 启动聊天服务器（在第一个终端窗口运行）
cd src && java -cp tcp_chat_system/build tcp_chat_system.frontend.ServerInterface
```

### 运行客户端

```bash
# 启动聊天客户端（可在多个终端窗口运行多个客户端）
cd src && java -cp tcp_chat_system/build tcp_chat_system.frontend.ClientInterface
```

### 运行测试

```bash
# 运行功能测试
cd src && java -cp tcp_chat_system/build tcp_chat_system.test.ChatSystemTest
```

## 功能特性

### 服务器端功能
- ✅ TCP ServerSocket多客户端并发处理
- ✅ 线程安全的用户管理
- ✅ 消息广播和历史记录
- ✅ 用户身份验证
- ✅ 优雅的连接断开处理

### 客户端功能
- ✅ TCP Socket连接管理
- ✅ 异步消息收发
- ✅ 实时聊天界面
- ✅ 连接状态监控
- ✅ 用户命令支持

### 消息系统
- ✅ 结构化消息格式
- ✅ 消息类型分类
- ✅ 序列化/反序列化
- ✅ 时间戳管理
- ✅ 中文消息支持

### 用户界面
- ✅ 服务器管理控制台
- ✅ 客户端聊天界面
- ✅ 用户命令处理
- ✅ 实时状态显示
- ✅ 错误信息提示

## 使用示例

### 基本使用步骤

1. **启动服务器**
   - 运行ServerInterface
   - 选择"启动服务器"
   - 服务器将在默认端口8888开始监听

2. **连接客户端**
   - 运行ClientInterface
   - 输入服务器地址（默认localhost）
   - 输入用户名
   - 开始聊天

3. **多用户聊天**
   - 在不同终端启动多个客户端
   - 每个客户端使用不同用户名
   - 消息会实时广播给所有在线用户

### 客户端命令

- `/help` - 显示帮助信息
- `/quit` - 退出聊天室
- `/status` - 显示连接状态
- `/clear` - 清屏
- `/username` - 显示当前用户名

## 教学要点

本项目适合用于演示以下网络编程概念：

1. **TCP Socket编程基础**
   - ServerSocket和Socket的使用
   - 输入输出流操作
   - 网络异常处理

2. **多线程网络编程**
   - 并发连接处理
   - 线程安全编程
   - 生产者-消费者模式

3. **客户端-服务器架构**
   - C/S架构设计
   - 协议设计原则
   - 状态管理

4. **实际网络应用开发**
   - 用户界面设计
   - 错误处理策略
   - 系统测试方法

## 扩展建议

如需进一步学习，可以考虑以下扩展：

- 实现私聊功能
- 添加用户认证系统
- 实现文件传输功能
- 增加聊天室管理功能
- 添加消息加密功能
- 实现GUI图形界面

## 技术说明

- **开发语言**: Java 8+
- **网络协议**: TCP Socket
- **并发模型**: 多线程
- **设计模式**: 观察者模式、生产者-消费者模式
- **设计原则**: 教学友好，代码简洁，注释丰富