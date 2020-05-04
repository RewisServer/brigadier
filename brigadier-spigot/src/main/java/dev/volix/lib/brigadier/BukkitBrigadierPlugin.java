package dev.volix.lib.brigadier;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Tobias Büser
 */
public class BukkitBrigadierPlugin extends JavaPlugin {

    @Getter private static BukkitBrigadierPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        Brigadier.getInstance().setAdapter(new BukkitBrigadierAdapter());
        Brigadier.getInstance().registerTypes(new PlayerParameterType());
    }

}
