/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public final class OnEntityDamageByEntity extends PMSListener
{
    private static final @NotNull Pattern comma = Pattern.compile(",");
    private final @NotNull HashSet<RichSound> conditions = new HashSet<>();
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
            int hurtIndex = condition.indexOf("hurt");
            int holdingIndex = condition.indexOf("holding");
            String damagerCriterion = condition.substring(0, hurtIndex).replace(" ", "");
            String victimCriterion = condition.substring(hurtIndex + 4, holdingIndex).replace(" ", "");
            String itemCriterion = condition.substring(holdingIndex + 7).replace(" ", "");

            return matchesCriterion(damagerCriterion, damager.getType().toString()) &&
                    matchesCriterion(victimCriterion, victim.getType().toString()) &&
                    matchesCriterion(itemCriterion, itemInDamagerHand.name());
        } catch (Exception ignored) {
            // If the user got the syntax wrong it will just return false.
        }

        return false;
    }

    private static boolean matchesCriterion(String criterion, String value)
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

        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        Configuration hurtSounds = Configurations.HURT_SOUNDS.getPluginConfig().getConfiguration();
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean specificHurtEnabled = false;

        for (Map.Entry<String, Object> condition : hurtSounds.getNodes().entrySet()) {
            if (condition.getValue() instanceof ConfigurationSection) {
                ConfigurationSection conditionSection = (ConfigurationSection) condition.getValue();

                if (conditionSection.getBoolean("Enabled").orElse(false)) {
                    conditions.add(new RichSound(conditionSection));
                    specificHurtEnabled = true;
                }
            }
        }

        if (defaultEnabled || specificHurtEnabled) {
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

        // If the default sound should play.
        boolean defaultSound = true;

        // Checking if any condition on hurt sounds.yml matches this scenario.
        for (RichSound condition : conditions) {
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
            RichSound sound = getRichSound();

            if (sound != null)
                if (!event.isCancelled() || !sound.isCancellable())
                    sound.play(player, damagerLocation);
        }
    }
}
