package lavs.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;

/**
 * A list of records kept on screen, such as a wizard's addresses or a loan's notes: shows a row for
 * each, adds and edits them through a RecordEditor, and redraws after every change. Rules such as
 * "only one primary" and paging are switched on per list.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 *
 * @param <T> the record in each row
 */
public final class EditableList<T> {

    private final RecordEditor<T> editor;
    private final VBox rows;
    private final BiFunction<T, Runnable, Node> row;
    private final String emptyMessage;
    private final List<OnlyOne<T>> onlyOnes = new ArrayList<>();

    private List<T> items = new ArrayList<>();
    private Runnable onChanged = () -> { };
    private PagerController pager;
    private long pageSize;
    private boolean newestFirst;

    /**
     * row draws one record, given what its Edit button does. emptyMessage shows when there are no
     * records; when it's null the rows box is hidden instead, for a screen that says so itself.
     */
    public EditableList(RecordEditor<T> editor, VBox rows, BiFunction<T, Runnable, Node> row, String emptyMessage) {
        this.editor = editor;
        this.rows = rows;
        this.row = row;
        this.emptyMessage = emptyMessage;
    }

    /**
     * Allows only one record marked a certain way, e.g. one primary address: when the record just
     * saved is marked, unmark is applied to every other marked record.
     */
    public EditableList<T> keepOnlyOne(Predicate<T> marked, UnaryOperator<T> unmark) {
        onlyOnes.add(new OnlyOne<>(marked, unmark));
        return this;
    }

    /** Shows the rows a page at a time with this pager. */
    public EditableList<T> paged(PagerController pagerController, long size) {
        this.pager = pagerController;
        this.pageSize = size;
        return this;
    }

    /** Puts added records at the top instead of the end, for a list kept newest first. */
    public EditableList<T> newestFirst() {
        this.newestFirst = true;
        return this;
    }

    /** Runs after every add, change or remove, once the rows are redrawn, e.g. to update a count. */
    public EditableList<T> onChanged(Runnable action) {
        this.onChanged = action;
        return this;
    }

    /** Edits this list in place, such as a draft's, and shows it from the first page. */
    public void use(List<T> records) {
        this.items = records;
        if (pager == null) {
            draw(indexes());
        } else {
            pager.show(this::pageOfIndexes, this::draw);
        }
    }

    /** The records as they are now. */
    public List<T> items() {
        return items;
    }

    /** Opens an empty form, for the list's Add button; a saved record goes on the end, or the top. */
    public void add() {
        editor.add(items.isEmpty(), record -> {
            int index = newestFirst ? 0 : items.size();
            items.add(index, record);
            keepRules(index);
            if (newestFirst && pager != null) {
                // The new record is at the very top, so show the first page where it landed
                use(items);
                onChanged.run();
            } else {
                changed();
            }
        });
    }

    private void edit(int index) {
        editor.edit(items.get(index), record -> {
            items.set(index, record);
            keepRules(index);
            changed();
        }, () -> {
            items.remove(index);
            changed();
        });
    }

    /** Applies the "only one" rules after the record at this position was saved. */
    private void keepRules(int index) {
        T record = items.get(index);
        for (OnlyOne<T> rule : onlyOnes) {
            if (rule.marked().test(record)) {
                unmarkOthers(rule, index);
            }
        }
    }

    private void unmarkOthers(OnlyOne<T> rule, int savedIndex) {
        for (int i = 0; i < items.size(); i++) {
            if (i != savedIndex && rule.marked().test(items.get(i))) {
                items.set(i, rule.unmark().apply(items.get(i)));
            }
        }
    }

    /** Redraws, staying on the same page where there is one, then tells the screen. */
    private void changed() {
        if (pager == null) {
            draw(indexes());
        } else {
            pager.reload();
        }
        onChanged.run();
    }

    /** Rows are drawn from positions in the list, so Edit changes exactly the record clicked. */
    private void draw(List<Integer> indexes) {
        List<Node> drawn = indexes.stream()
                .map(i -> row.apply(items.get(i), () -> edit(i)))
                .toList();
        boolean hidden = drawn.isEmpty() && emptyMessage == null;
        rows.setVisible(!hidden);
        rows.setManaged(!hidden);
        if (drawn.isEmpty() && emptyMessage != null) {
            Label empty = new Label(emptyMessage);
            empty.getStyleClass().add("text-muted");
            rows.getChildren().setAll(empty);
        } else {
            rows.getChildren().setAll(drawn);
        }
    }

    private List<Integer> indexes() {
        return IntStream.range(0, items.size()).boxed().toList();
    }

    private Page<Integer> pageOfIndexes(long page) {
        return Pages.of(indexes(), page, pageSize);
    }

    /** One "only one of these" rule. */
    private record OnlyOne<T>(Predicate<T> marked, UnaryOperator<T> unmark) {
    }
}
