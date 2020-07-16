import sys
import math
import random
import datetime


class Pall:
    def __init__(self, value, x, y):
        self.value = value
        self.x = x
        self.y = y
        self.voisins = []
        self.isLast = False
        self.objectif = []
        # self.checked 
    
    def __str__(self):
        return "value = {}, position = ({}, {})".format(self.value, self.x, self.y)
    
    def addVoisin(self, pall):
        self.voisins.append(pall)
    
    def update(self, value):
        self.value = value
    
    def setLast(self):
        if len(self.voisins) == 1:
            self.isLast = True

    def getValue(self):
        return self.value

class Pac:
    def __init__(self, id, x, y, type_pac, speed_left, cooldown):
        self.id = id
        self.x = x
        self.y = y
        self.type_pac = type_pac
        self.speed_left = speed_left
        self.cooldown = cooldown
        self.is_update = True
        self.chemin = [[x, y]]
        self.next = []
        self.next_move = []
        self.vis_pac = []
        self.type_move = ""
        self.without_ability = 0
        self.cannot_see = []
        self.objectif = []
    
    def __str__(self):
        return "pacId = {}, position = ({}, {})".format(self.id, self.x, self.y)

    def add_chemin(self, x, y):
        self.chemin.append([x, y])
    def del_chemin(self):
        self.chemin.pop()

    def update(self, x, y, type_pac, speed_left, cooldown):
        lab[self.next[1]][self.next[0]].update(0)
        set_checked(self.next[0], self.next[1])
        self.x = x
        self.y = y
        self.type_pac = type_pac
        self.speed_left = speed_left
        self.cooldown = cooldown
        self.chemin = [[x, y]]
        self.is_update = True
        self.vis_pac = []
        self.without_ability += 1
        self.cannot_see = []

def winner_type(my_type, e_type):
    if e_type == "PAPER" and my_type != "SCISSORS":
        return False, "SCISSORS"
    elif e_type == "ROCK" and my_type != "PAPER":
        return False, "PAPER"
    elif e_type == "SCISSORS" and my_type != "ROCK":
        return False, "ROCK"
    else:
        return True, my_type



# Grab the pellets as fast as you can!

# width: size of the grid
# height: top left corner is (x=0, y=0)

unchecked = []
start_time = datetime.datetime.now()
width, height = [int(i) for i in input().split()]
lab = []
for i in range(height):
    row = input()  # one line of the grid: space " " is floor, pound "#" is wall
    row_lab = []
    for j in range(width):
        if row[j] == " ":
            pall = Pall(1, j, i)
            row_lab.append(pall)
            # row_lab.append(0)
        else:
            pall = Pall(-100, j, i)
            row_lab.append(pall)
        
    # print("{}".format([str(pall.value) for pall in row_lab]), file=sys.stderr)
    lab.append(row_lab)

for i in range(height):
    for j in range(width):
        if lab[i][j].getValue() != -100:
            pal = lab[i][j]
            x, y = pal.x, pal.y
            xplus, xmoins, yplus, ymoins = x+1, x-1, y+1, y-1
            if x == 0:
                xmoins = width -1
            elif x == width - 1:
                xplus = 0

            if lab[y][xplus].getValue() != -100:
                pal.addVoisin(lab[y][xplus])
            if lab[y][xmoins].getValue() != -100:
                pal.addVoisin(lab[y][xmoins])
            if lab[y+1][x].getValue() != -100:
                pal.addVoisin(lab[y+1][x])
            if lab[y-1][x].getValue() != -100:
                pal.addVoisin(lab[y-1][x])
            lab[i][j].setLast()

x_zone, y_zone = 0, 0
while x_zone + 4 < width - 1:
    y_zone = 0
    while y_zone + 4 < height - 1:
        unchecked.append([True, [x_zone, y_zone], [x_zone + 3, y_zone + 3]])
        y_zone += 4
    unchecked.append([True, [x_zone, y_zone], [x_zone + 3, height - 1]])
    x_zone += 4
