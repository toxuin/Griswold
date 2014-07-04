Griswold
=========

Bukkit Plugin: NPCs now can repair and enchant your stuff! – Without any commands!

Show your stuff to blacksmith and he'll tell you how much he wants for the job. If you agree – just pass him the item. Users have no commands (because they don't need them)!

Simple!

##Features:##

* Command-free native minecraft game experience.

* Multiworld support.

* Mod items support!

* Repairing does not clear enchantments on enchanted items!

* Items that are fully repaired can be enchanted (optional)!

* Fully multilingual. There are english, [German](https://github.com/toxuin/Griswold/blob/master/de_DE.yml) and [Russian](https://github.com/toxuin/Griswold/blob/master/ru_RU.yml) out-of-the-box, but you can add more!

* All major economy systems supported. If [Vault](http://dev.bukkit.org/server-mods/vault/) supports something - than it'll work with Griswold.

* Lightweight!

##Roadmap:##

* Making NPC to be other living thing other than villagers

* Extracting the treats of NPCs to separate classes so you can make combinations!

* Your suggestions

##Give author a dollar!##

I started to develop for Minecraft more than two years from now and only did it in my spare time. I still don't have a legit Minecraft account. Having one would be helpfull to give support in-person on online-mode servers. So if you want me to come to your server or just want to say "Thanks, Tony!" – feel free to use the button below. I will buy myself a minecraft account from these money.

* [Give author a dollar](https://www.paypal.com/cgi-bin/webscr?business=3YDH29HLTFR7E&item_number=github&cmd=_xclick&currency_code=CAD&amount=1&item_name=Give%20Tony%20a%20dollar)

* [Give five](https://www.paypal.com/cgi-bin/webscr?business=3YDH29HLTFR7E&item_number=github&cmd=_xclick&currency_code=CAD&amount=1&item_name=Give%20Tony%20five%20bucks)

* [Buy him a Minecraft copy!](https://www.paypal.com/cgi-bin/webscr?business=3YDH29HLTFR7E&item_number=github&cmd=_xclick&currency_code=CAD&amount=1&item_name=A%20Minecraft%20copy%20for%20Tony)

##Permissions:##

* **griswold.admin** - lets you create and remove repairmen

* **griswold.tools** - lets users talk to repairmen who repair their weapons and tools

* **griswold.armor** - lets users talk to repairmen who repair armors

* **griswold.enchant** - lets users to enchant their gear at repairmen

##Commands:##

These commands are for administrative purposes, users don't need them.

* **/blacksmith create name** - creates new repairman. You can also specify more parameters: **/blacksmith create name type cost**. Name can be anything and will be displayed in chat when user talks to repairman. NPC types are described below. Cost is repairman's multiplier of cost – it can be greater than 1 to make prices higher or below 1 to make prices lower. Prices are rounded to 2 digits after comma.

* **/blacksmith list** - lists all the repairmen ever created

* **/blacksmith remove name** - removes a particular repairman

* **/blacksmith despawn** / **respawn** - despawns or respawns all repairmen. Config is not re-read, you have to use next command to reload config.

* **/blacksmith names** – toggles names above their heads.

* **/blacksmith sound name bukkit_sound_id** – sets the interaction sound for the repairman. Use standard bukkit Sound names (you can find those [here](http://jd.bukkit.org/rb/apidocs/org/bukkit/Sound.html)). You can set sound to "mute" to be silent.

* **/blacksmith reload** - reloads config, despawns and then respawns all the repairmen.

All commands require griswold.admin permission and fallbacks to OP if something bad happens.

/blacksmith command has an alias – /bsmith to avoid conflicts with other plugins.

##NPC Types:##

When you use /blacksmith create command you can specify a type of NPC. Available types are:

* "**all**" – can repair everything and enchant repaired things.

* "**weapon**" or "**armor**" – can repair only one type of items, cannot enchant.

* "**both**" – can repair everything but cannot enchant.

* "**enchant**"  – these guys are just enchanters and cannot repair things.

##Config:##

All parameters are stored in config.yml and are reloadable without restarting the server (**/blacksmith reload**). 

**Debug** is for debugging stuff and not usually useful if you don't know why you need it. Default is "**false**".

**Timeout** is for timeout of blacksmiths "remember" your item and are waiting for you to confirm the repair. Default is "**5000**" (5 sec).

**Language** is a name of language file (without .yml at the end) stored next to config.yml and containing all the text in preferred language. Default is "**en_US**".

**UseEnchantmentSystem** tells the plugin if he should allow spawning of enchant-type repairmen and allow all-type ones to add enchantments.

**PriceToAddEnchantment** sets the price to add one random set of enchantments. Default - 50.

**ClearOldEnchantments** says to repairmen whether they should blank the item before they enchant it or just add new enchantments to it. Default - true that means "yes, blank it".

**EnchantmentBonus** is a number of "virtual bookshelves" around blacksmith. Greater the number - cooler are enchantments. Default - 5.

You also can alternate price calculation by changing basic armor and tool prices and enchantment cost. Modify parameters **BasicArmorPrice**, **BasicToolPrice** and **BasicEnchantmentPrice**.

Prices are calculated like this: repairman cost parameter \* (basic item type cost + (basic enchantment price \* number of enchantments \* sum'd enchantment levels)).

This formula makes repairment of simple non-enchanted items quite cheap, items with 1-2 enchantments are at moderate cost and uber-enchanted items repairment cost a fortune.

All other config parameters are configurable in-game, so you don't have to bother about them. But if you want – they are quite self-explanatory.

###How to add a custom item###

You have to create a new entry in config with name CustomItems. This entry will contain two more entries: Tools and Armor. Under those you add your items in "'id': name" format.
Yes, it sounds complicated, but it is not! Here, look at my sample config:

    CustomItems:
      Tools:
        '261': Bow
        '259': Flint and steel
      Armor:
        '12': Random armor
        '69': Some other armor

You can just copy-paste it in your config at the very end of it and change the IDs. Names after ":" are just for your convenience and are not used anywhere in the plugin.

##Support links##

* [Plugin page on dev.bukkit.org](http://dev.bukkit.org/bukkit-plugins/griswold/)
* [Russian discussion and support](http://rubukkit.org/threads/15343/)
* Feel free to suggest something or report bugs in Issues
* Your pull-requests are always welcome!

##Stats:##

![Griswold stats](http://mcstats.org/signature/griswold.png)