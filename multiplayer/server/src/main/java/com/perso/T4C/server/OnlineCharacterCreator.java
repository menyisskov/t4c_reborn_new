package com.perso.T4C.server;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.CharacterCreationRules;
import com.perso.T4C.helper.CharacterStartingKit;
import com.perso.T4C.online.model.CharacterProgress;
import java.util.*;

/** Server-created starting state using the same class rolls and kit data as offline creation. */
final class OnlineCharacterCreator {
  record Prepared(CharacterProgress progress, long gold) {}

  static Prepared prepare(CharacterClass type, String gender, boolean evil, Random random) {
    var stats=CharacterCreationRules.roll(type,random);
    var inventory=new ArrayList<CharacterProgress.Item>();
    var equipment=new HashMap<String,UUID>();
    type.startingEquipment().forEach((slot,key) -> {
      var item=new CharacterProgress.Item(UUID.randomUUID(),key,1,0);
      inventory.add(item); equipment.put(slot.name(),item.id());
    });
    for (var key:type.startingStacks()) add(inventory,key,CharacterClass.STACKED_KIT_ITEM_COUNT,0);
    add(inventory,CharacterStartingKit.TORCH,CharacterStartingKit.TORCH_COUNT,0);
    for (var key:CharacterStartingKit.POTIONS) add(inventory,key,CharacterStartingKit.POTION_COUNT,1);
    var progress=new CharacterProgress(CharacterProgress.CURRENT_VERSION,type.id(),gender,
        new CharacterProgress.Attributes(stats.strength(),stats.endurance(),stats.dexterity(),stats.wisdom(),stats.intelligence()),
        new CharacterProgress.Vitals(stats.maxHp(),stats.maxHp(),stats.maxMana(),stats.maxMana()),
        new CharacterProgress.Advancement(1,0,0,0,evil ? CharacterStartingKit.EVIL_KARMA : CharacterStartingKit.GOOD_KARMA),
        inventory,equipment,CharacterStartingKit.SKILLS,Set.copyOf(type.startingSpellKeys()),Map.of(),List.of(),0,
        new CharacterProgress.Respawn(GameConstants.PLAYER_RESPAWN_TILE_Z,GameConstants.PLAYER_RESPAWN_TILE_X,GameConstants.PLAYER_RESPAWN_TILE_Y),
        List.of(),Map.of(),0,null);
    return new Prepared(progress,random.nextInt(CharacterStartingKit.MIN_GOLD,CharacterStartingKit.MAX_GOLD+1));
  }

  private static void add(List<CharacterProgress.Item> items,String key,int count,int charges) {
    for (int i=0;i<count;i++) items.add(new CharacterProgress.Item(UUID.randomUUID(),key,1,charges));
  }
}
