This mod will be a mod that allows players to claim chunks of land from the world. Other dimensions are not claimable.
Players will be able to join and create guilds to be able to gain benefits together. 
Instead of an item displaying guild stats, add a block that will have text displays above it. It will display the number of power points, guild name, number of active effects, the number of team members, e.t.c. of the guild whose territory it is inside of. 

## Utilities
- There should be two types of claims added as utilities, "Wilderness" which will be what the world is initially set to, and "Settlement" which will be added by administrators to protect certain regions from being broken, players should still be able to interact with blocks in this area. 
- Another type of claim would be "Dungeon" which will be 4 times the cost to claim. 
- When in creative or spectator mode, server operators will not be charged power points to claim a chunk group. 
- Another option for operators should appear when using the claim command which will allow them to claim a chunk group in the name of a guild or "Dungeon" or "Settlement"
- A configuration file should be created to configure the cost of the different types of land.
- If a chunk is set as a "Settlement", it should not spawn hostile mobs on the surface.
- If a chunk is a "Dungeon" territory then it should not have any spawning restritions. 
- Also in the config, there should be a field for the base amount of power points that a totem block gives a guild.
- Another configuration field should determine the maximum height a totem pole can be which will default to 15 blocks.
- A server operator can create a fake one of these blocks that is forced to display a specifc team no matter what kind of land territory they are in.  

## Claiming details
- In order to claim a chunk, a guild will need to spend "Power Points". 
- Chunks that are marked as settlements cannot be bought by players. 
- Chunks that are not connected to another chunk that the team owns, it should be 2 times the cost. 
- If a chunk group is not connected when it is claimed, it will not receive the benefits of the totem pole
- When a chunk is claimed, a function will run that will change all connect chunks owned by the claiming team that are not marked as connected to the totem block chunk will be changed to be connected
- If a chunk is unclaimed, it will iteratively mark all surrounding chunks that are not connected to the totem block chunk as being not connected

## Totem Pole Details
The totem base block will be what checks all operations regarding guild power and boosts. 
Blocks should be read in order from bottom to top. 
Players can create totem pole base blocks in a chunk within their guild's territory that will allow them to gain advantages and add power points to their guild by crafting blocks and placing them onto one of totem poles. 
A maximum of 1 totem pole per guild member will be allowed. 

# Types of Modifier Blocks
Another block to add to the totem pole would allow the team to keep their inventory while within their claimed land, this block will be consumed when the player dies.

If a block is consumed, all blocks above the air gap will be moved down to fill the gap. 
Totem base blocks will only check the blocks 3 blocks above it to give effects and other boosts unless a modifier block has the ability to increase the blocks checked. 
Three types of these blocks should be added, one that uses diamond blocks which will increase the maximum totem size by 4, one made from iron blocks that will increase it by 2.
These height blocks should not be counted toward the height of the totem block. 

Potion effect blocks should be a minecraft cauldron filled with the potion of choice with the duration being the length as the minecraft beacon. 
Another type of block should be an "Enhancer" or similarly named block. This will enhance the effect of the next effect block below it. 
If the block it is modifying effect hostile mob spawning, it should increase the number of hostile mobs that spawn. 
If the next block gives potion effects, it should increase the power level by 50 percent. 
Another type of block should be an "Inhibitor" which will decrease the effect of the next effect block below it. 
If the block it is modifying effect hostile mob spawning, it should decrease the number of hostile mobs that spawn. 
If the next block gives potion effects, it should decrease the power level by 50 percent. 

A block to increase the checked maximum height of the totem base block should be added. 
Another block modifier block should 
The totem base block should have the same hardness as obsidian and require at least iron tools to be picked up.
