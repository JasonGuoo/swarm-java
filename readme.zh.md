# Swarm-Java

[OpenAI的Swarm框架](https://github.com/openai/swarm) 的Java实现，用于探索基于大语言模型的、轻量级的多Agent平台。

SwarmJava 是一个Java库，用于构建基于LLM的应用程序，使多个AIAgent能够无缝协作。Swarm-Java通过与LLM的交互，确定需要调用的函数，并使用基于annotation的信息来动态调用Agent的函数。  
使整个系统具备完成某项特殊功能的能力，并且还通过函数调用来实现Agent的切换。即如果当前Agent没有能力进行操作，可以直接在函数调用中返回其他Agent实例，从而实现Agent的切换。  
使得整个系统具备基于LLM判别的切换到不同Agent的能力。  

## 核心概念

### 1. Agent（Agents）和系统提示（System Prompts）
Agent是Swarm的基本构建块。每个Agent都有特定的角色和专长，由其系统提示和行为定义。例如，研究Agent专门分析科学论文，而统计Agent则专注于验证统计方法。

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
Swarm的关键特性是其动态Agent协作系统，使Agent能够无缝协作。让我们看一个客服系统的例子：

1. **初始接触**：客服经理Agent接收并分析客户请求
2. **智能路由**：根据请求类型，经理自动将任务委派给专门的Agent：
   - 销售Agent：处理产品咨询、价格问题和购买协助
   - 退款Agent：处理退货请求和管理退款程序
   - 经理Agent：处理升级和需要监督的复杂情况

例如，当收到客户询问时：
1. 经理Agent分析请求："我想退回一个有缺陷的产品"
2. 识别这是退款案例，将其交给退款Agent
3. 如果退款金额超过某些限制，退款Agent可能会升级回经理Agent
4. 在处理过程中如有产品相关问题，可能会咨询销售Agent

这创建了一个动态的、自组织的系统，其中：
- Agent自主决定何时委托
- 每个Agent专注于其专业领域
- 复杂请求得到高效处理
- 在整个对话过程中保持上下文

Swarm的强大之处：
- **自主决策**：Agent独立决定何时使用工具、调用函数或委托任务
- **上下文感知**：内置的上下文管理确保Agent维护状态并理解其环境
- **灵活集成**：基于HTTP的简单实现确保与不同LLM提供商的兼容性
- **动态工作流**：由LLM驱动的Agent转换创建自适应任务处理

🚧 **积极开发中** 🚧
该项目目前处于早期开发阶段。API和功能可能会发生变化，许多组件仍在实现中。欢迎关注仓库以获取最新进展。

⚠️ **注意**: 这是一个实验性框架，旨在探索Java中多Agent系统的人体工程学接口。与原始Python实现一样，它主要用于教育目的。

## 示例

### 天气Agent示例

天气Agent展示了如何创建一个与外部API交互以提供天气信息的简单代理。它展示了：
- 基本代理实现
- 外部API集成
- 函数注解和参数

详情请参阅[天气示例](examples/weather/README.zh)。

### 动态服务路由示例

动态服务路由展示了如何实现一个用于客户服务路由的多代理系统。它展示了：
- 多代理协调
- 动态代理切换
- 跨代理上下文保持
- 基于函数的路由

系统包括：
- **路由代理（TriageAgent）**：将请求路由到专门的代理
- **销售代理（SalesAgent）**：处理产品咨询
- **退款代理（RefundsAgent）**：处理退款和折扣

详情请参阅[动态路由示例](examples/triage/README_zh.md)。

## 要求

- Java 11 (LTS) 或更高版本
- Maven 3.6+

## 快速开始

有关如何使用SwarmJava的完整示例，请查看我们的[Weather Agent Example](examples/weather/README.md)，它演示了：
- 使用系统提示定义Agent
- 函数注解和参数验证
- 动态Agent交接
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
