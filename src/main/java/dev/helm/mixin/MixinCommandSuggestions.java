package dev.helm.mixin;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.suggestion.Suggestions;
import dev.helm.command.CommandRegistry;
import dev.helm.command.CommandSystem;
import dev.helm.command.DollarSuggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public abstract class MixinCommandSuggestions {

    @Shadow
    private EditBox input;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void helmSuggestCommandLine(CallbackInfo callback) {
        String value = input.getValue();
        if (!CommandSystem.isCommandLine(value)) {
            return;
        }

        Suggestions suggestions = DollarSuggestions.build(CommandSystem.registry(), value, input.getCursorPosition());
        CommandSuggestions self = (CommandSuggestions) (Object) this;

        if (suggestions.isEmpty()) {
            pendingSuggestions = null;
            self.hide();
            callback.cancel();
            return;
        }

        pendingSuggestions = CompletableFuture.completedFuture(suggestions);
        self.showSuggestions(true);
        callback.cancel();
    }
}
