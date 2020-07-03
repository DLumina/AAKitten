package org.DLumina.bukkitplugin.AAKitten;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;
import org.bukkit.potion.PotionEffect;

public class SearchDestroy implements Runnable {
	//主进程
	//搜索所有的猫，检测多方块结构，检测范围内敌对实体，然后射击
	//索敌范围：以猫为中心，32m为水平距离半径，视线范围（俯角不超过arctg0.，仰角无上限）内的所有实体都可以被锁定
	private Set<String> absence_worlds = new HashSet<String>();
	private Set<String> enemies_list = new HashSet<String>();
	double consume_fish = 0.1;
	double attack_speed = 4;//主时钟每过多少循环攻击一次
	
	private static double attack_count = 0;//时钟计数
	
	public SearchDestroy(Set<String> absence_worlds, Set<String> enemies_list, double consume_fish, double attack_speed) {
		this.absence_worlds = absence_worlds;
		this.enemies_list = enemies_list;
		this.consume_fish = consume_fish;
		this.attack_speed = attack_speed;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		//System.out.println("Mainthread running");
		List<Cat> all_cats;
		for(World w : Bukkit.getServer().getWorlds()) {
			if (absence_worlds.contains(w.getName().toLowerCase()))
				continue;
			//炮塔失效的世界
			all_cats = (List<Cat>) w.getEntitiesByClass(org.bukkit.entity.Cat.class);
			for (org.bukkit.entity.Cat cat : all_cats) {
				if((cat.getCustomName()==null)||(cat.isSitting()==false))
					continue;
				//没命名的猫不要，站着的不要
				//System.out.println("A cat");
				Block curBlock = cat.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
				if(isContainer(curBlock)==false)
					continue;
				//得是能装下所需物品的容器
				if (!(
						(curBlock.getRelative(org.bukkit.block.BlockFace.EAST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.WEST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.NORTH).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.SOUTH).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.NORTH_EAST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.NORTH_WEST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.SOUTH_EAST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.SOUTH_WEST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						))
					continue;
				//周围得是八个台阶构成底座
				//(curBlock.getRelative(org.bukkit.block.BlockFace.SOUTH_WEST).getBlockData().getMaterial()==org.bukkit.Material.POLISHED_ANDESITE_STAIRS)
				//System.out.println("Multiblock");
				Location celoc = cat.getEyeLocation();//猫眼睛的位置
				double x0 = celoc.getX();
				double y0 = celoc.getY();
				double z0 = celoc.getZ();
				///猫的xyz坐标
				Inventory inv = ((Container)(curBlock.getState())).getInventory();
				if(
						!((inv.contains(org.bukkit.Material.ARROW))||(inv.contains(org.bukkit.Material.GUNPOWDER))||(inv.contains(org.bukkit.Material.ICE)))
						||
						!((inv.contains(org.bukkit.Material.COD))||(inv.contains(org.bukkit.Material.TROPICAL_FISH)))
						) {
					DustOptions dustOptions = new DustOptions(org.bukkit.Color.RED, 1);
					for(double i=0;i<12;i++)
						w.spawnParticle(org.bukkit.Particle.REDSTONE, x0 + 0.5*Math.sin(i*3.1416*2/12), y0+1, z0 + 0.5*Math.cos(i*3.1416*2/12), 1, dustOptions);
					continue;
				}
				//得有作为子弹的箭/火药/冰，以及作为动力的小鱼干，如果没有就显示红色粒子，然后收工
				//System.out.println("All OK");
				Chunk curChunk = curBlock.getChunk();
				int cx = curChunk.getX();
				int cz = curChunk.getZ();
				//区块的xz坐标
				double x = 0, y = 0, z = 0;//目标到猫的相对坐标
				double Hdis = -1L;//实体到猫的水平距离的平方
				double dis = 10000L;//实体到猫的距离的平方
				double dist = 20000L;//临时变量
				Entity target = cat;//目标实体，初始化到猫身上做标志
				List<Entity> all_entities = new ArrayList<Entity>();//待定的目标列表
				Entity[] temp = null;
				Location eloc;//目标位置，目标眼睛位置
				//all_entities = curChunk.getEntities();
				temp = curChunk.getEntities();
				for (Entity e : temp) {
					all_entities.add(e);
				}
				//获取本区块的所有实体，这批实体肯定在索敌范围内
				//索敌半径是固定的32，搜索最近25个区块的所有实体
				for (Entity e : all_entities) {
					if(!(enemies_list.contains(e.getType().getName().toLowerCase())))
						continue;
					//不在索敌清单里面就退出
					
					if (e instanceof Player) {
						//如果是玩家？那么就需要检测苦力怕脑袋和纸了
						boolean f = false;//标志变量
						if(inv.contains(org.bukkit.Material.CREEPER_HEAD)){
							for(int i=0;i<=26;i++) {
								//遍历整个物品栏
								if((inv.getItem(i)!=null)
										&&(inv.getItem(i).getType()==org.bukkit.Material.CREEPER_HEAD)
										&&(inv.getItem(i).getItemMeta().hasDisplayName()==true)
										&&(inv.getItem(i).getItemMeta().getDisplayName().equals( ((Player)e).getName() ))){
									f = true;
									break;
									//这里有个问题，如果这一格是空的，会报错。怎么检测这一格是不是空的呢
								}
							}
							if(f==false)
								continue;
						}
						else if(inv.contains(org.bukkit.Material.PAPER)) {
							for(int i=0;i<=26;i++) {
								//遍历整个物品栏
								if((inv.getItem(i)!=null)
										&&(inv.getItem(i).getType()==org.bukkit.Material.PAPER)
										&&(inv.getItem(i).getItemMeta().hasDisplayName()==true)
										&&(inv.getItem(i).getItemMeta().getDisplayName().equals( ((Player)e).getName() ))){
									f = true;
									break;
								}
							}
							if(f==true)
								continue;
						}
						else
							continue;
					}
					//以下计算到猫最近的，水平距离32m内的敌对实体
					//System.out.println("find enemy");
					eloc = e.getLocation();
					x = eloc.getX()-x0;
					y = eloc.getY()-y0;
					z = eloc.getZ()-z0;
					Hdis = x*x + z*z;
					if((y<0)&&(Hdis<(-y*2)))
							continue;
					//苏系炮台，永不低头（指不打过低实体）
					//System.out.println("find enemy1");
					dist = Hdis+y*y;
					if((dist<dis)&&
							(w.rayTraceBlocks(celoc, new Vector(x,y,z), Math.sqrt(dist), org.bukkit.FluidCollisionMode.NEVER, true) == null)){
						//打最近那个
						//检查视线碰撞，忽略所有液体，以及没有碰撞箱但是有可被选中区域的方块，如高草和告示牌
						dis = dist;
						target = e;
						//System.out.println("enemy locked");
					}
					else {
						Vector toEntityEye = ((LivingEntity) e).getEyeLocation().toVector().subtract(celoc.toVector());
						if(w.rayTraceBlocks(celoc, toEntityEye, toEntityEye.length(), org.bukkit.FluidCollisionMode.NEVER, true) == null) {
							dis = dist;
							target = e;
							//看不到脚，看到眼睛也可以打
						}
					}
				}
				//当前区块搜索
				
				
				all_entities.clear();//清空搜索清单
				if(target != cat) {
					//当前区块找到目标了，最外一圈区块不搜索
					for(int i=-1;i<=1;i++) {
						for(int j=-1;j<=1;j++) {
							if(i==0&&j==0)
								continue;
							temp = w.getChunkAt(cx+i,cz+j).getEntities();
							for (Entity e : temp) {
								all_entities.add(e);
							}
							//all_entities.addAll(0, w.getChunkAt(cx+i,cz+j).getEntities());
						}
					}
				}
				else {
					for(int i=-2;i<=2;i++) {
						for(int j=-2;j<=2;j++) {
							if(i==0&&j==0)
								continue;
							temp = w.getChunkAt(cx+i,cz+j).getEntities();
							for (Entity e : temp) {
								all_entities.add(e);
							}
							//all_entities.addAll(0, w.getChunkAt(cx+i,cz+j).getEntities());
						}
					}
				}
				for (Entity e : all_entities) {
					if(!(enemies_list.contains(e.getName().toLowerCase())))
						continue;
					//同上，不在索敌清单里面就退出
					eloc = e.getLocation();
					x = eloc.getX()-x0;
					y = eloc.getY()-y0;
					z = eloc.getZ()-z0;
					Hdis = x*x + z*z;
					if((y<0)&&((Hdis>1024)||(Hdis<(-y*2))))
							continue;
					//永不低头二度
					//这里多一条是不是在32m外的判定
					dist = Hdis+y*y;
					if(dist<dis&&
							w.rayTraceBlocks(cat.getEyeLocation(), new Vector(x,y,z), Math.sqrt(dist), org.bukkit.FluidCollisionMode.NEVER, true) == null) {
						//同上，检查距离和视线
						dis = dist;
						target = e;
					}
					else {
						Vector toEntityEye = ((LivingEntity) e).getEyeLocation().toVector().subtract(celoc.toVector());
						if(w.rayTraceBlocks(celoc, toEntityEye, toEntityEye.length(), org.bukkit.FluidCollisionMode.NEVER, true) == null) {
							dis = dist;
							target = e;
							//同上，看到眼睛也可以打
						}
					}
				}
				//外圈搜索
				
				if(target == cat) {
					DustOptions dustOptions = new DustOptions(org.bukkit.Color.GREEN, 1);
					for(double i=0;i<12;i++)
						w.spawnParticle(org.bukkit.Particle.REDSTONE, x0 + 0.5*Math.sin(i*3.1416*2/12), y0+1, z0 + 0.5*Math.cos(i*3.1416*2/12), 1, dustOptions);
					continue;
				}
				//这个猫没找到敌对实体，但是正常工作，显示绿色，结束
				
				
				DustOptions dustOptions = new DustOptions(org.bukkit.Color.BLUE, 1);
				for(double i=0;i<12;i++)
					w.spawnParticle(org.bukkit.Particle.REDSTONE, x0 + 0.5*Math.sin(i*3.1416*2/12), y0+1, z0 + 0.5*Math.cos(i*3.1416*2/12), 1, dustOptions);
				//索敌成功，显示蓝色
				attack_count += 1;
				if(attack_count < attack_speed) {
					//System.out.println("count down");
					continue;
				}//攻击周期没到，不攻击
				attack_count = 0;
				//攻击周期到了
				Location tloc = target.getLocation();
				x = tloc.getX()-x0;
				y = tloc.getY()-y0;
				z = tloc.getZ()-z0;
				dis = Math.sqrt(x*x+y*y+z*z);//注意从这里往后dis不再是距离平方了，就是距离本身
				//System.out.println("hit");
				if(inv.contains(org.bukkit.Material.ICE)) {
					//有冰，优先用冰攻击，施加缓慢粒子云
					//System.out.println("ice");
					inv.removeItem(new ItemStack(Material.ICE));
					//拿走一块冰
					if(Math.random()<consume_fish) {
						//猫猫饿了，猫猫吃小鱼干
						if(inv.contains(org.bukkit.Material.COD))
							inv.removeItem(new ItemStack(Material.COD));
						else
							inv.removeItem(new ItemStack(Material.TROPICAL_FISH));
					}
					ItemStack itemCrackData = new ItemStack(Material.ICE);
					for(double i=0;i<dis*5;i++)
						w.spawnParticle(org.bukkit.Particle.ITEM_CRACK, x0 + x*i/5, y0 + y*i/5, z0 + z*i/5, 1, itemCrackData);
					//暂时先这么写着，瞬间打击，以后再考虑弹道设计
					PotionEffect slow = new PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 100, 10);
					((LivingEntity)target).removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
					((LivingEntity)target).addPotionEffect(slow);
					//System.out.println("ice hit");
					for(Entity en : target.getNearbyEntities(1.5, 1.5, 1.5)) {
						if (en instanceof LivingEntity) {
							((LivingEntity)en).removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
							((LivingEntity)en).addPotionEffect(slow);
							//System.out.println("ice hit1");
						}
							
					}
				}
				else if(inv.contains(org.bukkit.Material.GUNPOWDER)){
					//有火药，其次用火药攻击，制造爆炸
					//System.out.println("gunpow");
					inv.removeItem(new ItemStack(Material.GUNPOWDER));
					//拿走一个火药
					if(Math.random()<consume_fish) {
						//猫猫饿了，猫猫吃小鱼干
						if(inv.contains(org.bukkit.Material.COD))
							inv.removeItem(new ItemStack(Material.COD));
						else
							inv.removeItem(new ItemStack(Material.TROPICAL_FISH));
					}
					for(double i=0;i<dis*5;i++)
						w.spawnParticle(org.bukkit.Particle.FLAME, x0 + x*i/5, y0 + y*i/5, z0 + z*i/5, 1);
					w.createExplosion(x+x0, y+y0, z+z0, 3, false, false, cat);
					//System.out.println("boom");
					//3级爆炸（tnt是4级），不产生火焰，不破坏方块，人头给猫
				}
				else {
					//只有箭可以吗
					//System.out.println("arrow");
					inv.removeItem(new ItemStack(Material.ARROW));
					//拿走一个箭
					if(Math.random()<consume_fish) {
						//猫猫饿了，猫猫吃小鱼干
						if(inv.contains(org.bukkit.Material.COD))
							inv.removeItem(new ItemStack(Material.COD));
						else
							inv.removeItem(new ItemStack(Material.TROPICAL_FISH));
					}
					for(double i=0;i<dis*5;i++)
						w.spawnParticle(org.bukkit.Particle.END_ROD, x0 + x*i/5, y0 + y*i/5, z0 + z*i/5, 1);
					((Damageable)target).damage(8,cat);
					//System.out.println("arrow hit");
					//造成8点伤害，幻翼有20生命
				}
				//找到敌对实体了，攻击
			}
		}
	}
	private static boolean isContainer(Block block) {
		Material mat = block.getType();
        return mat == Material.BARREL || mat == Material.CHEST || mat == Material.SHULKER_BOX || mat == Material.DROPPER || mat == Material.DISPENSER || mat == Material.HOPPER;
	}
}
