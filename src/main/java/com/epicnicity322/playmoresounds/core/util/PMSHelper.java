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

package com.epicnicity322.playmoresounds.core.util;

import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

public final class PMSHelper
{
    private static final @NotNull SecureRandom random = new SecureRandom();
    private static final @NotNull String chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";
    private static final int charsLength = chars.length();
    private static final Pattern invalidNamespaceCharacters = Pattern.compile("[^a-z0-9_.-]");
    private static final Pattern invalidKeyCharacters = Pattern.compile("[^a-z0-9/._-]");

    private PMSHelper()
    {
    }

    public static @NotNull String getRandomString(int length)
    {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; ++i)
            builder.append(chars.charAt(random.nextInt(charsLength)));

        return builder.toString();
    }

    public static boolean halloweenEvent()
    {
        LocalDateTime now = LocalDateTime.now();

        return now.getMonth() == Month.OCTOBER && now.getDayOfMonth() == 31 && Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Halloween Event").orElse(false);
    }

    public static boolean isChristmas()
    {
        LocalDateTime now = LocalDateTime.now();

        return now.getMonth() == Month.DECEMBER && now.getDayOfMonth() == 25;
    }

    public static <T> @NotNull HashMap<Long, ArrayList<T>> splitIntoPages(@NotNull Collection<T> collection,
                                                                          int maxPerPage)
    {
        HashMap<Long, ArrayList<T>> pages = new HashMap<>();

        if (collection.isEmpty())
            return pages;

        long l = 0;
        long page = 1;
        ArrayList<T> list = new ArrayList<>();

        for (T t : collection) {
            list.add(t);

            if (++l == maxPerPage) {
                pages.put(page++, list);
                list = new ArrayList<>();
                l = 0;
            }
        }

        pages.put(page, list);
        return pages;
    }

    /**
     * Tests if this is a valid namespaced key. Namespaced keys have a namespace and a key, they are separated by a colon,
     * e.g: minecraft:test. The namespace must have only [a-z0-9_.-] characters and the key [a-z0-9/._-] characters, both
     * cannot be empty. If you only input a key and not namespace, only the key is tested.
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

        return !namespace.isEmpty() && !key.isEmpty() && namespace.length() + key.length() + 1 <= 256 && !invalidNamespaceCharacters.matcher(namespace).find() && !invalidKeyCharacters.matcher(key).find();
    }
}
