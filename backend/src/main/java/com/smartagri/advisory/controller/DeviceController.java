package com.smartagri.advisory.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartagri.advisory.entity.DeviceToken;
import com.smartagri.advisory.repository.DeviceTokenRepository;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceController(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerDevice(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String platform = body.getOrDefault("platform", "android");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        Optional<DeviceToken> existing = deviceTokenRepository.findByToken(token);
        if (existing.isPresent()) {
            DeviceToken dt = existing.get();
            dt.setLastUsedAt(LocalDateTime.now());
            deviceTokenRepository.save(dt);
            return ResponseEntity.ok(Map.of("status", "updated", "message", "Device token refreshed"));
        }

        DeviceToken deviceToken = new DeviceToken(token, platform);
        deviceTokenRepository.save(deviceToken);
        return ResponseEntity.ok(Map.of("status", "registered", "message", "Device registered for push notifications"));
    }

    @PostMapping("/unregister")
    public ResponseEntity<Map<String, String>> unregisterDevice(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        deviceTokenRepository.findByToken(token).ifPresent(deviceTokenRepository::delete);
        return ResponseEntity.ok(Map.of("status", "unregistered", "message", "Device unregistered"));
    }
}