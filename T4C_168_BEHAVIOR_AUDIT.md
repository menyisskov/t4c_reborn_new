# Audit comportemental T4C 1.68 / implémentation Java

Sources de référence analysées :

- Client natif 1.68 RC14h : `elestranobaron/Client`, commit `7fa6abf`.
- Serveur natif associé : `elestranobaron/Server`, commit `ea0a8b5`.
- Implémentation comparée : workspace Java courant.

Ce document recense uniquement les écarts déjà vérifiés dans le code. `Absent` signifie qu’aucun point d’entrée ou service Java correspondant n’a été trouvé ; `Partiel` signifie qu’une partie locale existe mais que le comportement 1.68 complet n’est pas reproduit.

## 1. Architecture réseau et autorité

| Domaine | Client/serveur 1.68 | Java actuel | État |
|---|---|---|---|
| Transport | UDP avec en-tête, checksum, fragmentation, files de paquets, ACK et timeouts | Aucun package réseau, paquet ou socket dans `src/main/java` | Absent |
| Mouvement | Huit requêtes directionnelles `RQ_Move*`, envoyées au serveur ; la file de mouvement est limitée à 25 | Déplacement local continu avec budget de distance par frame et chemin local dans `PlayerMovement` | Partiel / comportement différent |
| Autorité d’état | Le client reçoit les positions, unités, HP, XP, or, mana, poids et effets via paquets | État principalement local dans `Player` et les services Java | Partiel |
| Anti-saturation | Les paquets de mouvement sont abandonnés au-delà de 25 éléments en file | Aucun équivalent réseau | Absent |
| Synchronisation | Paquets d’existence/unité manquante, unités périphériques, position et état serveur | Aucun mécanisme équivalent | Absent |

Preuves natives : `ComPacketHeader.h`, `Comm.cpp`, `Packet.cpp`, `PacketTypes.h`.

## 2. Temporisations client vérifiées

Le client 1.68 configure explicitement les valeurs suivantes dans `Comm.cpp` :

| Action | Délai maximum | ACK maximum |
|---|---:|---:|
| Mouvement | 0 ms | 0 |
| Attaque | 500 ms | 3 |
| Sort | 1000 ms | 5 |
| Compétence | 1000 ms | 3 |
| Téléportation | 750 ms | 5 |
| Vol | 1000 ms | 3 |
| Flèche touchée/manquée | 500 ms | 3 |
| Coffre | 500 ms | 5 |
| Commerce | 500 ms | 5 |

L’implémentation Java contient des cooldowns locaux d’attaque/sort/compétence, mais pas la couche ACK, retransmission, rejet et file d’attente qui donne leur sémantique au client original. Les valeurs ne peuvent donc pas être considérées comme équivalentes au comportement 1.68.

## 3. Fonctionnalités réseau absentes

Les paquets suivants existent dans le client original mais aucun service Java correspondant n’a été trouvé :

- compte et cycle de connexion : `RQ_RegisterAccount`, `RQ_PutPlayerInGame`, `RQ_DeletePlayer`, `RQ_CreatePlayer`, `RQ_ReturnToMenu`, `RQ_AuthenticateServerVersion` ;
- synchronisation joueurs : `RQ_GetPlayerPos`, `RQ_GetStatus`, `RQ_GetOnlinePlayerList`, `RQ_GetUnitName`, `RQ_GetNearItems` ;
- effets et ressources : `RQ_HPchanged`, `RQ_XPchanged`, `RQ_ManaChanged`, `RQ_UpdateWeight`, `RQ_CreateEffectStatus`, `RQ_DispellEffectStatus` ;
- monde : `RQ_WeatherMsg`, `RQ_GetTime`, `RQ_OpenURL` ;
- vol et tir : `RQ_Rob`, `RQ_DispellRob`, `RQ_ArrowHit`, `RQ_ArrowMiss` ;
- guildes : `RQ_GuildInvite`, `RQ_GuildKick`, `RQ_GuildLeave`, `RQ_GuildAlterRights`, `RQ_GuildRename`, `RQ_GuildInviteAnswer`.

Le Java contient des objets, sorts ou compétences portant parfois un nom similaire, mais pas le protocole ni les transitions d’état multi-joueur correspondants.

## 4. Commerce joueur-joueur

Le serveur original implémente `TradeMgr2` dans `Trade.h`/`Trade.cpp` avec :

- états de commerce `Invalid`, `Inviting`, `Trading` ;
- états individuels `EditingItems`, `Ready`, `Confirmed` ;
- validation de distance, disponibilité des deux joueurs et poids disponible ;
- verrou global des opérations ;
- ajout/retrait d’objets depuis un conteneur intermédiaire ;
- remise automatique des objets au sac en cas d’annulation ou de perturbation ;
- confirmation des deux parties avant transfert ;
- notifications client séparées pour contenu, statut, début, annulation et fin.

Le Java n’a ni package `trade`, ni `TradeService`, ni machine d’état ou transfert atomique entre deux joueurs. Écart : **fonctionnalité absente**.

## 5. Groupes

`Group.h`/`Group.cpp` du serveur original fournit : invitations, chef, exclusion, départ, portée des membres, partage automatique, distribution de l’XP de kill, distribution de l’or, mise à jour de la liste et HP des membres.

Le Java n’a ni package `group`, ni service de groupe, ni modèle de membre/chef/invitation. Les classes `SpawnGroup` concernent uniquement les groupes de spawn et ne reproduisent pas ce système. Écart : **fonctionnalité absente**.

## 6. Guildes

Le client contient `GuildUI` et les requêtes de gestion de guilde ; le serveur contient `Guilds` et les traitements associés. Le Java ne possède pas de service ou modèle de guilde multi-joueur. Les noms d’objets comme `GuildChest` ne constituent pas cette fonctionnalité. Écart : **fonctionnalité absente**.

## 7. Règles de combat déjà équivalentes ou presque

Les éléments suivants ne doivent pas être comptés comme absents :

- la précision originale `rnd(attackSkill) + rnd(attackAgi / 3) - rnd(dodgeSkill) - rnd(targetAgi / 3)` est reproduite dans `CombatResolver` ;
- les dégâts naturels originaux sont reproduits dans `CombatMath` pour mêlée et arc ;
- le Java possède des profils attaque/esquive, résistance, parade et pénalités d’équipement.

Les écarts restants de combat portent sur l’intégration serveur, la transmission des résultats, les timers réseau et les comportements client des unités distantes, pas sur cette formule de précision de base.

## 8. Déplacement

Le client original manipule des directions discrètes T4C (8 directions) et envoie des requêtes serveur. Le Java utilise une interpolation locale avec réservation de pas, glissement vers des directions adjacentes et chemin local dans `PlayerMovement`.

Différences vérifiées :

- pas de validation serveur ni de correction de position reçue ;
- pas de file de requêtes ni de limite de 25 mouvements ;
- le Java peut exécuter plusieurs pas dans une frame selon le budget de déplacement ;
- les blocages et glissements sont décidés localement, alors que le client original attend le résultat du serveur ;
- les événements de déplacement des autres unités ne proviennent pas d’un flux réseau.

## 9. Inventaire, objets et conteneurs

Le serveur original représente chaque entrée par un objet avec identifiant, apparence, référence statique, quantité et charges. `ItemContainer::Put` refuse l’ajout lorsque le poids de la quantité complète dépasse le poids libre, puis empile les objets compatibles selon leur caractère unique. La sérialisation envoie séparément quantité et charges (`ItemContainer.cpp`).

Le Java représente l’inventaire de `Player` comme une `List<String>` et maintient les charges dans une map séparée. `InventoryService` reproduit plusieurs validations utiles (poids, unicité, exigences, emplacement, durabilité), mais il n’existe pas d’identifiant d’instance d’objet ni de quantité portée par une instance.

Différences vérifiées :

- les quantités originales sont des champs d’objet et sont transmises avec l’ID ; le Java modélise les quantités empilées par répétition de clés dans une liste ;
- les objets originaux peuvent être ciblés par ID d’instance dans les requêtes ; le Java cible principalement une clé et parfois un index ;
- la sérialisation client originale distingue apparence, ID, référence statique, quantité et charges ; le Java n’a pas de sérialiseur de paquet équivalent ;
- les opérations atomiques du conteneur original sont verrouillées côté serveur ; les opérations Java sont locales et non transactionnelles entre deux acteurs.

Le calcul Java de poids existe, mais il ne reproduit pas la sémantique multi-instance et multi-client du conteneur serveur original.

## 10. Coffres et objets interactifs

Le client original reçoit un contenu de coffre via `RQ_ChestContents`, puis utilise des opérations distinctes `RQ_ChestAddItemFromBackpack`, `RQ_ChestRemoveItemToBackpack`, `RQ_ShowChest` et `RQ_HideChest`. Le coffre est donc un conteneur persistant présenté au client, avec transferts explicites dans les deux sens.

Le Java possède `ChestService`, mais son comportement actuel est différent : l’ouverture tire un loot local, le fait apparaître au sol et applique un respawn local ; il n’existe pas de conteneur de coffre manipulable, de transfert coffre/sac, de paquets d’ouverture/fermeture ou de contenu persistant par joueur.

Écart : **le Java implémente des coffres de loot locaux, pas les coffres-containers 1.68**.

## 11. Achat et vente auprès des PNJ

Le client original distingue les listes d’achat et de vente (`RQ_SendBuyItemList`, `RQ_SendSellItemList`) et les requêtes d’objets. Le serveur dispose de conteneurs d’objets, de quantités, poids et identifiants d’instances pour valider les opérations.

Le Java possède `ShopScreen` et calcule un prix de vente égal à la moitié du prix d’achat. La transaction est directement appliquée au `Player` local. Aucun stock de vendeur, identifiant d’offre, vérification serveur, réservation de quantité ou transaction atomique n’est présent.

Écarts vérifiés :

- interface locale présente, mais absence du protocole et de l’autorité serveur ;
- prix de vente codé comme `price / 2`, alors que le serveur original laisse la règle au contenu/serveur et peut distinguer les listes d’achat et de vente ;
- pas de stock vendeur persistant ni de quantité d’offre portée par instance ;
- pas de retour réseau pour confirmer ou refuser l’achat/vente.

## 12. Utilisation et équipement

Le Java couvre l’équipement, les exigences, les charges et la durabilité dans `InventoryService`/`ItemUseService`. L’écart restant est architectural : le client original envoie `RQ_UseObject`, `RQ_EquipObject` et `RQ_UnequipObject` et attend la mise à jour serveur de l’inventaire, de l’équipement, du poids et des charges. Le Java applique directement la mutation locale et ne peut pas reproduire les rejets concurrents ou les corrections d’état du serveur.

## Synthèse intermédiaire — sorts

## 13. Sorts, effets et ressources

Le Java possède une couverture importante : `SpellCastingService`, `SpellEffectManager`, `SpellRenderer`, effets persistants, cooldowns, coût de mana, portée, ligne de vue, projectiles et effets visuels. Le client/serveur original possède les mêmes grandes familles via `RQ_CastSpell`, `RQ_SpellEffect`, `RQ_CreateEffectStatus`, `RQ_DispellEffectStatus`, `RQ_ManaChanged` et les gestionnaires natifs de sorts/effets.

Les différences de fonctionnement restent néanmoins vérifiées :

- le client original ne décide pas localement du résultat d’un sort : il envoie l’ID et les paramètres puis reçoit l’effet, les changements de mana et les statuts ; le Java applique localement le coût, l’exhaustion, le cooldown et plusieurs effets ;
- le client original synchronise séparément l’impact visuel (`RQ_SpellEffect`) et le statut persistant (`RQ_CreateEffectStatus`/`RQ_DispellEffectStatus`) ; le Java peut déclencher rendu et mutation dans le même flux local ;
- les effets appliqués aux unités distantes, les corrections de mana et les expirations reçues du serveur n’ont pas d’équivalent réseau Java ;
- le Java fixe `MAXIMUM_CAST_RANGE_TILES` à 20 dans `SpellCastingService`, alors que le client original n’est pas l’autorité de cette limite et la reçoit du comportement serveur/du sort ; cette constante peut donc diverger selon le sort ;
- les callbacks Java de projectile (`Runnable onImpact`) exécutent la conséquence localement après animation, alors que le client original reçoit l’événement de résultat depuis le serveur.

État : **fonctionnalité locale partielle, non équivalente en multijoueur et potentiellement divergente pour les timings/résultats**.

## 14. Ressources et régénération

Le serveur original expose `HPregen`, `ManaRegen` et `FaithRegen` dans `GAME_RULES`, et le client reçoit les changements HP/mana par paquets. Le Java possède une régénération périodique locale HP/mana dans `Player.update` et `RegenerationRules`, mais :

- aucune ressource `faith` jouable n’a été trouvée dans le modèle Java ;
- les changements de ressources ne sont pas diffusés à d’autres clients ;
- une correction serveur après désynchronisation est impossible sans transport réseau ;
- la fréquence locale de tick (2 secondes dans `RegenerationRules`) n’est pas une preuve d’identité avec tous les chemins de régénération du serveur original.

État : **HP/mana partiels, foi absente, synchronisation absente**.

## Synthèse intermédiaire — chat

## 15. Chat et canaux

Le client original possède un système de chatter serveur avec messages directs, shout, page, discussion indirecte, canaux, liste des canaux, liste des utilisateurs d’un canal et ajout/retrait d’un canal. Les paquets sont traités dans `Packet.cpp` et l’interface les émet depuis `ChatterUI.cpp`.

Le Java possède `GameChat`, mais ce composant est une console locale : `addLocalMessage`, `addNpcMessage` et `addSystemMessage` alimentent directement la liste d’affichage, et le submit handler est local. Aucun transport, canal serveur, liste d’utilisateurs, message privé, shout/page ou accusé de réception n’a été trouvé.

État : **affichage de chat présent, communication 1.68 absente**.

## 16. Mort, pénalités et résurrection

- L’original distingue les morts contre monstre et contre joueur, avec des paramètres séparés pour XP, sac, équipement, or perdu et or lâché. Les valeurs spéciales 900–999 représentent une pénalité progressive dépendant du niveau.
- L’original applique la perte d’XP au-dessus du seuil du niveau courant. Le Java utilise `DeathPenaltyService` sur `currentXp` ; l’équivalence dépend donc de la sémantique exacte de `currentXp` et doit être verrouillée par test de niveau.
- L’original exclut explicitement les objets non jetables et manipule des instances avec quantité/charges. Le Java transmet les objets/charges à `spawnCorpse`, mais utilise des réglages par défaut codés et une représentation d’inventaire moins riche.
- La résurrection Java remet le joueur à la moitié des PV maximum, comme `GAME_RULES::DeathPenalties`. Les traitements de mode bataille, équipes, récompenses de meurtre et karma présents dans le serveur original ne sont pas reproduits par une logique d’équipe identifiable dans le Java.

## 17. PNJ, dialogues et quêtes

- Le client 1.68 envoie des requêtes distinctes de conversation indirecte, dirigée et de page ; les réponses serveur contrôlent texte, choix et effets. Le Java passe directement de `NPCInputHandler` à `NPCManager`/`NpcScriptEngine`, sans requête ni confirmation serveur.
- Le Java possède une couverture importante de scripts PNJ et un `QuestService` fonctionnel : flags, objectifs de monstres dans une zone, récompenses XP/or et persistance. Cela ne garantit pas l’équivalence des scripts originaux : les commandes serveur doivent encore être comparées handler par handler.
- La progression générique Java est limitée au nom du monstre, monde et zone circulaire. Les objectifs originaux fondés sur objets précis, variables, groupes, PvP, chronomètres, dialogues conditionnels ou événements de carte nécessitent un handler dédié.
- Le journal Java (`QuestScreen`) est local ; il ne remplace pas la synchronisation d’état et les événements serveur du client 1.68.

## 18. Météo, éclairage et rendu de carte

- Le client original contient un système météo explicite (`weather.cpp/.h`) avec pluie, neige, intensité, état activé/désactivé et rendu des particules. Aucun gestionnaire Java de pluie/neige, aucun état météo synchronisé et aucun traitement du paquet météo 1.68 n'a été trouvé.

Le détail du chemin natif est également fonctionnel : `RQ_WeatherMsg` (paquet 104) reçoit un effet `1`, `2` ou `3` pour pluie, neige ou brouillard et une valeur `OFF`/`ON`. La pluie conserve des positions aléatoires de gouttes et peut afficher des éclairs à intensité élevée ; la neige conserve ses flocons et leur variante de sprite entre les dessins. `bShowWeatherEffects` peut en plus masquer ces effets côté client, sans annuler nécessairement l’état reçu.

Le Java ne dispose ni du décodage de ces trois effets, ni d’un état persistant d’intensité/particules, ni du filtrage d’affichage météo équivalent. Une météo éventuellement représentée par des éléments de carte statiques ne peut donc pas reproduire l’activation serveur, l’évolution des particules, les éclairs et la séparation entre état reçu et option d’affichage du client 1.68.
- Le client original contient aussi `LightMap`, avec création/fusion de lightmaps et effets d'éclairage par zone. Le Java possède un `DayNightCycle` qui applique un niveau d'ambiance global, mais aucune équivalence trouvée pour une lightmap locale fusionnée tuile par tuile.

`LightMap::MakeBaseLightMap` initialise une surface de lumière demi-résolution, puis `MergeLightMap` fusionne les sources locales (dont la torche principale) avant que `MakeLightingFX` ne multiplie les canaux RGB de chaque pixel source par la valeur de lumière. Le chemin haute qualité (`bLightHightGraph`) change en plus l’algorithme de fusion et d’application. La valeur `LIGHT` reçue pour le joueur et les autres unités alimente ces sources ; ce n’est donc pas seulement une teinte globale choisie au changement de jour.

Le `DayNightCycle` Java ne reproduit pas cette chaîne de fusion, la résolution demi-écran, le traitement RGB par pixel ou la sélection haute/basse qualité native. Une torche, une unité lumineuse ou un objet dont la valeur `LIGHT` change peut donc éclairer une zone différente, sans modifier les pixels voisins avec le même dégradé que le client 1.68.
- Les collisions et le pathfinding Java existent et sont plutôt plus explicites que le simple rendu : cartes de collision, ligne de vue, diagonales et clearance sont gérées. L'écart restant est l'absence de validation serveur/réseau, donc un déplacement local peut être accepté alors que le client 1.68 aurait reçu une position corrigée.

## 19. État actuel de l’audit

Les domaines réseau, paquets, règles serveur, déplacement, combat, inventaire, objets, coffres, boutiques, sorts, ressources, groupes, guildes, chat, mort, PNJ, quêtes, météo, éclairage et collisions ont été examinés. Les différences critiques sont désormais documentées ; les seuls approfondissements restants sont des comparaisons de contenu exhaustives (chaque script/animation/objet) et des tests de scénarios pour quantifier les écarts déjà identifiés.

## 20. Compte, personnages et sélection

- Le client 1.68 prévoit un cycle serveur pour inscription de compte, suppression de personnage, création de personnage, authentification de version, arrivée séraphin et nombre maximal de personnages (`RQ_RegisterAccount`, `RQ_DeletePlayer`, `RQ_CreatePlayer`, `RQ_AuthenticateServerVersion`, `RQ_SeraphArrival`, `RQ_MaxCharactersPerAccountInfo`).
- Le Java utilise `LocalCharacterStore`, `characters.json` et un fichier JSON par personnage. La création, suppression, activation, nom, sexe, statistiques, inventaire initial, or initial et emplacement de départ sont exécutés localement, sans compte, identifiant serveur, validation distante, réservation de nom ou retour d'erreur réseau.
- Le Java limite localement le roster à trois personnages. Cette valeur n'est pas négociée avec un serveur comme le prévoit le paquet de capacité du client original.
- La persistance locale est atomique pour les fichiers JSON, mais elle ne fournit pas les garanties d'un stockage de compte partagé : deux clients peuvent créer le même nom, écraser un état, ou conserver un personnage supprimé dans une autre copie locale.

## 21. Unités distantes et multijoueur visible

- Le protocole client original possède des mises à jour d'unité (`RQ_UnitUpdate`), unités périphériques, mise à jour de groupe et états de membres. Le client doit donc créer, actualiser et retirer des personnages/monstres distants selon les notifications serveur.
- Dans le Java, les classes de personnage repérées concernent le joueur local (`Player`, `PlayerAnimations`, `PlayerHUD`) ; les managers de monstres/NPC gèrent des entités locales issues des spawns, pas un registre de joueurs distants alimenté par paquets.
- Il manque donc les fonctions observables suivantes : apparition/disparition d'un autre joueur, interpolation ou correction de sa position, apparence distante reçue, PV/mana/effets distants, animation d'attaque distante et retrait sur déconnexion. Les groupes Java sont absents, ce qui empêche aussi l'affichage synchronisé des membres.

## 22. Téléportation, fast mode et séraphin

- Le client 1.68 traite la téléportation comme une requête dédiée avec délai/ACK (`RQ_TeleportPlayer`, 750 ms, 5 ACK), et possède également un état fast mode (`RQ_PlayerFastMode`). Le Java téléporte directement le joueur lors d'objets, scripts ou déplacements locaux ; aucune réponse serveur, refus de destination, coût, cooldown ou correction n'est disponible.
- Le Java contient des registres de téléportation et des animations séraphin, mais ce sont des données/effets locaux. Ils ne constituent pas l'équivalent du cycle serveur `RQ_Seraph`, `RQ_SeraphArrival` et des validations de renaissance.
- Une téléportation locale peut donc contourner collision, zone sûre, combat, poids, sort lancé ou restrictions de carte qui étaient validés par le serveur original.

## 23. Sons, musique et animations

- Le client natif charge les sons via une base de données d'identifiants (`DatabaseLoadVSB`, `GameSounds`, `SoundFX`) et possède des sons dédiés pour les contrôles d'interface, objets, combat, zones, donjons et boss. Le Java a un `SoundManager` basé sur des noms de fichiers et couvre plusieurs sons d'interface, de sorts et de monstres, mais ne reproduit pas le routage par identifiant du client natif.
- Le Java joue les sons d'attaque PNJ au démarrage de la pose et les sons de blessure/mort dans les callbacks locaux. Le client 1.68 reçoit l'état de l'unité et orchestre le rendu/sound côté client ; sans unités distantes, le Java ne peut pas jouer les sons des actions des autres joueurs.
- `PlayerAnimations` fixe la durée d'une frame à `0.05f` et termine une attaque en conservant la dernière pose. Le client natif délègue les séquences à ses systèmes de sprites/animations et aux données de ressources. L'identité exacte des durées, du nombre de frames, de la pose finale et du déclenchement sonore n'est donc pas démontrée comme équivalente ; elle doit être validée animation par animation.
- La musique Java est sélectionnée par zones rectangulaires locales (`musicZones`). Le client natif possède en plus une logique de changement de musique par région, donjon et boss (`GameMusic.cpp`) ; aucune preuve ne permet d'affirmer que toutes ces priorités et transitions sont conservées dans les données Java.

## 24. Couverture réelle des commandes de scripts PNJ

La lecture du chemin d'exécution de `NpcScriptEngine` révèle des écarts plus précis que la simple présence des classes PNJ :

- `SendBuyItemList`, `SendTeachSkillList`, `SendTrainSkillList`, `SendTeachFormuleList` et `CreateFormuleList` sont reconnus par des branches vides. Ils ne produisent donc aucun paquet ni aucune ouverture de liste par eux-mêmes ; l'interface Java doit reconstruire l'offre à partir du résultat local.
- `HealPlayer`/`Heal` ne reproduisent pas un soin serveur : le moteur pose un indicateur `heal`, puis la conséquence dépend du code appelant local. Il n'y a pas de validation de distance, coût, cible distante ou correction réseau.
- `CastSpellTarget` et `CastSpellSelf` collectent des identifiants et les exécutent localement. Le serveur original pouvait appliquer résistances, cible, zone, effets persistants, cooldowns et broadcasts à plusieurs unités avant de répondre au client.
- `SUMMON`/`SUMMON2` créent des demandes locales de spawn. Ils ne reproduisent pas la réservation d'unité serveur, la visibilité périphérique, les limites de population, ni la diffusion aux autres clients.
- Le moteur Java ignore par construction toute commande non couverte par ses branches de parsing ; la présence d'un script importé ne prouve donc pas que chaque macro C++ est fonctionnelle. Les scripts portant sur variables serveur, commerce, formules, groupes, guildes, timers ou effets de carte sont particulièrement sensibles à cette perte silencieuse.

## 25. Compétences, statistiques et progression

- Le client original distingue liste de compétences, liste d'entraînement, liste d'achat, utilisation d'une compétence, statut, points de compétences/statistiques, XP, niveau, or, PV, mana et poids (`RQ_GetSkillList`, `RQ_GetTrainSkillList`, `RQ_GetBuySkillList`, `RQ_UseSkill`, `RQ_GetStatus`, `RQ_SkillStatPoints`, `RQ_XPchanged`, `RQ_LevelUp`, `RQ_GoldChange`, `RQ_HPchanged`, `RQ_ManaChanged`, `RQ_UpdateWeight`).
- Le Java possède des écrans et mutations locales : `Statistics` modifie les points de caractéristiques, `TrainScreen` dépense directement l'or et augmente une compétence, et `PlayerProgression` attribue directement XP, niveau, points, PV et mana. Il n'existe ni réponse serveur, ni refus distant, ni transaction d'entraînement, ni diffusion aux autres unités.
- La progression Java donne cinq points de statistiques et quinze points de compétence au niveau supérieur. Ces nombres et les gains aléatoires PV/mana sont des règles Java ; ils n'ont pas encore été démontrés identiques aux règles/configuration du serveur 1.68.
- L'interface Java n'expose que les compétences de combat `attack`, `dodge` et `archery` dans le tableau de statistiques, alors que le protocole original prévoit une liste dynamique de compétences. Les compétences scriptées, actives, passives ou dépendantes d'une liste serveur peuvent donc être absentes de l'interface et du cycle d'utilisation.
- Le client original reçoit les changements de ressources et d'XP comme événements séparés. Le Java modifie l'état puis l'affichage localement ; un rollback, un plafond serveur, une dépense concurrente ou une perte de paquet n'a pas d'équivalent.

## 26. Options client et effets graphiques configurables

- Les options natives comprennent, en plus du volume musique/son et de la luminosité, son de page, qualité d'éclairage, qualité d'effets, eau animée, dithering, alpha de l'interface, animation séraphin, affichage des statuts, texte de barre XP, affichage de l'or et mode 32 FPS (`SaveGame.h`).
- Les préférences Java couvrent volume, luminosité, plein écran, VSync, valeurs HUD, transparence GUI, animation séraphin, texte XP, qualité de police et journalisation. Les options natives eau animée, éclairage haut niveau, effets graphiques, dithering, son de page, affichage de l'or et cadence 32 FPS ne sont pas représentées comme réglages Java équivalents.
- Ce n'est pas seulement une différence d'interface : ces drapeaux modifient le rendu, l'animation et certains retours audio du client 1.68. Leur absence force un comportement Java unique, quelle que soit la configuration historique du joueur.

## 27. Macros, raccourcis, curseurs et ciblage

