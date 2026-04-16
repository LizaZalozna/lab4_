package org.example.notes.service;

import org.example.categories.repository.model.CategoryEntity;
import org.example.notes.repository.model.NoteEntity;
import org.example.users.api.dto.UserResponse;
import org.example.categories.api.dto.CategoryResponse;
import org.example.notes.api.dto.PageResponse;
import org.example.notes.api.dto.NoteResponse;
import org.example.users.repository.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class ApiMapper {

    public UserResponse toUserResponse(UserEntity entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .surname(entity.getSurname())
                .name(entity.getName())
                .isPremium(entity.isPremium())
                .build();
    }

    public CategoryResponse toCategoryResponse(CategoryEntity entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public NoteResponse toNoteResponse(NoteEntity entity) {
        return NoteResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .authors(entity.getAuthors().stream()
                        .map(UserEntity::getId)
                        .sorted()
                        .toList())
                .categoryIds(entity.getCategories().stream()
                        .map(CategoryEntity::getId)
                        .sorted(Comparator.naturalOrder())
                        .toList())
                .visibility(entity.getVisibility().name().toLowerCase())
                .build();
    }

    public <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
