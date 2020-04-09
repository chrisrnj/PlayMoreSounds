package com.epicnicity322.playmoresounds.core.addons.events;

import com.epicnicity322.playmoresounds.core.addons.PMSAddon;

public interface AddonLoadUnloadEvent
{
    void onLoadUnload(PMSAddon addon, boolean isLoading);
}
