package tu.emi.findetmemo.data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

import tu.emi.findetmemo.view.ViewTemplate;

abstract public class Memo implements Serializable {
    public static final String EXTRA_MEMO = "tu.emi.findetmemo.model.Memo";

    public static class Common implements Serializable {

        public final String title;
        public final Date creationDate;
        public final Date lastModificationDate;

        public Common(String title, Date creationDate, Date lastModificationDate) {
            this.title = title.trim();
            this.creationDate = creationDate;
            this.lastModificationDate = lastModificationDate;
        }

        public Common withTitle(String title) {
            Date now = new Date();
            return new Common(title, this.creationDate, now);
        }

        public boolean isEmpty() {
            return title.isEmpty();
        }
    }

    public final UUID uuid;
    public final Common common;

    Memo(UUID uuid, Common common) {
        this.uuid = uuid;
        this.common = common;
    }

    public abstract Memo withCommon(Common newCommon);

    public abstract boolean isEmpty();

    public abstract ViewTemplate summaryViewTemplate();

    public static class TitleComparator implements Comparator<Memo> {
        @Override
        public int compare(Memo lhs, Memo rhs) {
            return lhs.common.title.compareTo(rhs.common.title);
        }
    }
}
