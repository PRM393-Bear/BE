package com.example.PRM.product;

import com.example.PRM.dto.request.product.ProductFilterReq;
import com.example.PRM.dto.request.product.ProductReq;
import com.example.PRM.dto.response.product.ProductRes;
import com.example.PRM.entity.Category;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.event.ProductNotificationEvent;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.ProductMapper;
import com.example.PRM.repository.CategoryRepository;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.AuditLogServiceImpl;
import com.example.PRM.serviceImpl.ProductServiceImpl;
import com.example.PRM.status_enum.ProductStatus;
import com.example.PRM.status_enum.ProductType;
import com.example.PRM.util.ProductSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AuditLogServiceImpl auditLogService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private Product product;
    @Mock
    private ProductRes productRes;
    @Mock
    private ProductReq productReq;
    @Mock
    private ProductFilterReq productFilterReq;
    @Mock
    private User user;
    @Mock
    private Category category;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    private final UUID productId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final String username = "seller1";

    @BeforeEach
    void setUp() {
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMockedStatic.close();
    }

    private void mockAuthenticatedUser(String name) {
        securityContextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(name);
    }

    // ---------------------- getProductById ----------------------

    @Test
    void getProductById_found_returnsResponse() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        ProductRes result = productService.getProductById(productId);

        assertEquals(productRes, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductById_notFound_throwsNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.getProductById(productId));
        assertTrue(ex.getMessage().contains(productId.toString()));
    }

    // ---------------------- getAllProducts ----------------------

    @Test
    void getAllProducts_returnsAvailableProducts() {
        when(productRepository.findByStatus(ProductStatus.AVAILABLE)).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        List<ProductRes> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(productRes, result.get(0));
    }

    // ---------------------- createProduct ----------------------

    @Test
    void createProduct_titleNull_throwsIllegalArgumentException() {
        when(productReq.getTitle()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(productReq));
        verifyNoInteractions(userRepository);
    }

    @Test
    void createProduct_titleBlank_throwsIllegalArgumentException() {
        when(productReq.getTitle()).thenReturn("   ");

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(productReq));
    }

    @Test
    void createProduct_typeNull_throwsIllegalArgumentException() {
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(productReq));
    }

    @Test
    void createProduct_userNotFound_throwsNotFoundException() {
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(ProductType.ITEM);
        mockAuthenticatedUser(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.createProduct(productReq));
    }

    @Test
    void createProduct_withCategory_success() {
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(ProductType.ITEM);
        when(productReq.getCategoryId()).thenReturn(categoryId);
        mockAuthenticatedUser(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(productMapper.toEntity(productReq, user)).thenReturn(product);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productRes);

        ProductRes result = productService.createProduct(productReq);

        assertEquals(productRes, result);
        verify(product).setCategory(category);
        verify(product).setStatus(ProductStatus.PENDING);
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_withCategory_categoryNotFound_throwsNotFoundException() {
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(ProductType.ITEM);
        when(productReq.getCategoryId()).thenReturn(categoryId);
        mockAuthenticatedUser(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(productMapper.toEntity(productReq, user)).thenReturn(product);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.createProduct(productReq));
    }

    @Test
    void createProduct_withoutCategory_success() {
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(ProductType.ITEM);
        when(productReq.getCategoryId()).thenReturn(null);
        mockAuthenticatedUser(username);
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(productMapper.toEntity(productReq, user)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productRes);

        ProductRes result = productService.createProduct(productReq);

        assertEquals(productRes, result);
        verify(product, never()).setCategory(any());
        verify(categoryRepository, never()).findById(any());
    }

    // ---------------------- search ----------------------

    @Test
    void search_returnsFilteredProducts() {
        when(productRepository.findByCategoryNameAndPriceLessThanEqual("shoes", 100L))
                .thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        List<ProductRes> result = productService.search("shoes", 100L);

        assertEquals(1, result.size());
    }

    // ---------------------- searchProductByKeyword ----------------------

    @Test
    void searchProductByKeyword_null_returnsAvailableProducts() {
        when(productRepository.findByStatus(ProductStatus.AVAILABLE)).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        List<ProductRes> result = productService.searchProductByKeyword(null);

        assertEquals(1, result.size());
        verify(productRepository, never()).searchByKeyword(anyString(), any());
    }

    @Test
    void searchProductByKeyword_blank_returnsAvailableProducts() {
        when(productRepository.findByStatus(ProductStatus.AVAILABLE)).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        List<ProductRes> result = productService.searchProductByKeyword("   ");

        assertEquals(1, result.size());
        verify(productRepository, never()).searchByKeyword(anyString(), any());
    }

    @Test
    void searchProductByKeyword_valid_returnsMatchingProducts() {
        when(productRepository.searchByKeyword("shirt", ProductStatus.AVAILABLE))
                .thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        List<ProductRes> result = productService.searchProductByKeyword("  shirt  ");

        assertEquals(1, result.size());
        verify(productRepository).searchByKeyword("shirt", ProductStatus.AVAILABLE);
    }

    // ---------------------- updateProduct ----------------------

    @Test
    void updateProduct_notFound_throwsNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.updateProduct(productId, productReq));
    }

    @Test
    void updateProduct_forbidden_throwsForbiddenException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn("owner");
        mockAuthenticatedUser("someoneElse");

        assertThrows(ForbiddenException.class, () -> productService.updateProduct(productId, productReq));
    }

    @Test
    void updateProduct_titleNull_throwsIllegalArgumentException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(productReq.getTitle()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(productId, productReq));
    }

    @Test
    void updateProduct_titleBlank_throwsIllegalArgumentException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(productReq.getTitle()).thenReturn(" ");

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(productId, productReq));
    }

    @Test
    void updateProduct_typeNull_throwsIllegalArgumentException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(productId, productReq));
    }

    @Test
    void updateProduct_withCategory_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(ProductType.ITEM);
        when(productReq.getCategoryId()).thenReturn(categoryId);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        Short lifecycleGeneration = (short) 2;
        when(productReq.getLifecycleGeneration()).thenReturn(lifecycleGeneration);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productRes);

        ProductRes result = productService.updateProduct(productId, productReq);

        assertEquals(productRes, result);
        verify(product).setCategory(category);
        verify(product).setLifecycleGeneration(lifecycleGeneration);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_withCategory_categoryNotFound_throwsNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(ProductType.ITEM);
        when(productReq.getCategoryId()).thenReturn(categoryId);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.updateProduct(productId, productReq));
    }

    @Test
    void updateProduct_withoutCategory_andNullLifecycleGeneration_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(productReq.getTitle()).thenReturn("Shirt");
        when(productReq.getType()).thenReturn(ProductType.ITEM);
        when(productReq.getCategoryId()).thenReturn(null);
        when(productReq.getLifecycleGeneration()).thenReturn(null);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productRes);

        ProductRes result = productService.updateProduct(productId, productReq);

        assertEquals(productRes, result);
        verify(product, never()).setCategory(any());
        verify(product, never()).setLifecycleGeneration(any());
    }

    // ---------------------- getProductsByUserId ----------------------

    @Test
    void getProductsByUserId_returnsProducts() {
        when(productRepository.findBySellerUserId(userId)).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        List<ProductRes> result = productService.getProductsByUserId(userId);

        assertEquals(1, result.size());
    }

    // ---------------------- hideProduct ----------------------

    @Test
    void hideProduct_notFound_throwsNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.hideProduct(productId));
    }

    @Test
    void hideProduct_forbidden_throwsForbiddenException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn("owner");
        mockAuthenticatedUser("intruder");

        assertThrows(ForbiddenException.class, () -> productService.hideProduct(productId));
    }

    @Test
    void hideProduct_notAvailable_throwsBadRequestException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(product.getStatus()).thenReturn(ProductStatus.HIDDEN);

        assertThrows(BadRequestException.class, () -> productService.hideProduct(productId));
    }

    @Test
    void hideProduct_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(product.getStatus()).thenReturn(ProductStatus.AVAILABLE);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productRes);

        ProductRes result = productService.hideProduct(productId);

        assertEquals(productRes, result);
        verify(product).setStatus(ProductStatus.HIDDEN);
    }

    // ---------------------- filterProducts ----------------------

    @Test
    void filterProducts_sortByNull_usesNewestDefault() {
        try (MockedStatic<ProductSpecification> specMockedStatic = mockStatic(ProductSpecification.class)) {
            Specification<Product> spec = mock(Specification.class);
            specMockedStatic.when(() -> ProductSpecification.filterProducts(productFilterReq)).thenReturn(spec);
            when(productFilterReq.getSortBy()).thenReturn(null);
            when(productRepository.findAll(eq(spec), any(Sort.class))).thenReturn(List.of(product));
            when(productMapper.toResponse(product)).thenReturn(productRes);

            List<ProductRes> result = productService.filterProducts(productFilterReq);

            assertEquals(1, result.size());
        }
    }

    @Test
    void filterProducts_sortByBlank_usesNewestDefault() {
        try (MockedStatic<ProductSpecification> specMockedStatic = mockStatic(ProductSpecification.class)) {
            Specification<Product> spec = mock(Specification.class);
            specMockedStatic.when(() -> ProductSpecification.filterProducts(productFilterReq)).thenReturn(spec);
            when(productFilterReq.getSortBy()).thenReturn("   ");
            when(productRepository.findAll(eq(spec), any(Sort.class))).thenReturn(List.of(product));
            when(productMapper.toResponse(product)).thenReturn(productRes);

            List<ProductRes> result = productService.filterProducts(productFilterReq);

            assertEquals(1, result.size());
        }
    }

    @Test
    void filterProducts_sortByPriceAsc() {
        try (MockedStatic<ProductSpecification> specMockedStatic = mockStatic(ProductSpecification.class)) {
            Specification<Product> spec = mock(Specification.class);
            specMockedStatic.when(() -> ProductSpecification.filterProducts(productFilterReq)).thenReturn(spec);
            when(productFilterReq.getSortBy()).thenReturn("priceAsc");
            when(productRepository.findAll(eq(spec), any(Sort.class))).thenReturn(List.of(product));
            when(productMapper.toResponse(product)).thenReturn(productRes);

            List<ProductRes> result = productService.filterProducts(productFilterReq);

            assertEquals(1, result.size());
        }
    }

    @Test
    void filterProducts_sortByPriceDesc() {
        try (MockedStatic<ProductSpecification> specMockedStatic = mockStatic(ProductSpecification.class)) {
            Specification<Product> spec = mock(Specification.class);
            specMockedStatic.when(() -> ProductSpecification.filterProducts(productFilterReq)).thenReturn(spec);
            when(productFilterReq.getSortBy()).thenReturn("priceDesc");
            when(productRepository.findAll(eq(spec), any(Sort.class))).thenReturn(List.of(product));
            when(productMapper.toResponse(product)).thenReturn(productRes);

            List<ProductRes> result = productService.filterProducts(productFilterReq);

            assertEquals(1, result.size());
        }
    }

    @Test
    void filterProducts_sortByNewestExplicit() {
        try (MockedStatic<ProductSpecification> specMockedStatic = mockStatic(ProductSpecification.class)) {
            Specification<Product> spec = mock(Specification.class);
            specMockedStatic.when(() -> ProductSpecification.filterProducts(productFilterReq)).thenReturn(spec);
            when(productFilterReq.getSortBy()).thenReturn("newest");
            when(productRepository.findAll(eq(spec), any(Sort.class))).thenReturn(List.of(product));
            when(productMapper.toResponse(product)).thenReturn(productRes);

            List<ProductRes> result = productService.filterProducts(productFilterReq);

            assertEquals(1, result.size());
        }
    }

    @Test
    void filterProducts_sortByUnknown_usesDefaultBranch() {
        try (MockedStatic<ProductSpecification> specMockedStatic = mockStatic(ProductSpecification.class)) {
            Specification<Product> spec = mock(Specification.class);
            specMockedStatic.when(() -> ProductSpecification.filterProducts(productFilterReq)).thenReturn(spec);
            when(productFilterReq.getSortBy()).thenReturn("unknownSort");
            when(productRepository.findAll(eq(spec), any(Sort.class))).thenReturn(List.of(product));
            when(productMapper.toResponse(product)).thenReturn(productRes);

            List<ProductRes> result = productService.filterProducts(productFilterReq);

            assertEquals(1, result.size());
        }
    }

    // ---------------------- deleteProduct ----------------------

    @Test
    void deleteProduct_notFound_throwsNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        ProductRes result = productService.deleteProduct(productId);

        assertEquals(productRes, result);
        verify(productRepository).delete(product);
    }

    // ---------------------- getProductPendingStatus ----------------------

    @Test
    void getProductPendingStatus_returnsPendingProducts() {
        when(productRepository.findByStatus(ProductStatus.PENDING)).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        List<ProductRes> result = productService.getProductPendingStatus();

        assertEquals(1, result.size());
    }

    // ---------------------- approveProduct ----------------------

    @Test
    void approveProduct_notFound_throwsNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.approveProduct(productId));
    }

    @Test
    void approveProduct_success_publishesEvent() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(product.getSeller()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);
        when(product.getTitle()).thenReturn("Shirt");
        when(productMapper.toResponse(product)).thenReturn(productRes);

        productService.approveProduct(productId);

        verify(product).setStatus(ProductStatus.AVAILABLE);
        verify(product).setRejectReason(null);
        verify(eventPublisher).publishEvent(any(ProductNotificationEvent.class));
    }

    // ---------------------- rejectProduct ----------------------

    @Test
    void rejectProduct_notFound_throwsNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.rejectProduct(productId, "bad quality"));
    }

    @Test
    void rejectProduct_reasonNull_throwsIllegalArgumentException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> productService.rejectProduct(productId, null));
    }

    @Test
    void rejectProduct_reasonBlank_throwsIllegalArgumentException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> productService.rejectProduct(productId, "   "));
    }

    @Test
    void rejectProduct_success_publishesEvent() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(product.getSeller()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);
        when(product.getTitle()).thenReturn("Shirt");
        when(productMapper.toResponse(product)).thenReturn(productRes);

        productService.rejectProduct(productId, "Low quality images");

        verify(product).setStatus(ProductStatus.REJECTED);
        verify(product).setRejectReason("Low quality images");
        verify(eventPublisher).publishEvent(any(ProductNotificationEvent.class));
    }

    // ---------------------- getMyRejectedProducts ----------------------

    @Test
    void getMyRejectedProducts_returnsRejectedProducts() {
        when(userDetails.getUsername()).thenReturn(username);
        when(productRepository.findBySellerUserNameAndStatus(username, ProductStatus.REJECTED))
                .thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productRes);

        List<ProductRes> result = productService.getMyRejectedProducts(userDetails);

        assertEquals(1, result.size());
    }

    // ---------------------- unhideProduct ----------------------

    @Test
    void unhideProduct_notFound_throwsNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.unhideProduct(productId));
    }

    @Test
    void unhideProduct_forbidden_throwsForbiddenException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn("owner");
        mockAuthenticatedUser("intruder");

        assertThrows(ForbiddenException.class, () -> productService.unhideProduct(productId));
    }

    @Test
    void unhideProduct_notHidden_throwsBadRequestException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(product.getStatus()).thenReturn(ProductStatus.AVAILABLE);

        assertThrows(BadRequestException.class, () -> productService.unhideProduct(productId));
    }

    @Test
    void unhideProduct_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(user);
        when(user.getUserName()).thenReturn(username);
        mockAuthenticatedUser(username);
        when(product.getStatus()).thenReturn(ProductStatus.HIDDEN);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productRes);

        ProductRes result = productService.unhideProduct(productId);

        assertEquals(productRes, result);
        verify(product).setStatus(ProductStatus.AVAILABLE);
    }
}