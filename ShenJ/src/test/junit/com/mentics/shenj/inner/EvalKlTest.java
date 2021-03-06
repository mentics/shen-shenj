package com.mentics.shenj.inner;

import static com.mentics.shenj.ShenjRuntime.*;
import static com.mentics.shenj.inner.Primitives.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.mentics.shenj.Cons;
import com.mentics.shenj.Nil;
import com.mentics.shenj.Symbol;


public class EvalKlTest {
    @Test
    public void testEvalKl() throws Exception {
        // loadDefaultImage();
        loadImage(new File("shen-test.image"));
        
        // System.out.println(ShenjRuntime.compileContext.classes.size());
        assertEquals(3, evalKl(3));
        assertEquals(17.8, evalKl(17.8));
        assertEquals("purple", evalKl("purple"));
        assertEquals(new Symbol("blue"), evalKl(new Symbol("blue")));
        assertEquals(2, evalKl(new Cons(symbol("+"), new Cons(2, new Cons(2, Nil.NIL)))));
    }
}
