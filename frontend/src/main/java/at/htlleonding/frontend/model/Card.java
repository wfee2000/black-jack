package at.htlleonding.frontend.model;

import java.util.List;

public record Card(Sign sign, Value value) {
    public static int getSum(List<Card> cards) {
        return cards.stream().map(card -> card.value().getValue())
            .sorted((o1, o2) -> Integer.compare(o2, o1)).reduce((o1, o2) -> {
                if (o2 == 0) {
                    if (o1 + 11 > 21) {
                        return o1 + 1;
                    }

                    return 11;
                }

                return o1 + o2;
            }).orElse(0);
    }
}
