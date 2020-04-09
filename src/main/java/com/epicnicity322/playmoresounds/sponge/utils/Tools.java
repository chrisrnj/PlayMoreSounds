package com.epicnicity322.playmoresounds.sponge.utils;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class Tools
{
    public static Text formatColorCodes(String string)
    {
        return TextSerializers.FORMATTING_CODE.deserialize(string);
    }
}
