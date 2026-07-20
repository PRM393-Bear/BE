-- 1. Setup Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$ 
DECLARE 
    -- Roles
    role_admin_id UUID := gen_random_uuid();
    role_staff_id UUID := gen_random_uuid();
    role_member_id UUID := gen_random_uuid();
    role_org_id UUID := gen_random_uuid();

    -- Users
    admin_id UUID := gen_random_uuid();
    staff1_id UUID := gen_random_uuid();
    staff2_id UUID := gen_random_uuid();
    member1_id UUID := gen_random_uuid();
    member2_id UUID := gen_random_uuid();
    member3_id UUID := gen_random_uuid();
    member4_id UUID := gen_random_uuid();
    member5_id UUID := gen_random_uuid();
    org1_user_id UUID := gen_random_uuid();
    org2_user_id UUID := gen_random_uuid();
    
    -- Orgs
    org1_id UUID := gen_random_uuid();
    org2_id UUID := gen_random_uuid();

    -- Shops (for members)
    shop1_id UUID := gen_random_uuid();
    shop2_id UUID := gen_random_uuid();
    shop3_id UUID := gen_random_uuid();
    shop4_id UUID := gen_random_uuid();
    shop5_id UUID := gen_random_uuid();

    -- Categories
    cat1_id UUID := gen_random_uuid();
    cat2_id UUID := gen_random_uuid();

    -- Products
    p1_1 UUID := gen_random_uuid(); p1_2 UUID := gen_random_uuid(); p1_3 UUID := gen_random_uuid(); p1_4 UUID := gen_random_uuid(); p1_5 UUID := gen_random_uuid(); p1_6 UUID := gen_random_uuid();
    p2_1 UUID := gen_random_uuid(); p2_2 UUID := gen_random_uuid(); p2_3 UUID := gen_random_uuid(); p2_4 UUID := gen_random_uuid(); p2_5 UUID := gen_random_uuid(); p2_6 UUID := gen_random_uuid();
    p3_1 UUID := gen_random_uuid(); p3_2 UUID := gen_random_uuid(); p3_3 UUID := gen_random_uuid(); p3_4 UUID := gen_random_uuid(); p3_5 UUID := gen_random_uuid(); p3_6 UUID := gen_random_uuid();
    p4_1 UUID := gen_random_uuid(); p4_2 UUID := gen_random_uuid(); p4_3 UUID := gen_random_uuid(); p4_4 UUID := gen_random_uuid(); p4_5 UUID := gen_random_uuid(); p4_6 UUID := gen_random_uuid();
    p5_1 UUID := gen_random_uuid(); p5_2 UUID := gen_random_uuid(); p5_3 UUID := gen_random_uuid(); p5_4 UUID := gen_random_uuid(); p5_5 UUID := gen_random_uuid(); p5_6 UUID := gen_random_uuid();

    -- Chat Rooms
    room1_id UUID := gen_random_uuid();
    room2_id UUID := gen_random_uuid();

    -- Donation Events
    event1_id UUID := gen_random_uuid();
    event2_id UUID := gen_random_uuid();