unchecked.append([True, [x_zone, y_zone], [width - 1, height - 1]])


     

    # print("row : {}".format(row), file=sys.stderr)
    # print("row : {}".format(row_lab), file=sys.stderr)

def affiche(lab):
    n = len(lab)
    for i in range(n):
        for j in range(len(lab[i])):
                print(type(lab[i][j]), file=sys.stderr, end=", ")
            # if type(lab[i][j] == int):
            #     print("INT", file=sys.stderr, end=", ")
            # else:
            #     print("{}".format(lab[i][j].value), file=sys.stderr, end=", ")
        print("\n", file=sys.stderr)

def action(my_pac):
    # MOVE <pacId> <x> <y>
    inter = datetime.datetime.now() - start_time
    print("time before move : {}".format(inter.microseconds), file=sys.stderr)
    final = ""
    for pac in my_pac.values():
        print("next move : {}".format(pac.next_move), file=sys.stderr)
        final += pac.type_move
    print(final, file=sys.stderr)
    print(final[:-1])

def let_speed(my_pac):
    final = ""
    for pac in my_pac.values():
        final += "SPEED " + str(pac.id) + "|"
    print(final[:-1])



def minimax(lab, pac, my_pac, ennemy_pac, depth, max_depth):
    x = pac.chemin[-1][0]
    y = pac.chemin[-1][1]
    switch = False
    next_next = []

    # if x== 28 and y == 5:
    #     print("big pastille : {}".format(lab[y][x]), file=sys.stderr)

    if depth == 0:
        return lab[y][x].getValue(), next_next, False
    
    if depth == max_depth and lab[y][x].isLast:
        return 1000, [x, y], False

    
    for other_pac in my_pac.values():
        if other_pac.id != pac.id and other_pac.x == x and other_pac.y == y:

            return -10 * depth, [], False

    best_score = -99
    score = 0
    best_next = []
    for voisin in lab[y][x].voisins:

        
        
        next_move = voisin
        pac.chemin.append([voisin.x, voisin.y])
        score, pot_next_next, switch = minimax(lab, pac, my_pac, ennemy_pac, depth-1, max_depth)
        # best_score = max(score, best_score)
        if score > best_score:
           best_score = score
           next_next = pot_next_next
        pac.chemin.pop()
    
    if depth == max_depth - 1 and pac.speed_left > 0:  
        if [x, y] in pac.chemin[:-1]:
            best_score -= 10
        next_next = [x, y]
        # print("value : {}, best_score : {} for next {}".format(lab[y][x].value, best_score, next_next), file=sys.stderr)
    
    if depth == max_depth and next_next == []:
        next_next = [x, y]
        # print("value : {}, best_score : {} for next {}".format(lab[y][x].value, best_score, next_next), file=sys.stderr)

    

    if pac.speed_left > 0 and depth >= max_depth - 2:
        # print("chemin : {}".format(pac.chemin), file=sys.stderr)
        # print("next_next : {}".format([x, y]), file=sys.stderr)
        for e_pac in pac.vis_pac:
            if e_pac.x == x and e_pac.y == y:
                print("ennemy detected : {}".format(e_pac), file=sys.stderr)
                win, next_type = winner_type(pac.type_pac, e_pac.type_pac)
                if win and e_pac.cooldown > 1:
                    # print("I WIN !!!", file=sys.stderr)
                    return 20 * depth + best_score, [x, y], False
                # elif pac.cooldown == 0 and (not win) and e_pac.cooldown > 1:
                #     pac.type_pac = next_type
                #     print("I WILL WIN WITH {}!!!".format(next_type), file=sys.stderr)
                #     return 20 + best_score, [x, y], True
                return -20, [], False

    elif pac.speed_left == 0 and depth == max_depth:
        for e_pac in pac.vis_pac:
            if e_pac.x == x and e_pac.y == y:
                print("ennemy detected : {}".format(e_pac), file=sys.stderr)
                win, next_type = winner_type(pac.type_pac, e_pac.type_pac)
                if win and e_pac.cooldown > 1:
                    # print("I WIN !!!", file=sys.stderr)
                    return 20 * depth + best_score, [x, y], False
                # elif pac.cooldown == 0 and (not win) and e_pac.cooldown > 1:
                #     pac.type_pac = next_type
                #     print("I WILL WIN WITH {}!!!".format(next_type), file=sys.stderr)
                #     return 20 + best_score, [x, y], True
                return -20, [], False
            
         
    
    # print("score : {}, position : {}, depth : {}".format(lab[y][x].value + best_score, [x, y], depth), file=sys.stderr)
    return lab[y][x].getValue() * depth + best_score, next_next, False

