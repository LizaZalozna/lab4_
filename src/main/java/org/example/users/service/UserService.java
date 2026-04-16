package org.example.users.service;

import org.example.users.service.exception.BusinessRuleException;
import org.example.users.service.exception.NotFoundException;
import org.example.users.repository.model.UserEntity;
import org.example.users.repository.UserRepository;
import org.example.users.api.dto.CreateUserRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public void delete(Long id) {
        getById(id);
        userRepository.deleteById(id);
    }
}