- Le client natif possède `MacroHandler` : association d'une combinaison `VKey` à un callback, remplacement/suppression d'une macro, activation/désactivation globale et verrouillage de macros pendant certains états. Le Java ne possède pas de gestionnaire de macros utilisateur comparable ; ses raccourcis sont codés directement dans les écrans et handlers.
- Le natif distingue notamment curseur d'attaque, curseur d'attaque à distance et curseur de sort (`CombatCursor.h`), avec sélection de cible et comportement dépendant du mode d'attaque. Le Java possède des curseurs et cibles locales, mais la sélection ne peut viser que ses monstres/NPC chargés localement et ne reçoit aucune cible/unité distante.
- Le client original conserve aussi des mécanismes d'ignore-list et de touches accélératrices dans sa couche macro/localisation. Aucun stockage Java d'une ignore-list joueur ni filtrage des messages par nom n'a été trouvé.
- Les annulations Java (`clearCurrentAttackTarget`, annulation de sort, fermeture d'écran) interrompent des actions locales. Elles ne reproduisent pas les requêtes d'annulation, statuts intermédiaires et ACK du client/serveur 1.68.

## 28. Intégrité et sécurité du transport

- Le client natif calcule des CRC16 dans `CommCenter`, chiffre/déchiffre les paquets (`TFCCrypt::EncryptS/DecryptS`, `EncryptC/DecryptC`) et rejette un paquet lorsque le contrôle ou le déchiffrement échoue. Les paquets sécurisés utilisent également des délais/ACK dédiés.
- Le Java ne contient pas de transport réseau, de CRC, de chiffrement ou d'authentification de paquet. Toutes les mutations locales (or, XP, objets, téléportation, compétences et sorts) sont donc appelables sans la couche d'intégrité du client 1.68.
- Le client natif possède en outre une authentification de version serveur (`RQ_AuthenticateServerVersion`). Le Java n'effectue pas de négociation de version avant de lancer le monde local ; une incompatibilité de données ne peut donc pas être refusée au même moment.
- Cet écart est fonctionnel et sécuritaire : il affecte la validation des messages, la détection de paquets altérés/répétés et la compatibilité entre versions, pas uniquement l'implémentation technique du réseau.

## 29. Mondes, transitions et changement de carte

- Les deux implémentations déclarent quatre mondes de 3072×3072 : monde principal, donjon, caverne et monde souterrain. La correspondance de base des fichiers est donc présente côté Java (`MapDefinition`) et côté client natif (`V2_WorldMap.Map`, `V2_DungeonMap.Map`, `V2_CavernMap.Map`, `V2_Underworld.Map`).
- Le client natif, lors d'un téléport/changement de monde, verrouille le monde, valide les coordonnées dans `[0,3072]`, charge la carte de zone, change la position, puis lance une transition de fondu avant de déverrouiller. Le Java change directement la carte/position dans `MainGameScreen` et ses registres de téléportation ; aucun cycle de verrouillage réseau équivalent n'est présent.
- Le natif applique des traitements d'ambiance dépendant du monde et de la position : `NTime.cpp` force notamment des teintes spécifiques aux cavernes, donjons et à certaines zones du monde souterrain. Le Java utilise `DayNightCycle` avec une ambiance globale, sans équivalence confirmée pour ces teintes par monde/zone.
- Le rendu Java précharge ses quatre cartes et conserve des drops par monde, mais les états dynamiques natifs (objets reçus, unités périphériques, météo, lightmap, fade et musique déclenchés lors de la transition) ne sont pas transférés comme un état atomique de changement de monde.

## 30. Champs de personnage perdus ou simplifiés à la sauvegarde

La structure native `TFCPlayer` contient des champs que `PlayerStateDto` ne possède pas comme données de premier rang :

- charisme et chance (`Cha`, `Lck`) ;
- foi et foi maximale (`Faith`, `MaxFaith`) ;
- classe d'armure et poids/max poids ;
- puissances et résistances élémentaires structurées pour terre/feu/eau/air/ténèbres/lumière ;
- statistiques vraies et bonus séparés (`bStr`, `bEnd`, etc.) ;
- compteurs de morts, kills, séries de meurtres et points PvP ;
- indicateurs d'état serveur comme `CanRunScripts` et `CanSlayUsers`.

Le Java encode certains bonus/résistances dans des flags de quête et possède des calculs d'équipement, mais ce stockage indirect n'est pas équivalent à des champs natifs sérialisés. Il ne garantit ni la conservation complète de la valeur, ni la distinction valeur de base/valeur effective, ni la compatibilité avec un personnage 1.68.

Les buffs Java sauvegardés contiennent principalement nom et durée. Le client/serveur natif synchronise des statuts avec leurs données d'effet, icône, puissance et cible ; après rechargement Java, un effet complexe peut donc être affiché ou recalculé différemment, et les effets sur unités distantes sont de toute façon absents.

## 31. Paquets spécialisés et fonctions rares

- Le protocole natif possède des événements distincts de vol (`RQ_Rob`, `RQ_DispelRob`), de flèche touchée/manquée (`RQ_ArrowHit`, `RQ_ArrowMiss`), de météo (`RQ_WeatherMsg`), d'ouverture d'URL (`RQ_OpenURL`), d'information serveur (`RQ_InfoMessage`) et de mise à jour du drapeau GM (`RQ_GodFlagUpdate`).
- Le Java contient un projectile d'arc et des règles de combat à distance, mais pas de résultat réseau séparé touché/manqué pour une flèche. Le projectile local ne prouve donc pas que la consommation de flèche, le timing du dégât, le message de miss et l'état de la cible suivent le client 1.68.
- Aucun service Java de vol avec synchronisation, annulation de vol, notification de victime ou restauration d'état n'a été trouvé. L'icône `rob` dans l'écran de statistiques n'est pas une implémentation du mécanisme natif.
- Les fonctions météo, URL reçue du serveur, messages d'information et drapeaux GM sont absentes comme traitements Java dédiés. Les éléments séraphins présents dans Java couvrent une animation/aura locale, mais pas la totalité des événements serveur spécialisés.

## 32. Slots d'équipement et apparence

- Le client natif stocke 36 apparences d'objet (`Object[36]`) et définit 16 emplacements historiques : corps, pieds, gants, casque, jambes, anneaux, bracelet, collier, armes droite/gauche, deux mains, ceinture et manches. Le Java expose 23 valeurs `BodyPart`, dont plusieurs slots synthétiques (`BACK`, `HAIR`, `HAT`, `MASK`, `CAPE`, `ROBELEGS`, `BOOT`, `WEAPON2`, `SHIELD`).
- Cette modélisation Java est plus détaillée pour certains équipements, mais elle n'est pas un mapping un-à-un du tableau natif. Les règles de remplacement arme/bouclier, manches/gants, casque/cheveux et robe/jambes peuvent donc produire une apparence différente même quand l'objet logique est identique.
- Java applique des règles de concealment et préserve les suffixes de palette lors d'un override. Le client natif résout les apparences via ses groupes d'objets/palettes et ses tables `Apparence.h`; l'équivalence de chaque groupe et de chaque combinaison de sexe n'est pas démontrée par les seuls tests de présence.
- Le natif manipule aussi apparence, ID d'instance et référence de base séparément dans les objets d'équipement. Le Java stocke principalement une clé d'objet puis dérive le sprite ; deux instances visuellement identiques avec des données différentes ne peuvent pas être distinguées par le rendu local.

## 33. Objets de carte, animations et profondeur

- Le client natif maintient une liste d'objets visuels avec ID, type/apparence, position, direction, luminosité, PV, objet attaché et texte de nom/guilde. Il possède des chemins distincts pour objet normal, ombre, animation, overlay animé et objets 3D (`VisualObjectList.h`).
- Le Java possède bien `ObjectRenderer`, des frames, des sons d'ouverture/fermeture, des flags `behind` et un tri de profondeur. Cette couverture est donc partielle mais réelle, contrairement aux domaines réseau.
- L'écart vérifié restant est l'éclairage dynamique : le natif associe plusieurs lightmaps (`lmPlayerLight`, `lmOtherPlayerLight`, torches/lampes) et met à jour la lumière des objets ; aucun système Java équivalent de fusion de lightmap par joueur/torche n'a été trouvé.
- Le natif possède des tuiles d'eau animée et un lissage des raccords d'eau (`AnimWater01`, `WaterSmooth`, `DrawWaterLevel`). Le Java possède le rendu de terrain et de mosaïques, mais aucun cycle explicite d'eau animée ni réglage Java correspondant à `bAnimatedWater`.
- Les objets Java utilisent des positions/mappings logiques et un état d'animation local. Ils ne reçoivent pas les changements d'objet, déplacements, suppressions ou overlays d'une liste d'objets serveur, de sorte que les états interactifs multi-client restent différents.

## 34. Caméra, zoom et conversion écran-monde

Les dimensions de base sont alignées : le client original initialise des tuiles de 32×16 pixels, une dimension virtuelle de 256×256, des mondes de 3072×3072 et quatre mondes. Cela ne suffit pas à établir une équivalence de caméra.

Le client original possède un état de zoom explicite. `Global::SetZoomStatus` le limite entre 0 et 14, et l'option `bEnableZoom` permet de le désactiver. Chaque niveau modifie de 5 % les dimensions d'écran effectives. La conversion souris-vers-monde compense ce zoom, et les noms/textes de dialogue reçoivent aussi des offsets liés au zoom.

Le client Java ne possède ni état de zoom correspondant, ni préférence de zoom, ni équivalent de `SetZoomStatus`. Il utilise une caméra orthographique libGDX, un `ScreenViewport`, une conversion `unproject` et un alignement sur les pixels. Les comportements divergent donc lorsque le client 1.68 est zoomé ou lorsque la résolution change :

- nombre de tuiles visibles et limite de culling ;
- cellule du monde sélectionnée par un clic ;
- ciblage près des bords de l'écran ;
- position des noms, labels et dialogues ;
- relation entre coordonnées HUD et coordonnées monde.

La fonction native `ScreenPosToWL` utilise en outre des décalages asymétriques autour du centre de l'écran, avec les bases 32 pixels horizontalement et 16 verticalement. Java délègue à `camera.unproject`, qui suit un autre algorithme de projection. Même au zoom 0, l'équivalence doit être vérifiée par un test pixel par pixel pour chaque résolution supportée ; les dimensions de tuile identiques ne suffisent pas.

## 35. Modèle d'entrée souris/clavier et états d'interface

Le client natif ne traite pas seulement des touches instantanées. `MouseAction.cpp` transforme explicitement les événements en `DRAG`, `DROP`, `CLICK`, `DOUBLE_CLICK` et `DOWN`, puis les distribue aux contrôles via des messages distincts. Cette distinction est utilisée par les interfaces d'inventaire, coffre, échange, macro et sélection.

Le Java possède des écrans GUI et des contrôles libGDX, mais `GameInputHandler` ne gère directement que le déplacement clavier, quelques raccourcis globaux et des toggles de debug/carte. Aucun équivalent central natif de la séquence `DOWN → DRAG → DROP`, de la double activation et de la capture souris pendant un drag n'est présent dans ce handler. Les écrans Java peuvent donc répondre à un clic local tout en divergeant sur les cas suivants : déplacement d'un objet sans relâcher sur une zone valide, double-clic d'utilisation, clic droit contextuel, annulation d'un drag hors fenêtre et priorité entre GUI et monde.

Le clavier natif maintient également un état DirectInput de 256 touches et produit des événements de relâchement pour les lettres, chiffres, Entrée, Échap, Retour arrière, espace, plus et moins. Le Java interroge principalement `isKeyPressed`/`isKeyJustPressed` et réserve la saisie texte aux widgets. La répétition, le moment de déclenchement et la consommation d'une touche par un écran ne sont donc pas garantis identiques, en particulier pour les macros et les actions maintenues.

Enfin, le client natif restaure le zoom après certains traitements souris (`GetlastScrollStatus` puis `SetZoomStatus`). Le Java n'a pas cette restauration d'état, ce qui renforce l'écart entre navigation, interaction d'interface et coordonnées monde.

## 36. Déplacement, chemin et autorité de collision

Le client natif représente le déplacement par huit requêtes discrètes (`RQ_MoveNorth` à `RQ_MoveNorthWest`). Après un clic, `MovePl` appelle `Player::ScreenPosToWL`, construit une suite de mouvements avec `pfSetPosition`/`pfGetNextMovement`, puis envoie le prochain déplacement. Ces requêtes ont un ACK configuré à zéro délai : le client ne simule donc pas une validation serveur équivalente à une animation locale continue ; il avance selon les réponses/états reçus.

Le Java suit une autre architecture : `PlayerMovement.move` consomme un budget de pixels par frame (`PLAYER_SPEED × delta`), réserve localement une case, interpole la position et applique directement la collision. En cas de blocage, il tente automatiquement des directions adjacentes pour glisser autour de l'obstacle.

Les différences fonctionnelles vérifiées sont donc :

- le Java peut déplacer le personnage à une position intermédiaire entre deux cases, alors que le protocole natif raisonne en pas directionnels transmis ;
- le Java décide localement si une case et l'empreinte du joueur sont franchissables, tandis que le client natif transmet l'intention et reçoit la position autoritative ;
- le Java peut choisir une direction latérale de repli non demandée par le joueur à cause de `tryReserveAdjacentDirection` ; ce glissement automatique n'est pas démontré dans `MovePl` natif ;
- la vitesse effective Java dépend du delta de frame et de multiplicateurs locaux, alors que le rythme historique dépend de la cadence des requêtes, des réponses et des règles serveur ;
- un désaccord de collision, un déplacement interdit ou une correction de position n'a pas d'équivalent réseau dans le Java local.

Même si les deux clients utilisent une grille 32×16 et huit directions, leurs décisions de déplacement ne sont donc pas interchangeables. Une comparaison complète doit tester au minimum : diagonale contre angle bloqué, bord de carte, obstacle sur l'empreinte haute du personnage, destination téléportable, maintien d'une touche, clic maintenu et correction de position.

## 37. Ciblage, portée et ligne de vue

Le client natif maintient une cible par identifiant d'unité (`TargetID`, `FollowID`, `FreezeID`) à partir de la grille d'objets `GridID`. Le clic d'attaque peut sélectionner, suivre ou verrouiller une unité ; le double-clic active le verrouillage si l'option `bLockTarget` est activée. Les interfaces envoient ensuite l'identifiant de la cible au serveur, après vérification que l'objet existe dans la liste des unités reçues.

Le Java sélectionne principalement des instances locales de `BaseMonster`/`BaseNPC` et peut choisir le monstre le plus proche. Il ne possède pas de table d'identifiants réseau comparable pour les joueurs et unités périphériques. Le verrouillage Java est donc un état de référence locale, pas le même contrat que `TargetID`/`FreezeID` synchronisé.

La validation est également placée à un autre endroit :

- le natif affiche le curseur et transmet la cible/position ; la validation effective de portée, ligne de vue, état vivant, droit d'attaque et correction de position relève du serveur 1.68 ;
- le Java refuse localement certaines actions via `hasLineOfSight`, la distance et les collisions avant d'appliquer le résultat ; aucune réponse serveur ne peut confirmer ou contredire ce choix.

Pour les sorts positionnels, Java calcule la distance avec `distance / max(GRID_W, GRID_H)` et échantillonne la ligne en pas de collision. Ce calcul ne reproduit pas automatiquement une portée native éventuelle exprimée en cases, ni les règles serveur pour une cible occupant plusieurs cases. Une différence est particulièrement probable sur les diagonales, les coins de murs, les cibles proches de la limite de portée et les sorts de zone : Java peut exclure une cible avant impact alors que le client natif aurait envoyé la requête, ou l'inverse.

Le natif distingue aussi attaque normale, attaque à distance, attaque magique et suivi par des curseurs/états séparés. Le Java partage davantage le même pipeline de sélection et d'application locale ; les transitions attaque → sort → suivi, l'annulation par clic droit et le verrouillage de cible ne disposent donc pas du même automate d'état.

## 38. États temporaires, buffs et dissipation

Le protocole natif possède deux événements dédiés : `RQ_CreateEffectStatus` (83) et `RQ_DispellEffectStatus` (84). La création transporte un identifiant numérique d'effet, le temps restant, le temps total, l'identifiant d'icône et une description. La dissipation retire l'effet par son identifiant numérique, indépendamment du nom du sort.

Le Java applique les buffs directement après le lancement local d'un sort. `Player.ActiveBuff` les indexe principalement par nom de sort et conserve description, icône, expiration et une liste d'effets Java. Il n'existe pas d'identifiant d'effet natif reçu du serveur, ni de paquet séparé de création/mise à jour/dissipation. Les conséquences sont :

- deux effets provenant d'un même nom ou deux instances d'un même sort ne peuvent pas être distingués comme dans le protocole natif ;
- un renouvellement Java remplace/recalcule le buff local, alors que le client natif peut recevoir une nouvelle durée et un nouvel identifiant d'effet ;
- la durée affichée et la durée effective Java démarrent au moment du cast local, pas à la réception de l'événement serveur ;
- la dissipation par nom Java ne garantit pas la suppression du bon effet lorsque plusieurs effets sont actifs ;
- les effets persistants sur unités distantes, leur icône et leur expiration ne sont pas synchronisés.

Le Java ajoute des ticks locaux de régénération HP/mana et des règles locales pour invisibilité, détection, stun et effets périodiques. Le client 1.68, lui, affiche l'état transmis et reçoit les changements d'effet du serveur ; il ne peut pas déduire localement qu'un tick a réellement été accepté. Un désaccord de durée, de renouvellement, de dissipation ou de tick produit donc un état Java différent même si l'animation de sort est identique.

## 39. Musique, ambiance et sons d'interaction

Le client natif ne choisit pas la musique uniquement à partir d'un fichier de zones externe. `GameMusic::LoadNewSound` commence par le monde courant, le niveau et l'état extérieur, puis applique une longue série de régions géométriques (rectangles et zones diagonales) pour sélectionner boss, extérieur, forêt, donjon, caverne, tristesse, silence ou bruits. Les priorités sont déterminées par l'ordre des tests natifs et certains changements sont déclenchés lors d'un changement de monde.

Le Java choisit la dernière `MusicZoneEntry` contenant la case du joueur, recharge les zones JSON/binaire, puis appelle `SoundManager.playAmbient`. Il n'a pas démontré la reprise de toutes les régions codées dans `GameMusic.cpp`, ni la variable `OutSide` dépendant du niveau, ni les priorités exactes entre régions superposées. Une carte qui ne possède pas de fichier Java de zones peut donc jouer aucune musique ou une musique différente du client 1.68.

Les deux systèmes diffèrent aussi dans la gestion de lecture :

- le natif conserve une piste courante, la remplace via un gestionnaire protégé par section critique et peut utiliser musique streaming ou CD (`bUseCD`) ; il arrête/libère la piste pendant une transition ;
- Java arrête et détruit directement `ambientMusic`, recrée un `Music` libGDX et le boucle ; il ne gère pas le mode CD, le fondu de transition, la file de lecture ou la priorité native ;
- le natif possède des sons liés aux sprites/objets et à l'interface (`GameSounds`, `ItemDragSounds`) avec des identifiants de ressources ; Java résout surtout un nom de fichier puis joue un `Sound` ou un `Music` selon ce qui est disponible ;
- le natif sépare son musique, sons et contrôles de volume dans plusieurs gestionnaires/threads, tandis que Java utilise une résolution locale et ignore silencieusement les erreurs de chargement.

Ces écarts changent le résultat audible : transitions sans coupure ou avec coupure, piste choisie dans une zone de boss/ville, répétition d'un son d'interface, volume après modification des options et réaction lorsqu'un fichier manque.

## 40. Localisation et catalogue de textes

Le client natif possède trois catalogues distincts (`LocalString`, `GUILocalString` et `GUIDELocalString`) chargés depuis la langue sélectionnée. La langue est un choix explicite parmi English, French, Italian, Portugal, Spanish, German et Korean ; elle est stockée dans `Player.szLanguage` et peut être rechargée à chaud lorsque le launcher signale un changement. Les textes sont adressés par index numérique stable (`g_LocalString[xxx]`) et les formats natifs utilisent `sprintf`/`FORMAT` avec les paramètres du message.

Le Java utilise un unique catalogue JSON `assets/i18n/lang.json`, des clés textuelles et un fallback sur la clé ou la valeur fournie. `I18n.reload` échoue si le fichier manque ou est vide, et aucune préférence Java correspondant aux sept langues natives ni aucun rechargement déclenché par une langue serveur n'a été trouvé.

Cette différence affecte le fonctionnement visible :

- le client natif peut recharger les textes de jeu, d'interface et d'aide séparément après réception de la langue ; Java ne recharge qu'une carte JSON commune ;
- les scripts et messages importés qui référencent un index natif (`INTL(id, texte)`) ne correspondent pas automatiquement à une clé Java ;
- le formatage des paramètres et les règles de longueur/encodage ne sont pas les mêmes ;
- le fallback Java affiche parfois une clé technique (`ui.xxx`, `message.xxx`) alors que le natif dispose d'un texte indexé ;
- les textes envoyés par serveur restent des chaînes déjà formées dans Java, alors que le client natif peut les afficher dans le catalogue correspondant et construire certains messages localement.

Le Java a donc une infrastructure de traduction réelle, mais elle ne garantit ni la couverture des catalogues 1.68, ni l'identité des langues disponibles, ni la compatibilité avec les références `INTL` des scripts PNJ.

## 41. Création, relance et sélection de personnage

Le client natif traite la création, la suppression et la relance comme des opérations serveur : `RQ_CreatePlayer` (25), `RQ_DeletePlayer` (15) et `RQ_Reroll` (31) ont chacun une file d'ACK et un nombre maximal de tentatives. Le nombre de personnages autorisé est reçu via `RQ_MaxCharacters` (103), donc il n'est pas une constante purement client.

Le Java exécute ces opérations dans `CharacterSelectionScreen` et `LocalCharacterStore`, avec un maximum fixé à `MAX_CHARACTERS = 3`, des fichiers JSON locaux et un UUID local par personnage. La création écrit immédiatement l'état et le roster ; la suppression efface le fichier local ; la relance modifie les valeurs de la session locale. Aucun refus serveur, verrouillage de compte, conflit simultané, ACK, quota reçu ou rollback distant n'existe.

Le Java ajoute en outre une création par questionnaire d'affinités, un sexe et une table de valeurs initiales (`starting gold`, objets, potions, torches et compétences). Le client natif affiche les valeurs reçues par le serveur après `RQ_Reroll`/création ; ces règles initiales Java ne sont pas prouvées identiques aux règles 1.68 et peuvent créer un personnage avec un inventaire, un or ou des statistiques différents.

Les contraintes de nom et de roster divergent également : Java vérifie localement l'unicité dans son fichier JSON et normalise le nom avant écriture, alors que le client natif transmet le nom au serveur et affiche l'erreur renvoyée. Une seconde instance Java, un changement de compte ou une suppression externe ne peut pas être arbitré par le roster local.

## 42. Carte du monde, mini-carte et changement de zone affichée

Le client natif charge `Zone_Map.dat` pour le monde courant, maintient une fenêtre autour de la position du joueur et calcule la zone courante par la valeur de la cellule de cette carte 3072×3072. `ValidMapZonePosition` déclenche un changement de zone affiché lorsque la cellule change ; `GetDisplayZoneName` récupère ensuite le nom correspondant à la table `m_ZoneInfo[monde][zone]`. Le changement de monde peut aussi remplacer la bitmap de carte et réinitialiser la zone.

Le Java possède une carte du monde réelle (`GuiWorldMap`/`OriginalRtMap`) et un marqueur joueur, mais la mise à jour de la zone affichée n'est pas reliée à une table native `Zone_Map.dat` équivalente. `MainGameScreen` initialise notamment l'affichage avec `zone.lighthaven`; aucune routine Java comparable à `ValidMapZonePosition`/`ForceDisplayZone` n'a été trouvée pour recalculer automatiquement les noms des zones natives à chaque cellule.

Les conséquences fonctionnelles sont :

- le fond de carte peut être correct tandis que le nom de zone, son apparition temporaire et son déclenchement diffèrent ;
- la carte native distingue carte de monde, carte de donjon/caverne et fenêtre de vue chargée autour du joueur ; Java reconstruit la vue via `OriginalRtMap` et garde un marqueur positionné dans un cadre fixe ;
- les zones non déclarées dans la table Java ne peuvent pas produire le même nom ou le même événement d'entrée de zone ;
- `ForceDisplayZone` natif permet de forcer une zone après téléportation ou transition, sans équivalent confirmé dans le cycle Java.

## 43. Vie de session, keep-alive et AFK

Le client natif maintient un état de vie de la couche réseau (`PacketCenter::KeepAlive`, `isAlive`, `isHalf`, `SetAlive`, `LongLive`). Il distingue un état réseau normal d'un état dégradé après plusieurs secondes sans activité et considère le client perdu après un délai de 120 secondes. Le `CommCenter` possède en parallèle un timeout de backlog, des paquets en attente et des retransmissions ACK.

La configuration native conserve aussi un statut AFK et un message AFK (`dwAfkStatus`, `strAfkMessage`) avec l'adresse de compte et les paramètres de session. Le client peut donc afficher ou transmettre un état d'absence distinct de la simple absence de mouvement.

Le Java n'a pas de transport réseau ni de couche de session exposant un keep-alive, un état moitié-vivant, une retransmission ou une déconnexion après timeout. Il n'existe pas non plus de statut AFK persistant avec message associé dans `GamePreferences`, `PlayerStateDto` ou `GameChat`. Une fenêtre Java ouverte, une pause de rendu ou un joueur immobile restent donc des états locaux sans conséquence de session comparable au client 1.68.

Cet écart se manifeste lors d'une perte réseau, d'un serveur silencieux, d'une reprise après latence, d'une fermeture de fenêtre ou d'une inactivité prolongée : le natif peut afficher un état dégradé puis fermer la session, alors que Java conserve le monde et les mutations locales.

## 44. Cadence d'animation et timing des sprites

Le client natif centralise l'animation dans ses sprites V2 et son rythme d'affichage. Il possède une option `b32FPS` dans `SaveGame`, des compteurs globaux d'animation d'eau (`GetAnimWaterFrame`/`StepAnimWaterFrame`) et des ressources VSB/VSF décrivant les frames et leurs métadonnées. Les changements de cadence et les états d'animation sont donc liés au timer du client et aux ressources chargées.

Le Java fixe des durées dans le code : `PlayerAnimations.FRAME_DURATION = 0.05f` et `NPCAnimations.FRAME_DURATION = 0.10f`, puis fait avancer les frames avec le `delta` libGDX. Il n'a pas de réglage 32 FPS correspondant, ni de cadence extraite des métadonnées d'animation natives. Les animations de joueur et de PNJ peuvent donc être deux fois plus rapides/lentes selon la cadence historique de la ressource, et leur vitesse varie avec le delta de frame.

Les états diffèrent aussi : Java remet l'animation de marche à la frame 0 lorsqu'il n'y a plus de mouvement, conserve une pose finale d'attaque et active les animations PNJ statiques selon `standingIdle`. Le natif sépare les sprites d'attente, marche, attaque, distance et effets via ses objets/animations V2. Sans table de correspondance frame par frame, l'apparence de l'attaque, la durée de la pose finale, le flip et le moment où le son se déclenche ne sont pas garantis identiques.

## 45. Formats de ressources, palettes et comportement en cas d'absence

Le client natif charge les ressources depuis des bases indexées et empaquetées (`T4CGameFile.vsb`, `VSBDataBase`, `CV2Sprite`/VSF), avec des références par identifiant, palette et chunk. `GameIcons` conserve des maps d'objets et de sons et retourne toujours un sprite/son de secours (`???` ou ressource d'erreur) lorsqu'un identifiant n'est pas trouvé. Le rendu reste donc généralement dans la boucle de jeu même si une ressource est absente.

Le Java charge principalement des textures PNG et métadonnées via `SpriteLoader`, avec des caches de `TextureRegion`, des générations de texture et des caches de chunks/tiles composés. Les palettes et variantes sont résolues par des fichiers de mapping Java, et les ressources manquantes passent par des renderers de tuile manquante, des régions nulles ou des exceptions selon l'écran.

Les différences fonctionnelles sont les suivantes :

- un même identifiant natif n'implique pas le même résultat si le mapping palette Java est absent ou incomplet ;
- le natif peut fournir un sprite d'erreur non nul, alors qu'un écran Java peut masquer l'élément, dessiner une tuile de remplacement ou interrompre le chargement ;
- le natif libère/recharge ses ressources empaquetées par index et référence, Java invalide ses textures/caches et reconstruit des fichiers PNG ;
- les erreurs de ressource n'ont pas le même effet sur la liste d'objets, les frames d'animation, les offsets et les sons associés ;
- une modification de palette ou d'offset n'est donc pas nécessairement visible au même moment et ne provoque pas le même invalidation de cache.

Les deux clients possèdent un mécanisme de cache, mais leurs contrats de résolution et de secours sont différents : la présence d'un fichier PNG Java ne prouve pas que l'identifiant, la palette, la frame et le fallback correspondent à la ressource V2 1.68.

## 46. Focus, pause, redimensionnement et restauration graphique

Le client natif traite explicitement les transitions de fenêtre (`WM_ACTIVATE`, `WM_KILLFOCUS`, `WM_SETFOCUS`). À la sortie, il désacquiert le clavier et la souris DirectInput ; au retour, il réacquiert les périphériques, rétablit le focus logique et restaure les surfaces DirectDraw avant de reprendre l'affichage. Il possède aussi l'option `bLockResize`, qui bloque les redimensionnements de la fenêtre pendant le jeu.

Le Java implémente bien `resize()` et `resume()` via libGDX : les viewports/caméras sont recalculés et le HUD appelle `recoverAfterDisplayChange()`. En revanche, `pause()` est vide dans `MainGameScreen` et aucune logique de jeu équivalente à l'acquisition/libération des périphériques, au verrouillage de redimensionnement ou à la restauration explicite des surfaces/palettes natives n'a été trouvée.

Les conséquences divergent lors d'un Alt-Tab, d'une minimisation, d'une perte de contexte graphique ou d'un redimensionnement pendant une action : le backend Java peut restaurer le contexte OpenGL, mais le jeu ne reproduit pas les états focus/pause/reprise ni les transitions de périphériques du client 1.68. Le mouvement, les entrées maintenues, l'audio et les timers peuvent donc continuer ou reprendre à un moment différent.

## 47. Capture d'écran intégrée aux macros

Le client natif expose une action de capture d'écran (`MacroHandler::TakeScreenShot`) et appelle `VideoCapture::TakeDesktopSnapshot`, y compris depuis la boucle réseau/jeu. La capture est donc une fonction utilisateur intégrée au client et à ses macros, avec un état de demande consommé par la boucle de rendu.

Aucun équivalent de capture d'écran ou d'action macro correspondante n'a été trouvé dans le Java. Les touches et actions disponibles ne peuvent donc pas reproduire ce comportement 1.68 ; une capture externe du système ne constitue pas la même fonction intégrée ni le même moment de prise d'image.

## 48. Panneaux sociaux et opérations joueur-à-joueur

L’interface native contient des panneaux dédiés et distincts pour `GroupPlayUI`, `GuildUI`, `TradeUI` (avec `BuyUI`/`SellUI`), `RobUI`, `ChatterUI`, `MacroUI`, `EffectStatusUI` et `ChestUI`. Ces panneaux sont alimentés par les réponses réseau et maintiennent des états d’attente, de sélection et de confirmation propres à chaque opération.

Le Java possède des écrans d’inventaire, de boutique, de coffre et de sorts, mais aucun écran ou service équivalent pour le groupe, la guilde, l’échange joueur-à-joueur, le vol, les macros utilisateur ou la gestion complète des effets réseau. Les occurrences de `guild`, `group`, `trade` ou `rob` restantes correspondent principalement à des données de PNJ, des compétences ou des icônes ; elles ne constituent pas ces workflows interactifs.

Même lorsqu’une compétence ou une donnée d’apparence existe côté Java, il manque donc le cycle fonctionnel complet : invitation/demande, réponse distante, verrouillage des sélections, validation des deux parties, annulation, mise à jour des participants et fermeture synchronisée. L’interface Java ne peut pas reproduire les états intermédiaires du client 1.68.

## 49. Vérification de version et mise à jour avant connexion

Le client natif possède un flux de lancement distinct qui lit la version, envoie `RQ_AuthenticateServerVersion` (paquet 99), traite les réponses d’authentification et appelle `WebPatchUpdate` avec l’IP, le compte, le mot de passe et la version du client. Le démarrage peut donc être interrompu ou orienté vers une mise à jour avant l’accès au monde.

Le Java ne présente pas de launcher/patcher ni de négociation de version réseau comparable. `CharacterSelectionScreen` et `LocalCharacterStore` travaillent sur des personnages locaux et ne vérifient pas une version serveur avant l’entrée en jeu. Un client Java ancien, modifié ou incompatible peut donc atteindre les écrans de jeu sans le contrôle préalable imposé par le flux natif 1.68.

## 50. Données de profil client persistées

`CSaveGame` natif ne sauvegarde pas seulement les options : il conserve, par compte et personnage, l’inventaire d’interface, trois familles de macros (objets, sorts, compétences), les canaux de discussion avec mot de passe/couleur/activation, la liste d’ignorés, les coffres mémorisés et une carte RTMap de 10 couches en 192×192 cases. Ces données sont rechargées avec le profil avant le jeu.

`GamePreferences` Java ne persiste que quelques volumes, options graphiques et journaux. `LocalCharacterStore`/`PlayerStateStore` persistent l’état local du personnage, mais aucun modèle équivalent complet pour macros par type, canaux privés, liste d’ignorés, coffres mémorisés ou cases RTMap n’a été trouvé. Après changement de personnage, redémarrage ou changement de compte, les raccourcis, canaux, filtres sociaux et révélations de carte ne suivent donc pas les mêmes règles que dans le client 1.68.

## 51. Raccourcis globaux et macros clavier

Le client natif installe explicitement des raccourcis Ctrl configurables pour l’inventaire (`I`), statistiques/personnage (`S`), mode d’attaque (`C`), chat (`L`), groupe (`G`), sorts (`P`), macros (`M`), options (`O`), carte (`W`), échange (`T`), capture d’écran (`H`), taille du chat (`A`) et identification des objets (`V`). Les touches peuvent être remplacées dans le profil et sont exécutées par `Custom.gMacro`/`MacroUI`.

Le Java code en dur un sous-ensemble différent : Ctrl+T ouvre les statistiques, Ctrl+P les sorts, Ctrl+I l’inventaire, Ctrl+Q les quêtes et Ctrl+W la carte ; F1/F2/F3/R pilotent en plus des outils locaux. Il n’existe pas de résolution générique des macros natives ni de correspondance pour plusieurs actions (`C`, `L`, `G`, `M`, `O`, `H`, `A`, `V`). Les mêmes combinaisons de touches déclenchent donc des fonctions différentes, et les raccourcis personnalisés du client 1.68 ne sont pas portables dans Java.

## 52. Touches d’accès extraites des traductions

Les catalogues natifs `GUILocalString` et `GUIDELocalString` analysent les chaînes d’interface, extraient une lettre précédée d’un underscore comme touche d’accès et l’exposent par `GetHotKey`. La touche peut donc être localisée avec le texte et changer selon la langue sans modifier le code de la fenêtre.

Le catalogue Java `I18n` résout les textes et leurs paramètres, mais aucune extraction ou utilisation de touche d’accès/mnemonic n’a été trouvée. Les commandes Java restent liées aux touches codées dans les handlers ; une traduction ne peut donc pas déplacer automatiquement le raccourci associé à un bouton, et les versions linguistiques ne reproduisent pas le comportement interactif des menus natifs.

## 55. Statistiques XP/heure et statistiques PvP affichées dans le chat

Le client natif contient deux outils d’observation dédiés : `XpStat` mémorise le début d’une mesure, calcule l’XP gagnée, le temps écoulé et la vitesse moyenne en XP/heure ; `PvpRanking` affiche les points PvP, kills/morts courants et totaux, ainsi que les séries de meurtres. Les résultats sont injectés dans le backscroll de `ChatterUI` et restent consultables comme messages système.

Aucun équivalent Java de `XpStat` ou `PvpRanking` n’a été trouvé. Le Java conserve ou affiche certaines valeurs de progression et de combat, mais ne propose pas ces mesures démarrables, leur calcul temporel XP/heure, ni le rapport PvP complet dans l’historique du chat. Le suivi de progression et la consultation des statistiques ne fonctionnent donc pas comme dans le client 1.68.

## 56. Durée et gestion des textes au-dessus des personnages

`TFCObject` natif conserve séparément le texte de parole et le nom : le texte est centré, limité par `MaxOverheadLines` et peut recevoir un offset de lignes. La purge temporisée est différenciée : `ChkText` expire le texte du joueur principal après 10 secondes, mais laisse celui des unités distantes jusqu’à 25 secondes ; un `StopTalkText` explicite peut toutefois le supprimer avant. Le nom dispose de son propre état `DisplayName`/`StopNameDisplay`.

Le Java impose une expiration de 10 secondes pour le texte de parole du joueur (`talkTextExpiresAt`) et une durée paramétrée pour les noms (`showNameFor`), sans distinction Java équivalente de 25 secondes pour les unités distantes. Le nombre de lignes, la découpe, les offsets et le moment de disparition ne suivent donc pas le cycle natif ; un message provenant d’une autre unité peut disparaître trop tôt ou se superposer différemment.

## 57. Aide intégrée, lettres et pages graphiques

`RTHelp` natif est une vraie fenêtre paginée : 12 pages d’aide, 2 pages de lettres, 4 pages de carte du monde, une page de labyrinthe et une image spéciale. Les pages sont des ressources graphiques localisées selon `Player.szLanguage`, avec boutons précédent/suivant, fermeture et modes d’affichage distincts (aide, lettres, cartes). Le raccourci Ctrl+W ouvre également ce système dans le client natif.

Le Java contient une carte du monde et des textes d’aide ponctuels dans le chat, mais aucun écran paginé équivalent à `RTHelp`, aucun ensemble de pages graphiques localisées ni navigation aide/lettres/labyrinthe. L’utilisateur Java ne dispose donc pas du même contenu d’assistance ni du même parcours d’apprentissage intégré.

