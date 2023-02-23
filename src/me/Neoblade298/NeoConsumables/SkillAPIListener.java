package me.Neoblade298.NeoConsumables;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerAttributeLoadEvent;
import com.sucy.skill.api.event.PlayerAttributeUnloadEvent;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.events.NeoCoreLoadCompleteEvent;

public class SkillAPIListener implements Listener {

	@EventHandler
	public void onAttributeLoad(PlayerAttributeLoadEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		// SkillAPI calls this before NeoCore can load in consumable effect
		if (NeoCore.isLoaded(p) && ConsumableManager.needsEffects.contains(uuid)) {
			ConsumableManager.startEffects(uuid);
			ConsumableManager.needsEffects.remove(uuid);
		}
	}

	@EventHandler
	public void onAttributeUnload(PlayerAttributeUnloadEvent e) {
		ConsumableManager.endEffects(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onNeoCoreLoad(NeoCoreLoadCompleteEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (SkillAPI.isLoaded(p) && ConsumableManager.needsEffects.contains(uuid)) {
			ConsumableManager.startEffects(uuid);
			ConsumableManager.needsEffects.remove(uuid);
		}
	}
}
