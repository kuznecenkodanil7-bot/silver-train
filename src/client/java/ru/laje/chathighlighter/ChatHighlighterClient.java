package ru.laje.chathighlighter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ChatHighlighterClient implements ClientModInitializer {
    public static final String MOD_ID = "chat_highlighter";

    private static KeyBinding openMenuKey;
    public static ChatHighlighterConfig config;

    @Override
    public void onInitializeClient() {
        config = ChatHighlighterConfig.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chat_highlighter.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.chat_highlighter"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                client.setScreen(new ChatHighlightScreen(client.currentScreen));
            }
        });
    }

    public static void saveConfig() {
        if (config != null) {
            config.save();
        }
    }
}
