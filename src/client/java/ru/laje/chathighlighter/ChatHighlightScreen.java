package ru.laje.chathighlighter;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ChatHighlightScreen extends Screen {
    private static final int ROWS_PER_PAGE = 7;
    private static final int[] COLORS = {
            0xFFFF55,
            0xFFAA00,
            0x55FFFF,
            0x55FF55,
            0xFF5555,
            0xFF55FF,
            0xFFFFFF
    };

    private final Screen parent;
    private TextFieldWidget inputField;
    private int page = 0;
    private String preservedInput = "";

    public ChatHighlightScreen(Screen parent) {
        super(Text.translatable("screen.chat_highlighter.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();

        int centerX = this.width / 2;
        int left = centerX - 155;
        int top = 42;

        addDrawableChild(ButtonWidget.builder(toggleText(ChatHighlighterClient.config.enabled,
                        "screen.chat_highlighter.enabled.on", "screen.chat_highlighter.enabled.off"), button -> {
                    ChatHighlighterClient.config.enabled = !ChatHighlighterClient.config.enabled;
                    ChatHighlighterClient.saveConfig();
                    button.setMessage(toggleText(ChatHighlighterClient.config.enabled,
                            "screen.chat_highlighter.enabled.on", "screen.chat_highlighter.enabled.off"));
                })
                .dimensions(left, top, 98, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(toggleText(ChatHighlighterClient.config.caseSensitive,
                        "screen.chat_highlighter.case.on", "screen.chat_highlighter.case.off"), button -> {
                    ChatHighlighterClient.config.caseSensitive = !ChatHighlighterClient.config.caseSensitive;
                    ChatHighlighterClient.saveConfig();
                    button.setMessage(toggleText(ChatHighlighterClient.config.caseSensitive,
                            "screen.chat_highlighter.case.on", "screen.chat_highlighter.case.off"));
                })
                .dimensions(left + 106, top, 98, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(toggleText(ChatHighlighterClient.config.wholeWords,
                        "screen.chat_highlighter.words.on", "screen.chat_highlighter.words.off"), button -> {
                    ChatHighlighterClient.config.wholeWords = !ChatHighlighterClient.config.wholeWords;
                    ChatHighlighterClient.saveConfig();
                    button.setMessage(toggleText(ChatHighlighterClient.config.wholeWords,
                            "screen.chat_highlighter.words.on", "screen.chat_highlighter.words.off"));
                })
                .dimensions(left + 212, top, 118, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(colorText(), button -> {
                    ChatHighlighterClient.config.highlightColor = nextColor(ChatHighlighterClient.config.highlightColor);
                    ChatHighlighterClient.saveConfig();
                    button.setMessage(colorText());
                })
                .dimensions(centerX - 80, top + 24, 160, 20)
                .build());

        inputField = new TextFieldWidget(this.textRenderer, left, top + 52, 220, 20,
                Text.translatable("screen.chat_highlighter.input"));
        inputField.setMaxLength(64);
        inputField.setText(preservedInput);
        inputField.setFocused(true);
        addDrawableChild(inputField);
        setInitialFocus(inputField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.chat_highlighter.add"), button -> addKeyword())
                .dimensions(left + 228, top + 52, 102, 20)
                .build());

        page = clamp(page, 0, maxPage());
        int start = page * ROWS_PER_PAGE;
        int rowY = top + 84;

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            int keywordIndex = start + row;
            if (keywordIndex >= ChatHighlighterClient.config.keywords.size()) {
                break;
            }

            String keyword = ChatHighlighterClient.config.keywords.get(keywordIndex);
            addDrawableChild(ButtonWidget.builder(Text.translatable("screen.chat_highlighter.remove", keyword), button -> {
                        ChatHighlighterClient.config.removeKeyword(keywordIndex);
                        page = clamp(page, 0, maxPage());
                        preservedInput = inputField == null ? "" : inputField.getText();
                        rebuildWidgets();
                    })
                    .dimensions(left, rowY + row * 22, 330, 20)
                    .build());
        }

        int navY = this.height - 52;
        ButtonWidget previous = ButtonWidget.builder(Text.translatable("screen.chat_highlighter.prev"), button -> {
                    preservedInput = inputField == null ? "" : inputField.getText();
                    page = clamp(page - 1, 0, maxPage());
                    rebuildWidgets();
                })
                .dimensions(centerX - 155, navY, 90, 20)
                .build();
        previous.active = page > 0;
        addDrawableChild(previous);

        ButtonWidget next = ButtonWidget.builder(Text.translatable("screen.chat_highlighter.next"), button -> {
                    preservedInput = inputField == null ? "" : inputField.getText();
                    page = clamp(page + 1, 0, maxPage());
                    rebuildWidgets();
                })
                .dimensions(centerX + 65, navY, 90, 20)
                .build();
        next.active = page < maxPage();
        addDrawableChild(next);

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.chat_highlighter.done"), button -> close())
                .dimensions(centerX - 50, this.height - 28, 100, 20)
                .build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            addKeyword();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void addKeyword() {
        if (inputField == null) {
            return;
        }

        preservedInput = "";
        if (ChatHighlighterClient.config.addKeyword(inputField.getText())) {
            page = maxPage();
        }
        rebuildWidgets();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 16, 0xFFFFFF);

        if (ChatHighlighterClient.config.keywords.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("screen.chat_highlighter.empty"), centerX, 145, 0xAAAAAA);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("screen.chat_highlighter.page", page + 1, maxPage() + 1),
                    centerX, this.height - 47, 0xAAAAAA);
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private static Text toggleText(boolean value, String onKey, String offKey) {
        return Text.translatable(value ? onKey : offKey);
    }

    private static Text colorText() {
        return Text.translatable("screen.chat_highlighter.color",
                String.format("%06X", ChatHighlighterClient.config.highlightColor & 0xFFFFFF));
    }

    private static int nextColor(int current) {
        int rgb = current & 0xFFFFFF;
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == rgb) {
                return COLORS[(i + 1) % COLORS.length];
            }
        }
        return COLORS[0];
    }

    private static int maxPage() {
        int size = ChatHighlighterClient.config.keywords.size();
        if (size == 0) {
            return 0;
        }
        return (size - 1) / ROWS_PER_PAGE;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
