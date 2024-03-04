/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.productservice.service;

import com.malyskok.productservice.dto.ProductDto;
import com.malyskok.productservice.repository.ProductRepository;
import com.malyskok.productservice.util.EntityDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    Sinks.Many<ProductDto> productSink;

    public Flux<ProductDto> getAll() {
        return productRepository
                .findAll()
                .map(EntityDtoUtil::toDto);
    }

    public Mono<ProductDto> getProductById(String id) {
        throwRandomError();
        return productRepository
                .findById(id)
                .map(EntityDtoUtil::toDto);
    }

    public Mono<ProductDto> insertProduct(Mono<ProductDto> productDtoMono) {
        return productDtoMono
                .map(EntityDtoUtil::toEntity)
                .flatMap(productRepository::insert)
                .map(EntityDtoUtil::toDto)
                .doOnNext(productSink::tryEmitNext);
    }

    public Mono<ProductDto> updateProduct(String id, Mono<ProductDto> productDtoMono) {
        return productRepository
                .findById(id)
                .flatMap(existingProduct ->
                        productDtoMono
                                .map(EntityDtoUtil::toEntity)
                                .doOnNext(toSaveProduct -> toSaveProduct.setId(existingProduct.getId())))
                .flatMap(toSaveProduct -> productRepository.save(toSaveProduct)
                        .map(EntityDtoUtil::toDto));
    }

    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id);
    }

    public Flux<ProductDto> getByPriceRange(Integer min, Integer max) {
        return productRepository.findByPriceBetween(Range.closed(min, max))
                .map(EntityDtoUtil::toDto);
    }

    private void throwRandomError() {
        int number = ThreadLocalRandom.current().nextInt(1, 10);
        if (number > 5) {
            throw new RuntimeException("smth went wrong!");
        }
    }
}