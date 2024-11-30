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

## 项目状态

本项目的基础功能已经完备，目前可以：

1. 基于LLM的响应动态调用带注解的函数
2. 根据LLM的要求实现代理之间的切换
3. 已通过OpenAI API进行测试验证
4. 支持多种LLM模型：
   - OpenAI
   - Azure OpenAI
   - ChatGLM
   - Ollama

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

## 调用实况： Calling history of using GPT-4o
```json
14: 35: 17.323 [main] INFO  o.i.s.e.util.ExampleEnvironment - Using OpenAI configuration
14: 35: 17.817 [main] INFO  o.i.s.examples.triage.TriageExample - User: I'm interested in buying some bees for my garden
14: 35: 17.817 [main] INFO  org.icespace.swarm.core.Swarm - Starting Swarm execution with agent: TriageAgent, stream: false, maxTurns: 10
14: 35: 17.818 [main] DEBUG org.icespace.swarm.core.Swarm - Initialized context with 0 variables
14: 35: 17.818 [main] DEBUG org.icespace.swarm.core.Swarm - Starting turn 1/10
14: 35: 17.818 [main] DEBUG org.icespace.swarm.core.Swarm - Current history before completion: 14: 35: 17.889 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I'm interested in buying some bees for my garden"
    }
]
14: 35: 17.889 [main] DEBUG org.icespace.swarm.core.Swarm - Getting chat completion from LLM
14: 35: 17.966 [main] DEBUG org.icespace.swarm.core.Swarm - Request to LLM: {
    "messages": [
        {
            "role": "system",
            "content": "You are a customer service triage agent.\nYour role is to:\n1. Understand the user's request\n2. Route them to the appropriate specialized agent\n\nAvailable agents:\n- Sales Agent (transferToSales): For product inquiries and purchases\n- Refunds Agent (transferToRefunds): For refunds and discounts\n\nGuidelines:\n1. Always transfer to the most appropriate agent\n2. If unsure, ask clarifying questions\n3. Be polite and professional\n"
        },
        {
            "role": "user",
            "content": "I'm interested in buying some bees for my garden"
        }
    ],
    "tools": [
        {
            "type": "function",
            "function": {
                "name": "transferToSales",
                "description": "Transfer the conversation to the sales agent for product inquiries",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "transferToRefunds",
                "description": "Transfer the conversation to the refunds agent for refund and discount requests",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        }
    ],
    "tool_choice": "auto"
}
14: 35: 17.967 [main] DEBUG org.icespace.swarm.llm.OpenAIClient - Setting default model: openai/gpt-4o-mini-2024-07-18
14: 35: 21.523 [main] DEBUG org.icespace.swarm.core.Swarm - Response from LLM: {
    "id": "gen-XXXXX",
    "object": "chat.completion",
    "created": 1732948520,
    "model": "gpt-4o",
    "choices": [
        {
            "index": 0,
            "message": {
                "role": "assistant",
                "tool_calls": [
                    {
                        "id": "call_XXXXX",
                        "type": "function",
                        "index": 0,
                        "function": {
                            "name": "transferToSales",
                            "arguments": "{}"
                        }
                    }
                ],
                "refusal": null
            },
            "finish_reason": "tool_calls",
            "logprobs": null
        }
    ],
    "usage": {
        "rawJson": null,
        "prompt_tokens": 168,
        "completion_tokens": 11,
        "total_tokens": 179
    },
    "provider": "OpenAI",
    "system_fingerprint": "fp_0705bf87c0"
}
14: 35: 21.524 [main] DEBUG org.icespace.swarm.core.Swarm - Processing 1 tool calls
14: 35: 21.524 [main] DEBUG org.icespace.swarm.core.Swarm - Executing tool call: transferToSales
14: 35: 21.527 [main] DEBUG org.icespace.swarm.core.Swarm - Updated history after processing response: 14: 35: 21.535 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I'm interested in buying some bees for my garden"
    },
    {
        "role": "assistant",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "transferToSales",
                    "arguments": "{}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "org.icespace.swarm.examples.triage.SalesAgent@2e2ff723",
        "tool_name": "transferToSales",
        "tool_call_id": "call_XXXXX"
    }
]
14: 35: 21.535 [main] DEBUG org.icespace.swarm.core.Swarm - Starting turn 2/10
14: 35: 21.535 [main] DEBUG org.icespace.swarm.core.Swarm - Current history before completion: 14: 35: 21.542 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I'm interested in buying some bees for my garden"
    },
    {
        "role": "assistant",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "transferToSales",
                    "arguments": "{}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "org.icespace.swarm.examples.triage.SalesAgent@2e2ff723",
        "tool_name": "transferToSales",
        "tool_call_id": "call_XXXXX"
    }
]
14: 35: 21.543 [main] DEBUG org.icespace.swarm.core.Swarm - Getting chat completion from LLM
14: 35: 21.544 [main] DEBUG org.icespace.swarm.core.Swarm - Request to LLM: {
    "messages": [
        {
            "role": "system",
            "content": "You are an enthusiastic sales agent specializing in bee-related products.\nYour role is to:\n1. Help customers find the perfect bee products\n2. Answer product-related questions\n3. Provide pricing information\n\nGuidelines:\n1. Be enthusiastic about bees and bee products\n2. If asked about refunds or non-sales topics, use transfer_back_to_triage()\n3. Highlight the benefits and unique features of our products\n4. Be helpful and informative\n"
        },
        {
            "role": "user",
            "content": "I'm interested in buying some bees for my garden"
        },
        {
            "role": "assistant",
            "tool_calls": [
                {
                    "id": "call_XXXXX",
                    "type": "function",
                    "index": 0,
                    "function": {
                        "name": "transferToSales",
                        "arguments": "{}"
                    }
                }
            ],
            "refusal": null
        },
        {
            "role": "tool",
            "content": "org.icespace.swarm.examples.triage.SalesAgent@2e2ff723",
            "tool_name": "transferToSales",
            "tool_call_id": "call_XXXXX"
        }
    ],
    "tools": [
        {
            "type": "function",
            "function": {
                "name": "transferBackToTriage",
                "description": "Transfer back to triage agent if the query is not sales-related",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        }
    ],
    "tool_choice": "auto"
}
14: 35: 21.545 [main] DEBUG org.icespace.swarm.llm.OpenAIClient - Setting default model: openai/gpt-4o-mini-2024-07-18
14: 35: 24.635 [main] DEBUG org.icespace.swarm.core.Swarm - Response from LLM: {
    "id": "gen-XXXXX",
    "object": "chat.completion",
    "created": 1732948522,
    "model": "gpt-4o",
    "choices": [
        {
            "index": 0,
            "message": {
                "role": "assistant",
                "content": "That's fantastic! Having bees in your garden can greatly enhance pollination and support the ecosystem. While I can't help directly with purchasing bees, I recommend reaching out to local beekeeping associations or suppliers who can provide you with healthy bee colonies suitable for your garden.\n\nIf you're interested in bee-related products, like hives or bee feed, I’d love to assist you with those! Let me know what you need.",
                "refusal": ""
            },
            "finish_reason": "stop",
            "logprobs": null
        }
    ],
    "usage": {
        "rawJson": null,
        "prompt_tokens": 186,
        "completion_tokens": 84,
        "total_tokens": 270
    },
    "provider": "OpenAI",
    "system_fingerprint": "fp_0705bf87c0"
}
14: 35: 24.639 [main] DEBUG org.icespace.swarm.core.Swarm - No tool calls in response, ending turn
14: 35: 24.643 [main] INFO  org.icespace.swarm.core.Swarm - Swarm execution completed successfully after 2 turns
14: 35: 24.653 [main] INFO  o.i.s.examples.triage.TriageExample -  ===== Assistant: That's fantastic! Having bees in your garden can greatly enhance pollination and support the ecosystem. While I can't help directly with purchasing bees, I recommend reaching out to local beekeeping associations or suppliers who can provide you with healthy bee colonies suitable for your garden.

If you're interested in bee-related products, like hives or bee feed, I’d love to assist you with those! Let me know what you need.
14: 35: 24.655 [main] INFO  o.i.s.examples.triage.TriageExample - 
User: I need a refund for item_123, it was too expensive
14: 35: 24.655 [main] INFO  org.icespace.swarm.core.Swarm - Starting Swarm execution with agent: TriageAgent, stream: false, maxTurns: 10
14: 35: 24.655 [main] DEBUG org.icespace.swarm.core.Swarm - Initialized context with 0 variables
14: 35: 24.656 [main] DEBUG org.icespace.swarm.core.Swarm - Starting turn 1/10
14: 35: 24.656 [main] DEBUG org.icespace.swarm.core.Swarm - Current history before completion: 14: 35: 24.672 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I need a refund for item_123, it was too expensive"
    }
]
14: 35: 24.672 [main] DEBUG org.icespace.swarm.core.Swarm - Getting chat completion from LLM
14: 35: 24.673 [main] DEBUG org.icespace.swarm.core.Swarm - Request to LLM: {
    "messages": [
        {
            "role": "system",
            "content": "You are a customer service triage agent.\nYour role is to:\n1. Understand the user's request\n2. Route them to the appropriate specialized agent\n\nAvailable agents:\n- Sales Agent (transferToSales): For product inquiries and purchases\n- Refunds Agent (transferToRefunds): For refunds and discounts\n\nGuidelines:\n1. Always transfer to the most appropriate agent\n2. If unsure, ask clarifying questions\n3. Be polite and professional\n"
        },
        {
            "role": "user",
            "content": "I need a refund for item_123, it was too expensive"
        }
    ],
    "tools": [
        {
            "type": "function",
            "function": {
                "name": "transferToSales",
                "description": "Transfer the conversation to the sales agent for product inquiries",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "transferToRefunds",
                "description": "Transfer the conversation to the refunds agent for refund and discount requests",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        }
    ],
    "tool_choice": "auto"
}
14: 35: 24.674 [main] DEBUG org.icespace.swarm.llm.OpenAIClient - Setting default model: openai/gpt-4o-mini-2024-07-18
14: 35: 26.455 [main] DEBUG org.icespace.swarm.core.Swarm - Response from LLM: {
    "id": "gen-XXXXX",
    "object": "chat.completion",
    "created": 1732948526,
    "model": "gpt-4o",
    "choices": [
        {
            "index": 0,
            "message": {
                "role": "assistant",
                "tool_calls": [
                    {
                        "id": "call_XXXXX",
                        "type": "function",
                        "index": 0,
                        "function": {
                            "name": "transferToRefunds",
                            "arguments": "{}"
                        }
                    }
                ],
                "refusal": null
            },
            "finish_reason": "tool_calls",
            "logprobs": null
        }
    ],
    "usage": {
        "rawJson": null,
        "prompt_tokens": 172,
        "completion_tokens": 12,
        "total_tokens": 184
    },
    "provider": "OpenAI",
    "system_fingerprint": "fp_0705bf87c0"
}
14: 35: 26.455 [main] DEBUG org.icespace.swarm.core.Swarm - Processing 1 tool calls
14: 35: 26.456 [main] DEBUG org.icespace.swarm.core.Swarm - Executing tool call: transferToRefunds
14: 35: 26.461 [main] DEBUG org.icespace.swarm.core.Swarm - Updated history after processing response: 14: 35: 26.477 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I need a refund for item_123, it was too expensive"
    },
    {
        "role": "assistant",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "transferToRefunds",
                    "arguments": "{}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "org.icespace.swarm.examples.triage.RefundsAgent@22875539",
        "tool_name": "transferToRefunds",
        "tool_call_id": "call_XXXXX"
    }
]
14: 35: 26.477 [main] DEBUG org.icespace.swarm.core.Swarm - Starting turn 2/10
14: 35: 26.477 [main] DEBUG org.icespace.swarm.core.Swarm - Current history before completion: 14: 35: 26.480 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I need a refund for item_123, it was too expensive"
    },
    {
        "role": "assistant",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "transferToRefunds",
                    "arguments": "{}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "org.icespace.swarm.examples.triage.RefundsAgent@22875539",
        "tool_name": "transferToRefunds",
        "tool_call_id": "call_XXXXX"
    }
]
14: 35: 26.480 [main] DEBUG org.icespace.swarm.core.Swarm - Getting chat completion from LLM
14: 35: 26.489 [main] DEBUG org.icespace.swarm.core.Swarm - Request to LLM: {
    "messages": [
        {
            "role": "system",
            "content": "You are a helpful refunds agent.\nYour role is to:\n1. Process refund requests\n2. Apply discounts when appropriate\n3. Handle customer concerns about pricing\n\nGuidelines:\n1. For expensive items, offer a discount first\n2. Process refunds only if the customer insists\n3. Always get item_id for refunds (format: item_...)\n4. Use transfer_back_to_triage() for non-refund queries\n"
        },
        {
            "role": "user",
            "content": "I need a refund for item_123, it was too expensive"
        },
        {
            "role": "assistant",
            "tool_calls": [
                {
                    "id": "call_XXXXX",
                    "type": "function",
                    "index": 0,
                    "function": {
                        "name": "transferToRefunds",
                        "arguments": "{}"
                    }
                }
            ],
            "refusal": null
        },
        {
            "role": "tool",
            "content": "org.icespace.swarm.examples.triage.RefundsAgent@22875539",
            "tool_name": "transferToRefunds",
            "tool_call_id": "call_XXXXX"
        }
    ],
    "tools": [
        {
            "type": "function",
            "function": {
                "name": "transferBackToTriage",
                "description": "Transfer back to triage agent if the query is not refund-related",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "processRefund",
                "description": "Process a refund for a specific item",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "itemId": {
                            "description": "The ID of the item to refund (must start with 'item_')",
                            "type": "string"
                        },
                        "reason": {
                            "default": "NOT SPECIFIED",
                            "description": "Reason for the refund",
                            "type": "string"
                        }
                    },
                    "required": [
                        "itemId"
                    ]
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "applyDiscount",
                "description": "Apply a discount to the user's cart",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        }
    ],
    "tool_choice": "auto"
}
14: 35: 26.490 [main] DEBUG org.icespace.swarm.llm.OpenAIClient - Setting default model: openai/gpt-4o-mini-2024-07-18
14: 35: 28.919 [main] DEBUG org.icespace.swarm.core.Swarm - Response from LLM: {
    "id": "gen-XXXXX",
    "object": "chat.completion",
    "created": 1732948527,
    "model": "gpt-4o",
    "choices": [
        {
            "index": 0,
            "message": {
                "role": "assistant",
                "tool_calls": [
                    {
                        "id": "call_XXXXX",
                        "type": "function",
                        "index": 0,
                        "function": {
                            "name": "applyDiscount",
                            "arguments": "{}"
                        }
                    },
                    {
                        "id": "call_XXXXX",
                        "type": "function",
                        "index": 1,
                        "function": {
                            "name": "processRefund",
                            "arguments": "{\"itemId\": \"item_123\", \"reason\": \"Too expensive\"}"
                        }
                    }
                ],
                "refusal": null
            },
            "finish_reason": "tool_calls",
            "logprobs": null
        }
    ],
    "usage": {
        "rawJson": null,
        "prompt_tokens": 259,
        "completion_tokens": 49,
        "total_tokens": 308
    },
    "provider": "OpenAI",
    "system_fingerprint": "fp_0705bf87c0"
}
14: 35: 28.921 [main] DEBUG org.icespace.swarm.core.Swarm - Processing 2 tool calls
14: 35: 28.921 [main] DEBUG org.icespace.swarm.core.Swarm - Executing tool call: applyDiscount
14: 35: 28.923 [main] INFO  o.i.s.examples.triage.RefundsAgent - [MOCK] Applying standard discount
14: 35: 28.924 [main] DEBUG org.icespace.swarm.core.Swarm - Executing tool call: processRefund
14: 35: 28.936 [main] INFO  o.i.s.examples.triage.RefundsAgent - [MOCK] Processing refund for item_123 (Reason: Too expensive)
14: 35: 28.937 [main] DEBUG org.icespace.swarm.core.Swarm - Updated history after processing response: 14: 35: 28.953 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I need a refund for item_123, it was too expensive"
    },
    {
        "role": "assistant",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "transferToRefunds",
                    "arguments": "{}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "org.icespace.swarm.examples.triage.RefundsAgent@22875539",
        "tool_name": "transferToRefunds",
        "tool_call_id": "call_XXXXX"
    },
    {
        "role": "assistant",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "applyDiscount",
                    "arguments": "{}"
                }
            },
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 1,
                "function": {
                    "name": "processRefund",
                    "arguments": "{\"itemId\": \"item_123\", \"reason\": \"Too expensive\"}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "Applied 11% discount to your cart",
        "tool_name": "applyDiscount",
        "tool_call_id": "call_XXXXX"
    },
    {
        "role": "tool",
        "content": "Refund processed successfully for item_123",
        "tool_name": "processRefund",
        "tool_call_id": "call_XXXXX"
    }
]
14: 35: 28.953 [main] DEBUG org.icespace.swarm.core.Swarm - Starting turn 3/10
14: 35: 28.953 [main] DEBUG org.icespace.swarm.core.Swarm - Current history before completion: 14: 35: 28.960 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I need a refund for item_123, it was too expensive"
    },
    {
        "role": "assistant",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "transferToRefunds",
                    "arguments": "{}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "org.icespace.swarm.examples.triage.RefundsAgent@22875539",
        "tool_name": "transferToRefunds",
        "tool_call_id": "call_XXXXX"
    },
    {
        "role": "assistant",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "applyDiscount",
                    "arguments": "{}"
                }
            },
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 1,
                "function": {
                    "name": "processRefund",
                    "arguments": "{\"itemId\": \"item_123\", \"reason\": \"Too expensive\"}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "Applied 11% discount to your cart",
        "tool_name": "applyDiscount",
        "tool_call_id": "call_XXXXX"
    },
    {
        "role": "tool",
        "content": "Refund processed successfully for item_123",
        "tool_name": "processRefund",
        "tool_call_id": "call_XXXXX"
    }
]
14: 35: 28.960 [main] DEBUG org.icespace.swarm.core.Swarm - Getting chat completion from LLM
14: 35: 28.961 [main] DEBUG org.icespace.swarm.core.Swarm - Request to LLM: {
    "messages": [
        {
            "role": "system",
            "content": "You are a helpful refunds agent.\nYour role is to:\n1. Process refund requests\n2. Apply discounts when appropriate\n3. Handle customer concerns about pricing\n\nGuidelines:\n1. For expensive items, offer a discount first\n2. Process refunds only if the customer insists\n3. Always get item_id for refunds (format: item_...)\n4. Use transfer_back_to_triage() for non-refund queries\n"
        },
        {
            "role": "user",
            "content": "I need a refund for item_123, it was too expensive"
        },
        {
            "role": "assistant",
            "tool_calls": [
                {
                    "id": "call_XXXXX",
                    "type": "function",
                    "index": 0,
                    "function": {
                        "name": "transferToRefunds",
                        "arguments": "{}"
                    }
                }
            ],
            "refusal": null
        },
        {
            "role": "tool",
            "content": "org.icespace.swarm.examples.triage.RefundsAgent@22875539",
            "tool_name": "transferToRefunds",
            "tool_call_id": "call_XXXXX"
        },
        {
            "role": "assistant",
            "tool_calls": [
                {
                    "id": "call_XXXXX",
                    "type": "function",
                    "index": 0,
                    "function": {
                        "name": "applyDiscount",
                        "arguments": "{}"
                    }
                },
                {
                    "id": "call_XXXXX",
                    "type": "function",
                    "index": 1,
                    "function": {
                        "name": "processRefund",
                        "arguments": "{\"itemId\": \"item_123\", \"reason\": \"Too expensive\"}"
                    }
                }
            ],
            "refusal": null
        },
        {
            "role": "tool",
            "content": "Applied 11% discount to your cart",
            "tool_name": "applyDiscount",
            "tool_call_id": "call_XXXXX"
        },
        {
            "role": "tool",
            "content": "Refund processed successfully for item_123",
            "tool_name": "processRefund",
            "tool_call_id": "call_XXXXX"
        }
    ],
    "tools": [
        {
            "type": "function",
            "function": {
                "name": "transferBackToTriage",
                "description": "Transfer back to triage agent if the query is not refund-related",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "processRefund",
                "description": "Process a refund for a specific item",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "itemId": {
                            "description": "The ID of the item to refund (must start with 'item_')",
                            "type": "string"
                        },
                        "reason": {
                            "default": "NOT SPECIFIED",
                            "description": "Reason for the refund",
                            "type": "string"
                        }
                    },
                    "required": [
                        "itemId"
                    ]
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "applyDiscount",
                "description": "Apply a discount to the user's cart",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        }
    ],
    "tool_choice": "auto"
}
14: 35: 28.961 [main] DEBUG org.icespace.swarm.llm.OpenAIClient - Setting default model: openai/gpt-4o-mini-2024-07-18
14: 35: 31.653 [main] DEBUG org.icespace.swarm.core.Swarm - Response from LLM: {
    "id": "gen-XXXXX",
    "object": "chat.completion",
    "created": 1732948530,
    "model": "gpt-4o",
    "choices": [
        {
            "index": 0,
            "message": {
                "role": "assistant",
                "content": "I've processed a refund for item_123, and it has been successfully completed. Additionally, I've applied an 11% discount to your cart. If you have any further concerns, feel free to let me know!",
                "refusal": ""
            },
            "finish_reason": "stop",
            "logprobs": null
        }
    ],
    "usage": {
        "rawJson": null,
        "prompt_tokens": 337,
        "completion_tokens": 44,
        "total_tokens": 381
    },
    "provider": "OpenAI",
    "system_fingerprint": "fp_0705bf87c0"
}
14: 35: 31.654 [main] DEBUG org.icespace.swarm.core.Swarm - No tool calls in response, ending turn
14: 35: 31.654 [main] INFO  org.icespace.swarm.core.Swarm - Swarm execution completed successfully after 3 turns
14: 35: 31.654 [main] INFO  o.i.s.examples.triage.TriageExample - ======= Assistant: I've processed a refund for item_123, and it has been successfully completed. Additionally, I've applied an 11% discount to your cart. If you have any further concerns, feel free to let me know!
14: 35: 31.654 [main] INFO  o.i.s.examples.triage.TriageExample - 
User: I bought some bees but they're too expensive. What other options do I have?
14: 35: 31.654 [main] INFO  org.icespace.swarm.core.Swarm - Starting Swarm execution with agent: TriageAgent, stream: false, maxTurns: 10
14: 35: 31.654 [main] DEBUG org.icespace.swarm.core.Swarm - Initialized context with 0 variables
14: 35: 31.654 [main] DEBUG org.icespace.swarm.core.Swarm - Starting turn 1/10
14: 35: 31.655 [main] DEBUG org.icespace.swarm.core.Swarm - Current history before completion: 14: 35: 31.658 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I bought some bees but they're too expensive. What other options do I have?"
    }
]
14: 35: 31.661 [main] DEBUG org.icespace.swarm.core.Swarm - Getting chat completion from LLM
14: 35: 31.662 [main] DEBUG org.icespace.swarm.core.Swarm - Request to LLM: {
    "messages": [
        {
            "role": "system",
            "content": "You are a customer service triage agent.\nYour role is to:\n1. Understand the user's request\n2. Route them to the appropriate specialized agent\n\nAvailable agents:\n- Sales Agent (transferToSales): For product inquiries and purchases\n- Refunds Agent (transferToRefunds): For refunds and discounts\n\nGuidelines:\n1. Always transfer to the most appropriate agent\n2. If unsure, ask clarifying questions\n3. Be polite and professional\n"
        },
        {
            "role": "user",
            "content": "I bought some bees but they're too expensive. What other options do I have?"
        }
    ],
    "tools": [
        {
            "type": "function",
            "function": {
                "name": "transferToSales",
                "description": "Transfer the conversation to the sales agent for product inquiries",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "transferToRefunds",
                "description": "Transfer the conversation to the refunds agent for refund and discount requests",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        }
    ],
    "tool_choice": "auto"
}
14: 35: 31.663 [main] DEBUG org.icespace.swarm.llm.OpenAIClient - Setting default model: openai/gpt-4o-mini-2024-07-18
14: 35: 34.403 [main] DEBUG org.icespace.swarm.core.Swarm - Response from LLM: {
    "id": "gen-XXXXX",
    "object": "chat.completion",
    "created": 1732948533,
    "model": "gpt-4o",
    "choices": [
        {
            "index": 0,
            "message": {
                "role": "assistant",
                "content": "It sounds like you may be looking for information on discounts or alternatives for your purchase of bees. I will transfer you to our Refunds Agent who can assist you with this matter. \n\nPlease hold on a moment.",
                "tool_calls": [
                    {
                        "id": "call_XXXXX",
                        "type": "function",
                        "index": 0,
                        "function": {
                            "name": "transferToRefunds",
                            "arguments": "{}"
                        }
                    }
                ],
                "refusal": null
            },
            "finish_reason": "tool_calls",
            "logprobs": null
        }
    ],
    "usage": {
        "rawJson": null,
        "prompt_tokens": 175,
        "completion_tokens": 56,
        "total_tokens": 231
    },
    "provider": "OpenAI",
    "system_fingerprint": "fp_0705bf87c0"
}
14: 35: 34.411 [main] DEBUG org.icespace.swarm.core.Swarm - Processing 1 tool calls
14: 35: 34.412 [main] DEBUG org.icespace.swarm.core.Swarm - Executing tool call: transferToRefunds
14: 35: 34.419 [main] DEBUG org.icespace.swarm.core.Swarm - Updated history after processing response: 14: 35: 34.454 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I bought some bees but they're too expensive. What other options do I have?"
    },
    {
        "role": "assistant",
        "content": "It sounds like you may be looking for information on discounts or alternatives for your purchase of bees. I will transfer you to our Refunds Agent who can assist you with this matter. \n\nPlease hold on a moment.",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "transferToRefunds",
                    "arguments": "{}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "org.icespace.swarm.examples.triage.RefundsAgent@76884e4b",
        "tool_name": "transferToRefunds",
        "tool_call_id": "call_XXXXX"
    }
]
14: 35: 34.455 [main] DEBUG org.icespace.swarm.core.Swarm - Starting turn 2/10
14: 35: 34.455 [main] DEBUG org.icespace.swarm.core.Swarm - Current history before completion: 14: 35: 34.460 [main] DEBUG org.icespace.swarm.core.Swarm - 
Conversation History: [
    {
        "role": "user",
        "content": "I bought some bees but they're too expensive. What other options do I have?"
    },
    {
        "role": "assistant",
        "content": "It sounds like you may be looking for information on discounts or alternatives for your purchase of bees. I will transfer you to our Refunds Agent who can assist you with this matter. \n\nPlease hold on a moment.",
        "tool_calls": [
            {
                "id": "call_XXXXX",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "transferToRefunds",
                    "arguments": "{}"
                }
            }
        ],
        "refusal": null
    },
    {
        "role": "tool",
        "content": "org.icespace.swarm.examples.triage.RefundsAgent@76884e4b",
        "tool_name": "transferToRefunds",
        "tool_call_id": "call_XXXXX"
    }
]
14: 35: 34.460 [main] DEBUG org.icespace.swarm.core.Swarm - Getting chat completion from LLM
14: 35: 34.463 [main] DEBUG org.icespace.swarm.core.Swarm - Request to LLM: {
    "messages": [
        {
            "role": "system",
            "content": "You are a helpful refunds agent.\nYour role is to:\n1. Process refund requests\n2. Apply discounts when appropriate\n3. Handle customer concerns about pricing\n\nGuidelines:\n1. For expensive items, offer a discount first\n2. Process refunds only if the customer insists\n3. Always get item_id for refunds (format: item_...)\n4. Use transfer_back_to_triage() for non-refund queries\n"
        },
        {
            "role": "user",
            "content": "I bought some bees but they're too expensive. What other options do I have?"
        },
        {
            "role": "assistant",
            "content": "It sounds like you may be looking for information on discounts or alternatives for your purchase of bees. I will transfer you to our Refunds Agent who can assist you with this matter. \n\nPlease hold on a moment.",
            "tool_calls": [
                {
                    "id": "call_XXXXX",
                    "type": "function",
                    "index": 0,
                    "function": {
                        "name": "transferToRefunds",
                        "arguments": "{}"
                    }
                }
            ],
            "refusal": null
        },
        {
            "role": "tool",
            "content": "org.icespace.swarm.examples.triage.RefundsAgent@76884e4b",
            "tool_name": "transferToRefunds",
            "tool_call_id": "call_XXXXX"
        }
    ],
    "tools": [
        {
            "type": "function",
            "function": {
                "name": "transferBackToTriage",
                "description": "Transfer back to triage agent if the query is not refund-related",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "processRefund",
                "description": "Process a refund for a specific item",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "itemId": {
                            "description": "The ID of the item to refund (must start with 'item_')",
                            "type": "string"
                        },
                        "reason": {
                            "default": "NOT SPECIFIED",
                            "description": "Reason for the refund",
                            "type": "string"
                        }
                    },
                    "required": [
                        "itemId"
                    ]
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "applyDiscount",
                "description": "Apply a discount to the user's cart",
                "parameters": {
                    "type": "object",
                    "properties": {}
                }
            }
        }
    ],
    "tool_choice": "auto"
}
14: 35: 34.463 [main] DEBUG org.icespace.swarm.llm.OpenAIClient - Setting default model: openai/gpt-4o-mini-2024-07-18
14: 35: 36.703 [main] DEBUG org.icespace.swarm.core.Swarm - Response from LLM: {
    "id": "gen-XXXXX",
    "object": "chat.completion",
    "created": 1732948535,
    "model": "gpt-4o",
    "choices": [
        {
            "index": 0,
            "message": {
                "role": "assistant",
                "content": "I was unable to transfer you as the query does not relate to refunds. If you would like, I can assist you with other inquiries or provide assistance in different areas. Please let me know!",
                "refusal": ""
            },
            "finish_reason": "stop",
            "logprobs": null
        }
    ],
    "usage": {
        "rawJson": null,
        "prompt_tokens": 311,
        "completion_tokens": 40,
        "total_tokens": 351
    },
    "provider": "OpenAI",
    "system_fingerprint": "fp_0705bf87c0"
}
14: 35: 36.704 [main] DEBUG org.icespace.swarm.core.Swarm - No tool calls in response, ending turn
14: 35: 36.705 [main] INFO  org.icespace.swarm.core.Swarm - Swarm execution completed successfully after 2 turns
14: 35: 36.705 [main] INFO  o.i.s.examples.triage.TriageExample - ======== Assistant: I was unable to transfer you as the query does not relate to refunds. If you would like, I can assist you with other inquiries or provide assistance in different areas. Please let me know!

Process finished with exit code 0
```
