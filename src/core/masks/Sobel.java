package core.masks;

public class Sobel extends DirectionalMask {
	private static int [] core3_t = new int[] {1,2,1};
	private static int [] core3_b = new int[] {-1,-2,-1};
	private static int [] core3_c = new int[] {0,0,0};
	public Sobel() {
		super(core3_t, core3_c, core3_b);
	}
}
