# 计算机网络课程实验项目

这是一个用于计算机网络课程的教学实验项目集合，包含多个网络协议和通信技术的实现与演示。

---

## 实验列表

### 实验一：停等协议模拟系统
**目录**: `src/sliding_window_protocol/`

停等协议是计算机网络中最基本的可靠数据传输协议之一。发送方发送一个数据包后，必须等待接收方的确认（ACK）才能发送下一个数据包。

### 实验二：UDP聊天系统 
**目录**: `src/udp_chat_system/`

基于UDP协议实现的客户端-服务端(C/S)架构聊天系统，演示UDP Socket编程和多客户端并发通信。

---

## 实验一：停等协议模拟系统

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

## 实验二：UDP聊天系统

### 项目简介

UDP聊天系统演示了基于UDP协议的客户端-服务端(C/S)架构通信方式。与TCP不同，UDP是无连接的传输协议，本项目通过Java Socket编程实现了：

- UDP Socket通信基础
- 客户端-服务端架构设计  
- 多客户端并发连接管理
- 消息广播和转发机制
- 心跳保活和连接管理
- 异常处理和重连机制

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

### 文件说明

#### 核心实现（core目录）

- **Message.java**: 消息封装和序列化类
  - 消息类型枚举（连接、聊天、断开、心跳等）
  - 消息序列化和反序列化
  - 消息验证和格式化方法
  - 工厂方法创建各种类型消息

- **UDPServer.java**: UDP服务端核心实现
  - DatagramSocket监听客户端连接
  - 多线程处理并发消息
  - 客户端连接状态管理
  - 消息广播和转发功能
  - 心跳检测和超时处理

- **UDPClient.java**: UDP客户端核心实现
  - DatagramSocket连接服务端
  - 异步消息接收处理
  - 自动心跳保活机制
  - 连接状态监控和重连
  - 消息处理回调接口

#### 交互界面（frontend目录）

- **ServerDemo.java**: 服务端管理控制台
  - 启动/停止服务端功能
  - 在线客户端列表查看
  - 服务端状态和统计信息
  - 配置参数显示

- **ClientDemo.java**: 客户端聊天程序
  - 连接服务端功能
  - 实时聊天界面
  - 聊天命令支持
  - 连接状态监控

#### 测试用例（test目录）

- **ChatSystemTest.java**: 完整的自动化测试
  - 消息类序列化测试
  - 服务端启动停止测试
  - 客户端连接测试
  - 多客户端通信测试
  - 异常情况处理测试

### 编译和运行

#### 编译项目

```bash
# 编译所有Java文件到build目录
javac -d src/udp_chat_system/build src/udp_chat_system/core/*.java src/udp_chat_system/frontend/*.java src/udp_chat_system/test/*.java
```

#### 启动服务端

```bash
# 启动服务端（默认端口8888）
cd src && java -cp udp_chat_system/build udp_chat_system.frontend.ServerDemo

# 或指定端口启动
cd src && java -cp udp_chat_system/build udp_chat_system.frontend.ServerDemo 9999
```

#### 启动客户端

```bash
# 启动客户端
cd src && java -cp udp_chat_system/build udp_chat_system.frontend.ClientDemo
```

#### 运行测试

```bash
# 运行自动化测试
cd src && java -cp udp_chat_system/build udp_chat_system.test.ChatSystemTest
```

### 功能特性

#### 通信协议
- ✅ UDP DatagramSocket通信
- ✅ 自定义消息协议设计
- ✅ 消息类型分类管理
- ✅ 消息序列化/反序列化
- ✅ 消息完整性验证

#### 服务端功能
- ✅ 多客户端并发处理
- ✅ 客户端连接状态管理
- ✅ 消息广播转发
- ✅ 心跳检测和超时处理
- ✅ 线程池管理
- ✅ 运行统计信息

#### 客户端功能
- ✅ 自动连接和重连
- ✅ 实时消息收发
- ✅ 心跳保活机制
- ✅ 连接状态监控
- ✅ 异步消息处理
- ✅ 命令行聊天界面

#### 用户界面
- ✅ 服务端管理控制台
- ✅ 客户端聊天界面
- ✅ 菜单驱动操作
- ✅ 实时状态显示
- ✅ 错误提示和帮助

### 使用示例

#### 基本聊天流程

1. **启动服务端**
   ```bash
   cd src && java -cp udp_chat_system/build udp_chat_system.frontend.ServerDemo
   ```
   选择"启动服务端"

2. **启动客户端**
   ```bash
   cd src && java -cp udp_chat_system/build udp_chat_system.frontend.ClientDemo
   ```
   - 选择"连接到服务端"
   - 输入用户名
   - 选择"进入聊天模式"

3. **开始聊天**
   - 直接输入消息按回车发送
   - 输入`/help`查看命令
   - 输入`/quit`退出聊天

#### 多客户端测试

1. 启动一个服务端
2. 启动多个客户端实例
3. 使用不同用户名连接
4. 在任意客户端发送消息
5. 观察消息在所有客户端显示

### 教学要点

本项目适合用于演示以下网络编程概念：

1. **UDP协议特点**
   - 无连接通信模式
   - 不可靠传输特性
   - 数据报传输方式

2. **Socket编程基础**
   - DatagramSocket使用
   - DatagramPacket数据包处理
   - 客户端-服务端架构

3. **并发编程技术**
   - 多线程消息处理
   - 线程安全的数据结构
   - 异步消息处理机制

4. **网络应用设计**
   - 应用层协议设计
   - 消息格式定义
   - 状态管理和错误处理

### 技术特色

- **教学友好**: 代码结构清晰，中文注释丰富
- **功能完整**: 包含服务端、客户端和测试用例
- **实用性强**: 可直接运行的聊天系统
- **扩展性好**: 易于添加新功能和协议

### 扩展建议

如需进一步学习，可以考虑以下扩展：

- 添加文件传输功能
- 实现私聊和群组功能
- 增加用户认证机制
- 添加消息加密功能
- 实现消息历史记录
- 开发图形用户界面

### 技术说明

- **开发语言**: Java 8+
- **网络协议**: UDP (用户数据报协议)
- **架构模式**: 客户端-服务端 (C/S)
- **并发模式**: 多线程异步处理
- **消息格式**: 自定义文本协议
- **字符编码**: UTF-8
- **设计原则**: 教学导向，功能完整