# v1.8.0

## Changes

- Added the ability to manually move as many items as possible to the player by holding an item and shift + double clicking a slot
- Emptying a slot will move the other items forward
- Added manual and automatic sorting
  - Auto-sorting is disabled by default
  - Sort mode is insert-order by default
    - Four modes: insertion, mod, name, quantity

## Bug Fixes

- The dank begins to flicker after a while
- Integer overflow deletes a slot's item
- Config screen won't save values after opening it once
- Full slots won't accept and void items using the GUI
