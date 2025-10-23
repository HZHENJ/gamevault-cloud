package com.sg.nusiss.developer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DevGameStatistics {
    private String id;
    private String gameId;
    private int viewCount;
    private int downloadCount;
    private double rating;
    private LocalDateTime updatedAt;
}
