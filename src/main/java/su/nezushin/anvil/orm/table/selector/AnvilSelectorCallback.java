package su.nezushin.anvil.orm.table.selector;

import su.nezushin.anvil.orm.table.AnvilORMSerializable;

public interface AnvilSelectorCallback<T extends AnvilORMSerializable> {

    public void run(SimpleSelector<T> selector);
}
