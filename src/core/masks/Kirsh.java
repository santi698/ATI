package core.masks;

public class Kirsh extends DirectionalMask {
	private static int [] core_t = new int[] {5,5,5};
	private static int [] core_c = new int[] {-3,0,-3};
	private static int [] core_b = new int[] {-3,-3,-3};
	public Kirsh() {
		super(core_t, core_c, core_b);
	}
}
