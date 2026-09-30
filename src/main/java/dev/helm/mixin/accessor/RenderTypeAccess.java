package dev.helm.mixin.accessor;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderType.class)
public class RenderTypeAccess {

    @Shadow
    static RenderType create(String name, RenderSetup setup) {
        throw new AssertionError();
    }

    public static RenderType helmCreate(String name, RenderSetup setup) {
        return create(name, setup);
    }
}
