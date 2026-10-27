package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.entity.WarrantyClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {

    Optional<WarrantyClaim> findByClaimCode(String claimCode);

    List<WarrantyClaim> findBySerialNumberOrderByCreatedAtDesc(String serialNumber);

    List<WarrantyClaim> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    List<WarrantyClaim> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<WarrantyClaim> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<WarrantyClaim> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);
}
