package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.dto.OrderDto;
import com.alikeremkol.ecommerce_backend.enums.OrderStatus;
import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.*;
import com.alikeremkol.ecommerce_backend.repository.OrderRepository;
import com.alikeremkol.ecommerce_backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private final UserService userService;


    // CRUD - C
    @Transactional
    public Order placeOrder(Long userId) throws AccessDeniedException {
        User authenticatedUser = userService.getAuthenticatedUser();
        if (!authenticatedUser.getId().equals(userId) || isAdmin(authenticatedUser)) {
            throw new AccessDeniedException("You do not have permission to place an order for this user.");
        }

        Cart cart = cartService.getCartByUserId(userId);
        Order order = createOrder(cart);
        List<OrderItem> orderItemList = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItemList));
        order.setTotalAmount(calculateTotalAmount(orderItemList));
        Order savedOrder = orderRepository.save(order);
        cart.updateTotalAmount();
        cartService.clearCart(cart.getId());
        cart.updateTotalAmount();
        return savedOrder;
    }

    private Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        return cart
                .getItems()
                .stream()
                .map(cartItem -> {

                    Product product = cartItem.getProduct();

                    if (product.getInventory() < cartItem.getQuantity()) {
                        throw new ResourceNotFoundException("Out of stock: " + product.getName());
                    }

                    product.setInventory(product.getInventory() - cartItem.getQuantity());

                    productRepository.save(product);

                    return new OrderItem(
                            order,
                            product,
                            cartItem.getQuantity(),
                            cartItem.getUnitPrice());
                })
                .toList();
    }

    // CRUD - R
    @Transactional
    public OrderDto getOrder(Long orderId) throws AccessDeniedException {
        OrderDto order = orderRepository
                .findById(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User authenticatedUser = userService.getAuthenticatedUser();
        if (!order.getUserId().equals(authenticatedUser.getId()) && !isAdmin(authenticatedUser)) {
            throw new AccessDeniedException("You do not have permission to access this order");
        }

        return order;
    }

    @Transactional
    public List<OrderDto> getUserOrders(Long userId) throws AccessDeniedException {
        User authenticatedUser = userService.getAuthenticatedUser();
        if (!authenticatedUser.getId().equals(userId) && !isAdmin(authenticatedUser)) {
            throw new AccessDeniedException("You do not have permission to access these orders.");
        }

        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }

    // CRUD - U
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, String status) throws AccessDeniedException {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User authenticatedUser = userService.getAuthenticatedUser();
        if (!order.getUser().getId().equals(authenticatedUser.getId()) && !isAdmin(authenticatedUser)) {
            throw new AccessDeniedException("You do not have permission to update this order");
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setOrderStatus(orderStatus);
            orderRepository.save(order);
            return convertToDto(order);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    // CRUD - D
    @Transactional
    public void deleteOrder(Long orderId) throws AccessDeniedException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User authenticatedUser = userService.getAuthenticatedUser();
        if (!order.getUser().getId().equals(authenticatedUser.getId()) && !isAdmin(authenticatedUser)) {
            throw new AccessDeniedException("You do not have permission to delete this order");
        }

        orderRepository.delete(order);
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList) {
        return  orderItemList
                .stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
    }

}
