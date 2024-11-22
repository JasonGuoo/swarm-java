# Swarm-Java

[OpenAI's Swarm framework](https://github.com/openai/swarm) 的Java实现，用于探索人体工程学的、轻量级的多智能体编排。

SwarmJava 是一个Java库，用于构建基于LLM的应用程序，使多个AI智能体能够无缝协作。Swarm的核心是围绕注解构建的，这些注解允许你创建基于LLM的智能体并通过函数调用定义它们的交互。

## 核心概念

### 1. 智能体（Agents）和系统提示（System Prompts）
智能体是Swarm的基本构建块。每个智能体都有特定的角色和专长，由其系统提示和行为定义。例如，研究智能体专门分析科学论文，而统计智能体则专注于验证统计方法。

### 2. 函数调用（Function Calling）
SwarmJava使用强大的注解系统将Java方法作为可调用函数暴露给LLM。这个系统实现了LLM和代码之间的无缝通信：

1. **函数发现**：框架自动扫描带有`@FunctionSpec`注解的方法，告诉LLM有哪些函数可用以及它们的功能。

2. **参数映射**：方法参数上的`@Parameter`注解为LLM提供：
   - 每个参数功能的清晰描述
   - 参数类型（从Java类型自动推断）
   - 适用时的默认值

3. **动态调用**：当LLM决定使用一个函数时：
   - 它根据描述选择适当的函数
   - 以正确的格式提供所需参数
   - 框架自动验证和转换参数
   - 你的Java方法被调用并接收正确的参数

这种基于注解的方法意味着你不需要编写任何样板代码来集成LLM - 只需注解你的方法，框架就会处理其余部分。

### 3. 动态交接（Dynamic Hand-offs）
Swarm的关键特性是其动态智能体协作系统，使智能体能够无缝协作。让我们看一个客服系统的例子：

1. **初始接触**：客服经理智能体接收并分析客户请求
2. **智能路由**：根据请求类型，经理自动将任务委派给专门的智能体：
   - 销售智能体：处理产品咨询、价格问题和购买协助
   - 退款智能体：处理退货请求和管理退款程序
   - 经理智能体：处理升级和需要监督的复杂情况

例如，当收到客户询问时：
1. 经理智能体分析请求："我想退回一个有缺陷的产品"
2. 识别这是退款案例，将其交给退款智能体
3. 如果退款金额超过某些限制，退款智能体可能会升级回经理智能体
4. 在处理过程中如有产品相关问题，可能会咨询销售智能体

这创建了一个动态的、自组织的系统，其中：
- 智能体自主决定何时委托
- 每个智能体专注于其专业领域
- 复杂请求得到高效处理
- 在整个对话过程中保持上下文

Swarm的强大之处：
- **自主决策**：智能体独立决定何时使用工具、调用函数或委托任务
- **上下文感知**：内置的上下文管理确保智能体维护状态并理解其环境
- **灵活集成**：基于HTTP的简单实现确保与不同LLM提供商的兼容性
- **动态工作流**：由LLM驱动的智能体转换创建自适应任务处理

🚧 **积极开发中** 🚧
该项目目前处于早期开发阶段。API和功能可能会发生变化，许多组件仍在实现中。欢迎关注仓库以获取最新进展。

⚠️ **注意**: 这是一个实验性框架，旨在探索Java中多智能体系统的人体工程学接口。与原始Python实现一样，它主要用于教育目的。

## 要求

- Java 11 (LTS) 或更高版本
- Maven 3.6+

## 快速开始

有关如何使用SwarmJava的完整示例，请查看我们的[Weather Agent Example](examples/weather/README.md)，它演示了：
- 使用系统提示定义智能体
- 函数注解和参数验证
- 动态智能体交接
- 上下文管理和状态持久化
- 与外部服务集成

## 配置

### OpenAI
```java
LLMClient client = new OpenAIClient(apiKey)
    .setModel("gpt-4-0613")
    .setTemperature(0.7);
```

### Azure OpenAI
```java
LLMClient client = new AzureOpenAIClient(apiKey)
    .setDeploymentId("your-deployment")
    .setEndpoint("your-endpoint");
```

### ChatGLM
```java
LLMClient client = new ChatGLMClient()
    .setEndpoint("your-endpoint");
```

### Ollama
```java
LLMClient client = new OllamaClient()
    .setModel("llama2")
    .setEndpoint("http://localhost:11434");
```

## 从源代码构建

```bash
# 需要 Java 11 和 Maven 3.6+
git clone https://github.com/JasonGuoo/swarm-java.git
cd swarm-java
mvn clean install
```

## 故障排除

常见问题和解决方案：
1. API密钥错误：确保正确设置环境变量
2. 速率限制：在请求之间实现适当的延迟
3. 模型兼容性：验证模型是否支持函数调用
4. 内存使用：监控大型对话的上下文大小

## 贡献

我们欢迎贡献！请随时提交Pull Request。

## 许可证

该项目采用MIT许可证 - 详见[LICENSE](LICENSE)文件。
