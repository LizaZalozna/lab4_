package org.example.users.service;

import org.example.users.service.exception.BadRequestException;
import org.example.users.service.exception.NotFoundException;
import org.example.users.repository.model.UserEntity;
import org.example.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthContextService {

    private final UserRepository userRepository;

    public AuthContextService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity requireUser(Long userId) {
        if (userId == null) {
            throw new BadRequestException("Header X-User-Id is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }
}
