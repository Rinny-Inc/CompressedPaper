# CompressedPaper vs Paper 1.7 — Full Benchmark & Change Analysis

> **Scope**: 398 patched files, 15,655 lines added, 14,146 lines removed.  
> **Method**: Static code analysis of every patch. Numbers are theoretical estimates based on algorithmic improvements, not live profiling results.  
> **Legend**: 🔴 High impact · 🟡 Medium impact · 🟢 Minor / correctness

---

## Table of Contents

1. [Entity Tracking System](#1-entity-tracking-system)
2. [DataWatcher](#2-datawatcher)
3. [World & Physics](#3-world--physics)
4. [Entity Activation (ActivationRange)](#4-entity-activation-activationrange)
5. [Chunk System](#5-chunk-system)
6. [Player Connection & Movement](#6-player-connection--movement)
7. [Player List & Join/Leave](#7-player-list--joinleave)
8. [Network & Protocol](#8-network--protocol)
9. [MinecraftServer Tick Loop](#9-minecraftserver-tick-loop)
10. [Entity Classes](#10-entity-classes)
11. [Explosion](#11-explosion)
12. [Chunk I/O & Packets](#12-chunk-io--packets)
13. [Collection & Data Structure Upgrades](#13-collection--data-structure-upgrades)
14. [Profiler & Timing Removal](#14-profiler--timing-removal)
15. [Code Quality & Micro-optimizations](#15-code-quality--micro-optimizations)
16. [Summary Table](#16-summary-table)

---

## 1. Entity Tracking System

🔴 **Highest impact category**. Scales directly with entity count and player count.

### `EntityTracker`

| What                            | Paper 1.7                               | CompressedPaper                                                    |
| ------------------------------- | --------------------------------------- | ------------------------------------------------------------------ |
| Entry storage                   | `HashSet<EntityTrackerEntry>`           | `ArrayList<EntityTrackerEntry>` with swap-pop removal              |
| Removal cost                    | O(n) hash scan                          | O(1) swap-pop                                                      |
| Player→entries index            | None — untrack player scans all entries | `IdentityHashMap<EntityPlayer, List<EntityTrackerEntry>> byPlayer` |
| Untrack player cost             | O(all entries)                          | O(entries tracked by that player)                                  |
| `track()` entity type dispatch  | 20+ `instanceof` chain per entity       | `ITrack` interface virtual dispatch                                |
| Player scan for new entities    | `world.players` list scan               | `world.performOnInRangePlayers()` chunk-based                      |
| `updatePlayers()` movement pass | Allocates `new ArrayList` every tick    | Reuses `movedPlayers` field, skips if empty                        |
| `untrackPlayer()`               | Iterates all entries, `clear()` each    | Looks up `byPlayer` map, iterates only linked entries              |
| `a(EntityPlayer, Chunk)`        | Iterator over all entries               | Index-based for loop over ArrayList                                |

### `EntityTrackerEntry`

| What                           | Paper 1.7                                   | CompressedPaper                                                       |
| ------------------------------ | ------------------------------------------- | --------------------------------------------------------------------- |
| Tracked player set             | `HashSet` + separate `HashSet freshViewers` | Single `IdentityHashMap<EntityPlayer, Boolean>` — fresh flag in value |
| `track(List)`                  | Accepts player list parameter               | `track()` — calls `performOnInRangePlayers` internally                |
| `scanPlayers(List)`            | Linear scan of full player list             | Replaced entirely by chunk-based iteration                            |
| `clear(player)`                | `contains()` then `remove()` — two hash ops | Single `remove()` with return value check                             |
| Spawn packet dispatch          | `instanceof` chain ~15 types                | `IEntitySpecificSpawnPacket` interface virtual call                   |
| Position encoding              | `MathHelper.floor(x * 32)`                  | `Math.round(x * 32)` — more accurate fixed-point                      |
| `yRot` bug                     | Set to literal `1` on rotation update       | Correctly set to computed `l` value                                   |
| DataWatcher metadata broadcast | `broadcastIncludingSelf` for all entities   | Players get health-obfuscated copy; others get normal copy            |
| Mount packets                  | Only `PacketPlayOutAttachEntity`            | Version-aware: 1.8 gets Attach, 1.9+ gets Mount packet                |

### `TrackingRange`

- Converted from `class` with `static` method to `interface` with `default` method
- `EntityTracker` implements it via `this.getEntityTrackingRange()` — one fewer class reference per call
- Returns `short` instead of `int` — narrower type for range values that never exceed 512
- Separate NPC tracking range config (`npcTrackingRange`)

---

## 2. DataWatcher

🔴 **Very high impact** — called on every entity metadata update, every tick per entity.

| What                      | Paper 1.7                                       | CompressedPaper                                                   |
| ------------------------- | ----------------------------------------------- | ----------------------------------------------------------------- |
| Storage                   | `TIntObjectHashMap` (Trove)                     | Flat `Object[32]` array                                           |
| Dirty tracking            | Map entry iteration                             | `boolean[32] dirtyMap` + single `boolean dirty`                   |
| Read lock                 | `ReentrantReadWriteLock` acquire on every read  | No lock — direct array index                                      |
| Write lock                | `ReentrantReadWriteLock` acquire on every write | No lock — direct array index                                      |
| `WatchableObject` wrapper | Per-value wrapper object allocation             | Eliminated — raw values in array                                  |
| Type lookup               | `classToId.get(object.getClass())` Trove hash   | Removed — no runtime type checking                                |
| `a()` (isDirty)           | Map size check / iteration                      | `return this.dirty` — single boolean read                         |
| `d()` (isEmpty)           | Map `containsKey` check                         | `return this.empty` — single boolean read                         |
| `e()` (reset dirty)       | Clear map entries                               | `Arrays.fill(dirtyMap, false)`                                    |
| Multi-protocol write      | Per-type dispatch inside WatchableObject        | `switch(version)` dispatching to protocol-specific MetadataWriter |

---

## 3. World & Physics

🔴 **High impact** — `World` is the most frequently executed class on the server.

### Player Lookup

- `findNearbyPlayer()` and `findNearestAttackablePlayer()`: **rewrote from linear `world.players` scan to chunk-based iteration** — iterates `chunk.playersInChunk` for chunks in a bounding box around the target position. For sparse player distributions this is dramatically faster.
- `performOnInRangePlayers(Entity, int range, Consumer<EntityPlayer>)`: new helper used throughout tracking and physics to iterate only nearby players.

### Physics Propagation

- `applyPhysics()`: added `if (captureBlockStates) return;` guard — skips physics entirely during tree/structure generation (common during world gen and `setBlock` batches).
- Both `applyPhysics()` and `b()` (directional physics) rewritten using a static `byte[][] DIRECTIONS` table instead of 6 hardcoded coordinate expressions. Eliminates repeated literal arithmetic.

### Ray Trace

- Pre-computes directional boundary values (`d0/d1/d2`) and boolean flags outside the loop instead of recomputing each step.
- Removes `Vec3D` allocation per loop iteration (`Vec3D vec3d2 = Vec3D.a(...)` removed).
- 6 `if/else` direction chains replaced with `switch (b0)`.
- Comment: "Execution time: 16,989,978ns to 4,628,769ns" — ~3.7× speedup claimed in source.

### Lighting

- `updateBrightness(EnumSkyBlock, x, y, z, Chunk)` overload added — takes an already-known chunk, avoiding re-lookup during sky light column updates.
- Sky light column loop (`t()`) now passes the chunk: `for (i1 = k; chunk != null && i1 <= l; ++i1) { updateBrightness(..., chunk); }`.

### Block Set / Notify

- `methodProfiler.a("checkLight")` / `.b()` removed around `t(i,j,k)` call in `setTypeAndData`.
- `notifyAndUpdatePhysics`: static flag check added for `isStatic` world to skip unnecessary notifications.

### Tile Entity Removal

- Pending tile entity removal list (`b`) now uses `IdentityHashMap`-backed set for `removeAll()` instead of `ArrayList.removeAll()` — O(n) instead of O(n²).

### Entity Tick Loop

- Lightning entity loop uses `Iterator` with `Iterator.remove()` instead of index-based loop with `i--` on removal.
- Chunk section Y clamped: `Math.min(15, Math.max(0, floor(locY/16)))` — prevents out-of-bounds on extreme Y positions.
- `entity.h()` → `entity.tick()` rename throughout for clarity.

### Other

- `getCubes()` entity check: list size cached to local before loop.
- `a(AxisAlignedBB)` (collision): inline ternary replaces 5-line if/else for bedrock fallback.
- Weather duration/intensity logic simplified with ternary to remove duplicate if/else branches.
- `douseFire()`: 6-case `if/else if` chain replaced with `switch`.
- `MethodProfiler` field removed from `World` entirely — was referenced in ~10 places.

---

## 4. Entity Activation (ActivationRange)

🟡 **Medium impact** — runs every tick for every entity in every loaded chunk.

| What                     | Paper 1.7                                                               | CompressedPaper                             |
| ------------------------ | ----------------------------------------------------------------------- | ------------------------------------------- |
| Chunk lookup             | `world.getWorld().isChunkLoaded(i,j)` + `world.getChunkAt(i,j)`         | Single `world.getChunkIfLoaded(i,j)`        |
| `SpigotTimings` wrappers | `entityActivationCheckTimer.startTiming()` / `stopTiming()` in hot path | Removed entirely                            |
| `checkIfActive` timings  | `checkIfActiveTimer.startTiming()` / `stopTiming()`                     | Removed entirely                            |
| `effects.size() > 0`     | `HashMap.size()` call                                                   | `!effects.isEmpty()`                        |
| `ticksLived % 4 == 0`    | Modulo division                                                         | `(ticksLived & 3) == 0` — bitwise           |
| `instanceof` casts       | Explicit `(EntityCreature)`, `(EntityAnimal)`, etc.                     | Pattern matching — no explicit cast         |
| Entity type check order  | `EntityMonster` checked after creature                                  | Monsters checked first (more common branch) |

---

## 5. Chunk System

🟡 **Medium impact** — affects entity tracking, player lookup, and I/O.

### `Chunk`

| What                                         | Paper 1.7                          | CompressedPaper                                                            |
| -------------------------------------------- | ---------------------------------- | -------------------------------------------------------------------------- |
| Tile entity map                              | `HashMap`                          | `ConcurrentHashMap` — safe for async lighting                              |
| Player tracking                              | None — had to scan `world.players` | `HashSet<EntityPlayer> playersInChunk` — O(1) membership, direct iteration |
| `areNeighborsLoaded(1)`                      | Hardcoded multi-branch switch      | Bitmask computation + `switch` expression                                  |
| `ChunkCoordIntPair`                          | Computed on demand                 | Cached as field `chunkCoords`                                              |
| ChunkMap caching (`chunkMap17`/`chunkMap18`) | Large caching block in Paper       | Removed — different serialization path used                                |

### `ChunkProviderServer`

- `LongHash.toLong(i, j)` key cached to local `final` before map ops — avoids recomputation.
- `getOrCreateChunk`, `getChunkAt`, `unloadChunk` all use single cached `key` local.
- `ChunkRegionLoader` instanceof check uses pattern matching: `if (this.chunkLoader instanceof ChunkRegionLoader crl)`.
- Neighbor notification on chunk load: `getChunkIfLoaded` loop with null check instead of multiple `getChunkAt` calls.

### `RegionFile`

- `ChunkRegionLoader.b` list promoted to field for reuse.
- Existence check: `region.chunkExists(i & 31, j & 31)` — avoids loading region file just to check.

---

## 6. Player Connection & Movement

🔴 **High impact** — called every packet, every tick per player.

### Boolean flags compacted

Paper 1.7 has multiple `boolean` fields: `checkMovement`, `processedDisconnect`, `justTeleported`, `hasMoved`, `g` (unused). CompressedPaper packs them into a single `byte flags` field with bit masks:

```java
FLAG_CHECK_MOVEMENT    = 1
FLAG_PROCESSED_DISCONNECT = 1 << 1
FLAG_DISCONNECTING     = 1 << 2
FLAG_JUST_TELEPORTED   = 1 << 3
FLAG_HAS_MOVED         = 1 << 4
```

Smaller memory footprint, single field access.

### Disconnect guard

- `disconnect()` now checks `FLAG_DISCONNECTING` at entry — duplicate disconnect calls are no-ops immediately instead of re-entering kick event logic.

### Movement handling

- `PacketPlayInFlying` handler: dead player check moved up — exits early before doing any work if player is dead.
- `!this.checkMovement` → `!getFlag(FLAG_CHECK_MOVEMENT)` — bit read instead of boolean field.
- `MethodProfiler.a("keepAlive")` removed from `a()` tick method.
- Chat throttle `AtomicIntegerFieldUpdater` CAS loop removed (paper over-engineered this; simpler decrement used).

### PlayerInteractManager

- `playerConnection` cached to local `final` before blocks of `sendPacket` calls — eliminates repeated field dereference chain `this.player.playerConnection.sendPacket`.
- `isDenied` cached: `final boolean isDenied = event.useInteractedBlock() == Event.Result.DENY` — evaluated once, checked multiple times.

### NPC support

- Second `PlayerConnection(MinecraftServer, NetworkManager, EntityNPC)` constructor added.

---

## 7. Player List & Join/Leave

🟡 **Medium impact** — join/leave is not per-tick but affects stability under concurrent access.

| What                       | Paper 1.7                              | CompressedPaper                                                                           |
| -------------------------- | -------------------------------------- | ----------------------------------------------------------------------------------------- |
| `players` list type        | `ArrayList<EntityPlayer>`              | `CopyOnWriteArrayList<EntityPlayer>` — iterator safety under concurrent modification      |
| Player lookup by name      | Linear scan of `players` list          | `HashMap<String, EntityPlayer> playerMap` — O(1)                                          |
| Player lookup by UUID      | Linear scan                            | `HashMap<UUID, EntityPlayer> uuidMap` — O(1)                                              |
| Statistics map             | `HashMap`                              | Retained as `HashMap<UUID, ServerStatisticManager>`                                       |
| `tabIndex` counter         | `int`                                  | `byte` — reduced size                                                                     |
| Tab list broadcast on join | Two separate packets sent in two loops | Single pre-built `PacketPlayOutPlayerInfo` + display name packet, sent in one player loop |
| Logger                     | `private static final Logger` (shared) | `private final Logger` (per-instance) — avoids static init race                           |
| `getOPs().isEmpty()`       | `getOPs().size() == 0`                 | `isEmpty()`                                                                               |

---

## 8. Network & Protocol

🟡 **Medium impact** — correctness and multi-version support.

### `NetworkManager`

- `protocolVersion` `AttributeKey<Integer>` added — version stored on channel, cached to `int version` field after first read.
- `getVersion()` — single cached field read after initialization instead of channel attribute lookup every call.
- `SUPPORTED_VERSIONS = ImmutableSet.of(4, 5, 47, 107, 108, 109, 110)` — explicit version whitelist.
- `incomingPackets` / `queuedPackets` use `ConcurrentLinkedQueue` — lock-free concurrent queues.
- `isProxied`, `virtualHost` fields added for BungeeCord proxy support.

### Chunk Packets

- `PacketPlayOutMapChunkBulk`: `Deflater` moved to `ThreadLocal<Deflater>` — one deflater per thread, no allocation per packet.
- Compression level: 6 → 4 (Spigot patch) — faster compression at slight size cost.
- `PacketPlayOutMapChunk`: static `byte[] emptyChunkBytes` and pre-deflated `emptyChunkBytesDeflated` — empty chunk data computed once at class load, not per packet.

### Spawn Entity

- `PacketPlayOutSpawnEntity` fully rewritten — constructor takes `Entity` directly, extracts all fields at construction time instead of at write time.
- UUID field added to spawn packet.

---

## 9. MinecraftServer Tick Loop

🟡 **Medium impact** — runs 20× per second.

| What                     | Paper 1.7                                                | CompressedPaper                                      |
| ------------------------ | -------------------------------------------------------- | ---------------------------------------------------- |
| World loop index         | `int i`                                                  | `byte i` — narrower type, worlds < 127 always        |
| World loop size variable | `this.worlds.size()` re-called each iteration            | Cached to `byte i = (byte) this.worlds.size()`       |
| `TPS` constant           | `int`                                                    | `static final byte TPS = 20`                         |
| Tracker update guard     | `worldserver.getTracker().updatePlayers()` always called | `if (!playerList.players.isEmpty())` guard first     |
| `playerListBox`          | Untyped List                                             | `List<IUpdatePlayerListBox>`                         |
| Ping sample cap          | Uncapped                                                 | `Math.min(this.C(), 12)` — caps sample at 12 players |
| Server date change       | Not fired                                                | `ServerDateChangeEvent` added                        |
| Hologram tick            | Not present                                              | `CraftHologram.tickAll()` integrated                 |
| `worlds.isEmpty()`       | `size() == 0` check                                      | `!isEmpty()`                                         |

---

## 10. Entity Classes

### `EntityLiving`

| What                     | Paper 1.7                              | CompressedPaper                                                      |
| ------------------------ | -------------------------------------- | -------------------------------------------------------------------- |
| `effects` map            | `HashMap<Integer, MobEffect>`          | `ConcurrentHashMap<Integer, MobEffect>` — async effect access safety |
| Effect presence check    | `effects.size() > 0 && containsKey(i)` | `!effects.isEmpty() && containsKey(i)`                               |
| Damage tick check        | `ticksLived % 2 == 0`                  | `(ticksLived & 1) == 0` — bitwise                                    |
| Item collection dispatch | instanceof check inline                | `ICollect` interface check — extensible                              |
| Health regain event      | Local variable chain                   | `final float f1 = this.getHealth()` cached                           |

### `Entity`

- `tick()` method rename from `h()` — clarity throughout codebase.
- `isLevelAtLeast()` converted from `static boolean` to `public boolean` — allows instance context.
- `EnumEntitySize as` field removed — not in higher protocol versions, dead code.
- Bounding box inflation: local `final` for all 6 values before collision checks.

### `EntityHuman`

- Food eating: `g <= 25 && (g & 3) == 0` — bitwise tick modulo.
- `isEating()` method added — public predicate instead of repeated field checks.
- `isRelevantItem(ItemStack)` extracted as private static — avoids entering eat logic for irrelevant items; `switch` on item type.
- Yaw/pitch trig pre-computed as `final float` before use in two different calculations.
- Knockback: sin/cos cached to `sinYaw`/`cosYaw` before apply — computed once.
- Attack critical hit: removed duplicate `f > 0.0F` check (was already guaranteed by caller).
- `g` counter fix: `g > -1 && --g == 0` — prevents counter going below -1 indefinitely.

### `EntityPlayer`

- Implements `IEntitySpecificSpawnPacket` + `ITrack` — tracked and spawned via interface dispatch.
- `removeQueue` changed from `LinkedList<Integer>` → `ArrayDeque<Integer>` — O(1) head removal, better cache behavior.
- `chunkCoordIntPairQueue` uses `LinkedList` (was already linked list but now typed).
- `areNeighborsLoaded(1)` guard added before tile entity packet send on chunk load.
- `bW` flag removed (unused in this build).

### `EntityArrow`

- Implements `ITrack`, `IEntitySpecificSpawnPacket`, `ICollect`.
- `f1 = 0.3f` moved to declaration — avoids init in branch that's never taken in most calls.

### `EntityFishingHook`

- Fishing loot tables replaced with `List.of(...)` — immutable, allocated once at class load instead of per fish attempt.
- Three tables: junk, treasure, fish — all `static final List<PossibleFishingResult>`.

### `EntityMinecartAbstract`

- Implements `ITrack`, `IEntitySpecificSpawnPacket`.
- Minecart type construction: `if/else if` chain → `switch` expression.
- `flyingX/Y/Z` restored to vanilla double precision (`0.94999998807907104D`).
- `tick()` rename from `h()`.

---

## 11. Explosion

🟡 **Medium impact** — called on every TNT/creeper/etc.

| What                      | Paper 1.7                                        | CompressedPaper                                               |
| ------------------------- | ------------------------------------------------ | ------------------------------------------------------------- |
| Random instance           | `new Random()` per explosion                     | `static final LightRandom CACHED_RANDM` — shared, lighter RNG |
| Block list                | `ArrayList<BlockPosition>` rebuilt per explosion | `ArrayList<ChunkPosition>` reused field                       |
| Entity iteration          | `List` with index loop                           | For-each — cleaner, same perf                                 |
| Explosion knockback check | Inline conditions                                | `disableExplosionKnockback` flag centralized                  |
| Block list for event      | `new ArrayList` + loop building Block objects    | `stream().map().filter().collect()` — one pass                |
| Event block list restore  | `forEach` loop                                   | `stream().map().collect()` — one pass                         |

---

## 12. Chunk I/O & Packets

### `PacketPlayOutMapChunk`

- Static pre-deflated empty chunk bytes — zero allocation for empty chunk packets.
- `tileEntityData` properly typed as `ArrayList<NBTTagCompound>` with initial capacity.
- Section emptiness check with bitmask `(i & 1 << l) != 0` instead of separate counter.

### `PacketPlayOutMapChunkBulk`

- `ThreadLocal<Deflater>` — one deflater reused per thread, eliminates per-packet allocator pressure.
- Compression level 4 (was 6) — ~30% faster compression, ~5% larger output (acceptable tradeoff for chunk bulk).

### `ChunkRegionLoader`

- `chunkExists()` check uses `RegionFile.chunkExists()` — avoids loading region data just to check existence.

---

## 13. Collection & Data Structure Upgrades

| Class                                | Old                   | New                                           | Reason                                                              |
| ------------------------------------ | --------------------- | --------------------------------------------- | ------------------------------------------------------------------- |
| `PlayerList.players`                 | `ArrayList`           | `CopyOnWriteArrayList`                        | Iterator safety when players join/leave during iteration            |
| `EntityTracker.c`                    | `HashSet`             | `ArrayList` with index                        | O(1) swap-pop removal, better cache locality                        |
| `EntityTracker.byPlayer`             | None                  | `IdentityHashMap`                             | O(1) player→entries lookup, identity comparison correct for players |
| `EntityTrackerEntry.trackedPlayers`  | `HashSet`             | `IdentityHashMap.keySet()`                    | Shares map with fresh-flag tracking                                 |
| `Chunk.tileEntities`                 | `HashMap`             | `ConcurrentHashMap`                           | Async lighting thread safety                                        |
| `EntityLiving.effects`               | `HashMap`             | `ConcurrentHashMap`                           | Async potion effect access                                          |
| `HashTreeSet`                        | `HashSet` + `TreeSet` | `ConcurrentHashSet` + `ConcurrentSkipListSet` | Thread-safe iteration                                               |
| `IntCache`                           | `ArrayList`           | `ArrayDeque`                                  | O(1) head/tail ops, correct LIFO semantics                          |
| `NetworkManager` queues              | Not specified         | `ConcurrentLinkedQueue`                       | Lock-free packet queuing                                            |
| `DedicatedServer.j`                  | Unknown               | `ConcurrentLinkedQueue<ServerCommand>`        | Thread-safe console command queue                                   |
| `PacketPlayOutMapChunkBulk` Deflater | Per-instance          | `ThreadLocal<Deflater>`                       | One per thread, no allocation                                       |
| `LongObjectHashMap`                  | Manual array copy     | `Arrays.copyOf()`                             | JVM-intrinsified copy                                               |
| `UnsafeList`                         | Manual array copy     | `Arrays.copyOf()`                             | JVM-intrinsified copy                                               |

---

## 14. Profiler & Timing Removal

🔴 **High aggregate impact** — 214 profiler/timing lines removed across all files. Every removed timing call saves a method call, a string hash lookup, and a stack push/pop on every invocation.

**Removed from hot paths:**

| Location                                            | Removed timings                                                                                                                                             |
| --------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `World.tickEntities()`                              | `entities`, `global`, `remove`, `regular`, `blockEntities`, `pendingBlockEntities` sections                                                                 |
| `World.entityJoinedWorld()`                         | `chunkCheck` section                                                                                                                                        |
| `World.setTypeAndData()`                            | `checkLight` section                                                                                                                                        |
| `World.a(Vec3D, AxisAlignedBB)` (explosion density) | Direct                                                                                                                                                      |
| `World.C()` (chunk tick list)                       | `buildList`, `playerCheckLight` sections                                                                                                                    |
| `World.a(int,int,Chunk)` (mood sound)               | `moodSound`, `checkLight` sections                                                                                                                          |
| `World.updateLight()` / `t()`                       | `getBrightness`, `checkedPosition < toCheckCount` sections                                                                                                  |
| `World.findPath()` (pathfinding)                    | `pathfind` sections (×2)                                                                                                                                    |
| `EntityTracker.addEntity()`                         | Inline                                                                                                                                                      |
| `EntityTracker.untrackEntity()`                     | Inline                                                                                                                                                      |
| `PlayerConnection.a()`                              | `keepAlive` section                                                                                                                                         |
| `ActivationRange.activateEntities()`                | `entityActivationCheckTimer`                                                                                                                                |
| `ActivationRange.checkIfActive()`                   | `checkIfActiveTimer`                                                                                                                                        |
| `MinecraftServer` tick                              | Multiple world iteration timings                                                                                                                            |
| `WorldServer`                                       | Mob spawning section timings                                                                                                                                |
| `TileEntity.h()`                                    | `tickTimer` per tile entity                                                                                                                                 |
| `Entity` tick                                       | `tickTimer` per entity                                                                                                                                      |
| `AsyncCatcher.catchOp()`                            | 15 call sites removed: `entity track`, `entity untrack`, `entity add`, `entity remove`, `entity world add`, `player tracker update`, `player tracker clear` |

---

## 15. Code Quality & Micro-optimizations

These are individually minor but collectively represent consistent improvement across ~398 files.

### Primitive narrowing

- `int` → `byte` for world count loops in `MinecraftServer` (worlds < 127 guaranteed)
- `int` → `byte` for `TPS = 20` constant
- `int` → `short` for tracking range return values
- `int` → `byte` for tab list index
- `int` → `byte` for attack extra knockback flag in `EntityHuman`

### `final` declarations

1,676 new `final` local variable declarations added. This:

- Communicates intent clearly
- Enables JIT escape analysis — variables that don't escape can be stack-allocated
- Prevents accidental reassignment bugs

### Pattern matching `instanceof`

158 `instanceof` + explicit cast pairs replaced with pattern matching (`instanceof Foo bar`). Eliminates redundant cast and removes one local variable declaration per site.

### `isEmpty()` instead of `size() > 0`

78 call sites changed. `isEmpty()` is semantically correct (O(1) guaranteed) vs `size()` which is O(1) for most but not all implementations.

### Bitwise modulo

- `% 4 == 0` → `& 3) == 0` in entity activation check
- `% 2 == 0` → `& 1) == 0` in entity living damage tick
- `% 4 == 0` → `& 3) == 0` in EntityHuman food tick
- `% 60 == 0` stays (not power of 2)

### `switch` expressions (arrow syntax)

101 `switch` expressions added, replacing `if/else if` chains for:

- Biome mob list lookup
- Spawn limit by creature type
- Minecart type construction
- DataWatcher protocol dispatch
- Fire dousing direction
- Block intersection face detection
- Weather duration logic
- Difficulty damage scaling

### Early return pattern

Nested `if/else` structures flattened with early returns throughout `World`, `EntityLiving`, `BanEntrySerializer`, `BiomeBase`, `SpawnerCreature`.

### `List.of()` for static loot tables

`EntityFishingHook`: 3 mutable `ArrayList` loot tables replaced with `List.of()` — immutable, initialized once, no copy needed.

### `MethodProfiler` removal

`MethodProfiler` field removed from `World` entirely. Previously constructed and referenced in dozens of places — now `MethodProfiler.patch` shows it as intentionally empty.

---

## 16. Summary Table

| Category                          | Files     | Estimated Impact | Notes                                                           |
| --------------------------------- | --------- | ---------------- | --------------------------------------------------------------- |
| Entity Tracking (Tracker + Entry) | 2         | 🔴 Very High     | Scales with entity + player count; O(n)→O(1) for several ops    |
| DataWatcher                       | 1         | 🔴 Very High     | Every entity, every tick; lock removal + array vs hashmap       |
| World physics + player lookup     | 1         | 🔴 High          | Chunk-based player search; ray trace ~3.7× faster               |
| Profiler/timing removal           | 398       | 🔴 High          | 214 removed calls; constant overhead on every hot path          |
| AsyncCatcher removal              | ~15 sites | 🟡 Medium        | 15 method call + string hash eliminations in entity tracking    |
| Player Connection                 | 1         | 🟡 Medium        | Boolean packing; dead player early exit; cached fields          |
| Entity Activation                 | 1         | 🟡 Medium        | Single chunk lookup; bitwise mod; isEmpty()                     |
| Chunk (playersInChunk)            | 1         | 🟡 Medium        | Enables O(1) player proximity queries globally                  |
| Player List                       | 1         | 🟡 Medium        | O(1) name/UUID lookup; CopyOnWriteArrayList safety              |
| Network / Chunk Packets           | 4         | 🟡 Medium        | ThreadLocal Deflater; pre-deflated empty chunk; version caching |
| MinecraftServer tick              | 1         | 🟡 Medium        | Byte loops; isEmpty guards; tracker skip when no players        |
| EntityLiving / EntityHuman        | 2         | 🟡 Medium        | ConcurrentHashMap effects; bitwise ops; trig caching            |
| Explosion                         | 1         | 🟢 Low-Medium    | Shared LightRandom; stream-based block list                     |
| Collection upgrades (global)      | 398       | 🟡 Medium        | CopyOnWrite, IdentityHashMap, ConcurrentHashMap, ArrayDeque     |
| final + pattern matching          | 398       | 🟢 Low           | JIT hints; eliminates casts; 1,676 finals + 158 instanceof      |
| switch expressions                | 398       | 🟢 Low           | Readability + avoids branch misprediction chains                |
| isEmpty() / bitwise / byte        | 398       | 🟢 Low           | 78 + 105 + many sites; individual nano savings compound         |
| Fishing loot tables               | 1         | 🟢 Low           | List.of() — once per server start vs once per fish              |
| IntCache                          | 1         | 🟢 Low           | ArrayDeque — correct semantics + O(1) ops                       |

---

## Overall Assessment

CompressedPaper targets three tiers of optimization:

**Tier 1 — Algorithmic (large gains)**: Entity tracking data structures, DataWatcher rewrite, chunk-based player lookup, ray trace restructuring. These scale with server size — a 100-player server sees much more benefit than a 10-player one.

**Tier 2 — Constant overhead elimination**: Profiler/timing removal (214 call sites), AsyncCatcher removal (15 sites), boolean packing in PlayerConnection. These apply uniformly regardless of player/entity count.

**Tier 3 — Micro-optimizations**: `final`, `isEmpty()`, bitwise mod, pattern matching, `switch` expressions. Individually small, collectively consistent across 398 files.

**Servers most likely to benefit**: High entity count (factions, skyblock, survival), high player count (>50), PvP-heavy (frequent tracking updates, ray traces), or anything hitting mob spawner lag (SpawnerCreature + ActivationRange path).

---

## 17. Bukkit API Layer (CraftBukkit)

Previously omitted. These are changes to the Bukkit API wrapper layer that have real performance consequences.

### `CraftServer` 🟡

- **`offlinePlayers` cache**: `HashMap<UUID, OfflinePlayer>` → `Caffeine.newBuilder().weakValues().build()` — Caffeine is a high-performance caching library (W-TinyLFU eviction, lock-striping). The `weakValues()` means cached `OfflinePlayer` entries are GC-eligible when no plugin holds a reference, preventing memory leaks. Previously Paper had no cache — every `getOfflinePlayer(UUID)` call hit disk or the user cache.
- `players.isEmpty()` guard before iterating — avoids entering the loop at all on empty servers.
- User cache profile lookup: ternary instead of if/else.

### `CraftWorld` 🟡

- `getChunkAt(x, z)` — uses `world.chunkProviderServer.getChunkIfLoaded(x, z)` instead of forcing a chunk load. Returns null for unloaded chunks rather than loading them unnecessarily.
- `getLoadedChunks()` — uses `chunkProviderServer.chunks.values().stream()` directly instead of iterating world entity lists.
- Multiple method parameters cached to `final` locals before block interaction events — reduces repeated method call chains.

### `CraftEntity` 🟡

- **Entity→CraftEntity wrapping**: Paper used a large `instanceof` chain (20+ branches) to determine which `CraftEntity` subclass to instantiate. CompressedPaper replaces this with a static `HashMap<Class<?>, BiFunction<CraftServer, Entity, CraftEntity>> entityMappings` — initialized once at class load, O(1) lookup per `getEntity()` call. This is called every time a plugin receives an entity reference.

### `CraftInventory` 🟡

- `addItem()`: **`firstEmpty()` now cached** — in Paper it was called repeatedly inside the slot-scanning loop. Now called once per stack attempt and the result reused.
- `HashMap<Integer, ItemStack> leftover` pre-allocated — no resize allocations for typical small item sets.
- `Map<String, Integer> lastPartialIndex` added — caches the last partial slot per item type, avoiding rescanning from slot 0 on every partial stack.

### `BlockFlowing` (fluid simulation) 🟢

- All intermediate values in flow checks cached to `final` locals — avoids repeated `world.getType()` and `world.getData()` calls for the same coordinates.
- `BlockFace[]` array allocated once outside the direction loop instead of being rebuilt per flow check.
- `source` block lookup cached before the event dispatch loop.

### `AsynchronousExecutor` 🟡

- Task map changed from `HashMap<P, Task>` → `ConcurrentHashMap<P, Task>` — thread-safe async chunk loading without explicit locking.
- State constants changed from commented-out bit flags to plain `int` constants `PENDING=0, STAGE_1_ASYNC=1, STAGE_1_SYNC=2, STAGE_1_COMPLETE=3, FINISHED=4` — simpler state machine, easier for JIT to optimize switch dispatch.
- `task.callbacks.isEmpty()` — `isEmpty()` instead of size check.

### `RegionFile` 🟢

- `d` and `e` arrays (`int[1024]`) made `final` — JIT can treat base pointer as constant.
- Freed sector list (`f`) uses `ArrayList<Boolean>` with capacity hint matching existing sector count — avoids resize during initial file load.
- `DataInputStream` wrapping: compression type dispatch uses ternary (`b0 == 1 ? GZIPInputStream : InflaterInputStream`) — one branch instead of if/else.

### `PlayerChunk` 🟢

- `playerRunnables` map added: `HashMap<EntityPlayer, Runnable>` — tracks per-player chunk send callbacks, avoids repeated lambda allocations.
- `players.isEmpty()` checks (×3) used instead of `players.size() == 0`.
- Block change short encoding cached: `final short short1 = (short)(i << 12 | k << 8 | j)`.
- Tile entity packet cached to `final Packet packet` before sending to players — one allocation, N sends.

---

## 18. Updated Summary Table

| Category                          | Files     | Estimated Impact | Notes                                                            |
| --------------------------------- | --------- | ---------------- | ---------------------------------------------------------------- |
| Entity Tracking (Tracker + Entry) | 2         | 🔴 Very High     | O(n)→O(1) for untrack; interface dispatch; chunk-based scan      |
| DataWatcher                       | 1         | 🔴 Very High     | Array vs hashmap+lock; every entity tick                         |
| World physics + player lookup     | 1         | 🔴 High          | Chunk-based player search; ray trace ~3.7×; DIRECTIONS table     |
| Profiler/timing removal           | 398       | 🔴 High          | 214 removed calls across all hot paths                           |
| AsyncCatcher removal              | ~15 sites | 🟡 Medium        | 15 method call + string hash eliminations                        |
| Player Connection                 | 1         | 🟡 Medium        | Boolean packing; dead-player early exit; cached field            |
| Entity Activation                 | 1         | 🟡 Medium        | Single chunk lookup; bitwise mod; isEmpty()                      |
| Chunk (playersInChunk)            | 1         | 🟡 Medium        | Enables O(1) player proximity queries globally                   |
| Player List                       | 1         | 🟡 Medium        | O(1) name/UUID lookup; CopyOnWriteArrayList safety               |
| Network / Chunk Packets           | 4         | 🟡 Medium        | ThreadLocal Deflater; pre-deflated empty chunk; version cache    |
| MinecraftServer tick              | 1         | 🟡 Medium        | Byte loops; isEmpty guards; tracker skip when no players         |
| EntityLiving / EntityHuman        | 2         | 🟡 Medium        | ConcurrentHashMap effects; bitwise ops; trig caching             |
| CraftEntity wrapping              | 1         | 🟡 Medium        | 20+ instanceof chain → O(1) HashMap dispatch per entity ref      |
| CraftServer offline player cache  | 1         | 🟡 Medium        | Caffeine weakValue cache — was no cache at all                   |
| AsynchronousExecutor              | 1         | 🟡 Medium        | ConcurrentHashMap; simplified state machine                      |
| CraftInventory addItem            | 1         | 🟡 Medium        | firstEmpty() cached; lastPartialIndex avoids rescan              |
| CraftWorld chunk access           | 1         | 🟢 Low-Medium    | getChunkIfLoaded instead of forced load                          |
| Explosion                         | 1         | 🟢 Low-Medium    | Shared LightRandom; stream-based block list                      |
| Collection upgrades (global)      | 398       | 🟡 Medium        | CopyOnWrite, IdentityHashMap, ConcurrentHashMap, ArrayDeque      |
| PlayerChunk                       | 1         | 🟢 Low           | isEmpty() ×3; cached tile entity packet; cached short            |
| RegionFile                        | 1         | 🟢 Low           | final arrays; capacity-hinted list; ternary compression dispatch |
| BlockFlowing                      | 1         | 🟢 Low           | Cached locals; BlockFace[] allocated once                        |
| final + pattern matching          | 398       | 🟢 Low           | JIT hints; 1,676 finals + 158 instanceof                         |
| switch expressions                | 398       | 🟢 Low           | Avoids branch misprediction chains; 101 sites                    |
| isEmpty() / bitwise / byte        | 398       | 🟢 Low           | 78 + 105 + many sites; compound savings                          |
| Fishing loot tables               | 1         | 🟢 Low           | List.of() — once per server start                                |
| IntCache                          | 1         | 🟢 Low           | ArrayDeque — correct semantics + O(1) ops                        |
