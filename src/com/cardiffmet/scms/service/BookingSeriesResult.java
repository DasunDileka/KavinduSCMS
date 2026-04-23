package com.cardiffmet.scms.service;

/**
 * Result of creating a weekly recurring booking series.
 */
public record BookingSeriesResult(int createdCount, int skippedCount, String summary) {
}
