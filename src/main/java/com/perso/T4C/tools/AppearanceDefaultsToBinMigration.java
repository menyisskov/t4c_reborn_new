package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.AppearanceDefaultsBinaryIO;
import com.perso.T4C.helper.AppearanceDefaultsCatalog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes {@code assets/mappings/appearance/appearance_defaults.bin} from the values that used to be
 * hardcoded in {@code PlayerAppearanceDefaults}, {@code NPCAnimations}, {@code MainGameScreen}
 * and the Content Studio front-end.
 *
 * <p>Run once from the repository root; afterwards the asset is edited from Content Studio.
 *
 * <p>Usage: {@code AppearanceDefaultsToBinMigration [--dry-run]}
 */
public final class AppearanceDefaultsToBinMigration {

    private AppearanceDefaultsToBinMigration() {
    }

    private static final String[][] MALE_PARTS = {
            {"BODY", "PupNakedBody"},
            {"HEAD", "PupNakedHead"},
            {"HAIR", "PupNormalHair"},
            {"LEGS", "PupNakedLegs"},
            {"FEET", "PupNakedFoot"},
            {"LEFT_ARM", "PupNakedArmL"},
            {"RIGHT_ARM", "PupNakedArmR"},
            {"LEFT_HAND", "PupNakedHandL"},
            {"RIGHT_HAND", "PupNakedHandR"},
    };

    private static final String[][] FEMALE_PARTS = {
            {"BODY", "WoNakedBody"},
            {"HEAD", "WoNakedHead"},
            {"HAIR", "WoHairPonyTail"},
            {"LEGS", "WoNakedLegs"},
            {"FEET", "WoNakedFeet"},
            {"LEFT_ARM", "WoNakedArmL"},
            {"RIGHT_ARM", "WoNakedArmR"},
            {"LEFT_HAND", "WoNakedHandL"},
            {"RIGHT_HAND", "WoNakedHandR"},
    };

    /** {@code {triggerSlot, appearance, hiddenParts CSV, hidesExplicit}}. */
    private static final Object[][] CONCEALMENT = {
            {"HEAD", "PupLeatherHelm", "HEAD,HAIR", false},
            {"HEAD", "PupPlateHelm", "HEAD,HAIR", false},
            {"HEAD", "PupChainMailCoif", "HEAD,HAIR", false},
            {"HEAD", "PupHornedHelmet", "HEAD,HAIR", false},
            {"HEAD", "V2_Haume01", "HAIR", false},
            {"HEAD", "V2_Haume02", "HAIR", false},
            {"HEAD", "V2_Haume04", "HAIR", false},
            {"HEAD", "HalloweenHead", "HAIR", false},
            {"HEAD", "PupElvenHat", "HAIR", false},
            {"HEAD", "PupGoldenCrown", "HAIR", false},
            {"HEAD", "WoChainMailCoif", "HAIR", false},
            {"HEAD", "WoPlateHelm", "HAIR", false},
            {"HEAD", "64kItemGrSkeletonHelm", "HAIR", false},
    };

    private static final String[] ARM_COVERINGS = {
            "PupArmoredRobe", "PupChainMailBody", "PupLeatherArmor", "PupMageRobe",
            "PupNecromanRobe", "PupPlateBody", "PupMithrilPlateBody",
            "PupStuddedBodyArmor", "PupWhiteRobe",
            "WoChainBody", "WoClothBody", "WoLeatherBody", "WoMageRobe", "WoNecromanRobe",
            "WoPlateBody", "WoWhiteRobe", "ManLichRobeBlanc", "ManLichRobeGold",
            "ManLichRobeGreen", "ManLichRobeKimono", "ManLichRobeNoir", "ManLichRobeOri",
            "ManLichRobeRouge", "PupRedRobe", "PupSpikeLeatherBody", "PupLeatherBody",
            "V2_ManArmorBody01"
    };

    private static final String[] ROBES = {
            "PupArmoredRobe", "PupMageRobe", "PupNecromanRobe", "PupWhiteRobe", "PupRedRobe",
            "WoMageRobe", "WoNecromanRobe", "WoWhiteRobe", "ManLichRobeBlanc",
            "ManLichRobeGold", "ManLichRobeGreen", "ManLichRobeKimono", "ManLichRobeNoir",
            "ManLichRobeOri", "ManLichRobeRouge"
    };

    public static void main(String[] args) throws Exception {
        boolean dryRun = args.length > 0 && "--dry-run".equals(args[0]);

        List<AppearanceDefaultsBinaryIO.NakedPart> parts = new ArrayList<>();
        for (String[] part : MALE_PARTS) {
            parts.add(new AppearanceDefaultsBinaryIO.NakedPart(
                    AppearanceDefaultsCatalog.MALE, part[0], part[1]));
        }
        for (String[] part : FEMALE_PARTS) {
            parts.add(new AppearanceDefaultsBinaryIO.NakedPart(
                    AppearanceDefaultsCatalog.FEMALE, part[0], part[1]));
        }

        List<AppearanceDefaultsBinaryIO.ConcealmentRule> rules = new ArrayList<>();
        for (Object[] rule : CONCEALMENT) {
            rules.add(rule((String) rule[0], (String) rule[1], (String) rule[2], (Boolean) rule[3]));
        }
        for (String appearance : ARM_COVERINGS) {
            rules.add(rule("BODY", appearance, "LEFT_ARM,RIGHT_ARM", false));
        }
        for (String appearance : ROBES) {
            rules.add(rule("BODY", appearance, "LEGS,FEET", false));
            rules.add(rule("BODY", appearance, "BOOT", true));
        }

        List<AppearanceDefaultsBinaryIO.EquippedOverride> overrides = List.of(
                new AppearanceDefaultsBinaryIO.EquippedOverride(
                        AppearanceDefaultsCatalog.FEMALE, "BODY", "PupBodyClothSet1",
                        "BODY", "WoClothBody"),
                new AppearanceDefaultsBinaryIO.EquippedOverride(
                        AppearanceDefaultsCatalog.FEMALE, "LEGS", "PupLegsClothSet1",
                        "ROBELEGS", "WoClothRobe"));

        System.out.printf("nakedParts=%d concealmentRules=%d%n", parts.size(), rules.size());
        if (dryRun) {
            System.out.println("dry run - rien ecrit");
            return;
        }

        AppearanceDefaultsBinaryIO.write(new File(Paths.APPEARANCE_DEFAULTS_BIN),
                new AppearanceDefaultsBinaryIO.Defaults(parts, rules, overrides));
        System.out.println("written: " + Paths.APPEARANCE_DEFAULTS_BIN);
    }

    private static AppearanceDefaultsBinaryIO.ConcealmentRule rule(
            String triggerSlot, String appearance, String hiddenParts, boolean hidesExplicit) {
        return new AppearanceDefaultsBinaryIO.ConcealmentRule(triggerSlot, appearance,
                List.of(hiddenParts.split(",")), hidesExplicit);
    }
}
