# /dank/void

A continuation of the mod [/dank/null](https://www.curseforge.com/minecraft/mc-mods/dank-null) by p455w0rd to fix bugs and add features.

This mod requires [p455w0rd's Library](https://www.curseforge.com/minecraft/mc-mods/p455w0rds-library).

<img width="1490" height="977" alt="main_screenshot" src="https://github.com/user-attachments/assets/9c009739-3366-4b32-a985-f97200c43059" />

## Changes

### v1.8.0

- Added the ability to manually move as many items as possible to the player by holding an item and shift + double clicking a slot
- Emptying a slot will move the other items forward
- Added manual and automatic sorting
  - Auto-sorting is disabled by default
  - Sort mode is insert-order by default
    - Four modes: insertion, mod, name, quantity

### v1.9.0

- Overhauled the HUD to remove superfluous information
  - Legacy HUD config option added for the original
  - Credit to [Bank Storage](https://www.curseforge.com/minecraft/mc-mods/bank-storage) for the idea

## Bug Fixes

### v1.8.0

- The dank begins to flicker after a while
- Integer overflow deletes a slot's item
- Config screen won't save values after opening it once
- Full slots won't accept and void items using the GUI
- Count tooltip shows for slots with at least 1000 items instead of 10000

## Upgrading from /dank/null

**This mod is compatible with existing worlds and modpacks that use /dank/null, but please be sure to make a backup just in case!**

If /dank/null is installed already, simply place this mod in the mods folder and remove the DankNull jar. If a DankNull.cfg file exists, this mod will transfer the config options over to the new DankVoid.cfg file (DankNull.cfg can then be deleted).
