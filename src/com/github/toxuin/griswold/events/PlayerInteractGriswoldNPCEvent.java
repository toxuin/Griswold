package com.github.toxuin.griswold.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractGriswoldNPCEvent extends PlayerInteractEntityEvent {

    public PlayerInteractGriswoldNPCEvent(Player who, Entity clickedEntity) {
        super(who, clickedEntity);
    }
}
