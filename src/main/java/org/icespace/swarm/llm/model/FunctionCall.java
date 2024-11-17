package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FunctionCall {
    @JsonProperty("name")
    private String name;

    @JsonProperty("arguments")
    private String arguments;
}