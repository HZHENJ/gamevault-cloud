package com.sg.nusiss.developer.controller.developer;

import com.sg.nusiss.developer.controller.common.AuthenticatedControllerBase;
import com.sg.nusiss.developer.dto.DevGameResponse;
import com.sg.nusiss.developer.dto.DevGameSummaryResponse;
import com.sg.nusiss.developer.entity.DeveloperProfile;
import com.sg.nusiss.developer.repository.DeveloperProfileRepository;
import com.sg.nusiss.developer.service.DevGameQueryApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/developer/devgame")
@RequiredArgsConstructor
public class DevGameQueryController extends AuthenticatedControllerBase {

    private final DevGameQueryApplicationService devGameQueryApplicationService;
    private final DeveloperProfileRepository developerProfileRepository;

    @GetMapping("/my")
    public ResponseEntity<List<DevGameSummaryResponse>> getMyGames(@AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);

        DeveloperProfile developerProfile = developerProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    DeveloperProfile newProfile = new DeveloperProfile();
                    newProfile.setId(UUID.randomUUID().toString());
                    newProfile.setUserId(userId);
                    newProfile.setProjectCount(0);
                    developerProfileRepository.save(newProfile);
                    return newProfile;
                });

        List<DevGameSummaryResponse> games =
                devGameQueryApplicationService.listDevGamesWithCover(developerProfile.getId());
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<DevGameResponse> getGame(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable(value = "gameId") String gameId) {
        extractUserId(jwt); // 保证必须登录
        DevGameResponse game = devGameQueryApplicationService.queryDevGameDetails(gameId);
        return game != null
                ? ResponseEntity.ok(game)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
