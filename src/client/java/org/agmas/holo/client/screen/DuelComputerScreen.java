package org.agmas.holo.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.agmas.holo.block.BattleHologramComputer;
import org.agmas.holo.block.entity.HologramControllerBlockEntity;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.util.BattleHologramComputerEntry;
import org.agmas.holo.util.payloads.UpdateHologramOptionsC2SPacket;

public class DuelComputerScreen extends Screen {

    HologramControllerBlockEntity state;
    public DuelComputerScreen(HologramControllerBlockEntity baseState) {
        super(Text.literal("Duel Computer Configuration Screen"));
        state = baseState;
    }


    public CheckboxWidget alwaysPottedButton;
    public CheckboxWidget holoSaturationButton;
    public CheckboxWidget noEnchantsButton;
    public CheckboxWidget allowBuildingButton;
    public SliderWidget worldBorderSizeButton;
    public int worldBorderSize = 0;

    @Override
    public void close() {
        if (BattleHologramComputer.editing != null) {
            MinecraftClient.getInstance().execute(()->{
                BattleHologramComputerEntry.BattleHologramOptions options = new BattleHologramComputerEntry.BattleHologramOptions();
                options.alwaysHaveBestPotions = alwaysPottedButton.isChecked();
                options.holoSaturation = holoSaturationButton.isChecked();
                options.noEnchantments = noEnchantsButton.isChecked();
                options.allowBuilding = allowBuildingButton.isChecked();
                options.worldBorderSize = worldBorderSize;
                ClientPlayNetworking.send(new UpdateHologramOptionsC2SPacket(BattleHologramComputer.editing, options));
            });
        }
        super.close();
    }

    @Override
    protected void init() {
        alwaysPottedButton = CheckboxWidget.builder(Text.of("Speed, Strength and Fire Res"), MinecraftClient.getInstance().textRenderer)
                .pos(20,20)
                .checked(state.hologramOptions.alwaysHaveBestPotions)
                .build();
        holoSaturationButton = CheckboxWidget.builder(Text.of("Battle Holo Saturation"), MinecraftClient.getInstance().textRenderer)
                .pos(20,60)
                .checked(state.hologramOptions.holoSaturation)
                .build();
        noEnchantsButton = CheckboxWidget.builder(Text.of("No Enchantments"), MinecraftClient.getInstance().textRenderer)
                .pos(20,100)
                .checked(state.hologramOptions.noEnchantments)
                .build();
        allowBuildingButton = CheckboxWidget.builder(Text.of("Allow Building"), MinecraftClient.getInstance().textRenderer)
                .pos(20,140)
                .checked(state.hologramOptions.allowBuilding)
                .build();
        worldBorderSizeButton = new SliderWidget(20,180,200,20,Text.of("Worldborder Size: " + (int) Math.floor(state.hologramOptions.worldBorderSize)), state.hologramOptions.worldBorderSize/65f) {
            @Override
            protected void updateMessage() {
                if (value == 1) {
                    setMessage(Text.of("Worldborder Size: Infinite"));
                } else {
                    setMessage(Text.of("Worldborder Size: " + (int) Math.floor(value*65)));
                }
            }

            @Override
            protected void applyValue() {
                worldBorderSize = (int) Math.floor(value*65);
                if (worldBorderSize <= 0) worldBorderSize = 1;
            }
        };
        worldBorderSize = state.hologramOptions.worldBorderSize;


        addDrawableChild(alwaysPottedButton);
        addDrawableChild(holoSaturationButton);
        addDrawableChild(noEnchantsButton);
        addDrawableChild(allowBuildingButton);
        addDrawableChild(worldBorderSizeButton);


    }

    @Override
    public void tick() {
        super.tick();
    }
}
