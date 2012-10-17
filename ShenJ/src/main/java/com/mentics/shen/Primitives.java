package com.mentics.shen;

import static com.mentics.shen.RuntimeContext.*;
import static java.util.Calendar.*;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * TODO: Because Byte extends Number, we might be able to return it from functions here. Perhaps Integer, too, for
 * indexes.
 */
public class Primitives {
    public static final String PRIMITIVES_PATH = "com/stralos/shen/Primitives";

    public static final String LAMBDA_METHOD_NAME = "apply";
    public static final String LAMBDA_PATH = "com/stralos/lang/Lambda";
    public static final String LAMBDA_BASE_PATH = LAMBDA_PATH + "0";
    public static final String LAMBDA_INTERFACE_PATH = LAMBDA_PATH;
    public static final String NEW_LAMBDA_PATH_BASE = "shen/lambda/ToRun";

    public static final String SYMBOL_PATH = "com/stralos/shen/model/Symbol";
    public static final String MODEL_PATH = "com/stralos/shen/model/Model";

    public static final String COMPILER_PATH = "com/stralos/shen/ShenCompiler";
    public static final String COMPILER_METHOD_NAME = "compile";


    // Other //

    public static Lambda intern = new Lambda1() {
        public Object apply(Object str) {
            return RuntimeContext.symbol((String) str);
        }
    };


    // Strings //

    /**
     * string --> number --> string
     * given a natural number 0 ...n and a string S returns the nth unit string in S
     */
    public static Lambda pos = new Lambda2() {
        public Object apply(Object string, Object index) {
            int ind = ((Number) index).intValue();
            return ((String) string).substring(ind, ind + 1);
        }
    };

    /**
     * string --> string
     * returns all but the first unit string of a string
     */
    public static Lambda tlstr = new Lambda1() {
        public Object apply(Object str) {
            return ((String) str).substring(1);
        }
    };

    /**
     * string --> string --> string
     * concatenates two strings
     */
    public static Lambda cn = new Lambda2() {
        public Object apply(Object str0, Object str1) {
            return (String) str0 + (String) str1;
        }
    };

    /**
     * A --> string
     * maps any atom to a string
     * TODO: exactly how different atoms should be output: like strings with "'s
     */
    public static Lambda str = new Lambda1() {
        public Object apply(Object atom) {
            return atom.toString();
        }
    };

    /**
     * A --> boolean
     * test for strings
     */
    @Label("string?")
    public static Lambda string_ = new Lambda1() {
        public Object apply(Object string) {
            return string instanceof String;
        }
    };

    /**
     * number --> string
     * maps a code point in decimal to the corresponding unit string
     */
    @Label("n->string")
    public static Lambda n__string = new Lambda1() {
        public Object apply(Object codePoint) {
            return String.valueOf((char) ((Number) codePoint).intValue());
        }
    };

    /**
     * string --> number
     * maps a unit string to the corresponding decimal
     */
    @Label("string->n")
    public static Lambda string__n = new Lambda1() {
        public Object apply(Object string) {
            // TODO: throw error if more than 1 in length?
            return Long.valueOf(((String) string).charAt(0));
        }
    };


    // Assignments //

    /**
     * (value X) : A;
     * X : symbol;
     * Y : A;______
     * (set X Y) : A;
     * assigns a value to a symbol
     */
    public static Lambda set = new Lambda2() {
        public Object apply(Object symbol, Object value) {
            RuntimeContext.globalProperties.put((Symbol) symbol, value);
            return value;
        }
    };

    /**
     * retrieves the value of a symbol
     */
    public static Lambda value = new Lambda1() {
        public Object apply(Object symbol) {
            Object o = RuntimeContext.globalProperties.get(symbol);
            return o != null ? o : Nil.NIL;
        }
    };


    // Error Handling //

    /**
     * throws an exception
     */
    @Label("simple-error")
    public static Lambda simple_error = new Lambda1() {
        public Object apply(Object message) {
            throw new RuntimeException(message.toString());
        }
    };

    /**
     * exception --> string
     * maps an exception to a string
     */
    @Label("error-to-string")
    public static Lambda error_to_string = new Lambda1() {
        public Object apply(Object exc) {
            if (exc instanceof Throwable) {
                StringWriter writer = new StringWriter();
                ((Throwable) exc).printStackTrace(new PrintWriter(writer));
                return writer.toString();
            }
            return exc.toString();
        }
    };


    // Lists //

