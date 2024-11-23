package org.icespace.swarm.core;

import java.util.List;
import java.util.Map;

import org.icespace.swarm.llm.model.Message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwarmResponse {

    private List<Message> history;
    private Agent activeAgent;
    private Map<String, Object> context;

    public Message getLastMessage() {
        return history.get(history.size() - 1);
    }

}
