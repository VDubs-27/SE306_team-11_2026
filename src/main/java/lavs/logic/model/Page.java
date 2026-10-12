package lavs.logic.model;

import java.util.List;

public record Page<T>(List<T> items, long page, long size, long total) {

    public Page {
        items = List.copyOf(items);
        if (page < 1) {
            throw new IllegalArgumentException("Page must be 1 or more.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be 1 or more.");
        }
        if (total < 0) {
            throw new IllegalArgumentException("Total must be 0 or more.");
        }
    }

    public long totalPages() {
        return (total + size - 1) / size;
    }

    public boolean hasNext() {
        return page < totalPages();
    }

    public boolean hasPrevious() {
        return page > 1;
    }
}
