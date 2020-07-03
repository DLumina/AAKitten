## Anti-Aircraft Kitten

or *For He Can Creep*

放在多方块结构（中心一个容器，周围一圈8个任意台阶）上的，已命名且坐下的猫，会主动从身下的容器里面抽取弹药，攻击周围（水平距离32m以内，且猫猫肉眼可见）的敌对生物。

弹药种类：冰（无伤害，xyz4m范围内群体2.5秒缓慢10）/火药（3级爆炸）/箭矢（8点伤害）

可能会同时消耗小鱼干。

如果你在索敌列表里面加入了玩家的话，猫会搜索箱子里面的苦力怕头颅，然后攻击和头颅同名的玩家。如果没有苦力怕头颅，又有纸的话，就攻击和纸名字不一样的玩家。作者友情提示：在用纸设置白名单的时候请把你自己的名字最先放进去，否则可能在开着gui的时候死于非命。

如果炮台完整但是缺货，猫的头上会显示红色的圈圈。如果正常工作显示绿色。如果锁定了目标则显示蓝色。

配置文件：

* 猫猫不上班的世界列表（默认空）
* 索敌列表（默认为幻翼&苦力怕&蜘蛛&僵尸及其变种&玩家）
* 每次攻击消耗小鱼干的机率（默认0.1）
* 主时钟周期（如果你不清楚你在做什么，不要动这个数字，默认20tick，即1秒）
* 攻击周期（整数，表示主时钟转几次攻击一次，和主时钟周期相乘得到攻击间隔，默认2）。

----

When sitting on a multiblock structure (any container surrounded by 8 any stairs), a named cat will use the bullets in the container to attack nearby enemies. Notice that they only attack the enemies they can see.

max distance: 32m horizonal

bullets: ice(4\*4\*4 area slow 10, 2.5s) / gunpowder(explosion lv.3) / arrow(8 damage)

may consume fish by the way.

If “player” is in the attack list, then the cat will search for creeper head in the container, then attack the players have the same name with that item. If there is no creeper head but paper, then the cat will attack any player into its view, unless the player has the same name with paper.

If the turret is complete but there is no bullet & fish in the container, the kitten would have a red ring on its head. If it is properly working but with no enemies found, then green. If there is enemy locked, blue.

Configuration:

* dimensions that kitten won’t work
* entities that cat will attack (default: phantom, creeper, spider, any kind of zombies, player)
* chance consuming fish per hit
* main clock
* attack interval (integer, represents attack per main threads. You can multiply this with main clock to get the attack interval by tick)