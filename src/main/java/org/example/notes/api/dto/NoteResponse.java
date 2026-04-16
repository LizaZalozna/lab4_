package org.example.notes.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class NoteResponse {
    private Long id;
    private String title;
    private String content;

    @JsonProperty("created_at")
    private Instant createdAt;

    private List<Long> authors;

    @JsonProperty("category_ids")
    private List<Long> categoryIds;

    private String visibility;
}
