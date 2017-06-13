package org.centauri.cloud.cloud;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.centauri.cloud.cloud.plugin.ModuleLoader;
import lombok.Getter;
import lombok.Setter;
import org.centauri.cloud.cloud.config.WhitelistConfig;
import org.centauri.cloud.cloud.config.PropertyManager;
import org.centauri.cloud.cloud.event.EventManager;
import org.centauri.cloud.cloud.io.Console;
import org.centauri.cloud.cloud.listener.OpenCloudCommandListener;
import org.centauri.cloud.cloud.listener.TestListener;
import org.centauri.cloud.cloud.network.NettyServer;
import org.centauri.cloud.cloud.server.ServerManager;

public class Cloud {

	@Getter private static Cloud instance;
	@Getter @Setter private boolean running;
	@Getter private EventManager eventManager;
	@Getter private ServerManager serverManager;
	@Getter private NettyServer server;
	@Getter private ModuleLoader moduleManager;
	@Getter private Set<String> whitelistedHosts;
	
	//configurations
	@Getter @Setter private int timeout = 30;
	@Getter @Setter private int pingerIntervall = 25;
	@Getter @Setter private boolean whitelistActivated;
	
	public Cloud() {
		instance = this;
	}
	
	private void start(String... args) {
		this.printFancyCopyright();
		PropertyManager manager = new PropertyManager();
		manager.load();
		manager.initVariables(this);
		
		this.running = true;
		
		if(this.whitelistActivated) {
			this.whitelistedHosts = new HashSet<>();
			try {
				new WhitelistConfig();
			} catch (IOException ex) {
				ex.printStackTrace();
				this.running = false;
			}
		}
				
		this.eventManager = new EventManager();
		
		this.moduleManager = new ModuleLoader();
		this.moduleManager.initializeScheduler();
		
		this.serverManager = new ServerManager();
		
		this.server = new NettyServer();
		new Thread(() -> {
			try {
				server.run(Integer.valueOf(manager.getProperties().getProperty("port")));
			} catch (Exception ex) {
				ex.printStackTrace();
				this.stop(); //Stop server
			}
		}, "Netty-Thread").start();
		
		this.registerListeners();
		
		System.out.println("Cloud started");
		
		new Console();
		System.out.println("Cloud stopped");
		System.exit(0);
	}
	
	public void stop(){
		server.stop();
		running = false;
	}
	
	private void registerListeners() {
		this.eventManager.registerEventHandler(new OpenCloudCommandListener());
		this.eventManager.registerEventHandler(new TestListener());
	}
	
	private void printFancyCopyright() {
		System.out.println("######################################################\n" +
							"#   ___                    ____ _                 _  #\n" +
							"#  / _ \\ _ __   ___ _ __  / ___| | ___  _   _  __| | #\n" +
							"# | | | | '_ \\ / _ \\ '_ \\| |   | |/ _ \\| | | |/ _` | #\n" +
							"# | |_| | |_) |  __/ | | | |___| | (_) | |_| | (_| | #\n" +
							"#  \\___/| .__/ \\___|_| |_|\\____|_|\\___/ \\__,_|\\__,_| #\n" +
							"#       |_|                                          #\n" +
							"#   -- Master --                                     #\n" +
							"# by Centauri Team                                   #\n" +
							"######################################################\n");
	}
	
	public static void main(String... args) {
		new Cloud().start(args);
	}
	
}