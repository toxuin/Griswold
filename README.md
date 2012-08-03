Griswold
=========

Bukkit Plugin: Those useless before squidwards now can repair your stuff!

Just show your stuff to blacksmith and he'll tell you how much he wants for the job. If you agree – just pass him the item. If item is fully repaired then he can add random enchants to it.

Users have no commands (because they don't need them)!

Simple!

![Griswold: handsome man from Diablo I](http://s019.radikal.ru/i616/1205/32/5969e5ac378e.gif)

##Features:##

* Command-free native minecraft game experience.

* Enchant your items for money!

* Multiworld support.

* Fully multilingual. There are english, german and russian out-of-the-box, but you can add more!

* All major economy systems supported. If Vault supports something - than it'll work with Griswold.

* Simple.

##Permissions:##

* **griswold.admin** - lets you create and remove repairmen

* **griswold.tools** - lets users talk to repairmen who repair their weapons and tools

* **griswold.armor** - lets users talk to repairmen who repair armors

* **griswold.enchant** - lets users to enchant their gear at repairmen

##Commands:##

* **/blacksmith create name** - creates new repairman. You can also specify more parameters: **/blacksmith create name type cost**. Name can be anything and will be displayed in chat when user talks to repairman, type can be "all", "weapon", "armor" or "enchant" - it specifies type of items this repairman can repair and his ability to add enchantments to items.

* **/blacksmith list** - lists all the repairmen ever created

* **/blacksmith remove name** - removes a particular repairman

* **/blacksmith despawn** / **respawn** - despawns or respawns all repairmen. Config is not re-read, you have to use next command to reload config.

* **/blacksmith reload** - reloads config, despawns and then respawns all the repairmen.

All commands require griswold.admin permission and fallbacks to OP if something bad happens.

##Config:##

All parameters are stored in config.yml and are reloadable without restarting the server (**/blacksmith reload**). 

**Debug** is for debugging stuff and not usually useful if you don't know why you need it. Default if "**false**".

**Timaout** is for timeout of blacksmiths "remember" your item and are waiting for you to confirm the repair. Default is "**5000**" (5 sec).

**Language** is a name of language file (without .yml at the end) stored next to config.yml and containing all the text in preferred language. Default is "**ru_RU**".

**UseEnchantmentSystem** tells the plugin if he should allow spawning of enchant-type repairmen and allow all-type ones to add enchantments.

**PriceToAddEnchantment** sets the price to add one random set of enchantments. Default - 50.

**ClearOldEnchantments** says to repairmen whether they should blank the item before they enchant it or just add new enchantments to it. Default - true that means "yes, blank it".

**EnchantmentBonus** is a number of "virtual bookshelves" around blacksmith. Greater the number - cooler are enchantments. Default - 5.

You also can alternate price calculation by changing basic armor and tool prices and enchantment cost. Modify parameters **BasicArmorPrice**, **BasicToolPrice** and **BasicEnchantmentPrice**.

Prices are calculated like this: repairman cost parameter \* (basic item type cost + (basic enchantment price \* number of enchantments \* sum'd enchantment levels)).

This formula makes repairment of simple non-enchanted items quite cheap, items with 1-2 enchantments are at moderate cost and uber-enchanted items repairment cost a fortune.

All other config parameters are configurable in-game, so you don't have to bother about them. But if you want – they are quite self-explanatory.
