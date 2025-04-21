package com.landclaim.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ModConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    
    public static final ForgeConfigSpec.IntValue CLAIM_COST;
    public static final ForgeConfigSpec.IntValue MAX_TEAM_SIZE;
    public static final ForgeConfigSpec.IntValue POWER_POINTS_PER_TOTEM;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_CONTINUOUS_CLAIMS;
    
    static {
        BUILDER.comment("Land Claim Mod Configuration");
        
        BUILDER.push("Territory Settings");
        CLAIM_COST = BUILDER.comment("Power points required to claim a chunk")
                           .defineInRange("claimCost", 100, 1, 10000);
        
        REQUIRE_CONTINUOUS_CLAIMS = BUILDER.comment("Whether new claims must be adjacent to existing claims")
                                         .define("requireContinuousClaims", true);
        BUILDER.pop();
        
        BUILDER.push("Team Settings");
        MAX_TEAM_SIZE = BUILDER.comment("Maximum number of players in a team")
                              .defineInRange("maxTeamSize", 10, 1, 100);
        
        POWER_POINTS_PER_TOTEM = BUILDER.comment("Power points generated per minute by each totem")
                                       .defineInRange("powerPointsPerTotem", 5, 1, 1000);
        BUILDER.pop();
    }
    
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    
    public static void register() {
        ModLoadingContext.get().registerConfig(Type.COMMON, SPEC, "landclaim.toml");
    }
    
    public static void updateClaimCost() {
        com.landclaim.command.ClaimCommand.setClaimCost(CLAIM_COST.get());
    }
}