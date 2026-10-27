package com.perso.T4C.online.model;

import java.util.*;

/** Owner-only account summaries, without account keys, leases or gameplay payloads. */
public record OnlineRoster(List<CharacterSummary> characters,UUID nextAfter) {
  public static final int PAGE_SIZE=20;
  public record CharacterSummary(UUID id,String name,String characterClass,String gender,Integer level,int world,String availability) {
    public CharacterSummary {
      Objects.requireNonNull(id);
      if(name==null || name.isBlank() || name.length()>32 || world<0
          || !Set.of("ready","in_use","uninitialized","unhosted_region","unavailable_respawn").contains(availability))
        throw new IllegalArgumentException("Invalid character summary");
      if(characterClass==null || gender==null || level==null) {
        if(characterClass!=null || gender!=null || level!=null || !availability.equals("uninitialized"))
          throw new IllegalArgumentException("Incomplete character summary");
      } else if(characterClass.isBlank() || characterClass.length()>256 || !Set.of("male","female").contains(gender) || level<1)
        throw new IllegalArgumentException("Invalid character summary");
    }
  }
  public OnlineRoster {
    characters=List.copyOf(characters);
    if(characters.size()>PAGE_SIZE || (nextAfter!=null && (characters.isEmpty() || !characters.getLast().id().equals(nextAfter))))
      throw new IllegalArgumentException("Invalid roster page");
    var ids=new HashSet<UUID>();String previous=null;
    for(var character:characters) {
      String current=character.id().toString();
      if(!ids.add(character.id()) || (previous!=null && current.compareTo(previous)<=0)) throw new IllegalArgumentException("Unordered roster page");
      previous=current;
    }
  }
}
