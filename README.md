RepairMan
=========

Bukkit Plugin: Those useless before squidwards now can repair your stuff!

Just show your stuff to blacksmith and he'll tell you how much he wants for the job. If you agree – just pass him the item. Users have no commands (because they don't need them)!

Simple!

##Features:##

* Command-free native minecraft game experience.

* Multiworld support.

* All major economy systems supported. If Vault supports something - than it'll work with Griswold.

* Simple.

##Permissions:##

* **griswold.admin** - lets you create and remove repairmen

* **griswold.tools** - lets users talk to repairmen who repair their weapons and tools

* **griswold.armor** - lets users talk to repairmen who repair armors

##Commands:##

* **/blacksmith create name** - creates new repairman. You can also specify more parameters: **/blacksmith create name type cost**. Name can be anything and will be displayed in chat when user talks to repairman, type can be "all", "weapon" or "armor" - it specifies type of items this repairman can repair.

* **/blacksmith list** - lists all the repairmen ever created

* **/blacksmith remove name** - removes a particular repairman

* **/blacksmith despawn** / **respawn** - despawns or respawns all repairmen. Config is not re-read, you have to use next command to reload config.

* **/blacksmith reload** - reloads config, despawns and then respawns all the repairmen.

All commands require griswold.admin permission and fallbacks to OP if something bad happens.

##Config:##

The only parameters you might need to change in config are "**Debug**" and "**Timeout**". The first one is for debugging stuff and not usually useful if you don't know why you need it, and the second one is for timeout of blacksmiths "remember" your item and are waiting for you to confirm the repair. Default if "**false**" and "**5000**" (5 sec).

All other parameters are configurable in-game, so you don't have to bother about them. But if you want – they are quite self-explanatory.
