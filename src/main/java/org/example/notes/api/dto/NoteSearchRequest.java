package org.example.notes.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteSearchRequest {
    private String title;
    private Long authorId;
    private Long categoryId;
    private String visibility;
    private String from;
    private String to;
}
