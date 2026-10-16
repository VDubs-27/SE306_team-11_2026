package lavs.ui.common;

import lavs.logic.model.Page;
import lavs.logic.model.document.Document;
import lavs.logic.model.note.Note;
import lavs.logic.service.DocumentService;
import lavs.logic.service.NoteService;
import lavs.logic.service.Result;

/**
 * The Documents and Notes tabs' records, for a customer or a loan, through their services.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Sources {

    private Sources() {
    }

    /** The owner's documents, e.g. a customer's; the mainframe lists each by name, so each is read in full. */
    public static <K> ServiceList.Source<DocumentFile, DocumentFile> documents(DocumentService<K> service, K owner) {
        return new ServiceList.Source<>() {
            @Override
            public Result<Page<DocumentFile>> page(long page, long size) {
                return Fetch.page(service.list(owner, page, size),
                        document -> Fetch.run(() -> file(Fetch.value(service.get(owner, document.id())))));
            }

            @Override
            public Result<?> add(DocumentFile file) {
                return service.add(owner, details(file));
            }

            @Override
            public Result<?> update(DocumentFile document, DocumentFile changed) {
                return service.update(owner, document.id(), details(changed));
            }

            @Override
            public Result<?> remove(DocumentFile document) {
                return service.delete(owner, document.id());
            }
        };
    }

    /** The owner's notes, e.g. a loan's. The mainframe sets who wrote a note and when. */
    public static <K> ServiceList.Source<NoteRow, NoteRow> notes(NoteService<K> service, K owner) {
        return new ServiceList.Source<>() {
            @Override
            public Result<Page<NoteRow>> page(long page, long size) {
                return Fetch.page(service.list(owner, page, size), note -> Result.success(row(note)));
            }

            @Override
            public Result<?> add(NoteRow note) {
                return service.add(owner, details(note));
            }

            @Override
            public Result<?> update(NoteRow note, NoteRow changed) {
                return service.update(owner, note.id(), details(changed));
            }

            @Override
            public Result<?> remove(NoteRow note) {
                return service.delete(owner, note.id());
            }
        };
    }

    private static DocumentFile file(Document document) {
        return new DocumentFile(document.id(), document.type(), document.fileName(), document.fileType(),
                document.size());
    }

    private static Document.Details details(DocumentFile file) {
        return new Document.Details(file.type(), file.fileName(), file.fileType(), file.size());
    }

    private static NoteRow row(Note note) {
        return new NoteRow(note.id(), note.type().code(), note.author(), note.dateAdded(), note.lines());
    }

    private static Note.Details details(NoteRow note) {
        return new Note.Details(Note.Type.fromCode(note.type()), note.text());
    }
}
