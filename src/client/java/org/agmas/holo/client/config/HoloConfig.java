package org.agmas.holo.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "holo")
public class HoloConfig implements ConfigData {

    @Comment("Will permenantly enable the light framebuffers in veil.")
    public boolean useVeilLights = true;


}
