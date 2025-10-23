package com.sg.nusiss.developer.service;

import com.sg.nusiss.developer.cache.DevGameStatisticsCache;
import com.sg.nusiss.developer.entity.DevGameStatistics;
import com.sg.nusiss.developer.repository.DevGameStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DevGameStatisticsSyncService {

    private final DevGameStatisticsCache cache;
    private final DevGameStatisticsRepository repository;

    /**
     * 每5分钟同步一次 Redis -> PostgreSQL
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void syncStatisticsFromRedis() {
        log.info("🚀 [SYNC] Starting Redis -> PostgreSQL sync...");

        // === 1️⃣ 获取所有 view/download keys ===
        Set<String> viewKeys = cache.getKeysByPrefix("devgame:view:");
        Set<String> downloadKeys = cache.getKeysByPrefix("devgame:download:");

        // 合并两个集合，确保所有游戏都同步
        Set<String> allGameIds = new HashSet<>();
        for (String key : viewKeys) allGameIds.add(key.replace("devgame:view:", ""));
        for (String key : downloadKeys) allGameIds.add(key.replace("devgame:download:", ""));

        if (allGameIds.isEmpty()) {
            log.info("✅ [SYNC] No Redis stats to sync.");
            return;
        }

        // === 2️⃣ 遍历同步 ===
        for (String gameId : allGameIds) {
            try {
                long viewCount = cache.getViewCount(gameId);
                long downloadCount = cache.getDownloadCount(gameId);

                Optional<DevGameStatistics> optional = repository.findByGameId(gameId);
                if (optional.isEmpty()) {
                    // 初次写入
                    DevGameStatistics stats = new DevGameStatistics(
                            UUID.randomUUID().toString(),
                            gameId,
                            (int) viewCount,
                            (int) downloadCount,
                            0.0,
                            LocalDateTime.now()
                    );
                    repository.insert(stats);
                    log.info("🆕 [SYNC] Inserted new stats for game {} (views={}, downloads={})",
                            gameId, viewCount, downloadCount);
                } else {
                    // 更新时用累加方式（防止覆盖）
                    repository.updateCounts(gameId, (int) viewCount, (int) downloadCount);
                    log.info("🔄 [SYNC] Updated stats for game {} (views={}, downloads={})",
                            gameId, viewCount, downloadCount);
                }

                // === 3️⃣ 清理 Redis 累计值，避免重复累加 ===
                cache.resetCounters(gameId);

            } catch (Exception e) {
                log.error("❌ [SYNC] Failed to sync game {}: {}", gameId, e.getMessage());
            }
        }

        log.info("✅ [SYNC] Redis sync complete, total {} games updated.", allGameIds.size());
    }
}
