package org.example.users.service;

import org.example.users.service.exception.BusinessRuleException;
import org.example.users.service.exception.NotFoundException;
import org.example.users.repository.model.UserEntity;
import org.example.users.repository.UserRepository;
import org.example.users.api.dto.CreateUserRequest;
import org.example.notes.repository.NoteRepository;
import org.example.notes.repository.model.NoteEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    public UserService(UserRepository userRepository, NoteRepository noteRepository) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
    }

    public UserEntity create(CreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessRuleException("Username already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .surname(request.getSurname())
                .name(request.getName())
                .isPremium(Boolean.TRUE.equals(request.getIsPremium()))
                .password(request.getPassword())
                .build();

        return userRepository.save(user);
    }

    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

@Transactional
    public void delete(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow();
        List<NoteEntity> notes = noteRepository.findByAuthorId(id);
        for (NoteEntity note : notes) {
            int c = note.getAuthors().size();
            note.getAuthors().remove(user);
            if (c == 1) {
                noteRepository.delete(note);
            }
            else {
                noteRepository.save(note);
            }

        }
        userRepository.deleteById(id);
    }
}
