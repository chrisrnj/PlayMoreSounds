package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class OnPlayerGameModeChange extends PMSListener
{
    private final @NotNull HashMap<String, RichSound> specificGameModes = new HashMap<>();
    private final @NotNull PlayMoreSounds plugin;

    public OnPlayerGameModeChange(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Game Mode Change";
    }

    @Override
    public void load()
    {
        specificGameModes.clear();

        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        Configuration gameModes = Configurations.GAME_MODES.getPluginConfig().getConfiguration();
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean specificGameModeEnabled = false;

        for (Map.Entry<String, Object> gameMode : gameModes.getNodes().entrySet()) {
            if (gameMode.getValue() instanceof ConfigurationSection) {
                ConfigurationSection gameModeSection = (ConfigurationSection) gameMode.getValue();

                if (gameModeSection.getBoolean("Enabled").orElse(false)) {
                    specificGameModes.put(gameMode.getKey().toUpperCase(), new RichSound(gameModeSection));
                    specificGameModeEnabled = true;
                }
            }
        }

        if (defaultEnabled || specificGameModeEnabled) {
            if (defaultEnabled)
                setRichSound(new RichSound(defaultSection));

            if (!isLoaded()) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                setLoaded(true);
            }
        } else {
            if (isLoaded()) {
                HandlerList.unregisterAll(this);
                setLoaded(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        Player player = event.getPlayer();
        RichSound specificGameModeSound = specificGameModes.get(event.getNewGameMode().name());
        boolean defaultSound = true;

        if (specificGameModeSound != null) {
            specificGameModeSound.play(player);

            if (specificGameModeSound.getSection().getBoolean("Stop Other Sounds.Default Sound").orElse(false))
                defaultSound = false;
        }

        if (defaultSound)
            getRichSound().play(player);
    }
}
