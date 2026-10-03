package dev.helm.outline.block;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockSilhouetteModels {

    private static final BlockSilhouetteModels INSTANCE = new BlockSilhouetteModels();

    private final Map<BlockState, BlockModelRenderState> resolved = new HashMap<>();
    private ModelManager manager;
    private BlockModelResolver resolver;

    private BlockSilhouetteModels() {
    }

    public static BlockSilhouetteModels instance() {
        return INSTANCE;
    }

    public void reset() {
        resolved.clear();
    }

    public BlockModelRenderState of(BlockState state) {
        BlockModelRenderState model = resolved.get(state);
        if (model != null) {
            return model;
        }
        model = new BlockModelRenderState();
        resolver().update(model, state, BlockDisplayContext.create());
        if (model.isEmpty()) {
            return null;
        }
        resolved.put(state, model);
        return model;
    }

    private BlockModelResolver resolver() {
        ModelManager current = Minecraft.getInstance().getModelManager();
        if (resolver == null || manager != current) {
            manager = current;
            resolver = new BlockModelResolver(current);
        }
        return resolver;
    }
}
