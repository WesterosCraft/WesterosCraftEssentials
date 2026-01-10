# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the mod (creates JAR in build/libs/)
./gradlew build

# Clean build artifacts
./gradlew clean

# Run Minecraft client with the mod loaded (for testing)
./gradlew runClient

# Run Minecraft server with the mod loaded
./gradlew runServer

# Generate data (datagen)
./gradlew runDatagen
```

## Project Overview

WesterosCraftEssentials is a Fabric mod for Minecraft 1.21.1 that provides server-side gameplay modifications for the WesterosCraft server. It uses mixins to modify vanilla Minecraft behavior.

## Architecture

**Entry Point**: `WesterosCraftEssentials.java` - Implements `ModInitializer`, loads config on initialization.

**Configuration System**: `config/WesterosCraftConfig.java` - JSON-based config stored at `config/westeroscraft-essentials.json`. Static fields are loaded from JSON at startup. To add a new config option:
1. Add static field to `WesterosCraftConfig`
2. Add corresponding field to inner `ConfigData` class
3. Add assignment in `load()` method

**Mixin System**: All mixins are in `westeroscraft.mixin` package and registered in `westeroscraft-essentials.mixins.json`. Mixins inject into vanilla Minecraft classes to:
- Cancel behaviors (e.g., ice melting, snow melting)
- Override survival checks (e.g., allow snow on any surface)

Pattern for adding new block behavior modifications:
1. Create mixin class in `src/main/java/westeroscraft/mixin/`
2. Register mixin in `src/main/resources/westeroscraft-essentials.mixins.json`
3. Add config option to control the behavior

## LuckPerms Integration

Optional permission system integration via `LuckPermsIntegration.java`. LuckPerms must be installed separately on the server.

```java
// Check permission (returns true if LuckPerms not installed - fail-open)
LuckPermsIntegration.hasPermission(serverPlayer, "westeroscraft.somepermission");

// Check permission (returns false if LuckPerms not installed - fail-closed)
LuckPermsIntegration.hasPermissionStrict(serverPlayer, "westeroscraft.somepermission");
```

## Key Dependencies

- Fabric Loader 0.18.4+
- Fabric API
- Minecraft 1.21.1
- Java 21
- LuckPerms API 5.4 (compileOnly - optional runtime dependency)
- Uses official Mojang mappings (not Yarn)
