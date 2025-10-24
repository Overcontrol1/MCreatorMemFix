# MCreator Memory Fix

Fixes a large memory allocation bug in MCreator for Forge 1.20.1. 
Detects mods on first startup, then a restart is required for effects to activate.

## About

**TLDR**

* This mod fixes high allocation rates in some MCreator mods that use a lot of player-attached variables.
* This might result in less GC stuttering, less average/peak memory usage, and even a small FPS gain. 
  Your mileage may vary, and it entirely depends on your installed MCreator mods.
* There shouldn't be any downsides to having this mod installed.
* This mod will detect all applicable mods when you launch the game, 
but it will not come into effect until you launch the game again.
* Modpack creators should redistribute the `mcreator_mem_fix.json` file found in `.minecraft/config`,
and may optionally lock it by adding the `locked` key to the json object found within the config file, and setting it to true.
This prevents the mod from regenerating its config when the mod list changes.

*Some light technical stuff ahead. Nothing too bad.*

### The Issue

Whenever an MCreator mod accesses its player variables at all, it creates a new object to serve as a default, then accesses the stored one. 
If there is no stored one, the default one is used. If there is a stored one, the default one was created for no reason.

Where's the problem? Many MCreator mods like to access player variables a lot.
This results in a lot of unnecessary allocations.
These allocations can happen many, many times within both ticks and frames, 
resulting in a large amount of allocations per second.

### The Solution

This mod just stops that object from being created *every* time an MCreator mod wants to access its player variables.
It stores a default variables object for each mod, and instead of making a new object every time we access it, we fall back to the stored default. Easy.

There shouldn't be any functional difference with this mod installed.

## Effect

Some MCreator mods can allocate up to ~300MB of garbage before the GC does its thing.
Memory allocation is a slow operation, and allocating that much memory for no gain is not necessary.
With this mod, the memory allocation from MCreator mods, regarding player variables, should drop to next to nothing. 
This might result in less stuttering, less average/peak memory usage, and even a small FPS gain. Your mileage may vary.

I haven't actually tested much, but this mod shouldn't cause many issues. It's pretty much free, with little-to-no downside.

## Compatibility

No idea. Should probably be fine as long as no other mod is touching the same stuff this one touches (obviously, that's usually how compatibility works).

This mod is a little intrusive, it modifies a lot of the original mod's classes, BUT it should be in a relatively clean way.

**Any mod that doesn't explicitly touch MCreator mods should be 100% compatible, always. Unless I mess something up.**

## FAQ

**DISCLAIMER: Not a single individual has ever asked me any one of these questions.**

> How do I make it work for a specific MCreator mod?

*Upon running the game, a file in 
the config directory `.minecraft/config` will be created. 
After this file is created, you should restart the game to ensure the detected MCreator mods are modified.
After adding new MCreator mods, you should delete this file so the mod rescans your mods.
A message is logged when the mod scans for MCreator mods.*

*Modpack developers may safely redistribute this config file, 
preventing the end user from having to restart their game.
They may also lock this file by adding and setting the `locked` key to true in the config. 
This is not really necessary, but it stops the mod from searching for new candidates to modify, 
saving a few milliseconds on startup.*

> Something went wrong. How do I fix it?

*First things first, try deleting the config and re-running the game once or twice. 
If that doesn't work, submit an issue [here](https://github.com/Overcontrol1/MCreatorMemFix/issues).*

> You seem really cool. Can you be my friend?

*No. Maybe.*

> Can I use this in a modpack?

*Yeah, it's designed for that.*