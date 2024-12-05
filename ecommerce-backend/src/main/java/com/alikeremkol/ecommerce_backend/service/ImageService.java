package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.dto.ImageDto;
import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.Image;
import com.alikeremkol.ecommerce_backend.model.Product;
import com.alikeremkol.ecommerce_backend.repository.ImageRepository;
import com.alikeremkol.ecommerce_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProductService productService;

    private final ProductRepository productRepository;

    // CRUD - C
    public List<ImageDto> saveImages(List<MultipartFile> files, Long productId) {
        Product product = productService.getProductById(productId);
        List<ImageDto> savedImageDTOs = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Image image = new Image();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setImage(file.getBytes());
                image.setProduct(product);

                Image savedImage = imageRepository.save(image);

                String buildDownloadURL = "/api/v1/images/image/download/";
                String downloadURL = buildDownloadURL + savedImage.getId();
                savedImage.setDownloadURL(downloadURL);



                imageRepository.save(savedImage);

                ImageDto imageDto = new ImageDto();
                imageDto.setId(savedImage.getId());
                imageDto.setFileName(savedImage.getFileName());
                imageDto.setDownloadURL(savedImage.getDownloadURL());


                savedImageDTOs.add(imageDto);
            } catch (IOException e) {
                throw new RuntimeException("Error saving image: " + e.getMessage());
            }
        }
        return savedImageDTOs;
    }

    // CRUD - R
    public Image getImageById(Long id) {
        return imageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found."));
    }

    // CRUD - U
    public void updateImage(MultipartFile file, Long imageId) {
        Image image = getImageById(imageId);
        try {
            image.setFileName(file.getOriginalFilename());
            image.setImage(file.getBytes());
            imageRepository.save(image);
        } catch (IOException e) {
            throw new RuntimeException("Error updating image: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteImageById(Long imageId) {
        Image image = imageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException("Image not found."));

        Product product = image.getProduct();

        if (product != null) {
            product.getImages().remove(image);
            productRepository.save(product);
        }

        imageRepository.delete(image);
    }


    // Other functions



}
