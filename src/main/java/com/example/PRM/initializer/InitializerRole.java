package com.example.PRM.initializer;

import com.example.PRM.entity.Role;
import com.example.PRM.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class InitializerRole implements CommandLineRunner {
    private final RoleRepository roleRepository;

    public InitializerRole(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("ADMIN", "Quản trị viên hệ thống");
        createRoleIfNotExists("STAFF", "Nhân viên");
        createRoleIfNotExists("ORGANIZATION", "Tổ chức");
        createRoleIfNotExists("MEMBER", "Người dùng thông thường");
        createRoleIfNotExists("GUEST", "Khách");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .roleName(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            System.out.println("✅ Role created: " + roleName);
        } else {
            System.out.println("ℹ️ Role already exists: " + roleName);
        }
    }
}
