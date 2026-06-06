package com.example.PRM.initializer;

import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class InitializerProduct implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public InitializerProduct(ProductRepository productRepository,
                              UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            System.out.println("ℹ️ Products already exist, skipping...");
            return;
        }

        User seller = userRepository.findByUserName("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found. Run InitialzerAdmin first."));

        createProduct(
                seller,
                "Vintage Denim Jacket",
                "Classic blue denim jacket, lightly used, great for casual wear.",
                "Jacket",
                Product.ProductType.ITEM,
                (short) 8,
                250000L,
                "M",
                "Blue",
                List.of("https://example.com/images/denim-jacket-1.jpg"),
                List.of("denim", "jacket", "vintage"),
                Product.ProductStatus.AVAILABLE,
                (short) 1
        );

        createProduct(
                seller,
                "White Cotton T-Shirt",
                "Soft cotton t-shirt, comfortable everyday wear.",
                "T-Shirt",
                Product.ProductType.ITEM,
                (short) 9,
                80000L,
                "L",
                "White",
                List.of("https://example.com/images/white-tshirt-1.jpg"),
                List.of("cotton", "tshirt", "basic"),
                Product.ProductStatus.AVAILABLE,
                (short) 1
        );

        createProduct(
                seller,
                "Summer Outfit Bundle",
                "Bundle includes t-shirt, shorts, and cap.",
                "Bundle",
                Product.ProductType.BUNDLE,
                (short) 7,
                350000L,
                "M",
                "Mixed",
                List.of(
                        "https://example.com/images/bundle-1.jpg",
                        "https://example.com/images/bundle-2.jpg"
                ),
                List.of("bundle", "summer", "outfit"),
                Product.ProductStatus.AVAILABLE,
                (short) 1
        );

        System.out.println("✅ Example products created successfully!");
    }

    private void createProduct(
            User seller,
            String title,
            String description,
            String category,
            Product.ProductType type,
            Short condition,
            Long price,
            String size,
            String color,
            List<String> images,
            List<String> aiTags,
            Product.ProductStatus status,
            Short lifecycleGeneration
    ) {
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