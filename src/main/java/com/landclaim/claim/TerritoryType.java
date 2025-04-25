package com.landclaim.claim;

public enum TerritoryType {
    WILDERNESS,     // Unclaimed territory (default)
    CLAIMED,        // Territory claimed by a guild
    SETTLEMENT,     // Admin-protected territory with restricted hostile mob spawning
    DUNGEON         // Specially designated area with no spawning restrictions, costs more to claim
}