package com.perso.T4C.spawn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(Spawns.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Spawn {
  String type();

  int x();

  int y();

  int z();

  boolean stationary();

  boolean aggressive();

  SpawnKind kind() default SpawnKind.AUTO;
}
