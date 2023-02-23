package me.Neoblade298.NeoConsumables;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import me.Neoblade298.NeoConsumables.objects.DurationEffects;
import me.Neoblade298.NeoConsumables.objects.FoodConsumable;
import me.Neoblade298.NeoConsumables.objects.PlayerCooldowns;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.io.IOComponent;

public class ConsumableManager implements Listener, IOComponent {
	public static HashMap<UUID, PlayerCooldowns> cds = new HashMap<UUID, PlayerCooldowns>();
	public static HashMap<UUID, DurationEffects> effects = new HashMap<UUID, DurationEffects>();
	public static HashSet<UUID> needsEffects = new HashSet<UUID>();
	private static Consumables main;
	
	public ConsumableManager(Consumables main) {
		ConsumableManager.main = main;
		NeoCore.registerIOComponent(main, this, "ConsumableManager");
	}

	@Override
	public void savePlayer(Player p, Statement insert, Statement delete) {
		UUID uuid = p.getUniqueId();
		DurationEffects eff = ConsumableManager.effects.get(uuid);
		if (eff != null) {
			if (!eff.isRelevant()) {
				ConsumableManager.effects.remove(uuid);
			}
			
			try {
				if (eff.isRelevant()) {
					insert.addBatch("REPLACE INTO consumables_effects VALUES ('" + uuid + "','" + eff.getCons().getKey()
							+ "'," + eff.getStartTime() + ");");
				}
				else {
					delete.addBatch("DELETE FROM consumables_effects WHERE uuid = '" + uuid + "';");
				}
			} catch (Exception e) {
				Bukkit.getLogger().log(Level.WARNING, "Consumables failed to save effects for " + uuid);
				e.printStackTrace();
			}
			finally {
				effects.remove(uuid);
			}
		}
	}
	
	@Override
	public void cleanup(Statement insert, Statement delete) {
		try {
			for (Player p : Bukkit.getOnlinePlayers()) {
				savePlayer(p, insert, delete);
			}
			long previousDay = System.currentTimeMillis() - 86400000L;
			delete.executeUpdate("DELETE FROM consumables_effects WHERE startTime < " + previousDay);
		} catch (Exception ex) {
			Bukkit.getLogger().log(Level.WARNING, "Consumables failed to cleanup");
			ex.printStackTrace();
		}
	}

	@Override
	public void loadPlayer(Player p, Statement stmt) {
		UUID uuid = p.getUniqueId();
		ConsumableManager.effects.remove(uuid);
		if (Consumables.debug) {
			Bukkit.getLogger().log(Level.INFO, "[NeoConsumables] Loading UUID " + uuid);
		}
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM consumables_effects WHERE uuid = '" + uuid + "';");
			if (rs.next()) {
				FoodConsumable cons = (FoodConsumable) Consumables.getConsumable(rs.getString(2));
				if (Consumables.debug) {
					Bukkit.getLogger().log(Level.INFO, "[NeoConsumables] UUID effect loaded was " + cons.getKey());
				}
				if (cons.isDuration()) {
					DurationEffects effs = new DurationEffects(main, cons, rs.getLong(3), uuid, new ArrayList<BukkitTask>());
					if (effs.isRelevant()) {
						if (Consumables.debug) {
							Bukkit.getLogger().log(Level.INFO, "[NeoConsumables] Effect was placed in database");
						}
						ConsumableManager.effects.put(uuid, effs);
						needsEffects.add(uuid); // Used strictly in SkillAPIListener
					}
				}
			}
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Consumables failed to load effects for " + uuid);
			e.printStackTrace();
		}
	}
	
	public static void endEffects(UUID uuid) {
		DurationEffects effs = ConsumableManager.effects.get(uuid);
		if (effs != null) {
			effs.endEffects(false);
		}
	}
	
	public static void startEffects(UUID uuid) {
		DurationEffects effs = ConsumableManager.effects.get(uuid);
		if (effs != null) {
			if (Consumables.debug) {
				Bukkit.getLogger().log(Level.INFO, "[NeoConsumables] Starting effects for UUID " + uuid);
			}
			effs.startEffects();
		}
		else {
			if (Consumables.debug) {
				Bukkit.getLogger().log(Level.INFO, "[NeoConsumables] No effects for UUID " + uuid);
			}
		}
	}

	@Override
	public void preloadPlayer(OfflinePlayer p, Statement stmt) { }
}