## 58. Enregistrement vidéo du jeu

En plus de la capture ponctuelle d’écran, le natif possède `NMVideoCapture` avec `StartCapture`, `CaptureFrame` et `StopCapture`. Les callbacks Ctrl+B/Ctrl+N sont prévus pour démarrer et arrêter l’enregistrement, tandis que la boucle de rendu capture les frames et écrit un flux vidéo ; des messages de début et de fin sont ajoutés au chat système.

Le Java ne contient pas de composant d’enregistrement vidéo ni de capture de frames pilotable par macro. Les outils de profilage/JFR et la capture externe éventuelle ne produisent pas la vidéo du framebuffer du jeu ni les mêmes messages et transitions d’état que le client natif.

## 59. Formatage et retour à la ligne des textes

Le moteur natif `FormatText` ne fait pas qu’afficher une chaîne : il mesure chaque mot avec la police active, découpe selon une largeur en pixels, ajoute une indentation configurable aux lignes suivantes et interprète le marqueur système `<>` comme une rupture forcée du formatage. Le nombre de lignes et les offsets sont ensuite utilisés par les widgets et les bulles de dialogue.

Le Java utilise plusieurs chemins libGDX (`BitmapFont`, `GlyphLayout`, `NameRenderer`) avec des retours à la ligne et limites propres à chaque écran. Aucun traitement global du marqueur natif `<>`, de son indentation et de ses règles de découpe par police n’a été trouvé. Une même phrase longue, un dialogue formaté ou une bulle au-dessus d’un personnage peut donc être coupée sur des mots différents et occuper une hauteur différente.

## 53. Options visuelles et d’interface sans équivalent Java

Les options natives pilotent des comportements précis : `bShowItemSpec` affiche les caractéristiques au survol, `bLockTarget` modifie le double-clic de ciblage, `bHighFont` sélectionne une police de taille supérieure, `bOldStatBar` choisit l’ancien ou le nouveau format de barre de statistiques, `bShowNewLife` active les éléments de vie modernes, `bShowNewOmbrage` change l’ombrage, `bShowAnimDecorsLight` active la lumière des décors animés, `bShowWeatherEffects` filtre pluie/neige/brouillard, `bDisplayMacroFullScreen` agrandit la macro-interface et `bEnableDisplayGold` contrôle l’affichage de l’or.

`GamePreferences` Java ne contient pas ces commutateurs séparés. Il propose notamment `showHudValues`, `transparentGui`, `seraphAnimation` et `xpBarText`, mais ces préférences ne correspondent pas aux mêmes branches natives et aucun réglage Java ne permet de sélectionner les variantes de barre, police, ombrage, lumière des décors, météo, survol d’objet ou macro plein écran. Le rendu et les informations visibles restent donc imposés par le code Java, même quand le client 1.68 permettait à l’utilisateur de les désactiver.

## 54. Routage des événements souris et double-clic

Le natif distingue explicitement `DM_DOWN`, `DM_CLICK`, `DM_DOUBLE_CLICK`, `DM_DRAG`, `DM_DROP` et les clics du bouton droit. `MouseAction.cpp` route ces états vers l’interface, le dialogue, l’utilisation d’objet, le ramassage, le combat ou le déplacement ; tant qu’une interface bloque le mouvement, les actions de déplacement sont suspendues. Le double-clic peut en outre verrouiller la cible lorsque `bLockTarget` est actif.

Le Java possède des traitements `touchDown`/`touchUp` et certains glisser-déposer pour l’inventaire et la barre rapide, mais les handlers de monde n’exposent pas une machine d’états générale équivalente au double-clic natif. Le ciblage, le ramassage et le déplacement sont dispatchés par des handlers séparés et ne partagent pas le même verrouillage optionnel. Un double-clic ou un glisser sur une cible peut donc produire un déplacement, une attaque ou une interaction différente selon l’ordre des processors libGDX, alors que le client 1.68 centralise cette décision dans `MouseAction`.

## 60. Sélection de sort et capture de la prochaine cible

Dans `SpellUI` natif, sélectionner un sort ne lance pas seulement une action immédiate : pour un sort nécessitant une cible, le client installe une capture explicite du prochain événement souris (`LockNextEvent(DM_CLICK, ...)`). Le clic suivant est alors consommé par le handler de cible (position ou unité), au lieu d’être interprété comme un déplacement ou une interaction générique. Le double-clic sur un sort de la liste appelle directement `CastSpell`, et la page affiche simultanément l’icône, le coût de mana transmis avec le sort et l’état de sélection, avec un son de sélection.

Le `SpellBook` Java implémente surtout la sélection visuelle, le changement de page et le glisser-déposer vers les quick-slots ; son `onTouchDown` mémorise un sort à faire glisser et son `onTouchUp` l’affecte à un slot. Aucun état commun équivalent à « prochain clic capturé pour cibler le sort » ni double-clic de la liste lançant directement le sort n’a été trouvé. Les sorts Java sont déclenchés par les raccourcis/handlers de `MainGameScreen`, qui choisissent déjà la cible (soi, unité hostile ou position) avant l’appel à `SpellCastingService`. Cela change le parcours utilisateur et peut laisser un clic de ciblage agir comme déplacement ou autre interaction dans les cas où le client 1.68 attendait obligatoirement la cible du sort.

Les macros de sorts natives persistent également une structure dédiée (icône, identifiant, touche, position rapide et nom), chargée par `ClientInitialize`. Les quick-slots Java persistent une association de nom de sort, mais pas ce modèle de macro complet ni son comportement de lancement/capture associé.

## 61. Sélection multiple dans les fenêtres d’apprentissage et de commerce

Les fenêtres natives `BuyUI`, `SellUI`, `SkillTeachUI` et `SkillTrainUI` ont un état de panier par ligne : le clic sur les contrôles recalcule immédiatement le total, l’or restant et la quantité, puis le bouton d’action sérialise la liste complète dans une requête (`RQ_SendBuyItemList`, `RQ_SendSellItemList`, `RQ_SendTeachSkillList` ou `RQ_SendTrainSkillList`). Le double-clic sur une ligne est un raccourci fonctionnel : il réutilise le clic de quantité et peut envoyer directement la sélection selon le contexte. Les vérifications de capacité, de prix et d’or sont donc visibles avant l’envoi, mais la validation finale appartient au serveur.

`LearnScreen` Java possède bien un panier, mais son interaction passe par des boutons +/- génériques ajoutés par `GuiListScreen`; aucun double-clic de ligne équivalent aux handlers natifs n’a été trouvé. Surtout, `learnBasket()` déduit directement l’or et les points, modifie les niveaux/flags et marque les sorts appris localement, au lieu de sérialiser une requête native de liste et d’attendre les mises à jour serveur correspondantes. Même lorsque le résultat nominal est identique, une sélection multiple, un double-clic, une désynchronisation ou un refus serveur ne suit donc pas le même comportement que le client 1.68.

## 62. Interaction avec les effets actifs

`EffectStatusUI` natif ne fait pas qu’afficher des icônes : il trie les effets, limite la vue à sept emplacements, ajoute un défilement, supprime les effets expirés à chaque recalcul et affiche dans l’aide le temps restant au format heures/minutes/secondes. Un double-clic sur un effet tente de relancer le sort correspondant ; si ce sort ne peut pas être relancé, le client recherche aussi un objet du sac dont le nom est extrait de la description de l’effet et envoie `RQ_UseObject`. En cas d’échec, il écrit un message système dans le chat.

Le Java affiche tous les `ActiveBuff` les uns sous les autres dans `PlayerHUD`, sans limite native de sept éléments ni boutons de défilement. Le survol fournit une infobulle, mais aucun double-clic d’effet ne reproduit la séquence native « recast puis recherche d’objet dans le sac ». La dissipation existe dans les services de jeu, mais le parcours client et le fallback objet sont différents ; les effets nombreux peuvent aussi dépasser la zone HUD au lieu d’être paginés.

## 63. Canaux de discussion, ignore et changement de destination

`ChatterUI` natif maintient une liste de canaux nommés, avec couleur, mot de passe, activation individuelle et sélection du canal courant. L’entrée clavier possède plusieurs destinations distinctes : jeu, page privée, canal sélectionné et commande GM. L’utilisateur peut rejoindre un canal, en sortir/masquer ses messages, consulter les utilisateurs, envoyer une page privée et gérer une liste d’ignorés persistée dans `SaveGame`. Les messages système sont séparés des messages de canal et la file système est limitée à cinq éléments avant suppression des plus anciens.

Le Java dispose d’un `GameChat` orienté zone de texte et d’un journal optionnel, mais aucune structure équivalente de canaux persistés avec couleur/mot de passe/activation, aucune liste d’ignorés native et aucun routage unifié jeu/page/canal comparable n’a été trouvé. Les raccourcis et commandes Java sont dispatchés par des handlers distincts ; changer de destination, ignorer un joueur ou restaurer les canaux après redémarrage ne reproduit donc pas l’état du client 1.68. La rétention des messages système et leur séparation visuelle sont également différentes.

## 64. Carte temps réel et mémoire de carte

`RTMap` natif reconstruit la vue autour de la position du joueur à partir du monde courant, applique le masque de visibilité et utilise `CSaveGame::GetRTMapVal` pour retrouver la conversion des coordonnées. Le fichier de sauvegarde contient une mémoire de carte distincte par monde (jusqu’à 10 mondes, avec une grille de 192×192 valeurs dans `SaveGame.h`) ; cette mémoire permet de conserver les zones déjà révélées entre les sessions. La fenêtre possède aussi son propre masque, son marqueur de position et une conversion différente selon les anciennes cartes ou les cartes haute résolution.

`GuiWorldMap` Java recrée correctement une vue locale à partir de `OriginalRtMap` et applique un masque graphique statique, mais aucune lecture/écriture d’une mémoire de découverte par joueur et par monde n’a été trouvée dans `PlayerStateStore` ou `PlayerStateDto`. La vue Java est donc recalculée depuis la carte complète à chaque ouverture, avec un état de révélation qui ne suit pas la persistance native. Les deux clients peuvent afficher la même position et la même palette tout en révélant des zones différentes après déplacement ou redémarrage.

## 65. Fenêtre au premier plan et glisser-déposer global

`GameUI` natif maintient un contrôle de premier plan partagé par toute l’interface. Une fenêtre modale peut demander ce contrôle, empêcher les autres enfants de recevoir les clics et le rendre explicitement à sa fermeture. Le glisser-déposer est également global : la source, le visiteur d’événement, le parent et la position initiale sont conservés dans `GameUI`, ce qui permet à une zone de dépôt différente de la fenêtre source de finaliser ou d’annuler l’opération. Le déplacement minimum avant affichage de l’aide est aussi testé par une distance précise, afin qu’un clic bref ne démarre pas un drag ou une aide intempestive.

Le Java possède des états de drag locaux dans `Inventory`, `SpellBook` et `PlayerHUD`, et `GuiManager` ouvre/ferme des écrans, mais aucune capture globale unique de la source et du parent équivalente à `GameUI::SetDragItem` n’a été trouvée. Les zones de dépôt sont donc couplées à chaque écran (inventaire vers quick-slot, sort vers quick-slot, etc.) et une fenêtre ouverte par-dessus ne partage pas nécessairement le même verrou d’entrée avec le monde. Un drag commencé dans une interface puis déplacé vers une autre, ou une fermeture pendant ce drag, peut ainsi être annulé ou interprété différemment du client natif.

## 140. Gestionnaire global des fenêtres : superposition native contre écran unique Java

`RootBoxUI` natif conserve une collection de fenêtres simultanément visibles, un `foregroundChild`, une liste de fenêtres minimisées et un routage global des clics gauche/droit, du glisser, de la molette, du texte et des touches. L’ouverture d’un panneau plein écran minimise automatiquement le chat et les macros ; la carte, l’aide et la fiche d’objet ont encore des règles de priorité spécifiques. Le rendu dessine d’abord les fenêtres minimisées puis la fenêtre au premier plan, tandis que `IsMouseOwned` empêche le monde de recevoir un événement déjà consommé par l’interface.

`GuiManager` Java ne conserve qu’une seule référence `current`. L’ouverture d’un nouvel écran dispose immédiatement l’ancien, et les événements sont transmis uniquement à cet écran ; les éléments HUD sont gérés à côté, sans pile globale de fenêtres minimisées ni `foregroundChild` partagé. Le Java ne peut donc pas reproduire les combinaisons natives « chat + macros + panneau minimisé », les priorités inter-fenêtres ou le routage d’un même événement entre fenêtres superposées.

## 66. Minimap locale et filtrage des objets

`SideMenu` natif génère une TMI locale autour du joueur en lisant le monde courant, puis filtre les objets selon leur groupe (`m_bShowThisObjType`). Le panneau peut donc afficher ou masquer séparément différentes catégories d’objets proches, changer de monde et reconstruire la zone sans recharger l’ensemble de la carte. Ce filtrage est indépendant de la carte mondiale `RTMap` et du simple marqueur de position.

Le Java fournit `GuiWorldMap`/`MapScreen` pour la carte mondiale et les gestionnaires d’objets pour les interactions, mais aucun composant de minimap locale équivalent au TMI du `SideMenu` ni tableau de filtres persistants par groupe d’objet n’a été trouvé dans le HUD. Les objets proches sont donc rendus par le monde ou traités par les handlers, sans la même possibilité de les masquer sélectivement dans une mini-carte. La navigation et la lisibilité des éléments proches diffèrent ainsi même lorsque la carte mondiale est correcte.

## 67. Cycle de prise d’effet et de sauvegarde des options

`OptionsUI` natif charge les valeurs binaires de `OptionParam` à l’ouverture, utilise des plages discrètes (volume 0–10, luminosité 2–10), applique plusieurs réglages aux branches de rendu/audio pendant la session et appelle `g_SaveGame.bSave()` lors de la fermeture de la fenêtre. Le nom du fichier de journal peut être modifié par une popup dédiée, puis réinjecté dans le chatter. Les valeurs ne sont donc pas de simples préférences d’affichage : elles modifient le comportement courant et sont sauvegardées avec le profil client.

Le Java stocke des valeurs flottantes normalisées (volume 0–1, luminosité clampée 0,5–1,25) dans un fichier JSON séparé et ne possède pas le même cycle unique « chargement de la fenêtre → application de tous les effets → sauvegarde à la fermeture ». Plusieurs réglages natifs sont absents ou seulement représentés par une approximation (`highQualityFont`, journalisation, transparence), et le format ainsi que les bornes diffèrent. Une valeur importée ou réglée à l’identique n’a donc pas nécessairement le même effet immédiat ni la même compatibilité de persistance.

## 68. Double-clic d’objet, équipement et macros d’inventaire

Dans `InventoryUI` natif, le double-clic sur une ligne d’inventaire est une action dédiée : il tente d’utiliser l’objet, ou de l’équiper selon son type, et envoie ensuite `RQ_UseObject`/les requêtes d’équipement. Le clic droit demande séparément le nom puis les informations détaillées de l’objet. Un drag vers un coffre ou un échange passe par une popup de quantité et les paquets spécifiques ; un drag impossible est restauré à sa position d’origine. Le bouton Macro transforme aussi l’objet en macro persistante identifiée par son `baseId`, puis la macro recherche l’instance correspondante dans le sac avant de l’utiliser.

`Inventory` Java gère le drag et l’auto-équipement dans des handlers de toucher, mais aucune machine d’événements générale ne reproduit le double-clic natif, le clic droit « nom puis info », la popup de quantité inter-conteneurs et le retour global vers la position source. Les quick-slots Java associent un nom à un emplacement ; ils ne reproduisent pas les macros d’objet natives basées sur l’identifiant de base et la recherche d’une instance disponible. Un même geste sur une pile, un objet équipable ou un item provenant d’un coffre peut donc déclencher une action différente ou ne pas offrir le même choix de quantité.

## 69. Feuille de personnage : liste complète des compétences et utilisation

`CharacterUI` natif demande la liste des compétences au serveur (`RQ_GetSkillList`), construit une liste défilante de toutes les compétences reçues et affiche pour chacune l’icône, la valeur courante, la valeur vraie/non modifiée et une description détaillée. Le double-clic sur une compétence utilisable appelle `UseSkill`; selon le type, l’action est immédiate ou capture le prochain clic de position/unité via `LockNextEvent(DM_CLICK, ...)`. Les macros de compétences reprennent ensuite l’identifiant numérique de la compétence et la recherchent dans la liste reçue avant exécution.

`Statistics` Java affiche une sélection fixe de compétences de combat (`attack`, `dodge`, `archery`) et les valeurs de base/effectives, mais ne reproduit pas la liste serveur complète, son défilement, les compétences activables par double-clic ni la capture de cible propre aux compétences. Les raccourcis Java n’utilisent pas non plus le modèle natif de macro par identifiant de compétence. Une compétence apprise ou fournie dynamiquement par le monde peut donc être absente de la feuille Java ou ne disposer d’aucun parcours d’utilisation équivalent.

## 70. Informations de feuille absentes ou non alimentées

La feuille native affiche effectivement la chance (`Lck`), les kills et morts courants, le poids courant/maximum et le poids restant, les six résistances et six puissances élémentaires, ainsi que le karma transformé en neuf libellés localisés selon des seuils précis. Elle distingue aussi les HP vrais des HP maximum modifiés par les bonus et affiche l’XP restante jusqu’au niveau suivant. Ces champs sont rafraîchis à partir de l’état reçu par `RQ_GetStatus`.

`Statistics` Java réserve certains libellés (karma, XP restante), mais ne les alimente pas systématiquement avec une valeur native équivalente ; la chance, les compteurs de kills/morts et le détail du poids restant ne sont pas présentés comme dans `CharacterUI`. L’état Java conserve parfois le karma dans le DTO, sans reproduire la conversion localisée par seuils. La feuille peut donc sembler complète visuellement tout en donnant moins d’informations effectives et en réagissant différemment aux mises à jour de statut.

## 71. Interface de guilde et permissions d’actions

`GuildUI` natif récupère explicitement la liste des membres avec `RQ_GuildGetMembers`, conserve l’identifiant, le nom et une chaîne de droits de la guilde, puis n’affiche les boutons d’invitation et d’exclusion que si les bits de permission correspondants sont actifs. La sélection d’un membre peut ouvrir une page, l’invitation capture le prochain clic sur une unité du monde, et l’acceptation d’une invitation passe par une popup avec nom de guilde, nom de l’émetteur et choix oui/non. Les membres proches et éloignés, ainsi que le chef, ont des couleurs différentes dans la liste et les contrôles de départ/exclusion envoient des requêtes distinctes.

Dans le Java, aucune fenêtre de guilde interactive équivalente ni gestionnaire de droits/membres/invitations associé aux paquets natifs n’a été trouvé. Les éléments `guild`/`clan` existants concernent surtout les clans de monstres ou des comportements de PNJ, pas la guilde du joueur. L’utilisateur Java ne dispose donc pas du même flux d’acceptation d’invitation, de ciblage d’un joueur à inviter, de filtrage par permissions, de couleur de proximité ou de mise à jour serveur de la liste des membres.

## 72. Groupe : partage automatique, états de membres et ciblage

`GroupPlayUI` natif conserve pour chaque membre un identifiant, un nom, un statut de chef et un pourcentage de HP mis à jour séparément. La fenêtre distingue le chef et les membres proches/éloignés par couleur, affiche des barres de vie réduites dans le HUD, et active/désactive les boutons invitation, départ, exclusion et partage automatique selon le rôle courant. L’invitation cible le prochain clic sur une unité, tandis que l’acceptation d’une demande passe par une popup ; le changement de partage automatique envoie une action propre au serveur.

Le Java ne contient pas d’interface de groupe joueur équivalente avec membres distants, chef, HP en pourcentage, partage automatique et boutons conditionnés par le rôle. Les recherches `group`/`party` renvoient surtout des effets, des comportements de monstres ou des données de jeu, sans workflow de groupe réseau comparable. Les combats Java peuvent donc fonctionner sur des cibles locales sans fournir les informations de groupe, le suivi HUD et les transitions d’invitation du client 1.68.
## 73. Échange joueur-à-joueur : état bilatéral et validation

Dans le client 1.68, l’échange entre joueurs n’est pas une simple fenêtre d’inventaire : il possède un état de session piloté par le serveur. La fenêtre maintient séparément les objets et l’or proposés par le joueur local et par l’autre joueur, ainsi que les deux états de validation (`myStatus` et `otherStatus`). Les événements natifs couvrent explicitement le démarrage, l’annulation, la fin de l’échange et le changement de statut de chaque participant.

Les déplacements d’objets passent par des requêtes dédiées dans les deux sens (sac vers échange, échange vers sac) et prennent une quantité explicite. Le client affiche donc un sélecteur de quantité pour les piles, avec une limite calculée, au lieu de déplacer systématiquement toute la pile. La fermeture de l’interface alors qu’un échange est actif envoie une annulation, et le contenu du sac est rafraîchi depuis l’état serveur.

Dans le Java actuel, `ShopScreen`, l’inventaire et les coffres existent, mais aucune implémentation équivalente à `TradeUI` n’a été trouvée : pas d’offre bilatérale joueur/joueur, de statut de confirmation local/distant, d’événements start/cancel/finish, ni de transfert quantifié dans les deux directions. Il manque donc le comportement transactionnel complet : validation des deux côtés, annulation cohérente, verrouillage de l’offre et résolution atomique/rollback côté serveur. Les mécanismes d’offre repérés dans le Java concernent des boutiques ou des écrans internes et ne constituent pas cet échange joueur-à-joueur.
## 74. Session réseau : perte de connexion, ACK et reprise d’état

Le client 1.68 utilise une couche de communication persistante avec connexions suivies individuellement, paquets fragmentés/réassemblés, identifiants de paquets reçus, paquets en attente d’ACK, retransmission/expiration et liste des connexions perdues. `PacketCenter::KeepAlive()` est appelé à chaque tour de boucle pour détecter un blocage ou une attente infinie. La maintenance vérifie les timeouts des paquets, des fragments et de la connexion, puis retire la connexion expirée. La fermeture finale envoie également un paquet explicite de déconnexion.

Le Java actuel ne contient pas de couche réseau de jeu équivalente dans `src/main/java` : les occurrences de connexion/session/keepalive/reconnexion sont absentes, tandis que les mots `teleport` et `zone` correspondent à des fonctions locales de contenu et de rendu. Il n’existe donc pas de comportement comparable pour la détection de perte, les ACK, la déduplication, la fragmentation, la retransmission, la reprise ou le nettoyage des états UI/jeu après déconnexion. Le déplacement et le changement de carte Java restent locaux ; ils ne peuvent pas être comparés à une transition confirmée par serveur avec restauration d’état.
## 75. Vol à la tire (`RobUI`) : sélection et exécution

Le client 1.68 possède une interface dédiée au vol à la tire. Elle reçoit l’autorisation (`canRob`), le nom de la cible et une liste d’objets exposés, affiche les objets avec leur apparence, identifiant de base, identifiant d’instance et quantité, permet d’en sélectionner un, puis d’exécuter l’action via un bouton séparé. L’interface sait aussi refuser l’action et se fermer explicitement.

Le Java contient bien une référence de contenu/compétence nommée `rob` et un identifiant de sort, mais aucune fenêtre ou logique équivalente à `RobUI`, aucun inventaire de la cible sélectionnable et aucune commande d’exécution/validation du vol n’ont été trouvés. La présence du nom dans les définitions ne fournit donc pas le fonctionnement natif : sélection de l’objet, autorisation serveur, résultat, refus et fermeture manquent.
## 76. Fiche détaillée d’objet et requête serveur

Le client 1.68 possède une fiche d’objet (`RTItemI`) alimentée par `RQ_QueryItemInfo`. Un clic droit sur un objet au sol ou dans l’inventaire peut demander au serveur sa description complète : statistiques directes, résistances/pouvoirs, compétences, bonus et valeurs min/max. La fiche colore aussi l’objet selon le nombre de bonus et distingue les informations d’un objet d’inventaire de celles d’un objet au sol. Elle est affichée dans une fenêtre dédiée, puis invalidée lorsque la réponse disparaît ou lorsqu’une nouvelle requête est lancée.

Le Java affiche des tooltips construits localement à partir de `ItemDefinition`/de valeurs connues, mais aucun équivalent de requête d’information d’objet, de réponse asynchrone, de fiche `RTItemI` ou de calcul d’affichage des bonus min/max n’a été trouvé. Un objet au sol ne bénéficie donc pas du même cycle d’inspection serveur, et les données visibles peuvent être limitées aux définitions statiques locales.
## 77. Vente aux marchands : prix et identité des objets

La vente native (`SellUI`) reçoit du serveur une liste de vente contenant, pour chaque entrée, l’identifiant d’instance (`dwID`), l’apparence, le prix, la quantité disponible et `maxQty`. La sélection modifie le total demandé et le montant restant, puis `RQ_SendSellItemList` renvoie au serveur les couples identifiant d’instance/quantité vendue. Le serveur garde donc le contrôle du prix, de la disponibilité et de l’objet exact consommé.

Dans `ShopScreen.forSelling`, le Java reconstruit les entrées à partir de clés d’objets locales, fixe le prix à `def.getPrice() / 2`, limite la quantité par un simple comptage d’objets et, à la confirmation, détruit localement les objets puis crédite immédiatement l’or. Il n’y a pas d’identifiant d’instance, de prix transmis par le marchand, de `maxQty` serveur ni de requête de vente équivalente. Deux objets identiques avec durabilité/bonus différents ne peuvent donc pas suivre le même comportement que dans le client 1.68, et une vente locale n’a pas la validation/rollback serveur natif.
## 78. Achat marchand : panier local contre validation serveur

`BuyUI` construit un panier de plusieurs références, affiche le prix courant et vérifie localement l’or disponible, mais la confirmation envoie au serveur la liste des identifiants et quantités (`RQ_SendBuyItemList`). Le serveur reste l’autorité pour le stock, le prix final, le poids/volume d’inventaire et la création effective des objets.

Le Java déduit immédiatement l’or et ajoute directement les clés dans la liste d’inventaire de `Player`. `ShopScreen` ne possède pas de panier soumis à un serveur, de réponse d’achat, de contrôle de stock distant ou de compensation en cas d’échec. Le comportement visible peut donc diverger dès qu’un prix, une quantité disponible ou une contrainte d’inventaire est différente de la définition locale.
## 79. Statistiques XP/heure et statistiques PvP

Le client natif contient deux services distincts qui transforment les données reçues en messages dans l’historique de chat. `XpStat` mémorise l’XP de départ et l’horodatage, calcule l’XP gagnée, le temps écoulé et la vitesse moyenne horaire, puis affiche les résultats formatés. `PvpRanking` affiche séparément les points PvP, kills/morts courants et totaux, meilleure série et série en cours.

Le Java calcule et affiche certaines valeurs d’XP ou de PvP dans les écrans de statistiques, mais ne reproduit pas ce cycle natif de démarrage/arrêt de mesure, de calcul horaire et d’injection dans l’historique de chat. Les statistiques natives sont donc une fonction interactive persistante du client, pas seulement des champs de feuille de personnage ; ce parcours manque ou reste différent dans le Java.
## 80. Inventaire du protocole client : 131 opérations natives sans équivalent Java

Le dépouillement de `PacketTypes.h` trouve 131 symboles `RQ_` distincts. Ils couvrent notamment les huit directions de déplacement, les ACK, la création/suppression de personnage, l’authentification de version, les changements de monde, les mises à jour d’unités, les objets périphériques, les HP/mana/poids/XP, l’équipement, l’utilisation d’objets, les coffres, les achats/ventes, les échanges, les groupes, les guildes, les canaux de discussion, les effets, les sorts, les compétences, le vol, la météo, la mort, le séraphin et les statistiques PvP.

Le Java ne contient pas de `PacketTypes`, de sérialiseur client T4C, de dispatcher de réponses ou de couche de sockets de jeu. Les classes portant des noms comme `Request`, `PhysicalAttackRequest` ou les endpoints HTTP du Content Studio ne sont pas des implémentations de ces paquets : elles servent à l’éditeur ou à des appels locaux. Par conséquent, toute opération `RQ_` reste absente comme contrat d’exécution Java, même lorsqu’un écran ou une mutation locale porte un nom fonctionnel similaire. Les sections précédentes détaillent les conséquences par domaine ; ce comptage fournit la vérification globale de la couverture protocolaire.
## 81. Intégrité des fichiers de jeu avant connexion

Le démarrage natif vérifie des fichiers externes avant de poursuivre : index `gamefiles\MD.MD`, présence de `T4CGameFile.VSB` et de `VSBInfo.Txt`, puis comparaison de la taille du fichier de ressources avec la valeur déclarée. En cas d’absence ou de taille incohérente, il affiche une boîte d’erreur et interrompt/rejoue le chemin de chargement. Le client peut aussi lancer le WebPatch avant l’authentification et conserver les paramètres de serveur/login issus du launcher.

Le Java charge ses ressources à la demande via `SpriteLoader`, avec des exceptions souvent absorbées ou des replis graphiques. Il ne possède pas de phase d’amorçage qui valide un index global et la taille d’un conteneur de ressources avant l’entrée en jeu, ni de WebPatch/launcher intégré équivalent. Une installation partiellement ou incorrectement patchée peut donc atteindre l’écran de jeu avec des ressources manquantes, alors que le client 1.68 bloque ou relance la mise à jour plus tôt.
## 82. Raccourcis globaux et désactivation des macros en fenêtre modale

Le client natif installe dans `MacroHandler` des raccourcis globaux Ctrl+I (inventaire), Ctrl+S (personnage), Ctrl+C (mode attaque), Ctrl+L (chat), Ctrl+G (groupe), Ctrl+P (sorts), Ctrl+M (macros), Ctrl+O (options), Ctrl+W (carte), Ctrl+T (échange), Ctrl+H (capture), Ctrl+A (taille du chat) et Ctrl+V (identifier les objets). Les raccourcis sont remplaçables par le profil, appelés avant les autres traitements clavier et désactivables temporairement (`DisableMacroCall`) pendant l’édition d’une macro ou certaines popups.

Le Java `GameInputHandler` ne reprend qu’un sous-ensemble de ces combinaisons et les redirige vers des écrans/états locaux ; plusieurs ouvertures natives (groupe, échange, macros, options, identification globale, capture) n’ont pas de cible fonctionnelle équivalente. Les écrans Java n’utilisent pas non plus un registre central de callbacks avec blocage global des macros : selon la fenêtre active, une touche peut être ignorée, traitée par le chat ou agir sur le jeu. Le même raccourci et la même séquence d’ouverture ne sont donc pas garantis.
## 83. Événements système serveur : pages, messages, heure et URL

Le client 1.68 traite plusieurs réponses qui ne sont pas de simples lignes de chat : `RQ_MessageOfDay` affiche le message du jour, `RQ_ServerMessage` et `RQ_InfoMessage` alimentent des files/présentations système distinctes, `RQ_Page` et `RQ_TogglePage` gèrent les pages privées avec activation/désactivation, et `RQ_OpenURL` transmet une URL à l’action système du client. `RQ_GetTime` synchronise aussi une information d’heure serveur. `RQ_PlayerFastMode` modifie un mode d’exécution reçu/confirmé par le serveur, et `RQ_SafePlug` participe au contrôle anti-déconnexion/anti-plug natif.

Le Java possède un `GameChat` et des `SystemMessage`, mais pas de dispatcher de ces réponses ni de distinction protocolaire entre message du jour, message serveur, info, page privée activée/désactivée et canal de discussion. Aucun cycle d’heure serveur, d’ouverture d’URL reçue, de mode rapide confirmé ou de `SafePlug` équivalent n’a été trouvé. Les textes Java ressemblant à des messages système sont donc produits localement et ne reproduisent pas les transitions, files, permissions ou actions externes du client 1.68.
## 84. Opérations natives résiduelles : rafraîchissement, existence et nettoyage

Le contrôle inverse des symboles de `PacketTypes.h` laisse plusieurs opérations fonctionnelles qui ne sont pas des doublons de simples écrans :

- `RQ_ViewBackpack` et `RQ_ViewEquiped` demandent au serveur un état frais du sac et de l’équipement ;
- `RQ_QueryItemName` résout le nom d’une instance, notamment dans les coffres, l’inventaire et l’échange, avant affichage ;
- `RQ_JunkItems` envoie au serveur la liste des identifiants d’objets jetés en une action ;
- `RQ_UseItemByAppearance` et `RQ_CannotFindItemByAppearance` gèrent l’utilisation par apparence et le retour d’échec lorsque l’instance n’existe plus ;
- `RQ_MissingUnit`, `RQ_QueryUnitExistence` et `RQ_SendPeriphericObjects` maintiennent la cohérence des unités autour du joueur ;
- `RQ_QueryNameExistence`, `RQ_QueryPatchServerInfo` et `RQ_QueryServerVersion` arbitrent respectivement le nom, le patch et la compatibilité serveur ;
- `RQ_NotifyGroupDisband` et `RQ_ToggleChatterListening` propagent des changements distants qui ferment/actualisent les interfaces concernées.

