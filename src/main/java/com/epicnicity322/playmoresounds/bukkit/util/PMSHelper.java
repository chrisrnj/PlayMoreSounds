package com.epicnicity322.playmoresounds.bukkit.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

public final class PMSHelper
{
    private static final @NotNull String chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";
    private static final int charsLength = chars.length();

    private PMSHelper()
    {
    }

    public static @NotNull String getRandomString(int length)
    {
        Random random = new Random();
        StringBuilder string = new StringBuilder();

        while (string.length() < length) {
            int index = Math.round(random.nextFloat() * charsLength);

            string.append(chars.charAt(index));
        }

        return string.toString();
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
}
