package main.java.agents;

import edu.wlu.cs.levy.CG.KeySizeException;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import main.java.gui.BoardPanel;
import main.java.utils.AgentInTree;
import javafx.geometry.Point2D;

import java.util.ArrayList;

/**
 * Created by Fortun on 2014-12-03.
 *
 */
public class CommanderMinionBehaviour extends ReactiveBehaviour {

    boolean stance = false;
    Double commanderPosX = new Double(0);
    Double commanderPosY = new Double(0);
    double speedVec;

    @Override
    public void handleMessage(ACLMessage msg) {
        switch(msg.getConversationId()) {
            case "stance-fight":
                commanderPosX = Double.parseDouble(msg.getUserDefinedParameter("commanderPosX"));
                commanderPosY = Double.parseDouble(msg.getUserDefinedParameter("commanderPosY"));
                stance = true;
                break;
            case "stance-march":
                commanderPosX = Double.parseDouble(msg.getUserDefinedParameter("commanderPosX"));
                commanderPosY = Double.parseDouble(msg.getUserDefinedParameter("commanderPosY"));
                stance = false;
                speedVec = Double.parseDouble(msg.getUserDefinedParameter("speedVecXVal"));
                break;
            case "commander-dead":
                ((CannonFodder)myAgent).morale -= 10;
                myAgent.removeBehaviour(new CommanderMinionBehaviour());
                myAgent.addBehaviour(new BerserkBehaviour());
                commander = null;
                break;
        }
    }

    @Override
    public void decideOnNextStep() {
        CannonFodder agent = (CannonFodder) myAgent;
        if(stance) {
            ArrayList<AID> enemiesInRange = ((CannonFodder) myAgent).enemyInRange(agent);
            if (enemiesInRange.size() == 0) {
                if(commanderPosX != null && commanderPosY != null){
                    Point2D destination = new Point2D(commanderPosX, commanderPosY);
                    System.out.println("ide " + myAgent.getLocalName());
                    agent.world.moveAgent(agent, destination);
                }
            }
            else {
                enemyPosition = ((CannonFodder)myAgent).getNearestEnemy();
                if (((CannonFodder) myAgent).enemyInRangeOfAttack(enemyPosition))
                    doAction(() -> ((CannonFodder) myAgent).attack(enemy, enemyPosition));
                else
                    doAction(() -> ((CannonFodder) myAgent).gotoEnemy(enemyPosition));
            }
        }
        else {
            if(commanderPosX != null && commanderPosY != null){
                System.out.println(commanderPosX + " " + commanderPosY);
                Point2D destination = new Point2D(commanderPosX, commanderPosY);
                System.out.println("ide " + myAgent.getLocalName());
                agent.world.moveAgent(agent, destination);
            }
        }
    }

    private void doAction(Runnable action) {
        if (enemyPosition.isDead) {
            enemyPosition = null;
            enemy = null;
            state--;
            return ;
        }
        action.run();
    }
}
