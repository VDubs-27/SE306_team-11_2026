package lavs.ui.common;

import java.util.List;

import lavs.logic.model.Page;

/**
 * Pages a list already held on screen, the same way the mainframe pages the lists it returns.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Pages {

    private Pages() {
    }

    /** Page number page (from 1) of the items, size to a page. */
    public static <T> Page<T> of(List<T> items, long page, long size) {
        int from = (int) Math.min((page - 1) * size, items.size());
        int to = (int) Math.min(from + size, items.size());
        return new Page<>(items.subList(from, to), page, size, items.size());
    }
}
