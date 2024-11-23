# 天气助手示例

本示例展示如何使用 Swarm Java 框架创建一个天气助手。该助手可以查询天气状况并发送邮件通知，展示了框架的主要功能特性。

## 功能展示
- 自定义代理实现及工具函数
- 使用 `@FunctionSpec` 和 `@Parameter` 注解
- 系统提示词设计和工具选择配置
- 输入验证和错误处理
- JSON 响应格式化
- 流式和非流式响应处理
- 上下文管理和状态持久化

## 实现指南

### 1. 创建代理类

```java
public class WeatherAgent extends Agent {
    public WeatherAgent() {
        super();
    }
}
```

### 2. 定义系统提示词

系统提示词对于指导 LLM 的行为至关重要：

```java
@Override
public String getSystemPrompt(Map<String, Object> context) {
    return "你是一个有帮助的天气助手，可以：\n" +
           "1. 获取当前天气信息\n" +
           "2. 通过邮件发送天气更新\n" +
           "始终提供清晰简洁的回答。";
}
```

### 3. 配置工具选择

```java
@Override
public ToolChoice getToolChoice() {
    return ToolChoice.AUTO;  // 让 LLM 自动选择合适的工具
}
```

### 4. 实现工具函数

添加带有适当注解的函数：

```java
@FunctionSpec(description = "获取指定位置的当前天气")
public String getWeather(
    @Parameter(description = "城市和州，例如：旧金山，加利福尼亚") String location,
    Map<String, Object> context) {
    // 实现内容
}

@FunctionSpec(description = "发送天气更新邮件")
public Object sendEmail(
    @Parameter(description = "邮件接收者") String to,
    @Parameter(description = "邮件主题", defaultValue = "天气更新") String subject,
    @Parameter(description = "邮件内容") String body,
    Map<String, Object> context) {
    // 实现内容
}
```

### 5. 使用代理

基本使用模式：

```java
// 初始化框架
LLMClient client = ExampleEnvironment.createClient();
Swarm swarm = new Swarm(client);
WeatherAgent agent = new WeatherAgent();

// 创建消息
Message message = Message.builder()
    .role("user")
    .content("旧金山的天气怎么样？")
    .build();

// 运行代理
SwarmResponse response = swarm.run(
    agent,                    // 你的代理
    Arrays.asList(message),   // 消息列表
    new HashMap<>(),         // 上下文
    null,                    // 模型覆盖（可选）
    false,                   // 流式模式
    true,                    // 调试模式
    10                       // 最大对话轮次
);

// 获取响应
System.out.println(response.getLastMessage());
```

## 高级功能

### 1. 流式响应

启用流式响应以获得实时反馈：

```java
SwarmResponse response = swarm.run(
    agent,
    messages,
    context,
    null,
    true,  // 启用流式处理
    true,
    10
);
```

### 2. 上下文管理

使用上下文存储会话状态：

```java
Map<String, Object> context = new HashMap<>();
context.put("temperature_unit", "celsius");
context.put("email_signature", "此致，\n天气助手");
```

### 3. 错误处理

实现适当的验证和错误处理：

```java
if (location == null || location.trim().isEmpty()) {
    throw new IllegalArgumentException("位置不能为空");
}
```

## 最佳实践

1. **系统提示词**
   - 明确指定代理能力
   - 包含清晰的指令
   - 定义响应格式期望

2. **工具函数**
   - 使用描述性注解
   - 实现适当的验证
   - 返回结构化响应
   - 优雅处理错误

3. **响应格式化**
   - 使用一致的 JSON 结构
   - 包含所有相关信息
   - 正确处理特殊字符

4. **上下文管理**
   - 使用上下文存储会话状态
   - 实现适当的清理
   - 考虑线程安全

## 示例场景

本示例包含四个测试场景：
1. 基本天气查询
2. 天气 + 邮件组合
3. 流式天气更新
4. 错误处理演示

每个场景都展示了框架功能的不同方面。
