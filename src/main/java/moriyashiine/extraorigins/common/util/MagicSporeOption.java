package moriyashiine.extraorigins.common.util;

public enum MagicSporeOption {
	LEFT("Left"), OFFENSE("Offense"),
	RIGHT("Right"), DEFENSE("Defense"),
	UP("Up"), MOBILITY("Mobility"),
	NONE("None");

	private final String name;

	MagicSporeOption(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public enum BackwardsCompatibleMagicSporeMode {
		OFFENSE, DEFENSE, MOBILITY, CONVERTED;
	}
}