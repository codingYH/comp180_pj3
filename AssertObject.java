import java.util.NoSuchElementException;

public class AssertObject {
    private Object o;
    //init
    public AssertObject(Object o){
        this.o = o;
    }
    //represent
    public AssertObject isNotNull(){
        if(o != null){
            return this;
        }else throw new NoSuchElementException("object isNotNull: wrong");

    }
    public AssertObject isNull(){
        if(o == null){
            return this;
        }else throw new NoSuchElementException("object isNull: wrong");


    }
    public AssertObject isEqualTo(Object o2) {
        if (o != null) {
            if (o.equals(o2)) {
                return this;
            }
        }throw new NoSuchElementException("object isEqualTo: wrong");
    }

    public AssertObject isNotEqualTo(Object o2){
        if (o != null) {
            if(!o.equals(o2)){
            return this;
            }
        }throw new NoSuchElementException("object isNotEqualTo: wrong");
    }

    public AssertObject isInstanceOf(Class c){
        if (c != null) {
            if (c.isInstance(o)) {
                return this;
            }
        }throw new NoSuchElementException("object isInstanceOf: wrong");
    }
}
