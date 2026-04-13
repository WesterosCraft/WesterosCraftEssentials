# WesterosCraftEssentials

WesterosCraftEssentials is a custom Fabric mod that handles various requirements for the [WesterosCraft](https://westeroscraft.com) server


## Features
A /ptime command to allow players to set their own time without affecting the server's time

A /pweather command to allow players to set their own weather without affecting the server's weather

A registry that tracks the opening and closing of doors, gates and trapdoors. When a guest opens or closes a door, after some time, it is reset to it's original condition

A large array of mixins to prevent randomTick events:

| Config                    | Default | Notes                                                                      |
|---------------------------|---------|----------------------------------------------------------------------------|
| disableIceMelt            | true    | Disables ice melting                                                       |
| disableSnowMelt           | true    | Disables snow melt                                                         |
| snowLayerSurviveAny       | true    | Allows snow layers to be placed in places ordinarily not allowed           |
| doPreventLeafDecay        | true    | Prevents leaf decay                                                        |
| disableCropGrowth         | true    | Prevents crop growth                                                       |
| cropSurviveAny            | true    | Allows crops to be placed in places ordinarily not allowed                 |
| disableBambooSpread       | true    | Prevents bamboo spread                                                     |
| bambooSurviveAny          | true    | Allows bamboo to be placed in places ordinarily not allowed                |
| disableGrassSpread        | true    | Prevents grass spread                                                      |
| disableFluidTicking       | true    | Prevents water and lava to spread normally                                 |
| blockWitherSpawn          | true    | Prevents wither spawning                                                   |
| disableMushroomGrowFade   | true    | Prevents mushroom growth                                                   |
| mushroomSurviveAny        | true    | Allows mushrooms to be placed in places ordinarily not allowed             |
| forceAdventureMode        | true    | Enforces adventure mode on dimension changes                               |
| disableCactusGrowth       | true    | Prevents Cactus growth                                                     |
| cactusSurviveAny          | true    | Allows cactus to be placed in places ordinarily not allowed                |
| disableFallingBlocks      | true    | Prevents blocks such as sand and gravel from experiencing gravity          |
| disableFarmStomping       | true    | Prevents jumping on crops causing the farmland to dry                      |
| disableHunger             | true    | Disables hunger                                                            |
| disablePlantGrowFade      | true    | Prevents plant growth                                                      |
| blockHangingItemChanges   | true    | Prevents hanging items such as paintings and item frames from being broken |
| disableTNTExplode         | true    | Disables TNT explosions,                                                   |
| disableNetherWartGrowFade | true    | Prevents nether wart growth                                                |
| disableStemGrowFade       | true    | Prevents stem growth                                                       |
| disableSugarCaneGrowFade  | true    | Prevents sugar cane growth                                                 |
| sugarCaneSurviveAny       | true    | Allows sugar cane to be placed in places ordinarily not allowed            |
| disableVineGrowFade       | true    | Prevents Vine growth                                                       |
| vineSurviveAny            | true    | Allows vines to be placed in places ordinarily not allowed                 |
| disableDripstoneTransfer  | true    | Prevents dripstone from drying mud and lava farming                        |

## Item Restrictions

Item restrictions allow server admins to prevent specific groups of players from using items. Rules are defined in `config/item-restrictions.json`, which is created automatically on first run with an inactive example rule.

Restrictions are checked server-side only. The file can be reloaded at runtime without a server restart using `/datapack reload`.

### LuckPerms integration

Group membership is resolved via LuckPerms. LuckPerms is an optional dependency — if it is not installed, only rules with `denied_groups: ["*"]` (wildcard) take effect; group-specific rules are silently skipped.

### Config format

```json
{
  "rules": [
    {
      "items": ["minecraft:flint_and_steel", "minecraft:lava_bucket"],
      "modes": ["use", "interact"],
      "denied_groups": ["default"],
      "allowed_groups": ["moderator", "admin"],
      "message": "You don't have permission to use this item."
    }
  ]
}
```

### Rule fields

| Field | Required | Description |
|---|---|---|
| `items` | Yes | List of namespaced item IDs the rule applies to (e.g. `"minecraft:tnt"`) |
| `modes` | Yes | Which interactions to block — see table below |
| `denied_groups` | Yes | LuckPerms group names that are blocked. Use `"*"` to deny all players |
| `allowed_groups` | No | LuckPerms group names that are **exempt**. Always checked first — overrides `denied_groups` |
| `message` | No | Message sent to the player on denial. Defaults to `"You cannot use this item."` |

### Modes

| Mode | Blocks |
|---|---|
| `use` | Right-clicking with the item in air or on an entity |
| `interact` | Right-clicking a block while holding the item |
| `attack` | Left-clicking an entity while holding the item |
| `all` | All three modes above |

Multiple modes can be combined in the list, e.g. `["use", "interact"]`.

### Multiple rules

Multiple rules can target the same item. The **first matching rule** that applies to the player is used — subsequent rules for the same item are not evaluated.

```json
{
  "rules": [
    {
      "items": ["minecraft:tnt"],
      "modes": ["all"],
      "denied_groups": ["*"],
      "allowed_groups": ["admin"],
      "message": "TNT is restricted to admins only."
    },
    {
      "items": ["minecraft:flint_and_steel"],
      "modes": ["use", "interact"],
      "denied_groups": ["default"],
      "allowed_groups": ["builder", "moderator", "admin"],
      "message": "You don't have permission to use flint and steel."
    }
  ]
}
```