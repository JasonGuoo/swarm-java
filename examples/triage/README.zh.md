# Swarm Java 动态服务路由示例

本示例演示如何使用 Swarm Java 框架实现多代理系统。动态服务路由由三个专门的代理组成，它们协同工作以处理客户服务请求。

## 系统概述

动态服务路由实现了三个代理：

1. **路由代理（TriageAgent）**：将传入的请求路由到专门的代理
2. **销售代理（SalesAgent）**：处理产品咨询和购买
3. **退款代理（RefundsAgent）**：处理退款和应用折扣

## 实现指南

### 1. 创建代理

每个代理都继承基础的 `Agent` 类并实现其特定功能：

```java
public class TriageAgent extends Agent {
    @Override
    public String getSystemPrompt(Map<String, Object> context) {
        return "清晰的角色描述和可用功能...";
    }

    @FunctionSpec(description = "转接到销售代理")
    public Agent transferToSales(Map<String, Object> context) {
        return new SalesAgent();
    }
}
```

关键组件：
- `getSystemPrompt()`：定义代理的角色和能力
- `@FunctionSpec`：注解可用功能

### 2. 系统提示

系统提示对代理行为至关重要。遵循以下准则：

```java
@Override
public String getSystemPrompt(Map<String, Object> context) {
    return "你是一个客户服务分诊代理。\n" +
           "你的角色是：...";
}
```

最佳实践：
- 清晰定义代理角色
- 列出可用功能的确切名称
- 提供决策指南
- 包含任何约束或政策

### 3. 功能实现

使用适当的注解和验证实现功能：

```java
@FunctionSpec(description = "处理特定商品的退款")
public String processRefund(
        @Parameter(description = "商品ID（必须以'item_'开头）") String itemId,
        @Parameter(description = "退款原因") String reason,
        Map<String, Object> context) {
    
    if (itemId == null || !itemId.startsWith("item_")) {
        throw new IllegalArgumentException("无效的商品ID格式");
    }
    
    log.info("[MOCK] 正在处理商品 {} 的退款（原因：{}）", itemId, reason);
    return "成功处理商品 " + itemId + " 的退款";
}
```

要点：
- 使用描述性的 `@FunctionSpec` 注解
- 使用 `@Parameter` 记录参数
- 验证输入
- 使用适当的日志记录
- 返回有意义的响应

### 4. 代理通信

实现转接功能以在代理之间切换：

```java
@FunctionSpec(description = "转接到退款代理")
public Agent transferToRefunds(Map<String, Object> context) {
    return new RefundsAgent();
}
```

## 运行示例

1. 初始化框架：
```java
LLMClient client = ExampleEnvironment.createLLMClient();
Swarm swarm = new Swarm(client);
```

2. 创建初始代理：
```java
TriageAgent agent = new TriageAgent();
```

3. 创建用户消息：
```java
Message message = Message.builder()
        .role("user")
        .content("我需要退款商品item_123")
        .build();
```

4. 运行代理：
```java
SwarmResponse response = swarm.run(
        agent,
        Arrays.asList(message),
        new HashMap<>(),
        null,
        false,
        true,
        10);
```

## 示例交互

该示例演示了三种场景：

1. 销售咨询：
```
用户：我想为我的花园买一些蜜蜂
助手：让我为您转接到销售代理...
```

2. 退款请求：
```
用户：我需要退款商品item_123
助手：我会为您转接到退款代理...
```

3. 混合咨询：
```
用户：我买了蜜蜂，但是太贵了
助手：我理解您对价格有所顾虑...
```

## 实现技巧

1. **系统提示**
   - 明确代理角色
   - 列出确切的功能名称
   - 包含决策指南

2. **功能设计**
   - 使用清晰、描述性的名称
   - 验证所有输入
   - 返回有意义的响应

