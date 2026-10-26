# Unified player menu rollout — T4C-0129

Status: the user approved the design on 2026-10-25. The menu renderers now use the approved journal theme; no sprite packs were replaced.

The existing `JournalTheme` and `CharacterPanel` supply the reference palette and controls. Use the same implementation across menu families rather than recoloring each legacy background independently. Keep the world and character/item/spell art; replace the window and control chrome. The game remains English-only and new visible labels belong in the existing i18n catalogue.

## Coverage

| Player surface | Implementation | Coverage |
|---|---|---|
| Buy and sell | ShopScreen, GuiListScreen | Journal frame, clear balance/transaction totals, inset rows, selected states, quantity steppers and full-size action button. |
| Teach spells, learn skills and crafting formulas | LearnScreen, GuiListScreen | Same transaction layout; retain formula/recipe offers, requirements and reasons an entry cannot be learned. |
| Train known skills | LearnScreen training modes | Match teaching; retain current/cap values, point and gold costs, bulk selection and hold-to-repeat. |
| Train attributes | TrainScreen | Shared frame, aligned current/cost columns, shaded increment controls and readable balance. |
| Quest journal | QuestScreen | Already themed; verify category, stage, next action, rewards, kill-log link and long-text scrolling fit the common controls. |
| NPC conversation and yes/no prompts | NameRenderer, NpcBehaviorContext/GameChat | Theme speech surfaces and keyword/yes-no presentation without adding a blocking conversation window or changing dialogue dispatch. |
| Personal storage and Bloodline Vault | StorageScreen | Finish the staged storage design, using paired backpack/storage grids and separate balances. Preserve stack quantity selection in both directions. |
| Quantity selection | ItemQuantityConfirmScreen | Already themed; verify it works consistently for drop, junk and storage, including one item, a full stack and cancellation. |
| Character, inventory, spells, powers/resists, macros | Statistics, Inventory, SpellBook, ElementalStats, MacrosScreen | Already themed reference pages; audit remaining legacy spin controls, tooltip styling and disabled/selected states. |
| Appearance | RobeWardrobeScreen | Already a CharacterPanel; audit control and selection consistency. |
| Wiki and Adventure | WikiScreen, AdventureScreen | Already themed; audit search focus, navigation, text clipping and scrolling. |
| Options | OptionsScreen, GuiSlider, GuiCheckbox, GuiOptionList | Journal sections and matching sliders, toggles and selectors; retain persistence and existing available settings. |
| Controls | ControlsScreen | Journal frame and topic navigation; preserve keyboard help contents and scrolling. |
| Locations | LocationsScreen | Journal list/detail layout; keep locked/unreachable locations hidden and existing travel behavior. |
| World map | MapScreen, GuiWorldMap | Journal map frame and controls around the existing map; preserve projection, markers and pan/zoom behavior. |
| Kill log | MonsterKillLogScreen | Journal frame, readable aligned kill counts, existing ordering and scroll behavior. |
| Quit / switch character | ExitGameConfirmScreen, SwitchCharacterConfirmScreen | Compact matching confirmation windows; preserve explicit confirmation and Escape/cancel. |
| Character selection / create / delete / class picker | CharacterSelectionScreen | Journal window/control chrome with existing character previews, class descriptions and creation rules; preserve delete confirmation. |
| Loading | LoadingScreen, CharacterLoadingScreen | Matching text, progress treatment and backdrop; preserve loading and error behavior. |

The map editor is a separate authoring application, outside these player menus.

Both personal storage and the Bloodline Vault are included. The title follows the selected backend immediately. Returning from the quit confirmation recreates the Options window with its original callback and saved preferences rather than reusing disposed textures.

## Shared implementation boundaries

- Add a common standalone journal window base for NPC/service windows, without forcing personal character tabs into a merchant or confirmation.
- Keep the current personal-journal navigation. Small dialogs need a compact frame, not a full-page header/tab strip.
- Migrate `GuiListScreen` first so buying, selling, spell learning and all skill-training modes inherit the same layout and input bounds.
- Use `JournalTheme` button, inset and typography helpers for hover, pressed, selected and disabled states. Do not apply a blind global sprite substitution: old art contains labels and different hit boxes.
- Dispose each window's generated textures when it closes. Reuse per-window button art and avoid generating textures per frame or list rebuild.
- Keep title/close actions, content wells and footer actions at consistent insets. Long item names and requirements must wrap or reveal full text without covering price/quantity controls.
- Keep compact item and spell art, scroll wheels, Shift shortcuts, keyboard escape, mouse capture and repeat timing. Do not rebalance costs, requirements or rewards as part of this visual pass.

## Verification after installation

Render every changed family at the normal 1280 × 768 game resolution. Check long names, empty lists, a disabled purchase/lesson, insufficient funds/points, populated lists requiring scrolling, quantity controls, cancelled transfers and closed/reopened windows. Reuse focused coverage for transactions and quantity transfers; add tests only for changed input/layout contracts that could break behavior. Run the Maven build before handing the new game UI back.

Completed checks: 35 actual menu states rendered offscreen at 1280 × 768 without OpenGL errors, including an empty shop, unaffordable items, the world map, both storage backends, journal pages, character creation, loading and NPC speech. The smoke check verifies a quantity-button click updates the basket without spending gold, confirms the required arrow/minus glyphs exist, and reopens Options after cancelling Quit. It never confirms transactions, transfers or character creation.

The 45 focused tests cover option-toggle hit targets, controls content, quantity bounds, personal/account storage, sale pricing, lesson costs, character creation rules, locations and live quest-journal data.
