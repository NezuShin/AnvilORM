package su.nezushin.anvil.orm.table.selector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum ComparisonOperator {

    LESS("<"),
    GREATER(">"),
    LESS_OR_EQUAL("<="),
    GREATER_OR_EQUAL(">="),
    EQUAL("=", "=="),
    NOTEQUAL("<>", "!=");

    private List<String> operators;

    private ComparisonOperator(String... operator) {
        this.operators = Arrays.asList(operator);

    }

    public List<String> getOperators() {
        return operators;
    }

    public static ComparisonOperator getByOperator(String operator) {
        return Stream.of(ComparisonOperator.values()).filter(a -> a.operators.stream().anyMatch(i -> i.equalsIgnoreCase(operator))).findFirst().orElse(null);
    }


}
