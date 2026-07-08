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
        // TOP — Áo (25 sản phẩm)
        // ════════════════════════════════════
        createProduct(seller, "White Cotton T-Shirt", "Soft cotton t-shirt, comfortable everyday wear.",
                "top", ProductType.ITEM, (short) 9, 80000L, "L", "white",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782197343/prm/products/mnguyen0811/file_igmcxm.webp"), tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Oversize Tee", "Trendy oversized black t-shirt.",
                "top", ProductType.ITEM, (short) 9, 95000L, "L", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782197482/prm/products/mnguyen0811/file_e67wuz.jpg"), tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Striped Shirt", "Office-ready navy striped button-up.",
                "top", ProductType.ITEM, (short) 8, 250000L, "M", "navy",
                imgs("navy-shirt-1.jpg"), tags("office", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Formal Shirt", "Classic white shirt for office and formal events.",
                "top", ProductType.ITEM, (short) 9, 280000L, "M", "white",
                imgs("white-shirt-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Pink Blouse", "Elegant pastel pink blouse.",
                "top", ProductType.ITEM, (short) 8, 220000L, "S", "pink",
                imgs("pink-blouse-1.jpg"), tags("casual", "date", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Linen Shirt", "Breathable linen shirt, perfect for summer.",
                "top", ProductType.ITEM, (short) 9, 260000L, "M", "beige",
                imgs("beige-linen-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Formal Shirt", "Slim fit black dress shirt.",
                "top", ProductType.ITEM, (short) 9, 270000L, "L", "black",
                imgs("black-formal-1.jpg"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Hoodie", "Comfy gray hoodie for casual days.",
                "top", ProductType.ITEM, (short) 8, 320000L, "L", "gray",
                imgs("gray-hoodie-1.jpg"), tags("casual", "sport", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Denim Jacket", "Classic blue denim jacket, lightly used.",
                "top", ProductType.ITEM, (short) 8, 250000L, "M", "denim",
                imgs("denim-jacket-1.jpg"), tags("casual", "outdoor", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Red Polo Shirt", "Classic red polo, smart casual look.",
                "top", ProductType.ITEM, (short) 8, 180000L, "M", "red",
                imgs("red-polo-1.jpg"), tags("casual", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Cream Knit Sweater", "Cozy cream sweater for chilly days.",
                "top", ProductType.ITEM, (short) 9, 290000L, "M", "cream",
                imgs("cream-sweater-1.jpg"), tags("casual", "date", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Light Blue Shirt", "Casual light blue button-up.",
                "top", ProductType.ITEM, (short) 8, 230000L, "M", "light-blue",
                imgs("lightblue-shirt-1.jpg"), tags("casual", "office", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Olive Field Jacket", "Utility-style olive jacket.",
                "top", ProductType.ITEM, (short) 8, 340000L, "L", "olive",
                imgs("olive-jacket-1.jpg"), tags("casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Tank Top", "Basic black tank, great for layering or gym.",
                "top", ProductType.ITEM, (short) 9, 70000L, "S", "black",
                imgs("black-tank-1.jpg"), tags("sport", "casual", "beach"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Tank Top", "Lightweight white tank for summer.",
                "top", ProductType.ITEM, (short) 9, 70000L, "S", "white",
                imgs("white-tank-1.jpg"), tags("sport", "beach", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Camel Trench Coat", "Elegant camel trench coat.",
                "top", ProductType.ITEM, (short) 8, 580000L, "M", "camel",
                imgs("camel-trench-1.jpg"), tags("office", "formal", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Blazer", "Sharp black blazer for office and events.",
                "top", ProductType.ITEM, (short) 9, 450000L, "M", "black",
                imgs("black-blazer-1.jpg"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Blazer", "Smart navy blazer, versatile piece.",
                "top", ProductType.ITEM, (short) 8, 470000L, "L", "navy",
                imgs("navy-blazer-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Yellow Summer Shirt", "Bright yellow short-sleeve shirt.",
                "top", ProductType.ITEM, (short) 8, 190000L, "M", "yellow",
                imgs("yellow-shirt-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Lavender Soft Sweater", "Soft pastel lavender sweater.",
                "top", ProductType.ITEM, (short) 8, 240000L, "S", "lavender",
                imgs("lavender-sweater-1.jpg"), tags("casual", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Mint Green Blouse", "Fresh mint green blouse.",
                "top", ProductType.ITEM, (short) 8, 210000L, "S", "mint",
                imgs("mint-blouse-1.jpg"), tags("casual", "office", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Purple Casual Tee", "Relaxed fit purple t-shirt.",
                "top", ProductType.ITEM, (short) 8, 100000L, "M", "purple",
                imgs("purple-tee-1.jpg"), tags("casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Orange Windbreaker", "Lightweight orange windbreaker jacket.",
                "top", ProductType.ITEM, (short) 8, 310000L, "L", "orange",
                imgs("orange-jacket-1.jpg"), tags("sport", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Brown Flannel Shirt", "Warm brown flannel shirt.",
                "top", ProductType.ITEM, (short) 8, 260000L, "L", "brown",
                imgs("brown-flannel-1.jpg"), tags("casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gold Sequin Top", "Sparkly gold top for parties.",
                "top", ProductType.ITEM, (short) 9, 330000L, "S", "gold",
                imgs("gold-top-1.jpg"), tags("party", "formal"), ProductStatus.AVAILABLE, (short) 1);

        // ════════════════════════════════════
        // BOTTOM — Quần / Váy (25 sản phẩm)
        // ════════════════════════════════════
        createProduct(seller, "Black Slim Trousers", "Slim fit black trousers for office.",
                "bottom", ProductType.ITEM, (short) 9, 320000L, "32", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782442338/prm/products/taitna/file_lpynq5.webp"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Blue Denim Jeans", "Classic straight-leg blue jeans.",
                "bottom", ProductType.ITEM, (short) 8, 280000L, "32", "denim",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782442376/prm/products/taitna/file_cioftu.webp"), tags("casual", "date", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Dress Pants", "Tailored gray dress pants.",
                "bottom", ProductType.ITEM, (short) 9, 310000L, "32", "gray",
                imgs("gray-pants-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Linen Pants", "Relaxed beige linen pants.",
                "bottom", ProductType.ITEM, (short) 8, 240000L, "M", "beige",
                imgs("beige-pants-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Midi Skirt", "Elegant white midi skirt.",
                "bottom", ProductType.ITEM, (short) 8, 260000L, "S", "white",
                imgs("white-skirt-1.jpg"), tags("casual", "date", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Wide-Leg Pants", "Flowy black wide-leg trousers.",
                "bottom", ProductType.ITEM, (short) 8, 300000L, "M", "black",
                imgs("black-wideleg-1.jpg"), tags("casual", "office", "party"), ProductStatus.AVAILABLE, (short) 1);


        System.out.println("✅ 100+ Example products created successfully!");
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