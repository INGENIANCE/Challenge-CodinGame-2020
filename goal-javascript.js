/**
 * Grab the pellets as fast as you can!
 **/
var inputs = readline().split(' ');
const width = parseInt(inputs[0]); // size of the grid
const height = parseInt(inputs[1]); // top left corner is (x=0, y=0)
let grid = [];
for (let y = 0; y < height; y++) {
    const row = readline(); // one line of the grid: space " " is floor, pound "#" is wall
    for (let x = 0; x < width; x++) {
        // Define all possible moves.
        if (row[x] === " ") grid.push({x: x, y: y, picker: null, big: false, pick: false});
    }    
}
// PAC Types.
const type = {
    ROCK: 'ROCK',
    PAPER: 'PAPER',
    SCISSORS: 'SCISSORS',
    DEAD: 'DEAD'
}
// PAC Abilities.
const ability = {
    MOVE: 'MOVE',
    SWITCH: 'SWITCH',
    SPEED: 'SPEED'
}
// Tool functions
const utils = {
    // Euclidean distance
    distBetweenPacs: function(p1, p2) {
        var deltaX = this.diff(p1.x, p2.x);
        var deltaY = this.diff(p1.y, p2.y);
        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    },
    // Absolute delta value
    diff: function(num1, num2) {
        return (num1 > num2) ? num1 - num2 : num2 - num1;
    },
    // Check point is adjacent
    isAdjacent: function(x1, y1, x2, y2) {
        return Math.abs(x2 - x1) < 2 && Math.abs(y2 - y1) < 2 && (x1 === x2 || y1 === y2);
    },
    // Sorted points by nearest
    sortByDist: function(pac, otherPacs, reverse = false) {
        return otherPacs.sort((a, b) => {
            return (reverse) ?
                this.distBetweenPacs(pac, b) - this.distBetweenPacs(pac, a):
                this.distBetweenPacs(pac, a) - this.distBetweenPacs(pac, b);
        });
    },
    // Get pellet from the grid
    findPellet: function(grid, x, y){
        return grid.find(pellet => pellet.x === x && pellet.y === y);
    },
    // Get nearest free big pellet
    findBigPellet: function(pacId) {
        return  grid.filter(pellet => pellet.big && !pellet.pick && [null, pacId].includes(pellet.picker));
    },
    // Pac go to nearest big pellet.
    firstMove: function (grid, pacs, pac){
        const pellets = this.findBigPellet(pac.pacId);
        const nearestPellets = pellets.filter(pellet => this.sortByDist(pellet, Object.assign([], pacs))[0].pacId === pac.pacId);
        const firstMove = this.sortByDist(pac, nearestPellets)[0];
        if (firstMove){
            firstMove.picker = pac.pacId;
            Object.assign([], grid, firstMove);
            return `${ability.MOVE} ${pac.pacId} ${firstMove.x} ${firstMove.y}`;
        } else {
            return this.nextMove(grid, pac);
        }
    },
    // Calculate next PAC move
    nextMove: function(grid, pac, nearestPac = null){
        let nextMove = null;
        const foundMove = grid.find(pellet => pellet.picker === pac.pacId && !pellet.pick);
        const getBigFirst = grid.some(pellet => pellet.big && !pellet.pick && [null, pac.pacId].includes(pellet.picker));
        console.error(`STOP: ${pac.stop}`);
        if (foundMove && !pac.stop) {     
            // Always have a target.       
            possibleMove = (nearestPac && grid.filter(pellet => !(pellet.x === nearestPac.x && pellet.y === nearestPac.y)
                                            && !this.isAdjacent(nearestPac.x, nearestPac.y, pellet.x, pellet.y)
                                            && this.isAdjacent(pac.x, pac.y, pellet.x, pellet.y)))
                            || grid.filter(pellet => this.isAdjacent(pac.x, pac.y, pellet.x, pellet.y));
            nextMove = (!getBigFirst && possibleMove.find(pellet => !pellet.pick)) || foundMove;
        } else {
            // New target.
            let pellets = getBigFirst && !pac.stop ?
                this.findBigPellet(pac.pacId) :
                grid.filter(pellet => !pellet.pick && [null, pac.pacId].includes(pellet.picker));                    
            const sortedPellet = this.sortByDist(pac, pellets);
            if (pac.stop){
                possibleMove = grid.filter(pellet => !(pellet.x === nearestPac.x && pellet.y === nearestPac.y)
                                            && !this.isAdjacent(nearestPac.x, nearestPac.y, pellet.x, pellet.y)
                                            && this.isAdjacent(pac.x, pac.y, pellet.x, pellet.y));
                nextMove = possibleMove.find(pellet => !pellet.pick) || possibleMove[0];
            } else {
                nextMove = sortedPellet[0];
            }
            if (nextMove == null) nextMove = this.findPellet(grid, pac.x, pac.y);
            if (!pac.stop) nextMove.picker = pac.pacId;
            Object.assign([], grid, nextMove);
        }
        console.error(nextMove);        
        return `${ability.MOVE} ${pac.pacId} ${nextMove.x} ${nextMove.y}`;
    },
    // Define new PAC type
    newType: function(vsType, pac){
        let changeType = null;
        switch (vsType) {
            case type.ROCK: changeType = type.PAPER; break;            
            case type.PAPER: changeType = type.SCISSORS; break;
            case type.SCISSORS: changeType = type.ROCK; break;
            case type.DEAD: changeType = pac.typeId; break;
        }
        console.error(`${ability.SWITCH} to ${changeType}`);
        return `${ability.SWITCH} ${pac.pacId} ${changeType}`;
    },
    // Check if switch type is necessary
    haveToSwitch: function (pac, vsPac) {
        switch (pac.typeId) {
            case type.ROCK: if (vsPac.typeId === type.SCISSORS) return false; break;            
            case type.PAPER: if (vsPac.typeId === type.ROCK) return false;; break;
            case type.SCISSORS: if (vsPac.typeId === type.PAPER) return false; break;
            case type.DEAD: return false; break;
        }
        return true;
    },
    // Activate SPEED
    speedUp: function(pacId){
        console.error(ability.SPEED);
        return `${ability.SPEED} ${pacId}`;
    },
    // Update pellet status in sight
    updateSight: function(pac, pelletsInSight){
        const directions = [{x:1, y:0}, {x:0, y:1}, {x:-1, y:0}, {x:0, y:-1}];
        directions.forEach(({x:dirX, y:dirY}) => {
            var step = 1;
            while (value = grid.find(pellet => pellet.x === (pac.x + (dirX * step)) && pellet.y === (pac.y + (dirY * step)))) {
                value.pick = !pelletsInSight.some(pellet => pellet.x === value.x && pellet.y === value.y);
                Object.assign([], grid, value);
                step++;
            }
        });
    }
};
let firstLoop = true;
// game loop
while (true) {
    var inputs = readline().split(' ');
    const myScore = parseInt(inputs[0]);
    const opponentScore = parseInt(inputs[1]);
    const visiblePacCount = parseInt(readline()); // all your pacs and enemy pacs in sight
    let pacsInfo = [];
    for (let i = 0; i < visiblePacCount; i++) {
        var inputs = readline().split(' ');
        const pacId = parseInt(inputs[0]); // pac number (unique within a team)
        const mine = inputs[1] !== '0'; // true if this pac is yours
        const x = parseInt(inputs[2]); // position in the grid
        const y = parseInt(inputs[3]); // position in the grid
        const typeId = inputs[4]; // unused in wood leagues
        const speedTurnsLeft = parseInt(inputs[5]); // unused in wood leagues
        const abilityCooldown = parseInt(inputs[6]); // unused in wood leagues
        pacsInfo.push({
            pacId: pacId,
            mine: mine,
            x: x,
            y: y,
            stop: false,
            typeId: typeId,
            speedTurnsLeft: speedTurnsLeft,
            abilityCooldown: abilityCooldown });
        // Update pellet status under PAC.
        let pellet = utils.findPellet(grid, x, y);
        pellet.pick = true;
    }
    // Update grid picker status with dead PAC.
    grid.filter(pellet => !pacsInfo.filter(pac => pac.mine)
            .map(pac => pac.pacId)
            .includes(pellet.picker))
        .forEach(pellet => pellet.picker = null);
    const visiblePelletCount = parseInt(readline()); // all pellets in sight
    let pelletsInSight = [];
    for (let i = 0; i < visiblePelletCount; i++) {
        var inputs = readline().split(' ');
        const x = parseInt(inputs[0]);
        const y = parseInt(inputs[1]);
        const value = parseInt(inputs[2]); // amount of points this pellet is worth
        let pellet = utils.findPellet(grid, x, y);
        // Check big pellets status on grid.
        if (value === 10) pellet.big = true;
        // Update pellet status on grid.
        pellet.pick = false;
        pelletsInSight.push(pellet);
    }
    // Update big pellets status on grid.
    grid.filter(pellet => pellet.big && !pelletsInSight.filter(pellet => pellet.big).includes(pellet))
        .forEach(pellet => pellet.pick = true);
    let moves = [];
    // Get my PACS positions.
    const myPacs = pacsInfo.filter(pac => pac.mine);
    myPacs.forEach(pac => {
        console.error(`PAC: ${pac.pacId}`);
        // Update pellets status in sight.
        utils.updateSight(pac, pelletsInSight);
        // Manage next move
        const otherPacs = pacsInfo.filter(other => other.pacId !== pac.pacId || !other.mine);
        const sortedPacsByDist = utils.sortByDist(pac, otherPacs);
        if (otherPacs.length === 0) {
            // Only one PAC or no ennemy PAC in sight
            moves.push(utils.nextMove(grid, pac));
        } else {
            // Get dist of the nearest PAC            
            const nearestPac = sortedPacsByDist[0];
            const nearestPacDist = utils.distBetweenPacs(pac, nearestPac);
            console.error(`nearestPac: ${nearestPac.pacId}, nearestPacDist: ${nearestPacDist}`);
            if (pac.abilityCooldown === 0 && (nearestPacDist >= 6 || !nearestPac.mine)) {
                // Speed up.
                moves.push(utils.speedUp(pac.pacId));
            } else if (pac.abilityCooldown === 0 && !nearestPac.mine && nearestPac.typeId !== type.DEAD && ((utils.isAdjacent(pac.x, pac.y, nearestPac.x, nearestPac.y) || nearestPacDist <= 2) && utils.haveToSwitch(pac, nearestPac))) {
                // Switch type from ennemy.
                moves.push(utils.newType(nearestPac.typeId, pac));
            } else if (pac.abilityCooldown > 0 && !nearestPac.mine && nearestPac.typeId !== type.DEAD && nearestPac.abilityCooldown > 1 && ((utils.isAdjacent(pac.x, pac.y, nearestPac.x, nearestPac.y) || nearestPacDist <= 2) && utils.haveToSwitch(pac, nearestPac))
                || (nearestPacDist <= 2 & nearestPac.mine && !nearestPac.stop)) {
                // Manage collision with PACS, get another pellet location.
                pac.stop = true;
                moves.push(utils.nextMove(grid, pac, nearestPac));            
            } else {
                if (firstLoop) {
                    moves.push(utils.firstMove(grid, myPacs, pac));
                } else {
                    moves.push(utils.nextMove(grid, pac));
                }
            } 
        }
    });
    firstLoop = false;
    console.log(moves.join(' | '));
} 