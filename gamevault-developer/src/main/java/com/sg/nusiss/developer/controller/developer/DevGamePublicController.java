package com.sg.nusiss.developer.controller.developer;

import com.sg.nusiss.developer.controller.common.AuthenticatedControllerBase;
import com.sg.nusiss.developer.dto.DevGameListResponse;
import com.sg.nusiss.developer.dto.DevGameResponse;
import com.sg.nusiss.developer.dto.HotGameResponse;
import com.sg.nusiss.developer.service.DevGameQueryApplicationService;
import com.sg.nusiss.developer.service.DevGameStatisticsApplicationService;
import com.sg.nusiss.developer.service.DevGameStatisticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/developer/devgame/public")
@RequiredArgsConstructor
public class DevGamePublicController extends AuthenticatedControllerBase {

    private final DevGameQueryApplicationService devGameQueryApplicationService;
    private final DevGameStatisticsApplicationService devGameStatisticsApplicationService;
    private final DevGameStatisticsQueryService devGameStatisticsQueryService;

    /**
     * 🔒 GameHub 公共游戏列表（分页）— 需开发者登录后才能访问
     */
    @GetMapping("/all")
    public ResponseEntity<DevGameListResponse> listAllGames(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "12") int pageSize
    ) {
        String userId = extractUserId(jwt);
        System.out.println("👤 [listAllGames] Access by developer uid = " + userId);

        DevGameListResponse result = devGameQueryApplicationService.listAllGames(page, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 🔒 获取某个游戏详情（统计 + 浏览计数）
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<DevGameResponse> getPublicGameDetail(@AuthenticationPrincipal Jwt jwt,
                                                               @PathVariable(value = "gameId") String gameId) {
        String userId = extractUserId(jwt);
        System.out.println("👤 [getPublicGameDetail] Access by developer uid = " + userId);

        DevGameResponse game = devGameQueryApplicationService.queryDevGameDetails(gameId);
        if (game != null) {
            devGameStatisticsApplicationService.recordGameView(gameId);
            return ResponseEntity.ok(game);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 🔒 获取热门游戏榜单
     */
    @GetMapping("/hot")
    public ResponseEntity<List<HotGameResponse>> getHotGames(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "limit", defaultValue = "6") int limit) {
        String userId = extractUserId(jwt);
        System.out.println("👤 [getHotGames] Access by developer uid = " + userId);

        List<HotGameResponse> result = devGameStatisticsQueryService.getHotGames(limit);
        return ResponseEntity.ok(result);
    }
}
