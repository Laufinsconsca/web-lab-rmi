package pkg;

@ConnectableItem(name = "Kernel holder", type = Item.KERNEL)
public class KernelHolder {
    @ConnectableItem(name = "Smoothing kernel", type = Item.KERNEL, priority = 1)
    public static final double[][] smoothingKernel = {{1. / 16, 1. / 8, 1. / 16}, {1. / 8, 1. / 4, 1. / 8}, {1. / 16, 1. / 8, 1. / 16}};
    @ConnectableItem(name = "Sharping kernel", type = Item.KERNEL, priority = 2)
    public static final double[][] sharpingKernel = {{-1, -1, -1}, {-1, 9, -1}, {-1, -1, -1}};
    @ConnectableItem(name = "Emboss kernel", type = Item.KERNEL, priority = 3)
    public static final double[][] embossKernel = {{-2, -1, 0}, {-1, 1, -1}, {0, 1, 2}};
    @ConnectableItem(name = "Edge enhance kernel", type = Item.KERNEL, priority = 4)
    public static final double[][] edgeEnhanceKernel = {{0, -1, 0}, {0, 1, 0}, {0, 0, 0}};
    @ConnectableItem(name = "Edge detect kernel", type = Item.KERNEL, priority = 5)
    public static final double[][] edgeDetectKernel = {{0, -1, 0}, {-1, 4, -1}, {0, -1, 0}};
}
