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

package com.epicnicity322.playmoresounds.core.addons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class AddonUtil
{
    private AddonUtil()
    {
    }

    static void sortInTopologicalOrder(ArrayList<AddonDescription> toSort)
    {
        var allAddons = new ArrayList<>(toSort);
        toSort.clear();

        allAddons.stream()
                .map(addon -> convertToNode(addon, allAddons))
                .forEach(node -> {
                    if (!toSort.contains(node.addon())) {
                        add(node, toSort, allAddons);
                    }
                });
    }

    private static void add(Node node, ArrayList<AddonDescription> toAdd, ArrayList<AddonDescription> allAddons)
    {
        toAdd.add(node.addon());

        if (!node.dependants().isEmpty()) {
            for (AddonDescription dependant : node.dependants()) {
                toAdd.remove(dependant);
                add(convertToNode(dependant, allAddons), toAdd, allAddons);
            }
        }
    }

    private static Node convertToNode(AddonDescription toConvert, ArrayList<AddonDescription> allAddons)
    {
        var dependingOnMe = new HashSet<AddonDescription>();

        for (var addon : allAddons) {
            if (addon == toConvert) continue;
            if (addon.getAddonHooks().contains(toConvert.getName()) || addon.getRequiredAddons().contains(toConvert.getName())) {
                dependingOnMe.add(addon);
            }
        }

        return new Node(toConvert, dependingOnMe);
    }

    private record Node(AddonDescription addon, Set<AddonDescription> dependants)
    {
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof Node node)) return false;
            return addon.jar.equals(node.addon.jar);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(addon.jar);
        }
    }
}
