# v1.9.1

## Bug Fixes

- [DV] Fix quantity tooltip not showing at exactly 10000 items

# v1.9.0

## Changes

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

## Bug Fixes

- Opening the dank by sneak + clicking is finicky
  - Sneak clicking will now only open the dank if the player isn't looking at a block
  - If a player isn't looking at a block but they're close to the edge of a block where they could fall, it will only open if the player isn't moving.
- Critical RCE vulnerability
  - Credit to [jbredwards](https://github.com/p455w0rds-MC-Mods/DankNull/pull/364)
