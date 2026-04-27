package org.agmas.holo.client.render;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

import static org.lwjgl.opengl.ARBInternalformatQuery2.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glCopyTexSubImage2D;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;

public class PhoneScreenTexture extends AbstractTexture {
    @Override
    public void load(ResourceManager manager) throws IOException {

    }

    public void copy(AdvancedFbo fbo) {
        int id = this.getGlId();
        int width = fbo.getWidth();
        int height = fbo.getHeight();
        TextureUtil.prepareImage(NativeImage.InternalFormat.RGBA, id, 4, width, height);

        RenderSystem.bindTexture(id);
        fbo.bindRead();
        glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
        AdvancedFbo.unbind();
        glGenerateMipmap(GL_TEXTURE_2D);
    }
}
