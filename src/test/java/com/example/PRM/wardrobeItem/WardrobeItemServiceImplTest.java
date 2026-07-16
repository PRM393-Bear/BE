package com.example.PRM.wardrobeItem;

import com.example.PRM.dto.response.wardrobeItem.WardrobeItemLogRes;
import com.example.PRM.dto.response.wardrobeItem.WardrobeItemRes;
import com.example.PRM.entity.Category;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.entity.WardrobeItem;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.WardrobeItemMapper;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.repository.WardrobeItemRepository;
import com.example.PRM.serviceImpl.AuditLogServiceImpl;
import com.example.PRM.serviceImpl.WardrobeItemServiceImpl;
import com.example.PRM.status_enum.WardrobeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WardrobeItemServiceImpl}.
 * Aims for 100% line/branch coverage of every public method:
 * createWardrobeItem, getWardrobeItems, deleteWardrobeItem, updateWardrobeItem.
 */
@ExtendWith(MockitoExtension.class)
class WardrobeItemServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WardrobeItemRepository wardrobeItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WardrobeItemMapper wardrobeItemMapper;

    @Mock
    private AuditLogServiceImpl auditLogService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private WardrobeItemServiceImpl wardrobeItemService;

    private User user;
    private UUID userId;
    private UUID productId;
    private UUID wardrobeItemId;
    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        wardrobeItemId = UUID.randomUUID();

        user = new User();
        user.setUserId(userId);
        user.setUserName("john.doe");

        category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("SHIRT");
        category.setDescription("Shirts and tops");

        product = new Product();
        product.setTitle("Blue Shirt");
        product.setCategory(category);
        product.setImages(List.of("http://image.url/1.png"));
    }

    // ---------------------------------------------------------------
    // createWardrobeItem
    // ---------------------------------------------------------------

    @Test
    void createWardrobeItem_success_returnsMappedResponse() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        WardrobeItemLogRes expectedRes = new WardrobeItemLogRes();
        when(wardrobeItemMapper.toWardrobeItemLogRes(any(WardrobeItem.class))).thenReturn(expectedRes);

        WardrobeItemLogRes result = wardrobeItemService.createWardrobeItem(userDetails, productId);

        assertSame(expectedRes, result);

        ArgumentCaptor<WardrobeItem> captor = ArgumentCaptor.forClass(WardrobeItem.class);
        verify(wardrobeItemRepository).save(captor.capture());
        WardrobeItem saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals(product, saved.getProduct());
        assertEquals("Blue Shirt", saved.getName());
        assertEquals(WardrobeStatus.OWNED, saved.getStatus());
        assertEquals(category, saved.getCategory());
        assertEquals("http://image.url/1.png", saved.getImageUrl());
        assertNotNull(saved.getAcquiredAt());
    }

    @Test
    void createWardrobeItem_userNotFound_throwsNotFoundException() {
        when(userDetails.getUsername()).thenReturn("ghost");
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> wardrobeItemService.createWardrobeItem(userDetails, productId));

        assertTrue(ex.getMessage().contains("User not found"));
        verify(productRepository, never()).findById(any());
        verify(wardrobeItemRepository, never()).save(any());
    }

    @Test
    void createWardrobeItem_productNotFound_throwsNotFoundException() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> wardrobeItemService.createWardrobeItem(userDetails, productId));

        assertTrue(ex.getMessage().contains("Product not found"));
        verify(wardrobeItemRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // getWardrobeItems
    // ---------------------------------------------------------------

    @Test
    void getWardrobeItems_success_returnsMappedList() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));

        WardrobeItem item1 = new WardrobeItem();
        WardrobeItem item2 = new WardrobeItem();
        when(wardrobeItemRepository.findByUser_UserId(userId)).thenReturn(List.of(item1, item2));

        WardrobeItemRes res1 = new WardrobeItemRes();
        WardrobeItemRes res2 = new WardrobeItemRes();
        when(wardrobeItemMapper.toResponse(item1)).thenReturn(res1);
        when(wardrobeItemMapper.toResponse(item2)).thenReturn(res2);

        List<WardrobeItemRes> result = wardrobeItemService.getWardrobeItems(userDetails);

        assertEquals(2, result.size());
        assertTrue(result.contains(res1));
        assertTrue(result.contains(res2));
    }

    @Test
    void getWardrobeItems_emptyList_returnsEmptyList() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(wardrobeItemRepository.findByUser_UserId(userId)).thenReturn(Collections.emptyList());

        List<WardrobeItemRes> result = wardrobeItemService.getWardrobeItems(userDetails);

        assertTrue(result.isEmpty());
    }

    @Test
    void getWardrobeItems_userNotFound_throwsNotFoundException() {
        when(userDetails.getUsername()).thenReturn("ghost");
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> wardrobeItemService.getWardrobeItems(userDetails));

        assertTrue(ex.getMessage().contains("User not found"));
        verify(wardrobeItemRepository, never()).findByUser_UserId(any());
    }

    // ---------------------------------------------------------------
    // deleteWardrobeItem
    // ---------------------------------------------------------------

    @Test
    void deleteWardrobeItem_success_setsStatusDisposed() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));

        WardrobeItem item = new WardrobeItem();
        item.setUser(user);
        when(wardrobeItemRepository.findById(wardrobeItemId)).thenReturn(Optional.of(item));

        WardrobeItemLogRes expectedRes = new WardrobeItemLogRes();
        when(wardrobeItemMapper.toWardrobeItemLogRes(item)).thenReturn(expectedRes);

        WardrobeItemLogRes result = wardrobeItemService.deleteWardrobeItem(userDetails, wardrobeItemId);

        assertSame(expectedRes, result);
        assertEquals(WardrobeStatus.DISPOSED, item.getStatus());
        verify(wardrobeItemRepository).save(item);
    }

    @Test
    void deleteWardrobeItem_userNotFound_throwsNotFoundException() {
        when(userDetails.getUsername()).thenReturn("ghost");
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> wardrobeItemService.deleteWardrobeItem(userDetails, wardrobeItemId));

        verify(wardrobeItemRepository, never()).findById(any());
        verify(wardrobeItemRepository, never()).save(any());
    }

    @Test
    void deleteWardrobeItem_itemNotFound_throwsNotFoundException() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(wardrobeItemRepository.findById(wardrobeItemId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> wardrobeItemService.deleteWardrobeItem(userDetails, wardrobeItemId));

        assertTrue(ex.getMessage().contains("Wardrobe item not found"));
        verify(wardrobeItemRepository, never()).save(any());
    }

    @Test
    void deleteWardrobeItem_notOwner_throwsIllegalArgumentException() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));

        User otherUser = new User();
        otherUser.setUserName("someone.else");

        WardrobeItem item = new WardrobeItem();
        item.setUser(otherUser);
        when(wardrobeItemRepository.findById(wardrobeItemId)).thenReturn(Optional.of(item));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> wardrobeItemService.deleteWardrobeItem(userDetails, wardrobeItemId));

        assertTrue(ex.getMessage().contains("not authorized"));
        verify(wardrobeItemRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // updateWardrobeItem
    // ---------------------------------------------------------------

    @Test
    void updateWardrobeItem_success_setsNewStatus() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));

        WardrobeItem item = new WardrobeItem();
        item.setUser(user);
        when(wardrobeItemRepository.findById(wardrobeItemId)).thenReturn(Optional.of(item));

        WardrobeItemLogRes expectedRes = new WardrobeItemLogRes();
        when(wardrobeItemMapper.toWardrobeItemLogRes(item)).thenReturn(expectedRes);

        WardrobeItemLogRes result = wardrobeItemService.updateWardrobeItem(
                userDetails, wardrobeItemId, WardrobeStatus.DONATED);

        assertSame(expectedRes, result);
        assertEquals(WardrobeStatus.DONATED, item.getStatus());
        verify(wardrobeItemRepository).save(item);
    }

    @Test
    void updateWardrobeItem_userNotFound_throwsNotFoundException() {
        when(userDetails.getUsername()).thenReturn("ghost");
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> wardrobeItemService.updateWardrobeItem(userDetails, wardrobeItemId, WardrobeStatus.DONATED));

        verify(wardrobeItemRepository, never()).findById(any());
        verify(wardrobeItemRepository, never()).save(any());
    }

    @Test
    void updateWardrobeItem_itemNotFound_throwsNotFoundException() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(wardrobeItemRepository.findById(wardrobeItemId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> wardrobeItemService.updateWardrobeItem(userDetails, wardrobeItemId, WardrobeStatus.DONATED));

        assertTrue(ex.getMessage().contains("Wardrobe item not found"));
        verify(wardrobeItemRepository, never()).save(any());
    }

    @Test
    void updateWardrobeItem_notOwner_throwsBadRequestException() {
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));

        User otherUser = new User();
        otherUser.setUserName("someone.else");

        WardrobeItem item = new WardrobeItem();
        item.setUser(otherUser);
        when(wardrobeItemRepository.findById(wardrobeItemId)).thenReturn(Optional.of(item));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> wardrobeItemService.updateWardrobeItem(userDetails, wardrobeItemId, WardrobeStatus.DONATED));

        assertTrue(ex.getMessage().contains("not authorized"));
        verify(wardrobeItemRepository, never()).save(any());
    }
}