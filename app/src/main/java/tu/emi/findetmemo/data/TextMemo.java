package tu.emi.findetmemo.data;

import java.util.Date;
import java.util.UUID;

import tu.emi.findetmemo.view.TextMemoSummary;
import tu.emi.findetmemo.view.ViewTemplate;

public class TextMemo extends Memo {
    public final String textBody;

    public TextMemo(UUID uuid, Common common, String textBody) {
        super(uuid, common);
        this.textBody = textBody.trim();
    }

    public static TextMemo create(String title, String textBody) {
        Date now = new Date();
        Common common = new Common(title, now, now);
        return new TextMemo(UUID.randomUUID(), common, textBody);
    }

    public static TextMemo createEmpty() {
        return create("", "");
    }

    @Override
    public boolean isEmpty() {
        return common.isEmpty() && textBody.isEmpty();
    }

    @Override
    public Memo withCommon(Common common) {
        return new TextMemo(this.uuid, common, this.textBody);
    }

    public TextMemo withTextBody(String body) {
        return new TextMemo(this.uuid, this.common, body);
    }

    @Override
    public ViewTemplate summaryViewTemplate() {
        return new TextMemoSummary();
    }
}
