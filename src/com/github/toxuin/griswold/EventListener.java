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
import java.util.Optional;
import java.util.stream.Collectors;

public class EventListener implements Listener {

    private final Griswold plugin;
    private final Map<GriswoldNPC, Pair> npcChunks;

    EventListener(final Griswold plugin) {
        this.plugin = plugin;
        this.npcChunks = plugin.getNpcChunks();
    }

    // MAKE THEM INVINCIBLE
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        final Optional<Map.Entry<GriswoldNPC, Pair>> any = npcChunks.entrySet().stream()
                .filter(e -> e.getKey().getEntity().equals(event.getEntity()))
                .findAny();

        if (any.isPresent()) {
            event.setDamage(0d);
            event.setCancelled(true);
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
        npcChunks.entrySet().stream()
                .filter(e -> e.getKey().getEntity().equals(event.getRightClicked()))
                .map(Map.Entry::getKey).collect(Collectors.toList())
                .forEach(npc -> {
                    plugin.interactor.interact(event.getPlayer(), npc);
                    event.setCancelled(true);
                });
    }

    // NO ZOMBIE NO CRY
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onZombieTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Zombie)) return;

        final Optional<Map.Entry<GriswoldNPC, Pair>> any = npcChunks.entrySet().stream()
                .filter(e -> e.getKey().getEntity().equals(event.getTarget()))
                .findAny();

        if (any.isPresent()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        Pair coords = new Pair(event.getChunk().getX(), event.getChunk().getZ());
        npcChunks.entrySet().stream()
                .filter(e -> e.getValue().equals(coords))
                .map(Map.Entry::getKey).collect(Collectors.toList())
                .forEach(npc -> {
                    npc.spawn();
                    if (Griswold.debug)
                        Griswold.log.info("SPAWNED NPC " + npc.getName() + ", HIS CHUNK LOADED");
                });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Pair coords = new Pair(event.getChunk().getX(), event.getChunk().getZ());
        npcChunks.entrySet().stream()
                .filter(e -> e.getValue().equals(coords))
                .map(Map.Entry::getKey).collect(Collectors.toList())
                .forEach(npc -> {
                    npc.despawn();
                    if (Griswold.debug)
                        Griswold.log.info("DESPAWNED NPC " + npc.getName() + ", HIS CHUNK GOT UNLOADED");
                });
    }

}
