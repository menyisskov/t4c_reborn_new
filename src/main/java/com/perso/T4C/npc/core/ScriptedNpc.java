package com.perso.T4C.npc.core;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_RADIUS;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.gui.screen.RepairScreen;
import com.perso.T4C.gui.screen.ShopScreen;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.behavior.TrainingAndFleeBehavior;
import com.perso.T4C.npc.behavior.TrainingBehavior;
import com.perso.T4C.npc.catalog.ShopCatalog;
import com.perso.T4C.npc.catalog.TrainingCatalog;
import com.perso.T4C.npc.companion.CompanionManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.spell.NpcCastVfxHook;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.SystemMessage;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ScriptedNpc extends BaseNPC {

  private static final Set<String> KNOWN_SKILLS =
      Set.of(
          "attack",
          "archery",
          "dodge",
          "peek",
          "stun_blow",
          "powerful_blow",
          "rapid_healing",
          "first_aid",
          "parry",
          "critical_strike",
          "hide",
          "sneak",
          "search",
          "picklock",
          "armor_penetration",
          "two_weapons",
          "rob",
          "meditate",
          "strength",
          "dexterity",
          "endurance",
          "intelligence",
          "wisdom");

  private final NpcSpec spec;

  private final QuestService questService;

  private final Supplier<CompanionManager> companionManagerSupplier;

  private String pendingYesNoState;

  private int armorClass;

  private String damageFormula = "1d3";

  private boolean nativeSelfDestructRequested;

  private long nativeSelfDestructAtMillis;

  private boolean conversationResponseShown;

  protected NpcBehavior javaBehavior() {

    return null;
  }

  public final NpcBehavior publicBehavior() {

    return javaBehavior();
  }

  public final NpcSpec specification() {

    return spec;
  }

  /** Exposed so a subclass's custom {@link com.perso.T4C.npc.behavior.NpcBehavior} can call
   * {@link QuestService#giveOrReport}/{@link QuestService#statusFor} directly - needed when a
   * single keyword must dispatch to different quest ids depending on prerequisite quest state
   * (a multi-stage chain), which the declarative {@code GIVE_QUEST} action can't express on its
   * own. See {@code npc/HarbormasterRangor.java} for the pattern. */
  public final QuestService questService() {

    return questService;
  }

  private NpcBehavior resolvedBehavior() {

    NpcBehavior b = javaBehavior();

    if (b instanceof TrainingBehavior
        && (!spec.topics().isEmpty()
            || (spec.welcomeText() != null && !spec.welcomeText().isBlank()))) {

      b = combine(StaticDialogueBehavior.INSTANCE, b);
    }

    NpcBehavior t = TrainingCatalog.get(spec.id());

    NpcBehavior s = ShopCatalog.get(spec.id());

    if (b == null && (t != null || s != null)) {

      b = StaticDialogueBehavior.INSTANCE;
    }

    if (b != null
        && !(b instanceof TrainingBehavior)
        && b != StaticDialogueBehavior.INSTANCE
        && !spec.topics().isEmpty()) {

      b = withStaticDialogueFallback(b);
    }

    if (t != null && !(b instanceof TrainingBehavior) && !(b instanceof TrainingAndFleeBehavior)) {

      b = combine(b, t);
    }

    if (s != null) b = combine(b, s);

    return b;
  }

  private static NpcBehavior withStaticDialogueFallback(NpcBehavior custom) {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        custom.onConversationStart(c);
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String keyword) {

        return custom.onKeyword(c, keyword)
            || StaticDialogueBehavior.INSTANCE.onKeyword(c, keyword);
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String state, boolean answer) {

        return custom.onYesNo(c, state, answer);
      }

      @Override
      public void onAttacked(NpcBehaviorContext c) {

        custom.onAttacked(c);
      }

      @Override
      public void onDeath(NpcBehaviorContext c) {

        custom.onDeath(c);
      }

      @Override
      public void onInitialise(NpcBehaviorContext c) {

        custom.onInitialise(c);
      }

      @Override
      public void onPopup(NpcBehaviorContext c) {

        custom.onPopup(c);
      }

      @Override
      public void onDestroy(NpcBehaviorContext c) {

        custom.onDestroy(c);
      }

      @Override
      public void onHit(NpcBehaviorContext c) {

        custom.onHit(c);
      }

      @Override
      public void onAttack(NpcBehaviorContext c) {

        custom.onAttack(c);
      }

      @Override
      public void onAttackHit(NpcBehaviorContext c) {

        custom.onAttackHit(c);
      }
    };
  }

  private static NpcBehavior combine(NpcBehavior base, NpcBehavior... extras) {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        base.onConversationStart(c);

        for (NpcBehavior e : extras) if (e != null) e.onConversationStart(c);
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String k) {

        boolean handled = base.onKeyword(c, k);

        for (NpcBehavior e : extras) if (e != null) handled = e.onKeyword(c, k) || handled;

        return handled;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String s, boolean a) {

        boolean handled = base.onYesNo(c, s, a);

        for (NpcBehavior e : extras) if (e != null) handled = e.onYesNo(c, s, a) || handled;

        return handled;
      }

      @Override
      public void onAttacked(NpcBehaviorContext c) {

        base.onAttacked(c);

        for (NpcBehavior e : extras) if (e != null) e.onAttacked(c);
      }

      @Override
      public void onDeath(NpcBehaviorContext c) {

        base.onDeath(c);

        for (NpcBehavior e : extras) if (e != null) e.onDeath(c);
      }

      @Override
      public void onInitialise(NpcBehaviorContext c) {

        base.onInitialise(c);

        for (NpcBehavior e : extras) if (e != null) e.onInitialise(c);
      }

      @Override
      public void onPopup(NpcBehaviorContext c) {

        base.onPopup(c);

        for (NpcBehavior e : extras) if (e != null) e.onPopup(c);
      }

      @Override
      public void onDestroy(NpcBehaviorContext c) {

        base.onDestroy(c);

        for (NpcBehavior e : extras) if (e != null) e.onDestroy(c);
      }

      @Override
      public void onHit(NpcBehaviorContext c) {

        base.onHit(c);

        for (NpcBehavior e : extras) if (e != null) e.onHit(c);
      }

      @Override
      public void onAttack(NpcBehaviorContext c) {

        base.onAttack(c);

        for (NpcBehavior e : extras) if (e != null) e.onAttack(c);
      }

      @Override
      public void onAttackHit(NpcBehaviorContext c) {

        base.onAttackHit(c);

        for (NpcBehavior e : extras) if (e != null) e.onAttackHit(c);
      }
    };
  }

  protected ScriptedNpc(NpcSpec spec, NpcContext context) throws GameException {

    super(spec.id(), spec.spriteBase(), spec.partsArray());

    this.spec = spec;

    if ((spec.spriteBase() != null
            && (spec.spriteBase().startsWith("@static:") || spec.spriteBase().equals("@invisible")))
        || spec.parts().isEmpty()) {

      setStationary(true);
    }

    this.questService = context == null ? null : context.questService();

    this.companionManagerSupplier = context == null ? null : context.companionManagerSupplier();

    NpcSpec.CombatProfile combat = spec.combatProfile();

    this.maxHp = combat.maxHp();

    this.currentHp = maxHp;

    this.level = combat.level();

    this.strength = combat.strength();

    this.endurance = combat.endurance();

    this.dexterity = combat.dexterity();

    this.armorClass = combat.armorClass();

    setSkillLevel("attack", combat.attackSkill());

    setSkillLevel("dodge", combat.dodge());

    this.damageFormula = combat.damageFormula();

    if (spec.displayName() != null && !spec.displayName().isEmpty()) {

      String translatedName = I18n.resolve(spec.id());

      setDisplayName(
          translatedName.equals(spec.id()) ? I18n.resolve(spec.displayName()) : translatedName);
    }
  }

  @Override
  protected boolean canTalkThroughWalls() {
    return hasObjectAppearance()
        || Boolean.parseBoolean(sourceEvents().get("@interaction.talkThroughWalls"));
  }

  @Override
  protected void onInteractStart(Player player) {

    if (questService != null) {

      String completion = questService.turnInReadyQuests(spec.id(), player);

      if (completion != null && !completion.isBlank()) {

        showDialog(completion, 0L);

        return;
      }
    }

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      conversationResponseShown = false;

      behavior.onConversationStart(new NpcBehaviorContext(this, player));

      if (!conversationResponseShown
          && spec.welcomeText() != null
          && !spec.welcomeText().isBlank()) {

        showDialog(resolvePlayerName(spec.welcomeText(), player), 0L);
      }

      return;
    }

    if (spec.welcomeText() != null && !spec.welcomeText().isBlank()) {

      showDialog(resolvePlayerName(spec.welcomeText(), player), 0L);

      return;
    }
  }

  @Override
  protected List<String> getDialogKeywords() {

    List<String> keywords = new ArrayList<>(super.getDialogKeywords());

    for (NpcSpec.DialogueTopic topic : spec.topics()) {

      for (String keyword : topic.keywords()) addKeyword(keywords, keyword);
    }

    return keywords;
  }

  private static void addKeyword(List<String> keywords, String keyword) {

    String resolved = I18n.resolve(keyword);

    if (resolved != null && !resolved.isBlank() && !keywords.contains(resolved)) {

      keywords.add(resolved);
    }
  }

  @Override
  public boolean talk(String text, Player player) {

    NpcBehavior behavior = resolvedBehavior();

    boolean javaOk = isInteracting && behavior != null && behavior.handles(player);

    NpcBehaviorContext context = javaOk ? new NpcBehaviorContext(this, player) : null;

    if (javaOk) {

      if (pendingYesNoState != null) {

        String state = pendingYesNoState;

        boolean yes = normalizeCommand(text).equals("yes") || normalizeCommand(text).equals("oui");

        boolean no = normalizeCommand(text).equals("no") || normalizeCommand(text).equals("non");

        if (yes || no) {

          pendingYesNoState = null;

          if (behavior.onYesNo(context, state, yes)) return true;
        }
      }

      if (behavior.onKeyword(context, text)) return true;
    }

    if (isInteracting && respondToTopic(text, player)) return true;

    return super.talk(text, player);
  }

  public int getArmorClass() {

    return armorClass;
  }

  @Override
  public boolean isPassiveOnAttack() {

    return Boolean.parseBoolean(sourceEvents().get("@combat.passiveOnAttack"));
  }

  @Override
  public List<String> getFleeShouts() {

    return spec.fleeShouts().stream().map(I18n::resolve).toList();
  }

  @Override
  protected int rollHostileDamage() {

    return Math.max(0, com.perso.T4C.helper.DiceFormula.of(damageFormula).roll());
  }

  private Map<String, String> sourceEvents() {

    return spec.sourceEvents();
  }


  @Override
  public final void onInitialise(Player player) {

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      behavior.onInitialise(new NpcBehaviorContext(this, player));

      return;
    }
  }

  public void applyNativeDamage(int amount) {

    if (amount <= 0) return;

    currentHp = Math.max(0, currentHp - amount);
  }

  public void requestNativeSelfDestruct() {

    nativeSelfDestructRequested = true;
  }

  public void scheduleSelfDestructFrom(SpellData spell) {
    if (spell == null) {
      return;
    }
    int seconds =
        switch (spell.getSpellId()) {
          case 10797 -> 20;
          case 10765 -> 5;
          case 10752, 10753, 10799 -> 120;
          default -> 0;
        };
    if (seconds > 0) {
      nativeSelfDestructAtMillis = System.currentTimeMillis() + seconds * 1000L;
    }
  }

  public boolean isNativeSelfDestructRequested() {

    return nativeSelfDestructRequested
        || (nativeSelfDestructAtMillis > 0
            && System.currentTimeMillis() >= nativeSelfDestructAtMillis);
  }

  @Override
  public final void onPopup(Player player) {

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      behavior.onPopup(new NpcBehaviorContext(this, player));

      return;
    }
  }

  @Override
  public final void onAttack(Player player) {

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      behavior.onAttack(new NpcBehaviorContext(this, player));

      return;
    }
  }

  @Override
  public final void onAttacked(Player player) {

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      behavior.onAttacked(new NpcBehaviorContext(this, player));

      return;
    }
  }

  @Override
  public final void onDeath(Player player) {

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      behavior.onDeath(new NpcBehaviorContext(this, player));

      return;
    }
  }

  @Override
  public final void onDestroy(Player player) {

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      behavior.onDestroy(new NpcBehaviorContext(this, player));

      return;
    }
  }

  @Override
  public final void onHit(Player player) {

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      behavior.onHit(new NpcBehaviorContext(this, player));

      return;
    }
  }

  @Override
  public final void onAttackHit(Player player) {

    NpcBehavior behavior = resolvedBehavior();

    if (behavior != null && behavior.handles(player)) {

      behavior.onAttackHit(new NpcBehaviorContext(this, player));

      return;
    }
  }

  public final NpcSpec getSpec() {

    return spec;
  }

  public final boolean usesJavaBehavior() {

    return javaBehavior() != null;
  }

  public void say(String text, Player player) {

    String resolved = resolvePlayerName(text, player);

    if (resolved != null && !resolved.isBlank()) {

      conversationResponseShown = true;

      showDialog(resolved, 0L);
    }
  }

  public void systemMessage(String text) {

    if (text != null && !text.isBlank()) SystemMessage.showShared(I18n.resolve(text));
  }

  public void askYesNo(String state) {

    pendingYesNoState = state;
  }

  public String pendingYesNoState() {

    return pendingYesNoState;
  }

  private boolean respondToTopic(String text, Player player) {

    if (text == null || player == null) return false;

    for (NpcSpec.DialogueTopic topic : spec.topics()) {

      if (matches(topic, text)) {

        applyTopic(topic, player);

        return true;
      }
    }

    return false;
  }

  public static boolean matches(NpcSpec.DialogueTopic topic, String text) {

    for (String keyword : topic.keywords()) {

      String command = normalizeCommand(I18n.resolve(keyword));

      if (!command.isEmpty() && (" " + normalizeCommand(text) + " ").contains(" " + command + " "))
        return true;
    }

    return false;
  }

  private void applyTopic(NpcSpec.DialogueTopic topic, Player player) {

    if (topic.response() != null && !topic.response().isBlank()) {

      showDialog(resolvePlayerName(topic.response(), player), 0L);
    }

    for (NpcSpec.Action action : topic.actions()) executeAction(action, player);
  }

  public static String resolvePlayerName(String text, Player player) {

    String resolved = I18n.resolve(text);

    String playerName =
        player == null || player.getName() == null || player.getName().isBlank()
            ? I18n.resolve("npc.fallback.adventurer")
            : player.getName();

    return resolved.replace("%s", playerName);
  }

  private void executeAction(NpcSpec.Action action, Player player) {

    if (action == null || action.type() == null) return;

    switch (action.type()) {
      case OPEN_SPELL_LEARNING -> {
        List<String> valid =
            action.targets().stream()
                .filter(
                    id -> {
                      boolean found = SpellRegistry.findByName(id) != null;

                      if (!found) log.warn("NPC '{}' references unknown spell '{}'", spec.id(), id);

                      return found;
                    })
                .toList();

        GuiManager.open(new LearnScreen(player, valid));
      }

      case OPEN_SKILL_LEARNING -> {
        List<String> valid =
            action.targets().stream()
                .filter(
                    id -> {
                      boolean found = KNOWN_SKILLS.contains(id);

                      if (!found) log.warn("NPC '{}' references unknown skill '{}'", spec.id(), id);

                      return found;
                    })
                .toList();

        GuiManager.open(LearnScreen.forTraining(player, valid));
      }

      case OPEN_SHOP -> {
        List<String> valid =
            action.targets().stream()
                .filter(
                    id -> {
                      boolean found = ItemRegistry.findByKey(id) != null;

                      if (!found)
                        log.warn("NPC '{}' references unknown shop item '{}'", spec.id(), id);

                      return found;
                    })
                .toList();

        GuiManager.open(new ShopScreen(player, valid));
      }

      case OPEN_REPAIR -> GuiManager.open(new RepairScreen(player));

      case GIVE_ITEM -> {
        if (action.targets().isEmpty()) return;

        String itemKey = action.targets().get(0);

        if (ItemRegistry.findByKey(itemKey) == null) {

          log.warn("NPC '{}' references unknown gift item '{}'", spec.id(), itemKey);

        } else {

          InventoryService.add(player, itemKey);
        }
      }

      case GIVE_QUEST -> {
        if (action.targets().isEmpty() || questService == null) {

          if (questService == null) {

            log.warn("NPC '{}' cannot execute GIVE_QUEST without a quest service", spec.id());
          }

          return;
        }

        String response = questService.giveOrReport(action.targets().get(0), spec.id(), player);

        if (response != null && !response.isBlank()) {

          showDialog(response, 0L);
        }
      }

      case END_CONVERSATION -> endInteraction();

      case HEAL -> healFully(player);

      case SUMMON_COMPANION -> summonCompanion(action, player);
    }
  }

  public void openShop(Player player, List<String> itemKeys) {

    if (player == null || itemKeys == null) return;

    List<String> valid =
        itemKeys.stream().filter(id -> ItemRegistry.findByKey(id) != null).toList();

    GuiManager.open(new ShopScreen(player, valid));
  }

  public void openSellShop(Player player) {

    if (player != null) GuiManager.open(ShopScreen.forSelling(player, List.of()));
  }

  public void openSpellLearning(Player player, List<String> spellKeys) {

    if (player == null) return;

    List<String> valid =
        spellKeys.stream()
            .filter(
                id ->
                    SpellRegistry.findByName(id) != null
                        || SpellRegistry.findByName(id.replace("spell.", "")) != null)
            .toList();

    GuiManager.open(new LearnScreen(player, valid));
  }

  public void openSkillLearning(Player player, List<LearnScreen.TrainingOffer> offers) {

    if (player == null || offers == null) return;

    GuiManager.open(LearnScreen.forTrainingOffers(player, offers));
  }

  private void summonCompanion(NpcSpec.Action action, Player player) {

    CompanionManager companionManager =
        companionManagerSupplier == null ? null : companionManagerSupplier.get();

    if (companionManager == null) {

      log.warn("NPC '{}' cannot execute SUMMON_COMPANION without a companion manager", spec.id());

      return;
    }

    if (action.targets().isEmpty()) {

      log.warn("NPC '{}' declares SUMMON_COMPANION without a companion type", spec.id());

      return;
    }

    companionManager.spawnCompanionFor(player, action.targets().get(0), position);
  }

  private void healFully(Player player) {

    if (player.getCurrentHp() >= player.getMaxHp() && player.getMana() >= player.getMaxMana()) {

      showDialog(I18n.message("message.no_healing_needed"), 0L);

      return;
    }

    int healthBefore = player.getCurrentHp();

    player.applyHeal(player.getMaxHp(), player.getMaxHp());

    player.setMana(player.getMaxMana());

    if (player.getCurrentHp() > healthBefore) {

      SpellData healingSpell = SpellRegistry.findByName("spell.healing");

      if (healingSpell != null) {

        NpcCastVfxHook.playOnPlayer(healingSpell, player, position);
      }
    }

    showDialog(I18n.message("message.healed_dialog"), 0L);

    SystemMessage.showShared(I18n.message("message.wounds_healed"));
  }

  private static String normalizeCommand(String value) {

    if (value == null) return "";

    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^\\p{L}\\p{N}]+", " ")
        .trim()
        .replaceAll("\\s+", " ");
  }

  @Override
  protected boolean onDialogKeywordClick(String keyword, Player player) {

    return respondToTopic(keyword, player);
  }

  @Override
  protected float getPatrolRadius() {

    return spec.patrolRadiusTiles() > 0 ? spec.patrolRadiusTiles() * GRID_W : NPC_PATROL_RADIUS;
  }
}
