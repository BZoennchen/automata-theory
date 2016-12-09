package morphismEq.morphisms;

import grammar.impl.SLP;
import symbol.IJezSymbol;

import java.util.List;
import java.util.function.Function;

/**
 * The default implementation of a morphism. This morphism replaces each terminal
 * b by a list of terminals [b_1, b_2 ...].
 *
 * The morphism does not change the type of the input object. It transforms an
 * SLP<N> into an SLP<N>.
 *
 * @author Benedikt Zoennchen
 */
public class GenDefaultMorphism<N1> implements IMorphism<SLP<N1>, SLP<N1>> {

    private final Function<IJezSymbol<N1>, List<IJezSymbol<N1>>> replacement;

    public GenDefaultMorphism(final Function<IJezSymbol<N1>, List<IJezSymbol<N1>>> replacement) {
        this.replacement = replacement;
    }

    @Override
    public SLP<N1> apply(final SLP<N1> slp) {
        SLP<N1> clone = slp.clone();
        clone.getProductions().stream()
                .flatMap(p -> p.getRight().nodeStream())
                .filter(node -> node.getElement().isTerminal())
                .forEach(node -> {
                    replacement.apply(node.getElement()).forEach(element -> node.insertPrevious(element));
                    node.remove();
                });
        return clone;
    }
}
