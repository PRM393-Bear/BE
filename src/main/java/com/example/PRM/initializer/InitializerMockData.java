package com.example.PRM.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Order(10) // Chạy sau cùng, sau khi các Initializer khác đã tạo các Role cơ bản
public class InitializerMockData implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public InitializerMockData(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // Kiểm tra xem đã có dữ liệu mẫu (admin111) chưa
        String checkSql = "SELECT count(*) FROM users WHERE user_name = 'admin111'";
        try {
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            // Nếu chưa có user admin111 thì tiến hành chạy script mock_data.sql
            if (count == null || count == 0) {
                System.out.println("⏳ Đang chạy dữ liệu mẫu (mock_data.sql)...");
                
                ClassPathResource resource = new ClassPathResource("mock_data.sql");
                if (resource.exists()) {
                    String sql = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                    
                    // Thực thi nguyên khối lệnh SQL
                    jdbcTemplate.execute(sql);
                    System.out.println("✅ Chạy mock_data.sql thành công! Dữ liệu giả lập đã được thêm.");
                } else {
                    System.out.println("⚠️ Không tìm thấy file mock_data.sql trong resources!");
                }
            } else {
                System.out.println("ℹ️ Dữ liệu mẫu đã tồn tại (admin111), bỏ qua quá trình chạy mock_data.sql.");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi chạy file mock_data.sql: " + e.getMessage());
            // Bỏ qua lỗi nếu bảng chưa tồn tại lúc mới khởi tạo
        }
    }
}
