import com.google.gson.GsonBuilder;
import com.perso.T4C.combat.*;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.item.*;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.player.*;
import com.perso.T4C.spawn.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Offline, repeatable combat benchmark. Does not load or save a real character. */
public class ArcherLevelingSimulation {
  static final long REQUIRED_XP = 19_710_821_719L;
  static final int RUNS = 1000;
  static final long SEED = 20261025;
  static int decode(int value) {
    return value >= 0 && value < 100000 ? value : Math.max(0, Math.round(Float.intBitsToFloat(value)));
  }
  static Player archer(int archery) {
    var p = new Player();
    p.setLevel(200); p.setRebirthCount(50); p.setQuestFlag("__FLAG_NUMBER_OF_REMORTS",50);
    p.setStrength(270); p.setDexterity(1000); p.setEndurance(550);
    p.setIntelligence(270); p.setWisdom(270);
    p.setSkillLevel("archery",archery); p.setSkillLevel("dodge",1000);
    for(var s:List.of("armor_penetration","powerful_blow","stun_blow"))p.setSkillLevel(s,100);
    for(var key:List.of("tension_primordiale_ultimate","worldbreaker_quiver",
        "centaur_slaying_armor","centaur_slaying_protector","divine_artemis_helm",
        "divine_artemis_leggings","divine_artemis_boots","divine_artemis_gauntlets",
        "divine_artemis_necklace","wyrmling_scale_bracer","ring_of_the_archer",
        "concord_bell_mantle_archer")) {
      var i=Objects.requireNonNull(ItemRegistry.findByKey(key),key);
      p.getEquippedItems().put(i.getBodyPart(),i.getKey());
    }
    p.getEquippedItems().put(BodyPart.RING2,ItemRegistry.findByKey("concord_dawn_signet_archer").getKey());
    return p;
  }
  static Map<String,Object> simulate(Player player, MonsterDef monster, int pack, int baseArchery, boolean requestedDamage) {
    var profile=CombatProfiles.fromPlayer(player);
    var weapon=ItemRegistry.findByKey(player.getEquippedItems().get(BodyPart.WEAPON));
    var quiver=ItemRegistry.findByKey(player.getEquippedItems().get(BodyPart.WEAPON2));
    String formula=requestedDamage ? "1d154+396+3*arrow_dmg" : weapon.getDmgFormula();
    var match=Pattern.compile("1d(\\d+)\\+(\\d+)\\+(\\d+)\\*arrow_dmg").matcher(formula);
    if(!match.matches())throw new IllegalStateException(weapon.getDmgFormula());
    int faces=Integer.parseInt(match.group(1)),flat=Integer.parseInt(match.group(2)),coefficient=Integer.parseInt(match.group(3));
    int arrowFaces=Integer.parseInt(quiver.getDmgFormula().substring(2));
    int natural=(player.getEffectiveStrength()-20)/20+(player.getEffectiveDexterity()-20)/10+EquipmentBonusRules.bonus(player,10);
    var splash=quiver.getQuiverSplash();
    int acMin=decode(monster.getAcMin()),acMax=Math.max(acMin,decode(monster.getAcMax()));
    double delay=Double.parseDouble(weapon.getAtkDelay())/1000;
    long reward=Math.round(monster.getXpOnDeath()*(double)GameConstants.SERVER_XP_RATE);
    if(monster.getXpPerHit()!=0)throw new IllegalStateException("Benchmark assumes kill-only XP");
    var durations=new double[RUNS];long attempts=0,hits=0,damage=0;
    for(int run=0;run<RUNS;run++) {
      var random=new Random(SEED+run+pack*10000L+baseArchery);
      long xp=0;double combatSeconds=0;int pulls=0;
      while(xp<REQUIRED_XP) {
        pulls++;int[] hp=new int[pack];double[] stunnedUntil=new double[pack];Arrays.fill(hp,monster.getHealth());
        while(xp<REQUIRED_XP) {
          int primary=-1;
          for(int i=0;i<pack;i++)if(hp[i]>0&&(primary<0||hp[i]<hp[primary]))primary=i;
          if(primary<0)break;
          combatSeconds+=delay;attempts++;
          int armor=acMin+random.nextInt(acMax-acMin+1);
          var target=new CombatProfile(monster.getLevel(),monster.getStr(),monster.getEnd(),monster.getAgi(),
              monster.getAgi()+monster.getLevel(),monster.getAgi()+monster.getLevel(),monster.getDodge(),armor,
              stunnedUntil[primary]>combatSeconds,false,true,Map.of());
          int raw=random.nextInt(faces)+1+flat+coefficient*(random.nextInt(arrowFaces)+1)+natural;
          var result=CombatResolver.resolve(new PhysicalAttackRequest(profile,target,raw,0,true),random);
          if(!result.hit())continue;
          hits++;stunnedUntil[primary]=combatSeconds+result.stunDurationMillis()/1000d;
          if(result.damage()<=0)continue;
          int dealt=result.damage();damage+=dealt;hp[primary]-=dealt;
          if(hp[primary]<=0)xp+=reward;
          // Snapshot the same pulled pack; primary is included in the eight-target limit.
          if(pack>1)for(int i=0;i<pack;i++)if(i!=primary&&hp[i]>0) {
            hp[i]-=splash.damage(dealt);
            if(hp[i]<=0)xp+=reward;
          }
        }
      }
      // 80% attacking uptime (kiting/LOS/repositioning) plus eight seconds to pull each pack.
      durations[run]=combatSeconds/.8+pulls*8;
    }
    Arrays.sort(durations);double mean=Arrays.stream(durations).average().orElseThrow();
    var row=new LinkedHashMap<String,Object>();row.put("monster",monster.getName());row.put("level",monster.getLevel());
    row.put("hp",monster.getHealth());row.put("xpPerKill",reward);row.put("killsNeeded",(REQUIRED_XP+reward-1)/reward);
    row.put("baseArchery",baseArchery);row.put("effectiveArchery",profile.archery());row.put("effectiveAgility",profile.agility());
    row.put("simultaneousTargets",pack);row.put("meanSeconds",mean);row.put("p10Seconds",durations[100]);row.put("p90Seconds",durations[900]);
    row.put("hitRate",hits/(double)attempts);row.put("meanDamagePerSuccessfulHit",damage/(double)hits);
    row.put("weaponFormula",formula);row.put("damageModel",requestedDamage ? "earlier_requested_397_550" : "currently_installed");row.put("attackIntervalSeconds",delay);
    return row;
  }
  public static void main(String[] args)throws Exception {
    ItemJsonLoader.loadAndRegister();MonsterJsonLoader.loadAndRegister();var rows=new ArrayList<Map<String,Object>>();
    for(boolean requestedDamage:List.of(false,true))for(int skill:List.of(1000,2000)) {
      var player=archer(skill);
      rows.add(simulate(player,MonsterRegistry.findByName("Arch Drake"),1,skill,requestedDamage));
      for(int pack:List.of(1,4,8))rows.add(simulate(player,MonsterRegistry.findByName("Kraanian Dragonguard"),pack,skill,requestedDamage));
    }
    var output=new LinkedHashMap<String,Object>();output.put("requiredXp",REQUIRED_XP);output.put("runsPerScenario",RUNS);output.put("seed",SEED);
    output.put("assumptions",List.of("Level 200, 50 rebirths, STR 270 / AGI 1000 / END 550 / INT 270 / WIS 270",
        "Current installed bow, Worldbreaker Quiver and mixed offensive armor/accessories; 100 Armor Penetration, Powerful Blow and Stun Blow",
        "CombatResolver handles accuracy, armor, penetration, powerful hits and stuns; quiver splash uses the installed percentage",
        "80% attack uptime plus 8 seconds per pull; solo player receives full kill XP; no XP buffs, party sharing or quest rewards",
        "Successful continuous ranged kiting assumed: no deaths, incoming-hit delays, regeneration, Seraph aura damage/healing, or defensive spells",
        "Arch Drake has two nominal spawn copies with 30s respawn; kill time exceeds respawn so alternating copies causes no idle time",
        "Dragonguard field has 40 dense anchors and 8–12s refill; 17 kills fit initial population; 4/8-target packs assume targets stay inside splash radius and LOS",
        "Monte Carlo variation covers combat rolls only; it does not measure navigation, pulling feasibility, survivability or player mistakes"));
    output.put("results",rows);Files.writeString(Path.of("docs/content-ideas/xp-curve/level-200-simulation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(output)+"\n");
    for(var row:rows)System.out.printf("%s | base archery %s | targets %s | mean %.1f min | p10 %.1f p90 %.1f | hit %.1f%% | %s%n",
        row.get("monster"),row.get("baseArchery"),row.get("simultaneousTargets"),(double)row.get("meanSeconds")/60,
        (double)row.get("p10Seconds")/60,(double)row.get("p90Seconds")/60,(double)row.get("hitRate")*100,row.get("weaponFormula"));
  }
}
