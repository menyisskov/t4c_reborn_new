package com.perso.T4C.tools;

import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.spell.SpellData;

import java.io.File;
import java.util.List;

/** Debug helper: lists every spell's iconId from spells.bin and whether the sprite exists. */
public final class SpellIconDump {
    private SpellIconDump() {
    }

    public static void main(String[] args) throws Exception {
        com.badlogic.gdx.Gdx.files = new com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files();
        SpriteLoader loader = SpriteLoader.getInstance();
        loader.loadSpriteBin("assets/sprites/sprites.bin");
        List<SpellData> spells = SpellBinaryIO.read(new File("assets/spells/spells.bin"));
        for (SpellData s : spells) {
            String icon = s.getIconId();
            boolean ok = icon != null && !icon.isEmpty() && loader.getSpriteMeta(icon) != null;
            if (!ok) System.out.println("MISSING  " + s.getName() + " -> " + icon);
        }
        for (SpellData s : spells) {
            if ("Light".equals(s.getName()) || "Heal light".equals(s.getName())) {
                System.out.println(s.getName() + ": price=" + s.getPrice()
                        + " minLevel=" + s.getMinLevel() + " minInt=" + s.getMinInt()
                        + " minWis=" + s.getMinWis());
            }
        }
    }
}