Le Java modifie directement les listes locales d’inventaire/équipement, connaît les noms par `ItemRegistry`, ne dispose pas d’action groupée de rebut soumise au serveur et ne possède pas de cycle de rafraîchissement d’unités périphériques. Les recherches ne montrent pas non plus de réponses Java équivalentes pour l’existence d’un nom/unité, le patch, la dissolution d’un groupe ou l’écoute des canaux. Ces opérations complètent les écarts de protocole généraux : elles expliquent précisément pourquoi une liste Java locale peut rester valide visuellement tout en étant déjà obsolète ou invalide côté 1.68.
## 85. Rafraîchissement des points, sorts, remort et classement PvP

Le client 1.68 possède des requêtes distinctes pour relire les points de compétences (`RQ_GetSkillStatPoints`), envoyer un entraînement de caractéristiques (`RQ_SendStatTrain`), envoyer une liste d’apprentissage de sorts (`RQ_SendSpellList`), demander le remort (`RQ_Remort`), récupérer la liste de personnages/PC personnelle (`RQ_GetPersonnalPClist`) et demander le classement/statut PvP (`RQ_GetPvpRanking`). Chaque opération a son propre ACK et ses réponses alimentent ensuite les écrans concernés.

Le Java modifie les statistiques, les compétences, les sorts appris, le remort et les données PvP directement dans `Player`/les écrans, avec sauvegarde locale. Il n’existe pas de distinction entre demande, acceptation, refus et rafraîchissement serveur pour ces opérations. Une double validation, un coût différent, un plafond de points ou une modification effectuée par un autre état serveur ne peut donc pas produire la même transition que dans le client 1.68.
## 86. Horloge et cycle jour/nuit : temps serveur contre temps local

La réponse native `RQ_GetTime` transporte séparément seconde, minute, heure, jour, semaine, mois et année dans `g_TimeStructure`. Le temps affiché et les effets qui en dépendent partent donc d’une référence serveur reçue, même si le client continue ensuite à faire tourner ses compteurs locaux.

Le Java instancie `DayNightCycle` avec une heure par défaut ou une heure relue de `PlayerStateStore`, puis l’incrémente avec `delta`. Cette horloge n’est jamais recalée par une réponse serveur. Deux clients lancés à des moments différents peuvent donc avoir des phases jour/nuit différentes, alors que le client 1.68 converge vers la date/heure du serveur.
## 87. Flèches : résultat serveur et collision du projectile

Le client 1.68 reçoit `RQ_ArrowHit` avec l’identifiant du tireur, celui de la cible et le nouveau pourcentage de HP, puis lance `ShootArrow` et `PlAttack` avec cet état déjà résolu. `RQ_ArrowMiss` transporte au contraire le tireur, la position finale et un indicateur de collision ; le client joue alors la trajectoire et le résultat d’échec sans recalculer le jet de précision.

Dans `MainGameScreen`, le Java lance le projectile puis exécute `applyBowImpact` dans le callback visuel. `CombatResolver` décide localement du hit/miss et des dégâts au moment de l’impact, avant mise à jour de la cible. La collision visuelle, le résultat de combat et la diminution des HP ne suivent donc pas le contrat 1.68 : un projectile Java peut atteindre l’écran alors que le client natif aurait reçu un miss, ou inversement, et le résultat n’est pas corrigé par un paquet serveur.
## 88. Effets de sorts : paquet de résultat contre résolution locale

La réponse native `RQ_SpellEffect` ne contient pas seulement l’identifiant du sort : elle transporte le caster, la cible, les positions de cible et de caster, ainsi que `spellEffectId` et `spellChildId`. Le client choisit ensuite le déplacement et le suivi de l’effet (`Follow`), la position finale et la présentation offensive ou bénéfique à partir de cette réponse. L’animation est donc déclenchée par un résultat réseau qui peut être différent de la tentative initiale.

Le Java appelle `SpellEffectManager.resolve(...)` dans les callbacks d’impact de `MainGameScreen`, puis modifie localement HP, mana, buffs, drains, effets persistants et invocations. Aucun paquet de résultat équivalent, aucune validation serveur et aucun rollback n’intervient entre le lancement visuel et cette mutation. Le Java fait donc de la fin d’animation l’autorité de l’effet ; le client 1.68 fait de la réponse `RQ_SpellEffect` l’autorité, ce qui change les cas de cible morte, déplacée, hors portée, refusée ou déjà modifiée par un autre joueur.
## 89. Lancement de sort : validation locale complète sans requête `RQ_CastSpell`

Dans le protocole client 1.68, `RQ_CastSpell` est une opération distincte de `RQ_SpellEffect` : le lancement et son résultat d’impact ne sont pas une seule mutation locale. Le Java n’a pas de transport de cette requête. `SpellCastingService.begin(...)` valide localement le sort connu, la cible, le PvP, la ligne de vue, le cooldown, l’épuisement, le coût de mana et le taux de réussite, puis décrémente immédiatement le mana et active les cooldowns/exhaustions.

Cela reproduit une règle de gameplay plausible, mais pas le fonctionnement réseau 1.68 : le client Java peut consommer les ressources et démarrer l’animation avant toute acceptation distante, sans acquittement, refus serveur, resynchronisation ou correction du coût. La validation locale devient également dépendante des statistiques et de la formule Java, alors que le client natif délègue la décision finale à l’échange `RQ_CastSpell` puis attend les réponses serveur.
## 90. Statuts persistants : création réseau et expiration locale

Le client natif traite `RQ_CreateEffectStatus` (paquet 83) avec un identifiant d’effet, le temps restant, la durée totale, l’identifiant d’icône et une description, puis appelle `EffectStatusUI::AddEffect`. L’interface des effets persistants est donc alimentée par une notification serveur dédiée, indépendante du paquet d’impact visuel.

Le Java crée directement des `Player.ActiveBuff` depuis `SpellEffectManager`, `ItemUseService`, `SeraphAuraService` ou la restauration locale, puis fait décroître les durées dans `Player`. Il n’existe pas de message serveur séparé qui puisse créer, remplacer, prolonger ou retirer un statut. L’icône, la description, le temps restant et les contributions aux statistiques peuvent ainsi diverger lorsque le serveur 1.68 applique un statut sans animation, le remplace, le dissipe ou refuse l’application locale.
## 91. Dissipation de statut : `RQ_DispellEffectStatus` absent

Le protocole natif possède aussi `RQ_DispellEffectStatus` (paquet 84), qui appelle `EffectStatusUI::DispellEffect` avec l’identifiant de l’effet. La suppression d’un statut n’est donc pas seulement une expiration du compteur client : le serveur peut retirer explicitement un effet à tout moment.

Le Java ne montre pas de dispatcher réseau ni de méthode équivalente recevant un identifiant d’effet ; `Player.dispelBuff(...)` est appelé par des services locaux et par la logique de jeu Java. Une dissipation distante, un remplacement par ID ou un retrait simultané ne peut donc pas être reproduit fidèlement, et les bonus de statistiques liés au buff peuvent rester actifs jusqu’à l’expiration/au nettoyage local.
## 92. Ressources du personnage : mises à jour serveur séparées

Le client natif traite `RQ_GetStatus` comme un état complet initial/rafraîchi (HP et maximum, mana et maximum, XP, or, poids et autres champs), puis traite séparément `RQ_HPchanged` avec le HP courant, `RQ_ManaChanged` avec le mana courant et `RQ_UpdateWeight` avec poids et poids maximum. Ces paquets mettent à jour la feuille de personnage indépendamment des animations de combat ou d’utilisation d’objet.

Le Java ne possède pas ces canaux de synchronisation : `Player` est modifié directement par `SpellCastingService`, `SpellEffectManager`, les écrans de boutique/entraînement et les services locaux, puis sauvegardé dans `PlayerStateStore`. Il n’y a ni snapshot serveur périodique, ni distinction entre ressource acceptée et ressource prédite, ni correction lorsque HP, mana, or ou poids ont changé hors de l’action locale. Les écrans peuvent donc afficher une valeur cohérente avec l’action Java mais différente de l’état 1.68 attendu.
## 94. Cycle d’échange : démarrage, contenu, statuts et terminaison

Le client natif reçoit des événements séparés pour l’invitation, le démarrage, le contenu de chaque côté (`RQ_TradeContents`), les statuts de validation (`RQ_TradeSetStatus`), l’annulation et la fin (`RQ_TradeCancel`, `RQ_TradeFinish`). La fenêtre est donc pilotée par un état bilatéral serveur : une modification de contenu ou de validation peut invalider l’accord précédent avant la clôture.

Le Java possède une interface d’échange, mais les recherches ne montrent pas de transport de ces événements ni de machine d’état réseau correspondante. L’état local ne peut donc pas reproduire une invitation refusée, une annulation distante, une modification simultanée de l’autre inventaire ou la réinitialisation des validations après changement de contenu.
## 95. Groupe : invitation et états distants non séparés

Le client natif distingue l’invitation (`RQ_GroupInvite`), la liste des invitations à actualiser, la liste des membres, le départ, l’exclusion et le partage automatique (`RQ_GroupToggleAutoSplit`). Les HP des membres sont aussi actualisés par un paquet dédié (`RQ_UpdateGroupMemberHp`) et la fenêtre est vidée lorsqu’un départ ou une dissolution est reçu.

Le Java n’a pas de dispatcher de ces événements protocolaire : ses informations de groupe sont produites par l’état local et les écrans. Il manque donc les transitions d’invitation en attente, acceptation/refus, exclusion distante, partage automatique confirmé et HP d’un membre mis à jour sans rechargement complet.
## 96. Objets au sol : cohérence serveur contre ramassage local

Le client natif reçoit les objets proches et leurs mises à jour par le protocole (`RQ_GetNearItems`, `RQ_GetObject`, `RQ_ViewGroundItemIndentContent`). Lorsqu’un objet n’est plus présent ou ne peut pas être obtenu, le client le retire de sa liste et affiche l’état d’échec approprié ; le dépôt dans un conteneur (`RQ_DepositObject`) est également une transition réseau.

Le Java possède `GroundItemManager`, mais il crée les drops depuis le loot local, les affiche et les retire immédiatement au ramassage après validation locale de distance, poids et inventaire. Il n’existe pas d’identifiant d’objet serveur, de réponse de ramassage, de suppression distante ni de requête de détail d’apparence. Deux clients peuvent donc voir le même drop Java comme disponible alors que le client 1.68 l’a déjà consommé, déplacé ou déclaré introuvable.
## 97. Discussion : canaux et listes serveur absents

Le client natif distingue les conversations indirectes et dirigées, les cris, les messages de canal et les messages reçus, puis reçoit séparément la liste des canaux et la liste des utilisateurs avec leur titre et leur état d’écoute. Les opérations d’entrée, sortie, ajout/suppression et écoute d’un canal sont donc des changements d’état serveur, pas uniquement un préfixe dans le texte.

`GameChat` Java possède une saisie, un historique, une autocomplétion et un affichage local, mais aucune liste d’utilisateurs/canaux alimentée par protocole ni routage confirmé des messages privés, directs, cris et canaux. Les commandes textuelles Java ne reproduisent donc pas les droits, abonnements, filtres et retours d’erreur de la discussion 1.68.
## 93. Mise à jour d’effet : durée et icône reçues séparément du buff Java

Le client natif reçoit la création d’un effet avec un identifiant, une durée courante, une durée totale et une icône (`RQ_CreateEffectStatus`), puis peut le supprimer par son identifiant (`RQ_DispellEffectStatus`). L’interface peut ainsi rafraîchir la durée ou remplacer l’icône d’un effet déjà présent sans recalculer l’effet depuis le sort local.

Le Java construit ses `ActiveBuff` depuis l’utilisation locale d’un sort ou d’un objet, avec une durée et une icône issues de ses propres définitions. Il ne dispose pas d’un rafraîchissement distant indépendant par identifiant dans le chemin de jeu ; une correction de durée, d’icône ou de remplacement envoyée par le serveur ne peut donc pas suivre la même transition que dans le client 1.68.

## 98. XP et montée de niveau : notification serveur contre boucle locale

Le paquet natif de montée de niveau reçoit le nouveau niveau, l’XP restante/à atteindre, les HP et maximum, le mana et maximum, puis déclenche l’effet visuel de niveau supérieur. `RQ_XPchanged` reçoit séparément une XP 64 bits et met à jour les statistiques d’XP sans demander au client de recalculer lui-même le seuil.

Le Java fait l’inverse dans `PlayerProgression.addXp(...)` : il ajoute une quantité locale, boucle sur `xpToNextLevel`, augmente le niveau, ajoute les points et applique les gains HP/mana. Cette boucle peut produire un niveau ou des gains différents si la courbe, le multiplicateur de buff, l’XP déjà consommée ou la réponse serveur ne concordent pas ; le Java ne reçoit ni niveau autoritaire ni XP 64 bits de correction.
## 99. Arrêt imposé par le serveur

Le client natif traite `RQ_ExitGame` comme une fermeture distante : il affiche le message d’arrêt, ferme l’application et réinitialise l’état de connexion. Le Java ne montre qu’un écran de confirmation de sortie volontaire et aucun événement serveur équivalent pour arrêt, maintenance ou déconnexion forcée.
## 100. Actions GM et informations de marionnette : portée différente

Le client natif réserve des opérations spéciales à l’état serveur : `RQ_PuppetInformation` concerne les informations d’une unité/marionnette, `RQ_GodCreateObject` crée un objet contrôlé par le serveur et `RQ_BroadcastTextChange` déplace le texte attaché à une unité. `RQ_BreakConversation` force aussi la fermeture du dialogue courant, tandis que `RQ_Attack` peut être reçu comme ordre d’animation/attaque d’une unité distante.

Le Java expose des commandes GM locales et des scripts NPC, mais elles modifient directement `Player`, les monstres ou les objets sans protocole de permission, création serveur, diffusion ou interruption distante de dialogue. Un GM Java peut donc obtenir un effet local sans reproduire l’autorité, la visibilité pour les autres clients ni les mises à jour d’unités du client 1.68.
## 101. ACK, délais et surveillance de la connexion par opération

Le client natif associe à chaque type de paquet un profil d’ACK et de délai différent dans `Comm.cpp` : déplacement et mises à jour instantanées n’attendent pas comme un achat, un sort, un échange ou une transition de session. `RQ_Ack` est traité séparément, tandis que `PacketCenter::isAlive()` surveille aussi l’absence de trafic pendant environ 120 secondes.

Le Java n’a pas de couche de paquets de jeu, de numéro/attente d’ACK, de retransmission ou de délai par opération. Ses appels HTTP de contenu et son état local ne peuvent donc pas reproduire les blocages, expirations, indicateurs de connexion ou répétitions contrôlées du client 1.68.
## 102. Paquet de mort : branche native sans traitement client visible

Dans cette source 1.68, la branche `RQ_YouDied` est bien déclarée mais son traitement dans `Packet.cpp` est vide ; le client ne réalise donc pas, dans cette branche, la pénalité et la résurrection. Le Java exécute au contraire toute la pénalité dans `configurePlayerDeathCallback` : perte d’XP/or, drops, déplacement au point de réapparition, remise en état et sauvegarde.

Il ne s’agit pas d’une équivalence manquante à ajouter silencieusement : c’est une différence de responsabilité démontrée par le code. Le Java a déplacé dans le client une transition que cette branche native laisse au flux serveur/autre gestionnaire, ce qui peut modifier le moment, la source des valeurs et la possibilité d’une correction distante.
## 103. Entrée dans le monde : synchronisation native contre chargement local

Après la préparation du personnage, le client natif demande les objets proches (`RQ_GetNearItems`), marque l’état `EnterGame`, puis envoie `RQ_FromPreInGameToInGame`. Cette transition coordonne le chargement des unités, le nettoyage du chat, le fondu d’écran et l’activation réelle des contrôles ; elle est soumise au serveur et à ses réponses.

Le Java charge `LocalCharacterStore`, construit directement `MainGameScreen` et restaure l’état JSON local dans `CharacterLoadingScreen`. Il n’y a ni `PutPlayerInGame`, ni liste de personnages distante, ni synchronisation d’entrée avant activation du monde. Un personnage supprimé ou modifié par une autre session reste donc sélectionnable localement, et le Java peut entrer dans une carte avec un état que le client 1.68 aurait refusé ou rechargé.
## 104. Arrivée séraphin et remort : déclenchement réseau contre détection locale

Le client natif traite `RQ_SeraphArrival` avec les coordonnées, l’identité d’unité, la lumière, le type et le statut transmis, puis traite `RQ_Remort` en réinitialisant notamment les macros. L’animation et l’état affiché dépendent donc d’une notification réseau et du résultat du remort.

Le Java déduit l’arrivée séraphin à partir de l’état local du joueur dans `startSeraphArrivalIfNeeded()` et déclenche son animation/son blocage de mouvement sans paquet `RQ_SeraphArrival`. Les téléportations sont aussi déclenchées par des définitions Java lorsqu’une tuile est atteinte. Il manque la confirmation distante, les coordonnées imposées par le serveur, la mise à jour des autres unités et la réinitialisation native des macros au remort.
## 105. Drapeau serveur d’autorisation de scripts

`RQ_GodFlagUpdate` transporte un identifiant de drapeau et son état ; pour le drapeau concerné, le client natif active ou désactive `Player.CanRunScripts`. Le serveur peut donc retirer à chaud l’autorisation d’exécuter certaines actions scriptées ou GM côté client.

Le Java expose `GmCommandProcessor` et l’exécution des scripts NPC sans traitement d’un drapeau réseau équivalent. Ses permissions sont déterminées par la présence du composant/commande et l’état local, pas par une bascule serveur reçue. Une révocation distante ou un changement de contexte d’administration n’a donc pas la même portée.
## 106. Mise à jour et existence des unités visibles

Le client natif reçoit `RQ_UnitUpdate`/`SetUnitStat` pour modifier l’état d’une unité déjà connue, et utilise `RQ_MissingUnit` ou `RQ_QueryUnitExistence` pour marquer une unité absente, la supprimer ou vérifier qu’elle existe encore. `RQ_SendPeriphericObjects` alimente en outre la zone autour du joueur ; les objets distants ne sont pas conservés indéfiniment dans la liste client.

Le Java met à jour les monstres et NPC par leurs boucles locales (`MonsterManager`, `NPCManager`) et retire les entités selon leurs propres règles de distance, mort ou compagnon. Il n’existe pas d’identifiant réseau, de réponse d’existence ou de refresh périphérique serveur. Une unité créée, déplacée, tuée ou supprimée dans un autre état de jeu ne peut donc pas être réconciliée avec le client Java.
## 107. Identité et nom d’instance d’objet

Le client natif demande le nom d’une instance par identifiant (`RQ_QueryItemName`) pour le sac, le coffre et les deux côtés de l’échange ; il reçoit ensuite ce nom et le rattache à l’instance correspondante. `RQ_QueryItemInfo` est également prévu pour les informations détaillées d’objet, alors que la définition visuelle et l’instance sont deux notions distinctes.

Le Java résout les objets par clé/numéro dans `ItemRegistry` et affiche les caractéristiques statiques de `ItemDefinition`. Ses listes d’inventaire, coffre, drops et échange ne portent pas l’identifiant d’instance natif ni une réponse de nom différée. Deux objets de même définition mais d’état différent (charges, durabilité, flags ou propriété de quête) sont donc traités comme le même type local.
## 108. Opérations natives restantes : absence d’actions unitaires Java

Le protocole conserve des opérations unitaires distinctes que le Java ne transporte pas : les huit directions (`RQ_MoveEast`, `RQ_MoveSouth`, etc.), les conversations (`RQ_IndirectTalk`, `RQ_DirectedTalk`, `RQ_Shout`), l’entrée/sortie et l’ajout/retrait d’un canal, ainsi que les messages utilisateur/canal. De même, le groupe sépare invitation, jonction, exclusion, départ, mise à jour des membres et mise à jour de la liste d’invitations ; l’échange sépare ajout depuis le sac, retrait et nettoyage.

Les classes Java peuvent reproduire une partie de l’affichage ou appeler directement `Player.move`, `GameChat`, les écrans de groupe et l’échange, mais aucune de ces actions n’est sérialisée avec le type, l’ordre, l’ACK et l’échec natifs. La couverture visuelle partielle ne constitue donc pas une équivalence de fonctionnement : les validations et transitions inter-client de chacune de ces opérations restent absentes.
## 109. Compétence active : retour serveur contre cooldown local

Le client natif demande la liste des compétences (`RQ_GetSkillList`) au serveur et reçoit ensuite les états utilisables. Le retour d’utilisation contient un identifiant de compétence et un code de résultat (`SkillID`, `Return`) ; l’activation et l’échec ne sont donc pas déduits uniquement du bouton pressé.

Le Java utilise `SkillService.use(...)`, vérifie localement le niveau, les attributs, le cooldown et les conditions, puis déclenche le cooldown/mutation sans réponse serveur. Une compétence refusée, modifiée ou consommée par un autre état peut donc produire un effet Java différent, sans code de retour ni resynchronisation de la compétence.
## 110. Attaque normale : paquet d’animation et HP autoritaires

Le client natif reçoit les attaques validées avec l’attaquant, le défenseur, leurs positions et, pour une touche, le HP résultant (`pHp`) ; il lance ensuite `PlAttack`/`SetAttack`. Une attaque manquée produit une branche distincte qui anime l’attaque sans appliquer de dégâts. La précision et le montant final ne sont donc pas recalculés par le client à l’impact.

Le Java appelle `CombatResolver.resolve(...)` dans les callbacks locaux de `MainGameScreen`, `MonsterManager` et `BaseMonster`, puis applique directement les HP et les morts. Le même code décide à la fois du jet, du résultat et du rendu. Comme pour les flèches, cela permet des divergences de touche, de miss, de dégâts et de timing dès qu’un état distant devrait être prioritaire.
## 111. Équipement et utilisation d’objet : requêtes natives sans mutation locale immédiate

Le protocole natif distingue `RQ_EquipObject`, `RQ_UnequipObject` et `RQ_UseObject`, chacun soumis à son profil d’ACK. Le client envoie l’action sur l’instance, puis attend que les réponses d’état mettent à jour l’équipement, l’apparence, les charges et les caractéristiques ; l’équipement affiché n’est donc pas une preuve d’acceptation.

Le Java exécute `InventoryService.equip/unequip` et `ItemUseService.useOnSelf` directement depuis `Inventory` ou `MainGameScreen`, actualise les apparences, les charges et les effets, puis sauvegarde localement. Une contrainte serveur, un conflit d’instance, un objet déjà utilisé ou une réponse d’échec ne peut pas annuler cette mutation.
## 112. SafePlug : autorisation serveur de fermeture

Le client natif traite `RQ_SafePlug` avec un statut binaire : un statut interdit le logoff (`boInterruptLogoff`), tandis que l’autre autorise la fermeture forcée (`boForceLogoff`). Cette décision est reçue du serveur et s’applique au moment de la déconnexion, indépendamment de la fenêtre de confirmation.

Le Java ne possède pas de statut SafePlug ni de garde de déconnexion serveur. `ExitGameConfirmScreen` déclenche une sortie locale sans pouvoir être bloqué ou forcé par une réponse de session. Le moment et la sécurité de la fermeture diffèrent donc du client 1.68.

## 134. Compétences : valeur vraie, valeur effective et droit d’utilisation perdus

La réponse native `RQ_GetSkillList` transmet pour chaque compétence son identifiant, son nom, sa description, sa valeur courante (`dwStrength`), sa valeur vraie/non modifiée (`dwTrueStrength`) et un indicateur d’utilisation (`bUse`). Le client conserve ces champs séparément dans `USER_SKILL`, les affiche dans `CharacterUI` et n’autorise le double-clic ou la macro que si la compétence reçue est utilisable. La réponse d’exécution ajoute ensuite `SkillID` et `Return`, afin que le résultat réel soit fourni par le serveur.

Le Java charge `SkillDefinition`/`SkillRegistry` et stocke essentiellement un niveau local par identifiant. `SkillService.use` déduit l’autorisation du niveau et de son cooldown, puis applique immédiatement les mutations (`meditate`, `sneak`) ; il n’existe pas de paire valeur vraie/effective, de flag `bUse` reçu, ni de retour serveur `Return` par compétence. Les bonus qui modifient seulement la valeur effective, les refus serveur et l’état utilisable peuvent donc être affichés ou exécutés différemment.

## 113. Dialogue NPC : conversation serveur et affichage local découplés

Dans le client 1.68, un clic/parole vers un NPC construit `RQ_DirectedTalk` avec les coordonnées du NPC, son identifiant, la direction calculée du personnage, la couleur et le texte. Une parole générale passe par `RQ_IndirectTalk`. Le serveur peut ensuite renvoyer `RQ_GetUnitName` (identifiant, nom, couleur et nom de guilde) et `RQ_BreakConversation`, ce dernier réinitialisant explicitement la cible et l’état de conversation ; les textes au-dessus des unités sont donc liés à des identifiants et à une session de dialogue, pas seulement à une fenêtre locale.

Le Java traite le clic et la touche Entrée dans `NPCInputHandler`, puis `NPCManager`/les `NpcSpec` avancent directement un dialogue local (`welcomeText`, `DialogueTopic`, réponses et actions). Il n’existe pas de transport de `RQ_DirectedTalk`/`RQ_IndirectTalk`, de retour `RQ_GetUnitName` avec couleur/guilde, ni de paquet d’interruption qui invalide la conversation. Un dialogue Java peut donc continuer ou déclencher une action alors que le serveur 1.68 aurait refusé la distance, la cible, le texte ou interrompu la conversation.

## 114. Cycle personnage : roster local contre compte serveur

Le client natif expose des requêtes de session et de compte distinctes : `RQ_RegisterAccount`, `RQ_CreatePlayer`, `RQ_DeletePlayer`, `RQ_GetPersonnalPClist`, `RQ_PutPlayerInGame`, `RQ_ReturnToMenu`, `RQ_Reroll`, ainsi qu’une réponse `RQ_MaxCharactersPerAccountInfo`. Le serveur fournit donc la liste, les limites, l’identité et les résultats d’opération ; la sélection et le retour en jeu sont des transitions réseau, pas seulement des changements d’écran.

Le Java utilise `LocalCharacterStore` et `characters.json` pour créer, supprimer, lister et charger les personnages, applique localement `MAX_CHARACTERS`, puis entre dans `MainGameScreen`. Il n’y a pas de compte/session serveur ni de réponse d’acceptation pour ces opérations. Deux clients, une suppression distante, une limite de compte ou une création concurrente ne peuvent donc pas produire les mêmes erreurs et états que le client 1.68.

## 115. Scripts de conversation : conditions persistantes et effets serveur absents

Les scripts NPC livrés avec le client/source 1.68 décrivent des conversations qui consultent et modifient des flags persistants, vérifient le niveau, l’or, les objets, les délais, proposent des décisions Oui/Non, retirent plusieurs objets, donnent des objets ou déclenchent un téléport. Le dialogue est donc une façade d’un script serveur transactionnel : le texte affiché dépend de l’état réel du personnage et l’effet ne doit être appliqué qu’après validation de cette transaction.

Le Java représente principalement un NPC par `NpcSpec.DialogueTopic` (mots-clés, réponse, actions) et avance la conversation côté client. Même lorsque des `ActionType` existent, ils ne remplacent pas l’exécution atomique des conditions et consommations du script serveur 1.68. Une réponse peut être visible sans que les préconditions soient celles du serveur, ou une récompense/consommation peut être appliquée localement sans confirmation ni rollback.

## 116. Canaux de discussion et groupe : états distants non représentés

Le client natif reçoit des listes structurées de canaux et d’utilisateurs (`RQ_GetChatterChannelList`, `RQ_GetChatterUserList`) avec état d’écoute, titre et guilde. Il reçoit aussi les mises à jour de groupe avec identifiant, nom, niveau, pourcentage de HP, chef et partage automatique ; `RQ_GroupInvite` ouvre une invitation distante et `RQ_GroupLeave`/la dissolution vide explicitement la liste. Les ajouts, retraits et messages de canal sont des opérations séparées du chat général.

Le Java possède `GameChat` comme zone d’affichage/saisie, mais aucune représentation équivalente de canaux, utilisateurs abonnés, guildes, invitations, membres ou HP de groupe n’a été trouvée. Les messages peuvent être affichés localement, sans synchroniser l’abonnement, la liste des interlocuteurs, le partage d’expérience ou la sortie du groupe. Le comportement social et les mises à jour inter-clients divergent donc même si la saisie textuelle fonctionne.

## 135. Annuaire des canaux : rafraîchissement périodique et écoute individuelle absents

Dans `ChatterUI`, l’ouverture de la fenêtre demande la liste publique des canaux (`RQ_GetChatterChannelList`). La sélection d’un canal demande ensuite sa liste d’utilisateurs (`RQ_GetChatterUserList`) avec le nom du canal ; cette liste est rafraîchie automatiquement toutes les dix secondes. Chaque utilisateur reçu possède au minimum nom, titre, guilde et état d’écoute. Le bouton d’écoute envoie en plus le canal et le nouvel état (`RQ_ToggleChatterListening`).

`GameChat` Java ne possède ni annuaire de canaux, ni sélection d’un canal distant, ni liste d’utilisateurs, ni rafraîchissement périodique, ni abonnement d’écoute par canal. Le chat Java est donc une conversation locale/générale et ne reproduit pas la visibilité, le filtrage ou l’actualisation du système de canaux 1.68.

## 136. Aide intégrée : manuel paginé et aides contextuelles absents

Le client 1.68 instancie `RTHelp` comme une fenêtre native dédiée. Elle contient douze pages d’aide (`Help0` à `Help11`), deux pages de lettres, quatre pages de cartes et une page de labyrinthe, avec navigation précédente/suivante et affichage conditionnel. Elle est ouverte automatiquement lors de la première entrée en jeu (`Show(true, 0, 0)`) et peut aussi être affichée pour des aides spéciales, par exemple après l’utilisation d’une lettre particulière. L’interface globale traite cette fenêtre comme un écran modal qui masque/minimise les autres panneaux.

Le Java ne possède pas de `RTHelp`, de manuel paginé, de pages de cartes/labyrinthe ni de déclenchement contextuel lié à l’utilisation d’objets. `F1` est affectée à l’overlay de debug dans `GameInputHandler`, pas à l’aide du joueur. Même si des textes d’aide existent dans les scripts ou les commandes GM, ils ne reproduisent pas le parcours et le verrouillage modal du manuel 1.68.

## 137. AFK et réponse automatique aux pages : état persistant absent

Le client natif sauvegarde `dwAfkStatus` et un message AFK de 2048 caractères dans sa configuration. Les commandes `!AFK ON`, `!AFK OFF`, `!AFK VIEW` et `!AFK MESSAGE ...` modifient cet état. Lorsqu’une page privée arrive, le client répond automatiquement par `RQ_Page` avec le message configuré, tout en évitant les boucles et le flood sur certains textes ; les pages ignorées sont filtrées avant ce traitement.

Le Java ne possède ni état AFK persistant, ni commande équivalente, ni réponse automatique aux messages privés. `GameChat` affiche/saisit du texte mais ne reproduit pas la distinction entre page reçue, auto-réponse, anti-boucle et liste d’ignorés du client 1.68.

## 138. Capture d’écran : macro native et répertoire dédié absents

Le client 1.68 associe la capture d’écran à une macro configurable, par défaut `Ctrl+H`, capture le bureau ou la fenêtre selon le chemin d’exécution, crée le dossier `ScreenShot` dans le répertoire de sauvegarde puis écrit l’image. Le drapeau `TakeScreenShot` est consommé dans la boucle de rendu, ce qui garantit une capture après le rendu d’une frame.

Le Java ne contient pas de macro de capture ni de traitement `TakeScreenShot` dans `GameInputHandler`/les préférences. Ses raccourcis locaux affectent le debug, les coordonnées, la téléportation ou le rechargement des ressources ; la capture et son stockage automatique ne correspondent donc pas au comportement natif.

## 139. Options graphiques et d’interface : le modèle Java ne couvre qu’un sous-ensemble

