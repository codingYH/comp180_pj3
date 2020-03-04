public class Assertion {
    /* You'll need to change the return type of the assertThat methods */
    static AssertObject assertThat(Object o) {
	return new AssertObject(o);
    }
    static AssertString assertThat(String s) {
	return new AssertString(s);
    }
    static AssertBoolean assertThat(boolean b) {
	return new AssertBoolean(b);
    }
    static AssertInt assertThat(int i) {
	return new AssertInt(i);
    }
}