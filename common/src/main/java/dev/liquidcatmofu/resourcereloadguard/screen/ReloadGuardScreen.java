package dev.liquidcatmofu.resourcereloadguard.screen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class ReloadGuardScreen extends Screen {
    public record Action(Component label, Runnable callback, boolean dangerous) {}
    private final Screen returnScreen;
    private final List<Component> lines;
    private final List<Action> actions;
    private final int delayTicks;
    private final Runnable closeCallback;
    private final List<Button> dangerousButtons = new ArrayList<>();
    private List<FormattedCharSequence> configHintLines = List.of();
    private int ticks;
    private long openedAtNanos;

    public ReloadGuardScreen(Component title, Screen returnScreen, List<Component> lines, List<Action> actions,
                             int delayTicks, Runnable closeCallback) {
        super(title);
        this.returnScreen = returnScreen;
        this.lines = List.copyOf(lines);
        this.actions = List.copyOf(actions);
        this.delayTicks = Math.max(0, delayTicks);
        this.closeCallback = closeCallback;
    }

    @Override
    protected void init() {
        dangerousButtons.clear();
        ticks = 0;
        openedAtNanos = System.nanoTime();
        configHintLines = font.split(Component.translatable("resource_reload_guard.screen.config_hint"), Math.max(100, width - 20));
        int configHintHeight = configHintLines.size() * 10;
        int buttonWidth = Math.min(240, Math.max(120, width - 40));
        int startY = Math.max(32, Math.min(height - 36 - configHintHeight - actions.size() * 24, 92 + lines.size() * 12));
        Button safeFocus = null;
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            Button button = Button.builder(action.label(), ignored -> action.callback().run())
                .bounds((width - buttonWidth) / 2, startY + i * 24, buttonWidth, 20).build();
            if (action.dangerous()) {
                button.active = delayTicks == 0;
                dangerousButtons.add(button);
            } else if (safeFocus == null) safeFocus = button;
            addRenderableWidget(button);
        }
        if (safeFocus != null) setInitialFocus(safeFocus);
    }

    @Override
    public void tick() {
        ticks++;
        long minimumDelayNanos = delayTicks * 50_000_000L;
        if (ticks >= delayTicks && System.nanoTime() - openedAtNanos >= minimumDelayNanos)
            dangerousButtons.forEach(button -> button.active = true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 24, 0xFFFFFF);
        int y = 48;
        for (Component line : lines) {
            graphics.drawCenteredString(font, line, width / 2, y, 0xA0A0A0);
            y += 12;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        int hintY = height - 10 - configHintLines.size() * 10;
        for (FormattedCharSequence hintLine : configHintLines) {
            graphics.drawCenteredString(font, hintLine, width / 2, hintY, 0x808080);
            hintY += 10;
        }
    }

    @Override
    public void onClose() {
        closeCallback.run();
        minecraft.setScreen(returnScreen);
    }

    @Override
    public Component getNarrationMessage() {
        return Component.empty().append(title).append(". ")
            .append(Component.translatable("resource_reload_guard.screen.narration")).append(". ")
            .append(Component.translatable("resource_reload_guard.screen.config_hint"));
    }
}
