package dev.helm.mixin;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.suggestion.Suggestions;
import dev.helm.command.CommandTree;
import dev.helm.command.chat.DollarPrefix;
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
        if (!DollarPrefix.isCommand(value)) {
            return;
        }

        String body = DollarPrefix.body(value);
        int cursor = Math.max(0, input.getCursorPosition() - 1);
        CommandTree tree = CommandTree.instance();

        input.setSuggestion(tree.placeholder(body, cursor));

        Suggestions suggestions = tree.complete(body, cursor);
        CommandSuggestions self = (CommandSuggestions) (Object) this;

        if (suggestions.isEmpty()) {
            pendingSuggestions = null;
            self.hide();
        } else {
            pendingSuggestions = CompletableFuture.completedFuture(suggestions);
            self.showSuggestions(true);
        }
        callback.cancel();
    }
}