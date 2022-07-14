/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.playmoresounds.bukkit.listeners;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public final class OnEntityDamageByEntity extends PMSListener {
    private static final @NotNull Pattern comma = Pattern.compile(",");
    private final @NotNull HashSet<PlayableRichSound> conditions = new HashSet<>();
    private final @NotNull NamespacedKey killerUUID;

    public OnEntityDamageByEntity(@NotNull PlayMoreSounds plugin) {
        super(plugin);

        this.killerUUID = new NamespacedKey(plugin, "killer_uuid");
    }

    private static boolean matchesCondition(String condition, Entity damager, Entity victim, Material itemInDamagerHand) {
        try {
            // Getting the criterion of the condition and removing spaces, so everything works as intended on matchesCriterion.
            int hitIndex = condition.indexOf("hit");
            int holdingIndex = condition.indexOf("holding");
            var damagerCriterion = condition.substring(0, hitIndex).replace(" ", "");
            var victimCriterion = condition.substring(hitIndex + 4, holdingIndex).replace(" ", "");
            var itemCriterion = condition.substring(holdingIndex + 7).replace(" ", "");

            return matchesCriterion(damagerCriterion, damager.getType().name()) &&
                    matchesCriterion(victimCriterion, victim.getType().name()) &&
                    matchesCriterion(itemCriterion, itemInDamagerHand.name());
        } catch (Exception ignored) {
            // If the user got the syntax wrong it will just return false.
        }

        return false;
    }

    static boolean matchesCriterion(String criterion, String value) {
        criterion = criterion.toLowerCase();
        value = value.toLowerCase();

        if (criterion.equals(value) || criterion.startsWith("any"))
            return true;

        try {
            int bracketIndex = criterion.indexOf("[");

            // If criterion doesn't has any filters.
            if (bracketIndex == -1) {
                criterion = "equals[" + criterion + "]";
                bracketIndex = 6;
            }

            int lastBracketIndex = criterion.lastIndexOf("]");

            if (lastBracketIndex == -1)
                lastBracketIndex = criterion.length();

            String[] strings = comma.split(criterion.substring(bracketIndex + 1, lastBracketIndex));

            switch (criterion.substring(0, bracketIndex)) {
                case "contains" -> {
                    for (var string : strings)
                        if (value.contains(string))
                            return true;
                }
                case "endswith" -> {
                    for (var string : strings)
                        if (value.endsWith(string))
                            return true;
                }
                case "equals" -> {
                    for (var string : strings)
                        if (value.equals(string))
                            return true;
                }
                case "startswith" -> {
                    for (var string : strings)
                        if (value.startsWith(string))
                            return true;
                }
            }
        } catch (Exception ignored) {
            // If the user got the syntax wrong it will just return false.
        }

        return false;
    }

    @Override
    public @NotNull String getName() {
        return "Entity Hit";
    }

    @Override
    public void load() {
        conditions.clear();

        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        var hitSounds = Configurations.HIT_SOUNDS.getConfigurationHolder().getConfiguration();

        for (Map.Entry<String, Object> condition : hitSounds.getNodes().entrySet()) {
            if (condition.getValue() instanceof ConfigurationSection conditionSection) {
                if (conditionSection.getBoolean("Enabled").orElse(false)) {
                    conditions.add(getRichSound(conditionSection));
                }
            }
        }

        setRichSound(getRichSound(sounds.getConfigurationSection(getName())));

        // Player Kill and Player Killed sounds depend on this listener to know who is the killer.
        boolean playerKillKilledEnabled = sounds.getBoolean("Player Kill.Enabled").orElse(false) || sounds.getBoolean("Player Killed.Enabled").orElse(false);

        if (getRichSound() != null || !conditions.isEmpty() || playerKillKilledEnabled) {
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        var damager = event.getDamager();
        var victim = event.getEntity();
        var damagerHand = Material.AIR;
        var damagerLocation = damager.getLocation();
        // damager can be an entity, so damagerPlayer is only not null if the damager is a player.
        Player damagerPlayer = null;

        // Getting the damager main hand item.
        if (damager instanceof LivingEntity) {
            EntityEquipment equipment;

            // Avoiding double unnecessary casting.
            if (damager instanceof Player) {
                damagerPlayer = (Player) damager;
                equipment = damagerPlayer.getEquipment();
            } else {
                equipment = ((LivingEntity) damager).getEquipment();
            }

            if (equipment != null) {
                damagerHand = equipment.getItemInMainHand().getType();
            }
        }

        // The players last killer uuid is set here, so it can be get in PlayerDeathEvent and be used to play kill and killed sounds.
        // Then, after the player respawns, the killer uuid key is removed from player data.
        if (damagerPlayer != null && victim instanceof Player victimPlayer && victimPlayer.getHealth() - event.getFinalDamage() <= 0) {
            victimPlayer.getPersistentDataContainer().set(killerUUID, PersistentDataType.STRING, damagerPlayer.getUniqueId().toString());
        }

        // If the default sound should play.
        boolean defaultSound = getRichSound() != null;

        // Checking if any condition on hit sounds.yml matches this scenario.
        for (PlayableRichSound condition : conditions) {
            if (!event.isCancelled() || !condition.isCancellable()) {
                ConfigurationSection conditionSection = condition.getSection();

                if (matchesCondition(conditionSection.getName(), damager, victim, damagerHand)) {
                    condition.play(damagerPlayer, damagerLocation);

                    // Checking if default sound should play.
                    if (conditionSection.getBoolean("Prevent Other Sounds.Default Sound").orElse(false))
                        defaultSound = false;

                    // Checking if this loop should continue checking for other conditions.
                    if (conditionSection.getBoolean("Prevent Other Sounds.Other Conditions").orElse(false))
                        break;
                }
            }
        }

        // Playing the default sound.
        if (defaultSound && (!event.isCancelled() || !getRichSound().isCancellable())) {
            getRichSound().play(damagerPlayer, damagerLocation);
        }
    }
}
