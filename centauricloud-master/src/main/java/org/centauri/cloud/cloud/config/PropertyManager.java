package org.centauri.cloud.cloud.config;

import lombok.Getter;
import org.centauri.cloud.cloud.Cloud;
import org.centauri.cloud.common.network.util.ConfigUpdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class PropertyManager {

	@Getter private static PropertyManager instance;
	@Getter private Properties properties;

	public PropertyManager() {
		instance = this;
	}
	
	public void initVariables(Cloud cloud) {
		cloud.setTimeout(Integer.valueOf((String) properties.get("timeout")));
		cloud.setPingerIntervall(Integer.valueOf((String) properties.get("pingerIntervall")));
		cloud.setWhitelistActivated(Boolean.valueOf((String) properties.getProperty("whitelist")));
		cloud.setSharedDir(new File(properties.getProperty("sharedDir", "shared/")));
	}

	public void load() {
		File file = new File("config.properties");
		if (!file.exists()) {
			createFile(file);
		} else {
			if(new ConfigUpdater().updateConfig(file, "/config.properties")) {
				Cloud.getLogger().info("Configuration updated!");
			} else {
				Cloud.getLogger().error("Invalid filetype to update configuration!");
			}
		}
		try (InputStream inputStream = new FileInputStream(file)) {
			properties = new Properties();
			properties.load(inputStream);
		} catch (IOException e) {
			Cloud.getLogger().error(e.getMessage(), e);
		}
	}

	private void createFile(File out) {
		try {
			Files.copy(this.getClass().getResourceAsStream("/config.properties"), out.toPath());
		} catch (IOException e) {
			Cloud.getLogger().error(e.getMessage(), e);
		}

	}
}
