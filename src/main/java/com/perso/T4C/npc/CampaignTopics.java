package com.perso.T4C.npc;

import com.perso.T4C.npc.core.NpcSpec;
import java.util.List;

final class CampaignTopics {
  private CampaignTopics() {}

  static List<NpcSpec.DialogueTopic> all() {
    return List.of(topic("story"), topic("accept"), topic("report"), topic("route"),
        topic("witnesses"), topic("warders"), topic("moonwake"), topic("emberglass"),
        topic("hierarchy"), topic("threnody"));
  }

  private static NpcSpec.DialogueTopic topic(String name) {
    return new NpcSpec.DialogueTopic(
        List.of("${npc.hollow_dawn.keyword." + name + "}"),
        "${npc.hollow_dawn.topic." + name + "}",
        List.of());
  }
}
