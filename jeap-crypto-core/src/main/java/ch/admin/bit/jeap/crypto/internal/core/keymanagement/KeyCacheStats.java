package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

public record KeyCacheStats(long hitCount, long missCount, long evictionCount) {}
