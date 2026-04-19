package com.smartparking.dtos.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ValetEarningsResponseDTO {

    private Long   valetId;
    private String valetName;

    // Summary
    private Double totalEarningsToday;
    private Double totalEarningsThisWeek;
    private Double totalEarningsThisMonth;
    private Double totalUnpaidEarnings;    // pending payout
    private Long   totalJobsCompleted;

    // Per-job breakdown
    private List<EarningEntry> recentEarnings;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EarningEntry {
        private Long          jobId;
        private Double        jobAmount;
        private Double        valetCut;
        private Double        platformCut;
        private boolean       paid;
        private LocalDateTime earnedAt;
    }
}