package com.example.ecommerceelectroniccomponentsbackend.config;

import com.example.ecommerceelectroniccomponentsbackend.entity.Category;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.repository.CategoryRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Path uploadsDir = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(uploadsDir);

        List.of(
                "ArduinoUnoR3.jpg",
                "raspberry-pi-5-6.jpg",
                "cam-bien-vat-can-hong-ngoai-fm52-5.jpg",
                "61GYgJyVv5L.jpg",
                "man-hinh-lcd-oled-0-96-inch-giao-tiep-i2c-white-9w56-1.jpg",
                "dong-co-servo-sg90-180-do-rk7a-1.jpg",
                "resistores.jpg",
                "ic-logic-74ls90-dip14-1.webp",
                "nodemcu-esp32-01.webp",
                "placeholder.png"
        ).forEach(fileName -> {
            try {
                ensureAssetCopied(fileName);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to copy asset: " + fileName, e);
            }
        });

        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            addMissingVariantImages();
            return;
        }

        Category arduino = categoryRepository.save(newCategory("Arduino & Modules", "arduino-modules", "/uploads/ArduinoUnoR3.jpg"));
        Category raspberry = categoryRepository.save(newCategory("Raspberry Pi", "raspberry-pi", "/uploads/raspberry-pi-5-6.jpg"));
        Category sensor = categoryRepository.save(newCategory("Cảm biến", "cam-bien", "/uploads/cam-bien-vat-can-hong-ngoai-fm52-5.jpg"));
        Category power = categoryRepository.save(newCategory("Module nguồn", "module-nguon", "/uploads/61GYgJyVv5L.jpg"));
        Category display = categoryRepository.save(newCategory("Màn hình hiển thị", "man-hinh-hien-thi", "/uploads/man-hinh-lcd-oled-0-96-inch-giao-tiep-i2c-white-9w56-1.jpg"));
        Category motor = categoryRepository.save(newCategory("Motor & Driver", "motor-driver", "/uploads/dong-co-servo-sg90-180-do-rk7a-1.jpg"));
        Category components = categoryRepository.save(newCategory("Linh kiện điện tử", "linh-kien-dien-tu", "/uploads/resistores.jpg"));
        Category logic = categoryRepository.save(newCategory("IC Logic", "ic-logic", "/uploads/ic-logic-74ls90-dip14-1.webp"));

        productRepository.save(buildProduct(
                "Arduino Uno R3",
                "arduino-uno-r3",
                arduino,
                "ArduinoUnoR3.jpg",
                BigDecimal.valueOf(129000),
                BigDecimal.valueOf(99000),
                52,
                4.8,
                210
        ));

        productRepository.save(buildProduct(
                "Raspberry Pi 5 8GB",
                "raspberry-pi-5-8gb",
                raspberry,
                "raspberry-pi-5-6.jpg",
                BigDecimal.valueOf(2490000),
                BigDecimal.valueOf(2190000),
                18,
                4.9,
                145
        ));

        productRepository.save(buildProduct(
                "Cảm biến khoảng cách HC-SR04",
                "cam-bien-khoang-cach-hc-sr04",
                sensor,
                "cam-bien-vat-can-hong-ngoai-fm52-5.jpg",
                BigDecimal.valueOf(45000),
                BigDecimal.valueOf(32000),
                120,
                4.7,
                310
        ));

        productRepository.save(buildProduct(
                "Module nguồn buck 12V-5V 3A",
                "module-nguon-buck-12v-5v-3a",
                power,
                "61GYgJyVv5L.jpg",
                BigDecimal.valueOf(98000),
                BigDecimal.valueOf(76000),
                86,
                4.6,
                178
        ));

        productRepository.save(buildProduct(
                "Màn hình OLED 0.96 inch I2C",
                "man-hinh-oled-096-inch-i2c",
                display,
                "man-hinh-lcd-oled-0-96-inch-giao-tiep-i2c-white-9w56-1.jpg",
                BigDecimal.valueOf(180000),
                BigDecimal.valueOf(135000),
                64,
                4.8,
                200
        ));

        productRepository.save(buildProduct(
                "Servo SG90 180°",
                "servo-sg90-180-do",
                motor,
                "dong-co-servo-sg90-180-do-rk7a-1.jpg",
                BigDecimal.valueOf(95000),
                BigDecimal.valueOf(72000),
                140,
                4.5,
                260
        ));

        productRepository.save(buildProduct(
                "Điện trở 10k 1/4W",
                "dien-tro-10k-1-4w",
                components,
                "resistores.jpg",
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(1200),
                670,
                4.4,
                980
        ));

        productRepository.save(buildProduct(
                "IC Logic 74LS90 DIP-14",
                "ic-logic-74ls90-dip-14",
                logic,
                "ic-logic-74ls90-dip14-1.webp",
                BigDecimal.valueOf(35000),
                BigDecimal.valueOf(26000),
                77,
                4.7,
                130
        ));
    }

    private Category newCategory(String name, String slug, String imageUrl) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setImageUrl(imageUrl);
        category.setDescription("Danh mục demo cho dự án điện tử");
        return category;
    }

    private Product buildProduct(String name, String slug, Category category,
                                String imageFileName, BigDecimal price,
                                BigDecimal discountPrice, int inStock,
                                double avgRating, int soldQuantity) {
        Product product = new Product();
        product.setName(name);
        product.setSlug(slug);
        product.setAvgRating(avgRating);
        product.setSoldQuantity(soldQuantity);
        product.getCategories().add(category);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setVariantName("Phiên bản tiêu chuẩn");
        variant.setDescription("Sản phẩm demo dành cho website thương mại điện tử");
        variant.setSpecifications("- Chất lượng kiểm định\n- Tương thích với nhiều board và module\n- Hỗ trợ mở rộng ứng dụng");
        variant.setImageUrl("/uploads/" + imageFileName);
        variant.setPrice(price);
        variant.setDiscountPrice(discountPrice);
        variant.setInStock(inStock);
        variant.setIsAvailable(true);
        product.getVariants().add(variant);

        return product;
    }

    private void ensureAssetCopied(String fileName) throws IOException {
        Path target = Paths.get("uploads", fileName).toAbsolutePath().normalize();
        if (Files.exists(target)) {
            return;
        }

        Path source = resolveFrontendImagePath(fileName);
        if (Files.exists(source)) {
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path resolveFrontendImagePath(String fileName) {
        Path current = Paths.get("").toAbsolutePath().normalize();

        for (int i = 0; i < 8; i++) {
            Path candidate = current.resolve("frontend").resolve("public").resolve("images").resolve(fileName);
            if (Files.exists(candidate)) {
                return candidate;
            }
            if (current.getParent() == null) {
                break;
            }
            current = current.getParent();
        }

        return Paths.get("..", "..", "frontend", "public", "images", fileName)
                .toAbsolutePath()
                .normalize();
    }

    private void addMissingVariantImages() {
        Map<String, String> categoryImages = Map.of(
                "arduino-modules", "ArduinoUnoR3.jpg",
                "raspberry-pi", "raspberry-pi-5-6.jpg",
                "linh-kien-dien-tu", "resistores.jpg",
                "cam-bien", "cam-bien-vat-can-hong-ngoai-fm52-5.jpg",
                "module-nguon", "61GYgJyVv5L.jpg",
                "module-truyen-thong", "nodemcu-esp32-01.webp",
                "man-hinh-hien-thi", "man-hinh-lcd-oled-0-96-inch-giao-tiep-i2c-white-9w56-1.jpg",
                "motor-driver", "dong-co-servo-sg90-180-do-rk7a-1.jpg",
                "ic-logic", "ic-logic-74ls90-dip14-1.webp"
        );

        productRepository.findAll().forEach(product -> {
            String categorySlug = product.getCategories().stream()
                    .map(Category::getSlug)
                    .filter(categoryImages::containsKey)
                    .findFirst()
                    .orElse(null);
            String imageFileName = categorySlug == null
                    ? "placeholder.png"
                    : categoryImages.get(categorySlug);

            product.getVariants().stream()
                    .filter(variant -> variant.getImageUrl() == null || variant.getImageUrl().isBlank())
                    .forEach(variant -> variant.setImageUrl("/uploads/" + imageFileName));
        });
    }
}
