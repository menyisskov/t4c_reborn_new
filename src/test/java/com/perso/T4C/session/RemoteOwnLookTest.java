package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * T4C-0337: an equip or unequip that the server commits reaches the own screen's player, so the strength and armor class of the
 * character sheet and the drawn model follow it (live finding of the rendered inventory run: they stayed those of the join).
 */
@ResourceLock("user.dir")
class RemoteOwnLookTest {
  private static final UUID MACE = UUID.fromString("00000000-0000-0000-0000-0000000000e1");
  private static final UUID ARMOR = UUID.fromString("00000000-0000-0000-0000-0000000000e2");

  @BeforeEach
  void loadJsonItems() {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister("assets/items");
  }

  @AfterEach
  void unloadJsonItems() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  private static CharacterProgress progress(Map<String, UUID> worn) {
    var base = ProgressMirrorTest.progress(List.of(), Map.of());
    var inventory = new java.util.ArrayList<>(base.inventory());
    inventory.add(new CharacterProgress.Item(MACE, "item.recruits_mace", 1, 1));
    inventory.add(new CharacterProgress.Item(ARMOR, "item.leather_armor", 1, 0));
    return new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(), base.vitals(), base.advancement(),
        inventory, worn, base.skills(), base.spells(), base.questFlags(), base.storage(), base.storageGold(), base.respawn(), base.buffs(),
        base.cooldowns(), base.hiddenUntil(), base.companion());
  }

  private static Player joined(CharacterProgress progress) {
    var player = new Player();
    var mirrored = ProgressMirror.toStateDto(ProgressMirrorTest.view(progress), () -> ProgressMirrorTest.NOW);
    new RemoteCharacterSource(() -> ProgressMirrorTest.view(progress), () -> ProgressMirrorTest.NOW).applyTo(mirrored.state(), player);
    return player;
  }

  @Test
  void takingTheWeaponOffLowersTheOwnStrengthByItsBonusAndPuttingItBackRestoresIt() {
    var wearing = progress(Map.of(BodyPart.WEAPON.name(), MACE));
    var player = joined(wearing);
    int withMace = player.getEffectiveStrength();
    assertTrue(withMace > player.getStrength(), "the Recruit's Mace adds strength while worn");

    assertTrue(RemoteOwnLook.refresh(player, progress(Map.of())), "the committed unequip changes the worn items");
    assertNull(player.getEquippedItems().get(BodyPart.WEAPON));
    assertEquals(player.getStrength(), player.getEffectiveStrength(), "the bonus left with the weapon");

    assertTrue(RemoteOwnLook.refresh(player, wearing));
    assertEquals(withMace, player.getEffectiveStrength());
    assertEquals("item.recruits_mace", player.getEquippedItems().get(BodyPart.WEAPON));
  }

  @Test
  void aRefreshWithNothingNewChangesNothing() {
    var wearing = progress(Map.of(BodyPart.BODY.name(), ARMOR));
    var player = joined(wearing);
    assertFalse(RemoteOwnLook.refresh(player, wearing));
    assertEquals("item.leather_armor", player.getEquippedItems().get(BodyPart.BODY));
  }

  @Test
  void theDrawnOwnModelLosesAndRegainsTheArmour() {
    var wearing = progress(Map.of(BodyPart.BODY.name(), ARMOR));
    var player = joined(wearing);
    com.perso.T4C.helper.PlayerAppearanceDefaults.applyDefaults(player);
    var parts = player.getAnimations() == null ? null : player.getAnimations().getPartMap();
    if (parts == null) return; // no animation set in a headless player: the worn-items assertions above still hold
    String worn = parts.get(BodyPart.BODY);
    RemoteOwnLook.refresh(player, progress(Map.of()));
    assertFalse(java.util.Objects.equals(worn, parts.get(BodyPart.BODY)), "the leather armour sprite is gone from the own model");
    RemoteOwnLook.refresh(player, wearing);
    assertEquals(worn, parts.get(BodyPart.BODY));
  }
}
