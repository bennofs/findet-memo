package tu.emi.findetmemo.data;

import java.util.Date;
import java.util.UUID;

import tu.emi.findetmemo.view.TextMemoSummary;
import tu.emi.findetmemo.view.ViewTemplate;

public class TextMemo extends Memo {
    public final String textBody;

    public TextMemo(UUID uuid, String title, Date creationDate, Date lastModificationDate, String textBody) {
        super(uuid, title, creationDate, lastModificationDate);
        this.textBody = textBody;
    }

    public static TextMemo create(String title, String textBody) {
        return new TextMemo(UUID.randomUUID(), title, new Date(), new Date(), textBody);
    }

    @Override
    public <T> T visit(MemoVisitor<T> visitor) {
        return visitor.handle(this);
    }

    @Override
    public ViewTemplate summaryViewTemplate() {
        return new TextMemoSummary();
    }
}
