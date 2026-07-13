package com.example.PRM.initializer;

import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.repository.RoleRepository;
import com.example.PRM.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class InitializerStaff implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public InitializerStaff(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Chỉ tạo nếu chưa có
        if (!userRepository.existsByUserName("staff")) {
            Role staffRole = roleRepository.findByRoleName("STAFF")
                    .orElseThrow(() -> new RuntimeException("Role STAFF không tồn tại"));

            User admin = User.builder()
                    .userName("staff1")
                    .password(passwordEncoder.encode("staff123@"))
                    .fullName("Trần Thị Mỹ Hạnh")
                    .email("hanhhhe@fpt.com")
                    .phone("0909000000")
                    .role(staffRole)
                    .isVerified(true)
                    .isBlocked(false)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Staff account created successfully!");
        } else {
            System.out.println("ℹ️ Staff account already exists, skipping...");
        }
    }
}
