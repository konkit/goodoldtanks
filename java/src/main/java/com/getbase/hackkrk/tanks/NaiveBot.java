package com.getbase.hackkrk.tanks;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getbase.hackkrk.tanks.api.Command;
import com.getbase.hackkrk.tanks.api.GameSetup;
import com.getbase.hackkrk.tanks.api.Outcome;
import com.getbase.hackkrk.tanks.api.Tank;
import com.getbase.hackkrk.tanks.api.TanksClient;
import com.getbase.hackkrk.tanks.api.TurnResult;

public class NaiveBot {
	private static final String TRIPLEKILL = "triplekill";
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
			System.out.println("master");
		} else {
			client = new TanksClient("http://10.12.202.144:9999", "sandbox-3",
					"ClementLimeGreenStinkbugGiraffe");
			System.out.println("sandbox-3");
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

		TurnResult result = client.submitMove(generateRandomFireCommand());
		double currentPosition = getTankPosition(result);

		boolean tankOnEdge = false;
		while (!tankOnEdge) {
			result = client.submitMove(Command.move(-480d - currentPosition));
			currentPosition = getTankPosition(result);
			if (-500 - currentPosition < 30) {
				tankOnEdge = true;
			}
		}

		Command command = generateCommand();
		while (!gameFinished) {
			Outcome outcome = getOutcome(result);
			if (!Outcome.HitType.tank_hit.equals(outcome.type) || outcome.targetDestroyed) {
				command = generateCommand();
			}
			result = client.submitMove(command);

			gameFinished = result.last;
		}
	}

	private Outcome getOutcome(TurnResult result) {
		for (Outcome outcome : result.outcome) {
			if (TRIPLEKILL.equals(outcome.name)) {
				return outcome;
			}
		}
		return null;
	}

	private double getTankPosition(TurnResult result) {
		for (Tank tank : result.tanks) {
			if (TRIPLEKILL.equals(tank.name)) {
				return tank.position.x;
			}
		}
		return 0d;
	}

	public Command generateRandomFireCommand() {
		return Command.fire(rand.nextInt(180) - 90, rand.nextInt(70) + 30);
	}

	public Command generateCommand() {
		// if (rand.nextDouble() > 0.85) {
		return Command.fire(rand.nextInt(80) + 10, rand.nextInt(70) + 30);
		// } else {
		// return Command.move(rand.nextDouble() > 0.5 ? -100 : 100);
		// }
	}
}
