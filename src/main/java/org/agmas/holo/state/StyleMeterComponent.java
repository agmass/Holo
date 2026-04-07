package org.agmas.holo.state;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class StyleMeterComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<StyleMeterComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Holo.MOD_ID, "style"), StyleMeterComponent.class);
    private final PlayerEntity player;
    public int consecutiveHits = 0;
    public float c_totalStyleMeter =0;
    public int timeInAir = 0;
    public Item lastItemUsed = null;
    public HashMap<StyleReason, StylePoints> stylePoints = new HashMap<>();

    public StyleMeterComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        sync();
    }

    public void serverTick() {
        stylePoints.values().removeIf((stylePoint -> {
            if (stylePoint.ticksUntilDecay-- <= 0) {
                return stylePoint.stylePoints-- <= 0;
            }
            return false;
        }));
        if (!player.isOnGround()) {
            timeInAir++;
            if (timeInAir % 20 == 0) {
                addStylePoints(StyleReason.AIRTIME);
            }
        } else {
            timeInAir= 0;
        }
        sync();
    }

    public void addStylePoints(StyleReason reason) {
        if (!stylePoints.containsKey(reason)) {
            stylePoints.put(reason, new StylePoints());
        }
        stylePoints.get(reason).stylePoints += reason.pointsPerTick;
        stylePoints.get(reason).ticksUntilDecay = reason.baseTicksUntilDecay;
        stylePoints.get(reason).combo += 1;
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("combo", consecutiveHits);
        AtomicReference<Float> totalStylePoints = new AtomicReference<>((float) 0);
        NbtCompound stylePointsCompound = new NbtCompound();
        stylePoints.forEach(((reason, points) -> {
            NbtCompound pointCompount = new NbtCompound();
            pointCompount.putFloat("points", points.stylePoints);
            pointCompount.putFloat("combo", points.combo);
            totalStylePoints.updateAndGet(v -> (v + points.stylePoints));
            stylePointsCompound.put(reason.ordinal()+"", pointCompount);
        }));
        tag.putFloat("totalStylePoints", totalStylePoints.get());
        tag.put("stylePoints", stylePointsCompound);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        consecutiveHits = tag.getInt("combo");
        stylePoints.clear();
        NbtCompound stylePointsCompound = tag.getCompound("stylePoints");
        c_totalStyleMeter = tag.getFloat("totalStylePoints");
        for (String key : stylePointsCompound.getKeys()) {
            StylePoints points = new StylePoints();
            points.stylePoints = stylePointsCompound.getCompound(key).getFloat("points");
            points.combo = stylePointsCompound.getCompound(key).getFloat("combo");
            stylePoints.put(StyleReason.values()[Integer.valueOf(key)], points);
        }
    }

    public static class StylePoints {
        public int ticksUntilDecay = 0;
        public float stylePoints = 0;
        public float combo = 0;
    }


    public enum StyleReason {
        KILLED_MOB(seconds(2.5f),40),
        KILLED_PLAYER(seconds(10),200),
        ARSENAL(seconds(3),10),
        RANGED(seconds(10),10),
        CRITICAL(seconds(2.5f),5),
        BLOCK_DAMAGE(seconds(5),20),
        BERSERK(seconds(5),40),
        AIRTIME(40,10),
        SHIELD_BREAK(seconds(10),30),
        DAMAGE(seconds(1.5f),1),
        TEN_COMBO(seconds(20f),90),
        RANGED_KILL(seconds(15),200),
        SMASH(seconds(5),40);

        final public int baseTicksUntilDecay;
        final public int pointsPerTick;

        StyleReason(int baseTicksUntilDecay, int pointsPerTick) {
            this.baseTicksUntilDecay = baseTicksUntilDecay;
            this.pointsPerTick = pointsPerTick;
        }
        public static int seconds(float time) {
            return Math.round(time*20);
        }
    }
}




























