import java.util.NoSuchElementException;

public class AssertInt {
    private int i;
    public AssertInt(int i){
        this.i = i;
    }

    public AssertInt isEqualTo(int i2){
        if(i == i2){
            return this;
        }throw new NoSuchElementException("int isEqualTo: wrong");

    }
    public AssertInt isLessThan(int i2){
        if(i < i2){
            return this;
        }throw new NoSuchElementException("int isLessThan: wrong");

    }
    public AssertInt isGreaterThan(int i2) {
        if(i > i2){
            return this;
        }throw new NoSuchElementException("int isGreaterThan: wrong");

    }
}
