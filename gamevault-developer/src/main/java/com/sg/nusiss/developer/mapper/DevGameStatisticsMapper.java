package com.sg.nusiss.developer.mapper;

import com.sg.nusiss.developer.entity.DevGameStatistics;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface DevGameStatisticsMapper {

    Optional<DevGameStatistics> findByGameId(String gameId);

    void insert(DevGameStatistics stats);

    void updateCounts(String gameId, int viewIncrement, int downloadIncrement);

    void deleteByGameId(String gameId);
}
