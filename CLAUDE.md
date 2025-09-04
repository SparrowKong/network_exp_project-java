# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java educational project for a computer networking course implementing a simplified stop-and-wait protocol simulation. The project is designed to be simple and well-commented for educational purposes, using only 2-3 Java files for easy understanding.

## Current Project Structure

The project uses a simplified structure in `src/sliding_window_protocol/`:
- **StopAndWaitProtocol.java**: Core protocol implementation with embedded Packet and NetworkSimulator classes
- **ProtocolDemo.java**: Interactive console application with menu-driven interface
- **SimpleTest.java**: Basic functionality test cases

## Key Features

- Stop-and-wait protocol with alternating sequence numbers (0,1)
- Simulated network environment with configurable packet loss and delay
- Timeout and retransmission mechanism (max 3 retries)
- Console-based demonstration interface
- Comprehensive Chinese comments for educational purposes

## Build and Run Commands

```bash
# Compile all Java files to build directory
javac -d src/sliding_window_protocol/build src/sliding_window_protocol/core/*.java src/sliding_window_protocol/frontend/*.java src/sliding_window_protocol/test/*.java

# Run interactive demo
cd src && java -cp sliding_window_protocol/build sliding_window_protocol.frontend.ProtocolDemo

# Run simple test
cd src && java -cp sliding_window_protocol/build sliding_window_protocol.test.SimpleTest
```

## Development Notes

- The implementation is intentionally simplified for teaching purposes
- Network simulation includes 10% default packet loss rate
- All output and comments are in Chinese for local students
- Focus on core stop-and-wait concepts rather than complex edge cases
- 使用中文描述和回答我