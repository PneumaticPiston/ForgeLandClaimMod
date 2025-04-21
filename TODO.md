# Land Claim Mod TODO List

## Required Assets
- [ ] Create texture for claim marker block
- [ ] Create model for claim marker block
- [ ] Create texture for totem pole block
- [ ] Create model for totem pole block
- [ ] Create GUI textures for team management interface

## Build Instructions
1. Install JDK 17 (required for Minecraft 1.20.1)
2. Open command prompt in project directory
3. Run:
   - Windows: `gradlew build`
   - Linux/Mac: `./gradlew build`
4. Find compiled mod JAR in `build/libs/landclaim-1.0.0.jar`

## Development Setup
1. Configure IDE for Java 17
2. Import project as Gradle project
3. Run `gradlew genEclipseRuns` (Eclipse) or `gradlew genIntellijRuns` (IntelliJ)
4. Refresh Gradle project

## Testing
- [ ] Test claim creation
- [ ] Test team management
- [ ] Test protection systems
- [ ] Test chunk claiming mechanics
- [ ] Test totem pole effects

## Code Tasks
- [ ] Fix command registration in main mod class
- [ ] Implement proper team data persistence
- [ ] Add configuration file for customizable settings
- [ ] Implement chunk claim visualization
- [ ] Add proper error messages for all command failures