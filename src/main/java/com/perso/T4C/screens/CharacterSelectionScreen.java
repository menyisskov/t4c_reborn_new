package com.perso.T4C.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perso.T4C.MyGame;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.helper.CharacterCreationRules;
import com.perso.T4C.helper.LocalCharacterStore;
import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Faithful local implementation of the 1.68 character selection and creation flow. */
public final class CharacterSelectionScreen extends InputAdapter implements Screen {
    private static final float PANEL_ALPHA = 180f / 255f;
    private static final int VISIBLE_ROWS = 4;

    private final MyGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ScreenViewport viewport;
    private final Vector2 pointer = new Vector2();
    private final GlyphLayout layout = new GlyphLayout();
    private final Random random = new Random();
    private final TextureRegion background;

    private final TextureRegion title;
    private final TextureRegion panel;
    private final TextureRegion panelExtension;
    private final TextureRegion largeNormal;
    private final TextureRegion largeHover;
    private final TextureRegion largeDisabled;
    private final TextureRegion smallNormal;
    private final TextureRegion smallHover;
    private final TextureRegion rowHighlight;
    private final TextureRegion upNormal;
    private final TextureRegion upHover;
    private final TextureRegion downNormal;
    private final TextureRegion downHover;
    private final TextureRegion questionPanel;
    private final TextureRegion questionHighlight;
    private final TextureRegion rerollPanel;

    private final BitmapFont buttonFont;
    private final BitmapFont listFont;
    private final BitmapFont goldFont;
    private final BitmapFont whiteFont;
    private final BitmapFont redFont;
    private final BitmapFont grayFont;
    private final BitmapFont questionFont;

    private List<LocalCharacterStore.CharacterSlot> characters = List.of();
    private List<Integer> levels = List.of();
    private int selected;
    private int firstVisible;
    private Mode mode = Mode.SELECT;
    private String pendingName = "";
    private String pendingGender = LocalCharacterStore.MALE;
    private String errorMessage;
    private List<QuestionRun> questionnaire = List.of();
    private int questionIndex;
    private int selectedAnswer;
    private final int[] affinities = new int[CharacterCreationRules.AFFINITY_COUNT];
    private CharacterCreationRules.Stats rolledStats;

    public CharacterSelectionScreen(MyGame game) {
        this.game = game;
        this.batch = game.batch;
        camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new ScreenViewport(camera);

        background = loadSprite("Back01_1280");
        title = loadSprite("Connect_Title2");
        panel = loadSprite("PS_Back");
        panelExtension = loadSprite("PS_BackDown");
        largeNormal = loadSprite("PS_BtnN");
        largeHover = loadSprite("PS_BtnH");
        largeDisabled = loadSprite("PS_BtnD");
        smallNormal = loadSprite("PS_SmallBtnN");
        smallHover = loadSprite("PS_SmallBtnH");
        rowHighlight = loadSprite("PS_Over");
        upNormal = loadSprite("PS_SBtnUPN");
        upHover = loadSprite("PS_SBtnUPH");
        downNormal = loadSprite("PS_SBtnDNN");
        downHover = loadSprite("PS_SBtnDNH");
        questionPanel = loadSprite("Q_Back");
        questionHighlight = loadSprite("Q_BackSelect");
        rerollPanel = loadSprite("J_Back");

        FontManager fonts = FontManager.getInstance();
        buttonFont = fonts.getT4CBeaulieuFont(17, Color.BLACK);
        listFont = fonts.getT4CBeaulieuFont(17, Color.WHITE);
        goldFont = fonts.getT4CBeaulieuFont(17, new Color(222 / 255f, 158 / 255f, 0f, 1f));
        whiteFont = fonts.getT4CBeaulieuFont(17, Color.WHITE);
        redFont = fonts.getT4CBeaulieuFont(17, new Color(0.63f, 0.08f, 0.08f, 1f));
        grayFont = fonts.getT4CBeaulieuFont(17, Color.GRAY);
        questionFont = fonts.getT4CBeaulieuFont(19, new Color(222 / 255f, 158 / 255f, 0f, 1f));

        refreshCharacters();
        if (characters.isEmpty() && errorMessage == null) {
            startCreation();
        }
    }