def is_equal(list1, list2):
    # print("test1 : {}, test2 : {}".format(chemin), file=sys.stderr)
    return list1[0] == list2[0] and list1[1] == list2[1]

def find_zone():
    index = random.randint(0, len(unchecked) - 1)
    zone = unchecked[index]
    while not zone[0]:
        index = random.randint(0, len(unchecked) - 1)
        zone = unchecked[index]
    for x in range(zone[1][0], zone[2][0] + 1):
        for y in range(zone[1][1], zone[2][1] + 1):
            if lab[y][x].getValue() != -100:
                return [x, y]
    return []

def best_move(pac, lab, my_pac, ennemy_pac, other):
    print("===================================", file=sys.stderr)
    print("pac_id : {}".format(str(pac)), file=sys.stderr)
    
    if len(my_pac) < 3:
        if turn == 1:
            depth = 15
        depth = 9
    elif len(my_pac) < 5:
        if turn == 1:
            depth = 11
        else:
            depth = 8
    else:
        if turn == 1:
            depth = 11
        else:
            depth = 5

    best_score = -99
    # pac = voisin[0]
    x = pac.x
    y = pac.y
    voisins = lab[y][x].voisins
    n = len(voisins)
    best_next = []
    score = 0
    pac.chemin = [[x, y]]
    final_switch = False
    # print("chemin vérif : {}".format(chemin), file=sys.stderr)
    # if len(chemin) >= 4 and is_equal(chemin[-1], chemin[-2]) and is_equal(chemin[-1], chemin[-3]):
    #     pac.next_move = chemin[-4]
    # else:
    #if n == 1:
    #    x_next = voisin[1][0]
    #    y_next = voisin[1][1]
    #    move(pac[0], x_next, y_next)
    for i in range(n):
        next_move = voisins[i]
        pac.next_chemin = []
        # pac.next_chemin.append([next_move[0], next_move[1])
        # next_pac = [pac[0], next_move[0], next_move[1], pac[3], pac[4], pac[5]]
        #lab[next_move[1]][next_move[0]] = 0        
        pac.chemin.append([next_move.x, next_move.y])
        print("voisins : {}".format(str(voisins[i])), file=sys.stderr)

        inter1 = datetime.datetime.now()

        score, next_next, switch = minimax(lab, pac, my_pac, ennemy_pac, depth, depth)

        inter = datetime.datetime.now() - inter1
        print("time after minimax : {}, depth : {}".format(inter.microseconds, depth), file=sys.stderr)
        pac.chemin.pop()
        # print("score = {} for position {}, next = {}, cannot see : {}".format(score, [next_move.x, next_move.y], next_next, pac.cannot_see), file=sys.stderr)
        # print("chemin = {}".format(pac.next_chemin), file=sys.stderr)
        if score > best_score and next_next != [] and (next_next[0] != pac.x or next_next[1] != pac.y) and next_next not in pac.cannot_see:
            best_next = [next_next[0], next_next[1]]
            pac.next = [next_move.x, next_move.y]
            best_score = score
            final_switch = switch
        elif score == best_score:
            rand = random.random()
            if rand < 0.5:
                best_next = [next_next[0], next_next[1]]
                pac.next = [next_move.x, next_move.y]
                best_score = score


    print("best_score : {} for next {}".format(best_score, best_next), file=sys.stderr)
    if best_score <= 0:
        
        if pac.objectif != []:
            print("pac : {}, objectif {}".format(pac.id, pac.objectif), file=sys.stderr)
            pac.next_move = pac.objectif[:]
        else:
            pac.next_move = find_zone()
            pac.objectif = pac.next_move[:]
            if pac.next_move == []:
                pac.next_move = pac.chemin[-1]


    #     else:
    #         x_next, y_next = random.randint(0, width), random.randint(1, height)
    #         while lab[y_next][x_next].value == -100:
    #             x_next, y_next = random.randint(0, width), random.randint(1, height)
    #     pac.next_move = [x_next, y_next]
    elif best_next == []:
        pac.next_move = pac.chemin[-1]
    else:
        pac.next_move = best_next[:]

    
    if pac.cooldown == 0:
        pac.without_ability += 1
    
    for other_pac in my_pac.values():
        other_pac.cannot_see.append(next_move)
        other_pac.cannot_see.append(pac.next)
    pac.type_move = "MOVE " + str(pac.id) + " " + str(pac.next_move[0]) + " " + str(pac.next_move[1]) + "|" 


