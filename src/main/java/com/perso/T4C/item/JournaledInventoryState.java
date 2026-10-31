package com.perso.T4C.item;

import com.perso.T4C.player.BodyPart;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Mutable original Player views whose every write passes through the attached journal. */
public final class JournaledInventoryState {
  public enum Container { INVENTORY, EQUIPMENT, CHARGES }
  public enum Change { INSERT, DELETE, SET, PUT, REMOVE }

  public record Mutation(Container container, Change change, Object key, Object before,
      Object after, boolean existed, int operationId) {}

  private final InventoryMutationJournal journal;
  private final TrackedList inventory;
  private final TrackedMap<BodyPart, String> equipment;
  private final TrackedMap<String, Integer> charges;

  JournaledInventoryState(InventoryMutationJournal journal, List<String> inventory,
      Map<BodyPart, String> equipment, Map<String, Integer> charges) {
    this.journal = journal;
    this.inventory = new TrackedList(inventory);
    this.equipment = new TrackedMap<>(Container.EQUIPMENT, equipment);
    this.charges = new TrackedMap<>(Container.CHARGES, charges);
  }

  public List<String> inventory() { return inventory; }
  public Map<BodyPart, String> equipment() { return equipment; }
  public Map<String, Integer> charges() { return charges; }

  private final class TrackedList extends AbstractList<String> {
    private final ArrayList<String> values;

    private TrackedList(List<String> source) { values = new ArrayList<>(source); }
    @Override public int size() { return values.size(); }
    @Override public String get(int index) { return values.get(index); }
    @Override public String set(int index, String value) {
      String before = values.get(index);
      journal.record(Container.INVENTORY, Change.SET, index, before, value, true);
      return values.set(index, value);
    }
    @Override public void add(int index, String value) {
      if (index < 0 || index > values.size()) throw new IndexOutOfBoundsException(index);
      journal.record(Container.INVENTORY, Change.INSERT, index, null, value, false);
      values.add(index, value);
      modCount++;
    }
    @Override public String remove(int index) {
      String before = values.get(index);
      journal.record(Container.INVENTORY, Change.DELETE, index, before, null, true);
      String result = values.remove(index);
      modCount++;
      return result;
    }
    @Override protected void removeRange(int fromIndex, int toIndex) {
      for (int index = toIndex - 1; index >= fromIndex; index--) remove(index);
    }
    @Override public void clear() { removeRange(0, size()); }
  }

  private final class TrackedMap<K, V> extends AbstractMap<K, V> {
    private final Container container;
    private final HashMap<K, V> values;
    private final Set<Entry<K, V>> entries = new AbstractSet<>() {
      @Override public int size() { return values.size(); }
      @Override public Iterator<Entry<K, V>> iterator() {
        Iterator<Entry<K, V>> inner = values.entrySet().iterator();
        return new Iterator<>() {
          private Entry<K, V> last;
          @Override public boolean hasNext() { return inner.hasNext(); }
          @Override public Entry<K, V> next() {
            last = inner.next();
            K key = last.getKey();
            return new Entry<>() {
              @Override public K getKey() { return key; }
              @Override public V getValue() { return values.get(key); }
              @Override public V setValue(V value) { return TrackedMap.this.put(key, value); }
              @Override public boolean equals(Object object) {
                return object instanceof Entry<?, ?> entry
                    && Objects.equals(key, entry.getKey())
                    && Objects.equals(getValue(), entry.getValue());
              }
              @Override public int hashCode() {
                return Objects.hashCode(key) ^ Objects.hashCode(getValue());
              }
            };
          }
          @Override public void remove() {
            if (last == null) throw new IllegalStateException();
            K key = last.getKey();
            V before = last.getValue();
            journal.record(container, Change.REMOVE, key, before, null, true);
            inner.remove();
            last = null;
          }
        };
      }
    };

    private TrackedMap(Container container, Map<K, V> source) {
      this.container = container;
      this.values = new HashMap<>(source);
    }
    @Override public int size() { return values.size(); }
    @Override public boolean containsKey(Object key) { return values.containsKey(key); }
    @Override public V get(Object key) { return values.get(key); }
    @Override public V put(K key, V value) {
      boolean existed = values.containsKey(key);
      V before = values.get(key);
      journal.record(container, Change.PUT, key, before, value, existed);
      return values.put(key, value);
    }
    @Override public V remove(Object key) {
      if (!values.containsKey(key)) return null;
      V before = values.get(key);
      journal.record(container, Change.REMOVE, key, before, null, true);
      return values.remove(key);
    }
    @Override public void clear() {
      Iterator<Entry<K, V>> iterator = entrySet().iterator();
      while (iterator.hasNext()) { iterator.next(); iterator.remove(); }
    }
    @Override public Set<Entry<K, V>> entrySet() { return entries; }
  }
}
