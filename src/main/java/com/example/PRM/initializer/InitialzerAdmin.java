package com.example.PRM.initializer;

import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.repository.RoleRepository;
import com.example.PRM.repository.UserRepository;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class InitialzerAdmin implements CommandLineRunner{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialzerAdmin(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Chỉ tạo nếu chưa có
        if (!userRepository.existsByUserName("admin")) {
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN không tồn tại"));

            User admin = User.builder()
                    .userName("admin")
                    .password(passwordEncoder.encode("admin123@"))
                    .fullName("Tống Ngọc Anh Tài")
                    .email("taitnase181719@fpt.com")
                    .phone("0909000000")
                    .role(adminRole)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin account created successfully!");
        } else {
            System.out.println("ℹ️ Admin account already exists, skipping...");
        }
    }
}
