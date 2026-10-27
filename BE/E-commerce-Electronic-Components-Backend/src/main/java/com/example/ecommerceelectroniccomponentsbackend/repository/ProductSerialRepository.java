package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.entity.ProductSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {

    Optional<ProductSerial> findBySerialNumber(String serialNumber);

    List<ProductSerial> findByOrderCode(String orderCode);

    List<ProductSerial> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    List<ProductSerial> findByOrderItemId(Long orderItemId);

    boolean existsBySerialNumber(String serialNumber);
}
