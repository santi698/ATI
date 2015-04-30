package core.masks;

public class UnnamedMask extends DirectionalMask {
	private static int [] core_t = new int[] {1,1,1};
	private static int [] core_c = new int[] {1,-2,1};
	private static int [] core_b = new int[] {-1,-1,-1};
	public UnnamedMask() {
		super(core_t, core_c, core_b);
	}
}
