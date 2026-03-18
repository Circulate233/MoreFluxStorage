package com.circulation.more_flux_storage.compat.flux;

public final class FluxNeoForgeApiHandle {

    private static final String[] REQUIRED_CLASSES = {
        "sonar.fluxnetworks.api.device.IFluxProvider",
        "sonar.fluxnetworks.api.device.IFluxDevice",
        "sonar.fluxnetworks.api.device.IFluxStorage",
        "sonar.fluxnetworks.api.device.FluxDeviceType",
        "sonar.fluxnetworks.common.connection.TransferHandler",
        "sonar.fluxnetworks.common.device.TileFluxDevice"
    };

    public static final FluxNeoForgeApiHandle INSTANCE = create();

    private final boolean fluxPresent;
    private final String missingClassName;

    private FluxNeoForgeApiHandle(boolean fluxPresent, String missingClassName) {
        this.fluxPresent = fluxPresent;
        this.missingClassName = missingClassName;
    }

    private static FluxNeoForgeApiHandle create() {
        ClassLoader classLoader = FluxNeoForgeApiHandle.class.getClassLoader();
        if (!hasClass(classLoader, REQUIRED_CLASSES[0])) {
            return new FluxNeoForgeApiHandle(false, null);
        }
        for (String className : REQUIRED_CLASSES) {
            if (!hasClass(classLoader, className)) {
                return new FluxNeoForgeApiHandle(true, className);
            }
        }
        return new FluxNeoForgeApiHandle(true, null);
    }

    private static boolean hasClass(ClassLoader classLoader, String className) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public boolean isFluxPresent() {
        return fluxPresent;
    }

    public boolean isSupported() {
        return fluxPresent && missingClassName == null;
    }

    public String getMissingClassName() {
        return missingClassName;
    }
}