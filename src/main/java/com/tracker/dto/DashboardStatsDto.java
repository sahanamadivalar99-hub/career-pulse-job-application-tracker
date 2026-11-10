package com.tracker.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDto {
    private long totalApplications;
    private long appliedCount;
    private long inReviewCount;
    private long interviewCount;
    private long offerCount;
    private long rejectedCount;
    private long pendingRemindersCount;
    private int latestAtsScore;

    // Monthly data for chart (Month name -> Count)
    private Map<String, Long> monthlyTrend;

    // Status breakdown for donut chart
    private Map<String, Long> statusBreakdown;

    public DashboardStatsDto() {}

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getAppliedCount() {
        return appliedCount;
    }

    public void setAppliedCount(long appliedCount) {
        this.appliedCount = appliedCount;
    }

    public long getInReviewCount() {
        return inReviewCount;
    }

    public void setInReviewCount(long inReviewCount) {
        this.inReviewCount = inReviewCount;
    }

    public long getInterviewCount() {
        return interviewCount;
    }

    public void setInterviewCount(long interviewCount) {
        this.interviewCount = interviewCount;
    }

    public long getOfferCount() {
        return offerCount;
    }

    public void setOfferCount(long offerCount) {
        this.offerCount = offerCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getPendingRemindersCount() {
        return pendingRemindersCount;
    }

    public void setPendingRemindersCount(long pendingRemindersCount) {
        this.pendingRemindersCount = pendingRemindersCount;
    }

    public int getLatestAtsScore() {
        return latestAtsScore;
    }

    public void setLatestAtsScore(int latestAtsScore) {
        this.latestAtsScore = latestAtsScore;
    }

    public Map<String, Long> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(Map<String, Long> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }
}
