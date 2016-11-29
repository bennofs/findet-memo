package tu.emi.findetmemo.data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

import tu.emi.findetmemo.view.ViewTemplate;

abstract public class Memo implements Serializable {
    public static final String EXTRA_MEMO = "tu.emi.findetmemo.model.Memo";

    public final UUID uuid;
    public final String title;
    public final Date creationDate;
    public final Date lastModificationDate;

    public Memo(UUID uuid, String title, Date creationDate, Date lastModificationDate) {
        this.uuid = uuid;
        this.title = title;
        this.creationDate = creationDate;
        this.lastModificationDate = lastModificationDate;
    }

    public abstract <T> T visit(MemoVisitor<T> visitor);

    public abstract ViewTemplate summaryViewTemplate();

    public static class TitleComparator implements Comparator<Memo> {
        @Override
        public int compare(Memo lhs, Memo rhs) {
            return lhs.title.compareTo(rhs.title);
        }
    }
}
