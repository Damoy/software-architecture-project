package com.xaamruda.bbm.chaos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Connection;
import com.xaamruda.bbm.commons.json.JsonUtils;
import com.xaamruda.bbm.commons.logging.BBMLogger;
import com.xaamruda.bbm.commons.spring.context.ContextProvider;
import com.xaamruda.bbm.offers.OffersIOHandler;

@Component
public class ChaosManager {

	static private volatile int chaosLevel = 10;
	static private volatile boolean chaosActivated = false;
	final static private Random  chaosProvider = new Random(new Date().getTime());

	public ChaosManager() {}

	@Autowired 
	DataSource dataSource;


	@Autowired
	private/* static*/ OffersIOHandler offersIOHandler;

	public void lsPrint() throws IOException {
		final Process p = Runtime.getRuntime().exec("ls");

		new Thread(new Runnable() {
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null; 

				try {
					while ((line = input.readLine()) != null)
						System.out.println(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}


	public String handle(String jsonEvents) throws IOException {
		JsonObject jsonObject = JsonUtils.getFromJson(jsonEvents);
		JsonElement event = jsonObject.get("event");
		switch(event.getAsString()) {
		case "runCommand":{
			JsonElement data = jsonObject.get("data");
			runCommand(data.getAsString());
			break;
		}
		case "shutDownDB":{
			//JsonElement data = jsonObject.get("data");
			//runCommand(data.getAsString());
			this.offersIOHandler.shutDownDB();;
			break;
		}
		case "toggle":{
			this.toggleChaos();
			break;
		}
		case "change":{
			JsonElement data = jsonObject.get("data");
			this.changeChaos(data.getAsInt());
			break;
		}
		case "datasource":{
			try {
				java.sql.Connection con = dataSource.getConnection();
				System.out.println(con.isClosed());
				con.close();
				return "Datasource up";
			} catch (SQLException e) {
				return "Datasource down";
			}
		}
		case "closeConnection":{
			try {
				
				System.out.println();
				dataSource.getConnection().close();
				
				return "Connexion Closed";
				
			} catch (SQLException e) {
				//e.printStackTrace();
			}
			break;
		}
		default:{
			BBMLogger.errorln("No Admin event");
		}
		}
		return "Request " + event.getAsString() +  " OK";

	}

	public void run() throws IOException {
		final Process p = Runtime.getRuntime().exec("ls");
		new Thread(new Runnable() {
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null; 

				try {
					while ((line = input.readLine()) != null)
						System.out.println(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void runCommand(String str) throws IOException {
		final Process p = Runtime.getRuntime().exec(str);
		new Thread(new Runnable() {
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				try {
					while ((line = input.readLine()) != null)
						System.out.println(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void toggleChaos() throws IOException {
		chaosActivated  = !chaosActivated;
		System.out.println(chaosActivated);
	}

	public void changeChaos(int level) throws IOException {
		this.chaosLevel  = level;
	}

	public static void shutDownDataBase() {
		System.out.println();
		if(ChaosManager.chaosActivated && chaosProvider.nextInt(1000) < chaosLevel) {
			ContextProvider.getBean(ChaosManager.class).offersIOHandler.shutDownDB();
		}
	}


}
