package lavs.logic.adapter;

import java.util.List;

// The mainframe messages for one kind of document owner (a customer or a loan). Both kinds have the same fields,
// but each has its own message classes, so DocumentGateway reads and writes documents through this.
interface DocumentMessages<K> {

    Listed list(K owner, long page, long size);

    Fields retrieve(K owner, long id);

    long add(K owner, String type, String fileName, String fileType, long size);

    void delete(K owner, long id);

    record Listed(List<Item> items, long total) {
    }

    record Item(long id, String type, String fileName) {
    }

    record Fields(long id, String type, String fileName, String fileType, long size) {
    }
}
