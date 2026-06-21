package ru.laje.chathighlighter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ChatHighlighterClient implements ClientModInitializer {
    public static final String MOD_ID = "chat_highlighter";

    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(
            Identifier.of(MOD_ID, "chat_highlighter")
    );

    private static KeyBinding openMenuKey;
    public static ChatHighlighterConfig config;

    @Override
    public void onInitializeClient() {
        config = ChatHighlighterConfig.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chat_highlighter.open",
                GLFW.GLFW_KEY_H,
                CATEGORY
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
