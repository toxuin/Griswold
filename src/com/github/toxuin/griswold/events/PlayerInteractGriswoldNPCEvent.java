package com.github.toxuin.griswold.events;

import com.github.toxuin.griswold.Griswold;
import com.github.toxuin.griswold.npcs.GriswoldNPC;
import com.github.toxuin.griswold.util.ConfigManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractGriswoldNPCEvent extends PlayerInteractEntityEvent {

    public GriswoldNPC npc;
    public long time;
    public short itemDamage;
    public ItemStack item;

    public PlayerInteractGriswoldNPCEvent(Player who, Entity clickedEntity) {
        super(who, clickedEntity);
        this.time = System.currentTimeMillis();
        this.item = who.getItemInHand();
        this.itemDamage = who.getItemInHand().getDurability();

        for (GriswoldNPC gnpc : Griswold.plugin.getNpcChunks().keySet()) {
            if (gnpc.entity.equals(clickedEntity)) {
                this.npc = gnpc;
                break;
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof PlayerInteractGriswoldNPCEvent)) return false;
        PlayerInteractGriswoldNPCEvent event = (PlayerInteractGriswoldNPCEvent) object;
        int delta = (int) (this.time-event.time);
        return ((event.item.equals(this.item)) &&
                (!event.isCancelled()) &&
                (event.itemDamage == this.itemDamage) &&
                (event.getPlayer().equals(this.getPlayer())) &&
                (event.getRightClicked().equals(this.getRightClicked())) &&
                (delta < ConfigManager.timeout));
    }
}