    private static TextureRegion loadSprite(String name) {
        try {
            TextureRegion region = SpriteLoader.getInstance().getRegionFromSpriteName(name);
            if (region == null) {
                throw new IllegalStateException("Missing character-selection sprite: " + name);
            }
            return region;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load character-selection sprite: " + name, e);
        }
    }

    @Override
    public void render(float delta) {
        updatePointer(Gdx.input.getX(), Gdx.input.getY());

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        GuiDraw.drawRegionFlipped(batch, background, 0f, 0f,
                camera.viewportWidth, camera.viewportHeight);
        float titleY = titleOffset();
        GuiDraw.drawRegionFlipped(batch, title, (camera.viewportWidth - title.getRegionWidth()) / 2f, titleY);

        switch (mode) {
            case SELECT, NAME, SEX, DELETE_CONFIRM -> drawCharacterPanel();
            case QUESTIONS -> drawQuestionnaire(titleY + title.getRegionHeight() + 20f);
            case REROLL -> drawRerollPanel();
        }
        batch.end();
    }

    private void drawCharacterPanel() {
        float x = panelX();
        float y = panelY();
        drawTranslucent(panel, x, y);
        if (mode == Mode.NAME || mode == Mode.SEX || mode == Mode.DELETE_CONFIRM) {
            drawTranslucent(panelExtension, x, y + panel.getRegionHeight());
        }
        if (mode == Mode.NAME && errorMessage != null) {
            drawTranslucent(panelExtension, x, y + panel.getRegionHeight()
                    + panelExtension.getRegionHeight());
        }

        drawCentered(goldFont, I18n.key("character.column.name"), x + 16f, y + 44f, 191f);
        drawCentered(goldFont, I18n.key("character.column.level"), x + 215f, y + 44f, 55f);

        float rowY = y + 76f;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = firstVisible + row;
            if (index >= characters.size()) break;
            if (index == selected) {
                GuiDraw.drawRegionFlipped(batch, rowHighlight, x + 16f, rowY);
            }
            LocalCharacterStore.CharacterSlot slot = characters.get(index);
            listFont.draw(batch, (index + 1) + ". " + slot.name(), x + 26f, rowY + 3f);
            listFont.draw(batch, Integer.toString(levels.get(index)), x + 221f, rowY + 3f);
            rowY += 30f;
        }

