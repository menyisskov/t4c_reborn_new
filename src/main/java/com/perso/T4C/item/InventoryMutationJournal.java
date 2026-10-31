package com.perso.T4C.item;

import com.perso.T4C.item.JournaledInventoryState.Change;
import com.perso.T4C.item.JournaledInventoryState.Container;
import com.perso.T4C.item.JournaledInventoryState.Mutation;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Optional capture of original inventory operations and every write to their Player views. */
public final class InventoryMutationJournal {
  public record Projection(List<String> inventory, Map<BodyPart, String> equipment,
      Map<String, Integer> charges) {
    public Projection {
      inventory = Collections.unmodifiableList(new ArrayList<>(inventory));
      equipment = Collections.unmodifiableMap(new HashMap<>(equipment));
      charges = Collections.unmodifiableMap(new HashMap<>(charges));
    }
  }
  public record Operation(int id, int parentId, String name, List<Object> arguments, Object result) {}
  /** Only mutations describe spending; nested operations are provenance, never extra spends. */
  public record Prepared(Projection initial, Projection finalState, List<Operation> operations,
      List<Mutation> mutations) {}

  private final Player player;
  private final Projection initial;
  private final JournaledInventoryState state;
  private final List<Mutation> mutations = new ArrayList<>();
  private final List<Operation> operations = new ArrayList<>();
  private final Deque<Integer> stack = new ArrayDeque<>();
  private int nextOperationId = 1;
  private boolean invalid;
  private boolean closed;

  public InventoryMutationJournal(Player player, List<String> inventory,
      Map<BodyPart, String> equipment, Map<String, Integer> charges) {
    this.player = Objects.requireNonNull(player);
    this.initial = new Projection(inventory, equipment, charges);
    this.state = new JournaledInventoryState(this, inventory, equipment, charges);
  }

  public JournaledInventoryState state() { return state; }
  public Projection initial() { return initial; }
  public boolean isInvalid() { return invalid; }
  public boolean isClosed() { return closed; }

  public <T> T capture(String name, Supplier<T> original, Object... arguments) {
    requireOpen();
    int id = nextOperationId++;
    int parent = stack.isEmpty() ? 0 : stack.peek();
    List<Object> args = new ArrayList<>(Arrays.asList(arguments.clone()));
    stack.push(id);
    try {
      T result = original.get();
      operations.add(new Operation(id, parent, name, Collections.unmodifiableList(args), result));
      return result;
    } catch (RuntimeException | Error failure) {
      invalid = true;
      throw failure;
    } finally {
      stack.pop();
    }
  }

  void record(Container container, Change change, Object key, Object before, Object after,
      boolean existed) {
    requireOpen();
    mutations.add(new Mutation(container, change, key, before, after, existed,
        stack.isEmpty() ? 0 : stack.peek()));
  }

  public Prepared seal() {
    requireOpen();
    if (invalid || !stack.isEmpty()) throw new IllegalStateException("Inventory journal is invalid or busy");
    Projection actual = new Projection(state.inventory(), state.equipment(), state.charges());
    Projection replayed;
    try {
      replayed = replay();
    } catch (RuntimeException failure) {
      invalid = true;
      throw failure;
    }
    if (!replayed.equals(actual)) {
      invalid = true;
      throw new IllegalStateException("Unexplained inventory write; preparation refused");
    }
    closed = true;
    player.finishInventoryMutationJournal(this, false);
    List<Operation> ordered = new ArrayList<>(operations);
    ordered.sort(Comparator.comparingInt(Operation::id));
    return new Prepared(initial, actual, List.copyOf(ordered), List.copyOf(mutations));
  }

  /** Restores only inventory/equipment/charge state; callers restore other Player state separately. */
  public void rollback() {
    if (closed) throw new IllegalStateException("Inventory journal already closed");
    closed = true;
    player.finishInventoryMutationJournal(this, true);
  }

  private void requireOpen() {
    if (closed || invalid) throw new IllegalStateException("Inventory journal is not open");
  }

  private Projection replay() {
    List<String> inventory = new ArrayList<>(initial.inventory());
    Map<BodyPart, String> equipment = new HashMap<>(initial.equipment());
    Map<String, Integer> charges = new HashMap<>(initial.charges());
    for (Mutation mutation : mutations) {
      if (mutation.container() == Container.INVENTORY) {
        int index = (Integer) mutation.key();
        switch (mutation.change()) {
          case INSERT -> inventory.add(index, (String) mutation.after());
          case DELETE -> {
            if (!Objects.equals(inventory.get(index), mutation.before()))
              throw new IllegalStateException("Inventory removal replay mismatch");
            inventory.remove(index);
          }
          case SET -> {
            if (!Objects.equals(inventory.get(index), mutation.before()))
              throw new IllegalStateException("Inventory replacement replay mismatch");
            inventory.set(index, (String) mutation.after());
          }
          default -> throw new IllegalStateException("Invalid inventory mutation");
        }
      } else if (mutation.container() == Container.EQUIPMENT) {
        replayMap(equipment, mutation);
      } else {
        replayMap(charges, mutation);
      }
    }
    return new Projection(inventory, equipment, charges);
  }

  @SuppressWarnings("unchecked")
  private static <K, V> void replayMap(Map<K, V> map, Mutation mutation) {
    K key = (K) mutation.key();
    if (map.containsKey(key) != mutation.existed()
        || (mutation.existed() && !Objects.equals(map.get(key), mutation.before())))
      throw new IllegalStateException("Map mutation replay mismatch");
    if (mutation.change() == Change.PUT) map.put(key, (V) mutation.after());
    else if (mutation.change() == Change.REMOVE) map.remove(key);
    else throw new IllegalStateException("Invalid map mutation");
  }
}
