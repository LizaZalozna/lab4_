package org.example.notes.service;

import org.example.notes.service.exception.AccessDeniedException;
import org.example.notes.service.exception.BadRequestException;
import org.example.notes.service.exception.BusinessRuleException;
import org.example.notes.service.exception.NotFoundException;
import org.example.notes.repository.model.NoteEntity;
import org.example.notes.repository.model.NoteVisibility;
import org.example.notes.repository.NoteRepository;
import org.example.notes.api.dto.NoteRequest;
import org.example.notes.api.dto.NoteSearchRequest;
import org.example.users.repository.model.UserEntity;
import org.example.users.repository.UserRepository;
import org.example.users.service.AuthContextService;
import org.example.categories.repository.CategoryRepository;
import org.example.categories.repository.model.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NoteService {

    private static final long FREE_PUBLIC_NOTES_LIMIT = 10L;

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuthContextService authContextService;

    public NoteService(
            NoteRepository noteRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            AuthContextService authContextService
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public NoteEntity create(Long requesterId, NoteRequest request) {
        UserEntity requester = authContextService.requireUser(requesterId);
        Set<UserEntity> authors = resolveAuthors(request.getAuthors(), requester);
        Set<CategoryEntity> categories = resolveCategories(request.getCategoryIds());
        NoteVisibility visibility = parseVisibility(request.getVisibility());

        validateJointPremium(visibility, requester, authors);
        validatePublicLimit(visibility, requester, null);

        NoteEntity note = NoteEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .visibility(visibility)
                .authors(authors)
                .categories(categories)
                .build();

        return noteRepository.save(note);
    }

    public NoteEntity getById(Long requesterId, Long noteId) {
        UserEntity requester = authContextService.requireUser(requesterId);
        NoteEntity note = findNote(noteId);
        ensureCanRead(note, requester);
        return note;
    }

    @Transactional
    public NoteEntity update(Long requesterId, Long noteId, NoteRequest request) {
        UserEntity requester = authContextService.requireUser(requesterId);
        NoteEntity note = findNote(noteId);
        ensureCanEdit(note, requester);

        Set<UserEntity> authors = resolveAuthors(request.getAuthors(), requester);
        Set<CategoryEntity> categories = resolveCategories(request.getCategoryIds());
        NoteVisibility visibility = parseVisibility(request.getVisibility());

        validateJointPremium(visibility, requester, authors);
        validatePublicLimit(visibility, requester, note);

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setVisibility(visibility);
        note.setAuthors(authors);
        note.setCategories(categories);

        return noteRepository.save(note);
    }

    @Transactional
    public void delete(Long requesterId, Long noteId) {
        UserEntity requester = authContextService.requireUser(requesterId);
        NoteEntity note = findNote(noteId);
        ensureCanEdit(note, requester);
        noteRepository.deleteById(noteId);
    }

    @Transactional
    public NoteEntity addCategory(Long requesterId, Long noteId, Long categoryId) {
        UserEntity requester = authContextService.requireUser(requesterId);
        NoteEntity note = findNote(noteId);
        ensureCanEdit(note, requester);
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " not found"));
        note.getCategories().add(category);
        return noteRepository.save(note);
    }

    public Page<NoteEntity> search(Long requesterId, NoteSearchRequest request, Pageable pageable) {
        UserEntity requester = authContextService.requireUser(requesterId);
        Specification<NoteEntity> specification = Specification
                .where(NoteSpecification.accessibleTo(requester.getId()))
                .and(NoteSpecification.titleContains(request.getTitle()))
                .and(NoteSpecification.hasAuthor(request.getAuthorId()))
                .and(NoteSpecification.hasCategory(request.getCategoryId()))
                .and(NoteSpecification.hasVisibility(parseVisibilityNullable(request.getVisibility())))
                .and(NoteSpecification.createdFrom(parseInstant(request.getFrom())))
                .and(NoteSpecification.createdTo(parseInstant(request.getTo())));

        return noteRepository.findAll(specification, pageable);
    }

    private NoteEntity findNote(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Note with id " + noteId + " not found"));
    }

    private Set<UserEntity> resolveAuthors(List<Long> authorIds, UserEntity requester) {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new BadRequestException("authors must contain at least one user id");
        }

        Set<Long> normalizedIds = new HashSet<>(authorIds);
        normalizedIds.add(requester.getId());

        Set<UserEntity> authors = new HashSet<>(userRepository.findAllById(normalizedIds));
        if (authors.size() != normalizedIds.size()) {
            throw new NotFoundException("One or more authors do not exist");
        }

        return authors;
    }

    private Set<CategoryEntity> resolveCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Long> normalizedIds = new HashSet<>(categoryIds);
        Set<CategoryEntity> categories = new HashSet<>(categoryRepository.findAllById(normalizedIds));
        if (categories.size() != normalizedIds.size()) {
            throw new NotFoundException("One or more categories do not exist");
        }
        return categories;
    }

    private NoteVisibility parseVisibility(String visibility) {
        NoteVisibility parsed = parseVisibilityNullable(visibility);
        if (parsed == null) {
            throw new BadRequestException("visibility is required");
        }
        return parsed;
    }

    private NoteVisibility parseVisibilityNullable(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return null;
        }

        try {
            return NoteVisibility.valueOf(visibility.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported visibility: " + visibility);
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(value);
        } catch (Exception exception) {
            throw new BadRequestException("Invalid ISO date-time value: " + value);
        }
    }

    private void ensureCanRead(NoteEntity note, UserEntity requester) {
        if (note.getVisibility() == NoteVisibility.PUBLIC || isAuthor(note, requester.getId())) {
            return;
        }
        throw new AccessDeniedException("You do not have access to note " + note.getId());
    }

    private void ensureCanEdit(NoteEntity note, UserEntity requester) {
        if (isAuthor(note, requester.getId())) {
            return;
        }
        throw new AccessDeniedException("You do not have edit access to note " + note.getId());
    }

    private boolean isAuthor(NoteEntity note, Long userId) {
        return note.getAuthors().stream().anyMatch(author -> author.getId().equals(userId));
    }

    private void validateJointPremium(NoteVisibility visibility, UserEntity requester, Set<UserEntity> authors) {
        if (visibility != NoteVisibility.JOINT) {
            return;
        }

        if (!requester.isPremium()) {
            throw new BusinessRuleException("Only premium users can create joint notes");
        }

        boolean allPremium = authors.stream().allMatch(UserEntity::isPremium);
        if (!allPremium) {
            throw new BusinessRuleException("All joint note authors must have premium accounts");
        }
    }

    private void validatePublicLimit(NoteVisibility visibility, UserEntity requester, NoteEntity existingNote) {
        if (visibility != NoteVisibility.PUBLIC || requester.isPremium()) {
            return;
        }

        long publicNotes = noteRepository.countByAuthorAndVisibility(requester.getId(), NoteVisibility.PUBLIC);
        boolean alreadyPublic = existingNote != null && existingNote.getVisibility() == NoteVisibility.PUBLIC;
        if (!alreadyPublic && publicNotes >= FREE_PUBLIC_NOTES_LIMIT) {
            throw new BusinessRuleException("Free users can publish at most 10 public notes");
        }
    }
}
