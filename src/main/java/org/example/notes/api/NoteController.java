package org.example.notes.api;

import jakarta.validation.Valid;
import org.example.notes.service.NoteService;
import org.example.notes.api.dto.PageResponse;
import org.example.notes.api.dto.NoteRequest;
import org.example.notes.api.dto.NoteResponse;
import org.example.notes.api.dto.NoteSearchRequest;
import org.example.notes.service.ApiMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;
    private final ApiMapper apiMapper;

    public NoteController(NoteService noteService, ApiMapper apiMapper) {
        this.noteService = noteService;
        this.apiMapper = apiMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse create(
            @RequestHeader(name = "X-User-Id", required = false) Long requesterId,
            @Valid @RequestBody NoteRequest request
    ) {
        return apiMapper.toNoteResponse(noteService.create(requesterId, request));
    }

    @GetMapping("/{id}")
    public NoteResponse getById(
            @RequestHeader(name = "X-User-Id", required = false) Long requesterId,
            @PathVariable Long id
    ) {
        return apiMapper.toNoteResponse(noteService.getById(requesterId, id));
    }

    @PutMapping("/{id}")
    public NoteResponse update(
            @RequestHeader(name = "X-User-Id", required = false) Long requesterId,
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request
    ) {
        return apiMapper.toNoteResponse(noteService.update(requesterId, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(name = "X-User-Id", required = false) Long requesterId,
            @PathVariable Long id
    ) {
        noteService.delete(requesterId, id);
    }

    @PostMapping("/{id}/categories/{categoryId}")
    public NoteResponse addCategory(
            @RequestHeader(name = "X-User-Id", required = false) Long requesterId,
            @PathVariable Long id,
            @PathVariable Long categoryId
    ) {
        return apiMapper.toNoteResponse(noteService.addCategory(requesterId, id, categoryId));
    }

    @GetMapping("/search")
    public PageResponse<NoteResponse> search(
            @RequestHeader(name = "X-User-Id", required = false) Long requesterId,
            NoteSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<NoteResponse> responsePage = noteService.search(requesterId, request, pageable)
                .map(apiMapper::toNoteResponse);
        return apiMapper.toPageResponse(responsePage);
    }
}
