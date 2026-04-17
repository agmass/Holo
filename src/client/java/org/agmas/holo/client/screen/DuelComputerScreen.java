package org.agmas.holo.client.screen;

import net.fabricmc.api.EnvType;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

public class DuelComputerScreen extends Screen {

    public DuelComputerScreen(BlockState baseState) {
        super(Text.literal("Duel Computer Configuration Screen"));
    }


    public ButtonWidget alwaysPottedButton;
    public boolean alwaysPotted = false;
    public ButtonWidget holoSaturationButton;
    public boolean holoSaturation = true;
    public ButtonWidget noEnchantsButton;
    public boolean noEnchants = false;
    public SliderWidget worldBorderSizeButton;

    @Override
    protected void init() {

    }

    @Override
    public void tick() {
        super.tick();
    }
}
