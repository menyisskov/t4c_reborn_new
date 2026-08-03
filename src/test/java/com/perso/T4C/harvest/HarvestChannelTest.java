package com.perso.T4C.harvest;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HarvestChannelTest {
    @Test
    void completesExactlyAfterFiveSecondsWithoutRandomFailure() {
        HarvestChannel channel = new HarvestChannel(5f);
        assertFalse(channel.update(4.99f));
        assertTrue(channel.getProgress() > .99f);
        assertTrue(channel.update(.01f));
        assertEquals(1f, channel.getProgress());
    }

    @Test
    void cancellationNeverCompletes() {
        HarvestChannel channel = new HarvestChannel(5f);
        channel.cancel();
        assertTrue(channel.isCancelled());
        assertFalse(channel.update(10f));
    }

    @Test
    void movementCancelsHarvestImmediately() {
        Vector2 position = new Vector2(100f, 200f);
        HarvestChannel channel = new HarvestChannel(5f, .5f, () -> position);
        assertFalse(channel.update(1f));
        position.x += 1f;
        assertFalse(channel.update(1f));
        assertTrue(channel.isCancelled());
        assertEquals(.2f, channel.getProgress(), .0001f);
    }
}