`OptionParam` natif persiste, en plus du son et de la luminosité, des bascules qui modifient immédiatement le fonctionnement du client : éclairage graphique élevé, effets graphiques élevés, eau animée, dithering, transparence GUI, animation séraphin, affichage du statut, texte de barre d’XP, affichage de l’or, cadence 32 FPS, macro plein écran, verrouillage de cible, verrouillage du redimensionnement, police haute, zoom, hyperchat, fiche de spécifications d’objet, nouvelle barre de vie, ancienne barre de statistiques, ombrage et lumière des décors animés, ainsi que les effets météo. `OptionsUI` applique directement plusieurs changements au moteur (`ResetAnimWater`, activation/désactivation des effets de statut, zoom et redimensionnement).

`GamePreferences` Java ne contient que volumes, luminosité, plein écran/VSync, quelques valeurs HUD, animation séraphin, texte XP, police et journalisation. Il ne possède pas les interrupteurs natifs d’eau animée, éclairage/effets, dithering, or, 32 FPS, zoom, cible verrouillée, hyperchat, fiche objet, anciennes/nouvelles barres, ombres/lumières de décors ou météo. Ces comportements Java sont donc imposés par le moteur ou absents, sans possibilité de reproduire les profils d’options 1.68.

## 117. Échange : opérations de contenu et nettoyage serveur manquants

Dans le protocole 1.68, l’échange ne se limite pas à une fenêtre : `RQ_TradeInvite` crée l’invitation, `RQ_TradeStarted` ouvre la session, `RQ_TradeAddItemFromBackpack` et `RQ_TradeRemoveItemToBackpack` déplacent des instances entre sac et offre, `RQ_TradeClear` annule le contenu, puis `RQ_TradeSetStatus` et `RQ_TradeFinish` valident ou terminent la transaction. Le client reçoit la liste d’offre via `RQ_TradeContents` et doit refléter les changements de l’autre joueur.

Le Java ne dispose pas d’un transport d’échange ni d’un état bilatéral autoritaire correspondant ; ses services d’inventaire et interfaces locales peuvent seulement modifier le joueur courant. Il manque donc l’identité distante des objets, le nettoyage imposé par l’autre partie, le verrouillage avant validation et la distinction entre annulation, refus et réussite de la transaction.

## 118. Commandes sociales unitaires : absence des demandes d’adhésion et de gestion

Le jeu 1.68 distingue les demandes d’entrée/sortie de canal (`RQ_EnterChatterChannel`, `RQ_AddRemoveChatterChannel`, `RQ_RemoveFromChatterChannel`), l’envoi privé et canal (`RQ_SendChatterMessage`, `RQ_SendChatterChannelMessage`), et les réponses de groupe (`RQ_GroupJoin`, `RQ_GroupKick`, `RQ_UpdateGroupInviteList`, `RQ_UpdateGroupMembers`). Chaque action peut être acceptée, refusée ou modifier la liste distante sans que le client puisse l’inférer de la seule saisie.

Le Java ne possède pas ces commandes ni une couche de résultat social. `GameChat` accepte le texte et l’affiche, mais ne peut pas représenter une invitation en attente, une adhésion confirmée, une exclusion par le chef, une liste d’invitations ou un message privé adressé à un identifiant serveur. Les mêmes noms visibles dans l’interface ne garantissent donc pas le même routage ni les mêmes transitions d’état.

## 119. État visuel des unités : lumière, statut et apparence serveur

Lors de l’ajout ou de la mise à jour d’une unité, le client natif lit séparément le type, les coordonnées, la lumière, le statut et les HP. Les réponses peuvent ensuite modifier l’équipement composé d’un personnage (`SetPuppet` : corps, pieds, gants, casque, jambes, armes, cape), le nom/couleur/guilde, ou créer/retirer un effet de statut. La lumière reçue peut même être appliquée au joueur et alimenter le rendu de la carte et des torches.

`BaseNPC`, `Monster` et les entités Java construisent leur apparence depuis des définitions locales et conservent leurs HP/comportements locaux, sans état réseau équivalent pour `LIGHT`, le byte `STATUS`, les effets serveur, l’identité de guilde ou le paquet de composition reçu. Le rendu Java peut donc afficher un sprite plausible, mais pas reproduire une transformation d’équipement, une invisibilité/état spécial, une aura, une lumière de personnage ou une mise à jour distante arrivée après le spawn.

## 120. Préférences persistantes : macros, canaux et listes locales non équivalentes

`CSaveGame` du client 1.68 sauvegarde par compte/personnage les macros d’objets, de sorts et de compétences, les canaux avec mot de passe/couleur/écoute, la liste d’ignorés, le contenu de coffre mémorisé, l’inventaire local et de nombreux réglages graphiques et d’interface : zoom, cible verrouillée, affichage de l’or, 32 FPS, effets d’eau, météo, éclairage des décors, barres de statut et macros plein écran.

`GamePreferences` Java ne conserve qu’un sous-ensemble (volumes, luminosité, plein écran, VSync, quelques textes HUD et logs). Les macros, canaux, ignore-list, cache de coffre, verrouillage de cible, zoom et plusieurs options de rendu 1.68 n’ont pas d’équivalent persistant identifié. Une même installation ne retrouve donc pas le même état utilisateur après redémarrage, et certaines options natives qui changent directement l’interaction ou le rendu sont absentes plutôt que simplement présentées différemment.

## 121. Contrôle de combat : verrouillage double-clic et attaque forcée

Dans `MouseAction::Combat`, le client natif transforme un double-clic en verrouillage de cible lorsque `bLockTarget` est actif (`FreezeID`), distingue clic, double-clic et glisser, et réserve Shift à une requête d’attaque spéciale périodiquement limitée. Le clic droit identifie l’unité sous le curseur au lieu de déclencher le même chemin que le clic gauche.

Le Java sélectionne directement une cible au clic dans `MonsterInputHandler`, tandis que `ClickToMoveHandler` ignore Shift pour le déplacement. Aucun double-clic ne reproduit le verrouillage natif et aucune requête d’attaque Shift avec ses limites temporelles n’est générée. Le ciblage persistant, l’attaque forcée et le comportement clic droit/gauche ne sont donc pas équivalents, même si l’auto-combat Java conserve une cible en mémoire.

## 122. Musique et sons : moteur Java présent, sélection native non équivalente

`GameMusic` du client natif choisit une musique selon le monde, la zone, le donjon, la caverne, le boss, la tristesse ou les bruits ambiants. Il arrête/libère l’ancienne piste, charge la nouvelle en streaming, évite de relancer une piste identique et applique le volume musical sauvegardé. Le gestionnaire de sons traite séparément les effets d’animation, d’interface et de page, avec un volume d’effets distinct.

Le Java possède bien `SoundManager`, `MusicZoneBinaryIO` et des zones musicales rectangulaires chargées par `MainGameScreen`; il joue une ambiance en boucle et applique le volume de préférence. L’écart n’est donc pas une absence totale d’audio. En revanche, cette sélection dépend de fichiers de zones Java et de la dernière entrée contenant la case, alors que `GameMusic.cpp` encode aussi l’ordre de priorité des régions, les mondes, donjons, cavernes, boss, états de tristesse et bruits ambiants. Le Java ne démontre pas non plus l’équivalent de tous les sons natifs de page, d’interface et de transition. Une zone superposée, un boss ou un changement de monde peut donc choisir une piste différente ou aucune piste malgré la présence du moteur audio.


## 123. Pages privées : notification, réponse et bascule réseau absentes

Le client natif reçoit une page (`RQ_Page`), distingue au moins la page reçue, la réponse et l’utilisateur introuvable, joue éventuellement un son (`bPageSound`), ajoute le texte au backscroll puis peut répondre en renvoyant `RQ_Page` avec le destinataire. Le bouton de page inverse `bPageEnable` et envoie `RQ_TogglePage` au serveur ; ce n’est pas un simple filtre local.

Le Java ne présente pas de mode page, de destinataire courant, de réponse à une page ou de bascule réseau équivalente dans `GameChat`. Une notification privée ne peut donc pas déclencher le son, le type de message, le routage retour ou le refus « utilisateur introuvable » du client 1.68 ; elle est au mieux traitée comme du texte général.

## 124. Livre de sorts : état de liste serveur contre catalogue statique

`RQ_SendSpellList` transmet un marqueur de mise à jour, le mana courant et maximum, puis pour chaque sort l’identifiant, le type de cible, le coût de mana, la durée, le niveau, l’élément, le type mental/physique, l’icône, la description et le nom. Le client reconstruit alors le livre à partir de cette réponse et non d’une liste complète supposée connue localement.

Le Java charge `SpellRegistry` et `SpellData` depuis ses définitions, puis utilise `SpellBook` et `SpellCastingService` avec les sorts appris du joueur. Il n’existe pas de remplacement réseau de la liste, du mana ou des métadonnées par personnage. Un sort appris, retiré, modifié, renommé ou temporairement indisponible côté serveur peut donc rester visible et lançable selon l’état local Java.

## 125. Offres d’apprentissage : prérequis et droits serveur absents

Les réponses natives `RQ_SendTrainSkillList` et `RQ_SendTeachSkillList` incluent les points disponibles, le droit d’apprendre (`canHave`), l’identifiant, le niveau courant ou maximum, le prix, le nom, et pour l’enseignement les prérequis, les points requis et l’icône. L’offre affichée est donc recalculée par le serveur pour le personnage et le NPC au moment de la demande.

Le Java construit `TrainingCatalog` et des `LearnScreen.TrainingOffer` localement, puis `SkillService` modifie directement les points, l’or et les compétences. Le modèle d’offre Java ne transporte pas l’ensemble des champs natifs (notamment prérequis textuels, icône et refus serveur par entrée) et ne reçoit pas de liste recalculée. Une compétence temporairement interdite ou un coût modifié côté serveur peut ainsi être proposée ou validée différemment.

## 126. Feuille de personnage : champs natifs non modélisés à l’identique

`RQ_GetStatus` met à jour séparément HP/maximum, mana/maximum, expérience 64 bits, AC de base et effective, huit statistiques de base/effectives (force, endurance, agilité, volonté, sagesse, intelligence, chance), points de statistiques, niveau, points de compétence, poids/poids maximum, karma, vraie vie maximum, puis six puissances et six résistances élémentaires. Le client rafraîchit ensuite simultanément la feuille de personnage et l’équipement.

Le modèle Java `Stats` contient surtout les valeurs effectives de force, dextérité, endurance, intelligence, sagesse, HP, mana, XP, points et karma ; il n’expose pas le couple base/effectif natif, l’AC, la volonté, la chance, le poids ou la vraie vie maximum comme champs équivalents. Les résistances et puissances sont reconstruites via des maps de buffs/flags et non reçues dans un snapshot serveur. La feuille Java peut donc afficher une valeur plausible sans reproduire les distinctions et recalculs du statut 1.68.

## 127. Classement PvP : compteurs et séries serveur absents

Le client 1.68 possède un paquet `RQ_GetPvpRanking` dédié, acquitté séparément, qui transmet sept valeurs : morts totales, kills totaux, morts/kills courants, meilleure série, série courante et points PvP. `PvpRanking` les affiche dans une interface dédiée ; ces valeurs ne sont pas déduites des événements graphiques de combat.

Le Java possède un indicateur de mort PvP utilisé par `DeathPenaltyService`, mais aucune structure de classement équivalente ni les sept compteurs reçus par le client natif. Les kills, séries et points PvP ne sont donc ni synchronisés ni affichables selon le même état persistant ; une mort ou un kill local ne produit pas automatiquement le résultat de classement 1.68.

## 128. Objets au sol : synchronisation initiale et identité d’instance

À l’entrée en jeu, le client natif demande explicitement `RQ_GetNearItems`, puis reçoit les objets proches avec coordonnées, type, identifiant, lumière, statut et HP/état associé. Il renvoie cette demande après certaines transitions afin de rétablir le voisinage réel ; le joueur et les objets sont traités différemment selon l’identifiant reçu. Le ramassage et la disparition reposent donc sur une instance serveur, pas uniquement sur le nom d’un item à une case.

Le Java fait apparaître les objets au sol via `GroundItemManager.dropItem`, `spawnFromLoot` et `spawnCorpse`, puis `GroundItemClickHandler` appelle directement `pickUpAt` et ajoute l’item à l’inventaire. Il n’existe pas de demande `GetNearItems`, de réponse d’existence, de lumière/statut d’instance ni de resynchronisation serveur après entrée ou changement de monde. Un objet déjà ramassé, concurrent ou modifié par le serveur peut donc rester visible ou être ajouté localement.

## 129. Coffre : contenu et déplacement d’instances autorisés par serveur

Le protocole natif sépare `RQ_ShowChest`, `RQ_HideChest` et `RQ_ChestContents`. Le contenu reçu comprend, pour chaque objet, apparence, identifiant d’instance, identifiant de base, quantité et charges. Les dépôts/retraits déclenchent ensuite des requêtes distinctes et la réponse peut changer l’apparence d’un objet ou du joueur via `RQ_DepositObject`; l’interface n’est visible et valide que selon l’état reçu.

Le Java ouvre `ChestService` à partir d’une définition locale de coffre, tire le butin, ajoute directement l’or/les items et applique un cooldown local. Il ne transporte pas le contenu structuré du coffre, les identifiants d’instance, les charges reçues, ni les requêtes de dépôt/retrait avec ACK. Deux joueurs ou une fermeture serveur ne peuvent donc pas converger vers le même coffre Java.

## 130. Effets temporaires : rendu similaire, autorité différente

Le client natif ne déduit pas l’activation d’un buff de l’animation du sort : `RQ_CreateEffectStatus` impose l’identifiant, le temps déjà écoulé, la durée totale, l’icône et la description ; `RQ_DispellEffectStatus` supprime ensuite l’effet par identifiant. Le compteur visuel reste ainsi aligné sur le temps serveur et peut être remplacé ou dissipé sans relancer le sort localement.

Le Java possède bien `Player.ActiveBuff` et affiche icône, description et durée dans `PlayerHUD`, mais crée et expire ces buffs directement depuis `SpellEffectManager`/`Player`. Il manque le flux de création, remplacement et dissipation par identifiant serveur ; un buff Java peut donc rester actif après une dissipation distante, ou expirer à un instant différent malgré un rendu visuellement comparable.

## 131. Mode rapide : opération native distincte du multiplicateur Java

Le protocole 1.68 réserve `RQ_PlayerFastMode` à une opération dédiée avec son propre profil d’ACK. Le client natif distingue ainsi un mode de déplacement rapide contrôlé par le protocole des mouvements ordinaires et des animations accélérées ; ce n’est pas seulement une valeur graphique locale.

Le Java ne possède pas de gestionnaire de `RQ_PlayerFastMode`. Sa vitesse effective est le produit de multiplicateurs de buff et de `gmSpeedMultiplier`, ce dernier étant modifiable par une commande locale `.speed`. Le mode rapide natif, son activation/refus et sa synchronisation avec le serveur ne sont donc pas représentés par le même état.

## 132. Version client/serveur : authentification et patch conditionnel absents

Le client natif embarque une version numérique (`Version::GetVersion`), l’envoie pendant l’inscription/entrée, demande ou reçoit la version serveur, puis envoie `RQ_AuthenticateServerVersion`. Une version incompatible prend une branche d’erreur dédiée ; si le serveur indique une version supérieure, le client peut lancer `WebPatchUpdate` avant de poursuivre. L’entrée en jeu est ensuite séquencée avec `RQ_PutPlayerInGame`, `RQ_GetNearItems` et `RQ_FromPreInGameToInGame`.

Le Java ne possède pas cette négociation de version serveur, cette authentification de protocole ni ce patch conditionnel avant de construire la partie. Il charge ses catalogues, cartes et personnages locaux directement. Une incompatibilité de données ou de version ne produit donc pas le refus contrôlé et la séquence de resynchronisation du client 1.68.

## 133. Exploration de carte : le Java affiche une carte statique, sans mémoire RTMap

Le client natif possède une mémoire d’exploration par monde dans `CSaveGame` : `m_uchRTMap[10][192][192]`. Elle est initialisée, chargée et sauvegardée dans le fichier de compte/personnage. `SetRTMapVal` marque une cellule visitée et `TFCSocket.cpp` l’appelle lors de la mise à jour de la position du joueur. L’exploration est donc un état persistant, distinct de la simple carte graphique.

Le Java charge bien l’image originale via `OriginalRtMap` et applique seulement le masque graphique `GUI_RTMapMask` dans `GuiWorldMap`. `MapScreen` reconstruit la vue autour de la position courante ; je ne trouve aucun tableau de cellules visitées par monde, ni lecture/écriture de cet état dans `PlayerStateStore`. Conséquence : la carte Java ne reproduit pas le brouillard/progression d’exploration persistante du client 1.68 et son état ne survit pas à un changement de session.

## 142. Modèles 3D et sons attachés aux unités : rendu 2D Java

`VisualObjectList` natif réserve un tableau `VObject3D` et instancie des `Sprite3D` pour de nombreuses unités (par exemple Beholder, Wizard, Goblin, Mummy, Demon, Minotaur, Rat, Bat, Spider et Skeleton). Chaque modèle possède ses dimensions, directions, frames et parfois plusieurs sons d’attaque, de douleur ou de mort dans `Object3DSound`. Le type d’objet reçu détermine donc non seulement une apparence, mais aussi un chemin de rendu et une table sonore dédiée.

Le Java rend les monstres, NPC et objets avec `EntityAnimationsBase`, `PlayerAnimations`, `ObjectRenderer` et des textures 2D ; aucune classe ou branche `Sprite3D`/`Type3D` équivalente n’a été trouvée dans le chemin de jeu. Les sons Java sont associés à des actions ou définitions de sorts/monstres, pas à une table native de variantes par modèle 3D. Les unités natives utilisant ce chemin peuvent donc avoir une silhouette, une orientation, des frames et des sons différents dans le Java.

## 143. Ampleur du registre 3D : 153 chargements natifs, zéro référence Java

Le balayage de `VisualObjectList.cpp` trouve 153 appels `LoadSprite3D`, en plus des enregistrements d’objets et des entrées `Object3DSound`. Ce n’est donc pas un cas isolé limité à un monstre ou à un décor particulier : le client 1.68 dispose d’un registre substantiel de modèles 3D avec leur cycle de libération et leurs sons.

Le balayage de `src/main/java` ne trouve aucune référence `Sprite3D`, `LoadSprite3D`, `Type3D` ou `Object3DSound`. L’écart identifié à la section précédente est ainsi structurel et mesurable, pas seulement une différence de nommage dans une définition.

## 144. Capture vidéo native : pipeline optionnel absent du Java

Le client natif embarque `NMVideoCapture` avec `StartCapture`, `StopCapture` et `CaptureFrame`, ainsi que les callbacks `StartCapture`/`EndCapture` qui affichent l’état de la capture dans le chat. Le pipeline est parfois désactivé ou ses macros sont commentées dans cette révision, mais le code de capture par frames et son intégration à la boucle de rendu existent bien dans le client 1.68.

Le Java ne contient aucune classe ou branche de capture vidéo, aucun état de session vidéo et aucune commande équivalente. Même en tenant compte du caractère optionnel du chemin natif, il n’existe donc pas de capacité Java correspondante pour enregistrer les frames du jeu ou afficher le début/la fin de cet enregistrement.

## 145. Éditeur de chat : historique, destinations et commandes locales différents

Les deux clients possèdent un historique et un presse-papiers : le Java limite son historique à 50 entrées et son texte à 256 caractères, tandis que `ChatterUI` natif conserve jusqu’à 128 textes envoyés. Le natif parcourt cet historique avec son itérateur `rollbackTyped` et réinitialise la saisie au-delà des extrémités ; le Java utilise un index borné dans sa `List<String>`. Les deux offrent copie/collage, mais le natif injecte les caractères dans l’entrée active, alors que le Java normalise les retours ligne en espaces et tronque le collage à la longueur maximale.

Le natif possède trois états de destination explicites : `SendToGame`, `SendToPage` et `SendToChannel`. La touche Entrée construit alors des paquets et des préfixes différents selon le mode ; les boutons de page et de canal changent directement `textInputState`. `GameChat` Java n’a qu’un `submitHandler` et une zone d’entrée générale : aucun éditeur équivalent ne maintient une destination page/canal ni le routage de saisie correspondant dans le widget.

Enfin, `ChatterUI` intercepte localement des commandes de test/diagnostic (`.fog`, `.rain`, `.snow`, `.star`, `.dagger`, `.spell`) et les transforme en actions visuelles ou effets de sort sans les envoyer au jeu. La recherche du Java ne trouve pas ce parseur de commandes locales dans `GameChat` ; son traitement de commandes est séparé du widget et ne reproduit pas cette table de commandes client 1.68. Une saisie identique peut donc modifier uniquement le rendu natif, mais être envoyée ou traitée différemment par le Java.

## 211. Autocomplétion Java d’une commande GM sans équivalent natif

`GameChat.autocomplete()` reconnaît spécifiquement les entrées correspondant à `\.summon\s+(npc|monster)\s+(.+)`. Lorsque le curseur est en fin de ligne, la touche Tab demande au fournisseur Java les noms correspondant au préfixe, remplace le texte et fait défiler les candidats aux pressions suivantes ; l’opération est enregistrée dans l’annulation.

Aucune logique d’autocomplétion, de fournisseur de candidats ou de remplacement par Tab n’est présente dans `ChatterUI.cpp` ou dans les contrôles de saisie du client 1.68. Le natif possède des commandes locales et des macros, mais l’utilisateur doit saisir leurs textes manuellement. Le Java ajoute donc une assistance de commande spécifique aux GM, avec un comportement de Tab et de cycle de candidats absent du client original.
## 146. Macros clavier globales : registre configurable absent du Java

Le client 1.68 installe un registre de macros globales (`Custom.gMacro`) et laisse le launcher remplacer les touches de plusieurs actions. Les raccourcis par défaut couvrent notamment inventaire (`Ctrl+I`), personnage (`Ctrl+S`), mode attaque (`Ctrl+C`), chat (`Ctrl+L`), groupe (`Ctrl+G`), sorts (`Ctrl+P`), macros (`Ctrl+M`), options (`Ctrl+O`), carte (`Ctrl+W`), commerce (`Ctrl+T`), capture d’écran (`Ctrl+H`), redimensionnement du chat (`Ctrl+A`) et identification des objets (`Ctrl+V`). La configuration du launcher peut fournir les touches effectives et activer/désactiver plusieurs de ces entrées.

Le Java ne contient aucun registre `gMacro`, `VKey`, `AddNewMacro` ou équivalent dans le chemin de jeu. Ses touches sont traitées localement par les écrans et handlers LibGDX ; elles ne constituent pas une table de raccourcis reconfigurable et centralisée. Il n’y a donc pas d’équivalence fonctionnelle pour remapper globalement une action, pour conserver cette configuration du launcher, ni pour garantir qu’une même combinaison ouvre la même fenêtre quel que soit l’écran courant.

## 213. Commandes natives `!` de diagnostic absentes du chat Java

En plus des commandes de test visuel en point, `main2.cpp` traite localement plusieurs commandes préfixées par `!` : `!AFK` gère l’état et le message d’absence, `!POS` active l’affichage de la position sur la carte, `!Clear` vide le backscroll, `!FPS` bascule l’affichage du compteur d’images et `!Pvp stat` affiche le classement PvP. Ces commandes sont consommées par le client et ne deviennent pas des messages de jeu ordinaires.

Le Java ne possède pas de parseur `!` équivalent dans `GameChat` ou `GmCommandProcessor` : ce dernier n’intercepte que les lignes commençant par `.`. Le Java ne peut donc pas activer/désactiver localement l’AFK natif, l’affichage de position/FPS, ni vider le backscroll avec les mêmes commandes ; ses overlays de coordonnées/debug et ses statistiques sont déclenchés par d’autres contrôles et ne suivent pas la même syntaxe ni le même état persistant.

## 212. Jeu de commandes GM local différent

`GmCommandProcessor` Java intercepte toute ligne commençant par un point et exécute localement des commandes telles que `.level`, `.xp`, `.gold`, `.hp`, `.mana`, les modifications d’attributs/points, `.teleport X,Y,Z`, `.learn`, `.repair`, `.collision`/`.noclip` et `.speed`. Ces commandes modifient directement `Player`, l’inventaire, les compétences, la position ou la collision, puis affichent un message local ; elles ne passent par aucune requête serveur.

Le client natif 1.68 ne possède pas ce parseur GM général. Ses commandes locales documentées dans `ChatterUI` sont limitées aux tests visuels (`.fog`, `.rain`, `.snow`, `.star`, `.dagger`, `.spell`) et ne modifient pas les niveaux, l’or, les points, les compétences, la réparation ou la position persistée du personnage. Une chaîne GM identique n’a donc pas la même portée : le Java expose une console d’administration locale de gameplay que le client original ne fournit pas.

## 147. Raccourcis effectivement câblés : plusieurs combinaisons ne déclenchent pas la même action

La table native associe par défaut `Ctrl+S` au personnage, `Ctrl+T` au commerce, `Ctrl+G` au groupe, `Ctrl+P` aux sorts, `Ctrl+O` aux options et `Ctrl+W` à la carte. Le code Java câble au contraire `Ctrl+T` sur `Statistics`, `Ctrl+P` sur `SpellBook`, `Ctrl+I` sur `Inventory`, `Ctrl+Q` sur `QuestScreen` et `Ctrl+W` sur la carte. Il n’expose pas les ouvertures globales natives du personnage, du groupe, du commerce ou des macros sous ces mêmes raccourcis ; `Ctrl+T` est notamment une divergence directe et observable (commerce natif contre statistiques Java).

La boucle Win32 native traite séparément `WM_KEYDOWN`, `WM_CHAR`, `WM_UNICHAR`, l’état Ctrl/Shift et les messages système ; `F10` est explicitement neutralisée et les touches passent par le registre de macros avant la distribution des actions. Le Java distribue les événements LibGDX à `GuiManager`, `GameInputHandler` et aux handlers d’écran, avec des fonctions de debug/rechargement locales (`F1`, `F2`, `F3`, `F8`, `F9`, `F11`) qui n’appartiennent pas au comportement joueur du client 1.68. La même touche peut donc ouvrir une interface, activer un outil de développement ou ne rien faire selon le client.

Enfin, le Java utilise `S`/`W`/`A`/`D` et les flèches pour le déplacement dans `GameInputHandler`, tandis que le natif réserve ces combinaisons Ctrl aux macros et maintient un état clavier distinct. Sans couche de priorité de macros équivalente, une combinaison modifiée peut à la fois rester visible comme touche de mouvement et ne pas suivre le verrouillage/consommation d’événement du client natif.

## 148. Perte de focus et Alt-Tab : acquisition des périphériques absente du Java

À la sortie et au retour d’Alt-Tab, le client natif traite `WM_ACTIVATE` : il libère puis réacquiert le clavier et la souris DirectInput, restaure les surfaces DirectDraw en plein écran, réinitialise l’état Ctrl et met à jour l’état de focus de l’application. Cette séquence évite qu’une touche reste considérée comme enfoncée et permet de reprendre le rendu après perte de surface.

Le Java ne possède pas cette séquence dans `MainGameScreen` : `pause()` est vide, `resume()` ne réacquiert aucun périphérique et aucune restauration de surface ou remise à zéro globale des modificateurs n’est effectuée. LibGDX gère la fenêtre, mais cela ne reproduit pas les transitions d’état du client 1.68. Après une perte de focus, le déplacement, les touches Ctrl/Shift ou le rendu peuvent donc reprendre avec un état différent.

## 149. Conversion souris → grille : validation native par VirtualGrid absente

`DirectXInput::SetVirtualGrid` configure une grille virtuelle dérivée de la taille de la fenêtre. `GetStatus` convertit le déplacement souris en coordonnées de case avec les offsets historiques du client (`+48`, `-8`) et ne renvoie la case que si `VirtualGrid` l’autorise ; une case hors grille ou non valide devient `(0,0)`. Le clic natif est donc filtré et quantifié avant d’atteindre les actions d’interface ou du monde.

Le Java convertit principalement les coordonnées écran par `camera.unproject`, puis divise par `GRID_W`/`GRID_H` dans les handlers. `ObjectClickHandler` cherche ensuite une position d’objet et applique sa distance d’interaction, mais ne possède pas le même masque `VirtualGrid`, les mêmes offsets ni le même rejet précoce de case invalide. À bord de la fenêtre, sur une zone non marchable ou lors d’un zoom/redimensionnement, le clic peut ainsi être attribué à une case ou à une interaction différente du client 1.68.

## 150. File d’événements souris : traitement différé et synchronisé absent

Le client natif possède `UIMouseEvent`, une file globale protégée par verrou qui mémorise séparément `LeftMouseDown`, `LeftMouseUp`, `RightMouseDown`, `RightMouseUp`, molette et `Drag`, avec la position de chaque événement. Le thread de souris ajoute les événements puis `RootBoxUI` les résout dans la boucle d’interface ; l’état de l’interface peut donc consommer, ordonner ou différer un événement sans exécuter directement le code du périphérique.

Le Java transmet les événements LibGDX directement aux `InputMultiplexer`, écrans et handlers (`touchDown`, `touchUp`, `scrolled`) sans file centrale `MousePos` protégée ni phase de résolution séparée. Un clic Java est donc traité dans le contexte immédiat de la frame et de l’ordre courant des processors ; il n’existe pas de contrat natif équivalent pour mettre en file un drag/molette, le rejouer après verrouillage d’une fenêtre ou garantir le même ordre entre thread d’entrée et rendu.

## 152. Défilement des listes : contrôleur natif commun absent

`ScrollUI` est un contrôleur réutilisable du client 1.68 pour les listes de compétences, coffres, canaux, utilisateurs, options et historique de chat. Il possède une position bornée par la taille de liste, des régions distinctes haut/bas/barre libre, un bouton de curseur, le glisser du curseur, la répétition d’un bouton maintenu (`pressUp`/`pressDown`/`nextPress`) et un réglage de son de défilement. Les listes natives partagent donc les mêmes règles de déplacement et de rafraîchissement via `ScrollChanged`.

Le Java répartit le défilement entre `GameChat`, `GuiInventory`, `GuiListScreen`, `ShopScreen`, `OptionsScreen` et d’autres écrans, avec des pas et des bornes propres à chaque classe. Certains widgets acceptent la molette, d’autres des boutons de page ; le curseur graphique de boutique est notamment un bouton sans action de déplacement. Aucun contrôleur de jeu commun ne reproduit simultanément le clic haut/bas, le maintien avec répétition, le glisser de barre, le clamp natif et le son optionnel. Une même interaction de molette, de maintien ou de drag ne produit donc pas un comportement homogène 1.68 dans les différentes fenêtres Java.

## 151. Erreurs critiques : boîte modale native et interruption du flux absentes

Le client natif centralise les erreurs critiques dans `AppManagement::SetError` et `WarningBox`. Il restaure la vue DirectX avant d’appeler une `MessageBox` Windows au premier plan ; certaines erreurs de chargement ou de périphérique déclenchent ensuite une exception/fermeture, au lieu de laisser le monde continuer avec un état partiellement initialisé. Les erreurs de ressources ont donc un point de sortie et une présentation utilisateur déterminés.

Le Java mélange exceptions `GameException`, journalisation, messages d’écran et `catch` silencieux. Plusieurs composants (`GameChat`, `GuiWorldMap`, `PlayerStateStore`, `GuiPlayerPart`, gestionnaires de ressources) absorbent l’erreur ou utilisent un repli local, tandis que d’autres écrans interrompent leur chargement. Il n’existe pas de boîte modale critique globale qui restaure la vue, bloque l’entrée et impose la fermeture/reconnexion selon la même politique ; une ressource défectueuse peut donc produire un écran incomplet ou une partie encore active là où le client 1.68 aurait arrêté le flux.

## 153. Profils de polices et fallback : sélection native non reproduite

Le client natif crée des profils séparés pour `Tahoma` (système), `Verdana` (informations), `T4C BeaulieuxV2` (texte principal et boutons) et `Arial` (nouvelle interface), puis applique des tailles distinctes au menu, aux descriptions, aux compétences, aux messages système et aux boutons. L’option `bHighFont` modifie les tailles valides de ces profils ; les métriques sont ensuite utilisées par `FormatText` pour calculer les retours à la ligne et les hauteurs d’interface.

Le Java utilise principalement `T4CBeaulieu`, `JetBrains Mono`, `Verdana`, `Tahoma`, `HATTEN` et `Chewy`, avec des fallbacks différents selon l’environnement. `FontManager` génère les glyphes FreeType avec un supersampling et un filtre texture, mais ne conserve pas les mêmes profils natifs (notamment `T4C BeaulieuxV2`/Arial), ni la sélection `bHighFont` par famille et taille. Les mesures de texte, les retours à la ligne et la hauteur des boutons/dialogues peuvent donc changer même lorsque la chaîne et la taille logique affichée semblent identiques.

