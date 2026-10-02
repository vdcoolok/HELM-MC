package dev.helm.mixin.accessor;

import java.util.List;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockModelRenderState.class)
public interface BlockModelAccess {

    @Accessor("modelParts")
    List<BlockStateModelPart> modelParts();

    @Accessor("transformation")
    Matrix4fc transformation();
}