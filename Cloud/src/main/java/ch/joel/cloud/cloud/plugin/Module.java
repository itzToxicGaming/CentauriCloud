package ch.joel.cloud.cloud.plugin;

public interface Module {

	String getName();

	String getVersion();

	String getAuthor();

	void onEnable();

	void onDisable();

}
