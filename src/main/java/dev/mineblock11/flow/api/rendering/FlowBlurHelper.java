package dev.mineblock11.flow.api.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.mineblock11.flow.api.events.WindowResizeEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL30;

/**
 *
 * Credit to glisco for <a href="https://github.com/wisp-forest/owo-lib/blob/1.20/src/main/java/io/wispforest/owo/shader/BlurProgram.java">BlurProgram</a>
 */
public class FlowBlurHelper {
    public static void apply(float width, float height, DrawContext context, float size, float quality) {
        var buffer = Tessellator.getInstance().getBuffer();
        var matrix = context.getMatrices().peek().getPositionMatrix();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(matrix, 0, 0, 0).next();
        buffer.vertex(matrix, 0, height, 0).next();
        buffer.vertex(matrix, width, height, 0).next();
        buffer.vertex(matrix, width, 0, 0).next();

        FlowBlurHelper.INSTANCE.setParameters(16, quality, size);
        FlowBlurHelper.INSTANCE.use();

        Tessellator.getInstance().draw();
    }
    public static FlowBlurHelper INSTANCE = new FlowBlurHelper();
    public boolean loaded = false;
    private GlUniform inputResolution;
    private GlUniform directions;
    private GlUniform quality;
    private GlUniform size;
    private Framebuffer input;
    private ShaderProgram backingProgram;

    public FlowBlurHelper() {
        WindowResizeEvent.EVENT.register((width, height) -> {
            if (this.input == null) return;
            this.input.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    public void load(ShaderProgram backingProgram) {
        this.backingProgram = backingProgram;
        this.setup();

        this.loaded = true;
    }

    public void setParameters(int directions, float quality, float size) {
        this.directions.set((float) directions);
        this.size.set(size);
        this.quality.set(quality);
    }

    public void use() {
        Framebuffer buffer = MinecraftClient.getInstance().getFramebuffer();

        this.input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);

        this.inputResolution.set((float) buffer.textureWidth, (float) buffer.textureHeight);
        this.backingProgram.addSampler("InputSampler", this.input.getColorAttachment());

        RenderSystem.setShader(() -> this.backingProgram);
    }

    protected void setup() {
        this.inputResolution = this.findUniform("InputResolution");
        this.directions = this.findUniform("Directions");
        this.quality = this.findUniform("Quality");
        this.size = this.findUniform("Size");

        Window window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }

    private GlUniform findUniform(String key) {
        return backingProgram.getUniform(key);
    }
}