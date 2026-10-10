package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.SmartLockerDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.ISmartLockerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/smart-locker")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class SmartLockerController {

    private final ISmartLockerService smartLockerService;

    @GetMapping("/lockers")
    public ResponseEntity<List<SmartLockerDTO.Locker>> getActiveLockers() {
        List<SmartLockerDTO.Locker> lockers = smartLockerService.getActiveLockers();
        return ResponseEntity.ok(lockers);
    }
}
