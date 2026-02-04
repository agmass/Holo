package org.agmas.holo.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "holo")
public class HoloConfig implements ConfigData {

    @Comment("Veil lights can cause flickering, stutters or lag. Enable for glowing holos and computers. May requiring restarting to take into effect.")
    public boolean useVeilLights = false;


}
