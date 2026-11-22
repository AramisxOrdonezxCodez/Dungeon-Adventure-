# Dungeon Adventure

A Java dungeon exploration game developed for **TCSS 360 — Software Development**.

Explore a randomly generated maze, battle monsters, collect useful potions, and recover the four pillars of object-oriented programming before reaching the exit.

## Team Members

- Zachary Adams
- Kevin Munoz-Rivera
- Aramis Ordonez Chavez

## Game Objective

Collect all four pillars and reach the exit while your hero is still alive:

- **A** — Abstraction
- **E** — Encapsulation
- **I** — Inheritance
- **P** — Polymorphism

Each pillar is placed in a different room. Pillars never appear in the entrance or exit.

## How to Play

The following describes the intended gameplay. The player interface and complete game loop are still being connected, so playable controls are not yet available.

1. **Choose a hero.** Select a Warrior, Priestess, or Thief.
2. **Begin at the entrance.** The entrance is an empty room and is revealed when the adventure starts.
3. **Explore the dungeon.** Move north, south, east, or west through available doors. You cannot move through walls or outside the dungeon.
4. **Handle room encounters.** Rooms may contain monsters, potions, pits, and pillars.
5. **Collect the four pillars.** Explore different paths and backtrack when you reach a dead end.
6. **Reach the exit.** The intended victory condition is reaching the exit with all four pillars while alive.

## Hero Choices

| Hero | Special Ability |
|---|---|
| Warrior | Crushing Blow, a powerful attack that can deal increased damage. |
| Priestess | Healing, which restores her own health. |
| Thief | Surprise Attack, which can provide additional attacks. |

Heroes have different health, attack, and blocking statistics.

## Rooms and Encounters

The dungeon contains **25 rooms arranged in a 5×5 grid**.

- The entrance and exit contain no monsters, potions, pits, or pillars.
- Every other room receives an Ogre, Gremlin, or Skeleton.
- Healing potions, vision potions, and pits each have an independent **10% chance** of appearing in a room.
- A room may contain several features at once.
- Collected potions and pillars are removed from the room so they cannot be collected repeatedly.

### Healing Potions

A healing potion restores **5–15 hit points**, without exceeding the hero’s maximum health. Using it removes one healing potion from the inventory.

### Vision Potions

A vision potion reveals the current room and up to eight surrounding rooms, including diagonals. Fewer rooms are revealed near the dungeon’s edges and corners.

Using it removes one vision potion from the inventory. Revealed rooms remain visible.

### Pits

A pit causes **1–20 points of damage**. Its damage is randomly assigned when the room is created.

Pit damage bypasses combat blocking. The same pit can cause damage on later encounters, and the hero’s health cannot fall below zero.

## Reading the Map

Each room has a three-line text representation. Its edges show walls or doors, and its center shows its contents.

| Symbol | Meaning |
|---|---|
| `*` | Wall |
| `-` | North or south door |
| `\|` | East or west door |
| `i` | Entrance |
| `O` | Exit |
| `H` | Healing potion |
| `V` | Vision potion |
| `X` | Pit |
| `A`, `E`, `I`, `P` | Pillars |
| `M` | Multiple potion, pit, or pillar contents |
| Space | No displayed contents |
| `?` | Unrevealed room |

For example, this room contains the Polymorphism pillar and has doors in all four directions:

```text
*-*
|P|
*-*
```

The player’s map hides undiscovered rooms with question marks. Monsters do not currently have a dedicated map symbol.

## Saving and Loading

The save service supports **ten numbered save slots**. It stores the game state, including the hero, dungeon, location, collected pillars, and game phase.

The current saving rule allows saving only during exploration while the hero is alive. Player-facing save and load commands still need to be connected to the interface.

## How the Game Is Implemented

The project separates its responsibilities using a **Model–View–Controller** structure:

- **Model:** Stores game information and implements game rules.
- **View:** Displays information and receives player input.
- **Controller:** Coordinates player actions, room encounters, and game phases.

### Room

`Room` represents one location in the dungeon. It stores doors, contents, a monster, visibility, and entrance or exit status.

Doors use an `EnumSet<Direction>`, which prevents duplicate directions. Room contents are generated once during construction, so displaying a room does not change its contents.

### Dungeon and Maze Generation

`Dungeon` stores rooms in a two-dimensional array.

A private nested `MazeGenerator` uses **randomized depth-first search** to create passages:

1. Mark the current room as visited.
2. Randomly choose an unvisited neighboring room.
3. Create matching doors between both rooms.
4. Continue exploring recursively.
5. Backtrack when no unvisited neighbors remain.

This creates connected paths and dead ends. The dungeon’s dimensions remain fixed, but its passages and room contents vary between generated games.

A separate traversal then checks that every room is reachable from the entrance. If validation fails, the dungeon is regenerated.

Special-room placement removes selected positions from a list of available locations, preventing the entrance, exit, and pillars from overlapping.

### GameState

`GameState` tracks the hero’s location, current game phase, and collected pillars.

Movement checks the current room’s door, the destination’s boundaries, and the destination’s matching door before updating the position. Successful movement reveals the destination.

Collected pillars use an `EnumSet<Pillar>` to prevent duplicates. All four distinct pillars are required to complete the collection.

### Characters, Items, and Data

Hero and monster subclasses provide their respective character behavior. Inventory classes manage carried items and potion usage.

The data package contains character factories, a SQLite monster database, and a save service. The database can supply monster definitions through `MonsterFactory`; connecting that factory to dungeon generation is still pending.

Saving uses Java serialization. The save service writes to a temporary file before replacing a save slot.

### Automated Tests

JUnit tests check room behavior, dungeon generation, movement, inventory operations, potions, and pillar collection.

They also check invalid inputs and boundary conditions, including blocked movement, out-of-bounds coordinates, empty entrance and exit rooms, and pit damage that cannot be blocked.