import java.util.NoSuchElementException;

public class AssertBoolean {
    private boolean b;
    public AssertBoolean(boolean b){
        this.b = b;
    }
    public AssertBoolean isEqualTo(boolean b2) {
        if (b == b2){
            return this;
        }else throw new NoSuchElementException("boolean isEqualTo: wrong");

    }
    public AssertBoolean isTrue() {
        if (b == true){
            return this;
        }else throw new NoSuchElementException("boolean isTrue: wrong");
    }
    public AssertBoolean isFalse(){
        if (b == false){
            return this;
        }else throw new NoSuchElementException("boolean isFalse: wrong");

    }
}
