package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.Cart;
import com.alikeremkol.ecommerce_backend.model.User;
import com.alikeremkol.ecommerce_backend.repository.CartItemRepository;
import com.alikeremkol.ecommerce_backend.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // CRUD - C


    // CRUD - R
    public Cart getCart(Long id) {
        Cart cart = cartRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);
        return cartRepository.save(cart);
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart getCartIfOwnerOrAdmin(Long cartId) throws AccessDeniedException {
        User user = getAuthenticatedUser();
        Cart cart = getCart(cartId);

        if (!cart.getUser().getId().equals(user.getId()) && !isAdmin(user)) {
            throw new AccessDeniedException("You do not have permission to access this cart.");
        }

        return cart;
    }

    // CRUD - U


    // CRUD - D
    @Transactional
    public void clearCart(Long id) {
        Cart cart = getCart(id);
        cartItemRepository.deleteAllByCartId(id);
        cart.getItems().clear();
        cartRepository.deleteById(id);
    }

    @Transactional
    public void clearCartForOwner(Long cartId) throws AccessDeniedException {
        Cart cart = getCartIfOwner(cartId);
        cartItemRepository.deleteAllByCartId(cartId);
        cart.getItems().clear();
        cartRepository.deleteById(cartId);
    }


    // Other functions
    public BigDecimal getTotalPrice(Long id) throws AccessDeniedException {
        Cart cart = getCartIfOwnerOrAdmin(id);
        return cart.getTotalAmount();
    }

    public Cart initializeNewCart(User user) {
        return Optional
                .ofNullable(getCartByUserId(user.getId()))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private boolean isAdmin(User user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Cart getCartIfOwner(Long cartId) throws AccessDeniedException {
        User user = getAuthenticatedUser();
        Cart cart = getCart(cartId);

        if (!cart.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this cart.");
        }

        return cart;
    }

}