def distance(x, y, x1, y1):
    return abs(x1-x) + abs(y1-y)

def update_pall(pac, lab, other):
    x_test, y_test = pac.x, pac.y
    pall = lab[y_test][x_test]
    while pall.getValue() != -100:
        if [x_test, y_test] not in other:
            pall.update(0)
        if x_test == width-1:
            x_test = -1
        x_test += 1
        pall = lab[y_test][x_test]
    
    x_test = pac.x
    while pall.getValue() != -100:
        if [x_test, y_test] not in other:
            pall.update(0)
        if x_test == 0:
            x_test = width
        x_test -= 1
        pall = lab[y_test][x_test]
    
    x_test, y_test = pac.x, pac.y + 1
    while pall.getValue() != -100:
        if [x_test, y_test] not in other:
            pall.update(0)
        y_test += 1
        pall = lab[y_test][x_test]
    
    y_test = pac.y - 1
    while pall.getValue() != -100:
        if [x_test, y_test] not in other:
            pall.update(0)
        y_test -= 1
        pall = lab[y_test][x_test]



def set_checked(x, y):
    for zone in unchecked:
        x_zone, y_zone = zone[1][0], zone[1][0]
        x_zone2, y_zone2 = zone[2][0], zone[2][0]
        if zone[0] and x >= x_zone and x <= x_zone2 and y >= y_zone and y <= y_zone2:
            zone[0] = False



#affiche(lab)
x_pre, y_pre = -1, -1
cooldown_speed = 0
turn = 1
my_pac = {}

