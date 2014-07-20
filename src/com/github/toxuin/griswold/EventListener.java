package com.github.toxuin.griswold;

import com.github.toxuin.griswold.events.PlayerInteractGriswoldNPCEvent;
import com.github.toxuin.griswold.npcs.GriswoldNPC;
import com.github.toxuin.griswold.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Map;

public class EventListener implements Listener {

	private final Griswold plugin;
	private final Map<GriswoldNPC, Pair<Integer, Integer>> npcChunks;

	public EventListener(final Griswold plugin) {
		this.plugin = plugin;
		this.npcChunks = plugin.getNpcChunks();
	}

	// MAKE THEM INVINCIBLE
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (Griswold.isNpc(event.getEntity())) {
				event.setDamage(0d);
				event.setCancelled(true);
		}
	}

	// INTERACTION FILTER
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event instanceof PlayerInteractGriswoldNPCEvent) return;
		if (Griswold.isNpc(event.getRightClicked())) {
            Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractGriswoldNPCEvent(event.getPlayer(), event.getRightClicked()));
	        event.setCancelled(true);
		}
	}

    @EventHandler
    public void onEntityTargetPlayer(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player && Griswold.isNpc(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    // NO ZOMBIE NO CRY
    @EventHandler
    public void onZombieTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Zombie && Griswold.isNpc(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    // NPC DON'T BURN IN SUNLIGHT
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.getEntityType().equals(EntityType.ZOMBIE) || event.getEntityType().equals(EntityType.SKELETON)) {
            if (Griswold.isNpc(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onNewChunkLoad(ChunkLoadEvent event) {
		if (npcChunks.isEmpty()) return;
		Pair coords = new Pair(event.getChunk().getX(), event.getChunk().getZ());

		for (Pair pair : npcChunks.values()) {
			if (pair.equals(coords)) {
				for (GriswoldNPC rep : npcChunks.keySet()) {
					if (npcChunks.get(rep).equals(coords)) {
						plugin.registerNpc(rep);
						if (Griswold.debug) Griswold.log.info("SPAWNED NPC " + rep.name + ", HIS CHUNK LOADED");
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onNewChunkUnload(ChunkUnloadEvent event) {
		if (npcChunks.isEmpty()) return;
		Pair coords = new Pair(event.getChunk().getX(), event.getChunk().getZ());

		for (Pair pair : npcChunks.values()) {
			if (pair.equals(coords)) {
				for (GriswoldNPC rep : npcChunks.keySet()) {
					if (npcChunks.get(rep).equals(coords)) {
						rep.entity.remove();
						if (Griswold.debug) Griswold.log.info("DESPAWNED NPC " + rep.name + ", HIS CHUNK GOT UNLOADED");
					}
				}
			}
		}
	}

    //MAKE INTERACTION
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGriswoldInteraction(PlayerInteractGriswoldNPCEvent event) {
        event.npc.interact(event);
    }

}
