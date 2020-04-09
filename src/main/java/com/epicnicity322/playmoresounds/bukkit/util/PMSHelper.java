package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundType;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PMSHelper
{
    public static String addZerosToLeft(Integer value, int maxLength)
    {
        StringBuilder i = new StringBuilder(value.toString());

        while (i.length() < maxLength) {
            i.insert(0, "0");
        }

        return i.toString();
    }

    public static String getRandomString(int length)
    {
        String chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";
        Random random = new Random();
        StringBuilder string = new StringBuilder();

        while (string.length() < length) {
            int index = Math.round(random.nextFloat() * chars.length());

            string.append(chars.charAt(index));
        }

        return string.toString();
    }

    public static long formatTicks(String value)
    {
        value = value.trim();

        long l = Long.parseLong(value.substring(0, value.length() - 1));

        if (value.endsWith("h")) {
            return l * 60 * 60 * 20;
        } else if (value.endsWith("m")) {
            return l * 60 * 20;
        } else if (value.endsWith("s")) {
            return l * 20;
        } else {
            return Long.parseLong(value);
        }
    }

    public static String snakeCaseToSentence(String str)
    {
        str = str.toLowerCase().replace("_", " ");

        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = Pattern.compile("(\\b[a-z])").matcher(str);

        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, matcher.group().toUpperCase());
        }

        return matcher.appendTail(stringBuffer).toString();
    }

    public static FileConfiguration getConfig(String name)
    {
        return PlayMoreSounds.CONFIG.getConfig(Storage.TYPES.get(name));
    }

    public static void stopSound(@NotNull Player player, @Nullable HashSet<String> sounds, long delay)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (PlayMoreSounds.HAS_STOP_SOUND) {
                    if (sounds == null) {
                        for (String toStop : PlayMoreSounds.SOUND_LIST)
                            player.stopSound(SoundType.valueOf(toStop).getSoundOnVersion());
                    } else {
                        for (String sound : sounds) {
                            player.stopSound(sound);
                        }
                    }
                } else {
                    for (int i = 0; i < 70; ++i) {
                        player.playSound(player.getLocation(), Sound.valueOf("CHICKEN_HURT"), 1.0E-4f, 1.0f);
                    }
                }
            }
        }.runTaskLater(PlayMoreSounds.getPlugin(), delay);
    }

    public static HashMap<Integer, LinkedHashSet<String>> chopSet(LinkedHashSet<String> set, int maxPerPage)
    {
        HashMap<Integer, LinkedHashSet<String>> pages = new HashMap<>();

        if (set.isEmpty()) {
            return pages;
        }

        Iterator<String> iterator = set.iterator();

        int i = 0;
        int page = 1;

        LinkedHashSet<String> valuePerPage = new LinkedHashSet<>();

        while (iterator.hasNext()) {
            valuePerPage.add(iterator.next());
            ++i;

            if (i == maxPerPage) {
                i = 0;
                pages.put(page, valuePerPage);
                valuePerPage = new LinkedHashSet<>();
                ++page;
            }
        }

        pages.put(page, valuePerPage);
        return pages;
    }
}