# game loop
while True:
    best_points = []
    other_points = []
    my_score, opponent_score = [int(i) for i in input().split()]
    if turn != 1:
        start_time = datetime.datetime.now()
    visible_pac_count = int(input())  # all your pacs and enemy pacs in sight
    ennemy_pac = {}
    speeds = 0

    if any(my_pac):
        for pac in my_pac.values():
            pac.is_update = False

    for i in range(visible_pac_count):
        # pac_id: pac number (unique within a team)
        # mine: true if this pac is yours
        # x: position in the grid
        # y: position in the grid
        # type_id: unused in wood leagues
        # speed_turns_left: unused in wood leagues
        # ability_cooldown: unused in wood leagues
        pac_id, mine, x, y, type_id, speed_turns_left, ability_cooldown = input().split()
        pac_id = int(pac_id)
        mine = mine != "0"
        x = int(x)
        y = int(y)
        speed_turns_left = int(speed_turns_left)
        ability_cooldown = int(ability_cooldown)
        if mine:
            if turn == 1:
                my_pac[pac_id] = Pac(pac_id, x, y, type_id, speed_turns_left, ability_cooldown)
                x_mid = (width - 1) / 2
                set_checked(2 * x_mid - x, y)
                print("my_pac : {}, cooldown : {}".format(my_pac[pac_id], ability_cooldown), file=sys.stderr)
            elif pac_id in my_pac:
                my_pac[pac_id].update(x, y, type_id, speed_turns_left, ability_cooldown)
                print("my_pac : {}, cooldown : {}".format(my_pac[pac_id], ability_cooldown), file=sys.stderr)
                last_x, last_y = my_pac[pac_id].chemin[-1][0], my_pac[pac_id].chemin[-1][1]
                lab[last_y][last_x].update(0)
                if pac.objectif != [] and x == pac.objectif[0] and y == pac.objectif[1]:
                    print("j'ai atteint l'objectif : {}".format(my_pac[pac_id].objectif), file=sys.stderr)
                    pac.objectif = []
            set_checked(x, y)
            if lab[y][x].isLast:
                my_pac[pac_id].objectif = find_zone()
                print("je suis sur un last, objectif : {}".format(my_pac[pac_id].objectif), file=sys.stderr)
        else:
            if type_id != "DEAD":
                ennemy_pac[pac_id] = Pac(pac_id, x, y, type_id, speed_turns_left, ability_cooldown)
        lab[y][x].update(0)
    
    for pac in my_pac.values():
        for e_pac in ennemy_pac.values():
            if pac.x == e_pac.x or pac.y == e_pac.y:
                pac.vis_pac.append(e_pac)

    
    visible_pellet_count = int(input())  # all pellets in sight
    for i in range(visible_pellet_count):
        # value: amount of points this pellet is worth
        x, y, value = [int(j) for j in input().split()]
        lab[y][x].update(value)
        if value == 10:
            best_points.append(lab[y][x])
        other_points.append([lab[y][x].x, lab[y][x].y])
        #print("x : {}, y : {}".format(x, y), file=sys.stderr)
    #affiche(lab)

    # print("ennemy : {}".format([str(pac) for pac in ennemy_pac.values()]), file=sys.stderr)
    print("best : {}".format([str(points) for points in best_points]), file=sys.stderr)
    # print("speeds : {}".format(speeds), file=sys.stderr)

    moves = []
    id_to_del = []
    n_pac = len(my_pac)
    # for voisin, chemin in zip(voisins, chemins):

    for pac in my_pac.values():
        print("pac : {}".format(str(pac)), file=sys.stderr)
        update_pall(pac, lab, other_points)
        if pac.type_pac == "DEAD":
            id_to_del.append(pac.id)
            # del my_pac[pac.id]
        # elif pac.cooldown == 0 and turn == 2 or pac.without_ability > 3:
        elif pac.cooldown == 0 and turn > 1:
            print("BOOST {}".format(pac.cooldown), file=sys.stderr)
            pac.type_move = "SPEED " + str(pac.id) + "|"
            pac.without_ability = 0 
        else:
            if pac.objectif != []:
                count = 0
                for voisin in lab[pac.y][pac.x].voisins:
                    count += voisin.getValue()
                    print("voisins {}".format(voisin), file=sys.stderr)
                if count > 0:
                    pac.objectif = []
                else:
                    pac.next_move = pac.objectif[:]
                    pac.type_move = "MOVE " + str(pac.id) + " " + str(pac.next_move[0]) + " " + str(pac.next_move[1]) + "|"
            if pac.objectif == []:
                x, y, = pac.x, pac.y
                # print("voisins : {}".format(lab[y][x].voisins), file=sys.stderr)
                inter = datetime.datetime.now() - start_time
                print("time before best move : {}".format(inter.microseconds), file=sys.stderr)
                best_move(pac, lab, my_pac, ennemy_pac, other_points)
                # moves.append(pac)
    
    for id_p in id_to_del:
        del my_pac[id_p]
    # print("moves : {}".format(moves), file=sys.stderr)
    turn += 1
    action(my_pac)






    # Write an action using print
    # To debug: print("Debug messages...", file=sys.stderr)

