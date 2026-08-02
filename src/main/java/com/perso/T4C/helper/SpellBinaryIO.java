package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.i18n.I18n;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpellBinaryIO {
    private static final byte[] MAGIC = "T4CSPL".getBytes(StandardCharsets.US_ASCII);
    private static final short VERSION = 4;
    private static final int MAX_STRING_BYTES = 16384;

    private SpellBinaryIO() {
    }

    public static List<SpellData> read(File file) throws IOException, GameException {
        return BinaryCatalogueIO.read(file, MAGIC, "spell binary",
                version -> {
                    if (version < 1 || version > VERSION) {
                        throw new GameException("Unsupported spell binary version: " + version);
                    }
                },
                SpellBinaryIO::readSpell);
    }

    public static void write(File file, List<SpellData> spells) throws IOException {
        BinaryCatalogueIO.write(file, MAGIC, VERSION, spells, SpellBinaryIO::writeSpell);
    }

    private static SpellData readSpell(DataInputStream in, short version) throws IOException, GameException {
        String name = readString(in);
        String description = readString(in);
        String manaCost = readString(in);
        int radius = readIntLE(in);
        int minInt = readIntLE(in);
        int minWis = readIntLE(in);
        int minLevel = readIntLE(in);
        boolean attack = in.readBoolean();
        boolean lineOfSight = in.readBoolean();
        String iconId = readString(in);
        String projectileSpell = readString(in);
        String impactSpell = readString(in);
        int minDamage = readIntLE(in);
        int maxDamage = readIntLE(in);
        String sound = readString(in);
        String soundImpact = readString(in);
        int cooldownSeconds = readIntLE(in);
        int price = version >= 2 ? readIntLE(in) : 0;
        String duration = readString(in);
        SpellData.SpellBuff buff = null;
        if (in.readBoolean()) {
            Integer durationSeconds = readNullableInt(in);
            Boolean unlimited = readNullableBoolean(in);
            int effectCount = readIntLE(in);
            if (effectCount < 0) {
                throw new GameException("Invalid spell effect count: " + effectCount);
            }
            List<SpellData.SpellEffect> effects = new ArrayList<>(effectCount);
            for (int i = 0; i < effectCount; i++) {
                effects.add(new SpellData.SpellEffect(readString(in), readString(in), readString(in),
                        readString(in)));
            }
            buff = new SpellData.SpellBuff(durationSeconds, unlimited, effects);
        }
        // v3 fields
        int spellId = 0, element = 0, targetType = 0, attackType = 0;
        String successRate = null, mentalExhaustion = null, physicalExhaustion = null, attackExhaustion = null;
        int visualEffect = 0, visualEffectTarget = 0;
        boolean pvp = false;
        List<SpellData.T4cEffect> t4cEffects = Collections.emptyList();
        String frequency = null;

        if (version >= 3) {
            spellId = readIntLE(in);
            element = readIntLE(in);
            targetType = readIntLE(in);
            attackType = readIntLE(in);
            successRate = emptyToNull(readString(in));
            mentalExhaustion = emptyToNull(readString(in));
            physicalExhaustion = emptyToNull(readString(in));
            attackExhaustion = emptyToNull(readString(in));
            visualEffect = readIntLE(in);
            visualEffectTarget = readIntLE(in);
            pvp = in.readBoolean();
            int effectCount = readIntLE(in);
            if (effectCount > 0) {
                t4cEffects = new ArrayList<>(effectCount);
                for (int i = 0; i < effectCount; i++) {
                    int effectType = readIntLE(in);
                    int paramCount = readIntLE(in);
                    List<SpellData.T4cEffect.EffectParam> params = new ArrayList<>(paramCount);
                    for (int j = 0; j < paramCount; j++) {
                        int paramId = readIntLE(in);
                        String expression = emptyToNull(readString(in));
                        params.add(new SpellData.T4cEffect.EffectParam(paramId, expression));
                    }
                    t4cEffects.add(new SpellData.T4cEffect(effectType, params));
                }
            }
        }
        if (version >= 4) {
            frequency = emptyToNull(readString(in));
        }

        return new SpellData(name, description, manaCost, radius, minInt, minWis, minLevel, attack, lineOfSight,
                iconId, emptyToNull(projectileSpell), emptyToNull(impactSpell), minDamage, maxDamage,
                emptyToNull(sound), emptyToNull(soundImpact), cooldownSeconds, emptyToNull(duration), frequency, price, buff,
                spellId, element, targetType, attackType, successRate,
                mentalExhaustion, physicalExhaustion, attackExhaustion,
                visualEffect, visualEffectTarget, pvp, t4cEffects);
    }

    private static void writeSpell(DataOutputStream out, SpellData spell) throws IOException {
        writeString(out, spell == null ? "" : I18n.placeholder(spell.getKey()));
        writeString(out, spell == null ? "" : I18n.placeholderForKey("spell.description." + spell.getKey().substring("spell.".length()), spell.getDescription()));
        writeString(out, spell == null ? "" : spell.getManaCost());
        writeIntLE(out, spell == null ? 0 : spell.getRadius());
        writeIntLE(out, spell == null ? 0 : spell.getMinInt());
        writeIntLE(out, spell == null ? 0 : spell.getMinWis());
        writeIntLE(out, spell == null ? 0 : spell.getMinLevel());
        out.writeBoolean(spell != null && spell.isAttack());
        out.writeBoolean(spell != null && spell.isLineOfSight());
        writeString(out, spell == null ? "" : spell.getIconId());
        writeString(out, spell == null ? "" : spell.getProjectileSpell());
        writeString(out, spell == null ? "" : spell.getImpactSpell());
        writeIntLE(out, spell == null ? 0 : spell.getMinDamage());
        writeIntLE(out, spell == null ? 0 : spell.getMaxDamage());
        writeString(out, spell == null ? "" : spell.getSound());
        writeString(out, spell == null ? "" : spell.getSoundImpact());
        writeIntLE(out, spell == null ? 0 : spell.getCooldownSeconds());
        writeIntLE(out, spell == null ? 0 : spell.getPrice());
        writeString(out, spell == null ? "" : spell.getDuration());
        SpellData.SpellBuff buff = spell == null ? null : spell.getBuff();
        out.writeBoolean(buff != null);
        if (buff != null) {
            writeNullableInt(out, buff.getDurationSeconds());
            writeNullableBoolean(out, buff.getUnlimited());
            List<SpellData.SpellEffect> effects = buff.getEffects();
            writeIntLE(out, effects == null ? 0 : effects.size());
            if (effects != null) {
                for (int effectIndex = 0; effectIndex < effects.size(); effectIndex++) {
                    SpellData.SpellEffect effect = effects.get(effectIndex);
                    writeString(out, effect == null ? "" : effect.getType());
                    writeString(out, effect == null ? "" : effect.getAttribute());
                    writeString(out, effect == null ? "" : effect.getAmount());
                    writeString(out, effect == null ? "" : I18n.placeholderForKey(
                            "spell.effect." + normalized(spell.getName()) + "." + effectIndex, effect.getDescription()));
                }
            }
        }

        // v3 fields
        writeIntLE(out, spell == null ? 0 : spell.getSpellId());
        writeIntLE(out, spell == null ? 0 : spell.getElement());
        writeIntLE(out, spell == null ? 0 : spell.getTargetType());
        writeIntLE(out, spell == null ? 0 : spell.getAttackType());
        writeString(out, spell == null ? "" : nullToEmpty(spell.getSuccessRate()));
        writeString(out, spell == null ? "" : nullToEmpty(spell.getMentalExhaustion()));
        writeString(out, spell == null ? "" : nullToEmpty(spell.getPhysicalExhaustion()));
        writeString(out, spell == null ? "" : nullToEmpty(spell.getAttackExhaustion()));
        writeIntLE(out, spell == null ? 0 : spell.getVisualEffect());
        writeIntLE(out, spell == null ? 0 : spell.getVisualEffectTarget());
        out.writeBoolean(spell != null && spell.isPvp());
        List<SpellData.T4cEffect> t4cEffects = spell == null ? null : spell.getT4cEffects();
        writeIntLE(out, t4cEffects == null ? 0 : t4cEffects.size());
        if (t4cEffects != null) {
            for (SpellData.T4cEffect eff : t4cEffects) {
                writeIntLE(out, eff == null ? 0 : eff.getEffectType());
                List<SpellData.T4cEffect.EffectParam> params = eff == null ? null : eff.getParameters();
                writeIntLE(out, params == null ? 0 : params.size());
                if (params != null) {
                    for (SpellData.T4cEffect.EffectParam p : params) {
                        writeIntLE(out, p == null ? 0 : p.getParamId());
                        writeString(out, p == null ? "" : nullToEmpty(p.getExpression()));
                    }
                }
            }
        }
        writeString(out, spell == null ? "" : nullToEmpty(spell.getFrequency()));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT)
                .replaceFirst("^\\s*\\[[^]]+]\\s*", "").replaceFirst("^a\\s+", "")
                .replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    private static String readString(DataInputStream in) throws IOException, GameException {
        return BinaryIOUtils.readString(in, MAX_STRING_BYTES);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        BinaryIOUtils.writeString(out, value);
    }

    private static Integer readNullableInt(DataInputStream in) throws IOException {
        return in.readBoolean() ? BinaryIOUtils.readIntLE(in) : null;
    }

    private static void writeNullableInt(DataOutputStream out, Integer value) throws IOException {
        out.writeBoolean(value != null);
        if (value != null) {
            BinaryIOUtils.writeIntLE(out, value);
        }
    }

    private static Boolean readNullableBoolean(DataInputStream in) throws IOException {
        return in.readBoolean() ? in.readBoolean() : null;
    }

    private static void writeNullableBoolean(DataOutputStream out, Boolean value) throws IOException {
        out.writeBoolean(value != null);
        if (value != null) {
            out.writeBoolean(value);
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static int readIntLE(DataInputStream in) throws IOException {
        return BinaryIOUtils.readIntLE(in);
    }

    private static void writeIntLE(DataOutputStream out, int value) throws IOException {
        BinaryIOUtils.writeIntLE(out, value);
    }
}
