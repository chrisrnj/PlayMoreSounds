package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.sound.RelativeLocationSetter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PlayerHit implements Listener
{

    private static boolean matchesCriteria(String name, String criteria)
    {
        List<String> list = splitter(criteria.replace(" ", "") + ",");
        for (String s : list) {
            boolean sorter = startsEndsOrContains(name, s);

            if (sorter) {
                if (s.contains("[")) {
                    for (String bracket : splitter(s.substring(s.indexOf("[") + 1, s.lastIndexOf("]")) + ",")) {
                        if (startsEndsOrContains(name, bracket)) {
                            sorter = false;
                        }
                    }
                }
                if (sorter) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean startsEndsOrContains(String value, String filter)
    {
        value = value.toUpperCase();
        String name = filter;

        if (name.contains("=") && name.indexOf("=") < 10) {
            name = name.substring(name.indexOf("=") + 1);
        }

        name = splitAlphaNumeric(name).toUpperCase();

        if (filter.startsWith("Contains=")) {
            return value.contains(name);
        } else if (filter.startsWith("Ends=")) {
            return value.endsWith(name);
        } else if (filter.startsWith("Starts=")) {
            return value.startsWith(name);
        } else {
            return value.equalsIgnoreCase(name) | name.equalsIgnoreCase("ALL");
        }
    }

    private static String splitAlphaNumeric(String value)
    {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);

            if (!Character.toString(c).replace("_", "").replaceAll("[a-zA-Z]", "").equals("")) {
                return s.toString();
            }

            s.append(c);
        }
        return s.toString();
    }

    private static List<String> splitter(String expression)
    {
        int amount = 0;
        List<String> list = new ArrayList<>();
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < expression.length(); ++i) {
            char c = expression.charAt(i);

            s.append(c);

            if (c == '[') {
                ++amount;
            }
            if (amount > 0) {
                if (c == ']') {
                    --amount;
                }
            } else {
                if (c == ',') {
                    list.add(s.substring(0, s.length() - 1));
                    s = new StringBuilder();
                }
            }
        }

        return list;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageByEntity(EntityDamageByEntityEvent e)
    {
        //TODO: Write the new EntityHit.yml feature.

        if (e.getEntity().getPersistentDataContainer().has(RelativeLocationSetter.locked, PersistentDataType.STRING)) {
            e.setCancelled(true);
        }
    }
}
