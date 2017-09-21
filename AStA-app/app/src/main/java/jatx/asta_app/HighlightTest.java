package jatx.asta_app;

import java.net.URL;
import java.util.Objects;

/**
 * Created by jatx on 22.09.17.
 */

public class HighlightTest {
    public static final String CONST_STRING = "String literal";
    private int i = -1;
    private long l = 124363464765887l; // comment
    private double d = 34.15;
    public float f = .37f;
    final double d2 = 3.;
    private char c = 'f';

    private Object object = null;
    private int[] array = new int[]{1, 3, 7};

    private URL url;
    Long time = System.currentTimeMillis();

    @Override
    public String toString() {
        return "some string";
    }
}
