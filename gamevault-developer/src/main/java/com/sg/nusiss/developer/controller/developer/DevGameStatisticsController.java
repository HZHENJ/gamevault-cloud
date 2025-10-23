package com.sg.nusiss.developer.controller.developer;

import com.sg.nusiss.developer.controller.common.AuthenticatedControllerBase;
import com.sg.nusiss.developer.dto.DevDashboardDetailedResponse;
import com.sg.nusiss.developer.service.DevGameStatisticsDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/developer/dev/statistics")
@RequiredArgsConstructor
public class DevGameStatisticsController extends AuthenticatedControllerBase {

    private final DevGameStatisticsDashboardService dashboardService;

    @GetMapping("/dashboard/me")
    public ResponseEntity<DevDashboardDetailedResponse> getMyDashboard(@AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        DevDashboardDetailedResponse response = dashboardService.getDashboardByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/{developerId}")
    public ResponseEntity<DevDashboardDetailedResponse> getDashboardSummary(@AuthenticationPrincipal Jwt jwt,
                                                                            @PathVariable(value = "developerId") String developerId) {
        extractUserId(jwt);
        DevDashboardDetailedResponse response = dashboardService.getDashboardDetails(developerId);
        return ResponseEntity.ok(response);
    }
}
