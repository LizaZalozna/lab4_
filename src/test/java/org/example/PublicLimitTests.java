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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicLimitTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldEnforcePublicNotesLimitForFreeUser() throws Exception {
        // Seed user 2 is free.
        for (int i = 0; i < 10; i++) {
            Map<String, Object> request = Map.of(
                    "title", "Public " + i,
                    "content", "Free user public note " + i,
                    "authors", List.of(2),
                    "visibility", "public"
            );

            mockMvc.perform(post("/notes")
                            .header("X-User-Id", 2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        Map<String, Object> blocked = Map.of(
                "title", "Public blocked",
                "content", "Should be blocked by limit",
                "authors", List.of(2),
                "visibility", "public"
        );

        mockMvc.perform(post("/notes")
                        .header("X-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blocked)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Free users can publish at most 10 public notes"));
    }
}
