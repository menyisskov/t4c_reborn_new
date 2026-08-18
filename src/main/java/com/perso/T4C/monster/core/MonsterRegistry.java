package com.perso.T4C.monster.core;

import com.perso.T4C.monster.*;
import com.perso.T4C.monster.MonsterDef;

import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MonsterRegistry {
  @FunctionalInterface
  public interface MonsterFactory {
    BaseMonster create(MonsterDef definition, float worldX, float worldY) throws GameException;
  }

  private static final Map<String, MonsterFactory> SPECIALIZED_FACTORIES = new LinkedHashMap<>();
  private static List<MonsterDef> cache;
  private static Map<String, MonsterDef> byName;
  private static Map<String, MonsterDef> byNormalizedName;
  private static Map<String, MonsterDef> byAlias;

  private MonsterRegistry() {}

  static {
    registerSpecialized("MOBGAUZECORPSE1", r141MOBGAUZECORPSE1::new);
    registerSpecialized("MOBGIANTBLACKWIDOW1", r142MOBGIANTBLACKWIDOW1::new);
    registerSpecialized("MOBNEOFLARE1", r143MOBNEOFLARE1::new);
    registerSpecialized("MOBPURIFIER1", r144MOBPURIFIER1::new);
    registerSpecialized("MOBQUENTINTARANTULA1", r145MOBQUENTINTARANTULA1::new);
    registerSpecialized("MOBSHRIEKERRODENT1", r146MOBSHRIEKERRODENT1::new);
    registerSpecialized("MOBYOGGOTHWORM1", r147MOBYOGGOTHWORM1::new);
    registerSpecialized("MOONTUGGUARD11", r148MOONTUGGUARD11::new);
    registerSpecialized("MOONTUGGUARD21", r149MOONTUGGUARD21::new);
    registerSpecialized("MORLOKK", r150Morlokk::new);
    registerSpecialized("MORTALWOMBAT", r151MortalWombat::new);
    registerSpecialized("MRISHYELLOWBLOOD", r152MrishYellowblood::new);
    registerSpecialized("MUMMY", r153Mummy::new);
    registerSpecialized("MYSTICIST", r154Mysticist::new);
    registerSpecialized("NECROSPIDER", r155Necrospider::new);
    registerSpecialized("NEMESIS", r156Nemesis::new);
    registerSpecialized("NIGHTHUNTER", r157NightHunter::new);
    registerSpecialized("NIGHTBLADE", r158Nightblade::new);
    registerSpecialized("NIGHTBREED", r159Nightbreed::new);
    registerSpecialized("NIGHTCREEPER", r160Nightcreeper::new);
    registerSpecialized("OBSIDIANASSASSIN", r161ObsidianAssassin::new);
    registerSpecialized("OLINHAADBRIGAND", r162OlinHaadBrigand::new);
    registerSpecialized("OLINHAADGUARD3", r163OlinHaadGuard3::new);
    registerSpecialized("OLINHAADGUARD4", r164OlinHaadGuard4::new);
    registerSpecialized("OLINHAADGUARD5", r165OlinHaadGuard5::new);
    registerSpecialized("OLINHAADPRIVATEGUARD", r166OlinHaadPrivateGuard::new);
    registerSpecialized("OLINHAADSENTRY", r167OlinHaadSentry::new);
    registerSpecialized("OLINHAADSOLDIER10", r168OlinHaadSoldier10::new);
    registerSpecialized("OLINHAADSOLDIER12", r169OlinHaadSoldier12::new);
    registerSpecialized("ORACLEGATEGUARDIAN1", r170OracleGateGuardian1::new);
    registerSpecialized("ORACLEGATEGUARDIAN2", r171OracleGateGuardian2::new);
    registerSpecialized("ORACLEINVULNERABLEGUARDIAN", r172OracleInvulnerableGuardian::new);
    registerSpecialized("ORACLEQUICKNESSGUARDIAN", r173OracleQuicknessGuardian::new);
    registerSpecialized("ORCBATTLEMAGE", r174OrcBattlemage::new);
    registerSpecialized("ORCBERSERKER", r175OrcBerserker::new);
    registerSpecialized("ORCDESERTER", r176OrcDeserter::new);
    registerSpecialized("ORCGUARDIAN", r177OrcGuardian::new);
    registerSpecialized("ORCSCOUT", r178OrcScout::new);
    registerSpecialized("ORCSHAMAN", r179OrcShaman::new);
    registerSpecialized("ORCWARRIOR", r180OrcWarrior::new);
    registerSpecialized("ORGANICWASTE", r181OrganicWaste::new);
    registerSpecialized("PACKWOLF", r182PackWolf::new);
    registerSpecialized("PALADIN", r183Paladin::new);
    registerSpecialized("PILFERER", r184Pilferer::new);
    registerSpecialized("PLAGUERAT", r185PlagueRat::new);
    registerSpecialized("POISONOUSSNAKE", r186PoisonousSnake::new);
    registerSpecialized("PSIMONK", r187Psimonk::new);
    registerSpecialized("PSYKOWASPCOMMANDER", r188PsykowaspCommander::new);
    registerSpecialized("PSYKOWASPDEVASTATOR", r189PsykowaspDevastator::new);
    registerSpecialized("PSYKOWASPFEEDER", r190PsykowaspFeeder::new);
    registerSpecialized("PSYKOWASPSCOUT", r191PsykowaspScout::new);
    registerSpecialized("PSYKOWASPTROOPER", r192PsykowaspTrooper::new);
    registerSpecialized("PSYKOWASPWARRIOR", r193PsykowaspWarrior::new);
    registerSpecialized("PUPPETMASTER", r194PuppetMaster::new);
    registerSpecialized("PUTRIDBEAST", r195PutridBeast::new);
    registerSpecialized("QUELETHONTHETHIRSTY", r196QueletHonTheThirsty::new);
    registerSpecialized("RAIDER", r197Raider::new);
    registerSpecialized("RATSPUTIN", r198Ratsputin::new);
    registerSpecialized("RAVINGLUNATIC", r199RavingLunatic::new);
    registerSpecialized("RIBBONFIEND", r200RibbonFiend::new);
    registerSpecialized("ROAMINGCADAVER", r201RoamingCadaver::new);
    registerSpecialized("ROAMINGCORPSE", r202RoamingCorpse::new);
    registerSpecialized("ROBINHOOD", r203RobinHood::new);
    registerSpecialized("ROGUEMAGE", r204RogueMage::new);
    registerSpecialized("ROSHNAKTUL", r205RoshnakTul::new);
    registerSpecialized("RUK", r206Ruk::new);
    registerSpecialized("SANDWORM", r207SandWorm::new);
    registerSpecialized("SANDLORD", r208Sandlord::new);
    registerSpecialized("SCARFACERAZEK", r209ScarFaceRazek::new);
    registerSpecialized("SCAVENGERBAT", r210ScavengerBat::new);
    registerSpecialized("SHADOWDEMON", r211ShadowDemon::new);
    registerSpecialized("SHADOWFIEND", r212Shadowfiend::new);
    registerSpecialized("SHRIEKINGHORROR", r213ShriekingHorror::new);
    registerSpecialized("SKELETALCENTAUR", r214SkeletalCentaur::new);
    registerSpecialized("SKELETON", r215Skeleton::new);
    registerSpecialized("SKELETONGUARDIAN", r216SkeletonGuardian::new);
    registerSpecialized("SKELETONKING", r217SkeletonKing::new);
    registerSpecialized("SKELETONSERVANT1", r218SkeletonServant1::new);
    registerSpecialized("SKELETONSERVANT2", r219SkeletonServant2::new);
    registerSpecialized("SKELETONWARDER", r220SkeletonWarder::new);
    registerSpecialized("SKELETONWARLOCK", r221SkeletonWarlock::new);
    registerSpecialized("SKELETONWARRIOR", r222SkeletonWarrior::new);
    registerSpecialized("SKRAUGPEON", r223SkraugPeon::new);
    registerSpecialized("SKRAUGPEON2", r224Skraugpeon2::new);
    registerSpecialized("SKRAUGSHAMAN", r225SkraugShaman::new);
    registerSpecialized("SKRAUGSHAMAN2", r226SkraugShaman2::new);
    registerSpecialized("SKRAUGSKAVENGER", r227SkraugSkavenger::new);
    registerSpecialized("SKRAUGSKAVENGER2", r228Skraugskavenger2::new);
    registerSpecialized("SKRAUGWARRIOR", r229SkraugWarrior::new);
    registerSpecialized("SKRAUGWARRIOR2", r230Skraugwarrior2::new);
    registerSpecialized("SKRULL", r231Skrull::new);
    registerSpecialized("SKULLFIRE", r232Skullfire::new);
    registerSpecialized("SPECTRALKNIGHT", r233SpectralKnight::new);
    registerSpecialized("STINKBREATH", r234Stinkbreath::new);
    registerSpecialized("SUNKNIGHT", r235Sunknight::new);
    registerSpecialized("TAINTSCALE", r236Taintscale::new);
    registerSpecialized("TARANTULA", r237Tarantula::new);
    registerSpecialized("TAUNTINGHORROR", r238TauntingHorror::new);
    registerSpecialized("TBONE", r239TBone::new);
    registerSpecialized("TEMPLAR", r240Templar::new);
    registerSpecialized("TERRORDEMON", r241TerrorDemon::new);
    registerSpecialized("TESTSKELETONCENTAUR", r242TestSkeletonCentaur::new);
    registerSpecialized("THADOSS", r243Thadoss::new);
    registerSpecialized("TIMEGUARDIAN", r244TimeGuardian::new);
    registerSpecialized("TIMEPROTECTOR", r245TimeProtector::new);
    registerSpecialized("TIMEELEMENTAL", r246TimeElemental::new);
    registerSpecialized("TOLLTROLL", r247TollTroll::new);
    registerSpecialized("TOMBRAIDER", r248TombRaider::new);
    registerSpecialized("TRISHYELLOWBLOOD", r249TrishYellowblood::new);
    registerSpecialized("TROLLBIGBRUDDALBASHAH1", r250TROLLBIGBRUDDALBASHAH1::new);
    registerSpecialized("TROLLBIGWORVIKTOR1", r251TROLLBIGWORVIKTOR1::new);
    registerSpecialized("TROLLBLUDFIGHTOR1", r252TROLLBLUDFIGHTOR1::new);
    registerSpecialized("TROLLCLANGBANGAH1", r253TROLLCLANGBANGAH1::new);
    registerSpecialized("TROLLGRUBBRINGAH1", r254TROLLGRUBBRINGAH1::new);
    registerSpecialized("TROLLMADLIMBMANGLOR1", r255TROLLMADLIMBMANGLOR1::new);
    registerSpecialized("TROLLMEANHEADDRUMMAH1", r256TROLLMEANHEADDRUMMAH1::new);
    registerSpecialized("TROLLMOONFIREDANSAH1", r257TROLLMOONFIREDANSAH1::new);
    registerSpecialized("TROLLMUNCHHUNTOR1", r258TROLLMUNCHHUNTOR1::new);
    registerSpecialized("TROLLPROTEKTERRITOR1", r259TROLLPROTEKTERRITOR1::new);
    registerSpecialized("TROLLSTANDANBONKAH1", r260TROLLSTANDANBONKAH1::new);
    registerSpecialized("TROLLTOTEMHERBDOKTOR1", r261TROLLTOTEMHERBDOKTOR1::new);
    registerSpecialized("UNDEADBAT", r262UndeadBat::new);
    registerSpecialized("UNDEADGUARDIAN", r263UndeadGuardian::new);
    registerSpecialized("UNDEADSENTINEL", r264UndeadSentinel::new);
    registerSpecialized("UNSEENBAT", r265UnseenBat::new);
    registerSpecialized("VAMPIREBAT", r266VampireBat::new);
    registerSpecialized("WANDERINGORC", r267WanderingOrc::new);
    registerSpecialized("WARG", r268Warg::new);
    registerSpecialized("WASPDRONE", r269WaspDrone::new);
    registerSpecialized("WERERAT", r270Wererat::new);
    registerSpecialized("WIDOWHATCHLING", r271WidowHatchling::new);
    registerSpecialized("WILDHORSE", r272WildHorse::new);
    registerSpecialized("WIZARDMUMMY", r273WizardMummy::new);
    registerSpecialized("WOLFHOUND", r274Wolfhound::new);
    registerSpecialized("WOODSTALKER", r275Woodstalker::new);
    registerSpecialized("WORSHIPPER", r276Worshipper::new);
    registerSpecialized("WRAITHBAT", r277WraithBat::new);
    registerSpecialized("YOGGOTHWORM", r278YoggothWorm::new);
    registerSpecialized("ZZARTGAX", r279Zzartgax::new);
    registerSpecialized("MOBMERCENARYC", r280MOBMERCENARYC::new);
    registerGeneratedFactories();
    registerSpecialized("CARMAN", com.perso.T4C.monster.Carman::new);
    registerSpecialized("MAKRSHPTANGH2", com.perso.T4C.monster.MakrshPtangh2::new);
    registerSpecialized("FENRIR", Fenrir::new);
    registerSpecialized("JORMUNGAND", Jormungand::new);
    registerSpecialized("MAKRSHPTANGHSPAWNER", MAKRSHPTANGHSPAWNER::new);
    registerSpecialized("MOBFLESHGOLEM", FleshGolem::new);
    registerSpecialized("MOBDARKCLERIC", DarkCleric::new);
    registerSpecialized("MOBBLACKPROPHET", BlackProphet::new);
    registerSpecialized("MOBDARKNOBLE", DarkNoble::new);
    registerSpecialized("MOBMADBERSERKERDEMON", MadBerserkerDemon::new);
    registerSpecialized("MOBCRAZEDNURSE", CrazedNurse::new);
    registerSpecialized("MOBMADMAN", MadMan::new);
    registerSpecialized("MOBRAVINGLUNATIC", RavingLunatic::new);
    registerSpecialized("MOBRUNAWAYPATIENT", RunawayPatient::new);
    registerSpecialized("MOBMADPATIENT", MadPatient::new);
    registerSpecialized("MOBDERANGEDORDERLY", DerangedOrderly::new);
    registerSpecialized("MOBNIGHTRETRIEVER", NightRetriever::new);
    registerSpecialized("MOBPURIFIER", Purifier::new);
    registerSpecialized("MOBNEOFLARE", Neoflare::new);
    registerSpecialized("MOBSHADOWSTALKER", Shadowstalker::new);
    registerSpecialized("MOBILLUSIONWEAVER", IllusionWeaver::new);
    registerSpecialized("MOBGEHENNAREAVER", GehennaReaver::new);
    registerSpecialized("MOBHERETICWARRIOR", HereticWarrior::new);
    registerSpecialized("MOBLOON", Loon::new);
    registerSpecialized("MOBYOGGOTHWORM", YoggothWorm::new);
    registerSpecialized("MOBREDEYEDCENTAUR", RedEyedCentaur::new);
    registerSpecialized("MOBDELWOBBLE", Delwobble::new);
    registerSpecialized("MOBQUENTINTARANTULA", QuentinTarantula::new);
    registerSpecialized("MOBSHRIEKERRODENT", ShriekerRodent::new);
    registerSpecialized("MOBGIANTBLACKWIDOW", GiantBlackWidow::new);
    registerSpecialized("CENTAURAVENGER", CentaurAvenger::new);
    registerSpecialized("CENTAURCHAMPION", CentaurChampion::new);
    registerSpecialized("CENTAURMANAWEAVER", CentaurManaweaver::new);
    registerSpecialized("CENTAURSENTINEL", CentaurSentinel::new);
    registerSpecialized("CENTAURTRACKER", CentaurTracker::new);
    registerSpecialized("MOBMERCENARYA", MercenaryA::new);
    registerSpecialized("MOBMERCENARYB", MercenaryB::new);
    registerSpecialized("MOBMERCENARYC", MercenaryC::new);
    registerSpecialized("MOBMERCENARYLEADER", MercenaryLeader::new);
    registerSpecialized("MOBMERCENARYLIEUTENANTA", MercenaryLieutenantA::new);
    registerSpecialized("MOBMERCENARYLIEUTENANTB", MercenaryLieutenantB::new);
    registerSpecialized("MOBMERCENARYLIEUTENANTC", MercenaryLieutenantC::new);
    registerSpecialized("MOBHUNTER1", Hunter1::new);
    registerSpecialized("MOBHUNTER2", Hunter2::new);
    registerSpecialized("BLOODLUST", Bloodlust::new);
    registerSpecialized("DRVONPYRE", DrVonPyre::new);
    registerSpecialized("MOBMORDRED", Mordred::new);
    registerSpecialized("MADMADRIGAN", MadMadrigan::new);
    registerSpecialized("DOPPELGANGER", Doppelganger::new);
    registerSpecialized("OLINHAAD2", OlinHaad2::new);
    registerSpecialized("OLINHAADASSASSIN", OlinHaadAssassin::new);
    registerSpecialized("OLINHAADCOMMANDER", OlinHaadCommander::new);
    registerSpecialized("URIKTHIEF", UrikThief::new);
    registerSpecialized("GLURIURL", Gluriurl::new);
    registerSpecialized("GORLOKBLOODAXE", GorlokBloodaxe::new);
    registerSpecialized("HARVESTEROFLIFE", HarvesterOfLife::new);
    registerSpecialized("OLINHAADELITEGUARD", OlinHaadEliteGuard::new);
    registerSpecialized("MOBOBSIDIANCONCLAVEKNIGHT", ObsidianConclaveKnight::new);
    registerSpecialized("MOBOBSIDIANKNIGHT", ObsidianConclaveKnight::new);
    registerSpecialized("ANTHORTHEMAD", AnthorTheMad::new);
    registerSpecialized("DEEPONEBOSS", DeepOneBoss::new);
  }

  public static synchronized void registerSpecialized(String name, MonsterFactory factory) {
    if (name == null || name.isBlank() || factory == null) {
      throw new IllegalArgumentException("Monster specialization requires a name and factory");
    }
    SPECIALIZED_FACTORIES.put(normalize(name), factory);
  }

  private static void registerGeneratedFactories() {}

  public static synchronized BaseMonster create(MonsterDef definition, float worldX, float worldY)
      throws GameException {
    if (definition == null) throw new IllegalArgumentException("definition");
    MonsterFactory factory = SPECIALIZED_FACTORIES.get(normalize(definition.getName()));
    if (factory == null) {
      factory = resolveBinFactory(definition.getName());
    }
    return factory == null
        ? new NamedEventMonster(definition, worldX, worldY)
        : factory.create(definition, worldX, worldY);
  }

  private static MonsterFactory resolveBinFactory(String canonicalName) {
    if (canonicalName == null || canonicalName.isBlank()) return null;
    String compact = canonicalName.replaceAll("[^A-Za-z0-9]", "");
    String[] candidates = {
      "com.perso.T4C.monster." + canonicalName.replaceAll("[^A-Za-z0-9]", ""),
      "com.perso.T4C.monster." + compact.toUpperCase(Locale.ROOT)
    };
    for (String className : candidates) {
      try {
        Class<?> type = Class.forName(className);
        var constructor = type.getConstructor(MonsterDef.class, float.class, float.class);
        return (definition, worldX, worldY) -> {
          try {
            return (BaseMonster) constructor.newInstance(definition, worldX, worldY);
          } catch (ReflectiveOperationException e) {
            throw new GameException("Unable to instantiate " + className, e);
          }
        };
      } catch (ReflectiveOperationException | LinkageError ignored) {
      }
    }
    return null;
  }

  public static synchronized List<MonsterDef> load() {
    if (cache != null) {
      return cache;
    }
    File file = new File(Paths.MONSTERS_BIN);
    if (!file.exists()) {
      rebuild(List.of());
      return cache;
    }
    try {
      rebuild(MonsterDefBinaryIO.read(file));
    } catch (Exception ignored) {
      rebuild(List.of());
    }
    return cache;
  }

  public static synchronized void save(List<MonsterDef> defs) throws IOException {
    MonsterDefBinaryIO.write(new File(Paths.MONSTERS_BIN), defs);
    rebuild(defs);
  }

  public static synchronized MonsterDef findByName(String name) {
    load();
    if (name == null) return null;
    MonsterDef exact = byName.get(name);
    if (exact != null) return exact;
    MonsterDef normalized = byNormalizedName.get(normalize(name));
    if (normalized != null) return normalized;
    MonsterDef alias = byAlias.get(name);
    return alias != null ? alias : byAlias.get(normalize(name));
  }

  public static synchronized void invalidate() {
    cache = null;
    byName = null;
    byNormalizedName = null;
  }

  private static void rebuild(List<MonsterDef> defs) {
    cache = List.copyOf(defs);
    Map<String, MonsterDef> map = new LinkedHashMap<>();
    Map<String, MonsterDef> normalized = new LinkedHashMap<>();
    Map<String, MonsterDef> aliases = new LinkedHashMap<>();
    for (MonsterDef def : cache) {
      if (def != null && def.getName() != null) {
        map.put(def.getName(), def);
        normalized.putIfAbsent(normalize(def.getName()), def);
        String displayKey = I18n.keyOf(def.getDisplayName());
        if (displayKey != null) {
          normalized.putIfAbsent(normalize(displayKey.substring(displayKey.indexOf('.') + 1)), def);
        } else if (def.getDisplayName() != null) {
          normalized.putIfAbsent(normalize(def.getDisplayName()), def);
        }
        if (def.getSpawnAliases() != null) {
          for (String alias : def.getSpawnAliases()) {
            if (alias != null && !alias.isBlank()) {
              aliases.putIfAbsent(alias, def);
              aliases.putIfAbsent(normalize(alias), def);
            }
          }
        }
      }
    }
    byName = map;
    byNormalizedName = normalized;
    byAlias = aliases;
  }

  private static String normalize(String value) {
    return value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
  }
}
