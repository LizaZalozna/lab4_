package org.example.users.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String surname;
    private String name;

    @JsonProperty("is_premium")
    private boolean isPremium;
}