## 154. Résolution des palettes de sprites : algorithme V2 générique absent

`CV2PalManager::GetPal` charge la base compressée `V2ColorI.dpd`, recherche la meilleure entrée par identifiant de sprite et numéro de palette, distingue les numéros à un ou deux chiffres et conserve un fallback de palette de référence. `CV2Sprite` transmet ensuite cette palette à la décompression du sprite, applique la couleur transparente et convertit chaque index de pixel en couleur de surface. Une même ressource V2 peut donc changer d’apparence à partir d’un simple numéro de palette sans fichier sprite distinct.

Le Java résout principalement des noms PNG/mappings et ne possède pas de gestionnaire générique équivalent à `GetPal(spriteId, paletteNumber)`. `SpriteLoader` ne régénère que quelques variantes spécialisées (par exemple les palettes d’energy-ball) ou des régions masquées ; le reste dépend de fichiers déjà colorisés, de suffixes et d’overrides. Les changements de palette natifs, leur fallback et leur invalidation de cache ne sont donc pas reproduits pour l’ensemble des sprites 1.68.

## 155. Flags d’effets V2 : équivalents Java seulement partiels

Le rendu natif transporte les effets dans `V2SPRITEFX.dwFX` à chaque blit : miroir horizontal, absence de correction, clipping, contour, dithering, ajustement plein écran et `FX_NODRAW`. `DrawSpriteNSemiTrans` reçoit en plus un niveau alpha explicite ; les chemins `TransAlphaImproved` et `TransAlphaGlow` réalisent la semi-transparence et les halos avec détection des pixels voisins. Ces flags sont utilisés aussi bien par les fenêtres (`FX_NOCORRECTION`) que par les ombres, barres de vie, objets superposés et effets sélectionnés.

Le Java possède des équivalents ciblés pour le flip, le contour de survol, l’alpha d’occlusion et certains masques de sorts, mais pas un objet d’effets V2 générique propagé par tous les sprites. Aucun chemin commun ne reproduit les flags `FX_DITHER`, `FX_NOCORRECTION`, `FX_FIT2SCREEN` et `FX_NODRAW`, ni les algorithmes natifs de glow/semi-transparence par voisinage ; l’alpha LibGDX reste un blend RGBA général. Les ombres, interfaces corrigées, sprites partiellement masqués et effets lumineux peuvent donc avoir une apparence différente, être recadrés différemment ou rester visibles lorsqu’un dessin natif aurait été supprimé.

## 156. Fondu global de transition : overlay local Java au lieu de l’état de palette natif

Le client natif possède `PalManagement`, qui protège l’état de fondu par section critique, expose `FadeToBlack`, `FadeOut` et `inFadeOut`, et modifie la palette visible jusqu’au noir. Les transitions de monde utilisent cet état partagé avec `World.SetFading`/`World.RealFading` : le fondu n’est donc pas seulement un widget, il peut verrouiller le déroulement visuel pendant le changement de carte et être interrogé par la boucle principale.

Le Java dispose d’un fondu de durée limitée dans `GuiMapZoneDisplay` et de quelques overlays locaux, mais aucune gestionnaire global de palette/transition avec état `inFadeOut`, verrouillage et coordination avec le changement de monde. Un changement de carte, une reconnexion ou une transition forcée peut donc afficher le nouvel état directement ou avec une temporisation propre à l’écran, sans garantir le même moment de masquage, de déverrouillage des contrôles ou de reprise du rendu que le client 1.68.

## 157. Expiration des buffs : clignotement natif sous 15 secondes absent

`EffectStatusUI` natif conserve pour chaque effet un identifiant, une durée initiale, une échéance, une icône et une description. Lorsqu’il reste au plus 15 secondes, l’icône est masquée pendant les 300 premières millisecondes de chaque seconde ; la jauge continue parallèlement à diminuer et les effets expirés sont retirés par `CalcEffectInfo`. Les effets infinis suivent un chemin séparé sans échéance.

Le Java conserve bien des `ActiveBuff`, affiche une icône et une barre de durée dans `PlayerHUD`, et traite les buffs infinis. En revanche, `renderActiveBuffs` dessine l’icône à chaque frame sans la phase de clignotement native sous 15 secondes. La lisibilité et le signal d’urgence avant expiration diffèrent donc, même lorsque la durée et l’icône du buff sont correctement disponibles.

## 158. Animation des widgets : avancement par dessin natif contre durée Java

`AnimUI` natif stocke une liste de `GraphUI`, dessine `frames[currentFrame]`, puis incrémente l’index à chaque appel de `Draw` ; à la fin de la liste il revient à zéro. `Stop` remet explicitement l’index à 0 et l’animation dépend donc directement de la cadence effective de la boucle de rendu 1.68 (avec éventuellement l’alpha demandé par la frame). Ce mécanisme est utilisé par les séquences d’interface composées de sprites successifs.

`GuiAnimatedSprite` Java fait au contraire progresser ses frames selon un temps de frame (`frameTime`) accumulé dans `update`, indépendamment du nombre de dessins effectués. Une pause, un ralentissement ou une variation de FPS n’a donc pas la même incidence : le natif peut ralentir ou accélérer l’animation avec ses appels de dessin, tandis que Java cherche à conserver une durée temporelle. Les séquences UI ne peuvent pas être considérées identiques sans une table de cadence et un mode d’horloge communs.

## 159. Curseurs de réglage : pas et flèches natives absents du `GuiSlider`

`SliderUI` natif travaille sur une plage entière (`minRange`/`maxRange`) avec un `step`. Un clic dans la piste arrondit la position au pas, le glisser conserve ce quantificateur, les flèches gauche/droite déplacent d’un pas et bornent la valeur ; chaque changement notifie un `EventVisitor` et peut jouer un son de pression/relâchement. Le curseur est donc utilisable aussi bien par drag que par incrément discret.

`GuiSlider` Java convertit directement la position en valeur continue normalisée `[0,1]`. Il ne possède ni plage entière, ni pas, ni boutons fléchés, ni notification conditionnée par changement, ni sons de contrôle. Les réglages Java peuvent ainsi prendre des valeurs intermédiaires ou ne pas offrir les incréments clavier/clic du client 1.68, même lorsque la largeur et le thumb semblent identiques.

## 160. Interaction répétée avec le même PNJ : temporisation native de 5 secondes absente

Dans `MouseAction::Talking`, le client natif mémorise `TalkToID` et `TalkTime`. Un clic sur le même PNJ ne renvoie pas immédiatement une nouvelle demande : il faut changer de cible ou attendre plus de 5 secondes ; l’identification et la recherche de distance sont protégées par le verrou de la liste d’objets avant l’envoi. Cette temporisation limite les répétitions de dialogue dues aux clics/double-clics.

Le Java route directement le clic vers `NPCManager.onClick`/`handleDialogClick` et ne possède pas de garde globale `TalkToID` + délai de 5 secondes dans `NPCInputHandler`. Les contrôles propres à chaque quête peuvent avoir leurs propres timers, mais ils ne remplacent pas cette protection commune : un clic répété sur le même PNJ peut donc rouvrir ou avancer un dialogue plus souvent que dans le client 1.68.

## 161. Émission des déplacements : garde native de 500 ms avant une nouvelle direction

Dans `TFCSocket`, après l’envoi d’une direction, le client natif mémorise `Try` et n’autorise une nouvelle requête de déplacement qu’après plus de 500 ms, avec en plus les conditions `!Move` et `!NeedRedraw`. Chaque direction (les huit `RQ_Move...`) réinitialise cette horloge. Cette garde sépare la cadence d’émission réseau de l’animation locale et évite d’empiler des demandes pendant qu’un déplacement est encore traité.

Le Java fait avancer `PlayerMovement` avec le `delta` de LibGDX et des réservations de pas ; `GameInputHandler`/`ClickToMoveHandler` n’ont pas de garde réseau `Try` de 500 ms, puisqu’il n’existe pas de requête de déplacement T4C à attendre. Une pression maintenue ou un changement rapide de direction peut donc produire une cadence locale continue, avec des transitions de pas et de blocage différentes de la séquence d’ordres du client 1.68.

## 162. Disponibilité globale du nom : requête serveur native absente

Dans l’état `TFC_CHOOSE_NAME`, la validation du client natif envoie le paquet 90 (`RQ_QueryNameExistence`, commenté `RQ_ChooseName` dans `TFCSocket.cpp`) avec le nom saisi. Cette requête possède son propre profil d’attente (`1000 ms`, trois essais) dans `Comm.cpp` : l’absence du nom du roster local ne suffit donc pas, car l’état partagé des autres comptes est décidé par le serveur.

Le Java de `LocalCharacterStore`/`CharacterCreationRules` normalise le nom, vérifie sa syntaxe et le compare uniquement aux personnages du fichier JSON local. Il n’existe pas de requête d’existence globale ni de réponse de conflit entre installations : deux comptes peuvent donc accepter localement le même nom, alors que le client 1.68 attendrait la réponse serveur avant de poursuivre la création.

## 163. Chaîne de démarrage : états logo, introduction, titre et crédits absents

`TFCFlag.h` et la boucle de `main2.cpp` définissent une chaîne d’états distincts : connexion, ligne d’introduction, logo, splash, introduction, écran de titre, menu, choix du personnage, avertissements et crédits. `TFCSocket.cpp` fait progresser ces états selon les temporisations, les touches et les réponses de connexion ; le menu peut revenir au choix de personnage, à l’introduction ou aux crédits avant d’entrer dans `TFC_PLAY`.

Le Java ne possède pas de screens équivalents pour le logo/splash, l’introduction, le titre ou les crédits. Le démarrage arrive directement sur la sélection locale (`CharacterSelectionScreen`), puis sur `CharacterLoadingScreen`/`MainGameScreen`. Il manque donc les transitions temporisées, les interruptions clavier, les avertissements de connexion et les retours au menu qui font partie du parcours fonctionnel du client 1.68, même si certaines images ou boutons de sélection ont été réutilisés.

## 164. Aides contextuelles du début de jeu : drapeaux `EventHelp` absents

En plus du manuel paginé, le client natif conserve un état global `g_EventHelp` protégé par verrou. Il active ou désactive séparément les aides « acheter une potion », « pas d’argent », « PV », « acheter une torche », « statistiques » et « compétences » ; l’état `OutSide` dépend aussi du monde, du niveau et de l’entrée dans une zone extérieure ou souterraine. Ces drapeaux sont modifiés après les mises à jour de statut et certaines transitions de musique/monde, puis consommés par l’affichage d’aide. Ils permettent donc de ne montrer une indication qu’au moment approprié et de la désactiver après progression du débutant.

Le Java ne possède pas de registre global équivalent, de verrou d’accès ni de consommateur d’aides conditionné par niveau, argent, PV, torches et zone. Les textes de PNJ et les messages Java peuvent informer le joueur, mais ils ne reproduisent pas les transitions « aide en attente → aide affichée → drapeau consommé » ni la coordination avec l’état extérieur du client 1.68.

## 165. Rafraîchissement de composition visuelle : `RQ_QueryPuppetInfo` absent

Le client natif envoie le paquet 68 (`RQ_QueryPuppetInfo`) lorsqu’il doit obtenir ou réactualiser l’apparence composée d’une unité. La réponse `SetPuppet` remplit les champs d’équipement du personnage (corps, pieds, gants, casque, jambes, armes et cape), puis `Puppet::SetPuppet` recalcule les couches, les parties masquées, les ailes/capes spéciales et les sprites correspondants. Cette requête permet de corriger l’apparence après une mise à jour distante, même si l’unité existait déjà dans `VisualObjectList`.

Le Java applique ses apparences depuis l’inventaire et les définitions locales (`PlayerAppearanceDefaults`, `PlayerAnimations`, `BodyPart`) et ne possède ni paquet 68, ni tampon `PuppetInfo` reçu, ni réinterrogation serveur d’une unité. Une modification d’équipement, de cape, d’ailes ou de composition effectuée ailleurs ne peut donc pas forcer le même rafraîchissement ; l’apparence locale peut rester ancienne ou être recomposée selon des règles Java différentes.

## 166. État joueur incomplet : foi, résistances, poids et permissions natives absents

`TFCPlayer` ne stocke pas seulement les attributs affichés : il possède aussi `Faith`/`MaxFaith`, `AC`, `Weight`/`MaxWeight`, les huit attributs incluant agilité et chance, des compteurs de morts/tueries/PvP, `Power` et `Resist` par élément, ainsi que `CanRunScripts` et `CanSlayUsers`. Ces champs sont mis à jour par les messages de statut et servent de contexte à l’interface, aux restrictions et aux actions autorisées ; ils sont distincts des simples bonus temporaires.

Le modèle Java `Stats`/`PlayerStateDto` ne contient pas de foi, de poids, d’agilité, de chance, d’AC, de puissances/résistances élémentaires ni de compteurs PvP. Les résistances et attributs rencontrés dans `Player` sont seulement des contributions de buffs, et les commandes GM Java ne constituent pas des permissions reçues par personnage. Une réponse de statut 1.68 portant ces valeurs ne peut donc pas être représentée fidèlement : HUD, prérequis, surcharge, dégâts élémentaires et actions administrativement interdites peuvent diverger.

## 167. Double-clic sur les objets narratifs : lettres et cartes ouvrables absents

`InventoryUI::InventoryGridEvent::LeftDblClicked` possède un chemin spécial avant l’utilisation générique. Les lettres d’Owain et de Crimsonscale sont interceptées, ainsi que la carte du labyrinthe ; le client restaure le drag et ouvre `RTHelp` dans le mode de page approprié. Il parcourt aussi `m_vImageDisplay` : tout objet dont le nom correspond à une image narrative connue ouvre `RTHelp::ShowSpecial` avec cette image. L’objet est ainsi consultable sans être consommé comme une potion ou envoyé comme une utilisation ordinaire.

Le Java contient des définitions et des traductions pour ces objets, mais `Inventory` délègue le double-clic à `ItemUseService`, qui traite les consommables/sorts et les charges ; aucun équivalent de `ShowSpecial`, de table d’images narratives ou de fenêtre modale de lecture n’a été trouvé. Une lettre, une carte ou un autre objet illustré ne déclenche donc pas le même affichage spécial et peut être traité comme un objet générique, rester sans action ou suivre une logique de consommation différente.

## 168. Validation de version du profil local : en-tête natif absent du JSON Java

`CSaveGame::bLoad` attend l’en-tête binaire exact `BL_V2SAVEGAME_V004` avant de lire, dans un ordre défini, l’inventaire d’interface, les trois familles de macros, les canaux, les ignorés, le coffre, les options et les dix couches de carte révélée. Si l’en-tête ne correspond pas, le client marque `InvalideSaveGame` et libère les collections au lieu d’interpréter le contenu comme un profil valide.

`PlayerStateStore.load` lit directement le JSON et le désérialise avec Gson, sans numéro de schéma, signature ou validation de structure équivalente. Les champs manquants sont acceptés avec leurs valeurs par défaut et les champs supplémentaires sont ignorés ; un fichier ancien, partiellement écrit ou provenant d’une autre version peut donc être chargé comme un état exploitable plutôt que rejeté et réinitialisé comme le profil 1.68.

## 169. Objet narratif `__OBJ_DARK_STONE` : identifiant natif sans définition Java

La liste d’objets du serveur 1.68 déclare `__OBJ_DARK_STONE` avec l’identifiant 41839. Le client peut donc recevoir cette instance dans l’inventaire et la traiter comme un objet serveur, notamment dans la séquence narrative du squelette/Gluriurl ; les scripts originaux la distinguent de la Heartstone et vérifient sa présence par l’identifiant d’objet.

Le Java contient les traductions et plusieurs textes de quête mentionnant la pierre sombre, mais aucune `ItemDefinition`/entrée `ItemRegistry` pour `dark_stone` ou l’identifiant 41839 n’a été trouvée. Une récompense, un drop ou un test de quête qui fournit réellement cet objet ne peut donc pas être résolu par l’inventaire Java comme un objet normal : il risque d’être ignoré, affiché sans définition ou de ne pas pouvoir être consommé/retiré correctement.

## 170. Sons d’unité et de parade : table native par famille non reproduite

Le client natif associe à chaque famille `Object3D` des sons d’attaque, de blessure, de mort et de parade (`onAttack`, `onAttacked`, `onKilled`, `onParry`), puis copie ces vagues dans l’objet lors de son initialisation. Les familles Beholder, Goblin, Mummy, Demon, Minotaur, Rat, Spider et Skeleton, entre autres, ont des tables spécifiques ; le son de blessure peut donc différer du son d’attaque ou de mort, et une parade possède un événement audio séparé.

Le Java possède `soundAttack`, `soundDeath` et `soundHit` dans `MonsterDef`/`BaseMonster`, et `BaseMonster.takeDamage` joue effectivement `soundHit` pour un dégât non létal. En revanche, cette valeur est un son unique configuré par définition, sans table `onAttack/onAttacked/onKilled/onParry` par famille ; aucun appel équivalent à `onParry` n’a été trouvé dans le chemin de combat Java. Les blessures peuvent donc produire un son, mais les variantes natives et le son dédié de parade ne sont pas reproduits.

## 171. Sons de blessure et de mort du joueur absents du chemin Java

Dans le client natif, le modèle joueur chargé par `VisualObjectList` (case 71, `LoadBodyPart`) associe explicitement aux états de l’unité des sons d’attaque, de blessure et de mort : `Male Hit 1/2`, `Male Dying 1/2`, ainsi que les variantes `Female Hit 1/2` et `Female Dying 1/2`. Ces sons sont enregistrés dans `Object3DSound[71]` avec les sons d’attaque.

Dans Java, `Player.attack()` joue bien `Whooshh 1/2/3` ou `Bow Attack.wav`, mais `Player.takeDamage()` ne déclenche aucun son de blessure. `handleDeath()` déclenche uniquement le callback de mort ou la réapparition locale ; aucune lecture des variantes homme/femme de blessure ou de mort n’est présente dans le chemin `Player`.

Impact : le joueur Java reste silencieux lorsqu’il reçoit des dégâts et lorsqu’il meurt, alors que le client 1.68 sélectionne ces sons selon le modèle/sexe visuel.

## 172. Curseurs d’action animés du client natif réduits à une icône Java

Au démarrage, le client natif charge des séquences dédiées pour `AttackCursor00..08`, `64kCursorBow-a..k`, `TakeCursor00..10` et `TalkCursor00/01`. `CombatCursor` sélectionne ces animations selon l’action (attaque au corps-à-corps, arc, prise ou dialogue) et le contexte de cible ; le curseur est donc un état visuel de l’action en cours, pas seulement une image fixe.

Le Java expose un curseur d’attaque statique et un `GameCursorManager` qui anime partiellement les curseurs d’arc et de dialogue. En revanche, aucune séquence `TakeCursor` équivalente n’a été trouvée, et l’attaque au corps-à-corps ne possède pas la séquence native `AttackCursor00..08`. Le gestionnaire Java ne relie pas non plus ces curseurs à la grille native de catégories et d’état hostile ; le feedback du pointeur reste donc incomplet et peut choisir une action différente dans les contextes `GET`, attaque et unité devenue hostile.

## 173. Gestion du cadavre : délai natif de son et de suppression absent

Lorsqu’une unité meurt, `VisualObjectList` conserve l’état `Killed`, mémorise `KillTimer` et bloque ses directions. Après au moins 500 ms, et seulement lorsque sa file de déplacement est vide, le client joue le son de mort puis convertit l’unité vers son type de cadavre. Pour les types de monstres concernés, `CurrentCorpseFrame` est ensuite avancé à travers les frames de cadavre ; l’objet n’est marqué supprimable qu’après environ 5 secondes (`KillType`/`DeleteMe`).

Le Java lance l’animation de mort du monstre, avec une durée codée d’une seconde, puis `shouldRemoveAfterDeath()` autorise la suppression dès que cette animation est terminée pour un monstre sans respawn. Il ne conserve pas un état natif `Killed` avec attente de file de déplacement, conversion différée vers un type de cadavre, progression des frames de cadavre et délai uniforme de cinq secondes. La durée de visibilité du cadavre et le moment du son de mort peuvent donc différer sensiblement.

## 174. Invisibilité logique non appliquée au rendu du joueur Java

Le client natif transmet l’état `bInvisible` dans `TFCObject` et les chemins de rendu `Icon3D` ajoutent alors `FX_NODRAW` : l’unité n’est effectivement pas dessinée, tout en restant présente dans la liste d’objets. Ce n’est pas uniquement une règle de combat ou de détection.

Le Java possède bien `Player.hidden` et `isHidden()` pour les profils de combat et une dissipation lors du déplacement, mais `Player.render`, `render` avec shader et `renderOcclusionReveal` appellent directement `PlayerAnimations` sans tester cet état. Une invisibilité active peut donc continuer à afficher le joueur (et son rendu d’occlusion), alors que le client 1.68 le masque graphiquement.

## 175. Invisibilité dynamique des NPC/monstres distants non représentée

Dans le client natif, `bInvisible` est un champ de `TFCObject`, donc il s’applique à toute unité reçue : joueur, NPC ou monstre. Les paquets d’unité peuvent changer cet état après la création et le moteur le transmet au renderer avec `FX_NODRAW`.

Le Java ne possède pas de champ d’invisibilité dynamique dans `BaseNPC`, `ScriptedNpc` ou `BaseMonster`, ni de test correspondant dans leurs méthodes de rendu. Les nombreuses définitions Java utilisant `@invisible` décrivent un sprite absent dès le spawn ; elles ne remplacent pas une unité normalement visible qui devient invisible puis redevient visible sur notification serveur. Ce changement d’état distant ne peut donc pas être reproduit pour les NPC/monstres.

## 176. Révélation furtive locale au déplacement non présente dans le client natif

`Player.tickSneakUpkeep()` Java vérifie à chaque mise à jour si le joueur caché se déplace, compte les témoins et appelle localement `StealthRules.staysHidden(...)`. Un déplacement peut donc révéler immédiatement le joueur selon un tirage local, sans réponse réseau.

Dans le client 1.68, `bInvisible` est un état reçu/actualisé sur l’unité ; le rendu applique ou retire `FX_NODRAW`, mais le client ne décide pas lui-même de la réussite de la furtivité à partir du mouvement et d’un compteur de témoins. Le Java peut ainsi révéler un joueur que le serveur natif laisserait caché, ou conserver l’état malgré une mise à jour serveur absente, en plus du problème de rendu décrit ci-dessus.

## 177. Identité et cycle de vie des effets enfants de sort non reproduits

Pour `RQ_SpellEffect`, le client natif lit `spellEffectId` et `spellChildId`, crée l’objet visuel avec ces identifiants, puis utilise `Follow` et `SummonID` pour relier projectile, impact et effet enfant. `Packet.cpp` contient une table explicite de variantes (boules colorées, météores, éclairs, soins, malédictions, etc.) vers leur animation enfant/base. Ces identifiants permettent de distinguer les effets simultanés et de les déplacer ou remplacer selon la réponse serveur.

Le Java crée des `SpellProjectile` et `SpellImpact` avec le nom visuel et une callback `onImpact`. Les `SummonRequest` servent à créer des entités de jeu, mais aucun identifiant d’effet/effet enfant reçu du serveur n’est conservé dans `SpellRenderer`, et il n’existe pas de table de rattachement `spellEffectId`/`spellChildId` pour mettre à jour ou retirer un effet déjà affiché. Deux effets visuellement identiques peuvent donc être fusionnés, terminés au mauvais moment ou rester indépendants lorsqu’une réponse native les traiterait comme parent/enfant.

## 178. Correction verticale des noms/bulles dépendante du modèle absente

Le client natif initialise `TFCObject::TextCorrection` avec des valeurs différentes selon le type visuel (`-20`, `-40`, `-80`, `-90`, `-120`, etc.). `VisualObjectList` ajoute ensuite cette correction à la position du nom et du texte de parole ; elle compense donc la hauteur réelle du sprite, y compris pour les modèles 3D et les familles de monstres.

Le Java utilise des offsets génériques dans `NameRenderer`, `NPCAnimations` et les renderers d’entités, sans table équivalente indexée par type visuel ni champ `TextCorrection` reçu/initialisé par unité. Un même nom ou une même bulle se place ainsi trop haut ou trop bas selon la taille du sprite, alors que le client 1.68 ajuste cette position par modèle.
## 179. Nom de guilde et couleurs d’unité dynamiques absents du rendu Java

Le client natif reçoit l’identité visuelle d’une unité avec plusieurs attributs séparés : `SetName(..., color)` met à jour le nom et sa couleur, tandis que `SetGuildName(..., color)` met à jour le nom de guilde et sa couleur. `TFCObject` conserve `NameColor`, `GuildName` et `GuildColor`, puis `DrawName` dessine le nom de guilde avec un objet texte distinct au-dessus ou au-dessous du nom de l’unité.

Dans le Java actuel, `NameRenderer` utilise une couleur de nom fixe et les entités exposent seulement un nom d’affichage localisé/statique. Aucun état `GuildName`/`GuildColor` par unité ni mise à jour équivalente n’existe dans le chemin de rendu des joueurs, PNJ et monstres. Le client Java ne peut donc pas reproduire un changement distant de nom, l’étiquette de guilde au-dessus de l’unité ou les couleurs d’identité propres à chaque unité.

## 180. Verrouillage de cible au double-clic absent

Dans `MouseAction.cpp`, lorsque l’option native `bLockTarget` est active, un double-clic en combat appelle `Objects.Lock(42)` et mémorise la cellule dans `FreezeID`. Les clics et déplacements suivants réutilisent alors cette cible au lieu de recalculer librement la cible sous le curseur ; le verrou est aussi annulé par les transitions de combat prévues par le client.

Le Java possède des cibles temporaires pour les sorts et des sélections d’interface, mais aucune option équivalente, aucun `FreezeID`/état de cible verrouillée et aucune branche de double-clic qui fixe la cible de combat. Un déplacement du curseur ou une nouvelle interaction peut donc changer la cible Java alors que le client 1.68 conserve celle verrouillée.

## 181. Sélection native par cellule occupée et empilement d’objets différente du hit-test Java

Le client natif reconstruit une grille d’identifiants à chaque affichage (`VisualObjectList::IdentifyAll`). Chaque cellule couverte par un objet reçoit son `ID` selon l’ordre d’empilement ; `GridID(x2,y2)` est ensuite la seule source de vérité pour parler, voir, utiliser, ramasser ou attaquer. `Objects.Identify` et `Objects.NoIdentify` modifient cet état de grille, et une même cellule peut donc sélectionner l’objet visuel placé au premier plan plutôt qu’un rectangle arbitraire.

Le Java teste principalement `isMouseOver` sur des rectangles/bounds d’entités, puis parcourt les listes de managers (`NameableEntityHandler`, `NPCManager`, `MonsterInputHandler`, `GroundItemManager`). Il n’existe pas de grille d’ID visuelle commune qui arbitre toutes les catégories et les couches avant le dispatch d’action. Un clic sur une zone où se superposent joueur, PNJ, monstre, objet ou décor peut donc sélectionner une entité différente, ou déclencher une action différente, du client 1.68.

## 182. Position étendue des objets multi-cellules non conservée par le Java

Le mode natif `See()` calcule le décalage entre le curseur et l’origine du monde, essaie plusieurs cellules voisines avec `Objects::RealPos`, puis enregistre le résultat dans `Objects::SetExtended`. L’identification d’un objet ne fournit donc pas seulement son ID : elle conserve aussi la partie de l’empreinte visuelle réellement pointée, information réutilisable par les actions `USE`, `GET` et `USE_ONSITE`.

Le Java possède des bounds locaux (`isMouseOver`) pour les PNJ, monstres, drops et herbes, mais aucune position étendue par objet ni équivalent de `RealPos`/`SetExtended`. Une interaction sur une décoration ou un objet couvrant plusieurs cases ne peut donc pas transmettre la même sous-position : le Java traite le rectangle entier comme une zone uniforme ou choisit seulement l’entité de sa liste.

## 183. Résultat serveur des flèches et trajectoire de raté non reproduits

Le client natif ne déduit pas le résultat d’un tir à partir de l’animation d’attaque. `RQ_ArrowHit` fournit le lanceur, la cible et le nouveau pourcentage de PV ; `ShootArrow` actualise alors les PV de la cible, calcule la direction du lanceur et déplace le projectile jusqu’à la cible. `RQ_ArrowMiss` fournit au contraire une position finale et un indicateur de collision ; `ShootArrow` extrapole la flèche hors écran lorsqu’elle rate sans collision, ou la fait terminer sur place lorsqu’elle rencontre un obstacle.

Le Java possède une animation d’attaque à l’arc et des formules locales (`CombatResult`, `CombatMath`), mais aucun cycle distinct `ArrowHit`/`ArrowMiss` avec position finale, collision, pourcentage de PV reçu et trajectoire extrapolée. Le rendu peut donc jouer une animation d’arc sans reproduire le point d’impact, le raté hors écran ou la mise à jour serveur du tir natif.

## 184. Passage dynamique d’une unité en état hostile et changement de curseur absent

Dans `VisualObjectList::SetEvil`, lorsqu’un sort offensif atteint le joueur, le client natif cherche l’unité attaquante, la convertit dynamiquement en catégorie monstre (`Friendly = VOL_MONSTER`) si nécessaire, lui assigne le curseur hostile et reconstruit sa grille d’identification. `SetAttack` applique une transition similaire lorsque l’unité attaque le joueur. L’état visuel et le type d’interaction peuvent donc changer sans recréer l’unité.

Le Java possède des drapeaux locaux `isHostile` pour certains PNJ et une classification statique des monstres, mais pas d’état d’hostilité dynamique partagé par toutes les unités, de conversion de catégorie ni de changement de curseur déclenché par l’attaque reçue. Une unité initialement neutre ou joueur ne change donc pas automatiquement de comportement d’identification comme dans le client 1.68.

## 185. Curseurs contextuels des objets du monde non mappés dans le Java

Le client natif possède une table `DefaultMouseCursor` par groupe d’objet : une porte ou un coffre reçoit `USE`, un PNJ `TALK`, un objet ramassable `GET`, une chaise ou une caisse non interactive `NONE`, et ces valeurs sont recalculées lorsque l’objet change de type ou d’état. Le curseur affiché dépend donc directement de l’objet sous la cellule et annonce l’action que le clic va dispatcher.

Le `GameCursorManager` Java ne propose que les modes globaux défaut, attaque, sort, arc et dialogue. `ObjectClickHandler` traite les objets au clic, mais aucun mapping objet → `GET`/`USE`/`NONE` n’est appliqué au pointeur. Une porte, un coffre, un drop ou un décor non interactif peuvent donc présenter le même curseur général avant le clic, contrairement au feedback contextuel du client 1.68.

## 186. Curseur dessiné dans la scène native contre curseur système Java

`CMouseCursor::DrawCursor` rend le sprite du curseur directement dans la surface DirectX, avec une zone de clipping à la taille de la fenêtre, des corrections d’offset liées au zoom et un calcul de direction pour les curseurs forcés. Le curseur natif suit donc les coordonnées de rendu du jeu et reste soumis aux mêmes transformations que les sprites de l’interface.

`GameCursorManager` Java fabrique au contraire un objet `Cursor` LibGDX (`Gdx.graphics.newCursor`) et le remet au système avec `Gdx.graphics.setCursor`. Il n’existe pas de dessin du curseur dans le `SpriteBatch`, ni de clipping/offset de scène équivalent. Le hotspot, la mise à l’échelle DPI, le passage hors fenêtre et le positionnement par rapport à un zoom peuvent donc différer du client 1.68 même avec la même image source.

## 187. File d’événements de déplacement/attaque native contre animation Java non mise en file

`TFCObject` natif possède une `MovingQueue<Deplacement>`. `SetAttack` et les mises à jour de mouvement y ajoutent les événements reçus ; la boucle de `VisualObjectList` les retire dans l’ordre, vérifie le nombre d’éléments en attente et ne supprime l’unité qu’une fois la file vidée. Plusieurs attaques ou corrections de position arrivant entre deux frames sont donc conservées et jouées séquentiellement.

Dans le Java, `PlayerAnimations` garde un seul état `attacking`/`attackFrame`, et `BaseMonster` ne démarre une attaque que si `!animations.isAttacking()`. Une nouvelle attaque ou animation reçue pendant la précédente n’est pas placée dans une file par unité ; elle est retardée par le cooldown ou ignorée par la garde d’animation. Sous rafale d’événements, l’ordre et le nombre de poses visibles divergent donc du client 1.68.

## 188. Direction d’attaque forcée par le serveur absente

