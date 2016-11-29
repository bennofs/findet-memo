package tu.emi.findetmemo.data;

public interface MemoVisitor<T> {
    public T handle(TextMemo textMemo);
}
