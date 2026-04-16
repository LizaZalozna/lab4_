package org.example.notes.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NoteRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotEmpty
    private List<Long> authors;

    @NotBlank
    private String visibility;

    private List<Long> categoryIds;
}
