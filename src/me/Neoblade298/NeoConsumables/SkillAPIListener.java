package me.Neoblade298.NeoConsumables;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sucy.skill.api.event.PlayerAttributeLoadEvent;
import com.sucy.skill.api.event.PlayerAttributeUnloadEvent;
import com.sucy.skill.api.event.PlayerLoadCompleteEvent;

public class SkillAPIListener implements Listener {
	
	@EventHandler
	public void onAttributeLoad(PlayerAttributeLoadEvent e) {
		if (!ConsumableManager.loading.contains(e.getPlayer().getUniqueId())) {
			ConsumableManager.startEffects(e.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler
	public void onAttributeUnload(PlayerAttributeUnloadEvent e) {
		ConsumableManager.endEffects(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onLoadSynchronous(PlayerLoadCompleteEvent e) {
		// This needs to exist because loadplayer is async and
		// attributeload event is not async so it happens before loadplayer
		ConsumableManager.loading.add(e.getPlayer().getUniqueId());
	}
}
