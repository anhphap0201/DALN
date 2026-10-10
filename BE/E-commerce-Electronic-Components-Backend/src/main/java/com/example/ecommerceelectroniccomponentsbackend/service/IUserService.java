package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.request.ChangePasswordRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.RegisterRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.UserCreateRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.UserProfileRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.ChangePasswordResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.RegisterResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.UserCreateResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.UserProfileResponse;
import com.example.ecommerceelectroniccomponentsbackend.model.User;

public interface IUserService {

    UserCreateResponse createUser(UserCreateRequest request);

    RegisterResponse register(RegisterRequest request);

    ChangePasswordResponse changePassword(ChangePasswordRequest request);

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse upsertCurrentUserProfile(UserProfileRequest request);

    UserProfileResponse clearCurrentUserProfile();

    User getCurrentUser();
}

