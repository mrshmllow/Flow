package dev.mineblock11.flow.mixin.fixes.vanilla;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CartographyTableScreen;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoomScreen.class)
public class LoomFixMixin extends Screen {
    protected LoomFixMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void $invoke_render_background(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.renderBackground(context);
    }

    @Redirect(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/LoomScreen;renderBackground(Lnet/minecraft/client/gui/DrawContext;)V", ordinal = 0))
    public void $cancel_render_background(LoomScreen instance, DrawContext context) {}
}
