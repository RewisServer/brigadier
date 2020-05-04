package dev.volix.lib.brigadier;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author Tobias Büser
 */
public class BungeeBrigadierPlugin extends Plugin {

    @Getter private static BungeeBrigadierPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        Brigadier.getInstance().setAdapter(new BungeeBrigadierAdapter());
        Brigadier.getInstance().registerTypes(new PlayerParameterType());
    }

}
