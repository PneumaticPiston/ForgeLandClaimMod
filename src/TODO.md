## Environment Details
- This is a Minecraft Forge mod for 1.20.1

## General Mod Elements
- Add a creative tab that will have all blocks in the 'landclaim' namespace.
- Non guild members should not be prevented from breaking or placing blocks when inside of a chunk owned by a guild unless the chunk is marked as a "Settlement"
- Don't prevent block placement in any type of chunk claim except for settlements
- When chunks are claimed, they will be claimed in a 2x2 square of chunks
- Players will be able to join and create guilds to be able to gain benefits together.
- Instead of an item displaying guild stats, add a block that will have text displays above it. It will display the number of power points, guild name, number of active effects, the number of team members, etc. of the guild whose territory it is inside of.

## Claim Types & Utilities
- Add two types of claims: "Wilderness" (default) and "Settlement" (admin-protected, blocks breaking but allows interaction).
- Add "Dungeon" claim type, which costs 4x as much to claim.
- Operators in creative/spectator mode are not charged power points to claim a chunk group.
- Operators can claim a chunk group in the name of a guild, "Dungeon", or "Settlement" via command.
- Create a configuration file to configure the cost of different land types.
- If a chunk is set as a "Settlement", it should not spawn hostile mobs on the surface.
- "Dungeon" territories have no spawning restrictions.
- Config field for base power points a totem block gives a guild.
- Config field for maximum totem pole height (default 15 blocks).
- Server operator can create a fake stats block that displays a specific team regardless of territory.

## Claiming Details
- To claim a chunk, a guild must spend "Power Points".
- Settlement chunks cannot be bought by players.
- Chunks not connected to another owned chunk cost 2x as much.
- Disconnected chunk groups do not receive totem pole benefits.
- When a chunk is claimed, update all connected chunks that are also owned by the same guild to be marked as connected to the totem block chunk.
- When a chunk is unclaimed, iteratively mark all surrounding chunks not connected to the totem block chunk as not connected.

## Blocks
# Modify
- The totem base block should have a base amount of power that it gives to a guild that is configured in the landclaim.tom configuration file.
- Totem base blocks should have the same model and texture as a furnace both when placed and when held in the hand.
- The totem base block should have the same hardness as obsidian and require at least iron tools to be picked up.
- The Guild Stats block does not work properly. 

# Add
- Add a block to increase the checked maximum height of the totem base block.
- Add blocks that increase the maximum totem size: one using diamond blocks (+4), one using iron blocks (+2). These should not count toward the totem height.
- Add a block modifier block (see Totem Pole Details below).
- Add "Enhancer" block: enhances the effect of the next effect block below it (e.g., increases hostile mob spawn or potion effect power by 50%).
- Add "Inhibitor" block: decreases the effect of the next effect block below it (e.g., decreases hostile mob spawn or potion effect power by 50%).
- Add potion effect blocks: cauldrons filled with a potion, duration same as beacon.
- Add a block that allows the team to keep inventory within claimed land (consumed on death).
- If a block is consumed, all blocks above the air gap move down to fill the gap.

## Totem Pole Details
- The totem base block checks all operations regarding guild power and boosts.
- Blocks are read in order from bottom to top.
- Players can create totem pole base blocks in their guild's territory to gain advantages and add power points by crafting and stacking blocks.
- Maximum of 1 totem pole per guild member.
- Totem base blocks only check the 3 blocks above them for effects/boosts unless a modifier block increases the checked height.
- Three types of height-increasing blocks: diamond (+4), iron (+2), do not count toward height.
- Potion effect blocks: cauldrons with potion, duration as beacon.
- Enhancer and Inhibitor blocks modify the next effect block below them.
- If a block is consumed, blocks above move down to fill the gap.

## Items
- The Guild Statistics items should display the information for the guild the player using it is in including the guild's 'power points', what effects they get from the blocks on their totem, and anything else I am forgetting.

## Miscellaneous
- Chunks in other dimensions are not claimable.