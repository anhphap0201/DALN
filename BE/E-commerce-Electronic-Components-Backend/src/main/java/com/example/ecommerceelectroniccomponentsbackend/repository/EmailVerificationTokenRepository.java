package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.model.EmailVerificationToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends CrudRepository<EmailVerificationToken, String> {
    void deleteByEmail(String email);
    Optional<EmailVerificationToken> findByEmail(String email);
}




