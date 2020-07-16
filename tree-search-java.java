import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
class Point {
    int x;
    int y;
    public Point (int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void copy(Point p) {
        x = p.x;
        y = p.y;
    }
    public double distanceWith(Point p) {
        return Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
    }
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
class Game {
    int width;
    int height;
    //height //width
    ArrayList<ArrayList<Cell>> cells;
    HashSet<Cell> cellsWithMaxValue = new HashSet<>();
    PlayerInfo me = new MePlayerInfo();
    PlayerInfo opponent = new PlayerInfo();
    HashMap<Integer, Pac> pacs = new HashMap<>();
    int nbCell = 0;
    int maxPoint = 0;
    int visiblePoint;
    HashSet<Cell> visibleCell = new HashSet<>();
    int round = -1;
    long startTime;
    Scanner in;
    Game (Scanner in) {
        this.in = in;
        width = in.nextInt(); // size of the grid
        startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        height = in.nextInt(); // top left corner is (x=0, y=0)
        if (in.hasNextLine()) {
            in.nextLine();
        }
        cells = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            ArrayList<Cell> row = new ArrayList<>();
            cells.add(row);
            for (int j = 0; j < width; j++) {
                row.add(new Cell(j, i, this));
            }
        }
        for (int i = 0; i < height; i++) {
            String rowString = in.nextLine(); // one line of the grid: space " " is floor, pound "#" is wall
            ArrayList<Cell> row = cells.get(i);
            for (int j = 0; j < width; j++) {
                Cell cell = row.get(j);
                cell.setFloor(rowString.charAt(j) != '#');
                cell.left = row.get(j != 0 ? j - 1 : width - 1);
                cell.right = row.get(j != width - 1 ? j + 1 : 0);
                cell.top = cells.get(i != 0 ? i - 1 : height - 1).get(j);
                cell.bottom = cells.get(i != height - 1 ? i + 1 : 0).get(j);
                if (rowString.charAt(j) != '#') maxPoint++;
            }
        }
    }
    void readLines () {
        round++;
        visiblePoint = 0;
        me.score = in.nextInt();
        startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        opponent.score = in.nextInt();
        int visiblePacCount = in.nextInt();
        for (int i = 0; i < visiblePacCount; i++) {
            int pacId = in.nextInt();
            boolean mine = in.nextInt() != 0;
            int x = in.nextInt();
            int y = in.nextInt();
            String typeId = in.next();
            int speedTurnsLeft = in.nextInt();
            int abilityCooldown = in.nextInt();
            Cell cell = getCell(x, y);
            if (!pacs.containsKey(pacId + (mine ? 0 : 10))) {
                Pac pac = new Pac(pacId, mine ? me : opponent, cell, round, typeId, speedTurnsLeft, abilityCooldown);
                pacs.put(pacId + (mine ? 0 : 10), pac);
                if (mine) maxPoint -= 2;
            } else {
                pacs.get(pacId + (mine ? 0 : 10)).update(cell, round, typeId, speedTurnsLeft, abilityCooldown);
            }
        }
        int visiblePelletCount = in.nextInt(); // all pellets in sight
        for (int i = 0; i < visiblePelletCount; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
            int value = in.nextInt(); // amount of points this pellet is worth
            Cell cell = getCell(x, y);
            cell.setValue(value, round);
            if (value == 10) {
                if (round == 0) {
                    cellsWithMaxValue.add(cell);
                    maxPoint += 9;
                }
                visibleCell.add(cell);
            }
        }
        for (Pac pac: pacs.values()) {
            pac.checkUpdate(round);
        }
        for (Cell cell: new HashSet<>(cellsWithMaxValue)) {
            if (cell.lastUpdate != round) {
                cell.setValue(round, 0);
                cellsWithMaxValue.remove(cell);
            }
        }
    }
    void playAt(ArrayList<Action> mouvs) {
        boolean notFirst = false;
        for (Action move: mouvs) {
            if (notFirst)
                System.out.print("|");
            else
                notFirst = true;
            System.out.print(move.getAction());
        }
        System.out.println();
    }
    void printTab() {
        for (ArrayList<Cell> rows: cells) {
            for (Cell cell: rows)
                cell.printCell();
            System.err.println();
        }
    }
    void printTabPac() {
        for (ArrayList<Cell> rows: cells) {
            for (Cell cell: rows)
                cell.printCellPac();
            System.err.println();
        }
    }
    void printTimer(String msg) {
        long endTime = ZonedDateTime.now().toInstant().toEpochMilli();
        System.err.println(msg + " " + (endTime-startTime) + "ms");
    }
    Cell getCell(int x, int y) {
        return cells.get(y).get(x);
    }
}
class Cell extends Point {
    Game game;
    Cell left;
    Cell right;
    Cell top;
    Cell bottom;
    int lastUpdate = 0;
    private double value = 0;
    Boolean isFloor;
    Pac pac = null;
    Cell (int x, int y, Game g) {
        super(x, y);
        game = g;
    }
    void setFloor(boolean isFloor) {
        this.isFloor = isFloor;
        if (isFloor) {
            value = 1;
            game.nbCell++;
        }
    }
    void setValue(int val, int when) {
        value = val;
        lastUpdate = when;
        game.visiblePoint += val;
    }
    double getValue() {
        return value;
    }
    void checkUpdate (int time) {
        checkUpdateLeft(time, this);
        checkUpdateRight(time, this);
        checkUpdateTop(time, this);
        checkUpdateBottom(time, this);
    }
    void checkUpdateLeft (int time, Cell startCell) {
        if (!isFloor) return;
        if (time != lastUpdate) value = 0;
        if (startCell == left) return;
        left.checkUpdateLeft(time, startCell);
        game.visibleCell.add(this);
    }
    void checkUpdateRight (int time, Cell startCell) {
        if (!isFloor) return;
        if (time != lastUpdate) value = 0;
        if (startCell == right) return;
        right.checkUpdateRight(time, startCell);
        game.visibleCell.add(this);
    }
    void checkUpdateTop (int time, Cell startCell) {
        if (!isFloor) return;
        if (time != lastUpdate) value = 0;
        if (startCell == top) return;
        top.checkUpdateTop(time, startCell);
        game.visibleCell.add(this);
    }
    void checkUpdateBottom (int time, Cell startCell) {
        if (!isFloor) return;
        if (time != lastUpdate) value = 0;
        if (startCell == bottom) return;
        bottom.checkUpdateBottom(time, startCell);
        game.visibleCell.add(this);
    }
    void printCell() {
        char c = ' ';
        if (!isFloor) c = '-';
        if (getValue() == 1) c = 'x';
        if (getValue() == 10) c = 'X';
        if (getValue() < 1 && getValue() > 0) c = '?';
        System.err.print(c);
    }
    void printCellPac() {
        char c = ' ';
        if (!isFloor) c = '-';
        if (pac!=null && pac.owner.isMe) c = 'M';
        if (pac!=null && !pac.owner.isMe) c = 'E';
        System.err.print(c);
    }
    PathValue getMaxPath(HashSet<Cell> cellsVisited, HashSet<Cell> cellsNotAllow, int depth, Pac pac) {
        if (!isFloor || this.pac != null && this.pac != pac) {
            return new PathValue(-1000, cellsVisited, cellsNotAllow, this, -1000);
        }
        if (depth == 0) {
            return new PathValue(0, cellsVisited, cellsNotAllow, this, 0);
        }
        if (cellsNotAllow.contains(this)) {
            return new PathValue(0, cellsVisited, cellsNotAllow, this, 0);
        }
        double value = 0;
        if (!cellsVisited.contains(this)) {
            value += getValue();
            cellsVisited.add(this);
        }
        cellsNotAllow.add(this);
        PathValue maxValue = left.getMaxPath(new HashSet<>(cellsVisited), new HashSet<>(cellsNotAllow), depth - 1, pac);
        Cell nextCell = left;
        PathValue pathValue = right.getMaxPath(new HashSet<>(cellsVisited), new HashSet<>(cellsNotAllow), depth - 1, pac);
        if (pathValue.priority > maxValue.priority || pathValue.priority == maxValue.priority && pathValue.value > maxValue.value) {
            maxValue = pathValue;
            nextCell = right;
        }
        pathValue = top.getMaxPath(new HashSet<>(cellsVisited), new HashSet<>(cellsNotAllow), depth - 1, pac);
        if (pathValue.priority > maxValue.priority || pathValue.priority == maxValue.priority && pathValue.value > maxValue.value) {
            maxValue = pathValue;
            nextCell = top;
        }
        pathValue = bottom.getMaxPath(new HashSet<>(cellsVisited), new HashSet<>(cellsNotAllow), depth - 1, pac);
        if (pathValue.priority > maxValue.priority || pathValue.priority == maxValue.priority && pathValue.value > maxValue.value) {
            maxValue = pathValue;
            nextCell = bottom;
        }
        maxValue.priority += value * depth;
        maxValue.value += value;
        if (!(pac.speedTurnsLeft != 0 && depth % 2 == 0 && maxValue.nextCell != this)) {
            maxValue.nextCell = nextCell;
        }
        return maxValue;
    }
}
class PacType {
    static PacType ROCK = new PacType("ROCK");
    static PacType SCISSORS = new PacType("SCISSORS");
    static PacType PAPER = new PacType("PAPER");
    String name;
    PacType (String name) {
        this.name = name;
    }
    static PacType get(String name) {
        if (name.equals("ROCK")) return ROCK;
        if (name.equals("PAPER")) return PAPER;
        if (name.equals("SCISSORS")) return SCISSORS;
        return null;
    }
}
class Pac {
    PlayerInfo owner;
    int id;
    private Cell curCell;
    int lastUpdate;
    boolean isAlive = true;
    PacType pacType;
    int speedTurnsLeft;
    int abilityCooldown;
    Pac (int id, PlayerInfo owner, Cell cell, int lastUpdate, String pacType, int speedTurnsLeft, int abilityCooldown) {
        this.id = id;
        this.owner = owner;
        owner.pacs.add(this);
        update(cell, lastUpdate, pacType, speedTurnsLeft, abilityCooldown);
    }
    void update(Cell cell, int lastUpdate, String pacType, int speedTurnsLeft, int abilityCooldown) {
        if (isAlive) {
            if (curCell != null && curCell.pac == this) curCell.pac = null;
            if (pacType.equals("DEAD")) {
                isAlive = false;
                owner.pacs.remove(this);
                return;
            }
            cell.pac = this;
            curCell = cell;
            this.lastUpdate = lastUpdate;
            this.pacType = PacType.get(pacType);
            this.speedTurnsLeft = speedTurnsLeft;
            this.abilityCooldown = abilityCooldown;
        }
    }
    void checkUpdate(int current) {
        if (owner.isMe) {
            if (current == lastUpdate && isAlive) {
                getPos().checkUpdate(current);
            }
        } else {
            if (current != lastUpdate) {
                if (curCell != null && curCell.pac == this) curCell.pac = null;
                curCell = null;
            }
        }
    }
    Cell getPos() {
        return curCell;
    }
}
class PlayerInfo {
    Boolean isMe = false;
    int score;
    ArrayList<Pac> pacs = new ArrayList<>();
}
class MePlayerInfo extends PlayerInfo {
    MePlayerInfo () {
        isMe = true;
    }
}
abstract class Action {
    Pac pac;
    String msg;
    Action(Pac pac, String msg) {
        this.pac = pac;
        this.msg = msg;
    }
    abstract String getAction();
}
class Move extends Action {
    Cell to;
    Move (Pac p, Cell c) {
        super(p, "");
        to = c;
    }
    Move (Pac p, Cell c, String m) {
        super(p, m);
        to = c;
    }
    String getAction() {
        return "MOVE " + pac.id + " " + to.x + " " + to.y + " " + msg;
    }
}
class Speed extends Action {
    Speed (Pac p) {
        super(p, "");
    }
    Speed (Pac p, String m) {
        super(p, m);
    }
    String getAction() {
        return "SPEED " + pac.id + " " + msg;
    }
}
class Switch extends Action {
    PacType pacType;
    Switch (Pac p, PacType pt) {
        super(p, "");
        pacType = pt;
    }
    Switch (Pac p, PacType pt, String m) {
        super(p, m);
        pacType = pt;
    }
    String getAction() {
        return "SWITCH " + pac.id + " " + pacType.name + " " + msg;
    }
}
class Strategies {
    static ArrayList<Action> maxSumPath(Game game) {
        ArrayList<Action> moves = new ArrayList<>();
        HashSet<Cell> cellsVisited = new HashSet<>();
        HashSet<Cell> cellsNotAllow = new HashSet<>();
        for (Pac pac: game.me.pacs) {
            if (pac.abilityCooldown == 0) {
                moves.add(new Speed(pac));
            } else {
                PathValue pathValue = pac.getPos().getMaxPath(new HashSet<>(cellsVisited), new HashSet<>(cellsNotAllow), 18, pac);
                cellsNotAllow.add(pathValue.nextCell);
                cellsVisited = pathValue.cellsVisited;
                moves.add(new Move(pac, pathValue.nextCell));
            }
        }
        return moves;
    }
}
class PathValue {
    double value;
    HashSet<Cell> cellsVisited;
    HashSet<Cell> cellsNotAllow;
    Cell nextCell;
    int priority;
    PathValue(int value, HashSet<Cell> cellsVisited, HashSet<Cell> cellsNotAllow, Cell nextCell, int priority) {
        this.value = value;
        this.cellsVisited = cellsVisited;
        this.cellsNotAllow = cellsNotAllow;
        this.nextCell = nextCell;
        this.priority = priority;
    }
}
class Player {
    public static void main(String args[]) throws Exception {
        Game game = new Game(new Scanner(System.in));
        game.printTimer("Init =>");
        // game loop
        while (true) {
            game.readLines();
            ArrayList<Action> nextMoves = Strategies.maxSumPath(game);
            game.playAt(nextMoves);
            game.printTimer("=>");
        }
    }
}
