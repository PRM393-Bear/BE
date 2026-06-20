package com.example.PRM.serviceImpl;

import com.example.PRM.dto.response.WardrobeItemRes;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.entity.WardrobeItem;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.WardrobeItemMapper;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.repository.WardrobeItemRepository;
import com.example.PRM.service.WardrobeItemService;
import com.example.PRM.status_enum.AddedVia;
import com.example.PRM.status_enum.WardrobeStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WardrobeItemServiceImpl implements WardrobeItemService {
    private final UserRepository userRepository;
    private final WardrobeItemRepository wardrobeItemRepository;
    private final ProductRepository productRepository;
    private final WardrobeItemMapper wardrobeItemMapper;
    public WardrobeItemServiceImpl(UserRepository userRepository, WardrobeItemRepository wardrobeItemRepository, ProductRepository productRepository, WardrobeItemMapper wardrobeItemMapper) {
        this.userRepository = userRepository;
        this.wardrobeItemRepository = wardrobeItemRepository;
        this.productRepository = productRepository;
        this.wardrobeItemMapper = wardrobeItemMapper;
    }

    @Override
    public void createWardrobeItem(UserDetails userDetails, UUID productId) {
        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()
                -> new NotFoundException("User not found with userName: " + userDetails.getUsername()));
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new NotFoundException("Product not found with id: " + productId));
        WardrobeItem wardrobeItem = new WardrobeItem();
        wardrobeItem.setUser(user);
        wardrobeItem.setProduct(product);
        wardrobeItem.setName(product.getTitle());
        wardrobeItem.setStatus(WardrobeStatus.OWNED);
        wardrobeItem.setCategory(product.getCategory());
        wardrobeItem.setImageUrl(product.getImages().getFirst());
        wardrobeItem.setAcquiredAt(LocalDate.now());
        wardrobeItem.setAddedVia(AddedVia.PURCHASE);

        wardrobeItemRepository.save(wardrobeItem);
    }

    @Override
    public List<WardrobeItemRes> getWardrobeItems(UserDetails userDetails) {
        return wardrobeItemRepository.findAll()
                .stream()
                .map(wardrobeItemMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteWardrobeItem(UserDetails userDetails, UUID wardrobeItemId) {
        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()
                -> new NotFoundException("User not found with userName: " + userDetails.getUsername()));
        WardrobeItem wardrobeItem = wardrobeItemRepository.findById(wardrobeItemId).orElseThrow(()
                -> new NotFoundException("Wardrobe item not found with id: " + wardrobeItemId));
        if(!wardrobeItem.getUser().getUserName().equals(user.getUserName())){
            throw new IllegalArgumentException("You are not authorized to delete this wardrobe item");
        }
        wardrobeItem.setStatus(WardrobeStatus.DISPOSED);
        wardrobeItemRepository.save(wardrobeItem);
    }

    @Override
    public void updateWardrobeItem(UserDetails userDetails, UUID wardrobeItemId, WardrobeStatus status) {
        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()
                -> new NotFoundException("User not found with userName: " + userDetails.getUsername()));
        WardrobeItem wardrobeItem = wardrobeItemRepository.findById(wardrobeItemId).orElseThrow(()
                -> new NotFoundException("Wardrobe item not found with id: " + wardrobeItemId));
        wardrobeItem.setStatus(status);
        wardrobeItemRepository.save(wardrobeItem);
    }
}