BEGIN
    -- 1. Clean existing data
    TRUNCATE TABLE 
        community_post, notifications, chat_messages, chat_rooms, 
        order_items, orders, donation_event, 
        organization_detail, product, category, shops, users, roles 
    CASCADE;

    -- 2. Insert Roles
    INSERT INTO roles (role_id, role_name, description) VALUES
    (role_admin_id, 'ADMIN', 'Quản trị viên hệ thống'),
    (role_staff_id, 'STAFF', 'Nhân viên kiểm duyệt'),
    (role_member_id, 'MEMBER', 'Người dùng thông thường'),
    (role_org_id, 'ORGANIZATION', 'Tổ chức từ thiện/quyên góp');

    -- 3. Insert Users
    -- Mật khẩu sử dụng crypt(text, gen_salt) để băm bằng bcrypt, tương thích với BCryptPasswordEncoder của Spring Security
    INSERT INTO users (user_id, user_name, password, full_name, email, role_id, is_verified, is_blocked) VALUES
    (admin_id, 'admin111', crypt('admin111', gen_salt('bf', 10)), 'Administrator', 'admin111@example.com', role_admin_id, true, false),
    (staff1_id, 'staff111', crypt('staff111', gen_salt('bf', 10)), 'Staff One', 'staff111@example.com', role_staff_id, true, false),
    (staff2_id, 'staff222', crypt('staff222', gen_salt('bf', 10)), 'Staff Two', 'staff222@example.com', role_staff_id, true, false),
    
    (member1_id, 'member111', crypt('member111', gen_salt('bf', 10)), 'Member One', 'member111@example.com', role_member_id, true, false),
    (member2_id, 'member222', crypt('member222', gen_salt('bf', 10)), 'Member Two', 'member222@example.com', role_member_id, true, false),
    (member3_id, 'member333', crypt('member333', gen_salt('bf', 10)), 'Member Three', 'member333@example.com', role_member_id, true, false),
    (member4_id, 'member444', crypt('member444', gen_salt('bf', 10)), 'Member Four', 'member444@example.com', role_member_id, true, false),
    (member5_id, 'member555', crypt('member555', gen_salt('bf', 10)), 'Member Five', 'member555@example.com', role_member_id, true, false),
    
    (org1_user_id, 'org111', crypt('org111', gen_salt('bf', 10)), 'Tổ chức Red Cross', 'org111@example.com', role_org_id, true, false),
    (org2_user_id, 'org222', crypt('org222', gen_salt('bf', 10)), 'Quỹ Môi Trường Xanh', 'org222@example.com', role_org_id, true, false);

    -- 4. Insert Categories
    INSERT INTO category (id, name, description) VALUES
    (cat1_id, 'Áo nam', 'Các loại áo dành cho nam'),
    (cat2_id, 'Áo nữ', 'Các loại áo dành cho nữ');

    -- 5. Insert Shops (Mỗi member cần có 1 shop để đăng sản phẩm)
    INSERT INTO shops (id, owner_id, shop_name, address, phone, is_verified) VALUES
    (shop1_id, member1_id, 'Shop của Member 111', 'Hà Nội', '0901234561', true),
    (shop2_id, member2_id, 'Shop của Member 222', 'TP.HCM', '0901234562', true),
    (shop3_id, member3_id, 'Shop của Member 333', 'Đà Nẵng', '0901234563', true),
    (shop4_id, member4_id, 'Shop của Member 444', 'Hải Phòng', '0901234564', true),
    (shop5_id, member5_id, 'Shop của Member 555', 'Cần Thơ', '0901234565', true);

    -- 6. Insert Products (30 sản phẩm, mỗi member 6 sản phẩm)
    INSERT INTO product (id, seller_id, shop_id, category_id, title, description, type, condition, brand, price, size, color, ai_tags, lifecycle_generation, images, status, created_at) VALUES
    (p1_1, member1_id, shop1_id, cat1_id, 'Áo thun nam basic', 'Áo thun nam kiểu dáng basic rất đẹp', 'ITEM', 90, 'Adidas', 150000, 'L', 'black', '["casual", "basic"]', 1, '["https://images.unsplash.com/photo-1521572163474-6864f9cf17ab"]', 'AVAILABLE', NOW()),
    (p1_2, member1_id, shop1_id, cat1_id, 'Áo khoác nam thu đông', 'Áo khoác nam mặc siêu ấm', 'ITEM', 85, 'Nike', 250000, 'XL', 'gray', '["winter", "jacket"]', 1, '["https://images.unsplash.com/photo-1551028719-00167b16eac5"]', 'AVAILABLE', NOW()),
    (p1_3, member1_id, shop1_id, cat1_id, 'Áo sơ mi nam công sở', 'Sơ mi trắng chuẩn form', 'ITEM', 95, 'Owen', 300000, 'M', 'white', '["office", "formal"]', 1, '["https://images.unsplash.com/photo-1598032895397-b9472444bf93"]', 'AVAILABLE', NOW()),
    (p1_4, member1_id, shop1_id, cat1_id, 'Quần tây nam đen', 'Quần âu đi làm siêu lịch lãm', 'ITEM', 90, 'Việt Tiến', 350000, '31', 'black', '["pants", "office"]', 1, '["https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQTNP7T64vX3jsKTmZ3fDLCFsm5w9cmmt5JQLodk1ckvJpmqJ2iHQLV4HA&s=10"]', 'AVAILABLE', NOW()),
    (p1_5, member1_id, shop1_id, cat1_id, 'Áo phông nam hoạ tiết', 'Áo dạo phố mát mẻ', 'ITEM', 80, 'Routine', 120000, 'L', 'blue', '["casual", "summer"]', 1, '["https://images.unsplash.com/photo-1576871337622-98d48d1cf531"]', 'AVAILABLE', NOW()),
    (p1_6, member1_id, shop1_id, cat1_id, 'Áo jacket da nam', 'Áo khoác da thật 100%', 'ITEM', 88, 'Mango Man', 850000, 'XL', 'brown', '["jacket", "leather"]', 1, '["https://images.unsplash.com/photo-1520975954732-57dd22299614"]', 'AVAILABLE', NOW()),
    
    (p2_1, member2_id, shop2_id, cat2_id, 'Váy lụa mùa hè', 'Váy lụa cực mát mẻ', 'ITEM', 95, 'Zara', 200000, 'M', 'white', '["summer", "dress"]', 1, '["https://images.unsplash.com/photo-1572804013309-59a88b7e92f1"]', 'AVAILABLE', NOW()),
    (p2_2, member2_id, shop2_id, cat2_id, 'Áo sơ mi công sở nữ', 'Áo công sở thanh lịch', 'ITEM', 90, 'H&M', 180000, 'S', 'blue', '["office", "formal"]', 1, '["https://images.unsplash.com/photo-1512436991641-6745cdb1723f"]', 'AVAILABLE', NOW()),
    (p2_3, member2_id, shop2_id, cat2_id, 'Áo len cardigan nữ', 'Cardigan mỏng nhẹ', 'ITEM', 92, 'Uniqlo', 250000, 'M', 'beige', '["cardigan", "autumn"]', 1, '["https://images.unsplash.com/photo-1434389670869-c45d4f2858d1"]', 'AVAILABLE', NOW()),
    (p2_4, member2_id, shop2_id, cat2_id, 'Chân váy xoè nữ', 'Chân váy midi điệu đà', 'ITEM', 99, 'Canifa', 190000, 'S', 'black', '["skirt", "casual"]', 1, '["https://images.unsplash.com/photo-1539008835657-9e8e9680c956"]', 'AVAILABLE', NOW()),
    (p2_5, member2_id, shop2_id, cat2_id, 'Quần jean nữ cạp cao', 'Quần ôm tôn dáng', 'ITEM', 85, 'Levis', 350000, '28', 'blue', '["jeans", "casual"]', 1, '["https://images.unsplash.com/photo-1541099649105-f69ad21f3246"]', 'AVAILABLE', NOW()),
    (p2_6, member2_id, shop2_id, cat2_id, 'Đầm dự tiệc nữ', 'Đầm trễ vai gợi cảm', 'ITEM', 98, 'Marc', 600000, 'M', 'red', '["party", "dress"]', 1, '["https://images.unsplash.com/photo-1566174053879-31528523f8ae"]', 'AVAILABLE', NOW()),

    (p3_1, member3_id, shop3_id, cat1_id, 'Áo hoodie đen Unisex', 'Hoodie unisex cho mùa đông', 'ITEM', 80, 'Champion', 120000, 'M', 'black', '["winter", "hoodie"]', 1, '["https://images.unsplash.com/photo-1556821840-3a63f95609a7"]', 'AVAILABLE', NOW()),
    (p3_2, member3_id, shop3_id, cat1_id, 'Quần jean nam ống rộng', 'Quần ống rộng form chuẩn', 'ITEM', 100, 'Levis', 300000, '32', 'blue', '["jeans", "casual"]', 1, '["https://images.unsplash.com/photo-1542272604-780c8df94917"]', 'AVAILABLE', NOW()),
    (p3_3, member3_id, shop3_id, cat1_id, 'Áo khoác dù chống nước', 'Áo gió siêu nhẹ', 'ITEM', 90, 'The North Face', 400000, 'L', 'green', '["jacket", "outdoor"]', 1, '["https://images.unsplash.com/photo-1544441893-675973e31985"]', 'AVAILABLE', NOW()),
    (p3_4, member3_id, shop3_id, cat1_id, 'Áo thun dài tay', 'Thun dài tay trơn', 'ITEM', 85, 'Coolmate', 120000, 'M', 'white', '["casual", "long-sleeve"]', 1, '["https://images.unsplash.com/photo-1618517351616-3898bd307a52"]', 'AVAILABLE', NOW()),
    (p3_5, member3_id, shop3_id, cat1_id, 'Quần short kaki nam', 'Quần short mùa hè mát', 'ITEM', 95, 'Owen', 180000, '31', 'beige', '["short", "summer"]', 1, '["https://images.unsplash.com/photo-1591195853828-11db59a44f6b"]', 'AVAILABLE', NOW()),
    (p3_6, member3_id, shop3_id, cat1_id, 'Áo polo thể thao nam', 'Polo thoáng khí', 'ITEM', 88, 'Nike', 250000, 'L', 'navy', '["polo", "sports"]', 1, '["https://images.unsplash.com/photo-1581655353564-df123a1eb820"]', 'AVAILABLE', NOW()),

    (p4_1, member4_id, shop4_id, cat2_id, 'Áo len nữ hàn quốc', 'Áo len phong cách hàn quốc', 'ITEM', 99, 'Gucci', 500000, 'F', 'pink', '["winter", "sweater"]', 1, '["https://images.unsplash.com/photo-1620799140408-edc6dcb6d633"]', 'AVAILABLE', NOW()),
    (p4_2, member4_id, shop4_id, cat2_id, 'Chân váy ngắn caro', 'Chân váy caro năng động', 'ITEM', 88, 'Prada', 150000, 'S', 'red', '["skirt", "casual"]', 1, '["https://images.unsplash.com/photo-1582142306909-195724d33ffc"]', 'AVAILABLE', NOW()),
    (p4_3, member4_id, shop4_id, cat2_id, 'Áo croptop nữ', 'Áo croptop năng động', 'ITEM', 95, 'Forever 21', 110000, 'S', 'yellow', '["croptop", "summer"]', 1, '["https://images.unsplash.com/photo-1503342394128-c104d54dba01"]', 'AVAILABLE', NOW()),
    (p4_4, member4_id, shop4_id, cat2_id, 'Quần ống loe nữ', 'Quần vải ống loe', 'ITEM', 90, 'Mango', 220000, 'M', 'black', '["pants", "fashion"]', 1, '["https://images.unsplash.com/photo-1509631179647-0177331693ae"]', 'AVAILABLE', NOW()),
    (p4_5, member4_id, shop4_id, cat2_id, 'Áo trễ vai nữ', 'Áo trễ vai đi biển', 'ITEM', 85, 'Elise', 150000, 'M', 'white', '["top", "summer"]', 1, '["https://images.unsplash.com/photo-1551163943-3f6a855d1153"]', 'AVAILABLE', NOW()),
    (p4_6, member4_id, shop4_id, cat2_id, 'Áo khoác blazer nữ', 'Blazer công sở', 'ITEM', 96, 'NEM', 450000, 'L', 'beige', '["blazer", "office"]', 1, '["https://images.unsplash.com/photo-1548624149-f9b1859aa7d0"]', 'AVAILABLE', NOW()),

    (p5_1, member5_id, shop5_id, cat1_id, 'Áo polo trắng', 'Polo dành cho tiệc, sự kiện', 'ITEM', 92, 'Ralph Lauren', 220000, 'L', 'white', '["polo", "formal"]', 1, '["https://images.unsplash.com/photo-1581655353564-df123a1eb820"]', 'AVAILABLE', NOW()),
    (p5_2, member5_id, shop5_id, cat1_id, 'Bộ vest nam', 'Vest đi tiệc sang trọng', 'ITEM', 100, 'Hugo Boss', 1000000, 'XL', 'black', '["suit", "party"]', 1, '["https://images.unsplash.com/photo-1594938298603-c8148c4dae35"]', 'AVAILABLE', NOW()),
    (p5_3, member5_id, shop5_id, cat1_id, 'Áo sơ mi denim nam', 'Sơ mi bò chất chơi', 'ITEM', 88, 'Levis', 380000, 'L', 'blue', '["denim", "casual"]', 1, '["https://images.unsplash.com/photo-1534452203293-494d7ddbf7e0"]', 'AVAILABLE', NOW()),
    (p5_4, member5_id, shop5_id, cat1_id, 'Quần jogger nam', 'Quần jogger thể thao', 'ITEM', 95, 'Adidas', 280000, 'M', 'gray', '["jogger", "sports"]', 1, '["https://images.unsplash.com/photo-1517438476312-10d79c077509"]', 'AVAILABLE', NOW()),
    (p5_5, member5_id, shop5_id, cat1_id, 'Áo ba lỗ nam', 'Áo ba lỗ tập gym', 'ITEM', 90, 'Gymshark', 150000, 'L', 'black', '["tanktop", "gym"]', 1, '["https://images.unsplash.com/photo-1507314961162-1279a0ce6295"]', 'AVAILABLE', NOW()),
    (p5_6, member5_id, shop5_id, cat1_id, 'Áo len cổ lọ nam', 'Áo len siêu ấm mùa đông', 'ITEM', 98, 'Uniqlo', 400000, 'M', 'brown', '["winter", "sweater"]', 1, '["https://images.unsplash.com/photo-1612444530582-fc66183b16f7"]', 'AVAILABLE', NOW());

    -- 7. Insert Organization Details
    INSERT INTO organization_detail (id, user_id, org_name, description, address, status, total_donation_received, accepted_types) VALUES
    (org1_id, org1_user_id, 'Hội Chữ Thập Đỏ VN', 'Tổ chức chữ thập đỏ Việt Nam', 'Hà Nội', 'APPROVED', 0, '["CLOTHES", "SHOES"]'),
    (org2_id, org2_user_id, 'Quỹ Môi Trường Xanh', 'Tổ chức bảo vệ môi trường, thu nhận quần áo cũ', 'TP.HCM', 'APPROVED', 0, '["CLOTHES"]');

    -- 8. Insert Donation Events (Chiến dịch quyên góp)
    INSERT INTO donation_event (id, organization_detail_id, title, description, location, start_date, end_date, target_quantity, current_quantity, status) VALUES
    (event1_id, org1_id, 'Quyên góp mùa đông 2026', 'Ủng hộ áo ấm cho trẻ em vùng cao', 'Hà Nội', NOW(), NOW() + interval '30 days', 1000, 150, 'ONGOING'),
    (event2_id, org2_id, 'Đổi quần áo lấy mầm xanh', 'Sự kiện tái chế quần áo bảo vệ môi trường', 'TP.HCM', NOW(), NOW() + interval '15 days', 500, 50, 'ONGOING');

    -- 9. Insert Community Posts (Bài đăng cộng đồng của tổ chức)
    INSERT INTO community_post (id, user_id, donation_event_id, content, images, created_at, is_hidden) VALUES
    (gen_random_uuid(), org1_user_id, event1_id, 'Chiến dịch mùa đông đã bắt đầu! Rất mong nhận được sự ủng hộ từ các nhà hảo tâm.', '["https://images.unsplash.com/photo-1488521787991-ed7bbaae773c"]', NOW(), false),
    (gen_random_uuid(), org2_user_id, event2_id, 'Hãy chung tay vì một trái đất xanh bằng cách tái chế quần áo cũ!', '["https://images.unsplash.com/photo-1532996122724-e3c354a0b15b"]', NOW(), false);

    -- 10. Simulate Purchases (Member 1 mua của Member 2, Member 3 mua của Member 4)
    DECLARE
        order1_id UUID := gen_random_uuid();
        order2_id UUID := gen_random_uuid();
    BEGIN
        -- Giả lập Đơn hàng
        INSERT INTO orders (id, buyer_id, seller_id, total_amount, status, created_at, updated_at) VALUES
        (order1_id, member1_id, member2_id, 200000, 'COMPLETED', NOW(), NOW()),
        (order2_id, member3_id, member4_id, 500000, 'COMPLETED', NOW(), NOW());
        
        -- Giả lập Item Đơn hàng
        INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, subtotal) VALUES
        (gen_random_uuid(), order1_id, p2_1, 1, 200000, 200000),
        (gen_random_uuid(), order2_id, p4_1, 1, 500000, 500000);
        

    END;

    -- 11. Simulate Chat Rooms & Messages
    -- Tạo phòng chat giữa Member1 & Member2, và Member1 & Tổ chức 1
    INSERT INTO chat_rooms (id, user1_id, user2_id, updated_at) VALUES
    (room1_id, member1_id, member2_id, NOW()),
    (room2_id, member1_id, org1_user_id, NOW());
    
    -- Tin nhắn giả lập
    INSERT INTO chat_messages (id, room_id, sender_id, content, created_at, status) VALUES
    (gen_random_uuid(), room1_id, member1_id, 'Chào bạn, váy lụa còn không ạ?', NOW() - interval '1 hour', 'SENT'),
    (gen_random_uuid(), room1_id, member2_id, 'Dạ váy lụa vẫn còn nha bạn.', NOW() - interval '50 minutes', 'SENT'),
    (gen_random_uuid(), room1_id, member1_id, 'Ok mình đã đặt mua nhé.', NOW() - interval '45 minutes', 'SENT'),
    
    (gen_random_uuid(), room2_id, member1_id, 'Cho mình hỏi quyên góp quần áo đến địa chỉ nào ạ?', NOW() - interval '2 hours', 'SENT'),
    (gen_random_uuid(), room2_id, org1_user_id, 'Bạn mang tới số 123 Hà Nội nhé, cảm ơn bạn!', NOW() - interval '1 hour', 'SENT');

    -- 12. Simulate Notifications
    INSERT INTO notifications (user_id, title, message, is_read, created_at, type) VALUES
    (member2_id, 'Đơn hàng mới', 'Bạn có một đơn hàng mới từ Member One', false, NOW(), 'ORDER'),
    (member4_id, 'Đơn hàng mới', 'Bạn có một đơn hàng mới từ Member Three', false, NOW(), 'ORDER'),
    (member1_id, 'Tin nhắn mới', 'Bạn có tin nhắn mới từ Member Two', false, NOW(), 'CHAT'),
    (member1_id, 'Chiến dịch mới', 'Tổ chức Red Cross VN vừa tạo chiến dịch: Quyên góp mùa đông 2026', false, NOW(), 'EVENT'),
    (org1_user_id, 'Tin nhắn mới', 'Bạn có tin nhắn mới từ Member One', false, NOW(), 'CHAT');

END $$;
