# v1.9.1

## Bug Fixes

- Vanilla logs and Bedrock have the ore dictionary tag "railBed"
  - My best guess is that this was leftover from debugging
- Ore Dictionary mode enabled for an item won't automatically pick up and convert items nor allow them to be inserted into existing slots
  - Can bypass Ore Dictionary insertion by manually placing an item into the dank (i.e. oak wood in dank with ore dictionary enabled would normally convert all other logWood inserted into oak; can put another logWood into the dank by picking up the stack and clicking in an empty slot). If multiple items in the dank share the same ore dictionary name and one or more slots have ore dictionary enabled, then the dank will prioritize not-full slots.
- [DV] Quantity tooltip does not show at exactly 10000 items

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
