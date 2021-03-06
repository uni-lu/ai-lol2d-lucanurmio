package lol.client.ai;

import java.util.*;
import lol.game.*;
import lol.game.action.*;

public class RandomAI extends AIBase {
  protected Random random;
  protected BattlefieldTraversal traversal;

  public RandomAI(Arena arena, Battlefield battlefield) {
    super(arena, battlefield);
    traversal = new BattlefieldTraversal(battlefield);
    random = new Random();
  }
  public Turn championSelect() {
    Turn turn = new Turn();
    turn.registerAction(new ChampionSelect(teamID, randomChampionName()));
    turn.registerAction(new ChampionSelect(teamID, randomChampionName()));
    turn.registerAction(new ChampionSelect(teamID, randomChampionName()));
    return turn;
  }

  private String randomChampionName() {
    String championName = "Warrior";
    if(random.nextBoolean()) {
      championName = "Archer";
    }
    return championName;
  }

  public Turn turn() {
    Turn turn = new Turn();
    // Try to attack the Nexus first.
    tryAttackNexus(turn);
    //
    tryAttackMonster(turn);
    tryAttackTower(turn);
    trySelfHeal(turn);
    tryAttackChampion(turn);
    // Add a move action in case we could not attack the Nexus.
    tryMove(turn);
    return turn;
  }

  protected void tryAttackNexus(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), champion.attackRange(), new TileVisitor(){
        public void visitNexus(Nexus nexus) {
          if(nexus.teamOfNexus() != teamID) {
            turn.registerAction(new Attack(teamID, id, nexus.x(), nexus.y()));
          }
        }
      }));
  }

  // Champions don't attack enemy champions if it would kill them, as this instantly crashes the game
  protected void tryAttackChampion(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), champion.attackRange(), new TileVisitor(){
        public void visitChampion(Champion targetChampion) {
          if((targetChampion.teamID() != teamID) && (targetChampion.currentHP() > champion.damages())) {
            turn.registerAction(new Attack(teamID, id, targetChampion.x(), targetChampion.y()));
          }
        }
      }));
  }

  // Champions replenish their health next to friendly towers
  protected void trySelfHeal(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), 1, new TileVisitor(){
        public void visitTower(Tower tower) {
          if((tower.teamOfTower() == teamID) && (champion.currentHP() != champion.initialHP())) {
            turn.registerAction(new Attack(teamID, id, -1, -1));
            champion.reviveAt(champion.x(), champion.y());
          }
        }
      }));
  }

  protected void tryMove(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), champion.walkSpeed(), new TileVisitor(){
        public void visitGrass(int x, int y) {
          if(random.nextInt() % 3 == 0) {
            turn.registerAction(new Move(teamID, id, x, y));
          }
        }
      }));
  }

  protected void tryAttackMonster(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), champion.attackRange(), new TileVisitor(){
        public void visitMonster(Monster monster) {
          turn.registerAction(new Attack(teamID, id, monster.x(), monster.y()));
        }
      }));
  }

  private void tryAttackTower(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), champion.attackRange(), new TileVisitor(){
        public void visitTower(Tower tower) {
        if(tower.teamOfTower() != teamID) {
          turn.registerAction(new Attack(teamID, id, tower.x(), tower.y()));
        }
        }
      }));
  }
}
