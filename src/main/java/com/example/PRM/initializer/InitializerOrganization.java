package com.example.PRM.initializer;

import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.repository.OrganizationDetailRepository;
import com.example.PRM.repository.RoleRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.status_enum.VerificationOrganizationStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(3)
public class InitializerOrganization implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationDetailRepository organizationDetailRepository;
    private final PasswordEncoder passwordEncoder;

    public InitializerOrganization(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   OrganizationDetailRepository organizationDetailRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationDetailRepository = organizationDetailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByUserName("org_sos")) {
            Role orgRole = roleRepository.findByRoleName("ORGANIZATION")
                    .orElseThrow(() -> new RuntimeException("Role ORGANIZATION không tồn tại"));

            // 1. Làng trẻ em SOS Gò Vấp (TP.HCM)
            User user1 = createUser("org_sos", "sos@gmail.com", "Làng trẻ em SOS", orgRole);
            createOrganizationDetail(user1, "Làng trẻ em SOS Gò Vấp", "Nuôi dưỡng, chăm sóc trẻ mồ côi.", "697 Quang Trung, Phường 8, Gò Vấp, Hồ Chí Minh", "10.825700", "106.663185");

            // 2. Quỹ Từ thiện Trăng Khuyết (TP.HCM)
            User user2 = createUser("org_trangkhuyet", "trangkhuyet@gmail.com", "Quỹ Trăng Khuyết", orgRole);
            createOrganizationDetail(user2, "Quỹ Từ thiện Trăng Khuyết", "Hỗ trợ người già neo đơn và trẻ em lang thang.", "102/42 Cống Quỳnh, Phạm Ngũ Lão, Quận 1, Hồ Chí Minh", "10.771142", "106.685959");

            // 3. Hội bảo trợ người khuyết tật và trẻ mồ côi (TP.HCM)
            User user3 = createUser("org_hoibaotro", "hoibaotro@gmail.com", "Hội Bảo Trợ", orgRole);
            createOrganizationDetail(user3, "Hội bảo trợ người khuyết tật và trẻ mồ côi TP.HCM", "Bảo trợ những mảnh đời bất hạnh, khiếm khuyết.", "33 Phùng Khắc Khoan, Đa Kao, Quận 1, Hồ Chí Minh", "10.783685", "106.696144");

            // 4. Quỹ Tấm Lòng Vàng (Hà Nội)
            User user4 = createUser("org_tamlongvang", "tamlongvang@gmail.com", "Quỹ Tấm Lòng Vàng", orgRole);
            createOrganizationDetail(user4, "Quỹ Tấm Lòng Vàng (Hà Nội)", "Quỹ hỗ trợ bệnh nhân nghèo và sinh viên vượt khó.", "51 Hàng Bồ, Hoàn Kiếm, Hà Nội", "21.034511", "105.849817");

            // 5. Trung tâm nuôi dưỡng trẻ mồ côi Hoa Mai (Đà Nẵng)
            User user5 = createUser("org_hoamai", "hoamai@gmail.com", "Trung tâm Hoa Mai", orgRole);
            createOrganizationDetail(user5, "Trung tâm nuôi dưỡng trẻ mồ côi Hoa Mai", "Tổ ấm cho các em nhỏ không nơi nương tựa tại Đà Nẵng.", "Quốc Lộ 14B, Hoà Châu, Hòa Vang, Đà Nẵng", "15.981881", "108.203522");

            System.out.println("✅ 5 Sample Organizations created successfully!");
        } else {
            System.out.println("ℹ️ Organizations already exist, skipping...");
        }
    }

    private User createUser(String username, String email, String fullName, Role role) {
        User user = User.builder()
                .userName(username)
                .password(passwordEncoder.encode("Org123@"))
                .fullName(fullName)
                .email(email)
                .phone("090" + (int)(Math.random() * 10000000))
                .role(role)
                .isVerified(true)
                .isBlocked(false)
                .build();
        return userRepository.save(user);
    }

    private void createOrganizationDetail(User user, String name, String description, String address, String lat, String lng) {
        OrganizationDetail org = new OrganizationDetail();
        org.setUser(user);
        org.setOrgName(name);
        org.setDescription(description);
        org.setAddress(address);
        org.setLatitude(new BigDecimal(lat));
        org.setLongitude(new BigDecimal(lng));
        org.setStatus(VerificationOrganizationStatus.APPROVED);
        org.setSubmitAt(LocalDateTime.now().minusDays(10));
        org.setApprovedAt(LocalDateTime.now().minusDays(9));
        org.setAcceptedTypes(List.of("Quần áo", "Tiền mặt", "Sách vở"));
        
        organizationDetailRepository.save(org);
    }
}
