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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class OnPlayerCommandPreprocess extends PMSListener
{
    private final @NotNull HashMap<String, HashSet<RichSound>> filtersAndCriteria = new HashMap<>();
    private final @NotNull PlayMoreSounds plugin;

    public OnPlayerCommandPreprocess(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Send Command";
    }

    @Override
    public void load()
    {
        filtersAndCriteria.clear();

        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        Configuration commandTriggers = Configurations.COMMANDS.getPluginConfig().getConfiguration();
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean triggerEnabled = false;

        for (Map.Entry<String, Object> filter : commandTriggers.getNodes().entrySet()) {
            if (filter.getValue() instanceof ConfigurationSection) {
                ConfigurationSection filterSection = (ConfigurationSection) filter.getValue();
                HashSet<RichSound> criteria = new HashSet<>();

                for (Map.Entry<String, Object> criterion : filterSection.getNodes().entrySet()) {
                    if (criterion.getValue() instanceof ConfigurationSection) {
                        ConfigurationSection criterionSection = (ConfigurationSection) criterion.getValue();

                        if (criterionSection.getBoolean("Enabled").orElse(false)) {
                            criteria.add(new RichSound(criterionSection));
                            triggerEnabled = true;
                        }
                    }
                }

                filtersAndCriteria.put(filter.getKey(), criteria);
            }
        }

        if (defaultEnabled || triggerEnabled) {
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
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        Player player = event.getPlayer();
        boolean defaultSound = true;

        filterLoop:
        for (Map.Entry<String, HashSet<RichSound>> filter : filtersAndCriteria.entrySet()) {
            for (RichSound criteria : filter.getValue()) {
                ConfigurationSection criteriaSection = criteria.getSection();

                if (!event.isCancelled() || !criteria.isCancellable()) {
                    if (OnAsyncPlayerChat.matchesFilter(filter.getKey(), criteriaSection.getName(), message)) {
                        criteria.play(player);

                        if (criteriaSection.getBoolean("Stop Other Sounds.Default Sound").orElse(false))
                            defaultSound = false;

                        if (criteriaSection.getBoolean("Stop Other Sounds.Other Filters").orElse(false))
                            break filterLoop;
                    }
                }
            }
        }

        if (defaultSound) {
            RichSound sound = getRichSound();

            if (sound != null)
                if (!event.isCancelled() || !sound.isCancellable())
                    sound.play(player);
        }
    }
}
