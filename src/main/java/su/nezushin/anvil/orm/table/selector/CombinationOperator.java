package su.nezushin.anvil.orm.table.selector;

import java.util.stream.Stream;

public enum CombinationOperator {
    AND("AND"), OR("OR");


    private String operator;

    CombinationOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public static CombinationOperator getByOperator(String operator) {
        return Stream.of(CombinationOperator.values()).filter(a -> operator.equalsIgnoreCase(a.operator)).findFirst().orElse(null);
    }
}