    public static Lambda cons = new Lambda2() {
        public Object apply(Object head, Object tail) {
            return new Cons(head, tail);
            // Unify the two possibilities
            // if (list instanceof List) {
            // return Model.list(Location.UNKNOWN, ((List) list).cons(newElm));
            // } else if (list instanceof Cons) {
            // // System.out.println("cons: " + newElm + " to " + list);
            // return Model.list(((Cons) list).getLocation(), ((Cons) list).toList().cons(newElm));
            // } else {
            // // dotted pair thing?
            // return new Object[] { newElm, RuntimeContext.symbol("|"), list };
            // }
        }
    };


    public static Lambda hd = new Lambda1() {
        public Object apply(Object list) {
            return ((Cons) list).head;
        }
    };

    public static Lambda tl = new Lambda1() {
        public Object apply(Object list) {
            return ((Cons) list).tail;
        }
    };

    @Label("cons?")
    public static Lambda cons_ = new Lambda1() {
        public Object apply(Object c) {
            return c instanceof Cons && c != Nil.NIL; // TODO: safe?
        }
    };

    // Generic Functions, most are handled specially //

    /**
     * A --> A --> boolean
     * equality
     * 
     * NOTE: I'm assuming that null != null
     */
    @Label("=")
    public static Lambda equal = new Lambda2() {
        public Object apply(Object x0, Object x1) {
            // TODO: I'm not sure this is correct behavior, but it gets me past the "unexpected argument" error in
            // shen-change-pointer thing called through shen-initialise_arity_table
            if ((x0 == null || x0 == Nil.NIL) && (x1 == null || x1 == Nil.NIL)) {
                return true;
            }
            return x0 != null && x0.equals(x1);
        }
    };

    /**
     * X : A;
     * (type X A) : A;
     * labels the type of an expression
     */
    public static Lambda type = new Lambda2() {
        public Object apply(Object expr, Object type) {
            System.out.println("type called: " + expr + ", " + type);
            return expr;
        }
    };


    @Label("eval-kl")
    public static Object evalKl(Object kl) throws Exception {
        if (kl instanceof Cons) {
            return RuntimeContext.evalKl(kl);
            // Cons l = (Cons) kl;
            // // TODO: do we need a first compile to evaluate embedded things
            // // System.out.println("COMPILING: "+kl);
            // Lambda0 lam = ShenCompiler.compile(Environment.theEnvironment().threadEvalContext(), Model.slist(l));
            // return lam.apply();
        } else {
            return kl;
        }
    }


    // Vectors //

    /**
     * create a vector in the native platform
     */
    public static Lambda absvector = new Lambda1() {
        public Object apply(Object size) {
            // int sz = ((Number) size).intValue();
            // Object[] os = new Object[sz + 1];
            // os[0] = size;
            // for (int i = 1; i <= sz; i++) {
            // os[i] = FAIL;
            // }
            // return os;
            return new Object[((Number) size).intValue()];
        }
    };

    /**
     * destructively assign a value to a vector address
     */
    @Label("address->")
    public static Lambda address__ = new Lambda3() {
        public Object apply(Object v, Object index, Object value) {
            ((Object[]) v)[((Number) index).intValue()] = value;
            return v;
        }
    };

    /**
     * retrieve a value from a vector address
     */
    @Label("<-address")
    public static Lambda __address = new Lambda2() {
        public Object apply(Object v, Object index) {
            return ((Object[]) v)[((Number) index).intValue()];
        }
    };

    @Label("absvector?")
    public static Lambda absvector_ = new Lambda1() {
        public Object apply(Object v) {
            return v instanceof Object[];
        }
    };


    // Streams and I/O //

