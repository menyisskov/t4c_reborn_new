package com.perso.T4C.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class KeywordHighlighterTest {

  @Test
  void findsAWholeWordCaseInsensitiveKeywordMatch() {
    String text = "I can show you the CHART if you like.";
    List<KeywordHighlighter.Span> spans = KeywordHighlighter.findKeywordSpans(text, List.of("chart"));

    assertEquals(1, spans.size());
    KeywordHighlighter.Span span = spans.get(0);
    assertEquals("CHART", text.substring(span.start(), span.end()));
  }

  @Test
  void doesNotMatchAKeywordInsideALongerWord() {
    // "chart" must not light up inside "charter" - only whole-word matches count.
    String text = "This ship sails under an old charter.";
    List<KeywordHighlighter.Span> spans = KeywordHighlighter.findKeywordSpans(text, List.of("chart"));

    assertTrue(spans.isEmpty());
  }

  @Test
  void segmentsForSplitsTheTextAroundEachMatch() {
    String text = "Ask me about the quest or the key.";
    List<KeywordHighlighter.Segment> segments =
        KeywordHighlighter.segmentsFor(text, List.of("quest", "key"));

    StringBuilder rebuilt = new StringBuilder();
    for (KeywordHighlighter.Segment segment : segments) rebuilt.append(segment.text());
    assertEquals(text, rebuilt.toString(), "segments must reconstruct the original text exactly");

    assertTrue(segments.stream().anyMatch(s -> s.highlighted() && s.text().equals("quest")));
    assertTrue(segments.stream().anyMatch(s -> s.highlighted() && s.text().equals("key")));
    assertTrue(segments.stream().noneMatch(s -> s.highlighted() && s.text().equals("about")));
  }

  @Test
  void withNoMatchingKeywordsTheWholeTextIsASingleUnhighlightedSegment() {
    String text = "Nothing here matches any known topic.";
    List<KeywordHighlighter.Segment> segments = KeywordHighlighter.segmentsFor(text, List.of("chart"));

    assertEquals(1, segments.size());
    assertFalse(segments.get(0).highlighted());
    assertEquals(text, segments.get(0).text());
  }

  @Test
  void handlesNullOrEmptyKeywordsSafely() {
    String text = "Just a plain response.";
    assertTrue(KeywordHighlighter.findKeywordSpans(text, null).isEmpty());
    assertTrue(KeywordHighlighter.findKeywordSpans(text, List.of()).isEmpty());
    assertEquals(1, KeywordHighlighter.segmentsFor(text, null).size());
  }

  @Test
  void aLongerKeywordWinsOverAShorterOneItContains() {
    // "key" is itself a keyword, but "key of artherk" is a more specific one that also matches -
    // the longer keyword should win so it isn't split into a highlighted "key" plus plain text.
    String text = "You will need the key of artherk to proceed.";
    List<KeywordHighlighter.Segment> segments =
        KeywordHighlighter.segmentsFor(text, List.of("key", "key of artherk"));

    assertTrue(
        segments.stream()
            .anyMatch(s -> s.highlighted() && s.text().equalsIgnoreCase("key of artherk")));
    assertFalse(
        segments.stream().anyMatch(s -> s.highlighted() && s.text().equalsIgnoreCase("key")));
  }
}
