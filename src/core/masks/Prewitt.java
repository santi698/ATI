package core.masks;


public class Prewitt extends DirectionalMask {
	private static final int[] core_t = new int[] {1,1,1};
	private static final int[] core_c = new int[] {0,0,0};
	private static final int[] core_b = new int[] {-1,-1,-1};
	public Prewitt() {
		super(core_t, core_c, core_b);
	}
}
