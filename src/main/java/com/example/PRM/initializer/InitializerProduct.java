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

        User seller = userRepository.findByUserName("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found. Run InitialzerAdmin first."));

        // ════════════════════════════════════
        // TOP — Áo (chỉ giữ sản phẩm có URL ảnh Cloudinary thật)
        // ════════════════════════════════════
        createProduct(seller, "White Cotton T-Shirt", "Soft cotton t-shirt, comfortable everyday wear.",
                "top", ProductType.ITEM, (short) 9, 80000L, "L", "white",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782197343/prm/products/mnguyen0811/file_igmcxm.webp"), tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Oversize Tee", "Trendy oversized black t-shirt.",
                "top", ProductType.ITEM, (short) 9, 95000L, "L", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782197482/prm/products/mnguyen0811/file_e67wuz.jpg"), tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Striped Shirt", "Office-ready navy striped button-up.",
                "top", ProductType.ITEM, (short) 8, 250000L, "M", "navy",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784437974/prm/products/mnguyen/file_ti0q2k.webp"), tags("office", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Formal Shirt", "Classic white shirt for office and formal events.",
                "top", ProductType.ITEM, (short) 9, 280000L, "M", "white",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784438012/prm/products/mnguyen/file_qoyilw.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Pink Blouse", "Elegant pastel pink blouse.",
                "top", ProductType.ITEM, (short) 8, 220000L, "S", "pink",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784438045/prm/products/mnguyen/file_vremyu.jpg"), tags("casual", "date", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Linen Shirt", "Breathable linen shirt, perfect for summer.",
                "top", ProductType.ITEM, (short) 9, 260000L, "M", "beige",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784438288/prm/products/mnguyen/file_matzb8.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Formal Shirt", "Slim fit black dress shirt.",
                "top", ProductType.ITEM, (short) 9, 270000L, "L", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784438317/prm/products/mnguyen/file_tb3h3w.jpg"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Hoodie", "Comfy gray hoodie for casual days.",
                "top", ProductType.ITEM, (short) 8, 320000L, "L", "gray",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784438344/prm/products/mnguyen/file_u5rc4v.jpg"), tags("casual", "sport", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        // ════════════════════════════════════
        // BOTTOM — Quần / Váy (chỉ giữ sản phẩm có URL ảnh Cloudinary thật)
        // ════════════════════════════════════
        createProduct(seller, "Black Slim Trousers", "Slim fit black trousers for office.",
                "bottom", ProductType.ITEM, (short) 9, 320000L, "32", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782442338/prm/products/taitna/file_lpynq5.webp"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Blue Denim Jeans", "Classic straight-leg blue jeans.",
                "bottom", ProductType.ITEM, (short) 8, 280000L, "32", "denim",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782442376/prm/products/taitna/file_cioftu.webp"), tags("casual", "date", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        System.out.println("✅ 10 Example products (with real image URLs) created successfully!");
    }

    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────

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
            String categoryName,
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

        Category category = categoryRepository.findAll()
                .stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseGet(() -> {
                    Category newCat = new com.example.PRM.entity.Category();
                    newCat.setName(categoryName);
                    newCat.setDescription("Category for " + categoryName);
                    return categoryRepository.save(newCat);
                });

        Product product = new Product();
        product.setSeller(seller);
        product.setTitle(title);
        product.setDescription(description);
        product.setCategory(category);
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
        System.out.println("✅ Product created: " + title);
    }
}