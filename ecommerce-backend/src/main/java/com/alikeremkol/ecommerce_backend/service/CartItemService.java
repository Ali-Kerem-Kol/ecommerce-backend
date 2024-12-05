package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.Cart;
import com.alikeremkol.ecommerce_backend.model.CartItem;
import com.alikeremkol.ecommerce_backend.model.Product;
import com.alikeremkol.ecommerce_backend.model.User;
import com.alikeremkol.ecommerce_backend.repository.CartItemRepository;
import com.alikeremkol.ecommerce_backend.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final CartService cartService;
    private final UserService userService;

    // CRUD - C
    public void addItemToCart(Long productId, int quantity) {
        User user = userService.getAuthenticatedUser();

        Cart cart = cartService.initializeNewCart(user);

        Product product = productService.getProductById(productId);

        if (product.getInventory() < quantity) {
            throw new ResourceNotFoundException("Out of stock: " + product.getName());
        }

        CartItem cartItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
        } else {
            if (product.getInventory() < cartItem.getQuantity() + quantity) {
                throw new ResourceNotFoundException("Out of stock: " + product.getName());
            }

            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        cartItem.setTotalPrice();

        cartItemRepository.save(cartItem);
        cart.addItem(cartItem);
        cartRepository.save(cart);
    }

    // CRUD - R
    public CartItem getCartItem(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }

    // CRUD - U
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        User user = userService.getAuthenticatedUser();
        Cart cart = cartService.getCart(cartId);

        if (!cart.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to access this cart.");
        }

        cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    item.setUnitPrice(item.getProduct().getPrice());
                    item.setTotalPrice();
                });

        BigDecimal totalAmount = cart
                .getItems()
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }

    // CRUD - D
    public void removeItemFromCart(Long cartId, Long productId) {
        User user = userService.getAuthenticatedUser();
        Cart cart = cartService.getCart(cartId);

        if (!cart.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to access this cart.");
        }

        CartItem itemToRemove = getCartItem(cartId, productId);
        cart.removeItem(itemToRemove);
        cartRepository.save(cart);
    }

    // Other functions



}
