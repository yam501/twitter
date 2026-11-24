package com.yashin.userservice.config;

import com.yashin.userservice.entity.Role;
import com.yashin.userservice.entity.User;
import com.yashin.userservice.repository.RoleRepository;
import com.yashin.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

// справочник ролей наполняет schema.sql; здесь только администратор,
// через которого проверяются эндпоинты выдачи/снятия ролей
@Component
public class DataInitializer implements CommandLineRunner {

    @Value("${ADMIN_EMAIL:admin@example.com}")
    private String adminEmail;
    @Value("${ADMIN_PASSWORD:admin12345}")
    private String adminPassword;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }
        Role administrator = roleRepository.findByName("administrator").orElseThrow();

        User admin = new User();
        admin.setFirstname("Admin");
        admin.setLastname("Admin");
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.getRoles().add(administrator);
        userRepository.save(admin);
    }
}
