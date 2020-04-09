package com.epicnicity322.playmoresounds.sponge.utils;

import com.epicnicity322.playmoresounds.sponge.PlayMoreSounds;

public class PMSLogger
{
    public static void log(String message)
    {
        PlayMoreSounds.GAME.getServer().getConsole().sendMessage(Tools.formatColorCodes("&6[&9PlayMoreSounds&6] " + message));
    }
}
