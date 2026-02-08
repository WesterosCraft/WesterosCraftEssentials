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