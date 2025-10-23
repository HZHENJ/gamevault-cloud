package com.sg.nusiss.developer.controller.developer;

import com.sg.nusiss.developer.controller.common.AuthenticatedControllerBase;
import com.sg.nusiss.developer.dto.OperationResult;
import com.sg.nusiss.developer.service.DevGameApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/developer/devgame")
@RequiredArgsConstructor
public class DevGameDeleteController extends AuthenticatedControllerBase {

    private final DevGameApplicationService devGameApplicationService;

    @DeleteMapping("/{gameId}")
    public ResponseEntity<OperationResult> deleteGame(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable(value = "gameId") String gameId) {
        String userId = extractUserId(jwt);
        OperationResult result = devGameApplicationService.deleteGame(userId, gameId);

        return result.isSuccess()
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
}
