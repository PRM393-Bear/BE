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
                              UserRepository userRepository,
                              CategoryRepository categoryRepository) {
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

        User seller = userRepository.findByUserName("admin111")
                .orElseThrow(() -> new RuntimeException("Admin user not found. Run InitialzerAdmin first."));

        // ════════════════════════════════════
        // TOP — Áo (25 sản phẩm)
        // ════════════════════════════════════
        createProduct(seller, "White Cotton T-Shirt", "Soft cotton t-shirt, comfortable everyday wear.",
                "top", ProductType.ITEM, (short) 9, 80000L, "L", "white",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782197343/prm/products/mnguyen0811/file_igmcxm.webp"),
                tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Oversize Tee", "Trendy oversized black t-shirt.",
                "top", ProductType.ITEM, (short) 9, 95000L, "L", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782197482/prm/products/mnguyen0811/file_e67wuz.jpg"),
                tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

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

        createProduct(seller, "Denim Jacket", "Classic blue denim jacket, lightly used.",
                "top", ProductType.ITEM, (short) 8, 250000L, "M", "denim",
                img("denim-jacket-1.jpg"), tags("casual", "outdoor", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Red Polo Shirt", "Classic red polo, smart casual look.",
                "top", ProductType.ITEM, (short) 8, 180000L, "M", "red",
                img("red-polo-1.jpg"), tags("casual", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Cream Knit Sweater", "Cozy cream sweater for chilly days.",
                "top", ProductType.ITEM, (short) 9, 290000L, "M", "cream",
                img("cream-sweater-1.jpg"), tags("casual", "date", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Light Blue Shirt", "Casual light blue button-up.",
                "top", ProductType.ITEM, (short) 8, 230000L, "M", "light-blue",
                img("lightblue-shirt-1.jpg"), tags("casual", "office", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Olive Field Jacket", "Utility-style olive jacket.",
                "top", ProductType.ITEM, (short) 8, 340000L, "L", "olive",
                img("olive-jacket-1.jpg"), tags("casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Tank Top", "Basic black tank, great for layering or gym.",
                "top", ProductType.ITEM, (short) 9, 70000L, "S", "black",
                img("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784505984/prm/products/admin111/file_gadz0u.jpg"), tags("sport", "casual", "beach"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Tank Top", "Lightweight white tank for summer.",
                "top", ProductType.ITEM, (short) 9, 70000L, "S", "white",
                img("white-tank-1.jpg"), tags("sport", "beach", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Camel Trench Coat", "Elegant camel trench coat.",
                "top", ProductType.ITEM, (short) 8, 580000L, "M", "camel",
                img("camel-trench-1.jpg"), tags("office", "formal", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Blazer", "Sharp black blazer for office and events.",
                "top", ProductType.ITEM, (short) 9, 450000L, "M", "black",
                img("black-blazer-1.jpg"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Blazer", "Smart navy blazer, versatile piece.",
                "top", ProductType.ITEM, (short) 8, 470000L, "L", "navy",
                img("navy-blazer-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Yellow Summer Shirt", "Bright yellow short-sleeve shirt.",
                "top", ProductType.ITEM, (short) 8, 190000L, "M", "yellow",
                img("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784506111/prm/products/admin111/file_mw0jkk.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Lavender Soft Sweater", "Soft pastel lavender sweater.",
                "top", ProductType.ITEM, (short) 8, 240000L, "S", "lavender",
                img("lavender-sweater-1.jpg"), tags("casual", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Mint Green Blouse", "Fresh mint green blouse.",
                "top", ProductType.ITEM, (short) 8, 210000L, "S", "mint",
                img("mint-blouse-1.jpg"), tags("casual", "office", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Purple Casual Tee", "Relaxed fit purple t-shirt.",
                "top", ProductType.ITEM, (short) 8, 100000L, "M", "purple",
                img("purple-tee-1.jpg"), tags("casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Orange Windbreaker", "Lightweight orange windbreaker jacket.",
                "top", ProductType.ITEM, (short) 8, 310000L, "L", "orange",
                img("orange-jacket-1.jpg"), tags("sport", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Brown Flannel Shirt", "Warm brown flannel shirt.",
                "top", ProductType.ITEM, (short) 8, 260000L, "L", "brown",
                img("brown-flannel-1.jpg"), tags("casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gold Sequin Top", "Sparkly gold top for parties.",
                "top", ProductType.ITEM, (short) 9, 330000L, "S", "gold",
                img("gold-top-1.jpg"), tags("party", "formal"), ProductStatus.AVAILABLE, (short) 1);

        // ════════════════════════════════════
        // BOTTOM — Quần / Váy (25 sản phẩm)
        // ════════════════════════════════════
        createProduct(seller, "Black Slim Trousers", "Slim fit black trousers for office.",
                "bottom", ProductType.ITEM, (short) 9, 320000L, "32", "black",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784506377/prm/products/admin111/file_x8i29a.jpg"),
                tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Blue Denim Jeans", "Classic straight-leg blue jeans.",
                "bottom", ProductType.ITEM, (short) 8, 280000L, "32", "denim",
                imgs("https://res.cloudinary.com/dktu0nbjx/image/upload/v1782442376/prm/products/taitna/file_cioftu.webp"),
                tags("casual", "date", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Dress Pants", "Tailored gray dress pants.",
                "bottom", ProductType.ITEM, (short) 9, 310000L, "32", "gray",
                img("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784506277/prm/products/admin111/file_mtc2n7.jp"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Linen Pants", "Relaxed beige linen pants.",
                "bottom", ProductType.ITEM, (short) 8, 240000L, "M", "beige",
                img("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784506068/prm/products/admin111/file_um3my2.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Midi Skirt", "Elegant white midi skirt.",
                "bottom", ProductType.ITEM, (short) 8, 260000L, "S", "white",
                img("white-skirt-1.jpg"), tags("casual", "date", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Wide-Leg Pants", "Flowy black wide-leg trousers.",
                "bottom", ProductType.ITEM, (short) 8, 300000L, "M", "black",
                img("black-wideleg-1.jpg"), tags("casual", "office", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Chino Pants", "Smart casual navy chinos.",
                "bottom", ProductType.ITEM, (short) 8, 270000L, "32", "navy",
                img("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784506490/prm/products/admin111/file_bavuyg.jpg"), tags("casual", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Mini Skirt", "Chic black mini skirt for nights out.",
                "bottom", ProductType.ITEM, (short) 8, 190000L, "S", "black",
                img("black-miniskirt-1.jpg"), tags("party", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Khaki Cargo Pants", "Durable khaki cargo pants.",
                "bottom", ProductType.ITEM, (short) 8, 290000L, "32", "beige",
                img("khaki-cargo-1.jpg"), tags("casual", "outdoor", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Olive Cargo Shorts", "Comfortable olive cargo shorts.",
                "bottom", ProductType.ITEM, (short) 8, 180000L, "M", "olive",
                img("olive-shorts-1.jpg"), tags("casual", "outdoor", "beach"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Denim Shorts", "Classic blue denim shorts.",
                "bottom", ProductType.ITEM, (short) 8, 150000L, "M", "denim",
                img("denim-shorts-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Joggers", "Comfy black jogger pants.",
                "bottom", ProductType.ITEM, (short) 9, 200000L, "L", "black",
                img("black-joggers-1.jpg"), tags("sport", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Sweatpants", "Soft gray sweatpants.",
                "bottom", ProductType.ITEM, (short) 8, 190000L, "L", "gray",
                img("gray-sweatpants-1.jpg"), tags("sport", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Pink Pleated Skirt", "Soft pink pleated skirt.",
                "bottom", ProductType.ITEM, (short) 8, 220000L, "S", "pink",
                img("pink-skirt-1.jpg"), tags("casual", "date", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Brown Corduroy Pants", "Vintage style brown corduroy pants.",
                "bottom", ProductType.ITEM, (short) 7, 250000L, "32", "brown",
                img("brown-corduroy-1.jpg"), tags("casual", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Leather Pants", "Sleek black faux leather pants.",
                "bottom", ProductType.ITEM, (short) 8, 380000L, "M", "black",
                img("black-leather-pants-1.jpg"), tags("party", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Cream Wide Trousers", "Flowy cream wide-leg trousers.",
                "bottom", ProductType.ITEM, (short) 8, 280000L, "M", "cream",
                img("cream-trousers-1.jpg"), tags("office", "formal", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Swim Shorts", "Quick-dry navy swim shorts.",
                "bottom", ProductType.ITEM, (short) 9, 150000L, "M", "navy",
                img("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784505900/prm/products/admin111/file_zgtcod.jpg"), tags("beach", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Linen Shorts", "Breezy white linen shorts.",
                "bottom", ProductType.ITEM, (short) 8, 170000L, "M", "white",
                img("https://res.cloudinary.com/dktu0nbjx/image/upload/v1784506154/prm/products/admin111/file_gsvuim.jpg"), tags("beach", "casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Pencil Skirt", "Office-ready gray pencil skirt.",
                "bottom", ProductType.ITEM, (short) 8, 230000L, "S", "gray",
                img("gray-pencilskirt-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Formal Skirt", "Tailored black formal skirt.",
                "bottom", ProductType.ITEM, (short) 8, 250000L, "S", "black",
                img("black-formalskirt-1.jpg"), tags("office", "formal", "wedding"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Light Blue Jeans", "Relaxed fit light blue jeans.",
                "bottom", ProductType.ITEM, (short) 8, 270000L, "32", "light-blue",
                img("lightblue-jeans-1.jpg"), tags("casual", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Culottes", "Trendy black culottes.",
                "bottom", ProductType.ITEM, (short) 8, 240000L, "M", "black",
                img("black-culottes-1.jpg"), tags("casual", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Wedding Trousers", "Elegant beige trousers for formal events.",
                "bottom", ProductType.ITEM, (short) 9, 350000L, "32", "beige",
                img("beige-formal-pants-1.jpg"), tags("wedding", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Wedding Skirt", "Sophisticated navy skirt for weddings.",
                "bottom", ProductType.ITEM, (short) 9, 320000L, "S", "navy",
                img("navy-wedding-skirt-1.jpg"), tags("wedding", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        // ════════════════════════════════════
        // SHOES — Giày (25 sản phẩm)
        // ════════════════════════════════════
        createProduct(seller, "White Sneakers", "Classic clean white sneakers.",
                "shoes", ProductType.ITEM, (short) 9, 450000L, "42", "white",
                img("white-sneakers-1.jpg"), tags("casual", "sport", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Oxford Shoes", "Polished black oxford dress shoes.",
                "shoes", ProductType.ITEM, (short) 9, 650000L, "42", "black",
                img("black-oxford-1.jpg"), tags("office", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Sandals", "Comfortable beige strap sandals.",
                "shoes", ProductType.ITEM, (short) 8, 280000L, "38", "beige",
                img("beige-sandals-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Nude Heels", "Elegant nude block heels.",
                "shoes", ProductType.ITEM, (short) 9, 480000L, "37", "beige",
                img("nude-heels-1.jpg"), tags("formal", "party", "wedding", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Slip-On Shoes", "Casual navy slip-on shoes.",
                "shoes", ProductType.ITEM, (short) 8, 380000L, "42", "navy",
                img("navy-sliponshoes-1.jpg"), tags("casual", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Heels", "Classic black pointed heels.",
                "shoes", ProductType.ITEM, (short) 9, 420000L, "37", "black",
                img("black-heels-1.jpg"), tags("formal", "party", "wedding"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Canvas Shoes", "Lightweight white canvas shoes.",
                "shoes", ProductType.ITEM, (short) 8, 250000L, "40", "white",
                img("white-canvas-1.jpg"), tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Brown Loafers", "Classic brown leather loafers.",
                "shoes", ProductType.ITEM, (short) 8, 550000L, "42", "brown",
                img("brown-loafers-1.jpg"), tags("office", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Running Shoes", "Performance black running shoes.",
                "shoes", ProductType.ITEM, (short) 9, 520000L, "42", "black",
                img("black-running-1.jpg"), tags("sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Sneakers", "Versatile gray sneakers.",
                "shoes", ProductType.ITEM, (short) 8, 400000L, "41", "gray",
                img("gray-sneakers-1.jpg"), tags("casual", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Camel Ankle Boots", "Stylish camel ankle boots.",
                "shoes", ProductType.ITEM, (short) 8, 480000L, "38", "camel",
                img("camel-boots-1.jpg"), tags("casual", "outdoor", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Combat Boots", "Edgy black combat boots.",
                "shoes", ProductType.ITEM, (short) 8, 530000L, "40", "black",
                img("black-combatboots-1.jpg"), tags("casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Flip Flops", "Simple white flip flops for the beach.",
                "shoes", ProductType.ITEM, (short) 9, 90000L, "40", "white",
                img("white-flipflops-1.jpg"), tags("beach", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Boat Shoes", "Classic navy boat shoes.",
                "shoes", ProductType.ITEM, (short) 8, 420000L, "42", "navy",
                img("navy-boatshoes-1.jpg"), tags("casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gold Strappy Sandals", "Glamorous gold strappy heels.",
                "shoes", ProductType.ITEM, (short) 8, 390000L, "37", "gold",
                img("gold-sandals-1.jpg"), tags("party", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Silver Flat Sandals", "Chic silver flat sandals.",
                "shoes", ProductType.ITEM, (short) 8, 260000L, "38", "silver",
                img("silver-sandals-1.jpg"), tags("party", "casual", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Platform Sneakers", "Trendy black platform sneakers.",
                "shoes", ProductType.ITEM, (short) 8, 460000L, "39", "black",
                img("black-platform-1.jpg"), tags("casual", "date"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Tennis Shoes", "Sporty white tennis shoes.",
                "shoes", ProductType.ITEM, (short) 9, 470000L, "41", "white",
                img("white-tennis-1.jpg"), tags("sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Espadrilles", "Summer-ready beige espadrilles.",
                "shoes", ProductType.ITEM, (short) 8, 230000L, "38", "beige",
                img("beige-espadrilles-1.jpg"), tags("beach", "casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Derby Shoes", "Refined black derby shoes.",
                "shoes", ProductType.ITEM, (short) 9, 600000L, "42", "black",
                img("black-derby-1.jpg"), tags("office", "formal", "wedding"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Brown Oxford Shoes", "Classic brown leather oxfords.",
                "shoes", ProductType.ITEM, (short) 8, 580000L, "42", "brown",
                img("brown-oxford-1.jpg"), tags("office", "formal"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Pink Sneakers", "Fun pink casual sneakers.",
                "shoes", ProductType.ITEM, (short) 8, 350000L, "37", "pink",
                img("pink-sneakers-1.jpg"), tags("casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Hiking Boots", "Durable gray hiking boots.",
                "shoes", ProductType.ITEM, (short) 8, 620000L, "42", "gray",
                img("gray-hiking-1.jpg"), tags("outdoor", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Wedge Sandals", "Elegant navy wedge sandals.",
                "shoes", ProductType.ITEM, (short) 8, 340000L, "38", "navy",
                img("navy-wedge-1.jpg"), tags("party", "date", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Wedding Heels", "Delicate white heels for weddings.",
                "shoes", ProductType.ITEM, (short) 9, 510000L, "37", "white",
                img("white-weddingheels-1.jpg"), tags("wedding", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        // ════════════════════════════════════
        // ACCESSORY — Phụ kiện (25 sản phẩm)
        // ════════════════════════════════════
        createProduct(seller, "White Canvas Tote Bag", "Spacious white canvas tote.",
                "accessory", ProductType.ITEM, (short) 9, 150000L, "ONE", "white",
                img("white-tote-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Leather Belt", "Classic black leather belt.",
                "accessory", ProductType.ITEM, (short) 9, 180000L, "ONE", "black",
                img("black-belt-1.jpg"), tags("office", "formal", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Silk Scarf", "Luxurious navy silk scarf.",
                "accessory", ProductType.ITEM, (short) 8, 140000L, "ONE", "navy",
                img("navy-scarf-1.jpg"), tags("formal", "office", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Clutch Bag", "Sleek black clutch for evenings.",
                "accessory", ProductType.ITEM, (short) 8, 320000L, "ONE", "black",
                img("black-clutch-1.jpg"), tags("party", "formal", "wedding"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Brown Leather Belt", "Genuine brown leather belt.",
                "accessory", ProductType.ITEM, (short) 8, 190000L, "ONE", "brown",
                img("brown-belt-1.jpg"), tags("casual", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Straw Hat", "Wide-brim beige straw hat.",
                "accessory", ProductType.ITEM, (short) 8, 170000L, "ONE", "beige",
                img("beige-hat-1.jpg"), tags("beach", "outdoor", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Sunglasses", "Stylish black UV sunglasses.",
                "accessory", ProductType.ITEM, (short) 9, 220000L, "ONE", "black",
                img("black-sunglasses-1.jpg"), tags("casual", "beach", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gold Statement Necklace", "Eye-catching gold necklace.",
                "accessory", ProductType.ITEM, (short) 8, 280000L, "ONE", "gold",
                img("gold-necklace-1.jpg"), tags("party", "formal", "wedding"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Silver Hoop Earrings", "Classic silver hoop earrings.",
                "accessory", ProductType.ITEM, (short) 9, 95000L, "ONE", "silver",
                img("silver-earrings-1.jpg"), tags("casual", "office", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Backpack", "Durable navy canvas backpack.",
                "accessory", ProductType.ITEM, (short) 8, 350000L, "ONE", "navy",
                img("navy-backpack-1.jpg"), tags("casual", "outdoor", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Crossbody Bag", "Compact black crossbody bag.",
                "accessory", ProductType.ITEM, (short) 8, 290000L, "ONE", "black",
                img("black-crossbody-1.jpg"), tags("casual", "date", "office"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Leather Handbag", "Elegant beige leather handbag.",
                "accessory", ProductType.ITEM, (short) 8, 420000L, "ONE", "beige",
                img("beige-handbag-1.jpg"), tags("office", "formal", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Beanie", "Warm black knit beanie.",
                "accessory", ProductType.ITEM, (short) 9, 80000L, "ONE", "black",
                img("black-beanie-1.jpg"), tags("casual", "outdoor", "sport"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Baseball Cap", "Classic white baseball cap.",
                "accessory", ProductType.ITEM, (short) 9, 110000L, "ONE", "white",
                img("white-cap-1.jpg"), tags("sport", "casual", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gold Watch", "Elegant gold-tone wristwatch.",
                "accessory", ProductType.ITEM, (short) 8, 650000L, "ONE", "gold",
                img("gold-watch-1.jpg"), tags("formal", "office", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Leather Watch", "Minimalist black leather watch.",
                "accessory", ProductType.ITEM, (short) 9, 480000L, "ONE", "black",
                img("black-watch-1.jpg"), tags("office", "formal", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Pink Mini Bag", "Cute pink mini shoulder bag.",
                "accessory", ProductType.ITEM, (short) 8, 230000L, "ONE", "pink",
                img("pink-bag-1.jpg"), tags("casual", "date", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Navy Tie", "Classic navy silk tie.",
                "accessory", ProductType.ITEM, (short) 9, 120000L, "ONE", "navy",
                img("navy-tie-1.jpg"), tags("office", "formal", "wedding"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Black Tie", "Formal black silk tie.",
                "accessory", ProductType.ITEM, (short) 9, 120000L, "ONE", "black",
                img("black-tie-1.jpg"), tags("formal", "wedding", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Brown Leather Wallet", "Compact brown leather wallet.",
                "accessory", ProductType.ITEM, (short) 8, 160000L, "ONE", "brown",
                img("brown-wallet-1.jpg"), tags("office", "casual"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "White Pearl Necklace", "Timeless white pearl necklace.",
                "accessory", ProductType.ITEM, (short) 8, 310000L, "ONE", "white",
                img("pearl-necklace-1.jpg"), tags("wedding", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Beige Wedding Clutch", "Refined beige clutch for weddings.",
                "accessory", ProductType.ITEM, (short) 8, 270000L, "ONE", "beige",
                img("beige-clutch-1.jpg"), tags("wedding", "formal", "party"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Gray Wool Scarf", "Warm gray wool scarf for winter.",
                "accessory", ProductType.ITEM, (short) 8, 150000L, "ONE", "gray",
                img("gray-scarf-1.jpg"), tags("casual", "office", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Camel Leather Gloves", "Soft camel leather gloves.",
                "accessory", ProductType.ITEM, (short) 8, 190000L, "ONE", "camel",
                img("camel-gloves-1.jpg"), tags("office", "formal", "outdoor"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Silver Bracelet", "Delicate silver chain bracelet.",
                "accessory", ProductType.ITEM, (short) 9, 130000L, "ONE", "silver",
                img("silver-bracelet-1.jpg"), tags("casual", "party", "date"), ProductStatus.AVAILABLE, (short) 1);

        // ════════════════════════════════════
        // BUNDLE — vài combo
        // ════════════════════════════════════
        createProduct(seller, "Summer Outfit Bundle", "Bundle includes t-shirt, shorts, and cap.",
                "bundle", ProductType.BUNDLE, (short) 7, 350000L, "M", "Mixed",
                imgs("https://example.com/images/bundle-1.jpg", "https://example.com/images/bundle-2.jpg"),
                tags("bundle", "summer", "outfit", "beach"), ProductStatus.AVAILABLE, (short) 1);

        createProduct(seller, "Office Look Bundle", "Bundle includes shirt, trousers, and belt.",
                "bundle", ProductType.BUNDLE, (short) 8, 520000L, "M", "Mixed",
                img("office-bundle-1.jpg"), tags("bundle", "office", "formal"),
                ProductStatus.AVAILABLE, (short) 1);

        System.out.println("✅ 100+ Example products created successfully!");
    }

    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────

    /** Wraps a single placeholder file name into a full example.com image URL. */
    private List<String> img(String fileName) {
        return List.of("https://example.com/images/" + fileName);
    }

    /** Pass-through for one or more already-complete image URLs (e.g. Cloudinary). */
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
                    Category newCat = new Category();
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