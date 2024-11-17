# Swarm-Java

A Java implementation of [OpenAI's Swarm framework](https://github.com/openai/swarm) for exploring ergonomic, lightweight multi-agent orchestration.

🚧 **Under Active Development** 🚧
This project is currently in early development. APIs and features are subject to change, and many components are still being implemented. Feel free to star/watch the repository to stay updated on its progress.

⚠️ **Note**: This is an experimental framework intended to explore ergonomic interfaces for multi-agent systems in Java. Like the original Python implementation, it is primarily for educational purposes.

## Requirements

- Java 11 (LTS) or later
- Maven 3.6+

## Overview

Swarm-Java focuses on making agent **coordination** and **execution** lightweight, highly controllable, and easily testable in Java environments. It implements the core primitives of the original Swarm framework:

- `Agent`: Encapsulates instructions and tools
- **Handoffs**: Allows agents to transfer control to other agents
- **Multi-LLM Support**: Compatible with:
  - OpenAI API
  - Azure OpenAI
  - ChatGLM
  - Ollama

## Features

- Thread-safe context management
- Reactive streams support for response streaming
- Comprehensive error handling and retry mechanisms
- Built-in monitoring and metrics collection

## Installation

> ⚠️ Not yet available for production use. Check back soon for installation instructions.

### Maven Configuration

```xml
<dependency>
    <groupId>JasonGuoo</groupId>
    <artifactId>swarmjava</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Building from Source

```bash
# Requires Java 11 and Maven 3.6+
git clone https://github.com/JasonGuoo/swarm-java.git
cd swarm-java
mvn clean install
```

## License

[Add your license information here]

