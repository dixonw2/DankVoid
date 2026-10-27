# /dank/void

A continuation of the mod [/dank/null](https://www.curseforge.com/minecraft/mc-mods/dank-null) by p455w0rd to fix bugs and add features.

This mod requires [p455w0rd's Library](https://www.curseforge.com/minecraft/mc-mods/p455w0rds-library).

![main_screenshot](https://github.com/user-attachments/assets/9c009739-3366-4b32-a985-f97200c43059)

---

## Changes

- Added the ability to manually move as many items as possible to the player by holding an item and Shift + double-clicking a slot
- Emptying a slot will move the other items forward
- Added manual and automatic sorting
  - Auto-sorting is enabled by default
  - Sort mode is insertion order by default
  - Four modes: insertion, mod, name, quantity
- Overhauled the HUD to remove superfluous information (enabled by default)
  - Legacy HUD config option added for the original
  - Credit to [Bank Storage](https://www.curseforge.com/minecraft/mc-mods/bank-storage) for inspiration
- The HUD (both new and legacy) can be moved using the config screen
- Added the ability to always show tooltips (disabled by default)
  - The original /dank/null mod always displayed them
  - Holding Shift now shows all tooltips
  - Holding a key (Ctrl, Alt, etc.) shows the relevant action tooltip
- Inventory Bogo Sorter compatibility
  - Show locked slots while in the GUI
  - Sort the player's inventory while the dank is open
  - Move all to the dank
  - Move similar to and from the dank
  - Move single to the dank
  - **Note:** For now, LCtrl and LMenu (LAlt) are still hardcoded dank actions. With Bogo Sorter's default keybinds, moving similar _from_ the dank requires the slot to be selected first, and moving a single item _from_ the dank isn't possible. Binding these Bogo Sorter actions to keys that aren't used by hardcoded dank actions allows them to work.
- Added the ability to toggle Shift + clicking to open the GUI
  - Enabled by default

## Bug Fixes

- The dank begins to flicker after a while
- Integer overflow deletes a slot's item
- Config screen won't save values after opening it once
- Full slots won't accept and void items using the interface
- Count tooltip shows for slots with at least 1000 items instead of 10000
- Opening the dank by sneak + clicking is finicky
  - Sneak clicking now only opens the dank if the player isn't looking at a block
  - If the player isn't looking at a block but is close to an edge where they could fall, it only opens if the player isn't moving
- Critical RCE vulnerability
  - Credit to [jbredwards](https://github.com/p455w0rds-MC-Mods/DankNull/pull/364)

## Upgrading from /dank/null

**This mod is compatible with existing worlds and modpacks that use /dank/null, but please make a backup just in case!**

If /dank/null is already installed, place this mod in the mods folder and remove the DankNull jar. If a DankNull.cfg file exists, this mod will transfer the config options to the new DankVoid.cfg file. DankNull.cfg can then be deleted.
