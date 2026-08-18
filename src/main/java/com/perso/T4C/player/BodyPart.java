package com.perso.T4C.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BodyPart {
  LEGS("legs"),
  FEET("feet"),
  BODY("body"),
  HEAD("head"),
  BELT("belt"),
  NECK("neck"),
  BRACER("bracer"),
  BACK("back"),
  RING1("ring1"),
  RING2("ring2"),
  LEFT_ARM("leftArm"),
  RIGHT_ARM("rightArm"),
  LEFT_HAND("leftHand"),
  RIGHT_HAND("rightHand"),
  SHIELD("shield"),
  WEAPON("weapon"),
  WEAPON2("weapon2"),
  BOOT("boot"),
  ROBELEGS("robeLegs"),
  HAIR("hair"),
  HAT("hat"),
  MASK("mask"),
  CAPE("cape");
  private final String key;
}