    /**
     * string --> (stream out) --> string
     * print a string to a stream
     */
    public static Lambda pr = new Lambda2() {
        public Object apply(Object string, Object streamOut) {
            try {
                ((OutputStream) streamOut).write(((String) string).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return string;
        }
    };

    /**
     * (stream in) --> number
     * read an unsigned 8 bit byte from a stream
     * TODO: Note: we're using a long to read a byte?
     */
    @Label("read-byte")
    public static Lambda read_byte = new Lambda1() {
        public Object apply(Object streamIn) {
            try {
                return (long) ((InputStream) streamIn).read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    /**
     * File : string; Direction : direction;
     * (open file File Direction) : (stream Direction);
     * open a stream
     */
    public static Lambda open = new Lambda3() {
        public Object apply(Object type, Object location, Object direction) {
            if ("file".equals(type.toString())) {
                String dir = direction.toString();
                if ("in".equals(dir)) {
                    try {
                        return new FileInputStream((String) location);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else if ("out".equals(dir)) {
                    try {
                        return new FileOutputStream((String) location);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new RuntimeException("Invalid stream direction passed to first parameter of open: " + dir);
                }
            } else {
                throw new RuntimeException("Invalid stream type passed to first parameter of open: " + type);
            }
        }
    };

    /**
     * close a stream
     */
    public static Lambda close = new Lambda1() {
        public Object apply(Object stream) {
            try {
                ((Closeable) stream).close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return Nil.NIL;
        }
    };


    // Time //

    /**
     * symbol --> number
     * get the run/real time
     */
    @Label("get-time")
    public static Lambda get_time = new Lambda1() {
        public Object apply(Object x) {
            String type = x.toString();
            switch (type) {
            case "real":
                long time = System.nanoTime();
                return (double) time / 10E9;
            case "run":
                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                if (bean.isCurrentThreadCpuTimeSupported()) {
                    return bean.getCurrentThreadCpuTime() / 1000000000.0;
                } else {
                    throw new RuntimeException("CPU run time not supported.");
                }
            case "date":
                // year, month, day, hour, minute and second.
                Calendar c = GregorianCalendar.getInstance();
                return Cons.list(c.get(YEAR), c.get(MONTH), c.get(DAY_OF_MONTH), c.get(HOUR_OF_DAY), c.get(MINUTE),
                        c.get(SECOND));
            default:
                throw new RuntimeException("Invalid parameter to get-time.");
            }
        }
    };


    // Arithmetic //

    @Label("+")
    public static Lambda plus = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x + (Long) y;
            } else {
                double variable = ((Number) x).doubleValue() + ((Number) y).doubleValue();
                if ((variable == Math.floor(variable)) && !Double.isInfinite(variable)) {
                    return (long) variable;
                } else {
                    return variable;
                }
            }
        }
    };

    @Label("-")
    public static Lambda minus = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x - (Long) y;
            } else {
                return ((Number) x).doubleValue() - ((Number) y).doubleValue();
            }
        }
    };

    @Label("*")
    public static Lambda multiply = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x * (Long) y;
            } else {
                return ((Number) x).doubleValue() * ((Number) y).doubleValue();
            }
        }
    };

    @Label("/")
    public static Lambda divide = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                // TODO: it should return float unless divisor
                return (Long) x / (Long) y;
            } else {
                return ((Number) x).doubleValue() / ((Number) y).doubleValue();
            }
        }
    };

    @Label(">")
    public static Lambda greaterThan = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x > (Long) y;
            } else {
                return ((Number) x).doubleValue() > ((Number) y).doubleValue();
            }
        }
    };

    @Label("<")
    public static Lambda lessThan = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x < (Long) y;
            } else {
                return ((Number) x).doubleValue() < ((Number) y).doubleValue();
            }
        }
    };

    @Label(">=")
    public static Lambda greaterThanOrEqual = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x >= (Long) y;
            } else {
                return ((Number) x).doubleValue() >= ((Number) y).doubleValue();
            }
        }
    };

    @Label("<=")
    public static Lambda lessThanOrEqual = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x <= (Long) y;
            } else {
                return ((Number) x).doubleValue() <= ((Number) y).doubleValue();
            }
        }
    };

    @Label("number?")
    public static Lambda number_ = new Lambda1() {
        public Object apply(Object x) {
            return x instanceof Number;
        }
    };

    public static Lambda printOut = new Lambda1() {
        public Object apply(Object x) {
            System.out.print("PRINT OUT: ");
            if (x instanceof Object[]) {
                System.out.print("[");
                Object[] a = (Object[]) x;
                for (int i = 0; i < a.length; i++) {
                    System.out.print(a[i] + ",");
                }
                System.out.println("]");
            } else {
                System.out.println(x.toString());
            }
            return x;
        }
    };

    public static Lambda stralos_debug = new Lambda2() {
        public Object apply(Object prefix, Object x) {
            System.out.print("DEBUG: " + prefix);
            if (x instanceof Object[]) {
                System.out.print("[");
                Object[] a = (Object[]) x;
                for (int i = 0; i < a.length; i++) {
                    System.out.print(a[i] + ",");
                }
                System.out.println("]");
            } else {
                System.out.println(x.toString());
            }
            return x;
        }
    };


    public static void loadPrimitives() {
        try {
            for (Field f : Primitives.class.getFields()) {
                if (Lambda.class.isAssignableFrom(f.getType())) {
                    Label annotation = f.getAnnotation(Label.class);
                    String label = annotation != null ? label = annotation.value() : f.getName();
                    RuntimeContext.functions.put(symbol(label), (Lambda) f.get(null));
                    // System.out.println("put: " + ASMUtil.fromIdentifier(f.getName()) + ", " + (Lambda) f.get(null));
                }
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}
