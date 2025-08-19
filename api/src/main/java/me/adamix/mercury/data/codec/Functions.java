package me.adamix.mercury.data.codec;

public class Functions {
	@FunctionalInterface
	public interface F1<P1, R> {
		R apply(P1 p1);
	}

	@FunctionalInterface
	public interface F2<P1, P2, R> {
		R apply(P1 p1, P2 p2);
	}

	@FunctionalInterface
	public interface F3<P1, P2, P3, R> {
		R apply(P1 p1, P2 p2, P3 p3);
	}

	@FunctionalInterface
	public interface F4<P1, P2, P3, P4, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4);
	}

	@FunctionalInterface
	public interface F5<P1, P2, P3, P4, P5, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
	}

	@FunctionalInterface
	public interface F6<P1, P2, P3, P4, P5, P6, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
	}

	@FunctionalInterface
	public interface F7<P1, P2, P3, P4, P5, P6, P7, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);
	}

	@FunctionalInterface
	public interface F8<P1, P2, P3, P4, P5, P6, P7, P8, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);
	}

	@FunctionalInterface
	public interface F9<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9);
	}

	@FunctionalInterface
	public interface F10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10);
	}

	@FunctionalInterface
	public interface F11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11);
	}

	@FunctionalInterface
	public interface F12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12);
	}

	@FunctionalInterface
	public interface F13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13);
	}

	@FunctionalInterface
	public interface F14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14);
	}

	@FunctionalInterface
	public interface F15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15);
	}

	@FunctionalInterface
	public interface F16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16);
	}

	@FunctionalInterface
	public interface F17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16, P17 p17);
	}

	@FunctionalInterface
	public interface F18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16, P17 p17, P18 p18);
	}

	@FunctionalInterface
	public interface F19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19);
	}

	@FunctionalInterface
	public interface F20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R> {
		R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19, P20 p20);
	}
}
