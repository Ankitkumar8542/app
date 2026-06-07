package com.musicPlayer.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.musicPlayer.app.category.entity.Category;
import com.musicPlayer.app.category.repository.CategoryRepository;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedCategories();
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@musicplayer.com")) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@musicplayer.com")
                    .password(passwordEncoder.encode("Admin@12345"))
                    .role(User.Role.ADMIN)
                    .status(User.AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Admin user seeded: admin@musicplayer.com / Admin@12345");
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    Category.builder().name("Pop").color("#FF6B6B").description("Popular mainstream music").build(),
                    Category.builder().name("Rock").color("#4ECDC4").description("Rock and alternative music").build(),
                    Category.builder().name("Hip Hop").color("#45B7D1").description("Hip hop and rap music").build(),
                    Category.builder().name("Jazz").color("#96CEB4").description("Jazz and blues music").build(),
                    Category.builder().name("Classical").color("#FFEAA7").description("Classical and orchestral").build(),
                    Category.builder().name("Electronic").color("#DDA0DD").description("Electronic and EDM").build(),
                    Category.builder().name("R&B").color("#F0A500").description("Rhythm and blues").build(),
                    Category.builder().name("Country").color("#98D8C8").description("Country and folk music").build(),
                    Category.builder().name("Reggae").color("#6BCB77").description("Reggae and dancehall").build(),
                    Category.builder().name("Metal").color("#C0392B").description("Heavy metal and hard rock").build(),
                    Category.builder().name("Latin").color("#E67E22").description("Latin and salsa music").build(),
                    Category.builder().name("Bollywood").color("#9B59B6").description("Bollywood and Indian music").build()
            );
            categoryRepository.saveAll(categories);
            log.info("✅ {} music categories seeded", categories.size());
        }
    }
}