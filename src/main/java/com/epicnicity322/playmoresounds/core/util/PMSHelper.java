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

package com.epicnicity322.playmoresounds.core.util;

import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.regex.Pattern;

public final class PMSHelper
{
    private static final @NotNull SecureRandom random = new SecureRandom();
    private static final @NotNull String chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";
    private static final int charsLength = chars.length();
    private static final @NotNull Pattern invalidNamespaceCharacters = Pattern.compile("[^a-z0-9_.-]");
    private static final @NotNull Pattern invalidKeyCharacters = Pattern.compile("[^a-z0-9/._-]");

    private PMSHelper()
    {
    }

    /**
     * Checks if any section in a config has the boolean Enabled set to true. This method is meant to be used in configs
     * that have only sound sections. If a node is not a section, it is ignored.
     *
     * @param configuration The configuration to check.
     * @param prefix        If the config has some main section for sounds, define it here.
     * @return Whether the config has any sound enabled.
     */
    public static boolean anySoundEnabled(@NotNull Configuration configuration, @Nullable String prefix)
    {
        if (prefix == null) {
            for (Map.Entry<String, Object> node : configuration.getNodes().entrySet()) {
                var value = node.getValue();

                if (value instanceof ConfigurationSection section && section.getBoolean("Enabled").orElse(false)) {
                    return true;
                }
            }
        } else {
            for (Map.Entry<String, Object> node : configuration.getAbsoluteNodes().entrySet()) {
                var key = node.getKey();
                var value = node.getValue();

                if (Objects.equals(true, value) && key.equals(prefix + ".Enabled")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Generated a random string with characters A-Za-z0-9.
     *
     * @param length The length the random string should be.
     * @return The random string with random characters.
     */
    public static @NotNull String getRandomString(int length)
    {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; ++i)
            builder.append(chars.charAt(random.nextInt(charsLength)));

        return builder.toString();
    }

    /**
     * @return True if Halloween Event is enabled and today is halloween.
     */
    public static boolean halloweenEvent()
    {
        var now = LocalDateTime.now();

        return now.getMonth() == Month.OCTOBER && now.getDayOfMonth() == 31 && Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Halloween Event").orElse(false);
    }

    /**
     * @return True if today is christmas
     */
    public static boolean isChristmas()
    {
        var now = LocalDateTime.now();

        return now.getMonth() == Month.DECEMBER && now.getDayOfMonth() == 25;
    }

    /**
     * Splits a collection into a {@link HashMap} having the key as the page number, and value as a list that have the
     * size of maxPerPage.
     * <p>
     * If you use a empty collection or set maxPerPage to a value lower than or equal to 0, a map with one page will be
     * returned, but this page will have no entries.
     *
     * @param collection The collection to split.
     * @param maxPerPage The amount you want each page to have.
     * @param <T>        The type of the collection to split.
     * @return The map consisting of the pages.
     */
    public static <T> @NotNull HashMap<Integer, ArrayList<T>> splitIntoPages(@NotNull Collection<T> collection, int maxPerPage)
    {
        if (collection.isEmpty() || maxPerPage <= 0) {
            // Return 1 page with no entries.
            var emptyPage = new HashMap<Integer, ArrayList<T>>(1);
            emptyPage.put(1, new ArrayList<>(0));
            return emptyPage;
        }

        // pageAmount must always round up.
        int pageAmount = (int) Math.ceil(collection.size() / (double) maxPerPage);
        var pages = new HashMap<Integer, ArrayList<T>>(pageAmount);

        int count = 0;
        int page = 1;
        var list = new ArrayList<T>(maxPerPage);

        for (T t : collection) {
            list.add(t);

            if (++count == maxPerPage) {
                pages.put(page++, list);
                list = new ArrayList<>(maxPerPage);
                count = 0;
            }
        }

        if (!list.isEmpty()) pages.put(page, list);

        return pages;
    }

    /**
     * Repeats a char the specified amount of times.
     *
     * @param repeat The char to be repeated.
     * @param times  The times to repeat the char.
     * @return A string consisting of only the char repeated the amount of specified times.
     */
    public static @NotNull String repeatChar(char repeat, long times)
    {
        var builder = new StringBuilder();

        for (long l = 0; l < times; ++l) {
            builder.append(repeat);
        }

        return builder.toString();
    }

    /**
     * Tests if the string is a valid namespaced key. Namespaced keys have a namespace and a key, they are separated by a colon,
     * e.g: minecraft:test. The namespace must have only [a-z0-9_.-] characters and the key [a-z0-9/._-] characters, both
     * cannot be empty. If you use a string without a colon, the characters for key are tested.
     *
     * @param namespacedKey The namespaced key to test.
     * @return If the argument is a valid namespaced key.
     */
    public static boolean isNamespacedKey(@NotNull String namespacedKey)
    {
        int colon = namespacedKey.indexOf(":");

        if (colon == -1) {
            namespacedKey = "minecraft:" + namespacedKey;
            colon = 9;
        }

        String namespace = namespacedKey.substring(0, colon);
        String key = namespacedKey.substring(colon + 1);

        return !namespace.isEmpty() && !key.isEmpty() && (namespace.length() + key.length() + 1) <= 256 && !invalidNamespaceCharacters.matcher(namespace).find() && !invalidKeyCharacters.matcher(key).find();
    }
}
