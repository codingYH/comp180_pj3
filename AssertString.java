import java.util.NoSuchElementException;

public class AssertString {
    private String s;
    //init
    public AssertString(String s){
        this.s = s;
    }

    //represent
    public AssertString isNotNull(){
        if(s != null){
            return this;
        }else  throw new NoSuchElementException("string isNotNull: wrong");

    }
    public AssertString isNull(){
        if(s == null){
            return this;
        }else  throw new NoSuchElementException("string isNull: wrong");

    }
    public AssertString isEqualTo(Object o2){
        if(s.equals(o2)){
            return this;
        }else  throw new NoSuchElementException("string isEqualTo: wrong");

    }
    public AssertString isNotEqualTo(Object o2){
        if(!s.equals(o2)){
            return this;
        }else  throw new NoSuchElementException("string isNotEqualTo: wrong");

    }
    public AssertString startsWith(String s2){
        if(s.startsWith(s2)){
            return this;
        }else  throw new NoSuchElementException("string startsWith: wrong");

    }
    public AssertString isEmpty(){
        if(s.isEmpty()){
            return this;
        }else  throw new NoSuchElementException("string isEmpty: wrong");


    }
    public AssertString contains(String s2){
        if(s.contains(s2)){
            return this;
        }else  throw new NoSuchElementException("string contains: wrong");

    }
}
