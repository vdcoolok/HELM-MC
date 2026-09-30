package dev.helm.mixin;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import dev.helm.command.popup.PopupBounds;
import dev.helm.command.popup.PopupCommands;
import dev.helm.command.popup.Mode;
import dev.helm.command.popup.PopupGate;
import dev.helm.command.popup.PopupKeys;
import dev.helm.command.popup.PopupPainter;
import dev.helm.command.popup.PopupPlacement;
import dev.helm.command.popup.PopupRow;
import dev.helm.command.popup.PopupRows;
import dev.helm.command.popup.PopupState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Shadow
    protected EditBox input;

    private boolean helmPopup;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void helmSyncPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                               float partialTick, CallbackInfo callback) {
        helmRefresh();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void helmDrawPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                               float partialTick, CallbackInfo callback) {
        if (!helmPopup) {
            return;
        }
        PopupState state = PopupState.instance();
        if (!state.isOpen()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        PopupPlacement placement = helmPlacement(state.rows(), font);
        PopupPainter.paint(graphics, font, placement, state.selected(), mouseX, mouseY);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void helmPopupKey(KeyEvent event, CallbackInfoReturnable<Boolean> callback) {
        helmRefresh();
        if (!helmPopup) {
            return;
        }
        PopupState state = PopupState.instance();

        if (PopupKeys.isUp(event)) {
            helmMove(-1);
        } else if (PopupKeys.isDown(event)) {
            helmMove(1);
        } else if (PopupKeys.isLeft(event)) {
            state.moveToColumnSlot(-1);
        } else if (PopupKeys.isRight(event)) {
            state.moveToColumnSlot(1);
        } else if (PopupKeys.isTab(event)) {
            if (state.selected() < 0) {
                state.select(0);
            }
            helmApply();
        } else {
            return;
        }
        callback.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void helmPopupMouse(MouseButtonEvent event, boolean doubleClick,
                                CallbackInfoReturnable<Boolean> callback) {
        if (!helmPopup || !PopupState.instance().isOpen()) {
            return;
        }
        if (event.buttonInfo().button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        PopupState state = PopupState.instance();
        int row = helmPlacement(state.rows(), font).rowAt((int) event.x(), (int) event.y());
        if (row < 0) {
            return;
        }
        state.select(row);
        helmApply();
        callback.setReturnValue(true);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void helmPopupScroll(double horizontal, double vertical, double dx, double dy,
                                 CallbackInfoReturnable<Boolean> callback) {
        if (!helmPopup || !PopupState.instance().isOpen()) {
            return;
        }
        PopupState state = PopupState.instance();
        Font font = Minecraft.getInstance().font;
        PopupPlacement placement = helmPlacement(state.rows(), font);
        if (!placement.scrolls()) {
            return;
        }
        if (state.scrollBy(vertical < 0 ? 1 : -1,
                placement.totalLines() - placement.visibleLines())) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void helmPopupClosed(CallbackInfo callback) {
        helmClose();
    }

    private void helmMove(int delta) {
        PopupState state = PopupState.instance();
        state.moveWithinColumn(delta);
        PopupPlacement placement = helmPlacement(state.rows(), Minecraft.getInstance().font);
        if (state.current() != null) {
            state.reveal(state.currentRow(), placement.visibleLines(), placement.totalLines());
        }
    }

    private void helmApply() {
        PopupState state = PopupState.instance();
        PopupRow row = state.current();
        if (row == null) {
            return;
        }
        String command = PopupCommands.forRow(row);
        String value = input.getValue();
        int cursor = input.getCursorPosition();
        int start = PopupGate.tokenStart(value, cursor);
        String replaced = value.substring(0, start) + command + value.substring(cursor);
        int next = start + command.length();
        input.setValue(replaced);
        input.setCursorPosition(next);
        input.setHighlightPos(next);
        helmRefresh();
    }

    private PopupPlacement helmPlacement(List<PopupRow> rows, Font font) {
        return PopupPlacement.of(rows, font, input.getX(), input.getY(), PopupBounds.width(),
                PopupRows.columnsUsed(rows), PopupState.instance().offset());
    }

    private void helmRefresh() {
        String value = inputValue();
        Mode mode = PopupGate.mode(value, input == null ? 0 : input.getCursorPosition());
        if (mode == Mode.NONE) {
            helmClose();
            return;
        }
        List<PopupRow> offered = mode == Mode.INPUTS
                ? PopupRows.inputs()
                : PopupRows.build();
        String partial = PopupGate.partial(value, input.getCursorPosition() - 1);
        List<PopupRow> filtered = PopupRows.filter(offered, partial);
        if (filtered.isEmpty()) {
            helmClose();
            return;
        }
        helmPopup = true;
        PopupState state = PopupState.instance();
        if (!state.rows().equals(filtered)) {
            state.open(filtered);
        }
    }

    private String inputValue() {
        return input == null ? "" : input.getValue();
    }

    private void helmClose() {
        helmPopup = false;
        PopupState.instance().close();
    }
}