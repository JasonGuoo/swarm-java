## swarm request and response of weather agent, 2 rounds
### round 1:
request:
```json
{
  "model": "openai/gpt-4o-mini-2024-07-18",
  "messages": [
    {
      "role": "system",
      "content": "You are a helpful agent."
    },
    {
      "role": "user",
      "content": "weather of NY?"
    }
  ],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "get_weather",
        "description": "Get the current weather in a given location. Location MUST be a city.",
        "parameters": {
          "type": "object",
          "properties": {
            "location": {
              "type": "string"
            },
            "time": {
              "type": "string"
            }
          },
          "required": ["location"]
        }
      }
    },
    {
      "type": "function",
      "function": {
        "name": "send_email",
        "description": "",
        "parameters": {
          "type": "object",
          "properties": {
            "recipient": {
              "type": "string"
            },
            "subject": {
              "type": "string"
            },
            "body": {
              "type": "string"
            }
          },
          "required": ["recipient", "subject", "body"]
        }
      }
    }
  ],
  "tool_choice": null,
  "stream": false,
  "parallel_tool_calls": true
}
```
response:

```json
{
  "content": null,
  "refusal": null,
  "role": "assistant",
  "audio": null,
  "function_call": null,
  "tool_calls": [
    {
      "id": "call_3NSxcxxit8PiEKmRnZkLBfbN",
      "type": "function",
      "index": 0,
      "function": {
        "name": "get_weather",
        "arguments": "{\"location\":\"New York\"}"
      }
    }
  ]
}
```
and the history is:
```json
[
  {
    "role": "user",
    "content": "weather of NY?"
  },
  {
    "content": null,
    "refusal": null,
    "role": "assistant",
    "audio": null,
    "function_call": null,
    "tool_calls": [
      {
        "id": "call_3NSxcxxit8PiEKmRnZkLBfbN",
        "function": {
          "arguments": "{\"location\":\"New York\"}",
          "name": "get_weather"
        },
        "type": "function",
        "index": 0
      }
    ],
    "sender": "Weather Agent"
  },
  {
    "role": "tool",
    "tool_call_id": "call_3NSxcxxit8PiEKmRnZkLBfbN",
    "tool_name": "get_weather",
    "content": {
      "location": "New York",
      "temperature": "65",
      "time": "now"
    }
  }
]
```

## round 2:
request:
```json
[
    {
        "role": "system",
        "content": "You are a helpful agent."
    },
    {
        "role": "user",
        "content": "weather of NY?"
    },
    {
        "role": "assistant",
        "sender": "Weather Agent",
        "tool_calls": [
            {
                "id": "call_3NSxcxxit8PiEKmRnZkLBfbN",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "get_weather",
                    "arguments": "{\"location\":\"New York\"}"
                }
            }
        ]
    },
    {
        "role": "tool",
        "tool_call_id": "call_3NSxcxxit8PiEKmRnZkLBfbN",
        "tool_name": "get_weather",
        "content": "{\"location\": \"New York\", \"temperature\": \"65\", \"time\": \"now\"}"
    }
]

```
response:
```json
{
    "id": "gen-1732157205-qlO3UxHGiuu3vk3lDdUF",
    "choices": [
        {
            "finish_reason": "stop",
            "index": 0,
            "logprobs": null,
            "message": {
                "content": "The current weather in New York is 65°F.",
                "refusal": "",
                "role": "assistant",
                "audio": null,
                "function_call": null,
                "tool_calls": null
            }
        }
    ],
    "created": 1732157205,
    "model": "openai/gpt-4o-mini-2024-07-18",
    "object": "chat.completion",
    "service_tier": null,
    "system_fingerprint": "fp_0ba0d124f1",
    "usage": {
        "completion_tokens": 12,
        "prompt_tokens": 130,
        "total_tokens": 142,
        "completion_tokens_details": null,
        "prompt_tokens_details": null
    },
    "provider": "OpenAI"
}

```

then the history is:
```json
[
    {
        "role": "user",
        "content": "weather of NY?"
    },
    {
        "role": "assistant",
        "sender": "Weather Agent",
        "tool_calls": [
            {
                "id": "call_3NSxcxxit8PiEKmRnZkLBfbN",
                "type": "function",
                "index": 0,
                "function": {
                    "name": "get_weather",
                    "arguments": "{\"location\":\"New York\"}"
                }
            }
        ]
    },
    {
        "role": "tool",
        "tool_call_id": "call_3NSxcxxit8PiEKmRnZkLBfbN",
        "tool_name": "get_weather",
        "content": "{\"location\": \"New York\", \"temperature\": \"65\", \"time\": \"now\"}"
    },
    {
        "role": "assistant",
        "sender": "Weather Agent",
        "content": "The current weather in New York is 65°F.",
        "refusal": "",
        "tool_calls": null
    }
]

```

## debug outputs for using OpenAI:
Starting Swarm CLI 🐝  
User: >? weather of NY?  
[2024-11-21 10:34:44] Getting chat completion for...: [{'role': 'system', 'content': 'You are a helpful agent.'}, {'role': 'user', 'content': 'weather of NY?'}]  
[2024-11-21 10:40:56] Received completion: ChatCompletionMessage(content=None, refusal=None, role='assistant', audio=None, function_call=None, tool_calls=[ChatCompletionMessageToolCall(id='call_3NSxcxxit8PiEKmRnZkLBfbN', function=Function(arguments='{"location":"New York"}', name='get_weather'), type='function', index=0)])  
[2024-11-21 10:44:40] Processing tool call: get_weather with arguments {'location': 'New York'}  
[2024-11-21 10:46:42] Getting chat completion for...: [{'role': 'system', 'content': 'You are a helpful agent.'}, {'role': 'user', 'content': 'weather of NY?'}, {'content': None, 'refusal': None, 'role': 'assistant', 'audio': None, 'function_call': None, 'tool_calls': [{'id': 'call_3NSxcxxit8PiEKmRnZkLBfbN', 'function': {'arguments': '{"location":"New York"}', 'name': 'get_weather'}, 'type': 'function', 'index': 0}], 'sender': 'Weather Agent'}, {'role': 'tool', 'tool_call_id': 'call_3NSxcxxit8PiEKmRnZkLBfbN', 'tool_name': 'get_weather', 'content': '{"location": "New York", "temperature": "65", "time": "now"}'}]  
[2024-11-21 10:48:25] Received completion: ChatCompletionMessage(content='The current weather in New York is 65°F.', refusal='', role='assistant', audio=None, function_call=None, tool_calls=None)  
[2024-11-21 10:57:55] Ending turn.  
Weather Agent: get_weather("location"= "New York")  
Weather Agent: The current weather in New York is 65°F.  
