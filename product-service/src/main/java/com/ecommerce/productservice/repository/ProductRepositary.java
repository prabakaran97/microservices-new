package com.ecommerce.productservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.productservice.model.Product;

@Repository
public interface ProductRepositary extends MongoRepository<Product, String> {

}
