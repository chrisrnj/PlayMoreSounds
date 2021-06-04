/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

public final class OnEntityDamageByEntity extends PMSListener
{
    private static final @NotNull Pattern comma = Pattern.compile(",");
    private final @NotNull HashSet<PlayableRichSound> conditions = new HashSet<>();
    private final @NotNull PlayMoreSounds plugin;

    public OnEntityDamageByEntity(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    private static boolean matchesCondition(String condition, Entity damager, Entity victim, Material itemInDamagerHand)
    {
        try {
            // Getting the criterion of the condition and removing spaces, so everything works as intended on matchesCriterion.
            int hitIndex = condition.indexOf("hit");
            int holdingIndex = condition.indexOf("holding");
            String damagerCriterion = condition.substring(0, hitIndex).replace(" ", "");
            String victimCriterion = condition.substring(hitIndex + 4, holdingIndex).replace(" ", "");
            String itemCriterion = condition.substring(holdingIndex + 7).replace(" ", "");

            return matchesCriterion(damagerCriterion, damager.getType().toString()) &&
                    matchesCriterion(victimCriterion, victim.getType().toString()) &&
                    matchesCriterion(itemCriterion, itemInDamagerHand.name());
        } catch (Exception ignored) {
            // If the user got the syntax wrong it will just return false.
        }

        return false;
    }

    protected static boolean matchesCriterion(String criterion, String value)
    {
        criterion = criterion.toLowerCase();
        value = value.toLowerCase();

        if (criterion.startsWith("any") || criterion.equals(value))
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

            String filter = criterion.substring(0, bracketIndex);
            String[] strings = comma.split(criterion.substring(bracketIndex + 1, lastBracketIndex));

            switch (filter) {
                case "contains":
                    for (String string : strings)
                        if (value.contains(string))
                            return true;

                    break;
                case "endswith":
                    for (String string : strings)
                        if (value.endsWith(string))
                            return true;

                    break;
                case "equals":
                    for (String string : strings)
                        if (value.equals(string))
                            return true;

                    break;
                case "startswith":
                    for (String string : strings)
                        if (value.startsWith(string))
                            return true;

                    break;
            }
        } catch (Exception ignored) {
            // If the user got the syntax wrong it will just return false.
        }

        return false;
    }

    @Override
    public @NotNull String getName()
    {
        return "Entity Hit";
    }

    @Override
    public void load()
    {
        conditions.clear();

        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        Configuration hitSounds = Configurations.HIT_SOUNDS.getConfigurationHolder().getConfiguration();
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean playerKillKilledEnabled = sounds.getBoolean("Player Kill.Enabled").orElse(false) || sounds.getBoolean("Player Killed.Enabled").orElse(false);
        boolean specificHurtEnabled = false;

        for (Map.Entry<String, Object> condition : hitSounds.getNodes().entrySet()) {
            if (condition.getValue() instanceof ConfigurationSection) {
                ConfigurationSection conditionSection = (ConfigurationSection) condition.getValue();

                if (conditionSection.getBoolean("Enabled").orElse(false)) {
                    conditions.add(new PlayableRichSound(conditionSection));
                    specificHurtEnabled = true;
                }
            }
        }

        if (defaultEnabled || specificHurtEnabled || playerKillKilledEnabled) {
            if (defaultEnabled)
                setRichSound(new PlayableRichSound(defaultSection));

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

    // getItemInHand() is deprecated but is only used if you are running on older version of bukkit.
    @SuppressWarnings(value = "deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        Player player = null;
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        Material damagerHand = Material.AIR;
        Location damagerLocation = damager.getLocation();

        // Getting the damager main hand item.
        if (damager instanceof LivingEntity) {
            EntityEquipment equipment;

            // Avoiding double unnecessary casting.
            if (damager instanceof Player) {
                player = (Player) damager;
                equipment = player.getEquipment();
            } else {
                equipment = ((LivingEntity) damager).getEquipment();
            }

            if (equipment != null)
                if (VersionUtils.hasOffHand())
                    damagerHand = equipment.getItemInMainHand().getType();
                else
                    damagerHand = equipment.getItemInHand().getType();
        }

        if (VersionUtils.hasPersistentData() && player != null && victim instanceof Player) {
            Player victimPlayer = (Player) victim;

            if (victimPlayer.getHealth() - event.getFinalDamage() <= 0) {
                victimPlayer.getPersistentDataContainer().set(new NamespacedKey(plugin, "killer_uuid"), PersistentDataType.STRING, player.getUniqueId().toString());
            }
        }

        // If the default sound should play.
        boolean defaultSound = true;

        // Checking if any condition on hurt sounds.yml matches this scenario.
        for (PlayableRichSound condition : conditions) {
            if (!event.isCancelled() || !condition.isCancellable()) {
                ConfigurationSection conditionSection = condition.getSection();

                if (matchesCondition(conditionSection.getName(), damager, victim, damagerHand)) {
                    condition.play(player, damagerLocation);

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
        if (defaultSound) {
            PlayableRichSound sound = getRichSound();

            if (sound != null)
                if (!event.isCancelled() || !sound.isCancellable())
                    sound.play(player, damagerLocation);
        }
    }
}
