/*
 * PlayMoreSounds - A minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023 Christiano Rangel
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

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class PMSUtils {
    @NotNull
    private static final Pattern invalidNamespaceCharacters = Pattern.compile("[^a-z0-9_.-]");
    @NotNull
    private static final Pattern invalidKeyCharacters = Pattern.compile("[^a-z0-9/._-]");

    private PMSUtils() {
    }

    /**
     * Tests if the string is a valid namespaced key. Namespaced keys have a namespace and a key, they are separated by a colon,
     * e.g: minecraft:test. The namespace must have only [a-z0-9_.-] characters and the key [a-z0-9/._-] characters, both
     * cannot be empty. If you use a string without a colon, only the characters for key are checked.
     *
     * @param namespacedKey The namespaced key to validate.
     * @return If the argument is a valid namespaced key.
     */
    public static boolean isNamespacedKey(@NotNull String namespacedKey) {
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
