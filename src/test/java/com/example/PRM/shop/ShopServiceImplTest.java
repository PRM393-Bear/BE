package com.example.PRM.shop;

import com.example.PRM.dto.request.shop.ShopReq;
import com.example.PRM.dto.response.ShopRes;
import com.example.PRM.entity.Shop;
import com.example.PRM.entity.User;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.ShopMapper;
import com.example.PRM.repository.ShopRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.ShopServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ShopServiceImpl}.
 * Covers createShop, updateShop (every conditional field, including the
 * BigDecimal > 0 branches for latitude/longitude), deleteShop, and getShop.
 */
@ExtendWith(MockitoExtension.class)
class ShopServiceImplTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShopMapper shopMapper;

    @Mock
    private UserDetails userDetails;

    private ShopServiceImpl shopService;

    private UUID shopId;
    private Shop shop;
    private User owner;

    @BeforeEach
    void setUp() {
        shopService = new ShopServiceImpl(shopRepository, userRepository, shopMapper);
        shopId = UUID.randomUUID();

        owner = new User();
        owner.setUserName("john.doe");

        shop = new Shop();
        shop.setId(shopId);
        shop.setOwner(owner);
    }

    // ---------------------------------------------------------------
    // createShop
    // ---------------------------------------------------------------

    @Test
    void createShop_success_setsOwnerAndSaves() {
        ShopReq req = new ShopReq();
        Shop newShop = new Shop();
        when(shopMapper.toEntity(req)).thenReturn(newShop);
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(owner));

        shopService.createShop(req, userDetails);

        assertSame(owner, newShop.getOwner());
        verify(shopRepository).save(newShop);
    }

    @Test
    void createShop_userNotFound_throwsNotFoundException() {
        ShopReq req = new ShopReq();
        Shop newShop = new Shop();
        when(shopMapper.toEntity(req)).thenReturn(newShop);
        when(userDetails.getUsername()).thenReturn("ghost");
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> shopService.createShop(req, userDetails));
        verify(shopRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // updateShop
    // ---------------------------------------------------------------

    @Test
    void updateShop_allFieldsProvided_updatesEveryField() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));

        ShopReq req = new ShopReq();
        req.setShopName("New Shop Name");
        req.setAddress("123 New Street");
        req.setDescription("A great shop");
        req.setLatitude(BigDecimal.valueOf(10.5));
        req.setLongitude(BigDecimal.valueOf(20.5));
        req.setPhone("0909123456");

        shopService.updateShop(shopId, req);

        assertEquals("New Shop Name", shop.getShopName());
        assertEquals("123 New Street", shop.getAddress());
        assertEquals("A great shop", shop.getDescription());
        assertEquals(BigDecimal.valueOf(10.5), shop.getLatitude());
        assertEquals(BigDecimal.valueOf(20.5), shop.getLongitude());
        assertEquals("0909123456", shop.getPhone());
        verify(shopRepository).save(shop);
    }

    @Test
    void updateShop_allFieldsNull_leavesShopUnchanged() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));

        ShopReq req = new ShopReq();
        // every field left null

        shopService.updateShop(shopId, req);

        assertNull(shop.getShopName());
        assertNull(shop.getAddress());
        assertNull(shop.getDescription());
        assertNull(shop.getLatitude());
        assertNull(shop.getLongitude());
        assertNull(shop.getPhone());
        verify(shopRepository).save(shop);
    }

    @Test
    void updateShop_nonPositiveLatitudeAndLongitude_notUpdated() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));

        ShopReq req = new ShopReq();
        req.setLatitude(BigDecimal.ZERO);
        req.setLongitude(BigDecimal.valueOf(-5));

        shopService.updateShop(shopId, req);

        assertNull(shop.getLatitude());
        assertNull(shop.getLongitude());
        verify(shopRepository).save(shop);
    }

    @Test
    void updateShop_notFound_throwsNotFoundException() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> shopService.updateShop(shopId, new ShopReq()));
        verify(shopRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // deleteShop
    // ---------------------------------------------------------------

    @Test
    void deleteShop_ownerMatches_deletesShop() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        when(userDetails.getUsername()).thenReturn("john.doe");

        shopService.deleteShop(shopId, userDetails);

        verify(shopRepository).delete(shop);
    }

    @Test
    void deleteShop_notOwner_throwsForbiddenException() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        when(userDetails.getUsername()).thenReturn("someone.else");

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> shopService.deleteShop(shopId, userDetails));
        assertTrue(ex.getMessage().contains("not authorized"));
        verify(shopRepository, never()).delete(any());
    }

    @Test
    void deleteShop_notFound_throwsNotFoundException() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> shopService.deleteShop(shopId, userDetails));
        verify(shopRepository, never()).delete(any());
    }

    // ---------------------------------------------------------------
    // getShop
    // ---------------------------------------------------------------

    @Test
    void getShop_success_returnsMappedResponse() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        ShopRes expected = new ShopRes();
        when(shopMapper.toResponse(shop)).thenReturn(expected);

        ShopRes result = shopService.getShop(shopId);

        assertSame(expected, result);
    }

    @Test
    void getShop_notFound_throwsNotFoundException() {
        when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> shopService.getShop(shopId));
    }
}
