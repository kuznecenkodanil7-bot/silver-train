package ru.laje.chathighlighter;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class KeywordHighlighter {
    private KeywordHighlighter() {
    }

    public static Text highlight(Text original) {
        ChatHighlighterConfig config = ChatHighlighterClient.config;
        if (original == null || config == null || !config.enabled || config.keywords.isEmpty()) {
            return original;
        }

        MutableText highlighted = Text.empty();
        original.visit((style, segment) -> {
            appendHighlightedSegment(highlighted, segment, style, config);
            return Optional.empty();
        }, Style.EMPTY);

        return highlighted;
    }

    private static void appendHighlightedSegment(MutableText output, String segment, Style style, ChatHighlighterConfig config) {
        if (segment == null || segment.isEmpty()) {
            return;
        }

        List<String> keywords = config.keywords.stream()
                .filter(keyword -> keyword != null && !keyword.isBlank())
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();

        if (keywords.isEmpty()) {
            output.append(Text.literal(segment).setStyle(style));
            return;
        }

        String comparableSegment = config.caseSensitive ? segment : segment.toLowerCase(Locale.ROOT);
        int cursor = 0;

        while (cursor < segment.length()) {
            Match match = findNextMatch(segment, comparableSegment, keywords, cursor, config);

            if (match == null) {
                output.append(Text.literal(segment.substring(cursor)).setStyle(style));
                break;
            }

            if (match.start > cursor) {
                output.append(Text.literal(segment.substring(cursor, match.start)).setStyle(style));
            }

            Style highlightStyle = style
                    .withColor(TextColor.fromRgb(config.highlightColor))
                    .withBold(true);
            output.append(Text.literal(segment.substring(match.start, match.end)).setStyle(highlightStyle));
            cursor = match.end;
        }
    }

    private static Match findNextMatch(String source, String comparableSource, List<String> keywords, int from, ChatHighlighterConfig config) {
        Match best = null;

        for (String keyword : keywords) {
            String comparableKeyword = config.caseSensitive ? keyword : keyword.toLowerCase(Locale.ROOT);
            int start = comparableSource.indexOf(comparableKeyword, from);

            while (start >= 0) {
                int end = start + keyword.length();
                if (!config.wholeWords || isWholeWord(source, start, end)) {
                    if (best == null || start < best.start || (start == best.start && end > best.end)) {
                        best = new Match(start, end);
                    }
                    break;
                }
                start = comparableSource.indexOf(comparableKeyword, start + 1);
            }
        }

        return best;
    }

    private static boolean isWholeWord(String source, int start, int end) {
        boolean hasLeftWordChar = start > 0 && isWordChar(source.charAt(start - 1));
        boolean hasRightWordChar = end < source.length() && isWordChar(source.charAt(end));
        return !hasLeftWordChar && !hasRightWordChar;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private record Match(int start, int end) {
    }
}
