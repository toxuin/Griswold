package com.github.toxuin.griswold;

import com.github.toxuin.griswold.npcs.GriswoldNPC;
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
	private final Map<GriswoldNPC, Pair> npcChunks;

	public EventListener(final Griswold plugin) {
		this.plugin = plugin;
		this.npcChunks = plugin.getNpcChunks();
	}

	// MAKE THEM INVINCIBLE
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (npcChunks.isEmpty()) return;
		Set<GriswoldNPC> npcs = npcChunks.keySet();
		for (GriswoldNPC rep : npcs) {
			if (event.getEntity().equals(rep.entity)) {
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

		Set<GriswoldNPC> npcs = npcChunks.keySet();
		for (GriswoldNPC rep : npcs) {
			if (event.getRightClicked().equals(rep.entity)) {
				//plugin.interactor.interact(event.getPlayer(), rep);
				event.setCancelled(true);
			}
		}
	}

    // NO ZOMBIE NO CRY
    @EventHandler
    public void onZombieTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Zombie) {
            Set<GriswoldNPC> npcs = npcChunks.keySet();
            for (GriswoldNPC rep : npcs) {
                if (rep.entity.equals(event.getTarget())) {
                    event.setCancelled(true);
                    return;
                }
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
						plugin.registerRepairman(rep);
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

}
