package org.centauri.cloud.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Properties;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.centauri.cloud.centauricloud.connector.netty.Client;
import org.centauri.cloud.daemon.config.CloudConfiguration;
import org.centauri.cloud.daemon.netty.NetworkHandler;
import org.centauri.cloud.daemon.server.ServerManager;
import org.centauri.cloud.daemon.util.LoadTimer;

@Log4j2
public class Daemon {
	
	@Getter private static Daemon instance;
	@Getter private Client client;
	@Getter private CloudConfiguration cloudConfiguration;
	@Getter private ServerManager serverManager;
	public Daemon() {
		instance = this;
	}
	
	@SneakyThrows
	public void start(){
		this.serverManager = new ServerManager();
		
		Properties properties = new Properties();
		File configFile = new File("config.properties");
		if(!configFile.exists())
			Files.copy(this.getClass().getResourceAsStream("/config.properties"), configFile.toPath());
		properties.load(new FileInputStream(configFile));
		this.cloudConfiguration = new CloudConfiguration(properties);
	
		new File("templates/").mkdir();
		
		new Thread(() -> {
			System.out.println("Try to start netty client...");

			this.client = new Client(new NetworkHandler(), cloudConfiguration.getHostname(), cloudConfiguration.getPort());
			try {
				this.client.start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}, "Netty-Thread").start();
		
		new LoadTimer();
	}
	
	public static void main(String... args) {
		new Daemon().start();
	}
	
	

}