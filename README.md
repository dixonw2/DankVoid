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

- Overhauled the HUD to remove superfluous information (enabled by default)
  - Legacy HUD config option added for the original
  - Credit to [Bank Storage](https://www.curseforge.com/minecraft/mc-mods/bank-storage) for inspiration
- The HUD (both new and legacy) are movable using the config screen
- Added the ability to always show tooltips (disabled by default)
  - The original /dank/null mod always displayed them
  - Holding Shift now shows all tooltips
  - Holding a key (Ctrl, Alt, etc.) will show the relevant action tooltip
- Make auto-sorting enabled by default
- Inventory Bogo Sorter compatibility
  - Show locked slots while in GUI
  - Sorting the player's inventory while the dank is open
  - Move all to dank
  - Move similar to, from dank
  - Move single to dank
  - **Note**: For right now, LCtrl and LMenu (LAlt) are still hardcoded dank actions. If you use the default keybinds for Bogo Sorter, moving similar _from_ the dank requires the slot to be selected first, and moving a single item _from_ the dank isn't possible. If the keybinds were changed to a non-hardcoded dank action, the Bogo Sorter action works.
- Added ability to toggle shift + clicking to open the GUI
  - Enabled be default

### v2.0.0

## Bug Fixes

### v1.8.0

- The dank begins to flicker after a while
- Integer overflow deletes a slot's item
- Config screen won't save values after opening it once
- Full slots won't accept and void items using the GUI
- Count tooltip shows for slots with at least 1000 items instead of 10000

### v1.9.0

- Opening the dank by sneak + clicking is finicky
  - Sneak clicking will now only open the dank if the player isn't looking at a block
  - If a player isn't looking at a block but they're close to the edge of a block where they could fall, it will only open if the player isn't moving.
- Critical RCE vulnerability
  - Credit to [jbredwards](https://github.com/p455w0rds-MC-Mods/DankNull/pull/364)

### v2.0.0

## Upgrading from /dank/null

**This mod _should be_ compatible with existing worlds and modpacks that use /dank/null, but please be sure to make a backup just in case!**

If /dank/null is installed already, simply place this mod in the mods folder and remove the DankNull jar. If a DankNull.cfg file exists, this mod will transfer the config options over to the new DankVoid.cfg file (DankNull.cfg can then be deleted).
