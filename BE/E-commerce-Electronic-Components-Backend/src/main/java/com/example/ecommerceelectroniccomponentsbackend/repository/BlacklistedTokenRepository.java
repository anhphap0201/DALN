package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.model.BlacklistedToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedTokenRepository extends CrudRepository<BlacklistedToken, String> {

}
