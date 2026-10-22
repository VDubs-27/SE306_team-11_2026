package lavs.ui.common;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongConsumer;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.service.Result;

/**
 * A list of records kept on the mainframe, such as a customer's addresses: shows them a page at a
 * time, read in the background, and adds, changes and removes them through a service with a
 * RecordEditor pop-up, showing the page again after every change. Like EditableList, but the
 * records live on the mainframe instead of on screen.
 * AI-assisted: written by Jack, refined with Claude.
 *
 * @param <T> the record in each row, e.g. Address
 * @param <D> what the pop-up's form edits, e.g. Address.Details
 */
public final class ServiceList<T, D> {

    /**
     * Where the records come from and go to, e.g. a customer's addresses through AddressService.
     * Each method is called in the background.
     */
    public interface Source<T, D> {
        Result<Page<T>> page(long page, long size);

        Result<?> add(D details);

        Result<?> update(T record, D details);

        Result<?> remove(T record);
    }

    private final RecordEditor<D> editor;
    private final VBox rows;
    private final PagerController pager;
    private final long pageSize;
    private final BiFunction<T, Runnable, Node> row;
    private final Function<T, D> details;
    private final String emptyMessage;

    private Source<T, D> source;
    private long total;
    private LongConsumer onTotal = count -> { };
    private Runnable onChanged = () -> { };

    /**
     * row draws one record, given what its Edit button does; details is what its pop-up form starts
     * from; emptyMessage shows when there are none.
     */
    public ServiceList(RecordEditor<D> editor, VBox rows, PagerController pager, long pageSize,
                       BiFunction<T, Runnable, Node> row, Function<T, D> details, String emptyMessage) {
        this.editor = editor;
        this.rows = rows;
        this.pager = pager;
        this.pageSize = pageSize;
        this.row = row;
        this.details = details;
        this.emptyMessage = emptyMessage;
    }

    /** Hears how many records there are, each time a page is read, e.g. for the tab's count. */
    public ServiceList<T, D> onTotal(LongConsumer action) {
        this.onTotal = action;
        return this;
    }

    /** Runs after every add, change or remove, e.g. so the screen can update what depends on the list. */
    public ServiceList<T, D> onChanged(Runnable action) {
        this.onChanged = action;
        return this;
    }

    /** Shows the records from this source, from the first page. */
    public void show(Source<T, D> records) {
        this.source = records;
        rows.getChildren().setAll(muted("Loading…"));
        pager.showLoading(page -> source.page(page, pageSize), this::draw, this::loaded);
    }

    /** Opens an empty form, for the list's Add button; the first record is set up as primary. */
    public void add() {
        if (editor.addAndSave(total == 0, entered -> source.add(entered))) {
            changed();
        }
    }

    private void edit(T record) {
        if (editor.editAndSave(details.apply(record), changed -> source.update(record, changed),
                () -> source.remove(record))) {
            changed();
        }
    }

    private void changed() {
        pager.reload();
        onChanged.run();
    }

    private void draw(List<T> records) {
        if (records.isEmpty()) {
            rows.getChildren().setAll(muted(emptyMessage));
        } else {
            rows.getChildren().setAll(records.stream().map(record -> row.apply(record, () -> edit(record))).toList());
        }
    }

    /** Keeps the count up to date, or says the page couldn't be read. */
    private void loaded(Result<Page<T>> page) {
        if (page.successful()) {
            total = page.value().total();
            onTotal.accept(total);
        } else {
            rows.getChildren().setAll(muted("Couldn't load these. " + page.errorMessage()));
        }
    }

    private static Label muted(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("text-muted");
        return label;
    }
}