`VisualObjectList::SetAttack` reçoit un paramètre `forcedDirection`. Lorsque celui-ci est non nul, il remplace la direction calculée entre l’attaquant et le défenseur avant l’ajout de l’attaque dans `MovingQueue`. Le client natif peut ainsi afficher une attaque dans une direction imposée par le serveur, même si la position locale de la cible donnerait une autre orientation.

Le Java appelle `PlayerAnimations.startAttack` avec l’angle courant du mouvement et `BaseMonster`/`MonsterManager` orientent l’unité par `faceToward` à partir des positions locales. Aucun champ ou événement de direction forcée n’est transmis au chemin d’animation. Une correction serveur, une attaque sans cible ou une direction imposée produit donc une pose différente dans le Java.

## 189. Orientation immédiate native et convention de direction différente

`SetDirection` natif travaille sur les offsets de grille `OX/OY`, produit les directions numériques 1 à 9 et accepte `bSetNow` : l’appel peut seulement calculer la direction destinée à une attaque en file, ou modifier immédiatement `Object->Direction`. Lorsque les deux offsets coïncident, le natif retombe explicitement sur la direction 1.

Le Java conserve des angles textuels (`000`, `045`, `090`, `135`, etc.) avec un booléen de miroir, et `faceToward` ne fait rien lorsque les deux positions sont identiques. Il n’a pas de distinction équivalente entre direction calculée pour une action future et direction appliquée immédiatement. Les cas de cible sur la même cellule, de correction d’offset ou d’événement d’attaque sans cible n’aboutissent donc pas à la même orientation.

## 190. Références d’attachement et suppression différée des objets visuels absentes

Le client natif maintient `TFCObject::Count` sur l’objet parent lorsqu’un effet, une invocation ou un autre objet visuel est ajouté avec `AttachID`. La suppression du parent est alors différée (`DeleteMe`) tant que ce compteur n’est pas revenu à zéro ; à la destruction de chaque enfant, le compteur est décrémenté. Cette règle s’applique aussi aux objets hors zone et aux objets en mouvement, afin d’éviter de supprimer le support avant ses visuels attachés.

Le Java sépare `SpellImpact`, `SpellProjectile` et `ChannelEffect` dans des listes de rendu qui expirent indépendamment par leurs frames, leur durée ou leur handle. `GroundItem` ne possède pas de `AttachID` ni de compteur de références visuelles. Il n’existe donc pas de garde générique empêchant la disparition d’une unité ou d’un objet parent tant qu’un enfant visuel natif lui est encore rattaché ; selon le moment de l’expiration, un effet peut rester orphelin ou disparaître avec son support, au lieu de suivre le cycle de vie coordonné du client 1.68.

## 191. Météo serveur et particules d’environnement absentes du runtime Java

Le client natif traite `RQ_WeatherMsg` avec un effet et un état `ON/OFF` séparés : pluie, neige et brouillard sont mémorisés distinctement. `Tileset` initialise ou arrête les états de transition, puis `CWeather::DrawRain` et `DrawSnow` tirent des positions de gouttes/flocons, les réutilisent entre les frames et ajoutent parfois des éclairs selon l’intensité. La météo reçue du serveur est donc un état global rendu au-dessus de la scène, pas seulement une option graphique locale.

Dans le Java, aucune classe, état ou boucle de rendu équivalente à `CWeather`, `RQ_WeatherMsg`, `DrawRain` ou `DrawSnow` n’a été trouvée dans le runtime de jeu ; les occurrences `rain`/`snow` concernent des noms d’objets ou de sorts. `GamePreferences` peut mémoriser des réglages, mais aucune pluie, neige, brouillard, éclair ou transition météo n’est générée dans `MainGameScreen`/les renderers. Une météo activée par le serveur 1.68 est donc ignorée visuellement par le Java.

## 192. Eau animée du terrain et cadence globale native non reproduites

Le client natif possède six frames `AnimWater01`. `TileSet::DrawWaterLevel` parcourt les cellules compilées, reconnaît les familles de terrain et de bord d’eau animées, puis dessine chaque tuile avec la frame globale courante. Cette frame avance toutes les trois exécutions de `DrawWaterLevel` et est remise à zéro lorsque l’option `bAnimatedWater` change. L’animation concerne donc la texture du sol et ses raccords, avec une cadence commune à toute la scène.

Le Java sait reconnaître qu’un terrain contient le mot `water` et sait générer des raccords de terrain, mais `GroundRenderer` ne possède pas de compteur de frames d’eau ni de sélection périodique des six variantes natives. La seule animation d’objet générique observée dans `ObjectRenderer` avance à 120 ms et `isAmbientAnimation` ne reconnaît que les sprites de type `shop sign-`. Les surfaces d’eau Java restent donc statiques ou suivent un mapping différent, et l’activation/désactivation de l’option native ne réinitialise aucune animation de terrain équivalente.

## 193. Éclairage animé des fontaines et moulins absent du rendu Java

Pour certains décors, le client natif ne dessine pas toujours le sprite statique. Avec `bShowAnimDecorsLight`, `BIG_FONTAINE_1`, `BIG_MOULIND` et `BIG_MOULING` ajoutent une animation de superposition (`AddOverlapAnim`) identifiée par l’objet et rendue ensuite par `DrawObjectAnimOverLapID`. La fontaine et les deux orientations du moulin peuvent donc produire une couche lumineuse animée indépendante du décor de base ; la désactivation de l’option revient explicitement au sprite fixe.

Le Java possède un `DecorRenderer` et un `ObjectRenderer`, mais leur chemin de décor sélectionne une région statique et leur seule animation ambiante générique identifiée vise les enseignes `shop sign-`. `DayNightCycle` applique une ambiance globale, sans état de superposition par fontaine/moulin ni option `ShowAnimDecorsLight`. Ces décors Java ne reproduisent donc pas l’éclairage animé natif et ne basculent pas entre les deux chemins selon le réglage 1.68.

## 194. Priorité et consommation des macros clavier natives absentes

Dans `TFC_PLAY`, le client natif construit une clé composée de la touche, de Ctrl et de Shift, puis essaie successivement les macros système (`Custom.gMacro`) et les macros utilisateur (`MacroUI`). Si l’une réussit, l’événement est consommé et `RootBoxUI::VKeyInput` ne reçoit pas la touche ; seules les touches non reconnues atteignent ensuite le traitement normal. `MacroHandler::CallMacro` peut en outre être globalement désactivé et refuse l’appel lorsque `DoNotMove` est actif. Une macro peut donc empêcher simultanément une action d’interface ou de déplacement.

Dans le Java, les recherches dans le chemin de jeu (`MainGameScreen`, `GameInputHandler`, `GuiManager`) ne montrent ni registre de macros par combinaison Ctrl/Shift, ni étape de consommation avant la distribution normale, ni drapeau global `DoNotMove` appliqué à l’appel d’une macro. Les raccourcis sont traités par les écrans ou handlers concernés, et certaines combinaisons Shift servent directement au déplacement/à la sélection. Une touche configurée comme macro native peut donc ouvrir une autre action, parvenir à l’interface ou ne rien faire dans le Java au lieu de bloquer exactement le même événement.

## 195. Statistiques et écran de classement PvP natifs absents

Le client 1.68 traite `RQ_GetPvpRanking` et reçoit séparément les morts totales, les tueries totales, les morts et tueries de la période courante, la série actuelle, la meilleure série et les points PvP. `PvpRanking::myPvpStat` compose ensuite plusieurs messages d’information dédiés (`PVP Points`, `Current kills`, `Current serial killing`, `Current deaths`, `Best serial killing`, `Total kills`, `Total deaths`). Ces valeurs ne sont pas déduites des quêtes ou du niveau : elles proviennent du classement/état PvP serveur.

Le Java ne possède ni `PvpRanking`, ni champs `TotalKillNumber`/`CurrentPvpPoint`, ni requête ou écran de classement équivalent. Ses compteurs de kills rencontrés dans `QuestService` servent à la progression de quêtes, et `Stats` conserve des compétences/points plutôt que les séries PvP. Une mise à jour PvP native ne peut donc pas alimenter le HUD, les messages ou un classement Java, et une mort ou une victoire Java ne produit pas les mêmes statistiques persistantes.

## 196. Deuxième couche de cape native non représentée dans le modèle Java

Le `Puppet` natif distingue `PUP_CAPE` et `PUP_CAPE_2` dans ses tables `BodyOrder`, `BodyOrderA`, `BodyOrderAR` et `BodyOrderR`. La seconde couche est placée différemment selon la direction, l’attaque et le retournement ; elle sert notamment à conserver le bon recouvrement avec le corps, les bras, les armes et le bouclier. Elle fait partie de la composition envoyée/reconstruite par `SetPuppet`, au même titre que les autres parties d’équipement.

Le Java possède `BodyPart.CAPE`, mais aucune valeur `CAPE_2`. Dans `PuppetBodyOrder`, l’index correspondant à `PUP_CAPE_2` est explicitement `null` dans `INDEX_TO_BODY_PART`, donc la table d’ordre ne peut pas rendre une seconde ressource de cape. Les personnages Java qui utilisent une cape à plusieurs couches ou une variante nécessitant cette partie auront ainsi un recouvrement, une silhouette ou une cape incomplète par rapport au client 1.68.

## 197. Masque de visibilité des parties du personnage non reproduit

Le rendu natif ne parcourt pas aveuglément toutes les parties chargées : chaque entrée de `BodyOrder`, `BodyOrderA`, `BodyOrderAR` ou `BodyOrderR` est d’abord filtrée par `Object->VisiblePart & Pow2(part)`. Le `Puppet` peut ainsi masquer séparément une main, un bras, un casque, des cheveux, une robe, une cape, un bouclier ou une arme selon l’équipement, le sexe, la pose d’attaque et la variante visuelle, tout en conservant la partie chargée pour d’autres états.

Le Java collecte les parties présentes dans `partMap` et les dessine selon `PuppetBodyOrder`, mais aucune propriété `VisiblePart` ni masque de bits par entité/pose n’a été trouvée dans `PlayerAnimations`, `Player` ou `BodyPart`. Une partie configurée reste donc candidate au rendu même lorsque le client natif la désactiverait pour un équipement ou une pose donnée ; le résultat peut afficher des couches superposées (par exemple cheveux/casque, membres sous une robe ou arme secondaire) que le client 1.68 cache.

## 198. Ombre d’unité native semi-transparente et optionnelle non équivalente

Le client natif possède un passe séparé `DrawObjectShadow`, activé uniquement avec `bShowNewOmbrage`. Il rend `PlayerShadow` et les ombres des objets/unités avec `DrawSpriteNSemiTrans`, un niveau de transparence explicite (`dwNiveauTrans = 160`), les coordonnées interpolées `OX/OY + MovX/MovY`, des passes distinctes pour l’ordre de profondeur et des exceptions liées aux portes, au joueur mort et aux effets séraphins.

Le Java charge des régions suffixées `Shd` dans `EntityAnimationsBase` et les dessine directement dans le même chemin d’animation, sans passe globale `DrawObjectShadow`, sans réglage `bShowNewOmbrage`, sans alpha natif fixe 160 ni filtrage de type/profondeur équivalent. Les ombres Java peuvent donc être absentes lorsque la ressource `Shd` manque, rester opaques ou suivre une autre interpolation, et ne peuvent pas reproduire le basculement global ancien/nouveau du client 1.68.

## 199. Carte de lumière locale native absente du rendu Java

Le moteur natif calcule `CurrentLight` et `CurrentLow` à partir de la lumière du joueur et de chaque objet visuel, avec une atténuation dépendant de sa distance dans la grille. Il fusionne ensuite plusieurs cartes (`lmPlayerLight`, `lmOtherPlayerLight`, torches, lanternes, poutres et chandelles) par `LightMap::MergeLightMap`, puis applique `MakeLightingFX` à la surface rendue. Une torche, un joueur éclairé ou un décor lumineux modifie donc localement les pixels voisins, indépendamment de la luminosité ambiante générale.

Le Java applique un `DayNightCycle` global et un overlay uniforme de luminosité dans `MainGameScreen`; aucun `LightMap` par zone, aucune fusion de sources ponctuelles et aucune propriété de lumière locale par objet n’a été trouvée dans `GroundRenderer`, `ObjectRenderer` ou les entités. Les cartes souterraines et les scènes comportant torches, lanternes, joueurs lumineux ou effets d’éclairage ne produisent donc pas les mêmes halos ni la même illumination locale que le client 1.68.

## 201. Reprise après perte de focus : réacquisition native des entrées absente du Java

Lors de `WM_ACTIVATE`, le client 1.68 traite explicitement la sortie et le retour d'Alt-Tab : il désacquiert la souris DirectInput et le clavier quand la fenêtre devient inactive, puis réacquiert les deux périphériques au retour. En mode plein écran, il restaure également les surfaces DirectDraw (`DXDRestoreSurfaceF`/`DXDRestoreSurface`) avant de reprendre l'affichage. `WM_SETFOCUS` remet en outre `CTRL_State` à zéro afin qu'une touche Ctrl maintenue avant le changement de fenêtre ne reste pas considérée comme pressée.

Dans `MainGameScreen`, `pause()` est vide et `resume()` ne recalcule que la caméra HUD et appelle `hud.recoverAfterDisplayChange()`. Aucun chemin Java équivalent ne suspend/réinitialise l'état des entrées, ne réacquiert un périphérique, ne restaure les surfaces de rendu ou ne remet à zéro les modificateurs clavier lors d'une perte de focus. Après Alt-Tab, minimisation ou changement de contexte graphique, le Java peut donc conserver une action/modificateur dans un état différent ou reprendre avec des ressources d'affichage invalidées, alors que le client natif a une séquence dédiée de sortie et de reprise.

## 202. Courbe de volume audio différente entre DirectSound et LibGDX

Le client natif stocke le volume des effets et de la musique sur une échelle discrète de 0 à 10. Pour un son chargé en mémoire, `T3VSBSound::SetVolume` transmet à DirectSound `-166 * (10 - v)` (centièmes de décibel), et le niveau zéro arrête le buffer. La lecture d'un son en jeu est en plus conditionnée par `dwSoundVol`, tandis que les flux (`TS_STREAMING`) suivent un chemin distinct. Le volume natif n'est donc pas une simple multiplication linéaire de l'amplitude PCM.

`GamePreferences` Java conserve `musicVolume` et `effectsVolume` comme flottants `[0,1]`, puis transmet directement ces valeurs à `Music.setVolume` ou `Sound.play`. Aucun remappage de l'échelle 0–10 vers l'atténuation DirectSound n'existe, et les chemins mémoire/streaming natifs ne sont pas différenciés par le même état. À réglage utilisateur comparable, les niveaux intermédiaires, le seuil de silence et la réaction d'un effet ou d'une musique chargée dans un autre type de buffer ne produisent donc pas le même résultat audible.

## 204. Hauteur d'affichage par défaut différente (1280×800 natif contre 1280×768 Java)

Le constructeur `Global` du client 1.68 appelle explicitement `SetDisplaySize(1280,800)`. Cette hauteur sert ensuite de référence aux surfaces DirectDraw, aux limites de clipping, au positionnement des éléments bas de l'écran, au chargement des ressources `..._<ScreenH>` et aux conversions de la grille virtuelle.

`GameConstants` Java fixe `WINDOW_WIDTH = 1280` mais `WINDOW_HEIGHT = 768`, et `MyGame` utilise cette valeur lors de `setWindowedMode`. Même à largeur identique, les 32 pixels manquants modifient le cadrage vertical, la position relative du HUD/chat, les zones cliquables et les calculs qui prennent la hauteur courante comme base. Le Java ne reproduit donc pas la géométrie d'affichage par défaut du client 1.68.

## 205. Format de pixels et transparence : surfaces 16 bits/RGB565 natives contre RGBA8888 Java

`DXDCreate` initialise les surfaces du client 1.68 avec `ScreenBPP = 16`. Les sprites et les routines d'effets manipulent donc des mots 16 bits, convertissent les couleurs en RGB565 (`5 bits rouge, 6 bits vert, 5 bits bleu`) et définissent une couleur-clé DirectDraw précise pour la transparence. Les tests de masque, de glow, de semi-transparence et de raccordement comparent ces valeurs quantifiées, parfois avec les masques `wRMask`, `wGMask` et `wBMask` de la surface vidéo.

Le Java crée au contraire les Pixmaps et textures de ces chemins en `RGBA8888` et s'appuie sur l'alpha/blending OpenGL. La couleur-clé 16 bits n'est pas quantifiée de la même façon et un pixel proche de la clé native peut rester visible ou devenir transparent différemment ; les mélanges de canaux et les dégradés semi-transparents produisent également des valeurs différentes. Même avec les mêmes images source et les mêmes coordonnées, le rendu pixel par pixel ne peut donc pas être identique dans les contours, les halos et les masques d'objets.

## 206. Redimensionnement de fenêtre toujours interdit dans le Java

Le client 1.68 possède un réglage `bLockResize` : les messages de bordure (`WM_NCHITTEST`) bloquent les zones de redimensionnement uniquement lorsque cette option est active. Lorsqu'elle est désactivée, la fenêtre peut être redimensionnée et le client recalcule son affichage à partir de la largeur/hauteur courantes, avec ses corrections de viewport et ses ressources dépendantes de la résolution.

`MyGame` appelle systématiquement `config.setResizable(false)`, sans préférence Java correspondante ni état permettant d'autoriser le redimensionnement. Le Java interdit donc également les profils natifs où `bLockResize` est désactivé, et ne peut pas reproduire le redimensionnement interactif suivi d'une mise à jour des coordonnées, du HUD et des zones de clic.

## 203. Algorithmes de raccordement des tuiles natives non équivalents aux compositions Tmpl Java

`Tileset.cpp` contient plusieurs passes dédiées de raccordement (`Smootage`, `Smootage2`, `Smootage3` et `WaterSmooth`). Pour chaque variante directionnelle, elles lisent les surfaces de deux terrains et choisissent le pixel source selon le masque de la troisième surface : certaines variantes testent la couleur-clé DirectDraw, d'autres la valeur de masque `0`, `0x7FFF` ou le masque RGB de la carte vidéo. Les tables natives couvrent les directions normales, diagonales et les distances `X2` à `X5`, avec des tables séparées pour l'eau.

Le Java ne passe pas par ces tables ni par ces sentinelles DirectDraw. `GroundRenderer.renderTmplTile` lit un masque Pixmap, utilise son octet alpha/couleur comme clé exacte vers un terrain, prend le pixel correspondant ou un terrain transparent de secours, puis met en cache une texture composée. Il n'existe pas de sélection native équivalente par variante `Smoothing`/`Smoothing2`, de test du color-key RGB ou de passe `WaterSmooth`. Les raccords de terrain et les transitions eau/terre peuvent donc choisir une source différente sur les bords, les diagonales et les masques partiellement transparents, même lorsque les noms de tuiles et les dimensions restent identiques.

## 200. Cache RTMap natif non invalidé lors d'un changement de monde à coordonnées identiques

Dans `RTMap::CreateRTMap`, le client 1.68 considère la vue comme déjà chargée lorsque `m_dwLoadX == xPos*2` et `m_dwLoadY == yPos`; le test sur `m_dwLoadW == World` est explicitement commenté. Si le personnage passe dans un autre monde en gardant les mêmes coordonnées, la fonction peut donc réutiliser l'image et le masque du monde précédent au lieu de recharger la carte et sa mémoire d'exploration. Le monde n'est réinitialisé qu'à la destruction/cachage de la fenêtre ou lors d'un déplacement qui invalide ce cache.

`GuiWorldMap` Java inclut au contraire `world` dans sa clé (`renderedTileX`, `renderedTileY`, `renderedWorld`) et reconstruit la vue dès que le monde change. Le résultat diverge dans ce cas précis : le natif peut afficher une vue périmée, alors que le Java bascule immédiatement vers le monde demandé.

Le traitement des mondes hors plage diffère aussi : le natif remet `iWorld` à 0 dans `LoadRTWorld`, mais `CreateRTMap` retourne après avoir vidé la sortie si `World > 7`; le Java borne directement le monde à `[0,7]` avant le chargement. Une valeur de monde invalide ne produit donc pas la même carte de repli.

## 207. Synchronisation du premier chargement différente

Le client natif crée un thread `FirstInitObject` et maintient `g_bFirstLoadComplete` à faux tant que cette initialisation n'est pas terminée. La boucle de chargement continue de dessiner `LOAD<ScreenW>.PCX`, le texte de progression et le conseil du jour, puis attend explicitement le drapeau avant de poursuivre. Ce thread initialise la liste d'objets (`Objects.Create`) et crée notamment les sons mémoire `Open Box`, `Equip` et `Vampire Dying` avant de libérer le démarrage.

`LoadingScreen` Java met en file tous les WAV/MP3/OGG trouvés sous le dossier des sons et lance en parallèle `startMapPreloadAsync()`. Le passage à `CharacterSelectionScreen` dépend de la fin de l'`AssetManager`, pas de la fin de `mapPreloadExecutor` ; les cartes peuvent donc encore être chargées après l'écran de sélection et être ouvertes à la demande via `getOrLoadMapReader`. Le natif bloque son étape de démarrage sur son initialisation dédiée, alors que le Java autorise une progression d'interface pendant un préchargement de carte encore incomplet.

## 208. Éditeur de texte natif beaucoup plus limité que l’éditeur du chat Java

`NewInterface/EditUI.cpp` du client 1.68 ne gère que l’insertion au curseur, `Backspace`, `DeleteChar`, le déplacement gauche/droite et le saut de mots avec CTRL. `LeftClick` est encore un TODO et ne positionne pas le curseur dans le texte. Il n’y a ni ancre de sélection, ni sélection par SHIFT, ni copier/couper/coller, ni annulation ; la limite par défaut est de 256 caractères et un filtre optionnel décide seulement si le caractère entrant est accepté.

`GameChat.java` implémente au contraire la sélection (SHIFT et CTRL+A), le presse-papiers (`CTRL+C/X/V`), l’annulation (`CTRL+Z`), le déplacement HOME/END, l’historique et la répétition temporisée de gauche/droite/Backspace. Le collage remplace les retours à la ligne par des espaces et respecte la limite après suppression de la sélection. Le comportement Java est donc fonctionnellement plus riche que l’éditeur natif 1.68 ; une comparaison d’ergonomie ou de tests clavier ne peut pas être considérée équivalente même si la longueur maximale de 256 caractères coïncide.

## 209. Durabilité et réparation ajoutées par le Java sans équivalent client natif

Dans `Packet.cpp`, le client 1.68 ne maintient pas une jauge de durabilité locale par objet. Lorsqu’une réponse `RQ_GetObject` signale un objet inutilisable/cassé, il affiche le message localisé « The object is broken and cannot be used » puis supprime l’objet identifié de `VisualObjectList`. Les autres erreurs d’objet marquent l’unité comme manquante ou la retirent ; aucune valeur de durabilité, usure par coup ou écran de réparation n’est calculée par ce client.

Le Java introduit au contraire `ItemDurabilityService` avec des pourcentages par entrée d’inventaire et emplacement équipé, une usure à l’attaque et à la mort, le blocage des bonus/attaques lorsque l’équipement est cassé, l’affichage de la jauge et un `RepairScreen` qui facture la réparation individuelle ou globale. Ces règles sont donc une mécanique Java supplémentaire : elles peuvent casser, déséquiper implicitement ou réparer localement un objet dans des situations où le client 1.68 aurait seulement reçu une erreur serveur et retiré l’objet concerné, sans partager la même économie ni le même état d’instance.

## 210. Récolte d’herbes ajoutée au Java

L’arborescence du client 1.68 ne contient pas de gestionnaire d’herbes, de nœud récoltable, de canal de récolte ou de requête de récolte. Les interactions de monde du client portent sur les objets, unités, coffres, sorts et déplacements ; aucune classe native ne crée des plantes aléatoires, ne les marque comme récoltées ou ne distribue un objet après un temps de canalisation.

Le Java possède au contraire `HerbManager`, `HerbNode` et `HarvestChannel`. Il tire une définition parmi plusieurs herbes pondérées, crée des nœuds dans la carte, affiche leur nom au survol/clic droit, impose une durée de canalisation et une tolérance de déplacement, puis mémorise les cellules récoltées jusqu’à la session. Cette mécanique et ses règles de disponibilité sont donc entièrement supplémentaires au client natif 1.68 ; elles peuvent afficher une interaction, consommer du temps et produire un objet là où le client original ne présentait aucun élément récoltable.

## 214. Syntaxes natives de routage du chat absentes du chemin Java

Dans `main2.cpp`, le client 1.68 interprète le premier caractère du texte avant d’envoyer le paquet : `:message` devient un `RQ_Shout`, tandis que les chaînes courtes `:)` et `:p` restent des messages ordinaires (avec l’exception historique `:k`/`:K`). `/nom message` devient une page privée `RQ_Page` si l’option de pages est active ; un destinataire entre guillemets permet les noms composés, par exemple `/"First Last" message`. Le client bloque aussi l’envoi d’une page en mode AFK et affiche un retour local si le destinataire est ignoré. Enfin, `;message` est transmis au canal actuellement sélectionné via `SendMessageToCurrentChannel`.

`GameChat` Java remet le texte complet à un unique `submitHandler`. `MainGameScreen` ne route ensuite que les commandes commençant par `.` vers `GmCommandProcessor`; sinon il affiche le texte au-dessus du joueur et le transmet éventuellement à la conversation NPC active. Aucun parseur Java équivalent pour `:`, `/`, `;`, les guillemets de destinataire, l’état AFK ou la liste d’ignorés n’est présent dans ce chemin. Un joueur Java peut donc produire un texte local/NPC là où le client 1.68 changeait de type de paquet, de canal et de contrôle d’envoi.

## 215. Traitement des exceptions fatales et rapport de crash non équivalent

Le client natif installe `CExpFltr::Filter` avec `SetUnhandledExceptionFilter`. Pour une violation d’accès, division par zéro, débordement de pile, instruction illégale ou autre exception Win32, il construit un diagnostic, appelle `LogException` (date, adresse, code/description et contexte registres), exécute la procédure d’arrêt puis termine le processus avec le code 1. Le filtre limite aussi le nombre de fautes et possède une configuration de redémarrage automatique.

Le runtime Java ne présente pas d’installation équivalente de `Thread.setDefaultUncaughtExceptionHandler`, de rapport de crash avec contexte machine, de compteur global de fautes ou de redémarrage configuré dans le chemin du jeu. Les erreurs locales sont souvent attrapées et ignorées, tandis qu’une exception non interceptée dépend du comportement générique de la JVM. Le diagnostic, le fichier produit, le moment de fermeture et la possibilité de reprise après crash diffèrent donc du client 1.68.

## 216. Banque audio VSB chiffrée et chargement par morceaux absents du Java

Le client natif regroupe les effets et musiques dans `gamefiles\\T4CGameFile.vsb`. `VSBDataBase::LoadIndex` lit un index d’identifiants, offsets, tailles, fréquence et profondeur audio ; `MemMapFile::CpyMemory` déchiffre ensuite les octets avec une table XOR dépendant de la position (bloc de 4096 octets). `LoadChunck` fournit les segments à `T3VSBFilter`, qui garde un compteur de références et ne charge que les portions nécessaires au décodeur. Le démarrage vérifie aussi l’intégrité et peut reconstruire/décompresser la banque VSB.

`SoundManager` Java recherche au contraire un fichier audio individuel dans `Paths.SOUNDS_DIR`, le charge comme `Sound` ou `Music` via LibGDX, puis ignore les erreurs de chargement. Aucun lecteur d’index VSB, déchiffrement XOR positionnel, cache de chunks, compteur de références audio ou vérification de `T4CGameFile.vsb` n’est utilisé par le runtime Java. Une installation contenant uniquement la banque native, un identifiant sonore sans fichier séparé ou une banque partiellement corrompue ne produit donc pas le même chargement ni le même comportement de repli.

## 217. Résolution totale des icônes et sons inconnus contre retours `null` Java

`GameIcons::operator()` et `GameSounds::operator()` sont conçus comme des fonctions totales : un identifiant non enregistré renvoie respectivement un sprite `???` ou le son générique `Generic Drop Item`. Les appels de l’interface et du glisser-déposer conservent ainsi un objet visuel/audio valide même lorsqu’une liaison de contenu manque.

`SpriteLoader` Java renvoie au contraire `null` pour un nom absent, un ID hors limites, une image vide ou une palette indisponible ; `SoundManager` abandonne silencieusement la lecture si aucun fichier ne peut être chargé. Les appels Java doivent donc tester l’absence et peuvent supprimer le dessin, le feedback sonore ou l’élément d’interface, alors que le client 1.68 affichait/jouait systématiquement son fallback identifié. Le résultat d’une donnée de contenu incomplète n’est donc pas équivalent.

## 218. Catalogues de langue chiffrés et repli natif différents du JSON Java

`LocalString::LoadAllStrings` ouvre `English.elng` ou `French.elng` en binaire, puis déchiffre chaque octet avec une table pseudo-aléatoire déterministe de 7 823 positions. Le fichier décodé est ensuite analysé en entrées `[id]`, et une langue demandée qui manque revient à l’anglais ; un index supérieur au nombre d’entrées revient à la dernière chaîne chargée. Les catalogues GUI et aide suivent le même principe de ressources binaires indexées.

`I18n` Java lit directement `assets/i18n/lang.json` en UTF-8 avec des clés textuelles. Un fichier absent/vide provoque une exception, une clé absente renvoie la clé ou le fallback fourni, et aucun déchiffrement, contrôle de taille d’entrée ou repli vers la dernière chaîne n’existe. Le Java ne peut donc pas consommer directement les catalogues `.elng` du client 1.68 et ne réagit pas de la même manière à une langue ou une entrée de traduction manquante/corrompue.

## 219. Politique de collision des liaisons d’icônes et de sons différente

Dans `GameIcons::BindSprite` et `GameSounds::BindSound`, les liaisons sont insérées dans une `std::map` avec `insert`. Si un même ID numérique est lié plusieurs fois, la première entrée reste celle retournée ; l’objet sprite/son nouvellement créé pour la liaison en collision est détruit. Le client natif utilise donc explicitement une politique « première liaison gagnante ».

Les définitions Java (`ItemIconDefinitions` et les registres de contenu) reposent sur des maps/entrées statiques et ne reproduisent pas ce contrat d’insertion runtime : une collision de clé lors d’une construction immutable peut échouer au chargement, tandis qu’une map mutable utilisant `put` remplacerait la liaison précédente. Selon le chemin de génération/import, le même contenu dupliqué peut donc être rejeté, remplacer l’icône existante ou empêcher le démarrage, au lieu de conserver silencieusement la première liaison comme le client 1.68.

## 220. Résolution du minuteur système native absente du démarrage Java

Au démarrage, `main2.cpp` appelle `timeGetDevCaps`, puis `timeBeginPeriod(caps.wPeriodMin)` afin d’augmenter la résolution du minuteur Windows utilisé par les boucles de rendu, de maintenance et les attentes de cadence. Le client termine ensuite cette période avec `timeEndPeriod`. Cette configuration s’ajoute aux temporisations internes de 17/34 FPS et réduit l’arrondi des `Sleep`/mesures `timeGetTime`.

Le Java s’appuie sur la cadence LibGDX, `delta`, `System.nanoTime` et les services de temporisation de la JVM, sans demande équivalente de résolution du minuteur système. Même lorsque les mêmes durées nominales sont configurées, la granularité de réveil, le jitter des animations, la répétition d’entrée et les délais courts ne suivent donc pas nécessairement le profil temporel du client 1.68.

## 221. Algorithme de déplacement au clic : poursuite gloutonne native contre A* Java

`Pf.cpp` ne construit pas une liste de nœuds ni une carte de coûts. `pfSetPosition` mémorise seulement la case cible et la dernière position ; `pfGetNextMovement` compare les signes de `xDif`/`yDif` et renvoie directement l’une des huit directions. Si le joueur n’a pas effectué le pas attendu, la fonction renvoie 0 ; un mode `Force` peut en outre remplacer cette direction par l’angle du curseur, et `pfStopMovement` annule la poursuite. Le client 1.68 suit donc une cible par choix glouton à chaque pas, sans rechercher un détour optimal autour d’un obstacle.

Le Java possède `Pathfinding.findPath`, une file de priorité, des scores `g/f`, une heuristique octile, un coût diagonal `1.4142135` et l’examen de voisins jusqu’à trouver une route. Il peut donc calculer un détour, comparer plusieurs chemins et continuer vers la cible malgré un obstacle, tandis que le client natif s’arrête ou choisit seulement le prochain axe vers la cible. Sur une carte avec mur, couloir ou obstacle diagonal, les deux clients ne sélectionnent pas la même suite de cases, même avant de considérer l’autorité réseau et l’interpolation déjà décrites.

