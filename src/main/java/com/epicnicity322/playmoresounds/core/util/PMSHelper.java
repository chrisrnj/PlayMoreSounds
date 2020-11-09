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

package com.epicnicity322.playmoresounds.core.util;

import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public final class PMSHelper
{
    private static final @NotNull SecureRandom random = new SecureRandom();
    private static final @NotNull String chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";
    private static final int charsLength = chars.length();

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

        return now.getMonth() == Month.OCTOBER && now.getDayOfMonth() == 31 && Configurations.CONFIG.getPluginConfig().getConfiguration().getBoolean("Halloween Event").orElse(false);
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
}
