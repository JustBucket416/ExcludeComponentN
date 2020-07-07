import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResult;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.impl.LiveTemplateLookupElement;
import com.intellij.util.Consumer;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom contributor that filters out Kotlin data classes' componentN() methods which are used in destructuring declarations
 * from code completion dropdown list
 *
 * @author JustBucket on 06.07.2020
 */
public class ComponentNFilteringContributor extends CompletionContributor {

    private final Set<LookupElement> filteredSet = new HashSet<>();
    private final Consumer<CompletionResult> filteringConsumer = new FilteringConsumer(filteredSet);

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        result.runRemainingContributors(parameters, filteringConsumer, false, null);
        result.addAllElements(filteredSet);
        filteredSet.clear();
    }

    private static final class FilteringConsumer implements Consumer<CompletionResult> {

        private static final String COMPONENT_REGEX = "(.*\\.)*component";
        private final Set<LookupElement> filteredSet;

        private FilteringConsumer(Set<LookupElement> filteredSet) {
            this.filteredSet = filteredSet;
        }

        @Override
        public void consume(CompletionResult completionResult) {
            LookupElement lookupElement = completionResult.getLookupElement();
            if (lookupElement instanceof LiveTemplateLookupElement) {
                return;
            }
            String str = lookupElement.toString().replaceAll(COMPONENT_REGEX, "");
            if (!str.isEmpty()) {
                try {
                    Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    filteredSet.add(lookupElement);
                }
            } else {
                filteredSet.add(lookupElement);
            }
        }
    }
}
