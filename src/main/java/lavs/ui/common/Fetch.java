package lavs.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;

import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.service.Result;

/**
 * Helps load everything a screen shows in one go, in the background, from several service calls:
 * every page of a list, or each item in full. A load stops at the first call that fails, and gives
 * back why, so a screen either has all it needs or a reason it couldn't load.
 * <pre>
 * Result&lt;Profile&gt; loaded = Fetch.run(() -&gt; new Profile(
 *         Fetch.value(customers.retrieve(number)),
 *         Fetch.all(page -&gt; addresses.list(number, page, Fetch.PAGE_SIZE))));
 * </pre>
 * AI-assisted: written by Jack, refined with Claude.
 */
public final class Fetch {

    /** How many to ask for at once when reading a whole list. */
    public static final long PAGE_SIZE = 50;

    private Fetch() {
    }

    /** Runs a load made of several calls; its result, or the reason the first failed call gave. */
    public static <T> Result<T> run(Supplier<T> load) {
        try {
            return Result.success(load.get());
        } catch (Stopped stopped) {
            return Result.failed(stopped.getMessage());
        }
    }

    /** The value of a call inside run, or stops the load if the call failed. */
    public static <T> T value(Result<T> result) {
        if (!result.successful()) {
            throw new Stopped(reason(result));
        }
        return result.value();
    }

    /** Every item in a list, reading a page at a time until there are no more. For use inside run. */
    public static <T> List<T> all(LongFunction<Result<Page<T>>> loadPage) {
        List<T> items = new ArrayList<>();
        Page<T> page = value(loadPage.apply(1));
        items.addAll(page.items());
        while (page.hasNext()) {
            page = value(loadPage.apply(page.page() + 1));
            items.addAll(page.items());
        }
        return items;
    }

    /**
     * A page of a list with each item on it read in full, e.g. a page of address summaries as the
     * addresses themselves; or why the page, or an item on it, couldn't be read.
     */
    public static <S, T> Result<Page<T>> page(Result<Page<S>> listed, Function<S, Result<T>> read) {
        return run(() -> {
            Page<S> summaries = value(listed);
            return new Page<>(each(summaries.items(), read), summaries.page(), summaries.size(), summaries.total());
        });
    }

    /** Each item read in full, e.g. every address from its summary, in the same order. For use inside run. */
    public static <S, T> List<T> each(List<S> items, Function<S, Result<T>> read) {
        return items.stream().map(item -> value(read.apply(item))).toList();
    }

    /** Why a call failed, in words: its message, or what was wrong with the fields it was given. */
    public static String reason(Result<?> failed) {
        if (failed.errorMessage() != null) {
            return failed.errorMessage();
        }
        return String.join(" ", failed.fieldErrors().stream().map(FieldError::message).toList());
    }

    /** Ends a load early when a call in it fails. */
    private static final class Stopped extends RuntimeException {
        Stopped(String reason) {
            super(reason, null, false, false);
        }
    }
}
