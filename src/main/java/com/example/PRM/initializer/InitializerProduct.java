package com.example.PRM.initializer;

import com.example.PRM.entity.Category;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.repository.CategoryRepository;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.status_enum.ProductStatus;
import com.example.PRM.status_enum.ProductType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class InitializerProduct implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public InitializerProduct(ProductRepository productRepository,
                              UserRepository userRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            System.out.println("ℹ️ Products already exist, skipping...");
            return;
        }

        User seller = userRepository.findByUserName("admin1")
                .orElseThrow(() -> new RuntimeException("Admin user not found. Run InitialzerAdmin first."));

        // TẠO 5 DANH MỤC MẪU
        Category catAoNam = getOrCreateCategory("Áo Nam", "Thời trang áo dành cho nam giới");
        Category catAoNu = getOrCreateCategory("Áo Nữ", "Thời trang áo dành cho nữ giới");
        Category catQuanNam = getOrCreateCategory("Quần Nam", "Các loại quần dành cho nam");
        Category catQuanVayNu = getOrCreateCategory("Quần / Váy Nữ", "Quần và chân váy dành cho nữ");
        Category catPhuKien = getOrCreateCategory("Phụ Kiện", "Phụ kiện thời trang (túi, nón, thắt lưng...)");

        // ════════════════════════════════════
        // TOP — Áo (25 sản phẩm)
        // ════════════════════════════════════
        createProduct(seller, "Áo thun trắng", "Áo thun cotton mềm mại, mặc hàng ngày thoải mái.",
                catAoNam, "Uniqlo", ProductType.ITEM, (short) 9, 80000L, "L", "white",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782197343/prm/products/mnguyen0811/file_igmcxm.webp"), tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo thun đen form rộng", "Áo thun đen form rộng thời trang.",
                catAoNam, "Zara", ProductType.ITEM, (short) 9, 95000L, "L", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782197482/prm/products/mnguyen0811/file_e67wuz.jpg"), tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo sơ mi sọc xanh dương", "Sơ mi sọc xanh dương thanh lịch công sở.",
                catAoNam, "Owen", ProductType.ITEM, (short) 8, 250000L, "M", "navy",
                imgs("navy-shirt-1.jpg"), tags("office", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo sơ mi trắng công sở", "Sơ mi trắng cơ bản đi làm hoặc đi tiệc.",
                catAoNam, "Việt Tiến", ProductType.ITEM, (short) 9, 280000L, "M", "white",
                imgs("white-shirt-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo kiểu nữ hồng pastel", "Áo kiểu nữ màu hồng pastel nhẹ nhàng.",
                catAoNu, "H&M", ProductType.ITEM, (short) 8, 220000L, "S", "pink",
                imgs("pink-blouse-1.jpg"), tags("casual", "date", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo sơ mi linen màu be", "Sơ mi linen thoáng mát mùa hè.",
                catAoNu, "Uniqlo", ProductType.ITEM, (short) 9, 260000L, "M", "beige",
                imgs("beige-linen-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo sơ mi đen nam", "Sơ mi đen form ôm tôn dáng.",
                catAoNam, "Gucci", ProductType.ITEM, (short) 9, 270000L, "L", "black",
                imgs("black-formal-1.jpg"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo hoodie xám", "Hoodie xám ấm áp cho ngày lạnh.",
                catAoNam, "Champion", ProductType.ITEM, (short) 8, 320000L, "L", "gray",
                imgs("gray-hoodie-1.jpg"), tags("casual", "sport", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo khoác jean nam", "Áo khoác jean xanh cổ điển, mặc ít.",
                catAoNam, "Levi's", ProductType.ITEM, (short) 8, 250000L, "M", "denim",
                imgs("denim-jacket-1.jpg"), tags("casual", "outdoor", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo polo đỏ", "Polo đỏ cơ bản, lịch sự và năng động.",
                catAoNam, "Ralph Lauren", ProductType.ITEM, (short) 8, 180000L, "M", "red",
                imgs("red-polo-1.jpg"), tags("casual", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo len màu kem", "Áo len kem ấm áp dễ phối đồ.",
                catAoNu, "Zara", ProductType.ITEM, (short) 9, 290000L, "M", "cream",
                imgs("cream-sweater-1.jpg"), tags("casual", "date", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo sơ mi xanh nhạt", "Sơ mi xanh nhạt mặc hàng ngày thoải mái.",
                catAoNam, "Owen", ProductType.ITEM, (short) 8, 230000L, "M", "light-blue",
                imgs("lightblue-shirt-1.jpg"), tags("casual", "office", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo khoác kaki rêu", "Áo khoác kaki túi hộp màu xanh rêu.",
                catAoNam, "The North Face", ProductType.ITEM, (short) 8, 340000L, "L", "olive",
                imgs("olive-jacket-1.jpg"), tags("casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo ba lỗ đen", "Áo ba lỗ đen cơ bản đi tập gym hoặc mặc lót.",
                catAoNam, "Nike", ProductType.ITEM, (short) 9, 70000L, "S", "black",
                imgs("black-tank-1.jpg"), tags("sport", "casual", "beach"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo ba lỗ trắng", "Áo ba lỗ trắng mỏng nhẹ cho mùa hè.",
                catAoNam, "Adidas", ProductType.ITEM, (short) 9, 70000L, "S", "white",
                imgs("white-tank-1.jpg"), tags("sport", "beach", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo măng tô dáng dài", "Áo măng tô nữ dáng dài màu nâu lạc đà.",
                catAoNu, "Mango", ProductType.ITEM, (short) 8, 580000L, "M", "camel",
                imgs("camel-trench-1.jpg"), tags("office", "formal", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo blazer đen", "Blazer đen sắc sảo đi làm hoặc dự sự kiện.",
                catAoNu, "Zara", ProductType.ITEM, (short) 9, 450000L, "M", "black",
                imgs("black-blazer-1.jpg"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo blazer xanh dương", "Blazer xanh đậm thông minh, cực kỳ linh hoạt.",
                catAoNam, "Hugo Boss", ProductType.ITEM, (short) 8, 470000L, "L", "navy",
                imgs("navy-blazer-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo sơ mi đi biển màu vàng", "Sơ mi ngắn tay màu vàng rực rỡ cho mùa hè.",
                catAoNam, "H&M", ProductType.ITEM, (short) 8, 190000L, "M", "yellow",
                imgs("yellow-shirt-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo len mỏng tím nhạt", "Áo len mỏng màu tím pastel siêu mộng mơ.",
                catAoNu, "Uniqlo", ProductType.ITEM, (short) 8, 240000L, "S", "lavender",
                imgs("lavender-sweater-1.jpg"), tags("casual", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo lụa màu xanh ngọc", "Áo blouse màu xanh ngọc tươi mát.",
                catAoNu, "Elise", ProductType.ITEM, (short) 8, 210000L, "S", "mint",
                imgs("mint-blouse-1.jpg"), tags("casual", "office", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo thun tím", "Áo thun form suông màu tím thoải mái.",
                catAoNu, "H&M", ProductType.ITEM, (short) 8, 100000L, "M", "purple",
                imgs("purple-tee-1.jpg"), tags("casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo khoác gió cam", "Áo khoác gió nhẹ màu cam cực nổi bật.",
                catAoNam, "Adidas", ProductType.ITEM, (short) 8, 310000L, "L", "orange",
                imgs("orange-jacket-1.jpg"), tags("sport", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo sơ mi caro nâu", "Sơ mi flannel nỉ mỏng kẻ caro nâu.",
                catAoNam, "Uniqlo", ProductType.ITEM, (short) 8, 260000L, "L", "brown",
                imgs("brown-flannel-1.jpg"), tags("casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Áo đính kim sa vàng", "Áo nữ lấp lánh dự tiệc tối.",
                catAoNu, "Zara", ProductType.ITEM, (short) 9, 330000L, "S", "gold",
                imgs("gold-top-1.jpg"), tags("party", "formal"), ProductStatus.AVAILABLE, (short) 1);

        // ════════════════════════════════════
        // BOTTOM — Quần / Váy (6 sản phẩm)
        // ════════════════════════════════════
        createProduct(seller, "Quần âu đen ôm dáng", "Quần âu đen nam form slim fit mặc văn phòng.",
                catQuanNam, "Việt Tiến", ProductType.ITEM, (short) 9, 320000L, "32", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782442338/prm/products/taitna/file_lpynq5.webp"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Quần jean ống suông", "Quần bò xanh ống đứng nam tính năng động.",
                catQuanNam, "Levi's", ProductType.ITEM, (short) 8, 280000L, "32", "denim",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782442376/prm/products/taitna/file_cioftu.webp"), tags("casual", "date", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Quần âu xám tây", "Quần tây xám may đo chỉnh chu cho nam.",
                catQuanNam, "Owen", ProductType.ITEM, (short) 9, 310000L, "32", "gray",
                imgs("gray-pants-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Quần đũi rũ màu be", "Quần đũi nam/nữ màu be dễ mặc ngày hè.",
                catQuanVayNu, "Uniqlo", ProductType.ITEM, (short) 8, 240000L, "M", "beige",
                imgs("beige-pants-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Chân váy trắng dáng dài", "Chân váy midi chữ A màu trắng thanh lịch dịu dàng.",
                catQuanVayNu, "Zara", ProductType.ITEM, (short) 8, 260000L, "S", "white",
                imgs("white-skirt-1.jpg"), tags("casual", "date", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Quần ống rộng lụa đen", "Quần ống suông lụa nữ màu đen phối đồ sang chảnh.",
                catQuanVayNu, "H&M", ProductType.ITEM, (short) 8, 300000L, "M", "black",
                imgs("black-wideleg-1.jpg"), tags("casual", "office", "party"), ProductStatus.AVAILABLE, (short) 1);

        System.out.println("✅ " + productRepository.count() + " Example products created successfully!");
    }

    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────

    private Category getOrCreateCategory(String name, String description) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setName(name);
                    newCat.setDescription(description);
                    return categoryRepository.save(newCat);
                });
    }

    private List<String> imgs(String... urls) {
        return List.of(urls);
    }

    private List<String> tags(String... tags) {
        return List.of(tags);
    }

    private void createProduct(
            User seller,
            String title,
            String description,
            Category category,
            String brand,
            ProductType type,
            Short condition,
            Long price,
            String size,
            String color,
            List<String> images,
            List<String> aiTags,
            ProductStatus status,
            Short lifecycleGeneration
    ) {
        Product product = new Product();
        product.setSeller(seller);
        product.setTitle(title);
        product.setDescription(description);
        product.setCategory(category);
        product.setBrand(brand);
        product.setType(type);
        product.setCondition(condition);
        product.setPrice(price);
        product.setSize(size);
        product.setColor(color);
        product.setImages(images);
        product.setAiTags(aiTags);
        product.setStatus(status);
        product.setLifecycleGeneration(lifecycleGeneration);

        productRepository.save(product);
        System.out.println("✅ Product created: " + title + " (Brand: " + brand + ", Category: " + category.getName() + ")");
    }
}