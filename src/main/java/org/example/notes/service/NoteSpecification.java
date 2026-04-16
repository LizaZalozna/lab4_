package org.example.notes.service;

import jakarta.persistence.criteria.JoinType;
import org.example.notes.repository.model.NoteEntity;
import org.example.notes.repository.model.NoteVisibility;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class NoteSpecification {

    private NoteSpecification() {
    }

    public static Specification<NoteEntity> titleContains(String title) {
        return (root, query, cb) -> title == null || title.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<NoteEntity> hasAuthor(Long authorId) {
        return (root, query, cb) -> {
            query.distinct(true);
            return authorId == null
                    ? cb.conjunction()
                    : cb.equal(root.join("authors", JoinType.LEFT).get("id"), authorId);
        };
    }

    public static Specification<NoteEntity> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            query.distinct(true);
            return categoryId == null
                    ? cb.conjunction()
                    : cb.equal(root.join("categories", JoinType.LEFT).get("id"), categoryId);
        };
    }

    public static Specification<NoteEntity> hasVisibility(NoteVisibility visibility) {
        return (root, query, cb) -> visibility == null
                ? cb.conjunction()
                : cb.equal(root.get("visibility"), visibility);
    }

    public static Specification<NoteEntity> accessibleTo(Long requesterId) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.or(
                    cb.equal(root.get("visibility"), NoteVisibility.PUBLIC),
                    cb.equal(root.join("authors", JoinType.LEFT).get("id"), requesterId)
            );
        };
    }

    public static Specification<NoteEntity> createdFrom(Instant from) {
        return (root, query, cb) -> from == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<NoteEntity> createdTo(Instant to) {
        return (root, query, cb) -> to == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
