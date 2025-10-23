package com.sg.nusiss.developer.controller.developer;

import com.sg.nusiss.developer.dto.DevGameResponse;
import com.sg.nusiss.developer.service.DevGameApplicationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.util.Base64;

@RestController
@RequestMapping("/api/developer/devgame")
@RequiredArgsConstructor
public class DevGameUpdateController {

    private final DevGameApplicationService devGameApplicationService;

    @PutMapping("/update/{gameId}")
    public ResponseEntity<DevGameResponse> updateGame(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(value = "gameId") String gameId,
            @RequestParam(value = "name") String name,
            @RequestParam(value = "description") String description,
            @RequestParam(value = "releaseDate", required = false) String releaseDate,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "zip", required = false) MultipartFile zip
    ) {
        String userId = extractUserIdFromToken(authHeader);

        DevGameResponse response = devGameApplicationService.updateGame(
                userId, gameId, name, description, releaseDate, image, video, zip
        );
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 提取 JWT 并解析 userId（兼容 HS256 / RS256）
     */
    private String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKeyResolver(new SigningKeyResolverAdapter() {
                        @Override
                        public Key resolveSigningKey(JwsHeader header, Claims claims) {
                            // 根据算法动态选择密钥
                            if ("RS256".equals(header.getAlgorithm())) {
                                try (InputStream in = new FileInputStream("secrets/keys/rsa-public.pem")) {
                                    return Keys.hmacShaKeyFor(in.readAllBytes());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                // 默认走 HMAC
                                return Keys.hmacShaKeyFor(
                                        Base64.getDecoder().decode("F3NfhRgpwaw0zWvhLtGnDOHmZomVtzpdt9Js-UkDjGJYTe49kZgKjWoeNCk7VdLU5l5F_eKk5k7nrYzKX8SwA")
                                );
                            }
                        }
                    })
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object uid = claims.get("uid");
            return uid != null ? String.valueOf(uid) : claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT: " + e.getMessage());
        }
    }


}