package com.github.toxuin.griswold;

import com.github.toxuin.griswold.util.Pair;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Map;
import java.util.Set;

public class EventListener implements Listener {

    private final Griswold plugin;
    private final Map<Repairer, Pair> npcChunks;

    EventListener(final Griswold plugin) {
        this.plugin = plugin;
        this.npcChunks = plugin.getNpcChunks();
    }

    // MAKE THEM INVINCIBLE
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (npcChunks.isEmpty()) return;
        Set<Repairer> npcs = npcChunks.keySet();
        for (Repairer rep : npcs) {
            if (event.getEntity().equals(rep.getEntity())) {
                event.setDamage(0d);
                event.setCancelled(true);
            }
        }
    }

    // MAKE INTERACTION
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().hasPermission("griswold.tools")
                || !event.getPlayer().hasPermission("griswold.armor")
                || !event.getPlayer().hasPermission("griswold.enchant")) {
            return;
        }

        Set<Repairer> npcs = npcChunks.keySet();
        for (Repairer rep : npcs) {
            if (event.getRightClicked().equals(rep.getEntity())) {
                plugin.interactor.interact(event.getPlayer(), rep);
                event.setCancelled(true);
            }
        }
    }

    // NO ZOMBIE NO CRY
    @EventHandler
    public void onZombieTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Zombie) {
            Set<Repairer> npcs = npcChunks.keySet();
            for (Repairer rep : npcs) {
                if (rep.getEntity().equals(event.getTarget())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (npcChunks.isEmpty()) return;
        Pair coords = new Pair(event.getChunk().getX(), event.getChunk().getZ());

        for (Pair pair : npcChunks.values()) {
            if (pair.equals(coords)) {
                for (Repairer rep : npcChunks.keySet()) {
                    if (npcChunks.get(rep).equals(coords)) {
                        rep.spawn();
                        if (Griswold.debug) Griswold.log.info("SPAWNED NPC " + rep.getName() + ", HIS CHUNK LOADED");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (npcChunks.isEmpty()) return;
        Pair coords = new Pair(event.getChunk().getX(), event.getChunk().getZ());

        for (Pair pair : npcChunks.values()) {
            if (pair.equals(coords)) {
                for (Repairer rep : npcChunks.keySet()) {
                    if (npcChunks.get(rep).equals(coords)) {
                        rep.despawn();
                        if (Griswold.debug) Griswold.log.info("DESPAWNED NPC " + rep.getName() + ", HIS CHUNK GOT UNLOADED");
                    }
                }
            }
        }
    }

}
