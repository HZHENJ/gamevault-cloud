package com.sg.nusiss.developer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperProfile {
    private String id;
    private String userId;
    private Integer projectCount;
}