## 222. Validation des diagonales et interdiction du « corner cutting » différentes

Dans `Pathfinding.java`, tout voisin diagonal est rejeté si l’une des deux cases orthogonales adjacentes est bloquée (`sideX/sideY` ou `sideX2/sideY2`). Le chemin Java ne peut donc pas passer en diagonale dans l’angle formé par deux obstacles, même si la case diagonale elle-même est libre ; il cherche un autre trajet ou déclare la destination inaccessible.

`pfGetNextMovement` natif ne consulte pas la carte de collision pour choisir le prochain pas : après comparaison des écarts, il renvoie directement `2`, `4`, `6` ou `8` pour une diagonale (et le mode `Force` peut encore imposer l’angle du curseur). La collision et l’acceptation du pas sont décidées par le flux serveur/état joueur, pas par ce test local des deux côtés. Dans un angle bloqué, le Java refuse donc l’intention avant émission, alors que le natif peut envoyer la diagonale puis attendre une correction, un refus ou un état inchangé.

## 223. Rapprochement natif de la cible après correction absent du mouvement Java

Le client natif possède `pfNearPosition`, appelé par `MouseAction`, `TFCSocket` et plusieurs branches de traitement des réponses. Cette fonction déplace progressivement la cible mémorisée (`pfSaveXPosition`/`pfSaveYPosition`) d’une case vers la position réelle du joueur avant de recalculer le prochain mouvement. Le chemin automatique est ainsi rapproché de la position autoritative après un retard, une correction ou une réponse de déplacement, au lieu de conserver aveuglément l’ancienne destination.

`PlayerMovement` Java conserve sa destination/réservation de pas et, en cas de divergence, efface l’étape active puis recalcule avec l’entrée courante ; il n’a pas d’équivalent qui rapproche progressivement une cible persistante de la position corrigée avant de reprendre le chemin. Après une correction de position, un clic maintenu ou une réponse retardée simulée, le natif et le Java peuvent donc reprendre vers des cases différentes.

## 224. Fenêtres temporelles de double-clic non uniformes

`GameUI` natif fixe `ClickTime` à 250 ms et `RootBoxUI` compare directement deux horodatages `timeGetTime` pour transformer le second clic en `GWIN_MSG_DBLCLICK`. Cette même convention alimente les listes, les panneaux et les actions de monde qui reçoivent `DM_DOUBLE_CLICK` ; elle est indépendante du délai configuré par l’utilisateur dans Windows.

Le Java ne possède pas une constante globale équivalente : `Inventory` reconnaît un double-clic sous 300 ms, tandis que `ClickToMoveHandler` utilise 350 ms pour la barre rapide. Les autres écrans passent par les événements LibGDX ou leurs propres handlers. Un second clic entre 250 et 350 ms peut donc être un double-clic natif mais un clic simple dans un écran Java, ou déclencher une utilisation Java là où le client 1.68 ne l’aurait pas classée comme double-clic.
## 225. Contrôles de défilement natifs continus et répétition au maintien absents des listes Java

`ScrollUI` natif est un contrôleur partagé par les listes, grilles, textes, inventaire, coffre, commerce, compétences, guilde, options et historique du chat. Un clic sur une flèche déplace d’abord d’une ligne, puis le maintien répète l’action toutes les 100 ms. Un clic dans la piste saute de 5 lignes selon la moitié visée ; le bouton peut être glissé et convertit sa position en `linePos`. Les bornes sont centralisées sur `0..listSize-1`, et chaque changement appelle `ScrollChanged`.

Le Java n’a pas ce contrat global : `GuiChat` fait défiler la molette par 3 lignes, `GuiInventory` par pixels (`amountY * 24`), `GuiOptionList` par une ligne, et `GuiListScreen` par pages de six entrées. Les listes de commerce Java n’exposent pas le même bouton de piste/glisser ni le saut natif de 5 lignes, et le maintien d’une flèche n’est pas traité par une répétition commune à 100 ms. À position identique du curseur ou après un maintien, le nombre de lignes visibles, le moment de la mise à jour, le son et la sélection active divergent donc du client 1.68.

## 226. Matrice des touches de déplacement et dépendance au pavé numérique différentes

Dans `TFCSocket.cpp`, le déplacement manuel natif lit l’état DirectInput brut. Les huit directions acceptent les flèches, et aussi `NumPad 2/4/6/8` lorsque le calcul local de `NumLock` les rend actifs ; `NumPad 1/3/7/9` et `End/Home/PgDn/PgUp` fournissent en plus les quatre diagonales. Avant d’émettre, le client refuse le déplacement si Ctrl ou Shift est maintenu, si `ChestUI` ou `TradeUI` bloque le monde, ou si `DoNotMove`/`boKeyProcess` l’interdit. Chaque touche est testée séparément dans la boucle, après la combinaison diagonale, avec une vérification `GridBlocking` sur l’offset correspondant.

`GameInputHandler` Java ne lit que WASD (avec variantes Q/Z) et les quatre flèches : aucune touche du pavé numérique, aucune substitution Home/End/PgUp/PgDn et aucune dépendance équivalente à l’état NumLock n’est présente. Le filtrage Java repose principalement sur `textInputActive`, et ne reproduit pas la combinaison native « Ctrl/Shift bloque le déplacement » ni les mêmes états `ChestUI`/`TradeUI` et `DoNotMove`. Une pression du pavé numérique, de Shift ou de Ctrl peut donc déplacer le personnage dans un cas où le client 1.68 ne l’aurait pas fait, tandis que plusieurs diagonales natives ne produisent aucune entrée Java.
## 227. Aide contextuelle des grilles au clic droit non reproduite globalement

`GridUI::RightMouseUp` natif traite tout clic droit bref dans la grille : sur une case vide, il appelle l’aide de la grille ; sur une case occupée, il affiche l’aide de l’élément si `allowHelp` est actif puis transmet aussi `RightMouseUp` à l’élément ; hors des limites, il affiche encore l’aide de la grille. Cette résolution est commune aux grilles d’inventaire, coffre, échange et autres panneaux, et s’effectue après la capture de la sélection au `RightMouseDown`.

Le Java ne possède pas ce routage d’aide générique. `GroundItemClickHandler` affiche seulement le nom d’un drop sous le curseur et `ObjectClickHandler` seulement le nom d’un objet mappé ; un clic droit sur une case vide, hors objet, ou sur un contrôle sans handler dédié est consommé sans aide de grille. Le Java ne transmet donc pas systématiquement le clic droit à la cellule sélectionnée ni ne distingue l’aide de la grille de l’aide de l’élément comme le client 1.68.
## 228. Format et emplacement de configuration utilisateur incompatibles

`Global::ReadClientConfig` natif construit automatiquement le chemin `CSIDL_PERSONAL\Rebirth\T4CV2`, crée les répertoires, puis lit `T4C.dat` en binaire. Le fichier contient dans un ordre et des tailles fixes le nom de compte, l’adresse IP, le statut AFK, un message AFK de 2048 octets, les drapeaux debug/FPS/position, `FirstTimeAddon` et `WebpatchEnable`; `WriteClientConfig` réécrit ces mêmes blocs. Le client recharge cette configuration avant l’initialisation de la langue, des captures, des journaux et du launcher.

Le Java charge `game_preferences.json`, `characters.json` et `player_state.json` depuis le répertoire de travail, avec Gson et des champs JSON indépendants. Il ne lit ni n’écrit `T4C.dat`, ne crée pas le chemin utilisateur natif et ne conserve pas dans ce fichier binaire les mêmes informations de compte/IP, AFK, addon, webpatch et flags de diagnostic. Copier un profil 1.68 ou changer le répertoire de lancement ne produit donc pas le même état persistant, et une configuration native existante est invisible pour Java.
## 229. Système de domestication et de compagnon ajouté dans le Java

Le client 1.68 utilise le terme `Puppet` pour la composition visuelle d’un personnage : `RQ_PuppetInformation`/`SetPuppet` transmettent les parties d’équipement et reconstruisent les couches du sprite. Dans les sources natives du client, aucune séquence de domestication, barre de canalisation, registre de compagnon, mode d’IA, perte de compagnon ou sauvegarde d’un animal apprivoisé n’a été trouvée ; un monstre reste une unité visuelle/combat reçue du serveur.

Le Java ajoute une mécanique complète : `TameValidator` vérifie la cible et le niveau, `TameChannel` impose une canalisation et une tolérance de déplacement, `TamedCompanionFactory` transforme le monstre, `CompanionManager` gère le compagnon, ses modes, son XP et ses attaques, et `PlayerStateStore` le restaure entre les sessions. Un sort de domptage peut donc créer et conserver une unité alliée dans Java alors qu’aucun parcours équivalent n’existe dans le client 1.68 ; les combats, dégâts, effets et règles de disparition qui en découlent sont des comportements supplémentaires.
## 230. Source temporelle et seuils jour/nuit différents

Le client natif reçoit l’heure complète avec `RQ_GetTime` (`seconde`, `minute`, `heure`, jour, semaine, mois et année), la recopie dans `g_TimeStructure`, puis avance cette structure dans la boucle réseau avec `AddSeconde`. `NTime::SetLight` applique des paliers précis : nuit 00:00–04:00 et 21:00–24:00, aube 04:00–08:00, transition vers le jour 08:00–10:00, jour 10:00–19:00 et transition du soir 19:00–21:00. Les couleurs sont calculées séparément en canaux entiers 5 bits, et les mondes 1/2 ou certaines zones du monde 3 forcent en plus les teintes donjon/caverne.

`DayNightCycle` Java démarre par défaut à 07:00, avance avec le `delta` local de LibGDX et sauvegarde une heure flottante dans `PlayerStateStore`; aucun paquet Java équivalent à `RQ_GetTime` ne la recale sur une horloge serveur. Ses seuils sont nuit 18:00–06:00, transitions autour de 05:00–07:00 et 17:00–19:00, avec un overlay noir global. À une même heure nominale, les plages d’obscurité, la couleur, la précision temporelle et la réaction aux mondes souterrains ne sont donc pas celles du client 1.68.
## 231. Quantification et rejet des coordonnées souris différents

`DirectXInput::SetVirtualGrid` natif définit une grille virtuelle dépendante de la fenêtre. `GetStatus` convertit ensuite la souris en case en appliquant les offsets historiques du client (`+48` sur X et `-8` sur Y), puis vérifie que la case appartient à `VirtualGrid`. Une position hors grille ou invalide est normalisée en `(0,0)` avant d’être transmise aux actions ; le clic n’est donc pas interprété comme une coordonnée arbitraire hors monde.

Le Java déprojette directement le pixel avec la caméra LibGDX, puis plusieurs handlers convertissent séparément avec `(int)(world / GRID_W)` et `(int)(world / GRID_H)`. La caméra, le viewport et le point d’origine remplacent les offsets/limites natifs, sans grille virtuelle commune ni valeur sentinelle `(0,0)` pour un clic invalide. Un clic sur les bords, dans une zone négative ou pendant un changement de caméra peut ainsi viser une autre tuile, atteindre un handler d’objet ou déclencher un déplacement là où le client 1.68 aurait rejeté/normalisé la position.
## 232. Nettoyage des actions et de l'interface pendant un changement de monde

- Le client natif traite le changement de monde comme une transition exclusive : il active `DoNotMove`, appelle `CloseAllUI()`, arrête le pathfinding, réinitialise la musique et ne réautorise le jeu qu'après le chargement et le fondu de la nouvelle zone. Les fenêtres et les interactions de l'ancienne zone ne peuvent donc pas rester actives pendant le transfert.
- Dans `MainGameScreen.switchMapForZ`, le Java reconstruit le renderer et les gestionnaires de NPC/monstres, mais n'appelle pas `GuiManager.close()` et ne possède pas de verrou global équivalent. Il annule explicitement la récolte (`cancelHarvest()`), mais pas le canal d'apprivoisement ni les autres progressions/effets de sort en cours. Une fenêtre ouverte ou une action transitoire peut donc survivre au changement de carte selon son propre état, au lieu d'être invalidée atomiquement comme dans le client 1.68.
## 233. Réinitialisation et repopulation des unités autour du joueur

- Après un changement de monde, le client natif exécute `Objects.DeleteAll()` sous verrou, reconstruit les objets statiques/animés, déplace le joueur, puis envoie explicitement le paquet serveur `GetNearUnits` (opcode 60) avant de reprendre l'état de jeu. Les unités visibles sont donc une nouvelle fenêtre de proximité fournie par le serveur et non un reliquat de la zone précédente.
- `switchMapForZ` Java recrée `NPCManager` et `MonsterManager` à partir des spawns locaux de la carte et conserve séparément le compagnon. Il n'existe pas de requête `GetNearUnits`, de registre d'unités distantes ni de phase serveur équivalente ; le contenu visible dépend donc du fichier de carte et des gestionnaires locaux, avec une sémantique différente pour les joueurs/monstres apparus ou disparus pendant la transition.
## 234. Sélection et bornage de quantité pour les transferts d'objets

- Le client natif propose dans `ChestUI` et `TradeUI` une popup de quantité commune avec curseur borné par la quantité disponible, champ numérique filtré, longueur maximale de 9 caractères et validation qui rabat une valeur supérieure au stock réel. Pour une quantité maximale de 1, la popup renseigne automatiquement `1` et valide directement ; la quantité confirmée est ensuite envoyée dans une requête de transfert dédiée.
- Le Java n'a pas de popup équivalente ni de transfert de piles vers un coffre ou un échange joueur-à-joueur. `ChestService` ouvre un loot et le dépose au sol, tandis que l'inventaire et la boutique manipulent localement des clés/répétitions et des incréments unitaires. Une opération « déplacer N objets » et ses bornes/confirmations ne suivent donc pas le comportement UI et protocolaire du client 1.68.
## 235. Arrêt coordonné des threads internes

- Le client natif possède des états d'arrêt séparés pour le son, le contrôle sonore, le dessin, la maintenance, la souris et le CD. `AppManagement::AsyncClose` notifie d'abord les interfaces, positionne `g_boQuitApp`, réveille les événements bloquants, attend ou suspend les threads encore actifs et libère ensuite les handles/ressources. Le réseau, le rendu, les entrées et les sons ne sont donc pas simplement détruits dans l'ordre de l'appelant.
- Le Java est principalement mono-boucle LibGDX : `MainGameScreen.dispose()` libère ses ressources et `MyGame.dispose()` arrête le préchargeur de cartes, mais il n'existe pas d'état d'arrêt partagé entre entrée, logique réseau, rendu et audio, ni de coordination équivalente de files/threads. Une sortie pendant un chargement, une action ou une lecture audio ne suit donc pas les mêmes garanties d'achèvement et de nettoyage que le client 1.68.
## 236. Clé de tri des objets visuels différente

- `VisualObjectList::Sort` natif trie les unités selon une clé calculée à partir de `OY + OC`, puis départage les égalités avec `OC`, `OX` et l'identifiant d'objet attaché. Le tri intervient après le filtrage des objets hors fenêtre et la vérification des files de mouvement/enfants ; les objets attachés peuvent donc rester groupés avec leur parent et leur ordre de dessin dépend de la position interpolée, pas seulement de la tuile.
- Le Java sépare les passes sol, décors, entités et effets. `DecorRenderer` utilise une clé basée sur le nom du décor, la coordonnée Y et `getZOrderFast`, tandis que les entités sont rendues dans leur propre parcours ; aucune clé commune équivalente à `OY + OC → OC → OX → AttachID` n’arbitre toutes les catégories. Des chevauchements entre décor, unité, ombre et effet peuvent donc être dessinés dans un ordre différent du client 1.68, même lorsque les positions de grille sont identiques.
## 237. Fenêtre de culling et conservation des objets hors écran différentes

- Le client natif conserve une fenêtre fixe autour du joueur via `VisualObjectList::RangeWidth` et `RangeHeight`, indépendante du viewport graphique. Pendant la mise à jour, un objet dont `abs(OX) > RangeWidth` ou `abs(OY) > RangeHeight` est supprimé s’il est de type inférieur à `30000`, n’a pas `allowOutOfBound` et ne possède pas d’enfant (`Count == 0`). Les objets attachés décrémentent alors le compteur de leur parent ; certains objets système et les objets marqués hors-borne échappent à cette suppression. Les objets statiques et animés sont en outre réinjectés sur les bords avec des comparaisons exactes à `RangeWidth - 1`/`RangeHeight - 1`.
- Le Java calcule `renderStartX/Y` et `renderEndX/Y` depuis la caméra, ajoute un buffer de trois tuiles et un débord décor séparé, puis `NPCManager` et `MonsterManager` ignorent les entités hors de cette zone pendant leur mise à jour. Cette logique est une optimisation de visibilité liée à la caméra : elle ne supprime pas selon une règle commune de durée de vie, ne porte pas d’équivalent à `allowOutOfBound`, `Count`, `Type >= 30000` ou `AttachID`, et les décors/entités ont des marges distinctes. Une unité ou un objet natif conservé ou détruit au bord de la fenêtre peut donc être mis à jour, réinjecté ou disparaître à un moment différent dans Java.
## 238. Prétraitement conditionnel des scripts PNJ non équivalent

- Les sources natives des PNJ sont des fichiers C/C++ intégrés au client/serveur : elles contiennent des directives de préprocesseur (`#ifdef`, `#ifndef`, `#define`, `#endif`) et des blocs commentés qui sélectionnent ou retirent des dialogues, quêtes et handlers selon la variante compilée. Le comportement réellement disponible dans une build 1.68 dépend donc de la configuration de compilation, avant même l'exécution du dialogue.
- Le Java passe le texte de `originalScript.sourceScript()` directement à `NpcScriptEngine`. `statements(...)` ignore seulement les lignes vides, `//`, `/*` ou `*` ; aucune passe de préprocesseur ne résout les symboles ou ne supprime les branches `#ifdef/#else/#endif`. Les directives présentes dans les scripts importés deviennent donc des lignes inconnues ou du texte non exécutable, et les variantes de build natives ne peuvent pas être sélectionnées de la même façon. Deux installations utilisant une définition native différente peuvent ainsi partager le même comportement Java, alors que le client/serveur 1.68 n'exécutait qu'une branche compilée.
## 239. Sélection et double-clic des listes génériques différents

- `ListUI` natif est un contrôle commun à plusieurs fenêtres : il maintient une sélection d'item, de colonne et de ligne. Un clic gauche sélectionne la ligne, un double-clic est détecté dès le `LeftMouseDown` et déclenche l'action de l'item, tandis qu'un clic droit sélectionne également l'item et affiche son aide (ou l'aide par défaut de la liste). La sélection reste disponible pour l'événement visiteur, y compris après un défilement, et les colonnes d'une même ligne sont manipulées ensemble.
- Le Java n'a pas de `ListUI` central avec ce contrat. `GuiListScreen` compose des boutons/rows propres à chaque écran ; les sélections et callbacks sont implémentés séparément dans `ShopScreen`, `RepairScreen`, `QuestScreen`, etc. Aucun routage générique ne reproduit simultanément le double-clic natif, la sélection de colonne/ligne, l'aide au clic droit et la transmission de la sélection à un visiteur. Un clic droit ou un double-clic sur une liste Java peut donc être ignoré ou suivre le handler local de l'écran, au lieu de sélectionner et d'ouvrir l'aide/action de la ligne comme en 1.68.
## 240. Pagination des paragraphes et retour à la ligne différents

- `TextPageUI` natif ne stocke pas seulement une chaîne déjà rendue : il conserve une liste de paragraphes avec texte, couleur, taille de police, nombre de lignes calculé et indicateur `allowNewLine`. `AddText` recalcule le nombre total de lignes selon la largeur de la zone, `UpdateViewSize` ajuste la capacité visible et `ScrollChanged` reconstruit les `TextObject` affichés à partir de l'offset courant. Le même contrôle peut donc mélanger des paragraphes colorés, des retours forcés et du texte automatiquement reflué dans une zone défilante.
- Le Java répartit ce besoin entre `GuiBoxedText`/`GlyphLayout`, `SystemMessage` et `GameChat`. Le wrapping, la couleur, la hauteur et le défilement sont recalculés par écran ou par composant ; aucun modèle commun de paragraphe avec `allowNewLine`, taille de police stockée et offset de lignes natif n'est exposé. Un texte injecté dans une aide, une page ou un journal peut donc produire des coupures, une hauteur et une position de défilement différentes, même lorsque le contenu et la largeur visuelle semblent identiques.
## 241. Cycle de la touche Échap et état du menu différents

- Dans `RootBoxUI::VKeyInput`, Échap ne ferme pas simplement la fenêtre active. Si une fenêtre plein écran est ouverte, le natif masque le chat et le TMI puis restaure les macros ; sinon il fait progresser l'état persistant du menu (`MENU_BOTH` → chat seul/TMI → aucun, selon la combinaison courante), affiche ou masque `SideMenu` et `ChatterUI`, et met à jour `dwMenuState`. Une fenêtre au premier plan reçoit toutefois la touche en priorité, ce qui rend le cycle dépendant du focus global.
- Le Java donne à chaque écran sa propre règle Échap : les écrans appellent généralement `GuiManager.close()`, puis `MainGameScreen` ouvre `OptionsScreen` lorsqu'aucune interface n'est active. Il n'existe pas de cycle persistant chat/TMI/macros, ni de restauration/minimisation coordonnée des panneaux. Une pression répétée d'Échap ne produit donc pas les mêmes états intermédiaires et peut ouvrir les options Java là où le client 1.68 ne faisait que basculer l'interface de proximité.
## 242. Filtrage élémentaire du livre de sorts absent du Java

- `SpellUI` natif expose sept boutons d'élément (feu, eau, air, terre, lumière, ténèbres et normal). Le clic reconstruit temporairement l'index des pages en ne gardant que les sorts de l'élément choisi, replace la vue sur la première page du résultat, puis restaure la page précédente si aucun sort ne correspond. Les quatre emplacements de la page sont donc une vue filtrée de la liste reçue, et non toujours les quatre sorts consécutifs du livre complet.
- `SpellBook` Java reconstruit toujours les pages avec `currentPage * 4` dans la liste complète `spells`. Il n'a pas de boutons/état de filtre élémentaire ni de recalcul d'index par élément ; un joueur ne peut donc pas obtenir la même vue ciblée, le même retour de page ou le même comportement « aucun résultat » que dans le client 1.68, même si chaque `SpellData` possède une information d'élément exploitable par le moteur de sort.
## 243. Rappel du dernier sort lancé absent du Java

- `SpellUI` natif conserve `lastSpell` à chaque lancement réussi/engagé. `CastLastSpell` recherche ensuite cet identifiant dans le livre et relance le sort avec `autoTargetSelf == false` et le chemin `noCallback` prévu pour les actions répétées ; `MouseAction` appelle cette fonction dans plusieurs parcours de clic/attaque. Le rappel dépend donc de l'identifiant du sort encore présent dans la liste, et non du dernier emplacement de macro utilisé.
- Le Java ne possède pas de champ ou de service `lastSpell` équivalent. `MainGameScreen` garde des états transitoires (`selectedTargetedSpell`, `currentAttackSpell`, `lastClickedBuffSpellName`) et les quick-slots mémorisent un nom par emplacement, mais aucun geste général ne recherche puis ne relance le dernier sort par son identifiant après fermeture du livre ou changement de cible. Un rappel natif peut donc relancer le sort alors que le Java ne fait rien, conserve seulement une cible en attente ou réutilise un slot différent.
## 244. Priorité de type de cible pour les sorts différente

- Avant de capturer le clic de ciblage, `SpellUI::CastSpell` configure le mode de grille des objets : `monsterPriority` pour les sorts visant les monstres, `playerPriority` pour les joueurs et `equalPriority` pour les sorts acceptant tout type d'unité. Le gestionnaire natif peut ainsi résoudre une superposition selon la catégorie autorisée ; pour un sort d'unité, `CastSpellUnit` n'envoie rien si aucun identifiant d'unité valide n'est trouvé, tandis que le sort positionnel peut envoyer une position sans unité.
- Le Java classe principalement le sort avec `isHostileUnitSpell`, `isTameSpell`, `isPositionTargetSpell` et des handlers distincts. Il sélectionne un monstre/NPC local ou une position selon le chemin choisi, sans mode de grille commun qui arbitre une superposition joueur-monstre et sans la même distinction native entre absence d'unité (aucun envoi) et position de secours. Dans une case contenant plusieurs catégories ou une cible devenue invalide entre le clic et le lancement, le candidat retenu et l'action résultante peuvent donc différer.
## 245. Ordre des sorts dans le livre différent

- `SpellUI::UpdateSpells` trie systématiquement la liste reçue avec `Spell::operator<` : d'abord par élément numérique, puis par niveau croissant. Les quatre cases d'une page et le résultat des filtres élémentaires suivent donc cet ordre, quelle que soit l'ordre d'arrivée du paquet serveur.
- `SpellBook` Java parcourt `player.getSpells()` dans son ordre courant et ajoute chaque sort sans tri. Les définitions globales Java sont, elles, triées par identifiant lorsqu'elles sont découvertes, mais ce tri n'est pas appliqué à la liste personnelle dans `loadSpells()`. À liste reçue identique, les sorts peuvent donc apparaître sur des pages et dans des positions différentes du client 1.68, ce qui change aussi le sort sélectionné par un clic ou un glisser-déposer.
## 246. Description d'un sort dans le livre non affichée de la même façon

- Lors de `SpellPageUI::FillSpellPage`, le natif place `spell.desc` dans l'aide du contrôle (`GetHelpText()->SetText`). La description est donc disponible au survol/clic d'aide de chaque case, en plus du nom, niveau, durée, type, coût et icône affichés dans la page.
- `SpellBook.addSpellEntry` Java affiche uniquement le nom, type, durée, mana, niveau et icône. La méthode ne crée pas d'aide ou de tooltip à partir de `SpellData` (et son `onTouchDown` démarre un glisser-déposer sur l'icône). La description reçue/définie pour le sort n'est donc pas consultable depuis la case du livre comme dans le client 1.68 ; le geste peut engager un drag là où le natif fournit l'information contextuelle.
## 247. Conversion d'affichage des durées de sorts différente

- Le client natif reçoit une durée numérique en millisecondes et la formate dans `FillSpellPage` selon plusieurs branches : durée supérieure à une minute en minutes avec secondes résiduelles, durée supérieure à une seconde en secondes, durée nulle comme « instantané », et durée inférieure ou égale à une seconde avec une présentation dédiée. Les libellés et la combinaison minutes/secondes proviennent du catalogue localisé, pas d'une chaîne fournie par le serveur.
- `SpellData` Java stocke `duration` comme `String` et `SpellBook` l'affiche directement ; une valeur vide devient seulement le texte codé `instant`. Il n'existe pas dans ce chemin de conversion millisecondes → minutes/secondes ni de branche pour les durées sub-secondes. Une définition numérique, une durée fractionnaire ou une traduction différente peut donc apparaître littéralement ou avec un format différent de la fiche native.
## 248. Type et interprétation du coût de mana différents

- Dans la structure native `SpellUI::Spell`, `manaCost` est un `WORD` reçu avec la liste de sorts ; `FillSpellPage` le convertit avec `itoa` et affiche donc une valeur entière fixe. Le coût visible est le champ transmis pour ce sort, sans expression aléatoire à évaluer dans l'interface.
- `SpellData` Java stocke au contraire `manaCost` comme `String` et le livre l'affiche sans conversion. Le moteur Java peut ensuite interpréter cette chaîne comme une formule de dés (`DiceFormula`) dans `SpellCastingService`. Une définition telle que `1d6`, une chaîne vide ou une valeur non numérique peut donc produire un texte et un coût effectif différents du couple natif « entier reçu / entier affiché ».
## 249. Couleurs et aide des compétences différentes

- `CharacterUI::UpdateSkills` choisit la couleur de la valeur selon deux axes natifs : compétence utilisable ou non (`bUse`) et valeur effective supérieure, égale ou inférieure à la valeur vraie. Il produit ainsi six états visuels (gris/vert foncé/rouge si inutilisable, blanc/vert pâle/rouge pâle si utilisable) et compose l'aide avec la description et le couple `dwStrength / dwTrueStrength`.
- `Statistics` Java ne rend que trois compétences fixes et `buffedStatLabel` affiche la valeur effective seulement lorsqu'elle est supérieure, avec vert, sinon la valeur de base en blanc. Il n'a pas la matrice rouge/gris de `bUse`, ne distingue pas une valeur effective inférieure dans l'affichage et ne compose pas l'aide native description + `effective/true`. Un bonus négatif, une compétence désactivée ou une compétence temporairement inutilisable n'a donc pas le même feedback visuel.
## 250. Modes d'activation des boutons différents

- `ButtonUI` natif distingue le clic simple et le double-clic dans `LeftMouseUp`, joue les sons de pression/libération séparément et possède un mode `enableDragCycle` : pendant `DragCycle`, l'événement peut rappeler périodiquement `LeftClicked` tant que le bouton est maintenu. Le contrôle peut donc servir à des boutons répétés ou à des actions ayant un traitement spécifique du double-clic.
- `GuiButton` Java ne porte qu'un `Runnable` unique appelé lors d'un relâchement gauche resté dans la zone ; il ne possède ni callback double-clic, ni mode de répétition pendant drag, ni séparation configurable des sons pression/libération. Un maintien, un double-clic ou un relâchement après déplacement ne déclenche donc pas les mêmes callbacks qu'un `ButtonUI` natif configuré pour ces modes.
## 251. Visibilité et cadence du curseur de saisie différentes

- `EditUI` natif n'affiche son curseur que lorsque `EnableCursor` est actif et que le parent considère le contrôle comme dernier élément cliqué. Son état alterne avec deux délais configurables de 300 ms (`cursorBlinkOnTime` et `cursorBlinkOffTime`) ; le texte est dessiné avec `textOffset` et reste découpé par le rectangle de l'éditeur. La perte du focus parent masque donc le curseur même si le champ existe encore.
- `GameChat` Java affiche le curseur tant que le chat est `active`, avec une alternance basée sur une période fixe de 350 ms, sans notion de `parentUI`/dernier contrôle cliqué ni de paramètre `EnableCursor` par champ. Son `inputScroll` suit le curseur, mais l’état de visibilité dépend de l’activation globale du chat. Un champ conservé ouvert ou une saisie superposée peut ainsi montrer/masquer le curseur et le faire clignoter à un moment différent du client 1.68.
## 252. Capture des fenêtres et redimensionnement générique différents

- `BoxUI` natif est un conteneur de dispatch : il cherche le premier enfant visible touché, mais laisse la fenêtre enfant au premier plan détourner l'événement si elle n'est pas dans cette hiérarchie. Les clics gauche/droit, la molette, le clavier et `DragCycle` sont ensuite transmis au contrôle ciblé ; le clic droit sans enfant ouvre l'aide de la boîte. `DragCycle` est régulé globalement à environ 100 ms et la fenêtre affichée est replacée en tête de la liste racine par `Show(true)`.
- Le Java ajoute dans `GuiScreenBase` un geste transversal qui n'est pas une opération de `BoxUI` native : Ctrl-glisser déplace n'importe quel `GuiResizable`/élément candidat, tandis que Shift-glisser redimensionne son bord le plus proche avec une taille minimale de 8 pixels. Cette capture intervient avant les handlers propres de l'écran et journalise le résultat au relâchement ; elle ne reproduit ni la priorité `foregroundChild`, ni la transmission native de `DragCycle`, ni l'aide contextuelle du clic droit. Selon la touche modificatrice et le point de clic, Java peut donc déplacer/redimensionner une fenêtre que le client 1.68 aurait seulement utilisée pour dispatcher un contrôle, ou empêcher son événement enfant.
## 253. Nombre de questions et sélection du questionnaire de création différents

- Le client 1.68 mélange séparément les huit questions et les cinq réponses de chaque question (`Shuffle`). Il affiche ensuite seulement les quatre premières questions du parcours : à chaque réponse, il incrémente une des cinq affinités, puis envoie immédiatement `RQ_CreatePlayer` avec les cinq compteurs, le sexe et le nom lorsque `QuestionNumber == 4`. Les quatre questions présentées sont donc un tirage sans remise parmi huit, avec un ordre de réponses propre à chaque question.
- `CharacterSelectionScreen` Java mélange bien huit questions et cinq réponses, mais `acceptQuestionAnswer()` continue jusqu'à épuiser les huit entrées (`questionnaire.size() == 8`) avant de générer les statistiques et de poursuivre. Le Java demande donc deux fois plus de choix au joueur et accumule des affinités différentes pour une même graine/tirage ; le moment de la création et les statistiques initiales ne peuvent pas correspondre au parcours 1.68, même si les textes et l'ordre des cinq réponses sont par ailleurs repris.
