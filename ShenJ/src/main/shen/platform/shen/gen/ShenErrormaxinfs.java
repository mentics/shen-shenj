package shen.gen;



import com.mentics.shen.*;

public class ShenErrormaxinfs {

public static final Symbol SYMBOL = RuntimeContext.symbol("shen-errormaxinfs");
public static final Lambda LAMBDA = new Lambda0() {
public Object apply() throws Exception {
return defined();
}};
public static Object defined() throws Exception {

throw new SimpleError((String)"maximum inferences exceeded~%");


}
}
