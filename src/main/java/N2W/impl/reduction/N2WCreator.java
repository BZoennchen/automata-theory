package N2W.impl.reduction;

import LTW.impl.LTWRule;
import LTW.impl.RankedSymbol;
import N2W.inter.IN2WCreator;
import grammar.impl.SLP;
import utils.Pair;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Reduction implementation of a N2WCreator.
 *
 * @author Benedikt Zoennchen
 */
public class N2WCreator implements IN2WCreator<SLP<Character>, Pair<LTWRule, Integer>, RankedSymbol, Pair<LTWRule, Integer>, N2WState, NestedLetter, StackSymbol, N2WRule, N2W>
 {
    private StateFactory stateFactory;

     public N2WCreator() {
         this.stateFactory = new StateFactory();
     }

     @Override
     public N2WState createFreshState() {
         throw new UnsupportedOperationException("can not create a N2WState (r, j).");
     }

     @Override
     public N2WState createState(final Pair<LTWRule, Integer> name) {
         return new N2WState(name);
     }

     @Override
     public NestedLetter createNestedLetter(RankedSymbol element, boolean opening) {
         return new NestedLetter(element, opening);
     }

     @Override
     public StackSymbol createStackSymbol(Pair<LTWRule, Integer> element) {
         return new StackSymbol(element);
     }

     @Override
     public N2WRule createRule(N2WState srcState, N2WState destState, NestedLetter nestedLetter, StackSymbol stackSymbol, SLP<Character> outputWord) {
         return new N2WRule(srcState, destState, nestedLetter, outputWord, stackSymbol);
     }

     @Override
     public N2W createTransducer(Set<N2WRule> rules, Set<N2WState> initialStates, Set<N2WState> finalStates) {
         return new N2W(rules, initialStates, finalStates);
     }

     private class StateFactory {
         private Map<Pair<LTWRule, Integer>,WeakReference<N2WState>> baseSymbols;

         public StateFactory() {
             baseSymbols = new HashMap<>();
         }

         public N2WState createState(final Pair<LTWRule, Integer> name) {
             return lookup(name);
         }

         /**
          * Creates a new named state with the given name.
          *
          * @param pair name of the new state
          * @return a new named state
          */
         @SuppressWarnings("unchecked")
         private N2WState lookup(final Pair<LTWRule, Integer> pair) {
             WeakReference<N2WState> ref = baseSymbols.get(pair); //lookup if we know this state

             // this cast is safe because if we find a NamedState for
             // the name, its type parameter is the type of the name
             N2WState state = (ref!=null)?ref.get():null;
             if (state==null) {
                 state = new N2WState(pair);
                 baseSymbols.put(pair, new WeakReference<>(state));
                 return state;
             } else {
                 return state;
             }
         }
     }
 }