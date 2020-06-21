package com.epicnicity322.playmoresounds.core.addons.events;

import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import org.jetbrains.annotations.NotNull;

public interface AddonLoadUnloadEvent
{
    void onLoadUnload(@NotNull PMSAddon addon);
}