        if (mode == Mode.SELECT) {
            drawSelectionControls(x, y);
        } else if (mode == Mode.DELETE_CONFIRM) {
            goldFont.draw(batch, I18n.message("character.delete.confirm", selectedSlot().name()),
                    x + 16f, y + 244f);
            drawSmallButton(x + 289f, y + 243f, I18n.key("character.yes"), true);
            drawSmallButton(x + 367f, y + 243f, I18n.key("character.no"), true);
        } else if (mode == Mode.NAME) {
            goldFont.draw(batch, I18n.key("character.name.prompt"), x + 16f, y + 244f);
            whiteFont.draw(batch, pendingName + "_", x + 301f, y + 244f);
            if (errorMessage != null) {
                drawCentered(redFont, errorMessage, x + 6f, y + 309f, 441f);
            }
        } else if (mode == Mode.SEX) {
            goldFont.draw(batch, I18n.message("character.gender.prompt", pendingName),
                    x + 16f, y + 244f);
            drawSmallButton(x + 289f, y + 243f, I18n.key("character.gender.male"), true);
            drawSmallButton(x + 367f, y + 243f, I18n.key("character.gender.female"), true);
        }
    }

    private void drawSelectionControls(float x, float y) {
        boolean hasCharacter = !characters.isEmpty();
        drawLargeButton(x + 318f, y + 67f, I18n.key("character.enter"), hasCharacter);
        if (characters.size() < LocalCharacterStore.MAX_CHARACTERS) {
            drawLargeButton(x + 318f, y + 99f, I18n.key("character.create"), true);
        }
        drawLargeButton(x + 318f, y + 131f, I18n.key("character.delete"), hasCharacter);
        drawLargeButton(x + 318f, y + 177f, I18n.key("character.back"), true);

        float upX = x + 278f;
        float upY = y + 76f;
        GuiDraw.drawRegionFlipped(batch, contains(upX, upY, 20f, 21f) ? upHover : upNormal, upX, upY);
        float downY = y + 170f;
        GuiDraw.drawRegionFlipped(batch, contains(upX, downY, 20f, 21f) ? downHover : downNormal,
                upX, downY);
    }

    private void drawQuestionnaire(float preferredY) {
        float x = (camera.viewportWidth - questionPanel.getRegionWidth()) / 2f;
        float y = Math.min(preferredY, camera.viewportHeight - questionPanel.getRegionHeight());
        y = Math.max(0f, y);
        drawTranslucent(questionPanel, x, y);
        GuiDraw.drawRegionFlipped(batch, questionHighlight, x + 17f,
                y + 106f + selectedAnswer * 48f);

        QuestionRun run = questionnaire.get(questionIndex);
        String prefix = "character.question." + (run.sourceIndex + 1);
        questionFont.draw(batch, I18n.key(prefix + ".prompt"), x + 18f, y + 34f,
                500f, Align.left, true);
        for (int row = 0; row < CharacterCreationRules.AFFINITY_COUNT; row++) {
            int answer = run.answerOrder.get(row);
            whiteFont.draw(batch, I18n.key(prefix + ".answer." + (answer + 1)),
                    x + 18f, y + 106f + row * 48f, 500f, Align.left, true);
        }
        drawSmallButton(x + 537f, y + 265f, I18n.key("character.continue"), true);
        drawSmallButton(x + 537f, y + 313f, I18n.key("character.back"), true);
    }

    private void drawRerollPanel() {
        float x = (camera.viewportWidth - rerollPanel.getRegionWidth()) / 2f;
        float y = (camera.viewportHeight - rerollPanel.getRegionHeight()) / 2f + 30f;
        drawTranslucent(rerollPanel, x, y);
        drawCentered(goldFont, I18n.key("character.stats.title"), x, y + 44f, 254f);

        String[] labels = {
                I18n.key("character.stats.strength"), I18n.key("character.stats.endurance"),
                I18n.key("character.stats.dexterity"), I18n.key("character.stats.wisdom"),
                I18n.key("character.stats.intelligence"), I18n.key("character.stats.hp")
        };
        int[] values = {rolledStats.strength(), rolledStats.endurance(), rolledStats.dexterity(),
                rolledStats.wisdom(), rolledStats.intelligence(), rolledStats.maxHp()};
        for (int i = 0; i < labels.length; i++) {
            grayFont.draw(batch, labels[i], x + 22f, y + 78f + i * 20f);
            whiteFont.draw(batch, Integer.toString(values[i]), x + 175f, y + 78f + i * 20f);
        }
        drawLargeButton(x + 306f, y + 74f, I18n.key("character.stats.accept"), true);
        drawLargeButton(x + 306f, y + 106f, I18n.key("character.stats.reroll"), true);
        drawLargeButton(x + 306f, y + 178f, I18n.key("character.back"), true);
        if (errorMessage != null) {
            redFont.draw(batch, errorMessage, x + 22f, y + 211f, 410f, Align.center, true);
        }
    }

    private void drawLargeButton(float x, float y, String text, boolean enabled) {
        TextureRegion region = !enabled ? largeDisabled
                : contains(x, y, largeNormal.getRegionWidth(), largeNormal.getRegionHeight())
                ? largeHover : largeNormal;
        GuiDraw.drawRegionFlipped(batch, region, x, y);
        if (enabled) drawCentered(buttonFont, text, x, y + 3f, largeNormal.getRegionWidth());
    }

    private void drawSmallButton(float x, float y, String text, boolean enabled) {
        TextureRegion region = enabled && contains(x, y, smallNormal.getRegionWidth(), smallNormal.getRegionHeight())
                ? smallHover : smallNormal;
        GuiDraw.drawRegionFlipped(batch, region, x, y);
        if (enabled) drawCentered(buttonFont, text, x, y + 3f, smallNormal.getRegionWidth());
    }

    private void drawCentered(BitmapFont font, String text, float x, float y, float width) {
        layout.setText(font, text);
        font.draw(batch, layout, x + (width - layout.width) / 2f, y);
    }

    private void drawTranslucent(TextureRegion region, float x, float y) {
        Color previous = new Color(batch.getColor());
        batch.setColor(previous.r, previous.g, previous.b, previous.a * PANEL_ALPHA);
        GuiDraw.drawRegionFlipped(batch, region, x, y);
        batch.setColor(previous);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointerId, int button) {
        if (button != Input.Buttons.LEFT) return false;
        updatePointer(screenX, screenY);
        click();
        return true;
    }

    private void click() {
        switch (mode) {
            case SELECT -> clickSelection();
            case DELETE_CONFIRM -> clickDeleteConfirmation();
            case SEX -> clickGender();
            case QUESTIONS -> clickQuestionnaire();
            case REROLL -> clickReroll();
            case NAME -> { }
        }
    }

    private void clickSelection() {
        float x = panelX();
        float y = panelY();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = firstVisible + row;
            if (index < characters.size() && contains(x + 16f, y + 76f + row * 30f, 256f, 23f)) {
                selected = index;
                playButtonSound();
                return;
            }
        }
        if (contains(x + 278f, y + 76f, 20f, 21f)) {
            moveSelection(-1);
        } else if (contains(x + 278f, y + 170f, 20f, 21f)) {
            moveSelection(1);
        } else if (!characters.isEmpty() && contains(x + 318f, y + 67f, 116f, 27f)) {
            playButtonSound();
            enterSelectedCharacter();
        } else if (characters.size() < LocalCharacterStore.MAX_CHARACTERS
                && contains(x + 318f, y + 99f, 116f, 27f)) {
            playButtonSound();
            startCreation();
        } else if (!characters.isEmpty() && contains(x + 318f, y + 131f, 116f, 27f)) {
            playButtonSound();
            mode = Mode.DELETE_CONFIRM;
        } else if (contains(x + 318f, y + 177f, 116f, 27f)) {
            Gdx.app.exit();
        }
    }

    private void clickDeleteConfirmation() {
        float x = panelX();
        float y = panelY();
        if (contains(x + 289f, y + 243f, 72f, 27f)) {
            playButtonSound();
            deleteSelectedCharacter();
        } else if (contains(x + 367f, y + 243f, 72f, 27f)) {
            playButtonSound();
            mode = Mode.SELECT;
        }
    }

    private void clickGender() {
        float x = panelX();
        float y = panelY();
        if (contains(x + 289f, y + 243f, 72f, 27f)) {
            pendingGender = LocalCharacterStore.MALE;
            playButtonSound();
            startQuestionnaire();
        } else if (contains(x + 367f, y + 243f, 72f, 27f)) {
            pendingGender = LocalCharacterStore.FEMALE;
            playButtonSound();
            startQuestionnaire();
        }
    }

    private void clickQuestionnaire() {
        float x = (camera.viewportWidth - questionPanel.getRegionWidth()) / 2f;
        float y = Math.max(0f, Math.min(titleOffset() + title.getRegionHeight() + 20f,
                camera.viewportHeight - questionPanel.getRegionHeight()));
        for (int row = 0; row < CharacterCreationRules.AFFINITY_COUNT; row++) {
            if (contains(x + 17f, y + 106f + row * 48f, 500f, 40f)) {
                selectedAnswer = row;
                return;
            }
        }
        if (contains(x + 537f, y + 265f, 72f, 27f)) {
            playButtonSound();
            acceptQuestionAnswer();
        } else if (contains(x + 537f, y + 313f, 72f, 27f)) {
            playButtonSound();
            cancelCreation();
        }
    }

    private void clickReroll() {
        float x = (camera.viewportWidth - rerollPanel.getRegionWidth()) / 2f;
        float y = (camera.viewportHeight - rerollPanel.getRegionHeight()) / 2f + 30f;
        if (contains(x + 306f, y + 74f, 116f, 27f)) {
            playButtonSound();
            finishCreation();
        } else if (contains(x + 306f, y + 106f, 116f, 27f)) {
            playButtonSound();
            rolledStats = CharacterCreationRules.roll(affinities, random);
        } else if (contains(x + 306f, y + 178f, 116f, 27f)) {
            playButtonSound();
            cancelCreation();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (mode) {
            case SELECT -> handleSelectionKey(keycode);
            case NAME -> handleNameKey(keycode);
            case SEX -> handleGenderKey(keycode);
            case DELETE_CONFIRM -> handleDeleteKey(keycode);
            case QUESTIONS -> handleQuestionKey(keycode);
            case REROLL -> handleRerollKey(keycode);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        if (mode != Mode.NAME || Character.isISOControl(character)) return false;
        if (pendingName.length() < CharacterCreationRules.MAX_NAME_LENGTH
                && (Character.isLetter(character) || character == ' ' || character == '-'
                || character == '\'')) {
            pendingName += character;
            errorMessage = null;
        }
        return true;
    }

    private void handleSelectionKey(int keycode) {
        if (keycode == Input.Keys.UP) moveSelection(-1);
        else if (keycode == Input.Keys.DOWN) moveSelection(1);
        else if (keycode == Input.Keys.ENTER && !characters.isEmpty()) enterSelectedCharacter();
        else if (keycode == Input.Keys.INSERT && characters.size() < LocalCharacterStore.MAX_CHARACTERS) startCreation();
        else if (keycode == Input.Keys.FORWARD_DEL && !characters.isEmpty()) mode = Mode.DELETE_CONFIRM;
        else if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
        else if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
            int index = keycode - Input.Keys.NUM_1;
            if (index < characters.size()) selected = index;
        }
    }

    private void handleNameKey(int keycode) {
        if (keycode == Input.Keys.BACKSPACE && !pendingName.isEmpty()) {
            pendingName = pendingName.substring(0, pendingName.length() - 1);
            errorMessage = null;
        } else if (keycode == Input.Keys.ENTER) {
            validateNameAndContinue();
        } else if (keycode == Input.Keys.ESCAPE) {
            cancelCreation();
        }
    }

    private void handleGenderKey(int keycode) {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.M) {
            pendingGender = LocalCharacterStore.MALE;
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.F) {
            pendingGender = LocalCharacterStore.FEMALE;
        } else if (keycode == Input.Keys.ENTER) {
            startQuestionnaire();
        } else if (keycode == Input.Keys.ESCAPE) {
            mode = Mode.NAME;
        }
    }

    private void handleDeleteKey(int keycode) {
        if (keycode == Input.Keys.ENTER || keycode == Input.Keys.Y || keycode == Input.Keys.O) {
            deleteSelectedCharacter();
        } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.N) {
            mode = Mode.SELECT;
        }
    }

    private void handleQuestionKey(int keycode) {
        if (keycode == Input.Keys.UP) selectedAnswer = (selectedAnswer + 4) % 5;
        else if (keycode == Input.Keys.DOWN) selectedAnswer = (selectedAnswer + 1) % 5;
        else if (keycode == Input.Keys.ENTER) acceptQuestionAnswer();
        else if (keycode == Input.Keys.ESCAPE) cancelCreation();
        else if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_5) {
            selectedAnswer = keycode - Input.Keys.NUM_1;
        }
    }

    private void handleRerollKey(int keycode) {
        if (keycode == Input.Keys.ENTER) finishCreation();
        else if (keycode == Input.Keys.R) rolledStats = CharacterCreationRules.roll(affinities, random);
        else if (keycode == Input.Keys.ESCAPE) cancelCreation();
    }

    private void startCreation() {
        pendingName = "";
        pendingGender = LocalCharacterStore.MALE;
        errorMessage = null;
        mode = Mode.NAME;
    }

    private void validateNameAndContinue() {
        pendingName = CharacterCreationRules.normalizeName(pendingName);
        if (!CharacterCreationRules.isValidName(pendingName)) {
            errorMessage = I18n.key("character.name.invalid");
            return;
        }
        if (characters.stream().anyMatch(slot -> slot.name().equalsIgnoreCase(pendingName))) {
            errorMessage = I18n.key("character.name.exists");
            return;
        }
        errorMessage = null;
        mode = Mode.SEX;
    }

    private void startQuestionnaire() {
        List<Integer> questions = new ArrayList<>();
        for (int i = 0; i < 8; i++) questions.add(i);
        Collections.shuffle(questions, random);
        List<QuestionRun> runs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<Integer> answers = new ArrayList<>();
            for (int answer = 0; answer < 5; answer++) answers.add(answer);
            Collections.shuffle(answers, random);
            runs.add(new QuestionRun(questions.get(i), List.copyOf(answers)));
        }
        questionnaire = List.copyOf(runs);
        java.util.Arrays.fill(affinities, 0);
        questionIndex = 0;
        selectedAnswer = 0;
        mode = Mode.QUESTIONS;
    }

    private void acceptQuestionAnswer() {
        QuestionRun run = questionnaire.get(questionIndex);
        affinities[run.answerOrder.get(selectedAnswer)]++;
        questionIndex++;
        selectedAnswer = 0;
        if (questionIndex >= questionnaire.size()) {
            rolledStats = CharacterCreationRules.roll(affinities, random);
            errorMessage = null;
            mode = Mode.REROLL;
        }
    }

    private void finishCreation() {
        try {
            LocalCharacterStore.CharacterSlot slot = LocalCharacterStore.create(
                    pendingName, pendingGender, rolledStats);
            refreshCharacters();
            selected = Math.max(0, characters.indexOf(slot));
            enterCharacter(slot);
        } catch (Exception e) {
            errorMessage = I18n.key("character.create.failed") + ": " + e.getMessage();
        }
    }

    private void cancelCreation() {
        errorMessage = null;
        mode = Mode.SELECT;
    }

    private void deleteSelectedCharacter() {
        if (characters.isEmpty()) return;
        try {
            LocalCharacterStore.delete(selectedSlot());
            refreshCharacters();
            selected = Math.min(selected, Math.max(0, characters.size() - 1));
            firstVisible = Math.min(firstVisible, Math.max(0, characters.size() - VISIBLE_ROWS));
            if (characters.isEmpty()) startCreation();
            else mode = Mode.SELECT;
        } catch (IOException e) {
            errorMessage = I18n.key("character.delete.failed") + ": " + e.getMessage();
            mode = Mode.SELECT;
        }
    }

    private void enterSelectedCharacter() {
        if (!characters.isEmpty()) enterCharacter(selectedSlot());
    }

    private void enterCharacter(LocalCharacterStore.CharacterSlot slot) {
        try {
            LocalCharacterStore.activate(slot);
            MainGameScreen next = new MainGameScreen(game);
            game.setScreen(next);
            dispose();
        } catch (Exception e) {
            errorMessage = I18n.key("character.load.failed") + ": " + e.getMessage();
            mode = Mode.SELECT;
        }
    }

    private void moveSelection(int direction) {
        if (characters.isEmpty()) return;
        selected = Math.max(0, Math.min(characters.size() - 1, selected + direction));
        if (selected < firstVisible) firstVisible = selected;
        if (selected >= firstVisible + VISIBLE_ROWS) firstVisible = selected - VISIBLE_ROWS + 1;
        playButtonSound();
    }

    private void refreshCharacters() {
        try {
            characters = LocalCharacterStore.list();
            List<Integer> loadedLevels = new ArrayList<>(characters.size());
            for (LocalCharacterStore.CharacterSlot slot : characters) {
                PlayerStateDto state = LocalCharacterStore.loadState(slot);
                loadedLevels.add(state == null ? 1 : Math.max(1, state.level));
            }
            levels = List.copyOf(loadedLevels);
            selected = Math.min(selected, Math.max(0, characters.size() - 1));
            errorMessage = null;
        } catch (IOException e) {
            characters = List.of();
            levels = List.of();
            errorMessage = I18n.key("character.roster.failed") + ": " + e.getMessage();
        }
    }

    private LocalCharacterStore.CharacterSlot selectedSlot() {
        return characters.get(selected);
    }

    private float panelX() {
        return (camera.viewportWidth - panel.getRegionWidth()) / 2f;
    }

    private float panelY() {
        return (camera.viewportHeight - panel.getRegionHeight()) / 2f + 30f;
    }

    private float titleOffset() {
        return camera.viewportHeight == 720f ? 40f : camera.viewportHeight == 900f ? 60f : 20f;
    }

    private boolean contains(float x, float y, float width, float height) {
        return pointer.x >= x && pointer.x <= x + width && pointer.y >= y && pointer.y <= y + height;
    }

    private void updatePointer(float screenX, float screenY) {
        pointer.set(screenX, screenY);
        viewport.unproject(pointer);
    }

    private void playButtonSound() {
        SoundManager.interfaceSound("Button release sound.wav");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == this) Gdx.input.setInputProcessor(null);
    }

    @Override public void pause() { }
    @Override public void resume() { }

    @Override
    public void dispose() {
        // Sprite textures belong to the shared SpriteLoader cache.
    }

    private enum Mode { SELECT, NAME, SEX, DELETE_CONFIRM, QUESTIONS, REROLL }

    private record QuestionRun(int sourceIndex, List<Integer> answerOrder) { }
}
