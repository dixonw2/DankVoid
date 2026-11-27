# v2.0.0

The rewrite

## Changes

### General

- Slots can remember items
  - Default Extraction/Placement Mode is therefore All by default
  - To remove the ghost item, right-click the empty slot

### Client

- The GUI functions more similarly to other inventories
  - Mouse Tweaks integration
  - Dragging to split items, double-clicking a partially-filled slot to pick up other similar items, etc.
- Added a contents preview
  - Could be broken in some GUIs that skip the PostText event
    - [WIP] Possibly can fix by making the preview not inline
- Overhauled tooltips
  - Can change formatting and visibility
- Customizable keys for dank actions
- [WIP] Add better localization _support_
  - [WIP] Still needs proper language files

## Bug Fixes
