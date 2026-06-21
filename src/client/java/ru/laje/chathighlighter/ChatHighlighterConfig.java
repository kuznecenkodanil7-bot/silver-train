package ru.laje.chathighlighter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ChatHighlighterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "chat_highlighter.json";

    public boolean enabled = true;
    public boolean caseSensitive = false;
    public boolean wholeWords = false;
    public int highlightColor = 0xFFFF55;
    public List<String> keywords = new ArrayList<>();

    public static ChatHighlighterConfig load() {
        Path path = configPath();

        if (!Files.exists(path)) {
            ChatHighlighterConfig fresh = new ChatHighlighterConfig();
            fresh.save();
            return fresh;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            ChatHighlighterConfig loaded = GSON.fromJson(reader, ChatHighlighterConfig.class);
            if (loaded == null) {
                loaded = new ChatHighlighterConfig();
            }
            loaded.sanitize();
            return loaded;
        } catch (Exception ignored) {
            ChatHighlighterConfig fallback = new ChatHighlighterConfig();
            fallback.save();
            return fallback;
        }
    }

    public void save() {
        sanitize();
        Path path = configPath();

        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
            // The game should not crash only because a config file could not be written.
        }
    }

    public boolean addKeyword(String rawKeyword) {
        if (rawKeyword == null) {
            return false;
        }

        String keyword = rawKeyword.trim();
        if (keyword.isEmpty()) {
            return false;
        }

        for (String existing : keywords) {
            if (existing.equalsIgnoreCase(keyword)) {
                return false;
            }
        }

        keywords.add(keyword);
        save();
        return true;
    }

    public void removeKeyword(int index) {
        if (index >= 0 && index < keywords.size()) {
            keywords.remove(index);
            save();
        }
    }

    private void sanitize() {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }

        Set<String> seen = new LinkedHashSet<>();
        List<String> cleaned = new ArrayList<>();

        for (String keyword : keywords) {
            if (keyword == null) {
                continue;
            }

            String trimmed = keyword.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String identity = trimmed.toLowerCase(Locale.ROOT);
            if (seen.add(identity)) {
                cleaned.add(trimmed);
            }
        }

        keywords = cleaned;
        highlightColor = highlightColor & 0xFFFFFF;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}
