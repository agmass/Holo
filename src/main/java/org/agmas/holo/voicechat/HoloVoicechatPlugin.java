package org.agmas.holo.voicechat;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;

public class HoloVoicechatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return "holo";
    }

    public static VoicechatApi api;

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
        HoloVoicechatPlugin.api = api;
    }

}
