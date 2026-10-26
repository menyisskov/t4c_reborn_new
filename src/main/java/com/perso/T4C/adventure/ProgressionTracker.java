package com.perso.T4C.adventure;

import com.perso.T4C.combat.ArmorClassRules;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.*;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.*;
import com.perso.T4C.quest.definition.ConcordCampaign;
import com.perso.T4C.spawn.HuntingGrounds;
import com.perso.T4C.spell.*;
import com.perso.T4C.wiki.WikiCatalog;
import java.util.*;

/**
 * Achievable next steps drawn from current character state, never assumed unlocks or free travel.
 */
public final class ProgressionTracker {
  public record Goal(
      String id, String title, String detail, String wikiTarget, Integer x, Integer y) {}

  private static final String PIN = "progress.pin.";

  private ProgressionTracker() {}

  public static String style(Player p) {
    var weapon =
        ItemRegistry.findByKey(p.getEquippedItems().get(com.perso.T4C.player.BodyPart.WEAPON));
    if (weapon != null && weapon.isBow()) return "archer";
    var schools = List.of("fire", "water", "earth", "air", "light", "dark");
    String best = schools.stream().max(Comparator.comparingInt(p::getElementPower)).orElse("fire");
    if (p.getElementPower(best) <= 100)
      best = p.getWisdom() > p.getIntelligence() ? "earth" : "fire";
    if (p.getIntelligence() > p.getStrength()
        || p.getWisdom() > p.getStrength()
        || p.getElementPower(best) > 100) return best;
    return "warrior";
  }

  public static List<Goal> goals(Player p) {
    CollectionBook.synchronize(p);
    List<Goal> result = new ArrayList<>();
    int chapter = ConcordCampaign.chapter(p);
    if (chapter < ConcordCampaign.all().size()) {
      var q = ConcordCampaign.all().get(chapter);
      var entry = QuestJournal.entryFor(p, q);
      result.add(
          new Goal(
              "story_" + q.getId(),
              I18n.message("progress.story", entry.title()),
              entry.nextStep() + "\n" + I18n.message("progress.quest_level", q.getMinLevel()),
              WikiCatalog.id(WikiCatalog.Category.QUESTS, q.getId()),
              null,
              null));
    }
    var fields =
        HuntingGrounds.all().stream()
            .filter(
                g ->
                    g.trainingTier() > 0
                        && QuestService.hasUnlockedZone(p, g.unlock())
                        && (g.trainingTier() == 1 || p.getLevel() >= 180))
            .sorted(Comparator.comparingInt(HuntingGrounds.Ground::trainingTier).reversed())
            .toList();
    if (!fields.isEmpty()) {
      var g =
          fields.stream()
              .filter(
                  f ->
                      ArmorClassRules.effectiveArmorClass(p) >= (f.trainingTier() == 1 ? 150 : 250))
              .findFirst()
              .orElse(fields.get(fields.size() - 1));
      result.add(
          new Goal(
              "hunt_" + g.locationKey(),
              I18n.message("progress.hunt", g.monster()),
              I18n.message(
                  "progress.hunt_detail",
                  g.trainingTier() == 1 ? 150 : 250,
                  ArmorClassRules.effectiveArmorClass(p),
                  g.x(),
                  g.y()),
              WikiCatalog.id(WikiCatalog.Category.AREAS, g.locationKey()),
              g.x(),
              g.y()));
    } else
      result.add(
          new Goal(
              "unlock_witness",
              I18n.key("progress.unlock"),
              I18n.key("progress.unlock_detail"),
              WikiCatalog.id(WikiCatalog.Category.QUESTS, "concord_charter"),
              null,
              null));
    String style = style(p);
    int element =
        switch (style) {
          case "fire" -> 1;
          case "earth" -> 2;
          case "air" -> 3;
          case "water" -> 4;
          case "light" -> 5;
          case "dark" -> 6;
          default -> 0;
        };
    if (element > 0)
      SpellRegistry.load().stream()
          .filter(
              s ->
                  s.isAttack()
                      && s.getElement() == element
                      && s.getMinLevel() > 0
                      && s.getMinInt() <= p.getEffectiveIntelligence()
                      && s.getMinWis() <= p.getEffectiveWisdom()
                      && s.getPrice() > 0
                      && !s.getKey().startsWith("spell.npc_")
                      && !s.getKey().startsWith("spell.mob_")
                      && !s.getKey().startsWith("spell.item_")
                      && !SpellCastingService.hasLearnedSpell(p, s))
          .max(Comparator.comparingInt(SpellData::getMinLevel))
          .ifPresent(
              s ->
                  result.add(
                      new Goal(
                          "spell_" + s.getKey(),
                          I18n.message("progress.spell", I18n.resolve(s.getName())),
                          I18n.message(
                              "progress.spell_detail",
                              s.getMinLevel(),
                              s.getMinInt(),
                              s.getMinWis(),
                              s.getPrice()),
                          WikiCatalog.id(WikiCatalog.Category.SPELLS, s.getKey()),
                          null,
                          null)));
    CollectionBook.sets().stream()
        .filter(s -> !s.id().startsWith("slot_") && s.discovered(p) > 0 && !s.complete(p))
        .sorted(Comparator.comparingDouble(s -> -(double) s.discovered(p) / s.items().size()))
        .findFirst()
        .ifPresent(
            s -> {
              String missing =
                  s.items().stream()
                      .filter(k -> !CollectionBook.has(p, k))
                      .findFirst()
                      .orElseThrow();
              var item = ItemRegistry.findByKey(missing);
              result.add(
                  new Goal(
                      "set_" + s.id(),
                      I18n.message("progress.collection", s.name()),
                      I18n.message(
                          "progress.collection_detail",
                          s.discovered(p),
                          s.items().size(),
                          I18n.resolve(item.getName())),
                      WikiCatalog.id(WikiCatalog.Category.ITEMS, item.getKey()),
                      null,
                      null));
            });
    for (var e : WorldEvents.definitions())
      if (QuestService.hasUnlockedZone(p, e.unlock())) {
        var g = e.ground();
        result.add(
            new Goal(
                "event_" + e.id(),
                e.name(),
                I18n.message(
                    "progress.event_detail", e.level(), e.ac(), g.x(), g.y(), e.gold(), e.xp()),
                WikiCatalog.id(WikiCatalog.Category.AREAS, g.locationKey()),
                g.x(),
                g.y()));
      }
    for (var god : com.perso.T4C.divine.DivinePantheon.all()) {
      String gate = god.x() >= 5120 ? "threnody_reach" : "avalon_sanctuary";
      if (!QuestService.hasUnlockedZone(p, gate)
          || god.level() > p.getLevel() + 15
          || god.tier() > 0 && ArmorClassRules.effectiveArmorClass(p) < god.damage() / 2.0)
        continue;
      if (god.tier() > 0 && !god.school().equals(style)) continue;
      result.add(
          new Goal(
              "god_" + god.id(),
              I18n.message("progress.god", god.name()),
              I18n.message("progress.god_detail", god.level(), god.x(), god.y()),
              WikiCatalog.id(WikiCatalog.Category.MONSTERS, "Divine " + god.name()),
              god.x(),
              god.y()));
      break;
    }
    return List.copyOf(result);
  }

  public static void pin(Player p, Goal goal) {
    p.getQuestFlags().keySet().removeIf(k -> k.startsWith(PIN));
    p.setQuestFlag(PIN + goal.id(), 1);
  }

  public static Goal pinned(Player p, List<Goal> goals) {
    return goals.stream().filter(g -> p.getQuestFlag(PIN + g.id()) > 0).findFirst().orElse(null);
  }
}
