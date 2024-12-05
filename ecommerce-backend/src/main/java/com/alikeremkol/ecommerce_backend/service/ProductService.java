package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.dto.ImageDto;
import com.alikeremkol.ecommerce_backend.dto.ProductAddDto;
import com.alikeremkol.ecommerce_backend.dto.ProductDto;
import com.alikeremkol.ecommerce_backend.dto.ProductUpdateDto;
import com.alikeremkol.ecommerce_backend.exception.AlreadyExistsException;
import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.Category;
import com.alikeremkol.ecommerce_backend.model.Image;
import com.alikeremkol.ecommerce_backend.model.Product;
import com.alikeremkol.ecommerce_backend.repository.CategoryRepository;
import com.alikeremkol.ecommerce_backend.repository.ImageRepository;
import com.alikeremkol.ecommerce_backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;

    // CRUD - C
    public Product addProduct(ProductAddDto request) {

        if (productExists(request.getName(), request.getBrand())) {
            throw new AlreadyExistsException("Product: " + request.getBrand() + " " + request.getName() + " is already exists.");
        }

        Category category = categoryRepository
                .findByName(request.getCategory().getName())
                .orElseGet(() -> {
                    Category newCategory = new Category(request.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        request.setCategory(category);

        return productRepository.save(createProduct(request,category));
    }
    private Product createProduct(ProductAddDto request, Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category
        );
    }
    private boolean productExists(String name, String brand) {
        return productRepository.existsByNameAndBrand(name,brand);
    }

    // CRUD - R
    public Product getProductById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow((() -> new ResourceNotFoundException("Product not found")));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    public List<Product> getProductsByCategoryAndBrand(String category,String brand) {
        return productRepository.findByCategoryNameAndBrand(category,brand);
    }

    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> getProductsByBrandAndName(String brand,String name) {
        return productRepository.findByBrandAndName(brand,name);
    }

    // CRUD - U
    @Transactional
    public Product updateProduct(ProductUpdateDto request, Long productId) {
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct,request))
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }

    private Product updateExistingProduct(Product existingProduct, ProductUpdateDto request) {
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());
        Category category = categoryRepository
                .findByName(request.getCategory().getName())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        existingProduct.setCategory(category);
        return existingProduct;
    }

    // CRUD - D
    public void deleteProductById(Long id) {
        productRepository
                .findById(id)
                .ifPresentOrElse(productRepository::delete,() -> {
                    throw new ResourceNotFoundException("Product not found.");
                });
    }

    // Other functions
    public Long countProductsByBrandAndName(String brand,String name) {
        return productRepository.countByBrandAndName(brand,name);
    }



    @Transactional
    public ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream()
                .map(image -> modelMapper.map(image, ImageDto.class))
                .toList();
        productDto.setImages(imageDtos);
        return productDto;
    }

    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }

}
