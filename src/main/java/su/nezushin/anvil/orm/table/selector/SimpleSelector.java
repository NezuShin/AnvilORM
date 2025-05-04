package su.nezushin.anvil.orm.table.selector;

import su.nezushin.anvil.orm.table.AnvilORMSerializable;
import su.nezushin.anvil.orm.table.AnvilORMTable;

public class SimpleSelector<T extends AnvilORMSerializable> extends Selector<T, SimpleSelector<T>> {

    public SimpleSelector(AnvilORMTable<T> table) {
        super(table);
    }

    protected void whereAnd(StringBuilder sb, int position) {
        if (position != 0) sb.append(" ").append(combinationOperator.getOperator()).append(" ");
    }
}
