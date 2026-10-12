package com.deadstock.marketplace.controller.api;

import com.deadstock.marketplace.dto.AdminStatsDto;
import com.deadstock.marketplace.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin API", description = "Endpoints for platform monitoring, business verification, and statistics")
public class AdminApiController {

    private final AdminService adminService;

    public AdminApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform analytics and KPIs")
    public ResponseEntity<AdminStatsDto> getPlatformStats() {
        return ResponseEntity.ok(adminService.getAdminStats());
    }

    @PutMapping("/verify/{companyId}")
    @Operation(summary = "Verify and approve business profile")
    public ResponseEntity<?> verifyBusiness(@PathVariable Long companyId) {
        try {
            adminService.verifyBusiness(companyId);
            return ResponseEntity.ok(Map.of("message", "Business verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/reject/{companyId}")
    @Operation(summary = "Reject business profile verification")
    public ResponseEntity<?> rejectBusiness(@PathVariable Long companyId) {
        try {
            adminService.rejectBusiness(companyId);
            return ResponseEntity.ok(Map.of("message", "Business verification rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
