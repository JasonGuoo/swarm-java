package org.icespace.swarm.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Choice {
    @JsonProperty("index")
    private Integer index;

    @JsonProperty("message")
    private Message message;

    @JsonProperty("finish_reason")
    private String finishReason;

    private JsonNode rawJson;
}