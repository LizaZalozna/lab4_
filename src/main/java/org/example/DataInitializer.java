package org.example;

import org.example.notes.repository.model.NoteEntity;
import org.example.notes.repository.model.NoteVisibility;
import org.example.notes.repository.NoteRepository;
import org.example.categories.repository.CategoryRepository;
import org.example.categories.repository.model.CategoryEntity;
import org.example.users.repository.model.UserEntity;
import org.example.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository, CategoryRepository categoryRepository, NoteRepository noteRepository) {
        return args -> {
            if (!userRepository.findAll().isEmpty()) {
                return;
            }

            UserEntity anna = userRepository.save(UserEntity.builder()
                    .username("anna.work")
                    .name("Anna")
                    .surname("Koval")
                    .isPremium(true)
                    .password("secret1")
                    .build());

            UserEntity ivan = userRepository.save(UserEntity.builder()
                    .username("ivan.personal")
                    .name("Ivan")
                    .surname("Petrenko")
                    .isPremium(false)
                    .password("secret2")
                    .build());

            UserEntity olena = userRepository.save(UserEntity.builder()
                    .username("olena.team")
                    .name("Olena")
                    .surname("Marchenko")
                    .isPremium(true)
                    .password("secret3")
                    .build());

            CategoryEntity work = categoryRepository.save(CategoryEntity.builder()
                    .name("Work")
                    .description("Working notes and action items")
                    .build());

            CategoryEntity personal = categoryRepository.save(CategoryEntity.builder()
                    .name("Personal")
                    .description("Private notes and reminders")
                    .build());

            noteRepository.save(NoteEntity.builder()
                    .title("Sprint backlog")
                    .content("Discuss API performance and optimize search filters.")
                    .visibility(NoteVisibility.JOINT)
                    .authors(Set.of(anna, olena))
                    .categories(Set.of(work))
                    .build());

            noteRepository.save(NoteEntity.builder()
                    .title("Weekend plans")
                    .content("Buy groceries and prepare gifts.")
                    .visibility(NoteVisibility.PRIVATE)
                    .authors(Set.of(ivan))
                    .categories(Set.of(personal))
                    .build());

            noteRepository.save(NoteEntity.builder()
                    .title("Public REST checklist")
                    .content("Prepare Postman demo requests for the labwork.")
                    .visibility(NoteVisibility.PUBLIC)
                    .authors(Set.of(anna))
                    .categories(Set.of(work))
                    .build());
        };
    }
}
