package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.AdminOrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.AdminStatsDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.AdminUserDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.DashboardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAdminService {

    AdminStatsDTO getDashboardStats();

    List<DashboardDTO.DailyStats> getChartData(int days);

    DashboardDTO getDashboard();

    Page<AdminOrderDTO> getAllOrders(Pageable pageable, String status, String search);

    AdminOrderDTO getOrderById(Long orderId);

    AdminOrderDTO updateOrderStatus(Long orderId, String status);

    void deleteOrder(Long orderId);

    Page<AdminUserDTO> getAllUsers(Pageable pageable, String search);

    AdminUserDTO getUserById(Long userId);

    AdminUserDTO toggleUserActive(Long userId);
}

