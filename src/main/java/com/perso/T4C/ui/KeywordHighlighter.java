package com.perso.T4C.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure text-analysis helper: finds which substrings of an NPC's dialogue response are one of that
 * NPC's own recognized keywords, so the UI can render them in a distinct color. This tells the
 * player, at a glance, what they can type next.
 *
 * <p>Deliberately free of any LibGDX/rendering dependency so the matching logic can be unit
 * tested in isolation.
 */
public final class KeywordHighlighter {

  private KeywordHighlighter() {}

  /** A half-open [start, end) character range within the source text that matched a keyword. */
  public record Span(int start, int end) {}

  /** A run of text, tagged with whether it matched one of the NPC's keywords. */
  public record Segment(String text, boolean highlighted) {}

  /**
   * Finds every whole-word, case-insensitive occurrence of any of {@code keywords} within {@code
   * text}. Matches never overlap: when two keywords could both match the same characters (e.g. a
   * short keyword inside a longer one), the longest keyword wins for that span. Returned spans are
   * ordered by position.
   */
  public static List<Span> findKeywordSpans(String text, Collection<String> keywords) {

    List<Span> spans = new ArrayList<>();

    if (text == null || text.isEmpty() || keywords == null || keywords.isEmpty()) return spans;

    List<String> ordered =
        keywords.stream()
            .filter(k -> k != null && !k.isBlank())
            .map(String::trim)
            .distinct()
            .sorted((a, b) -> b.length() - a.length())
            .toList();

    if (ordered.isEmpty()) return spans;

    boolean[] covered = new boolean[text.length()];

    for (String keyword : ordered) {

      Pattern pattern =
          Pattern.compile(
              "\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

      Matcher matcher = pattern.matcher(text);

      while (matcher.find()) {

        int start = matcher.start();
        int end = matcher.end();

        if (start == end) continue;

        boolean overlaps = false;

        for (int i = start; i < end; i++) {
          if (covered[i]) {
            overlaps = true;
            break;
          }
        }

        if (overlaps) continue;

        for (int i = start; i < end; i++) covered[i] = true;

        spans.add(new Span(start, end));
      }
    }

    spans.sort((a, b) -> Integer.compare(a.start(), b.start()));

    return spans;
  }

  /**
   * Splits {@code text} into an ordered run of {@link Segment}s covering the whole string, where
   * every segment matching a keyword (per {@link #findKeywordSpans}) is marked highlighted. When
   * nothing matches, this returns a single, non-highlighted segment holding the whole text
   * unchanged.
   */
  public static List<Segment> segmentsFor(String text, Collection<String> keywords) {

    List<Segment> segments = new ArrayList<>();

    if (text == null || text.isEmpty()) return segments;

    List<Span> spans = findKeywordSpans(text, keywords);

    if (spans.isEmpty()) {

      segments.add(new Segment(text, false));

      return segments;
    }

    int cursor = 0;

    for (Span span : spans) {

      if (span.start() > cursor) segments.add(new Segment(text.substring(cursor, span.start()), false));

      segments.add(new Segment(text.substring(span.start(), span.end()), true));

      cursor = span.end();
    }

    if (cursor < text.length()) segments.add(new Segment(text.substring(cursor), false));

    return segments;
  }
}
