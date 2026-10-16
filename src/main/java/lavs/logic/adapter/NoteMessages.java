package lavs.logic.adapter;

import java.util.List;

// The mainframe messages for one kind of note owner (a customer or a loan). Both kinds have the same fields,
// but each has its own message classes, so NoteGateway reads and writes notes through this.
interface NoteMessages<K> {

    Listed list(K owner, long page, long size);

    Fields retrieve(K owner, long id);

    long add(K owner, String type, String lines);

    void update(K owner, long id, String type, String lines);

    void delete(K owner, long id);

    // The list message has no note text, only the ids to fetch it by
    record Listed(List<Long> ids, long total) {
    }

    record Fields(String type, String lines, String author, String dateAdded) {
    }
}
