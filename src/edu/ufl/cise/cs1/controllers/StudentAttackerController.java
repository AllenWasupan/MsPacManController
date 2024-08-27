package edu.ufl.cise.cs1.controllers;
import game.controllers.AttackerController;
import game.models.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public final class StudentAttackerController implements AttackerController
{
	public void init(Game game) { }

	public void shutdown(Game game) { }

	public int update(Game game,long timeDue)
	{
		int action = -1;

		Attacker gator = game.getAttacker();
		List<Node> pill = new ArrayList<>();
		//Goes to nearest pill
		if (game.getPillList().size() > 0) {
			pill = game.getPillList();
		}
		else {
			pill = game.getPowerPillList();
		}
		action = gator.getNextDir(gator.getTargetNode(pill,true), true);

		//Shows nearest enemy
		Actor danger = gator.getTargetActor(game.getDefenders(), true);
		Defender bad = game.getDefender(0);
		Defender badling = game.getDefender(1);
		List<Defender> badboys = game.getDefenders();


		//Overall, checks for nearest enemy and decides if they're close enough to run towards or away from
		for (int runs = 0; runs < 3; ++runs) {

			for (int i = 0; i < 4; ++i){
				//Aligns the closest enemy as the defender
				if (danger.getLocation() == game.getDefender(i).getLocation()) {
					if (runs == 0) {
						bad = badboys.get(i);
						badboys.remove(i);
						danger = gator.getTargetActor(badboys,true);
						gator.addPathTo(game,Color.RED, bad.getLocation());
						break;
					}
					else if (runs == 1 && i < 3)  {
						badling = badboys.get(i);
						badboys.remove(i);
						if (bad.getLocation().getPathDistance(badling.getLocation()) > 5) {
							gator.addPathTo(game,Color.YELLOW, badling.getLocation());
							runs++;
						}

						break;
					}
					else if (i < 2) {
						badling = badboys.get(i);
						gator.addPathTo(game,Color.YELLOW, badling.getLocation());
					}
				}
			}
		}

		List<Node> junc = game.getCurMaze().getJunctionNodes();
		int junkcount = 0;
		for (Node junk : junc) {
			junkcount++;
			if (junk.getNumNeighbors() <= 2) {
				junc.remove(junkcount);
			}
		}

		//Checks if any enemy is close
		//If the defender is vulnerable and there is still time, go towards it
		if ((bad.isVulnerable() && bad.getVulnerableTime() > 3) && gator.getLocation().getPathDistance(bad.getLocation()) <= 20) {
			action = gator.getNextDir(bad.getLocation(), true);
			//System.out.println("KILL KILL KILL");
		}

		//If the defender isn't vul, and the defender is close, run away
		else if ((gator.getLocation().getPathDistance(bad.getLocation()) <= 10) && game.getLevelTime() > bad.getLairTime()) {

			//Transfers the possible location to lists
			List <Node> badSpots = bad.getPossibleLocations();
			List <Node> spots = gator.getPossibleLocations(false);
			List<Integer> directions = gator.getPossibleDirs(true);
			//Checks the locations of the closest enemy to the gator
			for (Node badNode : badSpots) {
				for (Node playerNode : spots) {
					if(playerNode == badNode) {
						//If the nearest pill and either the closest enemy are in the same direction, go away
						if ((gator.getNextDir(bad.getLocation(),true) == gator.getNextDir(gator.getTargetNode(pill,true), true)) || (gator.getNextDir(badling.getLocation(),true) == gator.getNextDir(gator.getTargetNode(pill,true), true))) {
							action = gator.getNextDir(bad.getLocation(),false);
							//System.out.println("I'm running!" );

							//If the next enemy and second next enemy are not in the same direction, go to the nearest junction
							if ((gator.getNextDir(bad.getLocation(),false) != gator.getNextDir(badling.getLocation(),false)) && gator.getLocation().getPathDistance(badling.getLocation()) < 20) {

								//System.out.println("Whoa! Me: " + gator.getLocation() + " Junction: " + gator.getTargetNode(game.getCurMaze().getJunctionNodes(), true));

								//Once at a junction, goes in the direction without enemies
								if (gator.getLocation().isJunction() && gator.getLocation().getNumNeighbors() > 2) {

									//System.out.println("I'm in a pickle!");

									//Runs as long as there is space in the thing
									for (int numdirec = 0; numdirec < directions.size(); ++numdirec) {
										//Removes if there is a similarity
										if ((directions.get(numdirec) == gator.getNextDir(bad.getLocation(),true))) {
											directions.remove(numdirec);
										}
										else if ((directions.get(numdirec) == gator.getNextDir(badling.getLocation(),true))) {
											directions.remove(numdirec);
										}
									}
									//If the size is greater than 1, just make it smaller
									while (directions.size() > 1) {
										directions.remove(0);
									}
									//If the directions are at 1, go in this direction
									if (directions.size() == 1) {
										action = directions.get(0);
										break;
									}

								}
								action = gator.getNextDir(gator.getTargetNode(junc, true),true);

								//If the power pill is closer than a junction, go there when trapped

								if (game.getPowerPillList().size() > 0 && gator.getLocation().getPathDistance(gator.getTargetNode(game.getPowerPillList(),true)) < gator.getLocation().getPathDistance(gator.getTargetNode(game.getCurMaze().getJunctionNodes(), true))) {
									action = gator.getNextDir(gator.getTargetNode(game.getPowerPillList(),true),true);
								}
								break;
							}

							//If the enemy is in the same direction as the nearest pill, run away
							else if (gator.getNextDir(bad.getLocation(),true) != gator.getNextDir(gator.getTargetNode(pill,false), true)) {
								//System.out.println("Get OUT");

								action = gator.getNextDir(gator.getTargetNode(pill,false), true);
								/*
								if (gator.getNextDir(gator.getTargetNode(pill,false), true) == gator.getNextDir(badling.getLocation(),true)) {

								}

								 */

							}
						}
					}
				}
			}
		}

		//System.out.println(gator.getLocation().getX() + " " + gator.getLocation().getY());

		return action;
	}

}