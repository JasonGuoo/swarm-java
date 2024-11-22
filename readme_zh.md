# Swarm-Java

[OpenAI's Swarm framework](https://github.com/openai/swarm) 的Java实现，用于探索人体工程学的、轻量级的多智能体编排。

SwarmJava 是一个Java库，用于构建基于LLM的应用程序，使多个AI智能体能够无缝协作。Swarm的核心是围绕注解构建的，这些注解允许你创建基于LLM的智能体并通过函数调用定义它们的交互。

## 核心概念

### 1. 智能体（Agents）和系统提示（System Prompts）
智能体是Swarm的基本构建块。每个智能体都继承基础`Agent`类并定义其行为：
```java
public class ResearchAgent extends Agent {
    public ResearchAgent() {
        super("gpt-4", "Research paper analysis specialist");
    }

    @Override
    public String getSystemPrompt(Map<String, Object> context) {
        return "You are a research assistant specialized in scientific paper analysis...";
    }
}
```

### 2. 函数调用（Function Calling）
使用注解定义LLM可以调用的函数。这些函数会自动暴露给LLM，并进行参数验证：
```java
@FunctionSpec(
    description = "Analyze a research paper and extract key findings"
)
public PaperAnalysis analyzePaper(
    @Parameter(description = "URL or DOI of the research paper") 
    String paperIdentifier,
    
    @Parameter(
        description = "Specific aspects to focus on (e.g., methodology, results, conclusions)", 
        defaultValue = "all"
    ) 
    String aspects
) {
    // 实现代码
}
```

### 3. 动态交接（Dynamic Hand-offs）
Swarm最强大的特性是其能够根据任务需求在专门的智能体之间动态切换。这创建了一个灵活的、自组织的系统，其中智能体可以将任务委托给具有特定专长的其他智能体：

```java
@Agent(
    systemPrompt = "You are a research coordinator that delegates tasks to specialized agents..."
)
public class ResearchCoordinatorAgent {
    
    @FunctionSpec(
        description = "Analyze a research paper and coordinate with specialized agents as needed"
    )
    public Agent analyzePaper(
        @Parameter(description = "Paper analysis results") 
        PaperAnalysis analysis
    ) {
        if (analysis.requiresStatisticalReview()) {
            return new StatisticsAgent(); // 交接给统计专家
        } else if (analysis.containsMLComponents()) {
            return new MLReviewAgent();   // 交接给机器学习专家
        } else if (analysis.needsPeerReview()) {
            return new PeerReviewAgent(); // 交接给同行评审专家
        }
        return null; // 继续使用当前智能体
    }
}

@Agent(
    systemPrompt = "You are a statistics expert that reviews research methodology..."
)
public class StatisticsAgent {
    @FunctionSpec(
        description = "Review statistical methods and validate conclusions"
    )
    public StatisticalReview reviewStatistics(
        @Parameter(description = "Statistical methods used in the paper") 
        String methods,
        @Parameter(description = "Data analysis results") 
        String results
    ) {
        // 执行统计审查
    }
}
```

在这个例子中：
1. `ResearchCoordinatorAgent` 开始分析一篇论文
2. 根据内容，它可以动态地交接给专门的智能体：
   - `StatisticsAgent` 负责统计分析
   - `MLReviewAgent` 负责机器学习组件
   - `PeerReviewAgent` 负责一般同行评审
3. 每个专门的智能体可以根据需要进一步委托给其他智能体
4. LLM自动管理智能体之间的对话流程和上下文

这创建了一个动态工作流程，其中：
- 智能体自主决定何时委托
- 专业知识分布在专门的智能体中
- 复杂任务自然分解
- 在交接过程中保持上下文

Swarm的强大之处：
- **自主决策**: 智能体可以独立决定何时使用工具、调用函数或委托任务
- **上下文感知**: 内置的上下文管理确保智能体维护状态并理解其环境
- **灵活集成**: 基于HTTP的简单实现确保与不同LLM提供商的兼容性
- **动态工作流**: 由LLM驱动的智能体转换创建自适应任务处理

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
