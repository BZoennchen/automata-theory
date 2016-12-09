package morphismEq.morphisms;

/**
 * A morphism transforms an object of type D into an object of type I.
 *
 * @author Benedikt Zoennchen
 */
public interface IMorphism<D, I> {
    I apply(final D element);
}