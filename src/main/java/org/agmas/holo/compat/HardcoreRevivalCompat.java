package org.agmas.holo.compat;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.api.PlayerAboutToKnockOutEvent;
import org.agmas.holo.state.HoloPlayerComponent;

public class HardcoreRevivalCompat {
    public static void register() {
        Balm.getEvents().onEvent(PlayerAboutToKnockOutEvent.class, (playerAboutToKnockOutEvent -> {
            if (HoloPlayerComponent.KEY.get(playerAboutToKnockOutEvent.getPlayer()).inHoloMode) {
                playerAboutToKnockOutEvent.setCanceled(true);
            }
        }));
    }
}
