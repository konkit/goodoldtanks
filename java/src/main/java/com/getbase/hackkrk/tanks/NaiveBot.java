package com.getbase.hackkrk.tanks;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getbase.hackkrk.tanks.api.Command;
import com.getbase.hackkrk.tanks.api.GameSetup;
import com.getbase.hackkrk.tanks.api.Tank;
import com.getbase.hackkrk.tanks.api.TanksClient;
import com.getbase.hackkrk.tanks.api.TurnResult;

public class NaiveBot {
	private static final Logger log = LoggerFactory.getLogger(NaiveBot.class);
	private Random rand = ThreadLocalRandom.current();

	public static void main(String... args) throws Exception {
		new NaiveBot().run(args[0]);
	}

	public void run(String game) throws Exception {
		TanksClient client = null;

		if ("master".equals(game)) {
			client = new TanksClient("http://10.12.202.141:9999", "master",
					"ClementLimeGreenStinkbugGiraffe");
		} else {
			client = new TanksClient("http://10.12.202.144:9999", "sandbox-3",
					"ClementLimeGreenStinkbugGiraffe");
		}

		while (true) {
			log.info("Waiting for the next game...");
			GameSetup gameSetup = client.getMyGameSetup();
			log.info("Playing {}", gameSetup);

			playGame(gameSetup, client);
		}
	}

	private void playGame(GameSetup gameSetup, TanksClient client) {
		boolean gameFinished = false;

		TurnResult result = client.submitMove(generateCommand());

		for (Tank tank : result.tanks) {
			if ("triplekill".equals(tank.name)) {
				double x = tank.position.x;
				client.submitMove(Command.move(-470d - x));
			}
		}

		while (!gameFinished) {
			result = client.submitMove(generateCommand());

			gameFinished = result.last;
		}
	}

	public Command generateCommand() {
		// if (rand.nextDouble() > 0.85) {
		return Command.fire(rand.nextInt(80) + 10, rand.nextInt(70) + 30);
		// } else {
		// return Command.move(rand.nextDouble() > 0.5 ? -100 : 100);
		// }
	}
}
