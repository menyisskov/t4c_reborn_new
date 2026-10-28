package com.perso.T4C.session;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import java.util.function.Supplier;

/**
 * What the screen already owns that the entity registries are built from. {@code currentMap} is a
 * supplier because the summon callback keeps reading the screen's live map after the build.
 */
public record WorldEntityInputs(
    ShaderProgram outlineShader,
    QuestService questService,
    XpCurve xpCurve,
    Supplier<MapDefinition> currentMap,
    Player player) {}
