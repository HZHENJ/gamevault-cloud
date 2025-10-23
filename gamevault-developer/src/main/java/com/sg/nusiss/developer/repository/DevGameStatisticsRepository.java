package com.sg.nusiss.developer.repository;


import com.sg.nusiss.developer.entity.DevGameStatistics;

import java.util.Optional;

public interface DevGameStatisticsRepository {
    Optional<DevGameStatistics> findByGameId(String gameId);
    void insert(DevGameStatistics stats);
    void updateCounts(String gameId, int viewIncrement, int downloadIncrement);

    void deleteByGameId(String gameId);
}
