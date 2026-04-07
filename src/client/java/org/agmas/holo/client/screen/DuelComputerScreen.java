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


    public SimpleOption<Boolean> alwaysPottedButton;
    public boolean alwaysPotted = false;
    public SimpleOption<Boolean> holoSaturationButton;
    public boolean holoSaturation = true;
    public SimpleOption<Boolean> noEnchantsButton;
    public boolean noEnchants = false;
    public SliderWidget worldBorderSizeButton;

    @Override
    protected void init() {
        alwaysPottedButton = SimpleOption.ofBoolean("battlepotions",alwaysPotted);
        noEnchantsButton = SimpleOption.ofBoolean("noenchants",noEnchants);
        holoSaturationButton = SimpleOption.ofBoolean("normalsaturation",holoSaturation);

        addDrawableChild(alwaysPottedButton);
        addDrawableChild(holoSaturationButton.createWidget());
        addDrawableChild(noEnchantsButton);
    }

    @Override
    public void tick() {
        noEnchantsButton.
        super.tick();
    }
}
