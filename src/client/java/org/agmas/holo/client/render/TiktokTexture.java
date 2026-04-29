package org.agmas.holo.client.render;


import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public class TiktokTexture extends AbstractTexture {
    public TiktokTexture(int id) {
        glId = id;
    }

    public int getId() {
        return this.glId;
    }

    public void releaseId() { /* NO OP */ }

    @Override
    public void load(ResourceManager manager) throws IOException {

    }

    @Override public void close() { /* NO OP */}
}