package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DigitalDiaryApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateJointNoteForPremiumAuthors() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "Architecture sync",
                "content", "Shared premium note",
                "authors", List.of(1, 3),
                "visibility", "joint",
                "categoryIds", List.of(1)
        );

        mockMvc.perform(post("/notes")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("joint"))
                .andExpect(jsonPath("$.authors[0]").value(1));
    }

    @Test
    void shouldRejectJointNoteWhenFreeUserIncluded() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "Blocked collaboration",
                "content", "Shared note",
                "authors", List.of(1, 2),
                "visibility", "joint",
                "categoryIds", List.of(1)
        );

        mockMvc.perform(post("/notes")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("All joint note authors must have premium accounts"));
    }

    @Test
    void shouldFilterSearchResultsByAccessRules() throws Exception {
        mockMvc.perform(get("/notes/search")
                        .header("X-User-Id", 2)
                        .param("visibility", "private"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Weekend plans"));
    }
}
