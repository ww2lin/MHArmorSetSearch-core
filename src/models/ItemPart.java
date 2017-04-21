package models;

import interfaces.Nullable;

public class ItemPart implements Nullable {
    String name;
    int amount;

    private ItemPart(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public static ItemPart createMonsterPart(String name, int amount) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return new ItemPart(name, amount);
    }

    @Override
    public String toString() {
        return "ItemPart{" +
            "kind='" + name + '\'' +
            ", amount=" + amount +
            '}';
    }

    @Override
    public boolean isNull() {
        return name == null || name.trim().length() == 0;
    }
}
