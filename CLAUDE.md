# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java educational project for a computer networking course implementing multiple networking experiments. Each experiment is designed to be simple and well-commented for educational purposes, demonstrating core networking concepts.

## Current Project Structure

### 实验1: 停等协议模拟 (src/sliding_window_protocol/)
- **StopAndWaitProtocol.java**: Core protocol implementation with embedded Packet and NetworkSimulator classes
- **ProtocolDemo.java**: Interactive console application with menu-driven interface
- **SimpleTest.java**: Basic functionality test cases

### 实验2: TCP Socket聊天系统 (src/tcp_chat_system/)
- **core/ChatMessage.java**: 消息实体类，支持序列化和反序列化
- **core/ChatServer.java**: TCP服务器核心实现，支持多客户端并发
- **core/ChatClient.java**: TCP客户端核心实现，支持异步收发消息
- **frontend/ServerInterface.java**: 服务器管理控制台界面
- **frontend/ClientInterface.java**: 客户端聊天界面
- **test/ChatSystemTest.java**: 系统功能测试用例

## Key Features

### 停等协议模拟
- Stop-and-wait protocol with alternating sequence numbers (0,1)
- Simulated network environment with configurable packet loss and delay
- Timeout and retransmission mechanism (max 3 retries)
- Console-based demonstration interface

### TCP Socket聊天系统
- 基于Socket TCP协议的C/S架构
- 多客户端并发连接支持
- 实时消息广播功能
- 用户连接状态管理
- 消息历史记录保存
- 完整的错误处理和连接监控

## Build and Run Commands

### 停等协议模拟
```bash
# 编译停等协议实验
javac -d src/sliding_window_protocol/build src/sliding_window_protocol/core/*.java src/sliding_window_protocol/frontend/*.java src/sliding_window_protocol/test/*.java

# 运行交互演示
cd src && java -cp sliding_window_protocol/build sliding_window_protocol.frontend.ProtocolDemo

# 运行测试用例
cd src && java -cp sliding_window_protocol/build sliding_window_protocol.test.SimpleTest
```

### TCP Socket聊天系统
```bash
# 编译TCP聊天系统
javac -d src/tcp_chat_system/build src/tcp_chat_system/core/*.java src/tcp_chat_system/frontend/*.java src/tcp_chat_system/test/*.java

# 启动聊天服务器（在第一个终端窗口运行）
cd src && java -cp tcp_chat_system/build tcp_chat_system.frontend.ServerInterface

# 启动聊天客户端（可在多个终端窗口运行多个客户端）
cd src && java -cp tcp_chat_system/build tcp_chat_system.frontend.ClientInterface

# 运行功能测试
cd src && java -cp tcp_chat_system/build tcp_chat_system.test.ChatSystemTest
```

## Development Notes

- The implementation is intentionally simplified for teaching purposes
- Network simulation includes 10% default packet loss rate
- All output and comments are in Chinese for local students
- Focus on core stop-and-wait concepts rather than complex edge cases
- 使用中文描述和回答我
- 我在为计算机网络课程开发实验项目代码，这个代码需要为学习的同学们进行讲解，所以代码的结构尽量简单化，代码的注释尽量的丰富;
- 除了实现核心功能外，为每一个实验创建一个输入输出界面，不需要很复杂，能完整展示开发的内容就可以；
- 每个实验都需要编写测试用例；
- 代码结构：
1. 每个实验单独放在src下的子目录中，子目录的名字能够清晰的反映实验的内容；
2. 参考sliding_window_protocol的目录结构，将编译后的.class文件放在build下，核心实现代码放在core下，交互界面代码放在frontend下，测试用例放在test下；
- 每生成一个实验，将文档内容放在README.md下，并清楚的分割每个实验；